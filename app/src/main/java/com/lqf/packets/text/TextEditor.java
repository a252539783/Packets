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

    private static CacheThread mThread=null;

    private static int mSegmentLength =1000;
    private static int mCacheSize=4;
    private static int mEndLength=1;
    private static int mStartLength=1;

    private int mSegmentCount=0;

    private InputItem mRoot,mEnd;
    private int mStartIndex=0,mEndIndex;
    private int[] mCurrentIndex=new int[2];
    private InputItem mStart,mCurrent;

    private SparseArray<InputItem> mAllModified;

    private int mCharCount=0;

    public TextEditor(byte[] src) {
        mSource=src;
        mSegmentCount=src.length/ mSegmentLength +(src.length% mSegmentLength !=0?1:0);
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
        for (int i=0;i<count;i++)
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

    private void select(int id,int offset)
    {
        mCurrentIndex[id]=offset;

        if (id==0) {
            if (offset >= mCurrent.mOffset) {
                while (mCurrent.mOffset + mCurrent.mLength <= offset) {

                    mCurrent = mCurrent.mNext;
                    if (mEndIndex!=mSegmentCount-1&&mEnd.mIndex-mCurrent.mIndex<mEndLength)
                    {
                        mEndIndex++;
                        if (!mRoot.mModified)
                        {
                            mRoot.mPrevious=mEnd;
                            mRoot.mIndex=mEnd.mIndex+1;
                            mRoot.mOffset=mEnd.mOffset+mEnd.mLength;
                            mRoot.mLength=mSegmentLength;
                            mEnd.mNext=mRoot;

                            mEnd=mRoot;
                            mRoot=mRoot.mNext;
                            mEnd.mNext=null;

                            mEnd.mPrepared=false;

                        }else
                        {
                            mEnd.mNext=new InputItem(mEnd.mOffset+mEnd.mLength,mSegmentLength,mEnd.mIndex+1);
                            mEnd.mNext.mPrevious=mEnd;
                            mEnd=mEnd.mNext;

                            mRoot=mRoot.mNext;
                            mEnd.mNext=null;
                        }

                        mThread.cache(mRoot,mEnd,mEnd);
                    }
                }
            } else if (offset < mCurrent.mOffset) {
                while (mCurrent.mOffset> offset) {
                    mCurrent = mCurrent.mPrevious;

                    if (mRoot.mOffset!=0&&mCurrent.mIndex-mRoot.mIndex<mStartLength)
                    {
                        if (!mEnd.mModified)
                        {
                            mEnd.mNext=mRoot;
                            mEnd.mIndex=mRoot.mIndex-1;
                            mEnd.mLength=mSegmentLength;
                            mEnd.mOffset=mRoot.mOffset-mEnd.mLength;
                            mRoot.mPrevious=mEnd;

                            mRoot=mEnd;
                            mEnd=mEnd.mPrevious;
                            mRoot.mPrevious=null;

                            mRoot.mPrepared=false;

                        }else
                        {
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

    public int getByte(int index)
    {
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

        int mIndex=0;

        int mOffset =0;
        int mOverride=0;
        int mLength=0;
        byte [] mResult;

        boolean mModified=false;

        private boolean mPrepared=false;

        List<InputHistory> mHistory=new LinkedList<>();

        InputItem(byte []src,int offset,int length,int index)
        {
            mOffset =offset;
            mLength=length;
            mOverride=length;
            mResult =new byte[length];
            mIndex=index;
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

        public void cache(InputItem start,InputItem end,InputItem cache)
        {
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
                int index;

                Entry(int index,InputItem it)
                {
                    this.index=index;
                    item=it;
                }
            }

            static final int CACHE=0;
            static final int SAVE=2;

            private InputItem mStart,mEnd,mCache;

            private ConcurrentLinkedQueue<Entry> mCaches=new ConcurrentLinkedQueue<>();

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what==CACHE)
                {
                    if (mFile!=null)
                        doFileCacheUnchecked();
                    else if (mSrc!=null)
                        doByteCacheUnchecked();
                }
            }

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
