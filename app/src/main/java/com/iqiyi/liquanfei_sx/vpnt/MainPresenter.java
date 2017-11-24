package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Intent;
import android.net.VpnService;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.history.HistoryFragment;
import com.iqiyi.liquanfei_sx.vpnt.packet.ClientService;
import com.iqiyi.liquanfei_sx.vpnt.packet.ServerService;
import com.iqiyi.liquanfei_sx.vpnt.saved.SavedFragment;

/**
 * Created by Administrator on 2017/11/8.
 */

public class MainPresenter extends CommonPresenter implements View.OnClickListener{

    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TabLayout mTab;
    private FloatingActionButton mButton_start;

    private AppCompatActivity mActivity;

    public MainPresenter(AppCompatActivity activity)
    {
        mActivity=activity;
    }

    @Override
    protected void onViewBind(View v) {
        mViewPager=(ViewPager)v.findViewById(R.id.pager_main);
        mTab=(TabLayout)v.findViewById(R.id.tab_main);
        mButton_start=(FloatingActionButton)v.findViewById(R.id.fab_start);
        mButton_start.setOnClickListener(this);

        mPagerAdapter=new MainPagerAdapter(mActivity.getSupportFragmentManager(),new Fragment[]{
            new SavedFragment(),new HistoryFragment()
        },new String[]{
                "saved",        "history"
        });

        mViewPager.setAdapter(mPagerAdapter);
        mTab.setupWithViewPager(mViewPager);
    }

    private void startService()
    {
        final Intent i= VpnService.prepare(mActivity);

        if (i==null)
        {
            Log.e("xx","success");
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
}
