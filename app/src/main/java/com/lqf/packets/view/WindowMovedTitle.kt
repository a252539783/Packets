package com.lqf.packets.view

/**
 * Created by Administrator on 2017/12/13.
 */

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by Administrator on 2017/12/13.
 */

class WindowMovedTitle : Toolbar {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev)||ev.actionMasked==MotionEvent.ACTION_MOVE
    }
}
