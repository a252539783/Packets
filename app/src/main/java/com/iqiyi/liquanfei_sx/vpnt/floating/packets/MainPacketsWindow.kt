package com.iqiyi.liquanfei_sx.vpnt.floating.packets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iqiyi.liquanfei_sx.vpnt.R
import com.iqiyi.liquanfei_sx.vpnt.floating.FloatingWindow
import com.iqiyi.liquanfei_sx.vpnt.tools.DisplayHelper

/**
 * Created by Administrator on 2017/12/11.
 */
class MainPacketsWindow: FloatingWindow() {

    private val mPresenter:MainPresenter=MainPresenter();

    private var mWidth=FloatingWindow.screenWidth()/4*3;
    private var mHeight=FloatingWindow.screenHeight()/4;

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?): View {
        enableBorder(sWindowBorderWidth);
        return super.onCreateView(inflater, container);
    }

    override fun getLayout()=R.layout.floating_main;

    override fun getPresenter()=mPresenter;

    override fun autoMove() =false;

    override fun getDefaultWindowSize(size: IntArray?) {
        if (size!=null&&size.size==2)
        {
            size[0]=mWidth;
            size[1]=mHeight;
        }
    }

    override fun canMove()=true;

    override fun showBorder(): Boolean {
        return true;
    }
}