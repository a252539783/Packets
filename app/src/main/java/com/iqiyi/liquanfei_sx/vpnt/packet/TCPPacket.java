package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by liquanfei_sx on 2017/8/15.
 */

public class TCPPacket extends Packet {

    public static final byte URG=0x20;
    public static final byte ACK=0x10;
    public static final byte PSH=0x08;
    public static final byte RST=0x04;
    public static final byte SYN=0x02;
    public static final byte FIN=0x01;

    private IPPacket mIpInfo=null;
    private int mSourcePort,mDestPort;
    private int sn,cksn;
    public int mHeaderLength;
    private int mWindowSize;
    private int dataLen=0;

    public boolean urg, ack, psh, rst, syn, fin;

    public TCPPacket(byte[] data,int offset,IPPacket ip) {
        super(data,offset);
        mIpInfo=ip;

        mSourcePort=(((data[offset]&0xff)<<8)|data[offset+1]&0xff);
        mDestPort=(((data[offset+2]&0xff)<<8)|data[offset+3]&0xff);

        sn= ((data[offset + 4] & 0xff) << 24 | (data[offset + 5] & 0xff) << 16 | (data[offset + 6] & 0xff) << 8 | data[offset + 7] & 0xff);
        cksn= ((data[offset + 8] & 0xff) << 24 | (data[offset + 9] & 0xff) << 16 | (data[offset + 10] & 0xff) << 8 | data[offset + 11] & 0xff);

        mHeaderLength=(((data[offset+12]&0xff)>>>4))*4;

        dataLen=ip.length-mHeaderLength-offset;

        urg = (((data[offset + 13]&0xff) << 26) >>> 31 )== 1;
        ack = (((data[offset + 13]&0xff) << 27) >>> 31) == 1;
        psh = (((data[offset + 13]&0xff) << 28) >>> 31) == 1;
        rst = (((data[offset + 13]&0xff) << 29) >>> 31) == 1;
        syn = (((data[offset + 13]&0xff) << 30) >>> 31) == 1;
        fin = (((data[offset + 13]&0xff) << 31) >>> 31) == 1;
//        urg=(data[offset+13]&URG)!=0;
//        ack=(data[offset+13]&ACK)!=0;
//        psh=(data[offset+13]&PSH)!=0;
//        rst=(data[offset+13]&RST)!=0;
//        fin=(data[offset+13]&FIN)!=0;
//        syn=(data[offset+13]&SYN)!=0;


        mWindowSize=((data[offset+14]&0xff)<<8)|(data[offset+15]&0xff);

//        int checksum=0;
//        for (int i=0;i<(data.length-offset)/2;i++)
//        {
//            checksum+=(((data[offset + i*2]&0xff)<<8|(data[offset + i*2+1]&0xff)));
//        }
//        for (int i=6;i<10;i++)
//        {
//            checksum+=(((data[ i*2]&0xff)<<8|(data[ i*2+1]&0xff)));
//        }
//        checksum+=0x06+mHeaderLength+dataLen;
//        if (data.length%2!=0)
//        {
//            checksum+=((data[data.length-1]&0xff)<<8);
//        }
//        while (checksum>>16!=0)
//            checksum=(checksum>>16)+checksum&0xffff;
//        checksum=(~checksum)&0xffff;

//        if (getDestIp().equals("123.207.152.184")||getSourceIp().equals("123.207.152.184"))
//        {
//            Log.e("xx","check sum:"+checksum);
//            Log.e("xx","tcp:");
//            if (syn)
//                Log.e("xx","syn:");
//            if (ack)
//                Log.e("xx","ack:");
//            Log.e("xx","header:"+mHeaderLength);
//            Log.e("xx","window:"+mWindowSize);
//            Log.e("xx","sn:"+sn+" cksn:"+cksn);
//            Log.e("xx","port:"+mDestPort+" source:"+mSourcePort);
//
//            String s="";
//            for (int i=offset+20;i<offset+mHeaderLength;i++)
//            {
//                s+=data[i]+" ";
//            }
//
//            Log.e("xx","option "+s);
//            Log.e("xx","data:"+mHeaderLength);
//        }

    }

