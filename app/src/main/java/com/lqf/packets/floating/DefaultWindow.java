package com.lqf.packets.floating;

import android.view.View;

import com.lqf.packets.CommonPresenter;
import com.lqf.packets.R;
import com.lqf.packets.floating.packets.MainPacketsWindow;
import com.lqf.packets.packet.TCPPacket;

/**
 * Created by Administrator on 2017/12/1.
 */

public class DefaultWindow  extends FloatingWindow{

    public static TCPPacket test=null;
    private DefaultWindowPresenter mP=null;

    @Override
    public void getDefaultWindowSize(int[] size) {
        size[0]=100;
        size[1]=100;
    }

    @Override
    public boolean canMove() {
        return true;
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

    private class DefaultWindowPresenter extends FloatingPresenter implements View.OnClickListener
    {

        @Override
        protected void onViewBind(View v) {
            v.setOnTouchListener(DefaultWindow.this);
            v.setOnClickListener(this);
            disableBorder();
        }

        @Override
        public void onClick(View v) {
            startWindow(MainPacketsWindow.class);
        }
    }

    @Override
    public boolean autoMove() {
        return true;
    }

    @Override
    public boolean showBorder() {
        return false;
    }
}
