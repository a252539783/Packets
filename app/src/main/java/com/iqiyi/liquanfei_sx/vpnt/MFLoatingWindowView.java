package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2017/9/28.
 */

public class MFLoatingWindowView extends FrameLayout {

    public MFLoatingWindowView(Context context) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.float_main_button,null,false));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
