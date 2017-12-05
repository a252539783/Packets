package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.tools.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/20.
 */

public class PacketList extends Filter<PacketList.PacketItem>{

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
    private List<PacketItem> packets;
    int mIndex=0;

    public boolean mAlive=true;

    private int mLast=0;

    /**
     * 用于新产生的数据包
     */
    PacketList(TCPPacket init,int index) {
        super(1,new ArrayList<PacketItem>());
        setKey(NON_EMPTY,null,false);
        mIndex=index;
        packets = getSrc();
        mSPort = init.getSourcePort();
        mDPort = init.getPort();
        ip = init.getDestIp();
        mInfo = AppPortList.get().getAppInfo(mSPort);
        add(init,true);
    }

    /**
     * 用于读取存储于本地的历史数据包
     */
    PacketList(TCPPacket init,int index,long time,int uid)
    {
        super(7,new ArrayList<PacketItem>());
        setKey(NON_EMPTY,null,false);
        mAlive=false;

        mIndex=index;
        packets = getSrc();
        mSPort = init.getSourcePort();
        mDPort = init.getPort();
        ip = init.getDestIp();
        mInfo = AppPortList.get().getAppByUid(uid);
        add(init,time);
    }

    /**
     * 用于添加新产生的数据包
     */
    synchronized boolean add(TCPPacket p,boolean local) {
        PacketItem item=new PacketItem(System.nanoTime(),p);
        boolean res=super.add(item);
        LocalPackets.mgr().addRequest(PersistRequest.newWriteRequest(item.mTime,this,p));

        if (local)
        {
            mLast=packets.size()-1;
        }

        if (p.fin||p.rst)
            mAlive=false;

        return res;
    }

    boolean add(TCPPacket p,long time)
    {
        PacketItem item=new PacketItem(time,p);
        return super.add(item);
    }

    @Override
    public boolean filter(int key, PacketItem o) {
        if ((key&NON_EMPTY)!=0)
        {
            return o.mPacket.getDataLength() != 0;
        }

        return true;
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