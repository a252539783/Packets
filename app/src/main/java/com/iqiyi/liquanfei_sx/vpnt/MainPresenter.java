package com.iqiyi.liquanfei_sx.vpnt;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.history.HistoryFragment;
import com.iqiyi.liquanfei_sx.vpnt.saved.SavedFragment;

/**
 * Created by Administrator on 2017/11/8.
 */

public class MainPresenter extends CommonPresenter {

    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TabLayout mTab;

    private AppCompatActivity mActivity;

    public MainPresenter(AppCompatActivity activity)
    {
        mActivity=activity;
    }

    @Override
    protected void onViewBind(View v) {
        mViewPager=(ViewPager)v.findViewById(R.id.pager_main);
        mTab=(TabLayout)v.findViewById(R.id.tab_main);

        mPagerAdapter=new MainPagerAdapter(mActivity.getSupportFragmentManager(),new Fragment[]{
            new SavedFragment(),new HistoryFragment()
        },new String[]{
                "saved",        "history"
        });

//        mTab.addTab(mTab.newTab().setText("saved"));
//        mTab.addTab(mTab.newTab().setText("history"));
        mViewPager.setAdapter(mPagerAdapter);
        mTab.setupWithViewPager(mViewPager);
    }
}
