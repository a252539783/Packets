package com.iqiyi.liquanfei_sx.vpnt.packet;

import com.iqiyi.liquanfei_sx.vpnt.Constants;
import com.iqiyi.liquanfei_sx.vpnt.MApp;
import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteBufferPool;

import java.io.File;
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

    private List<WeakReference<OnSavedChangeListener>> mSavedChangeListeners;
    private List<WeakReference<OnSavedItemChangeListener>> mSavedItemChangeListeners;

    public List<CaptureInfo> mAllPackets=new ArrayList<>();
    public List<SavedInfo> mSavedPackets=new ArrayList<>();
    private AppPortList mPortList;

    private LocalPackets()
    {
    }

    public boolean containSaved(int uid)
    {
        for (int i=0;i<mSavedPackets.size();i++)
        {
            if (mSavedPackets.get(i).mUid==uid)
                return true;
        }

        return false;
    }

    public SavedInfo getSavedInfo(int uid)
    {
        for (int i=0;i<mSavedPackets.size();i++)
        {
            if (mSavedPackets.get(i).mUid==uid)
                return mSavedPackets.get(i);
        }

        return null;
    }

    public int indexOfSaved(int uid)
    {
        for (int i=0;i<mSavedPackets.size();i++)
        {
            if (mSavedPackets.get(i).mUid==uid)
                return i;
        }

        return -1;
    }

    public void addSavedChangeListener(OnSavedChangeListener l)
    {
        if (mSavedChangeListeners==null)
            mSavedChangeListeners=new LinkedList<>();

        if (!contains(mSavedChangeListeners,l))
        mSavedChangeListeners.add(new WeakReference<>(l));
    }

    public void removeSavedListener(OnSavedChangeListener l)
    {
        if (mSavedChangeListeners==null)
            return ;

        Iterator<WeakReference<OnSavedChangeListener>> it=mSavedChangeListeners.listIterator();
        while(it.hasNext())
        {
            if (it.next().get()==l)
            {
                it.remove();
            }
        }
    }

    public void addSavedItemChangeListener(OnSavedItemChangeListener l)
    {
        if (mSavedItemChangeListeners==null)
            mSavedItemChangeListeners=new LinkedList<>();

        if (!contains(mSavedItemChangeListeners,l))
        mSavedItemChangeListeners.add(new WeakReference<>(l));
    }

    public void removeSavedItemListener(OnSavedItemChangeListener l)
    {
        if (mSavedItemChangeListeners==null)
            return ;

        Iterator<WeakReference<OnSavedItemChangeListener>> it=mSavedItemChangeListeners.listIterator();
        while(it.hasNext())
        {
            if (it.next().get()==l)
            {
                it.remove();
            }
        }
    }

    public void addHistoryChangeListener(OnHistoryChangeListener l)
    {
        if (mHistoryChangeListeners==null)
            mHistoryChangeListeners=new LinkedList<>();

        if (!contains(mHistoryChangeListeners,l))
        mHistoryChangeListeners.add(new WeakReference<>(l));
    }

    public void removeHistoryListener(OnHistoryChangeListener l)
    {
        if (mHistoryChangeListeners==null)
            return ;

        Iterator<WeakReference<OnHistoryChangeListener>> it=mHistoryChangeListeners.listIterator();
        while(it.hasNext())
        {
            if (it.next().get()==l)
            {
                it.remove();
            }
        }
    }

    public void addPacketsChangeListener(OnPacketsChangeListener l)
    {
        if (mPacketsChangeListeners==null)
            mPacketsChangeListeners=new LinkedList<>();

        if (!contains(mPacketsChangeListeners,l))
        mPacketsChangeListeners.add(new WeakReference<>(l));
    }

    public void removePacketsListener(OnPacketsChangeListener l)
    {
        if (mPacketsChangeListeners==null)
            return ;

        Iterator<WeakReference<OnPacketsChangeListener>> it=mPacketsChangeListeners.listIterator();
        while(it.hasNext())
        {
            if (it.next().get()==l)
            {
                it.remove();
            }
        }
    }

    public void addPacketChangeListener(OnPacketChangeListener l)
    {
        if (mPacketChangeListeners==null)
            mPacketChangeListeners=new LinkedList<>();

        if (!contains(mPacketChangeListeners,l))
        mPacketChangeListeners.add(new WeakReference<>(l));
    }

    public void removePacketListener(OnPacketChangeListener l)
    {
        if (mPacketChangeListeners==null)
            return ;

        Iterator<WeakReference<OnPacketChangeListener>> it=mPacketChangeListeners.listIterator();
        while(it.hasNext())
        {
            if (it.next().get()==l)
            {
                it.remove();
            }
        }
    }

    static boolean contains(List ls,Object o)
    {
        Iterator<WeakReference> it=ls.iterator();

        while(it.hasNext())
        {
            if (it.next().get()==o)
                return true;
        }

        return false;
    }

    synchronized void newHistory(long time)
    {
        if (AppPortList.get()==null)
            AppPortList.init();

        mAllPackets.add(0,new CaptureInfo(time));
        mgr().addRequest(PersistRequest.newCreateRequest(time));
        callHistoryChange();
    }

    public void newSaved(int uid)
    {
        if (AppPortList.get()==null)
            AppPortList.init();

        mSavedPackets.add(new SavedInfo(uid,0));
        mgr().addRequest(PersistRequest.newCreateSavedRequest(uid));
        callSavedChange();
    }

    void initSavedPacket(int uid,long time,TCPPacket packet)
    {
        for (int i=0;i<mSavedPackets.size();i++)
        {
            if (mSavedPackets.get(i).mUid==uid)
            {
                //mSavedPackets.get(i).mPackets.add(new SavedItem(time,new PacketList(packet,0,time,uid),""));
                initSavedPacketUnchecked(i,time,packet);
                return;
            }
        }

        SavedInfo si=new SavedInfo(uid,0);
        mSavedPackets.add(si);
        //si.mPackets.add(new SavedItem(time,new PacketList(packet,0,time,uid),""));
        initSavedPacketUnchecked(mSavedPackets.size()-1,time,packet);
    }

    void initSavedPacketUnchecked(int list,long time,TCPPacket packet)
    {
        SavedInfo si=mSavedPackets.get(list);
        si.mPackets.add(new SavedItem(time,new PacketList(packet,0,time,si.mUid),""));
        callSavedItemChange(list,si.mPackets.size()-1);
    }

    void initSavedList(String[] files,int []nums)
    {
        if (AppPortList.get()==null)
            AppPortList.init();

        if (files!=null)
        {
            for (int i=0;i<files.length;i++)
            {
                int uid=Integer.parseInt(files[i]);
                if (indexOfSaved(uid)!=-1)
                    continue;
                mSavedPackets.add(new SavedInfo(uid,nums[i]));
            }
        }
        callSavedChange();
    }

    synchronized void initHistory(String [] files)
    {
        if (AppPortList.get()==null)
            AppPortList.init();

        if (files!=null)
        {
            long []times=new long[files.length];
            for (int i=0;i<files.length;i++)
                times[i]=Long.parseLong(files[i]);
            Arrays.sort(times);

            if (mAllPackets.size()!=0&&mAllPackets.get(0).mTime==times[0]) {
                callHistoryChange();
                return;
            }

            for (int i=0;i<times.length;i++) {
                mAllPackets.add(i,new CaptureInfo(times[i]));
            }
        }

        callHistoryChange();
    }

    synchronized void initPackets(int history,long time,TCPPacket packet,int listIndex,int uid)
    {
        CaptureInfo ci=mAllPackets.get(history);

        if (packet!=null)
        {
            ci.mPackets.add(new PacketList(packet,listIndex,time,uid));
            callPacketsChange(history,ci.mPackets.size()-1);
        }
    }

    synchronized PacketList initPackets(TCPPacket packet,int listIndex)
    {
        CaptureInfo ci=mAllPackets.get(0);
        PacketList pl=null;

        if (packet!=null)
        {
            pl=new PacketList(packet,listIndex);
            ci.mPackets.add(pl);
            callPacketsChange(0,listIndex);
        }

        return pl;
    }

    synchronized void initPacketList(int history,int index,long time,TCPPacket packet,boolean local)
    {
        if (packet!=null)
        {
            mAllPackets.get(history).mPackets.get(index).add(packet,time);
        }
        callPacketChange(history,index,mAllPackets.get(history).mPackets.get(index).size()-1);
    }

    synchronized void addPacket(int index,TCPPacket packet,boolean local)
    {
        if (packet!=null)
        {
            mAllPackets.get(0).mPackets.get(index).add(packet,local);
        }
        callPacketChange(0,index,mAllPackets.get(0).mPackets.get(index).size()-1);
    }

    private void callSavedChange()
    {
        if (mSavedChangeListeners==null)
            return;

        Iterator<WeakReference<OnSavedChangeListener>> it=mSavedChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnSavedChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else
                MApp.get().postMain(new OnSavedChangeRunnable(l));
        }
    }

    private void callHistoryChange()
    {
        if (mHistoryChangeListeners==null)
            return;

        Iterator<WeakReference<OnHistoryChangeListener>> it=mHistoryChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnHistoryChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else
                MApp.get().postMain(new OnChangeRunnable(l));
                //l.get().onChange();
        }
    }

    private void callPacketsChange(int time,int listIndex)
    {
        if (mPacketsChangeListeners==null)
            return ;

        Iterator<WeakReference<OnPacketsChangeListener>> it=mPacketsChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnPacketsChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else{
                if (listIndex==-1)
                {
                    MApp.get().postMain(new OnChangeRunnable(l,time));
                    //l.get().onChange(time);
                }else
                {
                    MApp.get().postMain(new OnAddRunnable(l,time,listIndex));
                    //l.get().onAdd(time,listIndex);
                }
            }
        }
    }

    private void callPacketChange(int time,int listIndex,int index)
    {
        if (mPacketChangeListeners==null)
            return ;

        Iterator<WeakReference<OnPacketChangeListener>> it=mPacketChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnPacketChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else{
                if (listIndex==-1)
                {
                    MApp.get().postMain(new OnChangeRunnable(l,time,listIndex));
                    //l.get().onChange(time,listIndex);
                }else
                {
                    MApp.get().postMain(new OnAddRunnable(l,time,listIndex,index));
                    //l.get().onAdd(time,listIndex,index);
                }
            }
        }
    }

    private void callSavedItemChange(int listIndex,int index)
    {
        if (mSavedItemChangeListeners==null)
            return ;

        Iterator<WeakReference<OnSavedItemChangeListener>> it=mSavedItemChangeListeners.listIterator();


        while (it.hasNext())
        {
            WeakReference<OnSavedItemChangeListener> l=it.next();
            if (l.get()==null)
                it.remove();
            else{
                MApp.get().postMain(new OnSavedAddRunnable(l,listIndex,index));
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

    public static class SavedInfo
    {
        public int mUid;
        public AppPortList.AppInfo mInfo;
        public List<SavedItem > mPackets;
        public int mNum=0;

        SavedInfo(int uid,int num)
        {
            mUid=uid;
            mNum=num;
            mInfo=AppPortList.get().getAppByUid(uid);
            mPackets=new ArrayList<>();
        }
    }

    public static class SavedItem
    {
        public String mDesc="";
        public long mTime=0;
        public PacketList mPackets;

        public SavedItem(long time,PacketList list,String name)
        {
            mDesc=name;
            mTime=time;
            mPackets=list;
        }
    }

    public interface OnSavedChangeListener
    {
        void onChange();
    }

    public interface OnHistoryChangeListener
    {
        void onChange();
    }

    public interface OnSavedItemChangeListener
    {
        void onChange(int time);

        void onAdd(int time,int index);
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

    private class OnAddRunnable implements Runnable
    {
        private int mTime=-1,mListIndex=-1,mIndex=-1;
        private WeakReference mListener;

        OnAddRunnable(WeakReference l,int time,int listIndex)
        {
            mListener=l;
            mTime=time;
            mListIndex=listIndex;
        }

        OnAddRunnable(WeakReference l,int time,int listIndex,int index)
        {
            this(l,time, listIndex);
            mIndex=index;
        }

        @Override
        public void run() {
            if (mIndex==-1)
            {
                OnPacketsChangeListener l= (OnPacketsChangeListener) mListener.get();
                if (l!=null)
                    l.onAdd(mTime,mListIndex);
            }else
            {
                OnPacketChangeListener l= (OnPacketChangeListener) mListener.get();
                if (l!=null)
                    l.onAdd(mTime,mListIndex,mIndex);
            }
        }
    }

    private class OnSavedAddRunnable implements Runnable
    {
        private int mListIndex=-1,mIndex=-1;
        private WeakReference mListener;

        OnSavedAddRunnable(WeakReference l,int listIndex,int index)
        {
            mListener=l;
            mIndex=index;
            mListIndex=listIndex;
        }

        @Override
        public void run() {
                OnSavedItemChangeListener l= (OnSavedItemChangeListener) mListener.get();
                if (l!=null)
                    l.onAdd(mListIndex,mIndex);
        }
    }

    private class OnChangeRunnable implements Runnable
    {
        private int mTime=-1,mListIndex=-1;
        private WeakReference mListener;

        OnChangeRunnable(WeakReference l)
        {
            mListener=l;
        }

        OnChangeRunnable(WeakReference l,int time)
        {
            this(l);
            mTime=time;
        }

        OnChangeRunnable(WeakReference l,int time,int listIndex)
        {
            this(l,time);
            mListIndex=listIndex;
        }

        @Override
        public void run() {
            if (mListIndex!=-1)
            {
                OnPacketsChangeListener l= (OnPacketsChangeListener) mListener.get();
                if (l!=null)
                    l.onChange(mTime);

                return ;
            }

            if (mTime!=-1)
            {
                OnPacketChangeListener l= (OnPacketChangeListener) mListener.get();
                if (l!=null)
                    l.onChange(mTime,mListIndex);

                return;
            }

            OnHistoryChangeListener l= (OnHistoryChangeListener) mListener.get();
            if (l!=null)
                l.onChange();
        }
    }

    private class OnSavedChangeRunnable implements Runnable
    {
        private WeakReference mL;

        OnSavedChangeRunnable(WeakReference l)
        {
            mL=l;
        }

        @Override
        public void run() {
            OnSavedChangeListener l= (OnSavedChangeListener) mL.get();
            if (l!=null)
                l.onChange();
        }
    }
}
