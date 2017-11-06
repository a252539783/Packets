package com.iqiyi.liquanfei_sx.vpnt.text;

/**
 * Created by Administrator on 2017/11/6.
 */

public class ByteConstants {
    public static final String [] HEX=new String[0xff];
    public static final String [] NUM=new String[0xff];

    public static void initHex()
    {
        for (int i=0;i<0xff;i++)
        {
            HEX[i]=String.format("%02x",i);
        }
    }
}
