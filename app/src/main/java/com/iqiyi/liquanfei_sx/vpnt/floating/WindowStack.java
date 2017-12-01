package com.iqiyi.liquanfei_sx.vpnt.floating;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;

import java.lang.reflect.Constructor;
import java.util.LinkedList;

/**
 * Created by Administrator on 2017/11/30.
 */

public class WindowStack {

    private static WindowStack instance=null;

    private WindowManager mWm;
    private WindowManager.LayoutParams params;
    private LayoutInflater mInflater;

    private LinkedList<FloatingWindow> mWindows =new LinkedList<>();

    private int[] mSize=new int[2];

    private ViewGroup mRoot;

    private WindowStack(Context c)
    {
        mRoot=new FrameLayout(c);
        mRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mInflater=LayoutInflater.from(c);
        mWm=(WindowManager) c.getSystemService(Context.WINDOW_SERVICE);

        params=new WindowManager.LayoutParams();
        params.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.type=WindowManager.LayoutParams.TYPE_PHONE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;
        params.y = 1000;

        mWm.addView(mRoot,params);
    }

    boolean startWindow(Class cls)
    {
        FloatingWindow fw=null;
        try {
            fw= (FloatingWindow) cls.newInstance();
        } catch (Exception e) {
            Log.e("xx",e.toString());//ignore it
        }

        if (fw==null)
            return false;

        fw.setWindowStack(this);
        startWindowUnChecked(fw);

        return true;
    }

    private void startWindowUnChecked(FloatingWindow fw)
    {
        FloatingWindow previous=mWindows.peek();
        if (previous!=null)
        {
            previous.onPause();
        }

        mWindows.push(fw);
        mRoot.removeAllViews();
        fw.getWindowSize(mSize);
        setWindowSize(mSize[0],mSize[1]);
        mRoot.addView(fw.onCreateView(mInflater,mRoot));

        fw.onResume();
    }

    void backWindow()
    {
        FloatingWindow current=mWindows.pop();
        current.onPause();
        current.onDestroy();

        mRoot.removeAllViews();
        FloatingWindow fw= mWindows.peek();
        fw.getWindowSize(mSize);
        setWindowSize(mSize[0],mSize[1]);
        mRoot.addView(fw.getView());

        fw.onResume();
    }

    void setWindowPosition(int x,int y)
    {
        params.x=x;
        params.y=y;
        mWm.updateViewLayout(mRoot,params);
    }

    void setWindowSize(int w,int h)
    {
        params.width=w;
        params.height=h;
        mWm.updateViewLayout(mRoot,params);
    }

    void hide()
    {
        mRoot.setVisibility(View.GONE);
    }

    public void destroy()
    {
        mWm.removeView(mRoot);
        instance=null;
    }

    public static void init(Context c)
    {
        instance=new WindowStack(c);

        instance.startWindow(DefaultWindow.class);
    }
}
