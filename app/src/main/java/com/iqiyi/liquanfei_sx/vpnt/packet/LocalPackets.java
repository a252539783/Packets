package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.iqiyi.liquanfei_sx.vpnt.Constants;
import com.iqiyi.liquanfei_sx.vpnt.MApp;
import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteBufferPool;
import com.iqiyi.liquanfei_sx.vpnt.tools.Filter;
import com.iqiyi.liquanfei_sx.vpnt.tools.LoopThread;
import com.iqiyi.liquanfei_sx.vpnt.tools.WeakLinkedList;

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

    private WeakLinkedList<OnHistoryChangeListener> mHistoryChangeListeners;
    private WeakLinkedList<OnPacketsChangeListener> mPacketsChangeListeners;
    private WeakLinkedList<OnPacketChangeListener> mPacketChangeListeners;

    private WeakLinkedList<OnSavedChangeListener> mSavedChangeListeners;
    private WeakLinkedList<OnSavedItemChangeListener> mSavedItemChangeListeners;

    public List<CaptureInfo> mAllPackets=new ArrayList<>();
    public List<SavedInfo> mSavedPackets=new ArrayList<>();
    private AppPortList mPortList;

    private LoadFilterThread mThread=new LoadFilterThread();

    private String mFilterIp=null,mFilterName=null,mFilterPkg=null;
    private int mFilterSPort=-1,mFilterDPort=-1,mKey=0;

    private LocalPackets()
    {
        mThread.start();
    }

    public void addFilterKey(int key,String word)
    {
        mKey|=key;
        switch (key)
        {
            case Filter.BY_NAME:
                mFilterName=word;
                break;
            case Filter.BY_IP_DEST:
                mFilterIp=word;
                break;
            case Filter.BY_PACKAGE:
                mFilterPkg=word;
                break;
        }
        for (int i=0;i<mAllPackets.size();i++)
        {
            mAllPackets.get(i).mPackets.setKey(mKey,null,false);
        }
    }

    public void addFilterKey(int key,int word)
    {
        mKey|=key;
        switch (key)
        {
            case Filter.BY_PORT_DEST:
                mFilterDPort=word;
                break;
            case Filter.BY_PORT_SOURCE:
                mFilterSPort=word;
                break;
        }

        for (int i=0;i<mAllPackets.size();i++)
        {
            mAllPackets.get(i).mPackets.setKey(mKey,null,false);
        }
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
            mSavedChangeListeners=new WeakLinkedList<>();

        if (!mSavedChangeListeners.contains(l))
            mSavedChangeListeners.add(l);
    }

    public void removeSavedListener(OnSavedChangeListener l)
    {
        if (mSavedChangeListeners==null)
            return ;

        mSavedChangeListeners.remove(l);
    }

    public void addSavedItemChangeListener(OnSavedItemChangeListener l)
    {
        if (mSavedItemChangeListeners==null)
            mSavedItemChangeListeners=new WeakLinkedList<>();

        if (!mSavedItemChangeListeners.contains(l))
        mSavedItemChangeListeners.add(l);
    }

    public void removeSavedItemListener(OnSavedItemChangeListener l)
    {
        if (mSavedItemChangeListeners==null)
            return ;

        mSavedItemChangeListeners.remove(l);
    }

    public void addHistoryChangeListener(OnHistoryChangeListener l)
    {
        if (mHistoryChangeListeners==null)
            mHistoryChangeListeners=new WeakLinkedList<>();

        if (!mHistoryChangeListeners.contains(l))
        mHistoryChangeListeners.add(l);
    }

    public void removeHistoryListener(OnHistoryChangeListener l)
    {
        if (mHistoryChangeListeners==null)
            return ;

        mHistoryChangeListeners.remove(l);
    }

    public void addPacketsChangeListener(OnPacketsChangeListener l)
    {
        if (mPacketsChangeListeners==null)
            mPacketsChangeListeners=new WeakLinkedList<>();

        if (!mPacketsChangeListeners.contains(l))
        mPacketsChangeListeners.add(l);
    }

    public void removePacketsListener(OnPacketsChangeListener l)
    {
        if (mPacketsChangeListeners==null)
            return ;

        mPacketsChangeListeners.remove(l);
    }

    public void addPacketChangeListener(OnPacketChangeListener l)
    {
        if (mPacketChangeListeners==null)
            mPacketChangeListeners=new WeakLinkedList<>();

        if (!mPacketChangeListeners.contains(l))
        mPacketChangeListeners.add(l);
    }

    public void removePacketListener(OnPacketChangeListener l)
    {
        if (mPacketChangeListeners==null)
            return ;

        mPacketChangeListeners.remove(l);
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

    //TODO 使用多个锁来获得更高效率
    synchronized void newHistory(long time)
    {
        if (AppPortList.get()==null)
            AppPortList.init();

        mAllPackets.add(0,new CaptureInfo(time));
        mgr().addRequest(PersistRequest.newCreateRequest(time));
        callHistoryChange(0);
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
                //callHistoryChange(0);
                return;
            }

            for (int i=0;i<times.length;i++) {
                mAllPackets.add(i,new CaptureInfo(times[i]));
                callHistoryChange(i);
            }
        }
    }

    void initPackets(int history,long time,TCPPacket packet,int listIndex,int uid)
    {
        CaptureInfo ci=mAllPackets.get(history);

        if (packet!=null)
        {
            if (ci.mPackets.add(new PacketList(packet,listIndex,time,uid)))
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
            if (ci.mPackets.add(pl))
            callPacketsChange(0,listIndex);
        }

        return pl;
    }

    void initPacketList(int history,int index,long time,TCPPacket packet,boolean local)
    {
        if (packet!=null)
        {
            if (mAllPackets.get(history).mPackets.get(index).add(packet,time))
            {
                callPacketChange(history,index,mAllPackets.get(history).mPackets.get(index).size()-1);
            }
        }
    }

    synchronized void addPacket(int index,TCPPacket packet,boolean local)
    {
        if (packet!=null)
        {
            if (mAllPackets.get(0).mPackets.get(0,index).add(packet,local))
            {
                callPacketChange(0,index,mAllPackets.get(0).mPackets.get(0,index).size()-1);
            }
        }
    }

    private void callSavedChange()
    {
        if (mSavedChangeListeners==null)
            return;

        Iterator<OnSavedChangeListener> it=mSavedChangeListeners.listIterator();


        while (it.hasNext())
        {
            OnSavedChangeListener l=it.next();
            if (l==null)
                it.remove();
            else
                MApp.get().postMain(new OnSavedChangeRunnable(new WeakReference(l)));
        }
    }

    private void callHistoryChange(int timeIndex)
    {
        if (mHistoryChangeListeners==null)
            return;

        Iterator<OnHistoryChangeListener> it=mHistoryChangeListeners.listIterator();


        while (it.hasNext())
        {
            OnHistoryChangeListener l=it.next();
            if (l==null)
                it.remove();
            else
            {
                if (timeIndex!=-1)
                {
                    MApp.get().postMain(new OnAddRunnable(new WeakReference(l),timeIndex));
                }else
                {
                    MApp.get().postMain(new OnChangeRunnable(new WeakReference(l)));
                }
            }
                //l.get().onChange();
        }
    }

    private void callPacketsChange(int time,int listIndex)
    {
        if (mPacketsChangeListeners==null)
            return ;

        Iterator<OnPacketsChangeListener> it=mPacketsChangeListeners.listIterator();


        while (it.hasNext())
        {
            OnPacketsChangeListener l=it.next();
            if (l==null)
                it.remove();
            else{
                if (listIndex==-1)
                {
                    MApp.get().postMain(new OnChangeRunnable(new WeakReference(l),time));
                    //l.get().onChange(time);
                }else
                {
                    MApp.get().postMain(new OnAddRunnable(new WeakReference(l),time,listIndex));
                    //l.get().onAdd(time,listIndex);
                }
            }
        }
    }

    private void callPacketChange(int time,int listIndex,int index)
    {
        if (mPacketChangeListeners==null)
            return ;

        Iterator<OnPacketChangeListener> it=mPacketChangeListeners.listIterator();


        while (it.hasNext())
        {
            OnPacketChangeListener l=it.next();
            if (l==null)
                it.remove();
            else{
                if (listIndex==-1)
                {
                    MApp.get().postMain(new OnChangeRunnable(new WeakReference(l),time,listIndex));
                    //l.get().onChange(time,listIndex);
                }else
                {
                    MApp.get().postMain(new OnAddRunnable(new WeakReference(l),time,listIndex,index));
                    //l.get().onAdd(time,listIndex,index);
                }
            }
        }
    }

    private void callSavedItemChange(int listIndex,int index)
    {
        if (mSavedItemChangeListeners==null)
            return ;

        Iterator<OnSavedItemChangeListener> it=mSavedItemChangeListeners.listIterator();


        while (it.hasNext())
        {
            OnSavedItemChangeListener l=it.next();
            if (l==null)
                it.remove();
            else{
                MApp.get().postMain(new OnSavedAddRunnable(new WeakReference(l),listIndex,index));
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

    public void filterLoadHistory(int time)
    {
        filterLoadHistory(time,-1);
    }

    public void filterLoadHistory(int time,int list)
    {
        Message msg=Message.obtain();
        msg.what=LoadFilterThread.LOAD_HISTORY;
        msg.arg1=time;
        msg.arg2=list;
        mThread.getHandler().sendMessage(msg);
    }

    public class PacketsFilter extends Filter<PacketList>
    {

        public PacketsFilter(int size, List src) {
            super(size, src);
        }

        @Override
        public boolean filter(int key, PacketList o) {
            if (key==0)
                return true;

            boolean res=false;

            if ((key&BY_NAME)!=0)
            {
                if (mFilterName==null||mFilterName.equals(""))
                {
                    res=true;
                }else
                {
                    res=o.info().appName.contains(mFilterName);
                }
            }

            return res;
        }
    }

    private class LoadFilterThread extends LoopThread
    {
        private static final int LOAD_HISTORY=0;
        private static final int LOAD_SAVED=1;

        @Override
        protected Handler onCreateHandler() {
            return new LoadHandler();
        }

        /**
         * msg.what:keyType
         */
        private class LoadHandler extends Handler implements Filter.LoadListener
        {
            int mCurrentTime=0;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what==LOAD_HISTORY)
                {
                    mCurrentTime=msg.arg1;
                    mAllPackets.get(msg.arg1).mPackets.load(new WeakReference<Filter.LoadListener>(this));
                }else if (msg.what==LOAD_SAVED)
                {

                }
            }

            @Override
            public void onLoadOne(int index) {
                callPacketsChange(mCurrentTime,index);
            }

            @Override
            public void onLoadComplete() {

            }
        }
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

        //TODO 使用阻塞队列
        public void addRequest(PersistRequest request)
        {
            if (request.hasRead())
            {
                if (request instanceof PersistRequest.LoadRequest)
                LocalPackets.get().filterLoadHistory(((PersistRequest.LoadRequest)request).mTimeIndex);
            }else
            {
                mThread.mWriteQueue.add(request);
                synchronized (this) {
                    this.notify();
                }
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

            //TODO 使用handler以获得更高效率
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

    public class CaptureInfo
    {
        public long mTime;
        public PacketsFilter mPackets;

        CaptureInfo(long time)
        {
            mTime=time;
            mPackets=new PacketsFilter(50,new ArrayList());
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
        void onAdd(int timeIndex);
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

        OnAddRunnable(WeakReference l,int time)
        {
            mTime=time;
            mListener=l;
        }

        @Override
        public void run() {
            if (mListIndex==-1)
            {
                OnHistoryChangeListener l= (OnHistoryChangeListener) mListener.get();
                if (l!=null)
                    l.onAdd(mTime);
            }else
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
