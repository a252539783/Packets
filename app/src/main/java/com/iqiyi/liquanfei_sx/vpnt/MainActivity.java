package com.iqiyi.liquanfei_sx.vpnt;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.service.ClientService;
import com.iqiyi.liquanfei_sx.vpnt.service.ServerService;

public class MainActivity extends AppCompatActivity {

    private ClientService mServer=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
        final Intent i= VpnService.prepare(this);

        if (i==null)
        {
            Log.e("xx","success");
            onActivityResult(1,1,null);
        }else
        {
            startActivityForResult(i,1);
        }
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
        startService(new Intent(this,ClientService.class));
        startService(new Intent(this,ServerService.class));
        bindService(new Intent(this, ClientService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServer=((ClientService.MB)service).get();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServer=null;
            }
        },BIND_AUTO_CREATE);
    }
}
