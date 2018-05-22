package com.lqf.packets;

import android.view.View;

/**
 * Created by Administrator on 2017/11/6.
 */

public abstract class CommonPresenter<V> {

    private V mView = null;

    public void setView(V v)
    {
        mView = v;
    }

    protected void onViewBind(View v)
    {
    }

    protected void onCreate(){};

    protected void onDead(){};

    protected void onPause(){};

    protected void onResume(){};

    public V getView() {
        return mView;
    }
}
