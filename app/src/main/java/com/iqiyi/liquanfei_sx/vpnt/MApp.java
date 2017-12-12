package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    private Resources.Theme mTheme=null;

    private List<WindowStack> mWindowStacks=new ArrayList<>();

    private ViewPager mPacketContent=null;

    private MainPagerAdapter2 mAdapter;

    private ActivityLifecycleCallbacks mActivityCallbacks=new MActivityLifeCallbacks();

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

        mWindowStacks.add(WindowStack.init(this, DefaultWindow.class));
        mWindowStacks.get(0).setHideWhenForeground(true);

        registerActivityLifecycleCallbacks(mActivityCallbacks);
    }

    public static MApp get()
    {
        return mInstance;
    }

    public void postMain(Runnable r)
    {
        mH.post(r);
    }

    public void removeDispatchListener(int requestCode,OnDispatchResourceListener l)
    {
        ListIterator<OnDispatchResourceListener> it=mListeners.get(requestCode).listIterator();
        while(it.hasNext())
        {
            if (it.next()==l)
            {
                it.remove();
            }
        }
    }

    public void getResource(int requestCode,OnDispatchResourceListener l)
    {
        WeakLinkedList<OnDispatchResourceListener> listeners=mListeners.get(requestCode);
        if (listeners==null) {
            listeners = new WeakLinkedList<>();
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

    public void notifyFilterChanged()
    {
        mAdapter.notifyFilterChanged();
    }

    public void releaseResource(int requestCode,Object obj)
    {
        WeakLinkedList<OnDispatchResourceListener> ls=mListeners.get(requestCode);
        if (ls!=null&&ls.size()!=0)
        {
            ls.poll().onDispatch(requestCode,obj);
            mResourceOccupied[requestCode]=true;
        }else
        {
            mResourceOccupied[requestCode]=false;
        }
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme!=null)
        {
            return mTheme;
        }

        return super.getTheme();
    }

    public interface OnDispatchResourceListener
    {
        void onDispatch(int resourceCode,Object resource);
    }

    public interface OnRequestResourceListener
    {
        void onRequest(int resourceCode);
    }

    private class MActivityLifeCallbacks implements ActivityLifecycleCallbacks
    {
        int mForegroundNum=0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            mForegroundNum++;

            for (int i=0;i<mWindowStacks.size();i++)
            {
                WindowStack ws=mWindowStacks.get(i);
                if (ws.hideWhenForeground())
                {
                    ws.hide();
                }
            }

            if (mTheme==null)
            {
                mTheme=activity.getTheme();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            mForegroundNum--;

            if (mForegroundNum<0) {
                mForegroundNum = 0;
            }

            if (mForegroundNum==0)
            {
                for (int i=0;i<mWindowStacks.size();i++)
                {
                    WindowStack ws=mWindowStacks.get(i);
                    if (ws.hideWhenForeground())
                    {
                        ws.show();
                    }
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
