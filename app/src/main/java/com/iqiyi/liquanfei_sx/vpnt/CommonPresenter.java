package com.iqiyi.liquanfei_sx.vpnt;

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

    public final void bindView(View v)
    {
        onViewBind(v);
    }

    protected abstract void onViewBind(View v);

    protected void onDead(){};

    protected void onPause(){};

    protected void onResume(){};
}
