package com.lqf.packets.view;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.lqf.packets.text.FileInfo;
import com.lqf.packets.text.SimpleFixedLayout;

/**
 * Created by Administrator on 2017/10/30.
 */

public class FixedWidthTextView extends View {

    private int mOneWidth,mOneHeight;

    private SimpleFixedLayout mLayout;
    private CharSequence mText;
    private TextPaint mPaint;

    private float mLastScrollY=0,mScrollY=0,mMaxScrollY;
    private float mScrollStartY=0;
    private float mScrollVelocity=0;
    private float mTapY=0;
    private float mLastMoveY=Float.MIN_VALUE;
    private float mScrollVDescentRatio=0.4f;
    private float mAutoScrollSensitive=0.1f;

    private Rect mCurrentRect=new Rect();

    private long mTimeLastUp=0,mTimeLastMove=0;

    private boolean mDoubleTap=false;
    private boolean mEditMode=false;

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
        mPaint.setTextSize(40);
        mPaint.setColor(Color.BLACK);
        mPaint.density=getResources().getDisplayMetrics().density;

        mOneWidth=70;
    }

    public void setText(CharSequence text)
    {
        mText=text;
        mLayout=new SimpleFixedLayout(mText,mPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,0,0);
        mLayout.setOne(mOneWidth,-1);
        mOneHeight=mLayout.getOneHeight();
        requestLayout();
    }

    public void setBytes(byte[] src)
    {
        mLayout=new SimpleFixedLayout(src,mPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,0,0);
        mLayout.setOne(mOneWidth,-1);
        mOneHeight=mLayout.getOneHeight();
        requestLayout();
    }

    public void setFile(FileInfo file)
    {
        mLayout=new SimpleFixedLayout(file,mPaint,getWidth(), Layout.Alignment.ALIGN_NORMAL,0,0);
        mLayout.setOne(mOneWidth,-1);
        mOneHeight=mLayout.getOneHeight();
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

        int selectIndex=((int)(mScrollY+event.getY())/mOneHeight*(getMeasuredWidth()/mOneWidth)+(int)event.getX()/mOneWidth);

        if (event.getAction()==MotionEvent.ACTION_DOWN)
        {
            /**
             * 按下之后立即停止滑动
             */
            stopNaturalScroll();

            if ((!mDoubleTap&&(SystemClock.uptimeMillis()-mTimeLastUp)<= ViewConfiguration.getDoubleTapTimeout()))     //double tap
            {
                mDoubleTap=true;
                mEditMode=true;
            }else           //tap
            {
                if (mLayout.isSelect(selectIndex))      //点击了光标或者选中位置，继续进行编辑选中
                {
                    mEditMode=true;
                }else
                {
                    mLayout.resetSelect();
                }

                mDoubleTap=false;
                mTapY=event.getY();
                mScrollStartY=mTapY;
            }
        }
        else if (event.getAction()==MotionEvent.ACTION_UP)
        {
            if (mLastScrollY==mScrollY)
                mTimeLastUp= SystemClock.uptimeMillis();

            mLayout.stopSelect();
            mEditMode=false;

            if (!mDoubleTap&&mScrollVelocity!=0)    //继续滑动并持续减速
            {
                naturalScroll();
            }

            mLastScrollY=mScrollY;
            mLastMoveY=Float.MIN_VALUE;

            //停止触摸即停止自动滚动
            stopAutoScroll();
        }else if (event.getAction()==MotionEvent.ACTION_MOVE)
        {
            if (mDoubleTap||mEditMode)
            {

            }else
            {
                /**
                 * 正常的滑动操作
                 */
                float dy=(mScrollStartY-event.getY());
                if (canScroll(dy)) {
                    mScrollY=mLastScrollY+dy;
                    checkFixScroll();
                }else
                {
                    /**
                     * 如果不在滑动到顶/底时更新记录的滚动值，那么此时反向滑动将不能立即奏效
                     */
                    mScrollStartY=event.getY();
                    mLastScrollY=mScrollY;
                }

                if (mLastMoveY!=Float.MIN_VALUE)//滚动速度
                {
                    mScrollVelocity=(mLastMoveY-event.getY())*10000000/(SystemClock.uptimeMillis()-mTimeLastMove);
                }
            }

            mLastMoveY=event.getY();
        }

        if (mEditMode)      //选中操作
        {
            mLayout.select(selectIndex);

            /**
             * 选中的时候，触摸点偏上或者偏下，就让它自己滚动
             */
            if (event.getY()<getMeasuredHeight()/5)
            {
                autoScroll(event.getY()-getMeasuredHeight()/5);
            }else if (event.getY()>getMeasuredHeight()/5*4)
            {
                autoScroll((event.getY()-getMeasuredHeight()/5*4));
            }else
            {
                stopAutoScroll();
            }
            invalidate();
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
        mLayout.increaseWidthTo(getMeasuredWidth());
        mLayout.setOne(mOneWidth,-1);
    }
}
