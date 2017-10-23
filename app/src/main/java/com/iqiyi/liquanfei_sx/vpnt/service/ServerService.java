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
import java.nio.channels.ClosedChannelException;
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

    private MB mB = new MB();
    private static AppPortList mPortList;
    private ClientService mLocal = null;
    private Selector mSelector = null;
    Queue<Key> prepareRegister = new ConcurrentLinkedQueue<>();
    private TransmitThread mTransmitThread = null;
    private WriteThread mWriteThread = null;
    private ReadThread mReadThread = null;

    private ListenerInfo mListenerInfo = new ListenerInfo();

    public static ArrayList<PacketList> mPackets = new ArrayList<>();

    private ByteBufferPool mBufferPool = ByteBufferPool.getDefault();

    private boolean registering = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("xx", "server started");
        mWriteThread = new WriteThread();
        mTransmitThread = new TransmitThread();
        mReadThread = new ReadThread();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        bindService(new Intent(ServerService.this, ClientService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mLocal = ((ClientService.MB) service).get();
                Log.e("xx", "bind to client");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
        return mB;
    }

    public void startDaemon() {
        new Thread() {
            @Override
            public void run() {
                mPortList = AppPortList.get(ServerService.this);
                try {
                    mSelector = Selector.open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                super.run();
                while (mLocal == null) ;
                mReadThread.start();
                mTransmitThread.start();
                mWriteThread.start();
            }
        }.start();

    }

    public void stopDaemon() {
        mTransmitThread.pause();
    }

    void setOnPacketAddListener(ClientService.OnPacketAddListener l) {
        mListenerInfo.mOnPacketAddListener = l;
    }

    void setOnPacketsAddListener(ClientService.OnPacketsAddListener l) {
        mListenerInfo.mOnPacketsAddListener = l;
    }

    void removeOnPacketsAddListener() {
        mListenerInfo.mOnPacketsAddListener = null;
    }

    public boolean transmit(Packet packet) {
        //Log.e("xx","add");
        mTransmitThread.mPackets.add(packet);
        if (!mTransmitThread.mPause)
            return false;

        return true;
    }

    public void setLocal(ClientService local) {
        mLocal = local;
    }

    class MB extends Binder {
        ServerService get() {
            return ServerService.this;
        }
    }

    private class TransmitThread extends Thread {
        LinkedBlockingQueue<Packet> mPackets;
        SparseArray<TCPStatus> mSockets;
        private ExecutorService mThreadPool;

        boolean mPause = true;

        TransmitThread() {
            mSockets = new SparseArray<>();
            mPackets = new LinkedBlockingQueue<>();
            mThreadPool = Executors.newCachedThreadPool();
        }

        @Override
        public void run() {
            super.run();
            mPause = false;
            Log.e("xx", "daemon started");
            while (!mPause || !mPackets.isEmpty()) {
                Packet packet = mPackets.poll();
                doTransmit(packet);
            }
            Log.e("xx", "daemon ended");
        }

        private void doTransmit(Packet packet) {
            if (packet instanceof IPPacket) {
                IPPacket ip = (IPPacket) packet;

                if (ip.getData() instanceof TCPPacket) {
                    Log.e("xx", "transmit tcp packet:");
                    TCPPacket tcp = (TCPPacket) ip.getData();
                    if (tcp.syn) {
                        Log.e("xx", "transmit tcp sync:");
                        mThreadPool.execute(new ConnectRunnable(tcp));
                    } else {
                        if (false && tcp.fin) {
                            Log.e("xx", "transmit tcp fin:");
                            TCPStatus status = mSockets.get(tcp.getSourcePort());
                            if (status != null) {
                                status.close();
                                mSockets.remove(tcp.getSourcePort());
                            }
                        } else {
                            TCPStatus status = mSockets.get(tcp.getSourcePort());
                            if (status == null) {
                                mWriteThread.write(new TCPPacket.Builder(tcp)
                                        .setDest(tcp.getIpInfo().getSourceIpB())
                                        .setSource(tcp.getIpInfo().getDestIpB())
                                        .build(tcp, null, TCPPacket.RST));
                                //mThreadPool.execute(new ConnectRunnable(tcp));
                                //new ConnectRunnable(tcp).run();
                            } else {
                                /**由于线程分配问题，此处如果直接简单的提交任务将有可能无法保持想要的任务顺序
                                 * 所以在任务提交之前先把待处理的任务放入转发队列，在实际转发的过程中判断任务是否处理完毕即可
                                 */
                                SendEntry se = new SendEntry(tcp);
                                status.mReadySend.add(se);
                                mThreadPool.execute(new ACKRunnable(se, status));
                                //new ACKRunnable(tcp,status).run();
                            }
                        }
                    }
                    Log.e("xx", "thread pool size:" + ((ThreadPoolExecutor) mThreadPool).getPoolSize());
                }
            }


        }

        public void remove(TCPStatus status) {
            mSockets.remove(status.mPacketList.mSPort);
        }

        synchronized TCPStatus connect(Packet packet) {
            if (!(packet instanceof TCPPacket))
                return null;

            TCPStatus status;
            try {
                status = new TCPStatus((TCPPacket) packet);
            } catch (IOException e) {
                return null;
            }

            mSockets.put(((TCPPacket) packet).getSourcePort(), status);
            return status;
        }

        void pause() {
            mPause = true;
        }


        class ConnectRunnable implements Runnable {
            private TCPPacket packet;

            ConnectRunnable(TCPPacket packet) {
                this.packet = packet;
            }

            @Override
            public void run() {
                if (connect(packet) != null)   //connect successfully and send sync-ack
                {
                    //Log.e("xx","connected successfully");
                }
            }
        }

        class ACKRunnable implements Runnable {
            private SendEntry packet;
            private TCPStatus status;

            ACKRunnable(SendEntry packet, TCPStatus status) {
                this.packet = packet;
                this.status = status;
            }

            @Override
            public void run() {
                status.ack(packet);
            }
        }
    }

    private class WriteThread extends Thread {
        Queue<Packet> mReadyWrite = new ConcurrentLinkedQueue<>();
        boolean mPause = true;

        @Override
        public void run() {
            super.run();
            mPause = false;
            while (!mPause) {
                Packet p;
                try {
                    p = mReadyWrite.poll();
                } catch (Exception e) {
                    continue;
                }

                if (p == null)
                    continue;

                while (!mLocal.write(p));
            }
        }

        void write(Packet packet) {
            mReadyWrite.add(packet);
            //mLocal.write(packet);
        }
    }

    /**
     * 转发队列的项目
     */
    static class SendEntry {
        SendEntry(TCPPacket p) {
            packet = p;

            /**
             * DataLength!=0 表明该项目会被转发(不是syn/fin/rst等)
             */
            if (packet.getDataLength() != 0)
                available = true;
        }

        boolean available = false;    //是否会被转发
        Queue<ByteBuffer> mReadySend = null;      //转发数据可能太大，该队列是转发数据分包的结果

        /**
         * 构造时表示要被转发的数据包；当要被转发的数据包进行分包保存后，改值被设置为被转发数据包的回复包
         */
        TCPPacket packet;
    }

    public class TCPStatus {
        //Socket mSocket;
//        InputStream is;
//        OutputStream os;
        SocketChannel mChannel;
        boolean closed = false;
        PacketList mPacketList;
        Queue<SendEntry> mReadySend = new LinkedList<>();
        int mPosition = 0;

        private TCPPacket.Builder mBuilder;

        public TCPStatus(TCPPacket packet) throws IOException {

            mPacketList = new PacketList(packet);
            if (mPacketList.mInfo != null) {
                mPackets.add(mPacketList);
                mPosition = mPackets.size() - 1;
                if (mListenerInfo.mOnPacketsAddListener != null) {
                    mListenerInfo.mOnPacketsAddListener.onPacketsAdd(mPackets.size() - 1);
                }
            }
            mBuilder = new TCPPacket.Builder(packet)
                    .setDest(packet.getIpInfo().getSourceIpB())
                    .setSource(packet.getIpInfo().getDestIpB());

            mChannel = SocketChannel.open();
            mLocal.protect(mChannel.socket());
            mChannel.configureBlocking(false);
            prepareRegister.add(new Key(mChannel, SelectionKey.OP_CONNECT, TCPStatus.this));
            mSelector.wakeup();
            mChannel.connect(new InetSocketAddress(packet.getDestIp(), packet.getPort()));
            registering = false;
            //ack(packet);
        }

        void close() {
            try {
                fin();
                if (closed) {
                    mChannel.close();
                    Log.e("xx", "local close");
                    mTransmitThread.remove(this);
                }
                closed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void reset() {
            try {
                rst();
                if (closed) {
                    mChannel.close();
                    Log.e("xx", "local reset");
                    mTransmitThread.remove(this);
                }
                closed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void fin() {
            mPacketList.add((TCPPacket) mBuilder.build(mPacketList.getLast(), null, TCPPacket.FIN).getData());
            mWriteThread.write(mPacketList.getLast());
        }

        void rst() {
            mPacketList.add((TCPPacket) mBuilder.build(mPacketList.getLast(), null, TCPPacket.RST).getData());
            mWriteThread.write(mPacketList.getLast());
        }

        public void ack(SendEntry se)      //避免发送队列混乱
        {
            if (se.packet.syn) {
                se.available = false;
                mWriteThread.write(mBuilder.build(se.packet));
                return;
            }

            mPacketList.add(se.packet);
            if (mListenerInfo.mOnPacketAddListener != null) {
                mListenerInfo.mOnPacketAddListener.onPacketAdd(mPosition, mPacketList.size() - 1);
            }

            if (se.packet.fin || se.packet.rst) {
                se.available = false;
                mPacketList.add((TCPPacket) mBuilder.build(se.packet).getData());
                mWriteThread.write(mPacketList.getLast());
            }

            if (!se.packet.syn && se.packet.getDataLength() == 0 && !se.packet.fin && !se.packet.rst) {
                se.available = false;
                return;
            }

            if (se.packet.getDataLength() != 0) {
                se.mReadySend = new LinkedList<>();
                /**分包*/
                ByteBuffer[] buffers = mBufferPool.get(se.packet.getRawData(), se.packet.mOffset + se.packet.mHeaderLength, se.packet.getDataLength());
                for (int i = 0; i < buffers.length; i++) {
                    se.mReadySend.add(buffers[i]);
                    //Log.e("write added :", new String(buffers[i].array(), 0, buffers[i].limit()));
                }
                /**设置回复包*/
                se.packet = (TCPPacket) mBuilder.build(se.packet).getData();

                prepareRegister.add(new Key(mChannel, SelectionKey.OP_WRITE, TCPStatus.this));
                mSelector.wakeup();
            }

            if (se.packet.fin) {
                se.available = false;
                if (closed) {
                    try {
                        mChannel.close();
                        Log.e("xx", "local close");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mTransmitThread.remove(this);
                } else {
                    closed = true;
                    close();
                }
            }

            if (se.packet.rst) {
                se.available = false;
                if (closed) {
                    try {
                        mChannel.close();
                        Log.e("xx", "local reset");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mTransmitThread.remove(this);
                } else {
                    closed = true;
                    reset();
                }
            }
        }

        public void ack(ByteBuffer data) {
            TCPPacket packet = mPacketList.getLast();
            if (packet.getPort() != 666666)
                try {
                    Log.e("6666", "recv" + packet.getPort() + ":" + new String(packet.getRawData(), packet.mOffset + packet.mHeaderLength, packet.getDataLength(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            mPacketList.add((TCPPacket) mBuilder.build(packet, data).getData());
            mWriteThread.write(mPacketList.getLast());
            if (mListenerInfo.mOnPacketAddListener != null) {
                mListenerInfo.mOnPacketAddListener.onPacketAdd(mPosition, mPacketList.size() - 1);
            }
        }
    }

    static class Key {
        SocketChannel channel;
        int op;
        TCPStatus status;

        Key(SocketChannel c, int o, TCPStatus s) {
            channel = c;
            op = o;
            status = s;
        }
    }

    class ReadThread extends Thread {

        ByteBuffer mBuffer = ByteBuffer.allocate(40960);

        @Override
        public void run() {
            super.run();
                while (true) {
                    while (!prepareRegister.isEmpty()) {
                        Key key;
                        try {
                            key = prepareRegister.poll();
                        } catch (NoSuchElementException e) {
                            break;
                        }
                        try {
                            key.channel.register(mSelector, key.op, key.status);
                        } catch (CancelledKeyException e) {

                        } catch (ClosedChannelException e) {

                        }
                    }

                    try {
                        if (mSelector.select() <= 0)
                            continue;
                    } catch (CancelledKeyException e) {

                    } catch (IOException e) {
                        Log.e("xx","selector is Stop!!!!!!!!!!!!!!!!!!!!!!!!!");
                        stopDaemon();
                    }
                    Iterator<SelectionKey> keys = mSelector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        SocketChannel channel = (SocketChannel) key.channel();
                        TCPStatus status = (TCPStatus) key.attachment();
                        try {
                            if (key.isConnectable()) {
                                Log.e("xx", "key connect");
                                if (channel.isConnectionPending()) {
                                    try {
                                        if (channel.finishConnect()) {
                                            //prepareRegister.add(new Key(channel,SelectionKey.OP_READ,status));
                                            //channel.register(mSelector,SelectionKey.OP_READ,status);
                                            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                                            status.ack(new SendEntry(status.mPacketList.get(0)));
                                            //prepareRegister.add(new Key(channel,SelectionKey.OP_CONNECT,status));
                                            //mSelector.wakeup();
                                            if (status.mPacketList.mInfo != null)
                                                Log.e("xx", "real connect:" + status.mPacketList.mInfo.appName + ":" + status.mPacketList.port() + ":" + status.mPacketList.ip());
                                        }
                                    } catch (IOException e) {
                                        Log.e("xx","finishConnect error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                    }
                                }
                            }
                            if (key.isWritable()) {
                                if (!status.mReadySend.isEmpty()) {
                                    while (status.mReadySend.peek() != null) {
                                        SendEntry se = status.mReadySend.peek();
                                        if (!se.available) {
                                            /**找到一个要被转发的包，或者退出*/
                                            status.mReadySend.poll();
                                            continue;
                                        }

                                        /**说明该包还未被处理，下一轮继续判断*/
                                        if (se.mReadySend == null) {
                                            break;
                                        }

                                        ByteBuffer buffer = se.mReadySend.peek();

                                        /**一个数据包已经被转发完成，此时把它的回复包写入本地*/
                                        if (buffer == null) {
                                            mWriteThread.write(se.packet);
                                            status.mReadySend.poll();
                                            continue;
                                        }

                                        /**一个数据包的一小部分转发完成，回收并发送下一个分包(如果未完成的话)*/
                                        if (buffer.position() == buffer.limit()) {
                                            se.mReadySend.poll();
                                            mBufferPool.recycle(buffer);
                                            continue;
                                        }

                                        try {
                                            /**实际转发*/
                                            channel.write(buffer);
                                            if (true) {
                                                //Log.e("written :",new String(buffer.array(),start,buffer.position()));
                                            }
                                        } catch (Exception e) {
                                            Log.e("xx", "when write:" + e.toString());
                                            key.cancel();
                                        }
                                        break;
                                    }
                                } else {
                                    /**转发队列为空，说明有转发任务已经完成,把OP_WRITE去掉，不然key会保留*/
                                    key.interestOps((key.interestOps() | SelectionKey.OP_READ) & ~SelectionKey.OP_WRITE);
                                }
                            }
                            if (key.isValid() && key.isReadable()) {
                                //Log.e("xx", "key read"+((IPPacket)status.mPacketList.getLast()).getDestIp());
                                mBuffer.clear();
                                try {
                                    if (status.closed || channel.read(mBuffer) < 0) {
                                        Log.e("xx", "closed by remote");
                                        status.reset();
                                        key.cancel();
                                    } else {
                                        mBuffer.flip();
                                        status.ack(mBuffer);
                                        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                                        //channel.register(mSelector,SelectionKey.OP_READ,status);
                                    }
                                } catch (Exception e) {
                                    Log.e("xx", "when read:" + e.toString());
                                    key.cancel();
                                }
                            }
                        } catch (CancelledKeyException e) {
                        }
                    }
                }

        }
    }

    public static class PacketList {
        AppPortList.AppInfo mInfo;
        public int mSPort, mDPort;
        private String ip;
        private ArrayList<TCPPacket> packets;

        PacketList(TCPPacket init) {
            packets = new ArrayList<>();
            add(init);
            mSPort = init.getSourcePort();
            mDPort = init.getPort();
            ip = init.getDestIp();
            mInfo = mPortList.getAppInfo(mSPort);
            if (mInfo != null)
                Log.e("xx", "find app:" + mInfo.info.packageName);
        }

        public int size() {
            return packets.size();
        }

        synchronized void add(TCPPacket p) {
            packets.add(p);
        }

        public TCPPacket get(int i) {
            return packets.get(i);
        }

        public int port() {
            return mDPort;
        }

        public String ip() {
            return ip;
        }

        public AppPortList.AppInfo info() {
            return mInfo;
        }

        TCPPacket getLast() {
            return packets.get(packets.size() - 1);
        }
    }

    static class ListenerInfo {
        private ClientService.OnPacketAddListener mOnPacketAddListener;
        private ClientService.OnPacketsAddListener mOnPacketsAddListener;
    }
}
