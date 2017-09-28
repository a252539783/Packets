package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Application;
import android.content.Intent;

import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;

/**
 * Created by liquanfei_sx on 2017/8/14.
 */

public class MApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppPortList.init(this);
    }
}
