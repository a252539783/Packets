package com.iqiyi.liquanfei_sx.vpnt.editor;

import android.view.Menu;

import com.iqiyi.liquanfei_sx.vpnt.CommonActivity;
import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;

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
