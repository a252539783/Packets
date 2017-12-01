package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends CommonActivity{

    private MainPresenter mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK)
        {
            mp.startServiceUnCheck();
        }
    }

    @Override
    public CommonPresenter getPresenter() {
        if (mp==null)
            mp=new MainPresenter(this);

        return mp;
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_main;
    }
}
