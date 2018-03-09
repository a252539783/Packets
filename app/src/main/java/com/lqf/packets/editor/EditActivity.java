package com.lqf.packets.editor;

import android.view.Menu;

import com.lqf.packets.CommonActivity;
import com.lqf.packets.CommonPresenter;
import com.lqf.packets.R;

/**
 * Created by Administrator on 2017/11/13.
 */

public class EditActivity extends CommonActivity {

    public static final String ACTION_OPEN_PACKET="packet";

    private CommonPresenter mPresenter;

    @Override
    public CommonPresenter getPresenter() {
        if (mPresenter==null)
            mPresenter=new EditPresenter(this);

        return mPresenter;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_test;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity,menu);
        return true;
    }
}
