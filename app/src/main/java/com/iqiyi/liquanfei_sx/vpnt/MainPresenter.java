package com.iqiyi.liquanfei_sx.vpnt;

import android.app.Activity;
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

    private AppCompatActivity mActivity;

    public MainPresenter(AppCompatActivity activity)
    {
        mActivity=activity;
    }

    @Override
    protected void onViewBind(View v) {
        mViewPager=(ViewPager)v.findViewById(R.id.pager_main);

        mPagerAdapter=new MainPagerAdapter(mActivity.getSupportFragmentManager(),new Fragment[]{
            new SavedFragment(),new HistoryFragment()
        });
        mViewPager.setAdapter(mPagerAdapter);
    }
}
