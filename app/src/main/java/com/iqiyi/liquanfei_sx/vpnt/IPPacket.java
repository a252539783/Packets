package com.iqiyi.liquanfei_sx.vpnt;

import android.net.VpnService;
import android.util.Log;

/**
 * Created by liquanfei_sx on 2017/8/15.
 */

public class IPPacket extends Packet{

    private byte[] mSourceIp,mDestIp;
    private byte mHeaderLength,mVersion,mTos,mProtocol,mTTL;
    private int mLength;

    private Packet mData;

    public IPPacket(byte[] data) {
        super(data,0);

        mVersion = (byte)(data[0] >> 4);          //4 or 6
        mHeaderLength = (byte)(data[0] & 0x0F);
        mHeaderLength *= 4;
        Log.e("xx", "IP Version:" + mVersion);
        Log.e("xx", "Header Length:" + mHeaderLength);

        mLength=(0xFFFF&data[2])<<8|(data[3]&0xff);

        Log.e("xx", "Total Length:" + mLength);


        mTTL=data[8];
        mProtocol=data[9];// Protocol  1表示为ICMP协议， 2表示为IGMP协议， 6表示为TCP协议， 1 7表示为UDP协议

        Log.e("xx", "ttl:" + mTTL);

        mSourceIp=new byte[4];
        mDestIp=new byte[4];
        mSourceIp[0]=data[12];
        mSourceIp[1]=data[13];
        mSourceIp[2]=data[14];
        mSourceIp[3]=data[15];
        mDestIp[0]=data[16];
        mDestIp[1]=data[17];
        mDestIp[2]=data[18];
        mDestIp[3]=data[19];

        Log.e("xx","source:"+mSourceIp[0]+"."+
        mSourceIp[1]+"."+
        mSourceIp[2]+"."+
        mSourceIp[3]+ " dest:"+
                mDestIp[0]+"."+
        mDestIp[1]+"."+
        mDestIp[2]+"."+
        mDestIp[3]);

        if (mProtocol==6)
        {
            mData=new TCPPacket(data,mHeaderLength,this);
        }

        int checksum=0;
        for (int i=0;i<10;i++)
        {
            checksum+=(((data[i*2]&0xff)<<8|(data[i*2+1]&0xff)));
        }
        checksum=(checksum>>16)+checksum&0xffff;
        checksum=(~checksum)&0xffff;
        Log.e("xx","check sum:"+checksum+"  ident:"+(((data[4]&0xff)<<8)|((data[5]&0xff))));
    }

    public byte[] getSourceIp()
    {
        return mSourceIp;
    }

    public byte[] getDestIp()
    {
        return mDestIp;
    }
}
