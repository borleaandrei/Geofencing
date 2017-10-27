package com.example.borleaandrei.geofenceapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService extends Service {
    private final static String TAG = "geofencing";

    public BroadcastService() {
    }

    private static BroadcastReceiver mReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Entered onReceive");
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG,"Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG,"Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG,"Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG,"Turning Bluetooth on...");
                        break;
                    case BluetoothAdapter.ERROR:
                        Log.i(TAG,"ERROR bluetooth state is't found");
                        break;
                }
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"entered onCreate in BroadcastService");
    }




    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"entered onDestroy() in BroadcastService");
    }
}
