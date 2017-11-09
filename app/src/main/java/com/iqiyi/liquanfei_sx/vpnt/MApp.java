package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Application;
import android.content.Intent;

import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;

/**
 * Created by liquanfei_sx on 2017/8/14.
 */

public class MApp extends Application {

    private static Application mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        Constants.init(this);
        AppPortList.init();
    }

    public static Application get()
    {
        return mInstance;
    }
}
