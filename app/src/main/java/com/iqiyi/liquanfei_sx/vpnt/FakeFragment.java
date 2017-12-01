package com.iqiyi.liquanfei_sx.vpnt;


import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/11/30.
 */

public abstract class FakeFragment {

    private View mView=null;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container) {
        mView=inflater.inflate(getLayout(),container,false);
        if (getPresenter()!=null)
            getPresenter().onViewBind(mView);
        return mView;
    }

    public View getView()
    {
        return mView;
    }

    public void onResume() {
        if (getPresenter()!=null)
            getPresenter().onResume();
    }

    public void onPause() {
        if (getPresenter()!=null)
            getPresenter().onPause();
    }

    public void onDestroy() {
        if (getPresenter()!=null)
            getPresenter().onDead();
    }

    public abstract int getLayout();

    public abstract CommonPresenter getPresenter();
}
