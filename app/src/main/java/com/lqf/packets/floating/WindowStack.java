package com.lqf.packets.floating;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.lqf.packets.R;
import com.lqf.packets.tools.DrawableHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/30.
 */

public class WindowStack{

    private static WindowStack sBgWindow=null;

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

    private DrawableHelper.BorderDrawable mBorderDrawable= DrawableHelper.INSTANCE.newBorderDrawable(Color.GRAY);

    private LinkedList<FloatingWindow> mWindows =new LinkedList<>();

    private int[] mSize=new int[2];

    private WindowContainer mRoot;

    private WindowStack(Context c)
    {
    }

    private void init(Context c,boolean touchable)
    {

        mRoot=new WindowContainer(c,this);
        mRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRoot.setVisibility(View.GONE);
        mRoot.setBorderColor(c.getResources().getColor(R.color.border_float_container));
        mRoot.setBackgroundResource(R.drawable.bg_window_container);
        mRoot.invalidate();

        mInflater=LayoutInflater.from(c);
        mWm=(WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        mWm.getDefaultDisplay().getSize(mMaxSize);
        int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值

            mMaxSize.y-= c.getResources().getDimensionPixelSize(resourceId);
        }
        FloatingWindow.sScreenHeight=mMaxSize.y;
        FloatingWindow.sScreenWidth=mMaxSize.x;

        params=new WindowManager.LayoutParams();
        params.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if (!touchable)
        {
            params.flags|= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        params.type=WindowManager.LayoutParams.TYPE_PHONE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;
        params.y = mMaxSize.y/3*2;
        mWindowY=params.y;
        mBorderDrawable.setX(params.x);
        mBorderDrawable.setY(params.y);

        mWm.addView(mRoot,params);

        if (sBgWindow!=null && sBgWindow!=this)
        {
            ((BgWindow)sBgWindow.getCurrentWindow()).postDraw(mBorderDrawable,-1);
        }
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

    void enableBorder(boolean enable,int width)
    {
        mRoot.setBorderWidth(width);
        mRoot.enableBorder(enable);
        mRoot.invalidate();
        mBorderDrawable.setEnable(enable);
        mBorderDrawable.setBorder(width);
        freshBg();
    }

    private void freshBg()
    {
        if (sBgWindow!=null&&sBgWindow!=this)
        {
            sBgWindow.getCurrentWindow().getView().invalidate();
        }
    }


    public FloatingWindow getCurrentWindow()
    {
        return mWindows.peekFirst();
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
        mBorderDrawable.setX(x);
        mBorderDrawable.setY(y);
        freshBg();
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
        mBorderDrawable.setX(params.x);
        mBorderDrawable.setY(params.y);
        getCurrentWindow().setWindowPosition(params.x,params.y);
        freshBg();
        mWm.updateViewLayout(mRoot,params);

        return res;
    }

    void resizeWindow(float dw,float dh,float movex,float movey)
    {
        FloatingWindow fw= mWindows.peek();
        fw.setWindowSize(fw.mWindowWidth+dw,fw.mWindowHeight+dh);
        fw.setWindowPosition(fw.mWindowX+movex,fw.mWindowY+movey);
        if (fw.mWindowHeight<FloatingWindow.sMinHeight)
        {
            mBorderDrawable.setH(FloatingWindow.sMinHeight);

            if (movey!=0) {
                mBorderDrawable.setY(mWindowY +params.height- FloatingWindow.sMinHeight);
            }
        }else
        {
            mBorderDrawable.setH(fw.mWindowHeight);
            mBorderDrawable.setY(fw.mWindowY);
        }

        if (fw.mWindowWidth<FloatingWindow.sMinWidth)
        {
            mBorderDrawable.setW(FloatingWindow.sMinWidth);

            if (movex!=0)
            mBorderDrawable.setX(mWindowX+params.width-FloatingWindow.sMinWidth);
        }else
        {
            mBorderDrawable.setW(fw.mWindowWidth);
            mBorderDrawable.setX(fw.mWindowX);
        }
        freshBg();
    }

    void endResize(int resize)
    {
        FloatingWindow fw= mWindows.peek();
        if (fw.mWindowHeight<FloatingWindow.sMinHeight)
        {
            fw.mWindowHeight=FloatingWindow.sMinHeight;
        }

        if (fw.mWindowWidth<FloatingWindow.sMinWidth)
        {
            fw.mWindowWidth=FloatingWindow.sMinWidth;
        }

        int movex=(int)fw.mWindowWidth-params.width;
        int movey=(int)fw.mWindowHeight-params.height;
        if ((resize&WindowContainer.Companion.getLEFT())==0)
        {
            movex=0;
        }

        if ((resize&WindowContainer.Companion.getTOP())==0)
        {
            movey=0;
        }

        fw.getWindowSize(mSize);
        setWindowSize(mSize[0],mSize[1]);
        moveWindow(-movex,-movey);
    }

    private void setWindowSize(int w,int h)
    {
        mMaxX=mMaxSize.x-w;
        mMaxY=mMaxSize.y-h;
        params.width=w;
        params.height=h;
        mBorderDrawable.setH(params.height);
        mBorderDrawable.setW(params.width);
        freshBg();
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

    public void post(Runnable r)
    {
        mRoot.post(r);
    }

    /*
    背景Window用来统一绘制一些全局内容，且不可点击
    比如窗口更改大小时的边框
     */
    public static void initBg(Context c)
    {
        if (sBgWindow==null)
        {
            sBgWindow=new WindowStack(c);
            sBgWindow.init(c,false);
            sBgWindow.startWindow(BgWindow.class);
            sBgWindow.show();
        }
    }

    public static WindowStack newWindow(final Context c, final Class cls)
    {
        if (sBgWindow==null)
        {
            initBg(c);
        }

        final WindowStack ws=new WindowStack(c);

//        new Thread()
//        {
//            @Override
//            public void run() {
//                super.run();
//
//                Looper.prepare();
//                Looper.loop();
//            }
//        }.start();

        ws.init(c,true);
        ws.startWindow(cls);
        instances.add(ws);
        return ws;
    }
}
