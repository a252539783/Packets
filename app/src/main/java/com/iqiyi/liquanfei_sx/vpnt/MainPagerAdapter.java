package com.iqiyi.liquanfei_sx.vpnt;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

/**
 * Created by Administrator on 2017/11/6.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    private Fragment []mFragments;

    public MainPagerAdapter(FragmentManager fm,Fragment[] fragments) {
        super(fm);
        mFragments=fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }
}
