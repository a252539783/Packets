package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.Constants;
import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteBufferPool;

import java.io.File;
import java.lang.ref.PhantomReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2017/11/7.
 */

public class LocalPackets {

    private static LocalPackets instance=new LocalPackets();

    private List<WeakReference<OnHistoryChangeListener>> mHistoryChangeListeners;
    private List<WeakReference<OnPacketsChangeListener>> mPacketsChangeListeners;
    private List<WeakReference<OnPacketChangeListener>> mPacketChangeListeners;

    List<CaptureInfo> mAllPackets=null;
    private AppPortList mPortList;

    private LocalPackets()
    {
    }

    public void addHistoryChangeListener(OnHistoryChangeListener l)
    {
        if (mHistoryChangeListeners==null)
            mHistoryChangeListeners=new LinkedList<>();

        mHistoryChangeListeners.add(new WeakReference(l));
    }

    public void addPacketsChangeListener(OnPacketsChangeListener l)
    {
        if (mPacketsChangeListeners==null)
            mPacketsChangeListeners=new LinkedList<>();

        mPacketsChangeListeners.add(new WeakReference(l));
    }

    public void addPacketChangeListener(OnPacketChangeListener l)
    {
        if (mPacketChangeListeners==null)
            mPacketChangeListeners=new LinkedList<>();

        mPacketChangeListeners.add(new WeakReference(l));
    }

    synchronized void newHistory(long time)
    {
        if (AppPortList.get()==null)
            AppPortList.init();

        mAllPackets.add(0,new CaptureInfo(time));
        mgr().addRequest(PersistRequest.newCreateRequest(time));
        callHistoryChange();
    }

    synchronized void initHistory(String [] files)
    {
        if (mAllPackets!=null)
            return ;


        if (AppPortList.get()==null)
            AppPortList.init();

        mAllPackets=new ArrayList<>();

        if (files!=null)
        {
            long []times=new long[files.length];
            for (int i=0;i<files.length;i++)
                times[i]=Long.parseLong(files[i]);
            Arrays.sort(times);
            for (long time : times) {
                mAllPackets.add(new CaptureInfo(time));
            }
        }

        callHistoryChange();
    }

    void initPackets(int history,TCPPacket packet,int listIndex,int uid)
    {
        CaptureInfo ci=mAllPackets.get(history);

        if (packet!=null)
        {
            ci.mPackets.add(new PacketList(packet,listIndex,uid));
        }
        callPacketChange(history,listIndex,ci.mPackets.size()-1);
    }

    void initPacketList(int history,int index,TCPPacket packet,boolean local)
    {
        if (packet!=null)
        {
            mAllPackets.get(history).mPackets.get(index).add(packet,local);
        }
        callPacketsChange(history,index);
    }

    private void callHistoryChange()
    {
        Iterator<WeakReference<OnHistoryChangeListener>> it=mHistoryChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnHistoryChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else
                l.get().onChange();
        }
    }

    private void callPacketsChange(int time,int listIndex)
    {
        Iterator<WeakReference<OnPacketsChangeListener>> it=mPacketsChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnPacketsChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else{
                if (listIndex==-1)
                {
                    l.get().onChange(time);
                }else
                {
                    l.get().onAdd(time,listIndex);
                }
            }
        }
    }

    private void callPacketChange(int time,int listIndex,int index)
    {
        Iterator<WeakReference<OnPacketChangeListener>> it=mPacketChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnPacketChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else{
                if (listIndex==-1)
                {
                    l.get().onChange(time,listIndex);
                }else
                {
                    l.get().onAdd(time,listIndex,index);
                }
            }
        }
    }

    public static LocalPackets get()
    {
        return instance;
    }

    public static LocalPacketsMgr mgr()
    {
        return LocalPacketsMgr.instance;
    }

    public static class LocalPacketsMgr
    {
        static final LocalPacketsMgr instance=new LocalPacketsMgr();

        private PersistThread mThread;
        private long mCurrentTime;

        private LocalPacketsMgr()
        {
            mThread=new PersistThread();
            mThread.start();
        }

        static LocalPacketsMgr get()
        {
            return instance;
        }

        public void setTime(long time)
        {
            mCurrentTime=time;
        }

        public void addRequest(PersistRequest request)
        {
            mThread.mWriteQueue.add(request);
            synchronized (this) {
                this.notify();
            }
        }

        private class PersistThread extends Thread
        {
            private ByteBufferPool mBufferPool=ByteBufferPool.getDefault();
            boolean mStart=false;
            private String mFolder;
            private Queue<PersistRequest> mWriteQueue=new ConcurrentLinkedQueue<>();

            @Override
            public synchronized void start() {
                mStart=true;
                super.start();
            }

            @Override
            public void run() {
                super.run();

                mFolder= Constants.PrivateFileLocation.HISTORY + File.separator;

                PersistRequest p;
                while (mStart)
                {
                    while ((p=mWriteQueue.poll())!=null)
                    {
                        String res=p.doRequest(mFolder);
                        if (res!=null)
                            mFolder=res;
                    }

                    synchronized (instance)
                    {
                        try {
                            instance.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static class CaptureInfo
    {
        public long mTime;
        public List<PacketList> mPackets;

        CaptureInfo(long time)
        {
            mTime=time;
            mPackets=new ArrayList<>();
        }
    }

    public static class PacketList {
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

        PacketList(TCPPacket init,int index,int uid)
        {
            mIndex=index;
            packets = new ArrayList<>();
            mSPort = init.getSourcePort();
            mDPort = init.getPort();
            ip = init.getDestIp();
            mInfo = AppPortList.get().getAppByUid(uid);
            add(init,true);
            if (mInfo != null)
                Log.e("xx", "find app:" + mInfo.info.packageName);
        }

        public int size() {
            return packets.size();
        }

        synchronized void add(TCPPacket p,boolean local) {
            PacketItem item=new PacketItem(System.nanoTime(),p);
            packets.add(item);

            LocalPackets.mgr().addRequest(PersistRequest.newWriteRequest(item.mTime,this,p));
            if (local)
            {
                mLast=packets.size()-1;
            }
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

    public interface OnHistoryChangeListener
    {
        void onChange();
    }

    public interface OnPacketsChangeListener
    {
        void onChange(int time);

        void onAdd(int time,int index);
    }

    public interface OnPacketChangeListener
    {
        void onChange(int time,int index);

        void onAdd(int time,int listIndex,int index);
    }
}
