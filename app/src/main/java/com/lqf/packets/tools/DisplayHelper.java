package com.lqf.packets.tools;

import android.content.Context;

/**
 * Created by Administrator on 2017/12/6.
 */

public class DisplayHelper {
    private static DisplayHelper mInstance=null;

    private float mDensity=0;

    private DisplayHelper(Context c)
    {
        mDensity=c.getResources().getDisplayMetrics().density;
    }

    public int getPx(float dp)
    {
        return (int) (dp * mDensity + 0.5f);
    }

    public int getDp(float px)
    {
        return (int) (px / mDensity + 0.5f);
    }

    public static DisplayHelper get()
    {
        return mInstance;
    }

    public static void init(Context c)
    {
        mInstance=new DisplayHelper(c);
    }
}
