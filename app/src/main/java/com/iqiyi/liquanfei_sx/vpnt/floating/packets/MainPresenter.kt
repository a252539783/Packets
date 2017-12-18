package com.iqiyi.liquanfei_sx.vpnt.floating.packets

import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter
import com.iqiyi.liquanfei_sx.vpnt.MApp
import com.iqiyi.liquanfei_sx.vpnt.R
import com.iqiyi.liquanfei_sx.vpnt.floating.FloatingPresenter
import com.iqiyi.liquanfei_sx.vpnt.view.ViewStub

/**
 * Created by Administrator on 2017/12/11.
 */
class MainPresenter:FloatingPresenter(),MApp.OnDispatchResourceListener
{
    private var mTabLayout:TabLayout?=null
    private var mPager:ViewPager?=null
    private var mStub: ViewStub?=null
    private var mTitle:Toolbar?=null

    override fun onDispatch(resourceCode: Int, resource: Any?) {
        mPager=mStub?.load(resource as View) as ViewPager;
        mTabLayout?.setupWithViewPager(mPager);
    }

    override fun onViewBind(v: View?) {
        super.onViewBind(v)

        mTitle=v?.findViewById(R.id.toolbar_float_main)as Toolbar;
        mTabLayout=v.findViewById(R.id.tab_float_main) as TabLayout;
        mStub=v.findViewById(R.id.pager_float_main) as ViewStub;

        mTitle?.setOnTouchListener(window);
    }

    override fun onResume() {
        super.onResume()
        MApp.get().getResource(MApp.RESOURCE_PACKET_PAGER,this);
    }

    override fun onPause() {
        super.onPause()
        ViewStub.replace(mPager,mStub)
        MApp.get().releaseResource(MApp.RESOURCE_PACKET_PAGER,mPager);
        MApp.get().releaseResource(MApp.RESOURCE_PACKET_PAGER,this);
    }
}