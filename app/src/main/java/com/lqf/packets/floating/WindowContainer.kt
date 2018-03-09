package com.lqf.packets.floating

import android.content.Context
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Created by Administrator on 2017/12/12.
 */

open class WindowContainer(context: Context,val mStack: WindowStack) : FrameLayout(context) {

    var mEnableBorder = false

    private var mResizePosition=0;

    private var mResizing:Boolean=false;

    private var mBorderWidth = 10
    private val mPaint = Paint()
    private var mLastX: Float = 0f
    private var mLastY:Float = 0f

    fun setBorderColor(color: Int) {
        mPaint.color = color
    }

    fun setBorderWidth(width: Int) {
        mBorderWidth = width
    }

    fun enableBorder(enable: Boolean) {
        mEnableBorder = enable
    }

    companion object {
        val LEFT=0x1
        val RIGHT=0x2;
        val TOP=0x4;
        val BOTTOM=0x8;
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        super.onTouchEvent(e)

        if (e!=null)
        {
            when (e.getActionMasked()) {
                MotionEvent.ACTION_DOWN -> {
                    mResizePosition=0;
                    if (e.x>=0&&e.x<mBorderWidth)
                    {
                        mResizePosition=mResizePosition or LEFT;
                        mResizing=true;
                    }else if (e.x>width-mBorderWidth&&e.x<=width)
                    {
                        mResizePosition =mResizePosition or RIGHT;
                        mResizing=true;
                    }

                    if (e.y>=0&&e.y<mBorderWidth)
                    {
                        mResizePosition = mResizePosition or TOP
                        mResizing=true;
                    }else if (e.y>height-mBorderWidth&&e.y<=height)
                    {
                        mResizePosition=mResizePosition or BOTTOM
                        mResizing=true;
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mResizing)
                    {
                        var dx=e.rawX-mLastX;
                        var dy=e.rawY-mLastY;
                        var movex=dx;
                        var movey=dy;

                        if (mResizePosition and LEFT !=0)
                        {
                            dx=-dx;
                        }else{
                            movex=0f;
                            if (mResizePosition and RIGHT==0)
                            {
                                dx=0f;
                            }
                        }

                        if (mResizePosition and TOP!=0)
                        {
                            dy=-dy;
                        }else
                        {
                            movey=0f;
                            if (mResizePosition and BOTTOM==0)
                            {
                                dy=0f;
                            }
                        }

                        mStack.resizeWindow(dx,dy,movex,movey);
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mResizing=false;
                    mStack.endResize(mResizePosition);
                    mResizePosition=0;
                }
            }

            mLastX=e.rawX;
            mLastY=e.rawY;
        }

        return true;
    }
}
