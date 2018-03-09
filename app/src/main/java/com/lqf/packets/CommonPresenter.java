package com.lqf.packets;

import android.view.View;

/**
 * Created by Administrator on 2017/11/6.
 */

public abstract class CommonPresenter {

    CommonFragment mFragment=null;

    public void setFragment(CommonFragment frag)
    {
        mFragment=frag;
    }

    protected void onViewBind(View v)
    {
    }

    protected void onCreate(){};

    protected void onDead(){};

    protected void onPause(){};

    protected void onResume(){};
}
