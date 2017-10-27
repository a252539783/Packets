package com.iqiyi.liquanfei_sx.vpnt.floating;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.iqiyi.liquanfei_sx.vpnt.MFLoatingWindowView;

/**
 * Created by Administrator on 2017/9/28.
 */

public class MFloatingWindow implements View.OnClickListener{

    WindowManager mWm;
    View v;
    WindowManager.LayoutParams params;

    public MFloatingWindow(Context c)
    {
        v=new MFLoatingWindowView(c);
        v.setOnClickListener(this);
        c=c.getApplicationContext();
        mWm=(WindowManager) c.getSystemService(Context.WINDOW_SERVICE);

        params=new WindowManager.LayoutParams();
        params.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.type=WindowManager.LayoutParams.TYPE_PHONE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;
        params.y = 1000;
        show();
    }

    public void remove()
    {
        mWm.removeView(v);
    }

    public void show()
    {
        mWm.addView(v,params);
    }

    @Override
    public void onClick(View v) {

    }
}
