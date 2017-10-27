package com.example.borleaandrei.geofenceapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * Created by borleaandrei on 24/10/2017.
 */

public class ServiceBluetoothScan extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private UUID SERVICE_UUID;
    private UUID CHARACTERISTIC_UUID = UUID.fromString("31517c58-66bf-470c-b662-e352a6c80cba");
    private UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothGatt mGatt;
    private BluetoothLeScannerCompat scanner;
    private boolean isConnected = false;
    private BluetoothDevice currentDevice;
    private Timer timer=null;
    private BluetoothManager mBluetoothManager;
    private byte[] value;
    private static final String TAG="bleQ";
    private boolean isBroadcastReceiverRegistered = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"Bluetooth off");
                        stopSelf();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {

            Log.d(TAG,"onScanResult");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

            if (!results.isEmpty()) {
                Log.d(TAG,"onBatchScanResults ->have result");
                ScanResult result = results.get(0);

                final int rssiValue = result.getRssi();

                if(rssiValue > -80){

                    scanner.stopScan(mScanCallback);

                    final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(result.getDevice().getAddress());
                    currentDevice = device;
                    mGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);

                    final int[] v = new int[]{0};
                    timer = new Timer();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            v[0]++;
                            if(v[0]==5 && mGatt!=null){
                                mGatt.close();
                                mGatt.disconnect();
                                mGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
                            }

                        }
                    },0,1000);
                }

            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG,"Scan error!!");
        }

    };


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.d(TAG,"2value="+value);
            Log.d(TAG,"4 status="+status+"  newState="+newState);

            if(status == BluetoothGatt.GATT_SUCCESS)
                Log.d(TAG,"onConnectionStateChanged Success");
            else
            if(status == BluetoothGatt.GATT_FAILURE)
                Log.d(TAG,"onConnectionStateChanged Failure");
            if (newState == BluetoothProfile.STATE_CONNECTED) {


                Log.d(TAG, "Connected to GATT client. Attempting to start service discovery");
                gatt.discoverServices();
                isConnected = true;
                timer.cancel();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT client");
            }


            if(status == 133){
                Log.d(TAG,"GATT_INTERNAL_ERROR=133");
                mGatt.close();
                mGatt.disconnect();
                mGatt = currentDevice.connectGatt(getApplicationContext(), false, mGattCallback);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Log.d(TAG,"3value="+value);
            Log.d(TAG,"onServiceDiscovered  "+status);
            BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID)
                    .getCharacteristic(CHARACTERISTIC_UUID);

            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            Log.d(TAG,"writeDescriptor");

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG,"4value="+value);
            Log.d(TAG,"onDescriptorWrite");
            value = new byte[]{1};

            BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
            characteristic.setValue(value);
            boolean result =gatt.writeCharacteristic(characteristic);
            Log.d(TAG,"first attempt to write on characteristic result="+result);
            if(!result)
            {
                Log.d(TAG,"trying for the second time");

            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG,"onCharacteristicWrite");

        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            Log.d(TAG,"onCharacteristicChanged");
            byte[] value = characteristic.getValue();

            if(value[0] == 1 ) {
                Log.d(TAG, "Door open");

                mGatt.close();
                mGatt.disconnect();
                mGatt = null;
                disableBluetooth();
                stopSelf();
            }
            else {
                Log.d(TAG, "Apparently the password from the server is incorrect");

                mGatt.close();
                mGatt.disconnect();
                mGatt = null;
                disableBluetooth();
                stopSelf();


            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"entered onCreate ServiceBluetoothScan");
    }

    private void startBluetoothScaning(){

        Log.d(TAG,"bluetooth scanning started");

        scanner = BluetoothLeScannerCompat.getScanner();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(500)
                .build();


        SERVICE_UUID = new UUID(0,Integer.parseInt("111111"));

        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_UUID)).build();

        scanner.startScan(Arrays.asList(scanFilter), settings, mScanCallback);

        Log.d(TAG,"reached");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {

        Log.d(TAG,"onStartCommand entered in ServiceBluetoothScan");

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        startBluetoothScaning();

        return START_NOT_STICKY;
    }


    public void disableBluetooth(){
        mBluetoothAdapter.disable();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        isBroadcastReceiverRegistered = true;
        registerReceiver(mReceiver, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if(isBroadcastReceiverRegistered)
            unregisterReceiver(mReceiver);
    }

}