package com.lqf.packets.packet;

import com.lqf.packets.tools.ByteConvert;

/**
 * Created by liquanfei_sx on 2017/8/15.
 */

public class IPPacket extends Packet{

    private byte[] mSourceIp,mDestIp;
    private byte mHeaderLength,mVersion,mTos,mProtocol,mTTL;
    public int length;

    private Packet mData;

    public IPPacket(byte[] data) {
        super(data,0);

        mVersion = (byte)(data[0] >> 4);          //4 or 6
        mHeaderLength = (byte)(data[0] & 0x0F);
        mHeaderLength *= 4;

        length = ByteConvert.parseInt(data,2,2);

        mTTL=data[8];
        mProtocol=data[9];// Protocol  1表示为ICMP协议， 2表示为IGMP协议， 6表示为TCP协议， 1 7表示为UDP协议

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


        if (mProtocol==6)
        {
            mData=new TCPPacket(data,mHeaderLength,this);
           // Log.e("xx","ip header l  "+mHeaderLength);
        }else
        {
            mData=new Packet(data,mHeaderLength);
        }

//        int checksum=0;
//        for (int i=0;i<10;i++)
//        {
//            checksum+=(((data[i*2]&0xff)<<8|(data[i*2+1]&0xff)));
//        }
//        checksum=(checksum>>>16)+checksum&0xffff;
//        checksum=(checksum>>>16)+checksum&0xffff;
//        checksum=(~checksum)&0xffff;


//        if (getDestIp().equals("123.207.152.184")||getSourceIp().equals("123.207.152.184")) {
//            Log.e("xx", "Header Length:" + mHeaderLength);
//            Log.e("xx", "protocol:" + mProtocol);
//            Log.e("xx", "ttl:" + mTTL);
//            Log.e("xx", "check:"+checksum+"  ident:" + (((data[4] & 0xff) << 8) | ((data[5] & 0xff))));
//        }
    }

    public byte[] getSourceIpB()
    {
        return mSourceIp;
    }

    public byte[] getDestIpB()
    {
        return mDestIp;
    }

    public String getDestIp()
    {
        return new StringBuilder(15).append(mDestIp[0]&0xff).append('.')
                .append(mDestIp[1]&0xff).append('.')
                .append(mDestIp[2]&0xff).append('.')
                .append(mDestIp[3]&0xff).toString();
    }

    public String getSourceIp()
    {
        return new StringBuilder(15).append(mSourceIp[0]&0xff).append('.')
                .append(mSourceIp[1]&0xff).append('.')
                .append(mSourceIp[2]&0xff).append('.')
                .append(mSourceIp[3]&0xff).toString();
    }

    public int getHeaderLength()
    {
        return mHeaderLength;
    }

    public String getHeader()
    {
        StringBuilder sb= new StringBuilder().append(getSourceIp()).append(":")
                .append(getDestIp());
        if (mData instanceof TCPPacket)
            sb.append(((TCPPacket) mData).getHeader());

        return sb.toString();
    }

    public Packet getData()
    {
        return mData;
    }
}
