package com.iqiyi.liquanfei_sx.vpnt.floating;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.packet.ClientService;
import com.iqiyi.liquanfei_sx.vpnt.packet.TCPPacket;

/**
 * Created by Administrator on 2017/12/1.
 */

public class DefaultWindow  extends FloatingWindow{

    public static TCPPacket test=null;
    private DefaultWindowPresenter mP=null;

    @Override
    public void getWindowSize(int[] size) {
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

    private class DefaultWindowPresenter extends CommonPresenter implements View.OnClickListener
    {
        private ClientService mClient=null;

        @Override
        protected void onViewBind(View v) {
            v.setOnTouchListener(DefaultWindow.this);
            v.setOnClickListener(this);

            v.getContext().bindService(new Intent(v.getContext(), ClientService.class), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mClient=((ClientService.MB)service).get();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mClient=null;
                }
            },Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onClick(View v) {
            Log.e("xx","clicked");
            if (mClient!=null&&test!=null)
            {
                mClient.inject(test);
            }
        }
    }

    @Override
    public boolean autoMove() {
        return true;
    }
}