    public String getDestIp()
    {
        return mIpInfo.getDestIp();
    }

    public String getSourceIp()
    {
        return mIpInfo.getSourceIp();
    }

    public int getDataLength()
    {
        return dataLen;
    }

    public int getPort()
    {
        return mDestPort;
    }

    public int getSourcePort()
    {
        return mSourcePort;
    }

    public String getHeader()
    {
        StringBuilder sb=new StringBuilder();
        sb.append(getSourcePort()).append(":").append(mDestPort)
                .append(" ").append(sn).append(":").append(cksn)
                .append(" ").append(mHeaderLength).append(" ");

        if (syn)
            sb.append("S");
        if (ack)
            sb.append("A");
        if (psh)
            sb.append("P");
        if (fin)
            sb.append("F");
        if (rst)
            sb.append("R");
        sb.append(mData[mOffset+13]);
        sb.append(" ");
        sb.append(getDataLength());

        return sb.toString();
    }

    public IPPacket getIpInfo()
    {
        return mIpInfo;
    }

    public static class Builder
    {
        static byte[] ident=new byte[2];
        static boolean idInit=true;

        ByteBuffer buffer=ByteBuffer.allocate(65535);
        private int sn=0;

        public Builder(TCPPacket initPacket)
        {
            byte[] b=buffer.array();
            int port=initPacket.getSourcePort();
            int sport=initPacket.getPort();
            b[20]=(byte)(sport<<16>>>24);
            b[21]=(byte)(sport<<24>>>24);
            b[22]=(byte)(port<<16>>>24);
            b[23]=(byte)(port<<24>>>24);         //source port and dest port

            b[0]=0x45;
            b[1]=0x00;
            b[7]=0;
            b[8]=64;
            b[9]=6;
            b[6]=0x40;

            for (int i=24;i<32;i++)
                b[i]=0;

            b[34]=(byte)(65535>>8);
            b[35]=(byte)(65535<<24>>>24);

            if (!idInit) {
                ident[0] = initPacket.getRawData()[4];
                ident[1] = initPacket.getRawData()[5];        //identifier
                idInit=true;
            }
        }

        public Builder setSource(byte []ip)
        {
            byte[] b=buffer.array();
            b[12]=ip[0];
            b[13]=ip[1];
            b[14]=ip[2];
            b[15]=ip[3];

            return this;
        }

        public Builder setDest(byte []ip)
        {
            byte[] b=buffer.array();
            b[16]=ip[0];
            b[17]=ip[1];
            b[18]=ip[2];
            b[19]=ip[3];
            return this;
        }

        public IPPacket build(TCPPacket packet)
        {
            return build(packet,null,ACK);
        }

        public IPPacket build(ByteBuffer data)
        {
            return build(null,data,ACK);
        }

        public IPPacket build(TCPPacket packet,ByteBuffer dataBuffer)
        {
            return build(packet, dataBuffer,ACK);
        }

