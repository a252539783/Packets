package com.iqiyi.liquanfei_sx.vpnt;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iqiyi.liquanfei_sx.vpnt.floating.MFloatingWindow;
import com.iqiyi.liquanfei_sx.vpnt.service.ClientService;
import com.iqiyi.liquanfei_sx.vpnt.service.ServerService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ClientService.OnPacketsAddListener,ClientService.OnServerConnectedListener{

    private ClientService mServer=null;
    private Button button;
    MFloatingWindow window;
    ExpandableRecyclerView rv;
    PacketsAdapter pa;
    ServiceConnection sc;
    private boolean foreground=true;
    StringBuilder builder=new StringBuilder("textTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
    TextView tv;
    String ss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //window=new MFloatingWindow(this);
        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.test_text);

        button=(Button)findViewById(R.id.b_test);
        button.setOnClickListener(this);
        tv.setText(builder);

        rv=(ExpandableRecyclerView)findViewById(R.id.rv);
    }

    @Override
    public void onClick(View v) {
        //window.remove();

        if (builder.length()<=200000)
            ss=builder.append(builder).toString();
        Log.e("xx",tv.getTransformationMethod()+""+tv.getLayout().getClass().getName());
        tv.setText(ss);

        //pa.notifyDataSetChanged();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startService(new Intent(this,ServerService.class));
        startService(new Intent(this,ClientService.class));
        bindService(new Intent(this, ClientService.class),sc= new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServer=((ClientService.MB)service).get();
                mServer.setOnServerConnectedListener(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServer=null;
            }
        },BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pa!=null)
        {
            pa.freshFilter();
            mServer.setOnPacketsAddListener(this);
        }else
        {

            final Intent i= VpnService.prepare(this);

            if (i==null)
            {
                Log.e("xx","success");
                //onActivityResult(1,1,null);
            }else
            {
                //startActivityForResult(i,1);
            }
        }
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
                pa.notifyDataInserted(position);
            }
        });
    }

    @Override
    public void onConnected() {
        rv.post(new Runnable() {
            @Override
            public void run() {

                pa=new PacketsAdapter(mServer.getPackets(),MainActivity.this);
                rv.setAdapter(pa);
                //pa.setFilterKey(PacketsAdapter.FILTER_IP,"180.149.136.228");
                mServer.setOnPacketsAddListener(MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.removeOnPacketsAddListener();
        mServer.removeOnServerConnectedListener();
        unbindService(sc);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pa!=null)
        {
            mServer.removeOnPacketsAddListener();
        }
    }
}
