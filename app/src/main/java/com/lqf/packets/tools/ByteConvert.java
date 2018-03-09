package com.lqf.packets.tools;

/**
 * Created by Administrator on 2017/11/7.
 */

public class ByteConvert {

    public static long parseLong(byte[] src,int offset) {
        long res=0;
        for (int i=0;i<8;i++)
        {
            res=(res|(src[offset+i]&0xff))<<8;
        }

        return res;
    }

    public static byte[] getLong(long l)
    {
        byte[] res=new byte[8];

        for (int i=0;i<8;i++)
        {
            res[7-i]=(byte)(l&0xff);
            l=l>>>8;
        }

        return res;
    }

    public static int parseInt(byte []src,int offset)
    {
        return parseInt(src,offset,4);
    }

    public static int parseInt(byte []src,int offset,int len)
    {
        int res=0;
        for (int i=0;i<len;i++)
        {
            res=((res<<8)|(src[offset+i]&0xff));
        }

        return res;
    }

}
