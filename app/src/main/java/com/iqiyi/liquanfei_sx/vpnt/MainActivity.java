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

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ClientService.OnPacketsAddListener,ClientService.OnServerConnectedListener{

    private ClientService mServer=null;
    private Button button;
    MFloatingWindow window;
    ExpandableRecyclerView rv;
    HistoryAdapter.PacketsAdapter pa;
    ServiceConnection sc;
    private boolean foreground=true;
    StringBuilder builder=new StringBuilder("abcdefg");
    FixedWidthTextView tv;
    String ss;

    MainPresenter mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //window=new MFloatingWindow(this);
        // Example of a call to a native method
        //tv = (FixedWidthTextView) findViewById(R.id.test_text);

        //button=(Button)findViewById(R.id.b_test);
        //button.setOnClickListener(this);

        //rv=(ExpandableRecyclerView)findViewById(R.id.erv_history);

        mp=new MainPresenter(this);
        mp.bindView(findViewById(R.id.drawer_main));

//        final Intent i= VpnService.prepare(this);
//
//        if (i==null)
//        {
//            Log.e("xx","success");
//            onActivityResult(1,1,null);
//        }else
//        {
//            startActivityForResult(i,1);
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onClick(View v) {
        //window.remove();
        //pa.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startService(new Intent(this,ServerService.class));
        startService(new Intent(this,ClientService.class));
//        bindService(new Intent(this, ClientService.class),sc= new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                mServer=((ClientService.MB)service).get();
//                //mServer.setOnServerConnectedListener(MainActivity.this);
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                mServer=null;
//            }
//        },BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    void fresh()
    {
        rv.post(new Runnable() {
            @Override
            public void run() {
                pa.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPacketsAdd(final int position) {
        rv.post(new Runnable() {
            @Override
            public void run() {
                //Log.e("xx","in ui  add");
                //pa.notifyDataInserted(position);
            }
        });
    }

    @Override
    public void onConnected() {
        rv.post(new Runnable() {
            @Override
            public void run() {

                //pa=new HistoryAdapter.PacketsAdapter(mServer.getPackets(),MainActivity.this);
                //rv.setAdapter(pa);
                //pa.setFilterKey(PacketsAdapter.FILTER_IP,"180.149.136.228");
                //mServer.setOnPacketsAddListener(MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mServer.removeOnPacketsAddListener();
//        mServer.removeOnServerConnectedListener();
//        unbindService(sc);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (pa!=null)
//        {
//            mServer.removeOnPacketsAddListener();
//        }
    }
}
