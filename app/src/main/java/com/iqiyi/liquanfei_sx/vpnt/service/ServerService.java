package com.iqiyi.liquanfei_sx.vpnt.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteBufferPool;
import com.iqiyi.liquanfei_sx.vpnt.packet.IPPacket;
import com.iqiyi.liquanfei_sx.vpnt.packet.Packet;
import com.iqiyi.liquanfei_sx.vpnt.packet.TCPPacket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by liquanfei_sx on 2017/8/11
 */

public class ServerService extends Service {

    private MB mB=new MB();
    private static AppPortList mPortList;
    private ClientService mLocal=null;
    private Selector mSelector= null;
    Queue<Key> prepareRegister=new LinkedList<>();
    private TransmitThread mTransmitThread=null;
    private WriteThread mWriteThread=null;
    private ReadThread mReadThread=null;

    private ListenerInfo mListenerInfo=new ListenerInfo();

    public static ArrayList<PacketList> mPackets=new ArrayList<>();

    private ByteBufferPool mBufferPool=ByteBufferPool.getDefault();

    private boolean registering=false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("xx","server started");
        mWriteThread=new WriteThread();
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
                mPortList=AppPortList.get(ServerService.this);
                try {
                    mSelector=Selector.open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                super.run();
                while (mLocal==null);
                mReadThread.start();
                mTransmitThread.start();
                mWriteThread.start();
            }
        }.start();

    }

    public void stopDaemon()
    {
        mTransmitThread.pause();
    }

    void setOnPacketAddListener(ClientService.OnPacketAddListener l)
    {
        mListenerInfo.mOnPacketAddListener=l;
    }

    void setOnPacketsAddListener(ClientService.OnPacketsAddListener l)
    {
        mListenerInfo.mOnPacketsAddListener=l;
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
                    }else
                    {
                        if (false||tcp.fin)
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
                                mWriteThread.write(new TCPPacket.Builder(tcp)
                                        .setDest(tcp.getIpInfo().getSourceIpB())
                                        .setSource(tcp.getIpInfo().getDestIpB())
                                        .build(tcp,null,TCPPacket.RST));
                                //mThreadPool.execute(new ConnectRunnable(tcp));
                                //new ConnectRunnable(tcp).run();
                            }else
                            {
                                mThreadPool.execute(new ACKRunnable(tcp,status));
                                //new ACKRunnable(tcp,status).run();
                            }
                        }
                    }
                    Log.e("xx","thread pool size:"+((ThreadPoolExecutor)mThreadPool).getPoolSize());
                }
            }


        }

        public void remove(TCPStatus status)
        {
            mSockets.remove(status.mPacketList.mSPort);
        }

        TCPStatus connect(Packet packet)
        {
            if (!(packet instanceof TCPPacket))
                return null;

            TCPStatus status;
            try {
                status = new TCPStatus((TCPPacket) packet);
            } catch (IOException e) {
                return null;
            }

            mSockets.put(((TCPPacket) packet).getSourcePort(),status);
            return status;
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
                if (connect(packet)!=null)   //connect successfully and send sync-ack
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
    }

    private class WriteThread extends Thread
    {
        Queue<Packet> mReadyWrite=new ConcurrentLinkedQueue<>();
        boolean mPause=true;

        @Override
        public void run() {
            super.run();
            mPause=false;
            while(!mPause)
            {
                Packet p;
                try {
                    p = mReadyWrite.poll();
                }catch (Exception e)
                {
                    continue;
                }

                if (p==null)
                    continue;
                mLocal.write(p);
            }
        }

        void write(Packet packet)
        {
            mReadyWrite.add(packet);
            //mLocal.write(packet);
        }
    }

    static class SendEntry
    {
        SendEntry(Packet p)
        {
            packet=p;
        }

        Queue<ByteBuffer> mReadySend=new LinkedList<>();
        Packet packet;
    }

    public class TCPStatus {
        //Socket mSocket;
//        InputStream is;
//        OutputStream os;
        SocketChannel mChannel;
        boolean closed=false;
        PacketList mPacketList;
        Queue<SendEntry> mReadySend=new LinkedList<>();
        int mPosition=0;

        private TCPPacket.Builder mBuilder;

        public TCPStatus(TCPPacket packet) throws IOException {

            mPacketList=new PacketList(packet);
            if (mPacketList.mInfo!=null)
            {
                mPackets.add(mPacketList);
                mPosition=mPackets.size()-1;
                if (mListenerInfo.mOnPacketsAddListener!=null)
                {
                    mListenerInfo.mOnPacketsAddListener.onPacketsAdd(mPackets.size()-1);
                }
            }
            mBuilder=new TCPPacket.Builder(packet)
                    .setDest(packet.getIpInfo().getSourceIpB())
                    .setSource(packet.getIpInfo().getDestIpB());

            mChannel=SocketChannel.open();
            mLocal.protect(mChannel.socket());
            mChannel.configureBlocking(false);
            prepareRegister.add(new Key(mChannel,SelectionKey.OP_CONNECT,TCPStatus.this));
            mSelector.wakeup();
            mChannel.connect(new InetSocketAddress(packet.getDestIp(),packet.getPort()));
            registering=false;
            //ack(packet);
        }

        public void close()
        {
            try {
                fin();
                if (closed)
                {
                    mChannel.close();
                    mTransmitThread.remove(this);
                }
                closed=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void fin()
        {
            mWriteThread.write(mBuilder.build(mPacketList.getLast(),null,TCPPacket.RST));
        }

        public synchronized void ack(TCPPacket packet)      //避免发送队列混乱
        {
            mPacketList.add(packet);
            if (mListenerInfo.mOnPacketAddListener!=null)
            {
                mListenerInfo.mOnPacketAddListener.onPacketAdd(mPosition,mPacketList.size()-1);
            }
            if (!packet.syn&&packet.getDataLength()==0 && !packet.fin)
            {
                return ;
            }

            if (packet.syn)
            {
                mWriteThread.write(mBuilder.build(packet));
            }

            //Log.e("write will add :",new String(packet.getRawData(),packet.mOffset+packet.mHeaderLength,packet.getDataLength()));

                if (packet.getDataLength() != 0)
                {
                    SendEntry se=new SendEntry(mBuilder.build(packet));
                    ByteBuffer []buffers=mBufferPool.get(packet.getRawData(), packet.mOffset + packet.mHeaderLength, packet.getDataLength());
                        for (int i = 0; i < buffers.length; i++) {
                            se.mReadySend.add(buffers[i]);
                            //Log.e("write added :",new String(buffers[i].array(),0,buffers[i].limit()));
                        }
                    mReadySend.add(se);

                    //Log.e("xx", "add write"+mPacketList.mInfo.appName+":"+buffers.length+":"+packet.getDataLength());

                    prepareRegister.add(new Key(mChannel,SelectionKey.OP_WRITE,TCPStatus.this));
                    mSelector.wakeup();
//                    try {
//                        registering=true;
//                        mSelector.wakeup();
//                        mChannel.register(mSelector,SelectionKey.OP_WRITE,TCPStatus.this);
//                        registering=false;
//                    } catch (ClosedChannelException e) {
//                        Log.e("xx", "close "+mPacketList.mInfo.appName);
//                    }
                }

                if (packet.fin||packet.rst){
                    if (closed)
                    {
                        try {
                            mChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mTransmitThread.remove(this);
                    }else
                        closed=true;
                }
        }

        public synchronized void ack(ByteBuffer data)
        {
            TCPPacket packet=mPacketList.getLast();
            if (packet.getPort()!=666666)
                try {
                    Log.e("6666","recv"+packet.getPort()+":"+new String(packet.getRawData(),packet.mOffset+packet.mHeaderLength,packet.getDataLength(),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            mPacketList.add((TCPPacket) mBuilder.build(packet,data).getData());
            mWriteThread.write(mPacketList.getLast());
            if (mListenerInfo.mOnPacketAddListener!=null)
            {
                mListenerInfo.mOnPacketAddListener.onPacketAdd(mPosition,mPacketList.size()-1);
            }
        }
    }

    static class Key
    {
        SocketChannel channel;
        int op;
        TCPStatus status;

        Key(SocketChannel c,int o,TCPStatus s)
        {
            channel=c;
            op=o;
            status=s;
        }
    }

    class ReadThread extends Thread{

        ByteBuffer mBuffer =ByteBuffer.allocate(65535);

        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    while (!prepareRegister.isEmpty())
                    {
                        Key key;
                        try {
                            key= prepareRegister.poll();
                        }catch (NoSuchElementException e)
                        {
                            break;
                        }
                        try {
                            key.channel.register(mSelector, key.op, key.status);
                        }catch (CancelledKeyException e)
                        {

                        }
                    }

                    try {
                        if (mSelector.select() <= 0)
                            continue;
                    }catch (CancelledKeyException e)
                    {

                    }
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
                                    //prepareRegister.add(new Key(channel,SelectionKey.OP_READ,status));
                                    //channel.register(mSelector,SelectionKey.OP_READ,status);
                                    key.interestOps(key.interestOps()|SelectionKey.OP_READ);
                                    status.ack(status.mPacketList.get(0));
                                    //prepareRegister.add(new Key(channel,SelectionKey.OP_CONNECT,status));
                                    //mSelector.wakeup();
                                    if (status.mPacketList.mInfo!=null)
                                    Log.e("xx","real connect:"+status.mPacketList.mInfo.appName+":"+status.mPacketList.port()+":"+status.mPacketList.ip());
                                }
                            }
                        }
                        if (key.isWritable()) {
                            if (status.mPacketList.mInfo!=null)
                            Log.e("xx", "key write"+status.mPacketList.mInfo.appName);
                            if (!status.mReadySend.isEmpty())
                            {
                                while (status.mReadySend.peek()!=null) {
                                    SendEntry se = status.mReadySend.peek();
                                    ByteBuffer buffer=se.mReadySend.peek();
                                    if (buffer==null) {
                                        mWriteThread.write(se.packet);
                                        status.mReadySend.poll();
                                        continue;
                                    }

                                    if (buffer.position() == buffer.limit()) {
                                        se.mReadySend.poll();
                                        mBufferPool.recycle(buffer);
                                        continue;
                                    }

                                    try
                                    {
                                        int start=buffer.position();
                                        channel.write(buffer);
                                        if (true)
                                        {
                                            Log.e("written :",new String(buffer.array(),start,buffer.position()));
                                        }
                                    }catch (Exception e)
                                    {
                                        Log.e("xx","when write:"+e.toString());
                                        key.cancel();
                                    }
                                    break;
                                }
                            }else
                            {
                                //Log.e("xx", "key write no data"+status.mPacketList.mInfo.appName);
                                key.interestOps((key.interestOps()|SelectionKey.OP_READ)&~SelectionKey.OP_WRITE);
                                //channel.register(mSelector,SelectionKey.OP_READ,status);
                            }
                        }
                        if (key.isValid()&&key.isReadable()) {
                            //Log.e("xx", "key read"+((IPPacket)status.mPacketList.getLast()).getDestIp());
                            mBuffer.clear();
                            try
                            {
                                if (status.closed||channel.read(mBuffer)<0)
                                {
                                    status.close();
                                    key.cancel();
                                }else {
                                    mBuffer.flip();
                                    status.ack(mBuffer);
                                    key.interestOps(key.interestOps()|SelectionKey.OP_READ);
                                    //channel.register(mSelector,SelectionKey.OP_READ,status);
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

    public static class PacketList
    {
        AppPortList.AppInfo mInfo;
        public int mSPort,mDPort;
        private String ip;
        private ArrayList<TCPPacket> packets;

        PacketList(TCPPacket init)
        {
            packets=new ArrayList<>();
            add(init);
            mSPort =init.getSourcePort();
            mDPort=init.getPort();
            ip=init.getDestIp();
            mInfo=mPortList.getAppInfo(mSPort);
            if (mInfo!=null)
            Log.e("xx","find app:"+mInfo.info.packageName);
        }

        public int size()
        {
            return packets.size();
        }

        void add(TCPPacket p)
        {
            packets.add(p);
        }

        public TCPPacket get(int i){
            return packets.get(i);
        }

        public int port()
        {
            return mDPort;
        }

        public String ip()
        {
            return ip;
        }

        public AppPortList.AppInfo info()
        {
            return mInfo;
        }

        TCPPacket getLast() {
            return packets.get(packets.size()-1);
        }
    }

    static class ListenerInfo
    {
        private ClientService.OnPacketAddListener mOnPacketAddListener;
        private ClientService.OnPacketsAddListener mOnPacketsAddListener;
    }
}
