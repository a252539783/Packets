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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by liquanfei_sx on 2017/8/11
 */

public class ServerService extends Service {

    private MB mB=new MB();
    private ClientService mLocal=null;
    private Selector mSelector= null;
    private TransmitThread mTransmitThread=null;
    private ReadThread mReadThread=null;
    private ArrayList<PacketList> mPackets=new ArrayList<>();
    private ByteBufferPool mBufferPool=ByteBufferPool.getDefault();

    private boolean registering=false;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mSelector=Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("xx","server started");
        mTransmitThread=new TransmitThread();
        mReadThread=new ReadThread();
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
                mReadThread.start();
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
                                TCPStatus status=mSockets.get(tcp.getSourcePort());
                                if (status!=null)
                                {
                                    status.close();
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
                    Log.e("xx","thread pool size:"+((ThreadPoolExecutor)mThreadPool).getPoolSize());
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
        //Socket mSocket;
//        InputStream is;
//        OutputStream os;
        SocketChannel mChannel;
        //ReadThread mReadThread;
        PacketList mPacketList;
        Queue<ByteBuffer> mReadySend=new LinkedList<>();

        private TCPPacket.Builder mBuilder;

        public TCPStatus(TCPPacket packet) throws IOException {


            //mSocket=new Socket();
            //mSocket.bind(null);
            //mLocal.protect(mSocket);
            //mSocket.connect(new InetSocketAddress("121.199.31.116",8908));
            //mSocket.connect(new InetSocketAddress(packet.getDestIp(),packet.getPort()));
            //os=mSocket.getOutputStream();
            //is=mSocket.getInputStream();
            mPacketList=new PacketList();
            mPacketList.add(packet.getIpInfo());
            mPackets.add(mPacketList);
            mBuilder=new TCPPacket.Builder(this,packet)
                    .setDest(packet.getIpInfo().getSourceIpB())
                    .setSource(packet.getIpInfo().getDestIpB());

            mChannel=SocketChannel.open();
            mLocal.protect(mChannel.socket());
            mChannel.configureBlocking(false);
            registering=true;
            mSelector.wakeup();
            mChannel.register(mSelector, SelectionKey.OP_CONNECT,TCPStatus.this);
            mChannel.connect(new InetSocketAddress(packet.getDestIp(),packet.getPort()));
            registering=false;

            //mReadThread.start();
            mLocal.write(mBuilder.build(packet));
        }

        public void close()
        {
            try {
                mChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void ack(TCPPacket packet)
        {
            //mBuilder.freshId();
            mPacketList.add(packet.getIpInfo());
            if (packet.getDataLength()==0 && !packet.fin)
            {
                return ;
            }

                if (packet.getDataLength() != 0)
                {
                    ByteBuffer []buffers=mBufferPool.get(packet.getRawData(), packet.mOffset + packet.mHeaderLength, packet.getDataLength());
                    for (int i=0;i<buffers.length;i++){
                        mReadySend.add(buffers[i]);
                    }

                    try {
                        registering=true;
                        mSelector.wakeup();
                        mChannel.register(mSelector,SelectionKey.OP_WRITE,TCPStatus.this);
                        registering=false;
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                    }
                }
//                    os.write(packet.getRawData(), packet.mOffset + packet.mHeaderLength, packet.getDataLength());
                mLocal.write(mBuilder.build(packet));

                if (packet.fin){
                    mLocal.write(mBuilder.build(packet,null,true));
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

//        class ReadThread extends Thread
//        {
//            ByteBuffer mBuffer=ByteBuffer.allocate(65535*100);
//
//            @Override
//            public void run() {
//                super.run();
//                try{
//                    int len;
//                    while (true)
//                    {
//                        len=0;
//                        while ((len=is.available())==0);
//                        read(is,mBuffer.array(),len);
//                        mBuffer.limit(len);
//
//                        ack(mBuffer);
//                    }
//                }catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//
//            }
//        }
    }

    class ReadThread extends Thread{
        ByteBuffer mBuffer =ByteBuffer.allocate(65535);

        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    while (registering || mSelector.select() == 0) ;
                    Iterator<SelectionKey> keys = mSelector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        SocketChannel channel = (SocketChannel) key.channel();
                        TCPStatus status = (TCPStatus) key.attachment();
                        if (key.isConnectable()) {
                            //Log.e("xx", "key connect");
                            if (channel.isConnectionPending()) {
                                if (channel.finishConnect())
                                {
                                    channel.register(mSelector,SelectionKey.OP_READ,status);
                                    Log.e("xx","real connect");
                                }
                            }
                        }
                        if (key.isWritable()) {
                            //Log.e("xx", "key write");
                            while (!status.mReadySend.isEmpty()) {
                                ByteBuffer buffer = status.mReadySend.peek();
                                if (buffer.position() == buffer.limit()) {
                                    status.mReadySend.poll();
                                    continue;
                                }

                                try
                                {
                                    channel.write(buffer);
                                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                                    channel.register(mSelector,SelectionKey.OP_READ,status);
                                }catch (Exception e)
                                {
                                    Log.e("xx","when write:"+e.toString());
                                    key.cancel();
                                }
                                break;
                            }
                        }
                        if (key.isValid()&&key.isReadable()) {
                            Log.e("xx", "key read"+((IPPacket)status.mPacketList.getLast()).getDestIp());
                            mBuffer.clear();
                            try
                            {
                                if (channel.read(mBuffer)<0)
                                {
                                    channel.close();
                                    key.cancel();
                                }else {
                                    mBuffer.flip();
                                    status.ack(mBuffer);
                                    channel.register(mSelector,SelectionKey.OP_READ,status);
                                }
                            }catch (Exception e)
                            {
                                Log.e("xx","when write:"+e.toString());
                                key.cancel();
                            }
                        }

                    }
                }


            } catch (IOException e) {
                Log.e("xx", e.toString());
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
