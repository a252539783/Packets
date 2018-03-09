package com.lqf.packets.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2017/11/20.
 */

public class NestedScrollViewPager extends ViewPager implements NestedScrollingParent,NestedScrollingChild{

    private NestedScrollingParentHelper mScrollingParentHelper=null;
    private NestedScrollingChildHelper mScrollingChildHelper=null;

    public NestedScrollViewPager(Context context) {
        super(context);
    }

    public NestedScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private NestedScrollingParentHelper getScrollingParentHelper() {
        if (mScrollingParentHelper == null) {
            mScrollingParentHelper = new NestedScrollingParentHelper(this);
        }
        return mScrollingParentHelper;
    }

    private NestedScrollingChildHelper getmScrollingChildHelper() {
        if (mScrollingChildHelper == null) {
            mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mScrollingChildHelper;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return getParent().onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getmScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        getParent().onNestedPreScroll(target, dx, dy, consumed);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        getScrollingParentHelper().onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {
        getScrollingParentHelper().onStopNestedScroll(child);
    }
}
