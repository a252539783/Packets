package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/20.
 */

public class PacketList {
    public class PacketItem
    {
        public long mTime;
        public TCPPacket mPacket;

        public PacketItem(long time,TCPPacket packet){
            mTime=time;
            mPacket=packet;
        }
    }

    AppPortList.AppInfo mInfo;
    public int mSPort, mDPort;
    private String ip;
    private ArrayList<PacketItem> packets;
    int mIndex=0;

    private int mLast=0;

    PacketList(TCPPacket init,int index) {
        mIndex=index;
        packets = new ArrayList<>();
        mSPort = init.getSourcePort();
        mDPort = init.getPort();
        ip = init.getDestIp();
        mInfo = AppPortList.get().getAppInfo(mSPort);
        add(init,true);
        if (mInfo != null)
            Log.e("xx", "find app:" + mInfo.info.packageName);
    }

    PacketList(TCPPacket init,int index,long time,int uid)
    {
        mIndex=index;
        packets = new ArrayList<>();
        mSPort = init.getSourcePort();
        mDPort = init.getPort();
        ip = init.getDestIp();
        mInfo = AppPortList.get().getAppByUid(uid);
        add(init,time);
        if (mInfo != null)
            Log.e("xx", "find app:" + mInfo.info.packageName);
    }

    public int size() {
        return packets.size();
    }

    synchronized PacketItem add(TCPPacket p,boolean local) {
        PacketItem item=new PacketItem(System.nanoTime(),p);
        packets.add(item);
        LocalPackets.mgr().addRequest(PersistRequest.newWriteRequest(item.mTime,this,p));

        if (local)
        {
            mLast=packets.size()-1;
        }

        return item;
    }

    PacketItem add(TCPPacket p,long time)
    {
        PacketItem item=new PacketItem(time,p);
        packets.add(item);

        return item;
    }

    public PacketItem get(int i) {
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
        return packets.get(mLast).mPacket;
    }
}