package com.iqiyi.liquanfei_sx.vpnt.packet;

/**
 * Created by liquanfei_sx on 2017/8/15.
 */

public class Packet {

    private byte[] mData=null;
    public int mOffset=0;

    public Packet(byte[] data,int offset)
    {
        mData=data;
        mOffset=offset;
    }

    public byte[] getRawData()
    {
        return mData;
    }

    @Override
    public String toString() {
        return new String(mData);
    }
}