package com.iqiyi.liquanfei_sx.vpnt;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/11/30.
 */

public abstract class PagerFragment {

    private View mView=null;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container) {
        mView=inflater.inflate(getLayout(),container,false);
        getPresenter().bindView(mView);
        return mView;
    }

    public View getView()
    {
        return mView;
    }

    public void onResume() {
        getPresenter().onResume();
    }

    public void onPause() {
        getPresenter().onPause();
    }

    public void onDestroy() {
        getPresenter().onDead();
    }

    public abstract int getLayout();

    public abstract CommonPresenter getPresenter();
}
