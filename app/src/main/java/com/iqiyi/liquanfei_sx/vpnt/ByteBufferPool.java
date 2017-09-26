package com.iqiyi.liquanfei_sx.vpnt;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Created by Administrator on 2017/9/19.
 */

public class ByteBufferPool {

    public static final int SIZE_INFINITY=-1;
    public static final int CAPABILITY_DEFAULT=1024;

    private static ByteBufferPool mDefault=new ByteBufferPool();

    private Queue<ByteBuffer> mBuffers=new LinkedList<>();
    private int mBufferCapability=CAPABILITY_DEFAULT;
    private int mBufferMaxSize=SIZE_INFINITY;

    private ByteBufferPool()
    {
    }

    public static ByteBufferPool getDefault()
    {
        return mDefault;
    }

    public void setCapability(int capability)
    {
        mBufferCapability=capability;
    }

    public void setMaxSize(int size)
    {
        if (size<-1)
            size=SIZE_INFINITY;

        mBufferMaxSize=size;
    }

    public synchronized ByteBuffer get()
    {
        if (mBuffers.peek()==null)
        {
            mBuffers.add(ByteBuffer.allocate(mBufferCapability));
        }

        return mBuffers.poll();
    }

    public ByteBuffer[] get(byte []src,int offset,int length)
    {
        ByteBuffer[] res=new ByteBuffer[length/mBufferCapability+((length%mBufferCapability)==0?0:1)];
        for (int i=0;i<res.length;i++)
        {
            res[i]=get();
            res[i].put(src,offset+mBufferCapability*i,i==res.length-1?length-mBufferCapability*i:mBufferCapability);
            res[i].flip();
        }

        return res;
    }

    public void recycle(ByteBuffer buffer)
    {
        if (mBufferMaxSize==SIZE_INFINITY || mBuffers.size() <mBufferMaxSize)
        {
            buffer.clear();
            mBuffers.add(buffer);
        }
    }
}
