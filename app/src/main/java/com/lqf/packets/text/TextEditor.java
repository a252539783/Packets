package com.lqf.packets.text;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2017/11/1.
 * 处理文本内容
 */

public class TextEditor {
    private byte[] mSource=null;
    private String mSSource=null;

    //单独线程来进行缓存
    private static CacheThread mThread=null;

    //文本按页进行划分，每页大小即mSegmentLength
    private static int mSegmentLength =1000;

    //实际缓存到内存中的页数量.
    // 因为如果要打开的文件过大，那么就不可能一次性将文件所有内容加载进内存，这里保证任何时候只有mCacheSize个页加载.
    private static int mCacheSize=4;

    //保证mCurrent距离mRoot大于mStartLength，距离mEnd大于mEndLength，否则会出现mCurrent在获取时还未加载的情况
    //但实际上这样也无法保证加载能够及时，如果获取获取频率太高跨度过大仍然会出现问题，当前策略只能保证非极端情况的完好
    //极端情况下还是需要主动通知界面更新的方式才能保证获取的正确性
    private static int mEndLength=1;
    private static int mStartLength=1;

    //目标文本总共有多少页
    private int mSegmentCount=0;

    //mRoot为已加载的第一页，mEnd为已加载的最后一页
    private InputItem mRoot,mEnd;
    private int mStartIndex = 0, mEndIndex;    //mRoot和mEnd的Index，其实没多大用，可以有替代
    private int[] mCurrentIndex = new int[2]; //目前没用

    //当前指向的页.当要获取文本时，移动mCurrent到目标文本所在页上，然后进行文本获取
    private InputItem mCurrent;

    private SparseArray<InputItem> mAllModified;

    private int mCharCount = 0;   //字符总数

    public TextEditor(byte[] src) {
        mSource=src;
        mSegmentCount=src.length/ mSegmentLength +(src.length% mSegmentLength !=0?1:0);

        //第一次要加载的页数。由于是直接给了byte源，所以就不需要缓存了，直接用就行
        int count=mSegmentCount<=mCacheSize?mSegmentCount:mCacheSize;
        InputItem item=mRoot=new InputItem(src,0,src.length<= mSegmentLength ?src.length: mSegmentLength,0);
        for (int i=1; i<count; i++)
        {
            item.mNext=new InputItem(src,i* mSegmentLength, i==mSegmentCount-1?src.length%mSegmentLength:mSegmentLength,i);
            item.mNext.mPrevious=item;
            item=item.mNext;
        }

        mEnd=item;
        mCurrent=mRoot;
        mEndIndex=count-1;

        mCharCount=src.length;
        mThread.mSrc=src;
    }

    public TextEditor(FileInfo file)
    {
        mSegmentCount=file.length/ mSegmentLength +(file.length% mSegmentLength !=0?1:0);

        //第一次要加载的页数。
        int count=mSegmentCount<=mCacheSize?mSegmentCount:mCacheSize;
        InputItem item=mRoot=new InputItem(0,file.length<= mSegmentLength ?file.length: mSegmentLength,0);
        for (int i=1; i<count; i++)
        {
            item.mNext=new InputItem(i* mSegmentLength, i==mSegmentCount-1?file.length%mSegmentLength:mSegmentLength,i);
            item.mNext.mPrevious=item;
            item=item.mNext;
        }

        mEnd=item;
        mCurrent=mRoot;
        mEndIndex=count-1;

        mCharCount=file.length;

        mThread.mFile=file;

        item=mRoot;
        for (int i = 0; i < count; i++)       //初始加载
        {
            mThread.cache(mRoot,mEnd,item);
            item=item.mNext;
        }
    }

    public void add(byte[] src)
    {
    }

    public int getCharCount()
    {
        return mCharCount;
    }

