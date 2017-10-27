package com.example.borleaandrei.geofenceapp;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Created by borleaandrei on 23/10/2017.
 */

public class PingLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private Location currentLocation;
    private static final String TAG="geofencing";

    private static BroadcastReceiver mReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Entered onReceive");
            final String action = intent.getAction();
            Toast.makeText(context, "Started", Toast.LENGTH_LONG).show();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG,"Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG,"Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG,"Bluetooth on");

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG,"Turning Bluetooth on...");
                        break;
                    case BluetoothAdapter.ERROR:
                        Log.e(TAG,"ERROR bluetooth state is't found");
                        break;
                }
            }

        }
    };




    private LocationCallback locationCallback = new LocationCallback(){

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            currentLocation = locationResult.getLastLocation();
            Log.d("locationQWERT","location   lat="+currentLocation.getLatitude()+" long="+currentLocation.getLongitude());
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

            if(!locationAvailability.isLocationAvailable())
                Log.d("location","location isn't available");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(" Ping"," Ping created");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
       // LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("location","GoogleApiConnection established");
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (currentLocation == null)
            Log.d("location","current location is null");

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("location","GoogleApiConnection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("location","GoogleApiConnection failed");
    }

    private  LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(createLocationRequest(), locationCallback,this.getMainLooper());
    }
    public static BroadcastReceiver getBroadcastReceiver(){

        return mReceiver;
    }
}
