package com.iqiyi.liquanfei_sx.vpnt;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.iqiyi.liquanfei_sx.vpnt.text.AddedInput;
import com.iqiyi.liquanfei_sx.vpnt.text.FileInfo;
import com.iqiyi.liquanfei_sx.vpnt.view.FixedWidthTextView;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2017/11/13.
 */

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_test);
        AddedInput.CacheThread.init();

        try {
            ((FixedWidthTextView)findViewById(R.id.text_test)).setFile(new FileInfo(new File(getIntent().getData().getPath())));
        } catch (IOException e) {
            Log.e("xx",e.toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        //Log.e("xx",getIntent().getDataString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //AddedInput.CacheThread.q
    }
}
