package com.iqiyi.liquanfei_sx.vpnt.floating;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.FakeFragment;

/**
 * Created by Administrator on 2017/9/28.
 */

public abstract class FloatingWindow extends FakeFragment implements View.OnTouchListener{

    private WindowStack mStack;

    private float mLastX,mLastY;
    private long mLastTouchTime=0;

    private float mVelocityX=0,mVelocityY=0;
    private float mFriction=0.01f;
    private float gravity=10;

    private boolean mTouched=false;

    private Runnable mAutoMoveRunnable=new Runnable() {
        @Override
        public void run() {
            if ((mVelocityY!=0||mVelocityX!=0)&&!mTouched)
            {
                int res=mStack.moveWindow(mVelocityX,mVelocityY);
                if ((res&WindowStack.CRASH_X)!=0)
                {
                    mVelocityX=-mVelocityX;
                }

                if ((res&WindowStack.CRASH_Y)!=0)
                {
                    mVelocityY=-mVelocityY;
                }
                mVelocityX-=mVelocityX*mFriction;
                mVelocityY-=mVelocityY*mFriction;

                if (mVelocityX<1&&mVelocityX>-1)
                    mVelocityX=0;

                if (mVelocityY<1&&mVelocityY>-1)
                    mVelocityY=0;

                Log.e("xx","move "+mVelocityX+":"+mVelocityY);
                moveToSide();
            }
        }
    };

    void setWindowStack(WindowStack stack)
    {
        mStack=stack;
    }

    void startWindow(Class windowCls)
    {
        mStack.startWindow(windowCls);
    }

    void back()
    {
        mStack.backWindow();
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        mTouched=true;
        switch (e.getActionMasked())
        {
            case MotionEvent.ACTION_MOVE:
                mStack.moveWindow(e.getRawX()-mLastX,e.getRawY()-mLastY);
                mVelocityX=(e.getRawX()-mLastX)*10/(SystemClock.uptimeMillis()-mLastTouchTime);
                mVelocityY=(e.getRawY()-mLastY)*10/(SystemClock.uptimeMillis()-mLastTouchTime);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouched=false;
                moveToSide();
                break;
        }
        mLastX=e.getRawX();
        mLastY=e.getRawY();
        mLastTouchTime= SystemClock.uptimeMillis();

        return true;
    }

    private void moveToSide()
    {
        getView().postDelayed(mAutoMoveRunnable,10);
    }

    public abstract boolean autoMove();

    public abstract void getWindowSize(int[] size);
}
