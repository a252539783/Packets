package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Application;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.floating.DefaultWindow;
import com.iqiyi.liquanfei_sx.vpnt.floating.WindowStack;
import com.iqiyi.liquanfei_sx.vpnt.history.HistoryFragment2;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.saved.SavedFragment2;
import com.iqiyi.liquanfei_sx.vpnt.tools.DisplayHelper;
import com.iqiyi.liquanfei_sx.vpnt.tools.WeakLinkedList;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by liquanfei_sx on 2017/8/14.
 */

public class MApp extends Application {

    public static final int RESOURCE_PACKET_PAGER=0;

    private SparseArray<WeakLinkedList<OnDispatchResourceListener>> mListeners=new SparseArray<>(1);
    private boolean[] mResourceOccupied=new boolean[1];
    private Object[] mSharedSource=new Object[1];

    private static MApp mInstance;
    private Handler mH=new Handler();

    private ViewPager mPacketContent=null;

    private MainPagerAdapter2 mAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        Constants.init(this);
        LocalPackets.mgr();
        LeakCanary.install(this);
        DisplayHelper.init(this);

        mPacketContent= new ViewPager(this);
        mAdapter=new MainPagerAdapter2(LayoutInflater.from(this),new FakeFragment[]{
                new SavedFragment2(),
                new HistoryFragment2()
        },new String[]{
                "saved",
                "history"
        });
        mPacketContent.setAdapter(mAdapter);
        mPacketContent.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mAdapter.onAttach();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                mAdapter.onDeAttach();
            }
        });
        mSharedSource[RESOURCE_PACKET_PAGER]=mPacketContent;

        WindowStack.init(this, DefaultWindow.class);
    }

    public View packetContent()
    {
        return mPacketContent;
    }

    public static MApp get()
    {
        return mInstance;
    }

    public void postMain(Runnable r)
    {
        mH.post(r);
    }

    public void getResource(int requestCode,OnDispatchResourceListener l)
    {
        WeakLinkedList listeners=mListeners.get(requestCode);
        if (listeners==null) {
            listeners = new WeakLinkedList();
            mListeners.put(requestCode,listeners);
        }

        if (mResourceOccupied[requestCode])
        {
            listeners.add(l);
        }else
        {
            l.onDispatch(requestCode,mSharedSource[requestCode]);
        }
    }

    public void releaseResource(int requestCode,Object obj)
    {
        WeakLinkedList<OnDispatchResourceListener> ls=mListeners.get(requestCode);
        if (ls!=null&&ls.size()==0)
        {
            ls.poll().onDispatch(requestCode,obj);
            mResourceOccupied[requestCode]=true;
        }else
        {
            mResourceOccupied[requestCode]=false;
        }
    }

    public interface OnDispatchResourceListener
    {
        void onDispatch(int resourceCode,Object resource);
    }

    public interface OnRequestResourceListener
    {
        void onRequest(int resourceCode);
    }
}
