package com.iqiyi.liquanfei_sx.vpnt.floating;

import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.iqiyi.liquanfei_sx.vpnt.FakeFragment;

/**
 * Created by Administrator on 2017/9/28.
 */

public abstract class FloatingWindow extends FakeFragment implements View.OnTouchListener{
    static int sScreenWidth=0,sScreenHeight=0;
    static int sMinWidth=100,sMinHeight=100;

    public static int sWindowBorderWidth=30;

    private WindowStack mStack;

    float mWindowWidth=-1,mWindowHeight=-1;
    float mWindowX=0,mWindowY=0;

    private float mLastX,mLastY;
    private long mLastTouchTime=0;

    private float mVelocityX=0,mVelocityY=0;
    private float mFriction=0.7f;
    private float gravity=2.3f;

    private boolean mTouched=false;

    private Runnable mAutoMoveRunnable=new Runnable() {
        @Override
        public void run() {

            Log.e("xx","auto moved...");

            if (autoMove())
            {
                switch (mStack.horizon())
                {
                    case WindowStack.POSITION_HORIZON_LEFT:
                        mVelocityX-=gravity;
                        break;
                    case WindowStack.POSITION_HORIZON_RIGHT:
                        mVelocityX+=gravity;
                        break;
                    case WindowStack.POSITION_HORIZON_SIDE:

                }
            }

            if ((mVelocityY!=0||mVelocityX!=0)&&!mTouched)
            {
                int res=mStack.moveWindow(mVelocityX,mVelocityY);
                if ((res&WindowStack.CRASH_X)!=0)
                {
                    mVelocityX=-mVelocityX;
                    if (mVelocityX>0)
                    {
                        if (mVelocityX<5*mFriction)
                            mVelocityX=0;
                        else{
                            mVelocityX-=5*mFriction;
                        }
                    }else if (mVelocityX<0)
                    {
                        if (-mVelocityX<5*mFriction)
                        {
                            mVelocityX=0;
                        }else
                        {
                            mVelocityX+=5*mFriction;
                        }
                    }
                }

                if ((res&WindowStack.CRASH_Y)!=0)
                {
                    mVelocityY=-mVelocityY;
                    if (mVelocityY>0)
                    {
                        if (mVelocityY<5*mFriction)
                            mVelocityY=0;
                        else{
                            mVelocityY-=5*mFriction;
                        }
                    }else if (mVelocityY<0)
                    {
                        if (-mVelocityY<5*mFriction)
                        {
                            mVelocityY=0;
                        }else
                        {
                            mVelocityY+=5*mFriction;
                        }
                    }
                }

                if (Math.abs(mVelocityX)<mFriction)
                {
                    mVelocityX=0;
                }else {
                    mVelocityX-=mVelocityX>0?mFriction:-mFriction;
                }

                if (Math.abs(mVelocityY)<mFriction)
                {
                    mVelocityY=0;
                }else {
                    mVelocityY-=mVelocityY>0?mFriction:-mFriction;
                }

                if (mVelocityX<0.8f&&mVelocityX>-0.8f)
                    mVelocityX=0;

                if (mVelocityY<0.8f&&mVelocityY>-0.8f)
                    mVelocityY=0;

                moveToSide();
            }


        }
    };

    public static int screenWidth()
    {
        return sScreenWidth;
    }

    public static int screenHeight()
    {
        return sScreenHeight;
    }

    void setWindowStack(WindowStack stack)
    {
        mStack=stack;
    }

    public void startWindow(Class windowCls)
    {
        mStack.startWindow(windowCls);
    }

    public void enableBorder(int width)
    {
        mStack.enableBorder(true,width);
    }

    public void disableBorder()
    {
        mStack.enableBorder(false,0);
    }

    public void back()
    {
        mStack.backWindow();
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        mTouched=true;
        v.onTouchEvent(e);      //处理onClick等
        switch (e.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                mLastX=e.getRawX();
                mLastY=e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (canMove()&&(mLastX>0||mLastY>0)) {
                    mStack.moveWindow(e.getRawX() - mLastX, e.getRawY() - mLastY);
                    mVelocityX = (mVelocityX + (e.getRawX() - mLastX) * 10 / (SystemClock.uptimeMillis() - mLastTouchTime)) / 2;
                    mVelocityY = (mVelocityY + (e.getRawY() - mLastY) * 10 / (SystemClock.uptimeMillis() - mLastTouchTime)) / 2;
                }
                mLastX=e.getRawX();
                mLastY=e.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                moveToSide();
                mLastX=mLastY=-1;
                mTouched=false;
                break;
        }
        mLastTouchTime= SystemClock.uptimeMillis();

        return true;
    }

    private void moveToSide()
    {
        getView().postDelayed(mAutoMoveRunnable,10);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (getPresenter()!=null&&getPresenter() instanceof FloatingPresenter)
        {
            ((FloatingPresenter) getPresenter()).setWindow(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View v=super.onCreateView(inflater, container);

        FrameLayout.LayoutParams lp;
        if ((lp=(FrameLayout.LayoutParams) v.getLayoutParams())!=null && !(this instanceof BgWindow))
        {
            lp.setMargins(sWindowBorderWidth,sWindowBorderWidth,sWindowBorderWidth,sWindowBorderWidth);
        }
        return v;
    }

    public void getWindowSize(int []size)
    {
        if (mWindowWidth==-1)
        {
            getDefaultWindowSize(size);
            mWindowWidth=size[0];
            mWindowHeight=size[1];
        }else
        {
            size[0]=(int)mWindowWidth;
            size[1]=(int)mWindowHeight;
        }
    }

    void setWindowSize(float w,float h)
    {
        mWindowWidth=w;
        mWindowHeight=h;
    }

    void setWindowPosition(float x,float y)
    {
        mWindowX=x;
        mWindowY=y;
    }

    public abstract boolean autoMove();

    public abstract void getDefaultWindowSize(int[] size);

    public abstract boolean canMove();
}
