package com.lqf.packets.view

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * Created by Administrator on 2017/12/14.
 */
class DrawableView(context: Context,attributes: AttributeSet?=null,style:Int=0): View(context,attributes,style) {

    constructor(context:Context,attrs:AttributeSet) : this(context,attrs,0) {
    }

    private var mDrawId=0;

    private val mDrawList=LinkedList<DrawInfo>()

    fun postDraw(runnable: DrawRunnable,duration:Long=-1):Int
    {
        mDrawList.add(DrawInfo(runnable,duration,++mDrawId));

        return mDrawId;
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas==null)
            return;

        val it=mDrawList.listIterator();
        while(it.hasNext())
        {
            val di=it.next();

            if (di.mDuration!=-1L&&SystemClock.uptimeMillis()-di.mStartTime>=di.mDuration)
            {
                it.remove();
            }else
            {
                di.mRunnable.onDraw(canvas);
            }
        }
    }

    class DrawInfo(val mRunnable: DrawRunnable,val mDuration: Long,val mId:Int)
    {
        val mStartTime:Long=SystemClock.uptimeMillis()
    }

    interface DrawRunnable
    {
        fun onDraw(c:Canvas);
    }
}