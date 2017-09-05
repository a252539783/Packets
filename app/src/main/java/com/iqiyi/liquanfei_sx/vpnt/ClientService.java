package com.iqiyi.liquanfei_sx.vpnt;

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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by liquanfei_sx on 2017/8/7.
 */

public class ClientService extends VpnService{
    static final String TAG="xx";

    private String addr="127.0.0.1";
    private int port=4444;
    private MB mb=new MB();
    private InetSocketAddress mServer=null;
    private ServerService server=null;

    private ParcelFileDescriptor mInterface;
    DatagramChannel mTunnel;
    OutputStream os=null;
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("xx","client started");

        bindService(new Intent(this,ServerService.class),new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                server=((ServerService.MB)service).get();
                Log.e("xx","connected to server");
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

                while (server==null);
                server.startDaemon();
                /*mServer=new InetSocketAddress(addr,port);
                try {
                    mTunnel=DatagramChannel.open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!protect(mTunnel.socket())) {
                    throw new IllegalStateException("Cannot protect the tunnel");
                }
                try {
                    mTunnel.connect(mServer);
                    mTunnel.configureBlocking(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                build();

                new Thread() {
                    public void run() {
                        // Packets to be sent are queued in this input stream.
                        FileInputStream in = new FileInputStream(
                                mInterface.getFileDescriptor());
                        os=new FileOutputStream(mInterface.getFileDescriptor());
                        // Allocate the buffer for a single packet.
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
            }
        }.start();


        return super.onStartCommand(intent, flags, startId);
    }

    public void write(Packet packet)
    {
        ByteBuffer b=ByteBuffer.wrap(packet.getRawData());
        try {
            os.write(packet.getRawData());
        } catch (IOException e) {
            Log.e("xx","when write to tunnel:"+e.toString());
        }
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

    class MB extends Binder
    {
        ClientService get()
        {
            return ClientService.this;
        }
    }
}
