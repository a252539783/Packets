package com.iqiyi.liquanfei_sx.vpnt;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2017/11/30.
 */

public abstract class CommonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        getPresenter().bindView(getWindow().getDecorView());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPresenter().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPresenter().onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().onDead();
    }
    public abstract CommonPresenter getPresenter();

    public abstract int getLayoutId();
}
