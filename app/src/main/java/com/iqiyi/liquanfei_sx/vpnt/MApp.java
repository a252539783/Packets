package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Application;
import android.os.Handler;

import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by liquanfei_sx on 2017/8/14.
 */

public class MApp extends Application {

    private static MApp mInstance;
    private Handler mH=new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        Constants.init(this);
        LocalPackets.mgr();
        LeakCanary.install(this);
        //AppPortList.init();
    }

    public static MApp get()
    {
        return mInstance;
    }

    public void postMain(Runnable r)
    {
        mH.post(r);
    }
}
