package com.iqiyi.liquanfei_sx.vpnt.floating;

import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;

/**
 * Created by Administrator on 2017/12/1.
 */

public class DefaultWindow  extends FloatingWindow{
    private DefaultWindowPresenter mP=null;

    @Override
    public void getWindowSize(int[] size) {
        size[0]=100;
        size[1]=100;
    }

    @Override
    public int getLayout() {
        return R.layout.float_main_button;
    }

    @Override
    public CommonPresenter getPresenter() {
        if (mP==null)
            mP=new DefaultWindowPresenter();

        return mP;
    }

    private class DefaultWindowPresenter extends CommonPresenter
    {

        @Override
        protected void onViewBind(View v) {

        }
    }
}
