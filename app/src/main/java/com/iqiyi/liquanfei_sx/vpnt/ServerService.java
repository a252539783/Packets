package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
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
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("xx","server started");
        mTransmitThread=new TransmitThread();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        bindService(new Intent(ServerService.this, ClientService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mLocal=((ClientService.MB)service).get();
                Log.e("xx","bind to client");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        },BIND_AUTO_CREATE);
        return mB;
    }

    public void startDaemon()
    {
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                while (mLocal==null);
                mTransmitThread.start();
            }
        }.start();

    }

    public void stopDaemon()
    {
        mTransmitThread.pause();
    }

    public boolean transmit(Packet packet)
    {
        //Log.e("xx","add");
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
            Log.e("xx","daemon started");
            while (!mPause||!mPackets.isEmpty())
            {
                Packet packet=mPackets.poll();
                doTransmit(packet);
            }
            Log.e("xx","daemon ended");
        }

        private void doTransmit(Packet packet)
        {
            if (packet instanceof IPPacket)
            {
                IPPacket ip=(IPPacket)packet;

                if (ip.getData() instanceof TCPPacket)
                {
                    Log.e("xx","transmit tcp packet:");
                    TCPPacket tcp=(TCPPacket) ip.getData();
                    if (tcp.syn)
                    {
                        Log.e("xx","transmit tcp sync:");
                        mThreadPool.execute(new ConnectRunnable(tcp));
                    }else if (tcp.ack)
                    {
                        if (tcp.fin)
                        {
                            Log.e("xx","transmit tcp fin:");
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
                        }else
                        {
                            TCPStatus status=mSockets.get(tcp.getSourcePort());
                            if (status==null)
                            {
                                Log.e("xx","find no connected:ServerService.TransmitThread.doTransmit()");
                            }else
                            {
                                mThreadPool.execute(new ACKRunnable(tcp,status));
                            }
                        }
                    }
                    else
                    {
                        Log.e("xx","transmit tcp unknown:");
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
                    //Log.e("xx","connected successfully");
                }
            }
        }

        class ACKRunnable implements Runnable
        {
            private TCPPacket packet;
            private TCPStatus status;

            ACKRunnable(TCPPacket packet,TCPStatus status) {
                this.packet = packet;
                this.status = status;
            }

            @Override
            public void run() {
                status.ack(packet);
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
        Socket mSocket;
        InputStream is;
        OutputStream os;
        ReadThread mReadThread;
        PacketList mPacketList;
        private TCPPacket.Builder mBuilder;

        public TCPStatus(TCPPacket packet) throws IOException {
            Log.e("xx","socket connecting "+packet.getDestIp()+":"+packet.getPort()+"from:"+packet.getSourcePort());
            mSocket=new Socket();
            mSocket.bind(null);
            mLocal.protect(mSocket);
            //mSocket.connect(new InetSocketAddress("121.199.31.116",8908));
            mSocket.connect(new InetSocketAddress(packet.getDestIp(),packet.getPort()));
            Log.e("xx","socket connected");
            mReadThread=new ReadThread();
            os=mSocket.getOutputStream();
            is=mSocket.getInputStream();
            mPacketList=new PacketList();
            mPacketList.add(packet.getIpInfo());
            mPackets.add(mPacketList);

            mBuilder=new TCPPacket.Builder(this,packet)
            .setDest(packet.getIpInfo().getSourceIpB())
            .setSource(packet.getIpInfo().getDestIpB());

            mLocal.write(mBuilder.build(packet));
            mReadThread.start();
        }

        public void ack(TCPPacket packet)
        {
            //mBuilder.freshId();
            mPacketList.add(packet.getIpInfo());
            if (packet.getDataLength()==0)
            {
                return ;
            }

            try {
                if (packet.getPort()==6666)
                    Log.e("6666","send"+new String(packet.getRawData(),packet.mOffset+packet.mHeaderLength,packet.getDataLength(),"utf-8"));
                os.write(packet.getRawData(),packet.mOffset+packet.mHeaderLength,packet.getDataLength());
                mLocal.write(mBuilder.build(packet));
            } catch (IOException e) {
                Log.e("xx","write to dest fail:ServerService.TCPStatus.ack(TCPPacket)");
            }
        }

        public void ack(ByteBuffer data)
        {
            //mBuilder.freshId();
            TCPPacket packet=(TCPPacket) ((IPPacket) mPacketList.getLast()).getData();
            if (packet.getPort()==6666)
                try {
                    Log.e("6666","recv"+new String(packet.getRawData(),packet.mOffset+packet.mHeaderLength,packet.getDataLength(),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            mPacketList.add(mBuilder.build(packet,data));
            mLocal.write(mPacketList.getLast());
        }

        class ReadThread extends Thread
        {
            ByteBuffer buffer=ByteBuffer.allocate(32767);

            @Override
            public void run() {
                super.run();
                try{
                    int len;
                    while (true)
                    {
                        len=0;
                        while ((len=is.available())==0);
                        read(is,buffer.array(),len);
                        buffer.limit(len);

                        ack(buffer);
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

        Packet get(int i){
            return packets.get(i);
        }

        Packet getLast() {
            return packets.get(packets.size()-1);
        }
    }
}
