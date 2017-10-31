package com.iqiyi.liquanfei_sx.vpnt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.iqiyi.liquanfei_sx.vpnt.text.SimpleFixedLayout;

/**
 * Created by Administrator on 2017/10/30.
 */

public class FixedWidthTextView extends View {

    private int mOneWidth,mOneHeight;
    private SimpleFixedLayout mLayout;
    private CharSequence mText;
    private TextPaint mPaint;

    private float mLastScrollY=0,mScrollY=0,mMaxScrollY;
    private float mScrollVelocity=0;
    private float mTapY=0;
    private float mLastMoveY=Float.MIN_VALUE;
    private float mScrollVDescentRatio=0.4f;
    private float mAutoScrollSensitive=0.1f;

    private Rect mCurrentRect=new Rect();

    private long mTimeLastUp=0,mTimeLastMove=0;
    private boolean mDoubleTap=false;

    private float mAutoScrollSpeed=0;
    private Runnable mSmoothAutoScrollRunnable=new Runnable() {
        @Override
        public void run() {

            if (mAutoScrollSpeed!=0&&canScroll(mAutoScrollSpeed))
            {
                mScrollY+=mAutoScrollSpeed;
                checkFixScroll();
            }else if (mScrollVelocity!=0)
            {
                if (canScroll(mScrollVelocity))
                {

                    mScrollY+=mScrollVelocity;
                    Log.e("xx","naturalscroll"+mScrollVelocity);

                    if (mScrollVelocity>0) {
                        mScrollVelocity -= mScrollVDescentRatio;

                        if (mScrollVelocity<0)
                        {
                            mScrollVelocity=0;
                        }
                    }
                    else if (mScrollVelocity<0)
                    {
                        mScrollVelocity+=mScrollVDescentRatio;

                        if (mScrollVelocity>0)
                        {
                            mScrollVelocity=0;
                        }
                    }

                    if (!checkFixScroll())
                    {
                        mScrollVelocity=0;
                    }
                }
            }
            else
            {
                mLastScrollY=mScrollY;
                return;
            }

            postDelayed(mSmoothAutoScrollRunnable,10);
        }
    };

    public FixedWidthTextView(Context context) {
        this(context,null,0);
    }

    public FixedWidthTextView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public FixedWidthTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        setClickable(true);
        mPaint=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTypeface(Typeface.MONOSPACE);
        mPaint.setTextSize(30);
        mPaint.setColor(Color.BLACK);
        mPaint.density=getResources().getDisplayMetrics().density;

        mOneHeight=31;
        mOneWidth=30;
    }

    public void setText(CharSequence text)
    {
        mText=text;
        mLayout=new SimpleFixedLayout(mText,mPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,0,0);
        mLayout.setOne(mOneWidth,mOneHeight);
        Paint.FontMetrics fm=mPaint.getFontMetrics();
        Log.e("xx","desc:"+fm.descent+" sc:"+fm.ascent+" top:"+fm.top+" bottom:"+fm.bottom+ " lead:"+fm.leading);
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(0,-mScrollY);
        canvas.clipRect(0,mScrollY,getRight(),mCurrentRect.bottom-mCurrentRect.top+mScrollY);
        //canvas.translate();
        mLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.e("xx",event.getAction()+":"+event.getX());
        if (event.getAction()==MotionEvent.ACTION_DOWN)
        {
            stopNaturalScroll();
            if (!mDoubleTap&&(SystemClock.uptimeMillis()-mTimeLastUp)<= ViewConfiguration.getDoubleTapTimeout())  //double tap
            {
                mDoubleTap=true;
            }else
            {
                mDoubleTap=false;
                mTapY=event.getY();
            }
        }
        else if (event.getAction()==MotionEvent.ACTION_UP)
        {
            if (mLastScrollY==mScrollY)
                mTimeLastUp= SystemClock.uptimeMillis();

            if (!mDoubleTap&&mScrollVelocity!=0)
            {
                naturalScroll();
            }

            mLastScrollY=mScrollY;
            mLastMoveY=Float.MIN_VALUE;
            stopAutoScroll();
        }else if (event.getAction()==MotionEvent.ACTION_MOVE)
        {
            if (mDoubleTap)
            {
                mLayout.selected(((int)(mScrollY+event.getY())/mOneHeight*(getMeasuredWidth()/mOneWidth)+(int)event.getX()/mOneWidth));

                if (event.getY()<0)
                {
                    autoScroll(event.getY());
                }else if (event.getY()>getMeasuredHeight())
                {
                    autoScroll((event.getY()-getMeasuredHeight()));
                }else
                {
                    stopAutoScroll();
                }
                invalidate();
            }else
            {
                float dy=(mTapY-event.getY());
                if (canScroll(dy)) {
                    mScrollY=mLastScrollY+dy;
                    checkFixScroll();
                }

                if (mLastMoveY!=Float.MIN_VALUE)
                {
                    mScrollVelocity=(mLastMoveY-event.getY())*10000000/(SystemClock.uptimeMillis()-mTimeLastMove);
                }
            }

            mLastMoveY=event.getY();
        }

        return super.onTouchEvent(event);
    }

    private boolean canScroll(float dy)
    {
        return !(dy<0&&mScrollY==0||dy>0&&mScrollY==mMaxScrollY||getMeasuredHeight()>mLayout.getLineCount()*mOneHeight);
    }

    private boolean checkFixScroll()
    {
        if (mScrollY<0)
        {
            mScrollY=0;
            invalidate();
            return false;
        }else if (mScrollY>mMaxScrollY)
        {
            mScrollY=mMaxScrollY;
            invalidate();
            return false;
        }else
        {
            invalidate();
            return true;
        }
    }

    private void autoScroll(float speed)
    {
        speed=(speed*mAutoScrollSensitive);
        if (mAutoScrollSpeed==0) {
            mAutoScrollSpeed=speed;
            post(mSmoothAutoScrollRunnable);
        }else
        {
            mAutoScrollSpeed=speed;
        }
    }

    private void naturalScroll()
    {
        Log.e("xx","execute naturalscroll");
        post(mSmoothAutoScrollRunnable);
    }

    private void stopAutoScroll()
    {
        mAutoScrollSpeed=0;
    }

    private void stopNaturalScroll()
    {
        mScrollVelocity=0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize((mLayout.getLineCount()+1)*mOneHeight,heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        getLocalVisibleRect(mCurrentRect);
        mMaxScrollY=mLayout.getLineCount()*mOneHeight-getMeasuredHeight();
    }
}
