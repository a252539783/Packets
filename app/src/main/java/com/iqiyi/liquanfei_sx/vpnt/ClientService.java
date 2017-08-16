package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private InetSocketAddress mServer=null;

    private ParcelFileDescriptor mInterface;
    DatagramChannel mTunnel=null;
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                mServer=new InetSocketAddress(addr,port);
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
                }
                build();

                /*
                new Thread() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            DatagramSocket socket = new DatagramSocket(port);
                            DatagramPacket packet = new DatagramPacket(new byte[255],
                                    255);
                            protect(socket);
                            //Socket ss=new Socket("127.0.0.1",port);
                            //InputStream is=ss.getInputStream();
                            //OutputStream os=ss.getOutputStream();
                            //byte[] b;
                            while (true) {
                                try {
                                    //b=new byte[255];
                                    //socket.connect(new InetSocketAddress(port));
                                    socket.receive(packet);
                                    //is.read(b);
                                    Log.e("xx","socket read "+packet.getSocketAddress());
                                    //os.write(b);
                                    //socket.connect(new InetSocketAddress(packet.getAddress().getHostAddress(),packet.getPort()));
                                    socket.send(packet);
                                    packet.setLength(255);

                                } catch (IOException e) {
                                    Log.e("xx",""+e.toString());
                                }

                            }
                        } catch (SocketException e) {
                            Log.e("xx",""+e.toString());
                        }
                    }

                }.start();
*/

                new Thread() {
                    public void run() {
                        // Packets to be sent are queued in this input stream.
                        FileInputStream in = new FileInputStream(
                                mInterface.getFileDescriptor());
                        FileOutputStream out=new FileOutputStream(mInterface.getFileDescriptor());
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
                                    new IPPacket(packet.array());
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

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private void build()
    {
        if (mInterface==null) {
            Builder builder = new Builder();
            builder.setMtu(1500);
            builder.addAddress("10.0.2.0", 32);
            //builder.addAddress("127.0.0.1", port);
            builder.addRoute("0.0.0.0", 0);
            mInterface = builder.establish();
        }
    }

    private void debugPacket(ByteBuffer packet) {
		/*
		 * for(int i = 0; i < length; ++i) { byte buffer = packet.get();
		 *
		 * Log.d(TAG, "byte:"+buffer); }
		 */

        int buffer = packet.get();
        int version;
        int headerlength;
        version = buffer >> 4;          //4 or 6
        headerlength = buffer & 0x0F;
        headerlength *= 4;
        Log.e(TAG, "IP Version:" + version);
        Log.e(TAG, "Header Length:" + headerlength);

        String status = "";
        status += "Header Length:" + headerlength;

        buffer = packet.get(); // DSCP + EN
        buffer = packet.getChar(); // Total Length

        Log.e(TAG, "Total Length:" + buffer);

        buffer = packet.getChar(); // Identification
        buffer = packet.getChar(); // Flags + Fragment Offset
        buffer = packet.get(); // Time to Live
        buffer = packet.get(); // Protocol  1表示为ICMP协议， 2表示为IGMP协议， 6表示为TCP协议， 1 7表示为UDP协议。

        Log.e(TAG, "Protocol:" + buffer);

        status += "  Protocol:" + buffer;

        buffer = packet.getChar(); // Header checksum

        String sourceIP = "";
        buffer = packet.get()&0xff; // Source IP 1st Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get()&0xff; // Source IP 2nd Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get()&0xff; // Source IP 3rd Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get()&0xff; // Source IP 4th Octet
        sourceIP += buffer;

        Log.e(TAG, "Source IP:" + sourceIP);

        status += "   Source IP:" + sourceIP;

        String destIP = "";
        buffer = packet.get()&0xff; // Destination IP 1st Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get()&0xff; // Destination IP 2nd Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get()&0xff; // Destination IP 3rd Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get()&0xff; // Destination IP 4th Octet
        destIP += buffer;

        Log.e(TAG, "Destination IP:" + destIP);

        status += "   Destination IP:" + destIP;

    }


    class MB extends Binder
    {
        ClientService get()
        {
            return ClientService.this;
        }
    }
}
