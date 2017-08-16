package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by liquanfei_sx on 2017/8/11
 */

public class ServerService extends Service {

    private MB mB=new MB();
    private ClientService mLocal=null;
    private TransmitThread mTransmitThread=null;

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

        boolean mPause=true;

        TransmitThread()
        {
            mSockets=new SparseArray<>();
            mPackets=new LinkedBlockingQueue<>();
        }

        @Override
        public void run() {
            super.run();
            mPause=false;

            while (!mPause||mPackets.isEmpty())
            {

            }
        }

        boolean connect(Packet packet)
        {
            if (!(packet instanceof TCPPacket))
                return false;

            TCPStatus status=null;
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
    }

    static class TCPStatus {
        public static final int SYN_CONNECT = 0;
        public static final int ACK_CONNECT = 1;
        public static final int FIN_CONNECT = 2;

        Socket mSocket;
        int mStatus;
        InputStream is;
        OutputStream os;
        ReadThread mReadThread;

        public TCPStatus(TCPPacket packet) throws IOException {
            mSocket=new Socket(packet.getDestIp(),packet.getPort());
            mReadThread=new ReadThread();
        }

        static class ReadThread extends Thread
        {
            @Override
            public void run() {
                super.run();
            }
        }
    }
}
