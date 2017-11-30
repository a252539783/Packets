package com.iqiyi.liquanfei_sx.vpnt.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * this ViewStub only can load a view from code
 */

public class ViewStub extends View {
    public ViewStub(Context context) {
        super(context);
    }

    public ViewStub(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewStub(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View load(View view)
    {
        replace(this,view);
        return view;
    }

    public static void remove(View v)
    {
        if (v==null)
            return ;

        final ViewParent viewParent = v.getParent();
        if (viewParent == null || !(viewParent instanceof ViewGroup))
            return ;

        final ViewGroup parent = (ViewGroup) viewParent;
        parent.removeViewInLayout(v);
    }

    public static void replace(View from,View to)
    {
        if (from==null||to==null)
            return ;

        final ViewParent viewParent = from.getParent();
        if (viewParent == null || !(viewParent instanceof ViewGroup))
            return ;

        final ViewGroup parent = (ViewGroup) viewParent;

        to.setId(from.getId());
        final int index = parent.indexOfChild(from);
        parent.removeViewInLayout(from);
        final ViewGroup.LayoutParams layoutParams = from.getLayoutParams();
        if (layoutParams != null) {
            parent.addView(to, index, layoutParams);
        } else {
            parent.addView(to, index);
        }
    }
}
