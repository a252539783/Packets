package com.lqf.packets.floating;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

import com.lqf.packets.CommonPresenter;
import com.lqf.packets.R;
import com.lqf.packets.floating.packets.MainPacketsWindow;
import com.lqf.packets.packet.ClientService;
import com.lqf.packets.packet.TCPPacket;

/**
 * Created by LQF on 2018/3/12.
 */

public class TestWindow extends FloatingWindow {


    public static TCPPacket test = null;
    FloatingPresenter mP = null;
    ClientService cs = null;

    @Override
    public int getLayout() {
        return R.layout.float_main_button;
    }

    @Override
    public CommonPresenter getPresenter() {
        if (mP == null)
            mP = new FloatingPresenter() {
                @Override
                protected void onViewBind(View v) {
                    v.setOnTouchListener(TestWindow.this);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (cs == null) {
                                v.getContext().bindService(new Intent(v.getContext(), ClientService.class), new ServiceConnection() {
                                    @Override
                                    public void onServiceConnected(ComponentName name, IBinder service) {
                                        cs = ((ClientService.MB) service).get();
                                    }

                                    @Override
                                    public void onServiceDisconnected(ComponentName name) {
                                        cs = null;
                                    }
                                }, 0);
                            } else {
                                if (test != null)
                                    cs.inject(test);
                            }
                        }
                    });
                    disableBorder();
                }
            };
        return mP;
    }

    @Override
    public boolean autoMove() {
        return true;
    }

    @Override
    public void getDefaultWindowSize(int[] size) {
        size[0] = 100;
        size[1] = 100;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public boolean showBorder() {
        return false;
    }
}
