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
    private float mFriction=0.7f;
    private float gravity=2.3f;

    private boolean mTouched=false;
    private boolean mTouchMoved=false;

    private Runnable mAutoMoveRunnable=new Runnable() {
        @Override
        public void run() {

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
                    mVelocityX-=mVelocityX>0?5*mFriction:-5*mFriction;
                }

                if ((res&WindowStack.CRASH_Y)!=0)
                {
                    mVelocityY=-mVelocityY;
                    mVelocityY-=mVelocityY>0?5*mFriction:-5*mFriction;
                }

                mVelocityX-=mVelocityX>0?mFriction:-mFriction;
                mVelocityY-=mVelocityY>0?mFriction:-mFriction;

                if (mVelocityX<0.8f&&mVelocityX>-0.8f)
                    mVelocityX=0;

                if (mVelocityY<0.8f&&mVelocityY>-0.8f)
                    mVelocityY=0;

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
            case MotionEvent.ACTION_DOWN:
                mTouchMoved=false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (canMove()) {
                    mTouchMoved=true;
                    mStack.moveWindow(e.getRawX() - mLastX, e.getRawY() - mLastY);
                    mVelocityX = (mVelocityX + (e.getRawX() - mLastX) * 10 / (SystemClock.uptimeMillis() - mLastTouchTime)) / 2;
                    mVelocityY = (mVelocityY + (e.getRawY() - mLastY) * 10 / (SystemClock.uptimeMillis() - mLastTouchTime)) / 2;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouched=false;
                if (!mTouchMoved)
                {
                    v.performClick();
                }
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

    public abstract boolean canMove();
}
