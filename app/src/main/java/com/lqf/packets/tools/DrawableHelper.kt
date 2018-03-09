package com.lqf.packets.tools

import android.graphics.Canvas
import android.graphics.Paint
import com.lqf.packets.view.DrawableView

/**
 * Created by Administrator on 2017/12/14.
 */
object DrawableHelper {

    fun newBorderDrawable(color:Int):BorderDrawable
    {
        val p=Paint();
        p.color=color;
        return BorderDrawable(p);
    }

    class BorderDrawable(var mPaint:Paint):DrawableView.DrawRunnable
    {
        var x=0f;
        var y=0f
        var w=0f
        var h=0f
        var border=0f;

        var enable=false;

        override fun onDraw(c: Canvas) {
            if (enable)
            {
                c.drawRect(x, y,x+w, y+border,mPaint )  //top
                c.drawRect(x+w-border,y,x+w,y+h,mPaint);    //right
                c.drawRect(x,y+h-border,x+w,y+h,mPaint);   //bottom
                c.drawRect(x,y,x+border,y+h,mPaint);  //left
            }
        }
    }
}