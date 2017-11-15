package com.iqiyi.liquanfei_sx.vpnt.editor;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.text.AddedInput;
import com.iqiyi.liquanfei_sx.vpnt.text.FileInfo;
import com.iqiyi.liquanfei_sx.vpnt.view.FixedWidthTextView;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2017/11/13.
 */

public class EditActivity extends AppCompatActivity {

    public static final String ACTION_OPEN_PACKET="packet";

    private CommonPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_test);

        mPresenter=new EditPresenter(this);
        mPresenter.bindView(findViewById(R.id.container_editor));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity,menu);
        return true;
    }
}
