package com.lqf.packets.tools;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Administrator on 2017/12/5.
 */

public abstract class LoopThread extends Thread {

    private Handler mH;

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mH=onCreateHandler();
        Looper.loop();
    }

    public Handler getHandler()
    {
        return mH;
    }

    protected abstract Handler onCreateHandler();
}
