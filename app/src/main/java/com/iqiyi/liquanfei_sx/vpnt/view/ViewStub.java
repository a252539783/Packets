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
        final ViewParent viewParent = getParent();

        if (viewParent != null && viewParent instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup) viewParent;
            view.setId(getId());

            final int index = parent.indexOfChild(this);
            parent.removeViewInLayout(this);

            final ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                parent.addView(view, index, layoutParams);
            } else {
                parent.addView(view, index);
            }
        }

        return view;
    }
}
