package com.iqiyi.liquanfei_sx.vpnt.text;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Administrator on 2017/11/1.
 * 表示一个文件中的所有更改
 */

public class AddedInput {
    private byte[] mSource=null;
    private String mSSource=null;

    private int segmentLength=1000;

    private InputItem mRoot;
    private int[] mCurrentIndex=new int[2];
    private InputItem mStart,mCurrent;

    private int mCharCount=0;

    public AddedInput(byte[] src){
        int i,l;
        InputItem item=mRoot=new InputItem(src,0,src.length<=segmentLength?src.length:segmentLength);
        for (i=1;i<src.length/segmentLength;i++)
        {
            item.mNext=new InputItem(src,i*segmentLength,segmentLength);
            item.mNext.mPrevious=item;
            item=item.mNext;
        }

        if ((l=src.length%segmentLength)!=0)
        {
            item.mNext=new InputItem(src,i*segmentLength,l);
            item.mNext.mPrevious=item;
        }

        mCurrent=mRoot;
        mCharCount=src.length;
    }

    public void delete(int offset,int length)
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
                while (mCurrent.mOffset + mCurrent.mLength < offset) {
                    mCurrent = mCurrent.mNext;
                }
            } else if (offset < mCurrent.mOffset) {
                while (mCurrent.mOffset + mCurrent.mLength > offset) {
                    mCurrent = mCurrent.mPrevious;
                }
            }
        }
    }

    public byte getByte(int index)
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

        int mOffset =0;
        int mOverride=0;
        int mLength=0;
        byte [] mResult;

        List<InputHistory> mHistory=new LinkedList<>();

        InputItem(byte []src,int offset,int length)
        {
            mOffset =offset;
            mLength=length;
            mOverride=length;
            mResult =new byte[length];
            System.arraycopy(src,offset, mResult,0,length);
        }

        public void delete(int offset,int length)
        {

        }
    }

}