    //移动mCurrent,直到mCurrent对应页包含offset位置
    private void select(int id, int offset) {
        mCurrentIndex[id]=offset;

        if (id==0) {
            if (offset >= mCurrent.mOffset) { //目标位置在当前页之后
                while (mCurrent.mOffset + mCurrent.mLength <= offset) { //移动直到当前页覆盖目标

                    mCurrent = mCurrent.mNext;  //向后移一步
                    if (mEndIndex!=mSegmentCount-1&&mEnd.mIndex-mCurrent.mIndex<mEndLength) {
                        mEndIndex++;
                        //mCurrent移动后，让mRoot和mEnd也随着移动一步，
                        if (!mRoot.mModified) {
                            //在mRoot没有被修改时，直接将mEnd更新为mRoot，mRoot后移，更新相关数据即可，避免了创建和销毁无用的对象
                            mRoot.mPrevious=mEnd;
                            mRoot.mIndex=mEnd.mIndex+1;
                            mRoot.mOffset=mEnd.mOffset+mEnd.mLength;
                            mRoot.mLength=mSegmentLength;
                            mEnd.mNext=mRoot;

                            mEnd=mRoot;
                            mRoot=mRoot.mNext;
                            mEnd.mNext=null;

                            mEnd.mPrepared=false;

                        }else {
                            //一旦mRoot发生修改，自然不能直接使用，所以需要再次创建，并保存mRoot
                            //TODO 不过这部分逻辑暂时还不可用
                            mEnd.mNext=new InputItem(mEnd.mOffset+mEnd.mLength,mSegmentLength,mEnd.mIndex+1);
                            mEnd.mNext.mPrevious=mEnd;
                            mEnd=mEnd.mNext;

                            mRoot=mRoot.mNext;
                            mEnd.mNext=null;
                        }

                        //mRoot和mEnd改变，mEnd指向了一个新的页，缓存它
                        //由于在mCurrent移动时也移动了mEnd，所以此时可以保证mCurrent不是mEnd，mCurrent现在应该是个已经加载过的页（除非获取速度极快）
                        mThread.cache(mRoot,mEnd,mEnd);
                    }
                }
            } else if (offset < mCurrent.mOffset) { //目标位置在当前页之前
                while (mCurrent.mOffset > offset) { //移动直到当前页覆盖目标，剩下逻辑基本同上
                    mCurrent = mCurrent.mPrevious;

                    if (mRoot.mOffset!=0&&mCurrent.mIndex-mRoot.mIndex<mStartLength) {
                        if (!mEnd.mModified) {
                            mEnd.mNext=mRoot;
                            mEnd.mIndex=mRoot.mIndex-1;
                            mEnd.mLength=mSegmentLength;
                            mEnd.mOffset=mRoot.mOffset-mEnd.mLength;
                            mRoot.mPrevious=mEnd;

                            mRoot=mEnd;
                            mEnd=mEnd.mPrevious;
                            mRoot.mPrevious=null;

                            mRoot.mPrepared=false;

                        }else {
                            mRoot.mPrevious=new InputItem(mRoot.mOffset-mSegmentLength,mSegmentLength,mRoot.mIndex-1);
                            mRoot.mPrevious.mNext=mRoot;
                            mRoot=mRoot.mPrevious;

                            mEnd=mEnd.mPrevious;
                            mRoot.mPrevious=null;
                        }

                        mThread.cache(mRoot,mEnd,mRoot);
                    }
                }
            }
        }
    }

    /**
     * 获取一个字节（不是字符）
     *
     * @param index
     * @return
     */
    public int getByte(int index) {
        /*if  (index>=mCurrent.mOffset)
        {
            while (true)
            if (index<mCurrent.mOffset+mCurrent.mLength)
            {
                return mCurrent.mResult[index-mCurrent.mOffset];
            }else
            {
                mCurrent=
            }
        }*/
        select(0,index);
        return mCurrent.mResult[index-mCurrent.mOffset];
    }


    /**
     * 表示一个片段中的所有更改
     */
    static class InputItem
    {
        static int DEFAULT_SIZE=16;

        private InputItem mNext,mPrevious;

        int mIndex = 0;   //页编号

