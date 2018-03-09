package com.lqf.packets;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/11/6.
 */

public abstract class CommonFragment extends Fragment{

    public static final String ARG_LAYOUT="1";
    public static final String ARG_PRESENT="2";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(getLayout(),container,false);
        if (getPresenter()!=null)
        getPresenter().onViewBind(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getPresenter()!=null)
        getPresenter().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getPresenter()!=null)
        getPresenter().onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getPresenter()!=null)
        getPresenter().onDead();
    }

    public abstract int getLayout();

    public abstract CommonPresenter getPresenter();
}
