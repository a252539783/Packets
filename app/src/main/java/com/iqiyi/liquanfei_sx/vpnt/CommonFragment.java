package com.iqiyi.liquanfei_sx.vpnt;

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
        getPresenter().bindView(v);
        return v;
    }

    public abstract int getLayout();

    public abstract CommonPresenter getPresenter();
}
