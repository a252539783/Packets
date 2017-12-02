package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Intent;
import android.net.VpnService;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.packet.ClientService;
import com.iqiyi.liquanfei_sx.vpnt.packet.ServerService;
import com.iqiyi.liquanfei_sx.vpnt.view.ViewStub;


public class MainPresenter extends CommonPresenter implements View.OnClickListener{

    private ViewPager mViewPager;
    private ViewStub mStub;
    private PagerAdapter mPagerAdapter;
    private TabLayout mTab;
    private FloatingActionButton mButton_start;

    private boolean mPaused=false;

    private AppCompatActivity mActivity;

    public MainPresenter(AppCompatActivity activity)
    {
        mActivity=activity;
    }

    @Override
    protected void onViewBind(View v) {
        mStub=(ViewStub) v.findViewById(R.id.pager_main);
        mTab=(TabLayout)v.findViewById(R.id.tab_main);
        mButton_start=(FloatingActionButton)v.findViewById(R.id.fab_start);
        mButton_start.setOnClickListener(this);
    }

    private void startService()
    {
        final Intent i= VpnService.prepare(mActivity);

        if (i==null)
        {
            startServiceUnCheck();
        }else
        {
            mActivity.startActivityForResult(i,1);
        }
    }

    void startServiceUnCheck()
    {
        if (!ClientService.isRun())
            mActivity.startService(new Intent(mActivity,ServerService.class));
        mActivity.startService(new Intent(mActivity,ClientService.class));
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.fab_start)
        {
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
            mViewPager=(ViewPager) mStub.load(MApp.get().packetContent());
            mTab.setupWithViewPager(mViewPager);
            mButton_start.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mButton_start.setVisibility(View.GONE);
        ViewStub.replace(mViewPager,mStub);
    }
}
