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

public class MainActivity extends AppCompatActivity{

    private MainPresenter mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //window=new MFloatingWindow(this);

        mp=new MainPresenter(this);
        mp.bindView(findViewById(R.id.drawer_main));
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
    protected void onResume() {
        super.onResume();
    }

//    @Override
//    public void onConnected() {
//        rv.post(new Runnable() {
//            @Override
//            public void run() {
//
//                //pa=new HistoryAdapter.PacketsAdapter(mServer.getPackets(),MainActivity.this);
//                //rv.setAdapter(pa);
//                //pa.setFilterKey(PacketsAdapter.FILTER_IP,"180.149.136.228");
//                //mServer.setOnPacketsAddListener(MainActivity.this);
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
