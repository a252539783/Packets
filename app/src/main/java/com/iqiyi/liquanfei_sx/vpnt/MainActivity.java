package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.iqiyi.liquanfei_sx.vpnt.floating.MFloatingWindow;
import com.iqiyi.liquanfei_sx.vpnt.history.HistoryAdapter;
import com.iqiyi.liquanfei_sx.vpnt.packet.ClientService;
import com.iqiyi.liquanfei_sx.vpnt.packet.ServerService;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView;
import com.iqiyi.liquanfei_sx.vpnt.view.FixedWidthTextView;

public class MainActivity extends CommonActivity{

    private MainPresenter mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK)
        {
            mp.startServiceUnCheck();
        }
    }

    @Override
    public CommonPresenter getPresenter() {
        if (mp==null)
            mp=new MainPresenter(this);

        return mp;
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_main;
    }
}
