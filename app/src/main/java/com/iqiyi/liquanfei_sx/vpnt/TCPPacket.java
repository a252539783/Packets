package com.iqiyi.liquanfei_sx.vpnt;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by liquanfei_sx on 2017/8/15.
 */

public class TCPPacket extends Packet {

    private IPPacket mIpInfo=null;
    private int mSourcePort,mDestPort;
    private long sn,cksn;
    private byte mHeaderLength;
    private int mWindowSize;

    boolean URG,ACK,PSH, RST,SYN,FIN;

    public TCPPacket(byte[] data,int offset,IPPacket ip) {
        super(data,offset);
        mIpInfo=ip;

        mSourcePort=(data[offset]&0xff<<8|data[offset+1]&0xff);
        mDestPort=(data[offset+2]&0xff<<8|data[offset+3]&0xff);

        sn=(data[offset+4]&0xffL<<24|data[offset+5]&0xffL<<16|data[offset+6]&0xffL<<8+data[offset+7]&0xffL)&0xffffffffL;
        cksn=(data[offset+8]&0xffL<<16|data[offset+9]&0xffL<<16|data[offset+10]&0xffL<<8+data[offset+11]&0xffL)&0xffffffffL;

        mHeaderLength=(byte)((data[offset+12]&0xff>>4));

        URG= (data[offset + 13]&0xff << 2 >> 7 )== 1;
        ACK= (data[offset + 13]&0xff << 3 >> 7) == 1;
        PSH= (data[offset + 13]&0xff << 4 >> 7) == 1;
        RST = (data[offset + 13]&0xff << 5 >> 7) == 1;
        SYN= (data[offset + 13]&0xff << 6 >> 7) == 1;
        FIN= (data[offset + 13]&0xff << 7 >> 7) == 1;

        mWindowSize=(data[offset+14]&0xff<<8)|data[offset+15]&0xff;
        Log.e("xx","tcp:");
        Log.e("xx","header:"+mHeaderLength);
        Log.e("xx","window:"+mWindowSize);
        Log.e("xx","sn:"+sn+" cksn:"+cksn);
        Log.e("xx","port:"+mDestPort+" source:"+mSourcePort);
    }

    public String getDestIp()
    {
        return new StringBuilder(15).append(mIpInfo.getDestIp()[0]&0xff).append('.')
                .append(mIpInfo.getDestIp()[1]&0xff).append('.')
                .append(mIpInfo.getDestIp()[2]&0xff).append('.')
                .append(mIpInfo.getDestIp()[3]&0xff).toString();
    }

    public int getPort()
    {
        return mDestPort;
    }

    public int getSourcePort()
    {
        return mSourcePort;
    }

    public IPPacket getIpInfo()
    {
        return mIpInfo;
    }

    static class Builder
    {
        static ByteBuffer buffer=ByteBuffer.allocate(32767);

        Builder(ServerService.TCPStatus status)
        {
            byte[] b=buffer.array();
            b[4]=0;
            b[5]=0;

            for (int i=24;i<32;i++)
                b[i]=0;
        }

        Builder setSource(byte []ip)
        {
            byte[] b=buffer.array();
            b[12]=ip[0];
            b[13]=ip[1];
            b[14]=ip[2];
            b[15]=ip[3];

            return this;
        }

        Builder setDest(byte []ip)
        {
            byte[] b=buffer.array();
            b[16]=ip[0];
            b[17]=ip[1];
            b[18]=ip[2];
            b[19]=ip[3];
            return this;
        }

        Builder setSourcePort(int port)
        {

            return this;
        }

        Builder setDestPort(int port)
        {
            return this;
        }

        IPPacket build(byte[] data)
        {
            byte[] b=buffer.array();
            if (b[5]==-1)
            {
                b[4]++;
            }
            b[5]++;                 //identifier

            int len=40+data.length;
            b[2]=(byte)(len>>8);
            b[3]=(byte)(len<<24>>24);       //total length

            int checksum=0;
            b[10]=b[11]=0;
            for (int i=0;i<10;i++)
            {
                checksum+=(((data[i*2]&0xff)<<8|(data[i*2+1]&0xff)));
            }
            checksum=(checksum>>16)+checksum&0xffff;
            checksum=(~checksum)&0xffff;
            b[10]=(byte)(checksum>>8);
            b[11]=(byte)(checksum<<24>>24);             //checksum

            return null;
        }

        static
        {
            byte[] b=buffer.array();
            b[0]=4<<4|5;
            b[1]=0x08;
            b[6]=4;
            b[7]=0;
            b[8]=64;
            b[9]=6;
        }
    }
}
