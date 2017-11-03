package com.iqiyi.liquanfei_sx.vpnt.text;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Administrator on 2017/11/1.
 */

public class AddedInput {

    private List<InputItem> mAllModify=new LinkedList<>();
    private Set<InputItem> mAllResult=new TreeSet<>();
    private byte[] mSource=null;

    public AddedInput(byte[] src){
        mSource=src;
    }

    public void edit(int type,int offset,int length)
    {
        Iterator<InputItem> it=mAllResult.iterator();

        InputItem ii,last=it.next();
        int current=last.mOffset;
        int result=offset;

        while (it.hasNext())
        {
            ii=it.next();
            int difference=last.difference();
            current=current+ii.mOffset-last.mOffset+difference;

            if (current>=offset)
            {
            }

            result-=difference;
        }

    }

    int realOffset(int offset)
    {
        if (mAllResult.isEmpty())
            return offset;

        Iterator<InputItem> it=mAllResult.iterator();

        InputItem ii,last=it.next();
        int current=last.mOffset;
        int result=offset;

        while (it.hasNext())
        {
            ii=it.next();
            int difference=last.difference();
            current=current+ii.mOffset-last.mOffset+difference;

            if (current>=offset)
            {
                return result;
            }

            result-=difference;
        }

        return result;
    }

    static class InputItem
    {
        static int DEFAULT_SIZE=16;

        int mOffset =0;
        int mOverride=0;
        int mLength=0;
        byte [] mResult;

        List<InputHistory> mHistory=new LinkedList<>();

        InputItem(int offset)
        {
            mOffset =offset;
            mResult =new byte[DEFAULT_SIZE];
        }

        InputItem(byte []src,int offset,int length)
        {
            mOffset =offset;
            mLength=length;
            mOverride=length;
            mResult =new byte[length];
            System.arraycopy(src,offset, mResult,0,length);
        }

        int difference()
        {
            if (mOverride==0)
                return mLength;

            return mLength-mOverride;
        }
    }

}
