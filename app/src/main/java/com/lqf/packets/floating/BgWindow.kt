package com.lqf.packets.floating

import android.util.Log
import android.view.View
import com.lqf.packets.CommonPresenter
import com.lqf.packets.R
import com.lqf.packets.view.DrawableView

/**
 * Created by Administrator on 2017/12/14.
 */
class BgWindow:FloatingWindow() {
    private var mView:DrawableView?=null;

    override fun getLayout(): Int {
        return R.layout.background_window;
    }

    override fun getPresenter(): CommonPresenter {
        return object: CommonPresenter(){
            override fun onViewBind(v: View?) {
                super.onViewBind(v)
                mView=v as DrawableView;
            }
        }
    }

    override fun autoMove(): Boolean {
        return false;
    }

    override fun getDefaultWindowSize(size: IntArray?) {
        if (size!=null)
        {
            size[0]= sScreenWidth;
            size[1]= sScreenHeight;
            Log.e("xx","bgwindow x:$sScreenWidth")
        }
    }

    override fun canMove(): Boolean {
        return false;
    }

    fun postDraw(runnable:DrawableView.DrawRunnable,duration:Long=-1)
    {
        mView?.postDraw(runnable, duration);
    }

    override fun showBorder(): Boolean {
        return false;
    }
}