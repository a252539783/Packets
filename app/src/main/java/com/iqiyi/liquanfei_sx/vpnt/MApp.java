package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Application;

/**
 * Created by liquanfei_sx on 2017/8/14.
 */

public class MApp extends Application {
    static {
        System.loadLibrary("native-lib");
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }
}