        int mOffset = 0;     //相对于源的偏移
        int mOverride = 0;    //暂时没用，预留之后可能的修改操作
        int mLength = 0;      //当前页大小.虽然加载页大小已经固定，但是对于最后一页以及修改过的页，大小还是未知的
        byte[] mResult;    //当前页实际内容

        boolean mModified = false;        //是否被修改过，目前没有修改文本的相关逻辑，所以暂时不管它

        private boolean mPrepared = false;    //暂时没用，也忘了之前想用来干啥了....

        List<InputHistory> mHistory=new LinkedList<>();

        InputItem(byte []src,int offset,int length,int index)
        {
            mOffset =offset;
            mLength=length;
            mOverride=length;
            mResult =new byte[length];
            mIndex= index;
            //为了保证不改动源，复制一份
            System.arraycopy(src,offset, mResult,0,length);
            mPrepared=true;
        }

        InputItem(int offset,int length,int index)
        {
            mOffset =offset;
            mLength=length;
            mOverride=length;
            mIndex=index;
            mResult =new byte[length];
        }

        public void delete(int offset,int length)
        {

        }
    }

    public static class CacheThread extends Thread
    {
        private FileInfo mFile=null;
        private byte[] mSrc=null;

        private boolean mStop=false;

        private H mH;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            mH=new H();
            Looper.loop();
        }

        /**
         * 缓存一个页面
         * @param start
         * @param end
         * @param cache 要缓存的页面，缓存结果会放入该页，该页必须提前给定index值
         */
        public void cache(InputItem start, InputItem end, InputItem cache) {
            while (mH==null);
            mH.mStart=start;
            mH.mEnd=end;
            mH.mCache=cache;
            mH.add(cache);

            mH.sendEmptyMessage(H.CACHE);
        }

        public static void init()
        {
            if (mThread==null) {
                mThread = new CacheThread();
                mThread.start();
            }
        }

        public void quit()
        {
            mH.getLooper().quit();
        }

        private class H extends Handler
        {
            private class Entry
            {
                InputItem item;
                int index;  //用于判断item的index是否在实际加载前有了改动

                Entry(int index,InputItem it)
                {
                    this.index=index;
                    item=it;
                }
            }

            static final int CACHE=0;
            static final int SAVE = 2;

            private InputItem mStart, mEnd, mCache;   //暂时没用

            private ConcurrentLinkedQueue<Entry> mCaches=new ConcurrentLinkedQueue<>();

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what==CACHE) {
                    if (mFile!=null)
                        doFileCacheUnchecked();
                    else if (mSrc != null)    //即使已经给了byte源，但是为了保证安全还是需要进行复制缓存
                        doByteCacheUnchecked();
                }
            }

            //队列缓冲
            private void add(InputItem it)
            {
                mH.mCaches.add(new H.Entry(it.mIndex,it));
            }

            private void doFileCacheUnchecked()
            {
                while (!mCaches.isEmpty())
                {
                    Entry e=mCaches.poll();

                    /**
                     * 缓存速度跟不上界面滑动速度的时候，缓存队列中将出现大量失效请求
                     */
                    if (e.index!=e.item.mIndex)
                        continue;

                        try {
                            mFile.seek(e.index*mSegmentLength);
                            int l=mFile.length-e.index*mSegmentLength;
                            mFile.mFile.read(e.item.mResult,0,l<mSegmentLength?l:mSegmentLength);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                }
            }

            private void doByteCacheUnchecked()
            {
                while (!mCaches.isEmpty())
                {
                    Entry e=mCaches.poll();

                    /**
                     * 缓存速度跟不上界面滑动速度的时候，缓存队列中将出现大量失效请求
                     */
                    if (e.index!=e.item.mIndex)
                        continue;
                    int l = mSrc.length - e.index * mSegmentLength;
                    System.arraycopy(mSrc,e.item.mIndex*mSegmentLength,e.item.mResult,0,l<mSegmentLength?l:mSegmentLength);
                }
            }
        }
    }
}
