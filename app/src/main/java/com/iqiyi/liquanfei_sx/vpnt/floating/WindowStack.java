package com.iqiyi.liquanfei_sx.vpnt.floating;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/30.
 */

public class WindowStack {

    private static List<WindowStack> instances=new LinkedList<>();

    static final int CRASH_X=0x01;
    static final int CRASH_Y=0x02;

    static final int POSITION_HORIZON_SIDE=0;
    static final int POSITION_HORIZON_LEFT=1;
    static final int POSITION_HORIZON_RIGHT=2;

    private WindowManager mWm;
    private WindowManager.LayoutParams params;
    private LayoutInflater mInflater;

    private boolean mShown=false;
    private boolean mHideWhenForeground=false;

    private float mWindowX=0,mWindowY=0;
    private Point mMaxSize=new Point();
    private int mMaxX,mMaxY;

    private LinkedList<FloatingWindow> mWindows =new LinkedList<>();

    private int[] mSize=new int[2];

    private ViewGroup mRoot;

    private WindowStack(Context c)
    {
        mRoot=new FrameLayout(c);
        mRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRoot.setVisibility(View.GONE);

        mInflater=LayoutInflater.from(c);
        mWm=(WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        mWm.getDefaultDisplay().getSize(mMaxSize);
        FloatingWindow.sScreenHeight=mMaxSize.y;
        FloatingWindow.sScreenWidth=mMaxSize.x;

        params=new WindowManager.LayoutParams();
        params.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_SECURE;
        params.type=WindowManager.LayoutParams.TYPE_PHONE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;
        params.y = mMaxSize.y/3*2;
        mWindowY=params.y;

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
        fw.onCreate();
        startWindowUnChecked(fw);

        return true;
    }

    private void startWindowUnChecked(FloatingWindow fw)
    {
        FloatingWindow previous=mWindows.peek();
        if (previous!=null)
        {
            previous.onPause();
            if (previous.getView()!=null)
                mRoot.removeViewInLayout(previous.getView());
        }

        mWindows.push(fw);
        mRoot.addView(fw.onCreateView(mInflater,mRoot));
        fw.getWindowSize(mSize);
        setWindowSize(mSize[0],mSize[1]);

        fw.onResume();
    }

    void backWindow()
    {
        FloatingWindow current=mWindows.pop();
        if (current!=null)
        {
            current.onPause();
            current.onDestroy();

            mRoot.removeViewInLayout(current.getView());
        }


        FloatingWindow fw= mWindows.peek();
        if (fw!=null)
        {
            fw.getWindowSize(mSize);
            setWindowSize(mSize[0],mSize[1]);
            mRoot.addView(fw.getView());

            fw.onResume();
        }
    }

    void setWindowPosition(int x,int y)
    {
        mWindowX=x;
        mWindowY=y;
        params.x=x;
        params.y=y;
        mWm.updateViewLayout(mRoot,params);
    }

    int moveWindow(float x,float y)
    {
        int res=0;

        mWindowX+=x;
        if (mWindowX<0) {
            mWindowX = 0;
            res|=CRASH_X;
        }else if (mWindowX>mMaxX)
        {
            mWindowX=mMaxX;
            res|=CRASH_X;
        }

        mWindowY+=y;
        if (mWindowY<0)
        {
            mWindowY=0;
            res|=CRASH_Y;
        }else if (mWindowY>mMaxY)
        {
            mWindowY=mMaxY;
            res|=CRASH_Y;
        }

        params.x=(int)mWindowX;
        if (params.x<10) {
            params.x = 0;
            res|=CRASH_X;
        }else if (params.x>mMaxX-10)
        {
            params.x=mMaxX;
            res|=CRASH_X;
        }
        params.y=(int)mWindowY;
        mWm.updateViewLayout(mRoot,params);

        return res;
    }

    void notifyWidowSizeChange()
    {
        FloatingWindow fw= mWindows.peek();
        fw.getWindowSize(mSize);
        setWindowSize(mSize[0],mSize[1]);
    }

    void setWindowSize(int w,int h)
    {
        mMaxX=mMaxSize.x-w;
        mMaxY=mMaxSize.y-h;
        params.width=w;
        params.height=h;
        mWm.updateViewLayout(mRoot,params);
    }

    public boolean hideWhenForeground()
    {
        return mHideWhenForeground;
    }

    public void setHideWhenForeground(boolean hide)
    {
        mHideWhenForeground=hide;
    }

    public boolean isShown()
    {
        return mShown;
    }

    public void hide()
    {
        if (mShown)
        {
            mShown=false;
            mRoot.setVisibility(View.GONE);
            mWindows.peek().onPause();
        }
    }

    public void show()
    {
        if (!mShown)
        {
            mShown=true;
            mRoot.setVisibility(View.VISIBLE);
            mWindows.peek().onResume();
        }
    }

    int horizon()
    {
        if (params.x==0||params.x==mMaxX)
            return POSITION_HORIZON_SIDE;

        if (params.x<(mMaxSize.x-params.width)/2)
        {
            return POSITION_HORIZON_LEFT;
        }

        return POSITION_HORIZON_RIGHT;
    }

    public void destroy()
    {
        mWm.removeView(mRoot);

        for(FloatingWindow fw:mWindows)
        {
            fw.onDestroy();
        }
        instances.remove(this);
    }

    public static WindowStack init(Context c,Class cls)
    {
        WindowStack ws=new WindowStack(c);
        ws.startWindow(DefaultWindow.class);
        instances.add(ws);

        return ws;
    }
}
