package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by liquanfei_sx on 2017/8/7.
 */

public class ClientService extends VpnService{

    static boolean debug=true;

    public static String addr="10.0.10.0";
    private int port=4444;
    private MB mb=new MB();
    private ServerService server=null;
    private boolean mRunning=false;

    private ServiceConnection mConn=new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            server=((ServerService.MB)service).get();

            if (mOnServerConnectedListener!=null)
            {
                mOnServerConnectedListener.onConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            server=null;
        }
    };

    private static boolean mAlreadyRun=false;

    private OnServerConnectedListener mOnServerConnectedListener=null;

    private ParcelFileDescriptor mInterface;
    OutputStream os=null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mAlreadyRun){
            server.stopDaemon();
            unbindService(mConn);
            server=null;
            return super.onStartCommand(intent, flags, startId);
        }

        mAlreadyRun=true;

        bindService(new Intent(this,ServerService.class),mConn,BIND_AUTO_CREATE);

        new Thread()
        {
            @Override
            public void run() {
                super.run();
                mRunning=true;
                setPriority(MAX_PRIORITY);

                while (server==null);
                server.startDaemon();
                build();


                // Packets to be sent are queued in this input stream.
                FileInputStream in = new FileInputStream(
                        mInterface.getFileDescriptor());
                os=new FileOutputStream(mInterface.getFileDescriptor());
                // Allocate the mBuffer for a single packet.
                ByteBuffer packet = ByteBuffer.allocate(32767);
                int length;
                int errorTime=0;
                    while (mRunning) {
                        if (server!=null) {//此时可能已经进入了停止阶段，等待ServerService解除绑定后停止自身
                        try {
                            length = in.read(packet.array());
                            if (length > 0) {
                                packet.limit(length);
                                byte []b=new byte[length];
                                System.arraycopy(packet.array(),0,b,0,length);
                                    server.transmit(new IPPacket(b));
                                packet.clear();
                            }
                            errorTime=0;
                        } catch (IOException e) {
                            if (errorTime>10) {//连续出错10次
                                server.stopDaemon();
                                while(mRunning);
                            }
                            else
                                errorTime++;
                        }
                        }
                    }

                    if (debug)
                    Log.e("xx","client end");
                stopSelf();

            }
        }.start();


        return super.onStartCommand(intent, flags, startId);
    }

    void stop()
    {
        mRunning=false;
        try {
            mInterface.close();
        } catch (IOException e) {
            //报错不管
        }
    }

    @Override
    public void onDestroy() {
        mAlreadyRun=false;
        if (debug)
        Log.e("xx","client service dead");
        super.onDestroy();
    }

    public void inject(TCPPacket packet)
    {
        server.inject(packet);
    }

    public static boolean isRun()
    {
        return mAlreadyRun;
    }

    public boolean write(Packet packet)
    {
        try {
            os.write(packet.getRawData());
        } catch (IOException e) {
            Log.e("xx","when write to tunnel:"+e.toString());
            return false;
        }

        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mb;
    }

    private void build()
    {
        if (mInterface==null) {
            Builder builder = new Builder();
            builder.setMtu(1500);
            builder.addAddress(addr, 32);
            //builder.addAddress("127.0.0.1", port);
            builder.addRoute("0.0.0.0", 0);
            mInterface = builder.establish();
        }
    }

    public ArrayList<PacketList> getPackets()
    {
        return ServerService.mPackets;
    }

    public class MB extends Binder
    {
        public ClientService get()
        {
            return ClientService.this;
        }
    }

    public interface OnPacketsAddListener
    {
        void onPacketsAdd(int position);
    }

    public interface OnPacketAddListener
    {
        void onPacketAdd(int packetsPosition,int position);
    }

    public interface OnServerConnectedListener
    {
        void onConnected();
    }
}
