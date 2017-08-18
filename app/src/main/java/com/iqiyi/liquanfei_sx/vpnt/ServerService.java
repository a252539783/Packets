package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by liquanfei_sx on 2017/8/11
 */

public class ServerService extends Service {

    private MB mB=new MB();
    private ClientService mLocal=null;
    private TransmitThread mTransmitThread=null;
    private ArrayList<PacketList> mPackets=new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mTransmitThread=new TransmitThread();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mB;
    }

    public void startDaemon()
    {
        mTransmitThread.start();
    }

    public void stopDaemon()
    {
        mTransmitThread.pause();
    }

    public boolean transmit(Packet packet)
    {
        mTransmitThread.mPackets.add(packet);

        if (!mTransmitThread.mPause)
            return false;

        return true;
    }

    public void setLocal(ClientService local)
    {
        mLocal=local;
    }

    class MB extends Binder
    {
        ServerService get()
        {
            return ServerService.this;
        }
    }

    private class TransmitThread extends Thread
    {
        LinkedBlockingQueue<Packet> mPackets;
        SparseArray<TCPStatus> mSockets;
        private ExecutorService mThreadPool;

        boolean mPause=true;

        TransmitThread()
        {
            mSockets=new SparseArray<>();
            mPackets=new LinkedBlockingQueue<>();
            mThreadPool= Executors.newCachedThreadPool();
        }

        @Override
        public void run() {
            super.run();
            mPause=false;

            while (!mPause||!mPackets.isEmpty())
            {
                Packet packet=mPackets.poll();
                doTransmit(packet);
            }
        }

        private void doTransmit(Packet packet)
        {
            if (packet instanceof TCPPacket)
            {
                TCPPacket tcp=(TCPPacket) packet;
                if (tcp.SYN)
                {
                    mThreadPool.execute(new ConnectRunnable(tcp));
                }else if (tcp.FIN)
                {
                    try {
                        TCPStatus status=mSockets.get(tcp.getSourcePort());
                        if (status!=null)
                        {
                            status.mSocket.close();
                            mSockets.remove(tcp.getSourcePort());
                        }

                    } catch (IOException e) {
                        mSockets.remove(tcp.getSourcePort());
                    }
                }
            }
        }

        boolean connect(Packet packet)
        {
            if (!(packet instanceof TCPPacket))
                return false;

            TCPStatus status;
            try {
                status = new TCPStatus((TCPPacket) packet);
            } catch (IOException e) {
                return false;
            }

            mSockets.put(((TCPPacket) packet).getSourcePort(),status);
            return true;
        }

        void pause()
        {
            mPause=true;
        }


        class ConnectRunnable implements Runnable
        {
            private TCPPacket packet;

            ConnectRunnable(TCPPacket packet) {
                this.packet = packet;
            }

            @Override
            public void run() {
                if (connect(packet))   //connect successfully and send sync-ack
                {

                }
            }
        }

        class WriteRunnable implements Runnable
        {
            private TCPPacket packet;

            WriteRunnable(TCPPacket tcp)
            {
                this.packet=tcp;
            }

            @Override
            public void run() {

            }
        }
    }

    class TCPStatus {
        public static final int SYN_CONNECT = 0;
        public static final int ACK_CONNECT = 1;
        public static final int FIN_CONNECT = 2;

        Socket mSocket;
        int mStatus;
        InputStream is;
        OutputStream os;
        ReadThread mReadThread;
        PacketList mPacketList;
        int mId=0;
        int sn=0;
        int mPort=0;
        private TCPPacket.Builder mBuilder;

        public TCPStatus(TCPPacket packet) throws IOException {
            mSocket=new Socket(packet.getDestIp(),packet.getPort());
            mLocal.protect(mSocket);
            mReadThread=new ReadThread();
            os=mSocket.getOutputStream();
            is=mSocket.getInputStream();
            mPacketList=new PacketList();
            mPackets.add(mPacketList);

            mBuilder=new TCPPacket.Builder(this)
            .setDest(packet.getIpInfo().getSourceIp())
            .setSource(packet.getIpInfo().getDestIp())
            .setDestPort(packet.getSourcePort())
            .setSourcePort(packet.getPort());
        }

        class ReadThread extends Thread
        {
            ByteBuffer buffer=ByteBuffer.allocate(32767);

            @Override
            public void run() {
                super.run();
                try{
                    while (true)
                    {
                        int len=0;
                        while ((len=is.available())==0);
                        read(is,buffer.array(),len);
                    }
                }catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    static void read(InputStream is,byte[] b,int length) throws IOException {
        int len=0;
        while (len!=length)
        {
            len+=is.read(b,len,length-len);
        }
    }

    static class PacketList
    {
        private String pkgName,appName;
        private int port;
        private ArrayList<Packet> packets;

        PacketList()
        {
            packets=new ArrayList<>();
        }

        void add(Packet p)
        {
            packets.add(p);
        }
    }
}