        public IPPacket build(TCPPacket packet,ByteBuffer dataBuffer,byte type)
        {
            byte[] data;
            if (dataBuffer==null) {
                dataBuffer = ByteBuffer.allocate(0);
                dataBuffer.limit(0);
                data=dataBuffer.array();
            }
            else {
                data = dataBuffer.array();
            }

            /**ip*/
            byte[] b=buffer.array();
            freshId();

            int ipHeadLen;
            int len;
            int tcpHeadLen;
            if (packet!=null)
            {
                len=packet.mHeaderLength+packet.mOffset+dataBuffer.limit();
                ipHeadLen=packet.mOffset;
                tcpHeadLen=packet.mHeaderLength;
            }else
            {
                len=40+dataBuffer.limit();
                ipHeadLen=20;
                tcpHeadLen=20;
            }
            b[32]=(byte)((packet.mHeaderLength/4)<<4);

            b[2]=(byte)(len>>8);
            b[3]=(byte)(len<<24>>>24);       //total length

            int checksum=0;
            b[10]=b[11]=0;
            for (int i=0;i<10;i++)
            {
                checksum+=(((b[i*2]&0xff)<<8|(b[i*2+1]&0xff)));
            }
            checksum=(checksum>>>16)+checksum&0xffff;
            checksum=(~checksum)&0xffff;
            b[10]=(byte)(checksum>>>8);
            b[11]=(byte)(checksum<<24>>>24);             //checksum

            /**tcp*/
            int ack=0;
            if (true) {
                ack = packet.sn + packet.dataLen;
                if (packet.syn) ack++;
                if (packet.fin) ack++;
                //Log.e("xx","build ack sn"+ack);
            }
            b[28] = (byte) (ack >>> 24);
            b[29] = (byte) (ack << 8 >>> 24);
            b[30] = (byte) (ack << 16 >>> 24);
            b[31] = (byte) (ack << 24 >>> 24);                  //ack sn

                b[24] = (byte) (sn >>> 24);
                b[25] = (byte) (sn << 8 >>> 24);
                b[26] = (byte) (sn << 16 >>> 24);
                b[27] = (byte) (sn << 24 >>> 24);               //sn
                sn += dataBuffer.limit();

            b[33]=ACK;
            if (type==ACK)
            {
                if (packet.syn&&!packet.ack) {
                    b[33] = (byte)(b[33]|SYN);
                    sn++;
                }
            }else if (type==FIN)
            {
                b[33] = (byte)(b[33]|FIN);
                sn++;
            }else if (type==RST)
            {
                b[33] = (byte)(b[33]|RST);
                sn++;
            }

            if (packet!=null)
            for (int i=packet.mOffset+20;i<packet.mHeaderLength+packet.mOffset;i++)
            {
                b[i]=packet.getRawData()[i];
            }

            b[36]=b[37]=0;
            checksum=0;
            for (int i=10;i<(tcpHeadLen+ipHeadLen)/2;i++)
            {
                checksum+=(((b[i*2]&0xff)<<8|(b[i*2+1]&0xff)));
            }
            for (int i=6;i<10;i++)
            {
                checksum+=(((b[ i*2]&0xff)<<8|(b[ i*2+1]&0xff)));
            }
            for (int i=0;i<dataBuffer.limit()/2;i++)
            {
                checksum+=(((data[ i*2]&0xff)<<8|(data[ i*2+1]&0xff)));
            }
            if (dataBuffer.limit()%2!=0)
            {
                checksum+=((data[dataBuffer.limit()-1]&0xff)<<8);
            }

            checksum+=0x06+tcpHeadLen+dataBuffer.limit();
            while (checksum>>16!=0)
                checksum=(checksum>>>16)+checksum&0xffff;
            checksum=(~checksum)&0xffff;
            b[36]=(byte)(checksum<<16>>>24);
            b[37]=(byte)(checksum<<24>>>24);     //checksum

            checksum=0;
            for (int i=10;i<(tcpHeadLen+ipHeadLen)/2;i++)
            {
                checksum+=(((b[i*2]&0xff)<<8|(b[i*2+1]&0xff)));
            }
            for (int i=6;i<10;i++)
            {
                checksum+=(((b[ i*2]&0xff)<<8|(b[ i*2+1]&0xff)));
            }
            checksum+=0x06+tcpHeadLen+dataBuffer.limit();
            while (checksum>>16!=0)
                checksum=(checksum>>>16)+checksum&0xffff;
            checksum=(~checksum)&0xffff;
//            Log.e("xx","build cal checksum:"+checksum);

            byte[] src=new byte[len];
            try{
                System.arraycopy(b,0,src,0,ipHeadLen+tcpHeadLen);
                if (dataBuffer.limit()!=0)
                    System.arraycopy(data,0,src,ipHeadLen+tcpHeadLen,dataBuffer.limit());
            }catch (IndexOutOfBoundsException e)
            {
                Log.e("IndexOOB","ttLen:"+len+"headlen:"+(ipHeadLen+tcpHeadLen)+" dataLen:"+dataBuffer.limit());
            }


            if (packet.syn)
            {
                Log.e("xx","b33 :"+b[33]);
            }
            return new IPPacket(src);
        }

        private synchronized void freshId()
        {
            byte []b=buffer.array();
            if ((ident[1]&0xff)==0xff)
            {
                ident[0]++;
            }
            ident[1]++;                 //identifier

            b[4]=ident[0];
            b[5]=ident[1];
        }
    }
}
