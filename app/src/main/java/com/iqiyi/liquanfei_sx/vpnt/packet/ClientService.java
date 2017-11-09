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
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;

/**
 * Created by liquanfei_sx on 2017/8/7.
 */

public class ClientService extends VpnService{
    static final String TAG="xx";

    private String addr="127.0.0.1";
    private int port=4444;
    private MB mb=new MB();
    private ServerService server=null;

    private OnServerConnectedListener mOnServerConnectedListener=null;

    private ParcelFileDescriptor mInterface;
    DatagramChannel mTunnel;
    OutputStream os=null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("xx","client started");

        if (server!=null)
            return super.onStartCommand(intent, flags, startId);

        bindService(new Intent(this,ServerService.class),new ServiceConnection(){

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

            }
        },BIND_AUTO_CREATE);

        new Thread()
        {
            @Override
            public void run() {
                super.run();
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
                try {
                    while (true) {
                        length = in.read(packet.array());
                        if (length > 0) {
                            // while ((length = in.read(packet.array())) > 0) {
                            // Write the outgoing packet to the tunnel.
                            //Log.e("xx","tun read-write"+new String(packet.array()));
                            packet.limit(length);
                            //debugPacket(packet); // Packet size, Protocol,
                            byte []b=new byte[length];
                            System.arraycopy(packet.array(),0,b,0,length);
                            server.transmit(new IPPacket(b));
                            // source, destination
                            packet.clear();
                            //mTunnel.write(packet);
                            //out.write(packet.array(),0,length);
                        }
                    }
                } catch (IOException e) {
                    Log.e("xx",e.toString());
                }
            }
        }.start();


        return super.onStartCommand(intent, flags, startId);
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

    public void setOnServerConnectedListener(OnServerConnectedListener l)
    {
        mOnServerConnectedListener=l;
        if (server!=null)
            l.onConnected();
    }

    public void removeOnServerConnectedListener()
    {
        mOnServerConnectedListener=null;
    }

    public void setOnPacketAddListener(OnPacketAddListener l)
    {
        server.setOnPacketAddListener(l);
    }

    public void setOnPacketsAddListener(OnPacketsAddListener l)
    {
        server.setOnPacketsAddListener(l);
    }

    public void removeOnPacketsAddListener()
    {
        server.removeOnPacketsAddListener();
    }

    public void removeAllListener()
    {
        removeOnPacketsAddListener();
        removeOnServerConnectedListener();
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
            builder.addAddress("10.0.10.0", 32);
            //builder.addAddress("127.0.0.1", port);
            builder.addRoute("0.0.0.0", 0);
            mInterface = builder.establish();
        }
    }

    public ArrayList<LocalPackets.PacketList> getPackets()
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
