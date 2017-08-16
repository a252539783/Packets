package com.iqiyi.liquanfei_sx.vpnt;

import android.os.Build;

/**
 * Created by liquanfei_sx on 2017/8/15.
 */

public class TCPPacket extends Packet {

    private IPPacket mIpInfo=null;
    private int mSourcePort,mDestPort;
    private int sn,cksn;
    private byte mHeaderLength;
    private int mWindowSize;

    private boolean URG,ACK,PSH,PST,SYN,FIN;

    public TCPPacket(byte[] data,int offset,IPPacket ip) {
        super(data,offset);
        mIpInfo=ip;

        mSourcePort=(data[offset]<<8|data[offset+1]);
        mDestPort=(data[offset+2]<<8|data[offset+3]);

        sn=(data[offset+4]<<24|data[offset+5]<<16|data[offset+6]<<8+data[offset+7]);
        cksn=(data[offset+8]<<24|data[offset+9]<<16|data[offset+10]<<8+data[offset+11]);

        mHeaderLength=(byte)(data[offset+12]>>4);

        URG= data[offset + 13] << 2 >> 7 == 1;
        ACK= data[offset + 13] << 3 >> 7 == 1;
        PSH= data[offset + 13] << 4 >> 7 == 1;
        PST= data[offset + 13] << 5 >> 7 == 1;
        SYN= data[offset + 13] << 6 >> 7 == 1;
        FIN= data[offset + 13] << 7 >> 7 == 1;

        mWindowSize=data[offset+14]<<8|data[offset+15];
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
}
