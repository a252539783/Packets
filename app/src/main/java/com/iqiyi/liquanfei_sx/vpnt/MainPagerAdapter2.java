package com.iqiyi.liquanfei_sx.vpnt;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/11/30.
 */

public class MainPagerAdapter2 extends PagerAdapter {

    private FakeFragment[]mViews;
    private String[] mTitles;

    private LayoutInflater mInflater;

    public MainPagerAdapter2(LayoutInflater inflater, FakeFragment[]views, String[] title)
    {
        mViews=views;
        mTitles=title;
        mInflater=inflater;
    }

    public void onAttach()
    {
        for (FakeFragment pf:mViews)
        {
            pf.onResume();
        }
    }

    public void onDeAttach()
    {
        for (FakeFragment pf:mViews)
        {
            pf.onPause();
        }
    }

    @Override
    public int getCount() {
        return mViews.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v=mViews[position].getView();
        if (v==null)
            v=mViews[position].onCreateView(mInflater,container);

        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        container.removeView(mViews[position].getView());
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }
}
