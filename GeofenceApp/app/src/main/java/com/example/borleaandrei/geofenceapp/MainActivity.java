package com.example.borleaandrei.geofenceapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList = new ArrayList<Geofence>();
    private ArrayList<GeofenceLocation> mGeofenceLocations = new ArrayList<GeofenceLocation>();
    private PendingIntent mGeofencePendingIntent;
    private static final int REQUEST_LOCATION_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, PingLocationService.class);
        startService(intent);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        GeofenceLocation loc1 = new GeofenceLocation(45.763199, 21.225712, 100,"Piata Consiliul Europei");
        GeofenceLocation loc2 = new GeofenceLocation(45.754067, 21.225857, 100,"Opera Nationala");
        GeofenceLocation loc3 = new GeofenceLocation(45.750728, 21.224276, 100,"Catedrala");
        GeofenceLocation loc4 = new GeofenceLocation(45.749230, 21.220205, 100,"BusyMachines");
        GeofenceLocation loc5 = new GeofenceLocation(45.747271, 21.226563, 100,"UPT");
        GeofenceLocation loc6 = new GeofenceLocation(45.746796, 21.234866, 100,"Ion Vidu");
        GeofenceLocation loc7 = new GeofenceLocation(45.748359, 21.239745, 100,"Cantina Poli");
        GeofenceLocation loc8 = new GeofenceLocation(45.755500, 21.227356, 100,"Piata Libertatii");
        GeofenceLocation loc9 = new GeofenceLocation(45.758759, 21.227201, 100,"SuppaBar");
        GeofenceLocation loc10 = new GeofenceLocation(45.764968,21.219474 ,100,"Piata Dacia");
        GeofenceLocation loc11 = new GeofenceLocation(45.765589,21.222473 ,100,"Brandusei nr.6");
        GeofenceLocation loc12 = new GeofenceLocation(45.764512,21.224215,100,"OTP Bank");
        GeofenceLocation loc13 = new GeofenceLocation(45.766720,21.228569,100,"Iulius Mall");
        mGeofenceLocations.add(loc1);
        mGeofenceLocations.add(loc2);
        mGeofenceLocations.add(loc3);
        mGeofenceLocations.add(loc4);
        mGeofenceLocations.add(loc5);
        mGeofenceLocations.add(loc6);
        mGeofenceLocations.add(loc7);
        mGeofenceLocations.add(loc8);
        mGeofenceLocations.add(loc9);
        mGeofenceLocations.add(loc10);
        mGeofenceLocations.add(loc11);
        mGeofenceLocations.add(loc12);
        mGeofenceLocations.add(loc13);

        for (GeofenceLocation location : mGeofenceLocations) {

            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(location.getRequestId())
                    .setCircularRegion(location.getLatitude(), location.getLongitude(), location.getRadius())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }
        checkLocationPermissions();
    }

    private void checkLocationPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);
        }
        else addGeofences();
    }

    private void addGeofences() {
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("geofence", "Geofences added");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("geofence", "Failed to add geofences");

                    }
                });

        Log.d(" Ping", "Ping");

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Log.d("Intent","Intent");
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode){

            case REQUEST_LOCATION_PERMISSIONS: {

                if(!(grantResults.length == 2 && grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED  )){
                    Toast.makeText(getApplicationContext(),"You need Location Permissions for this app!!",Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                    addGeofences();
            }
        }
    }
}