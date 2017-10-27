package com.example.borleaandrei.geofenceapp;

/**
 * Created by borleaandrei on 13/10/2017.
 */

public class GeofenceLocation {

    private double latitude;
    private double longitude;
    private float radius;
    private String requestId;

    public GeofenceLocation(double latitude, double longitude, float radius, String requestId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.requestId = requestId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return radius;
    }

    public String getRequestId() {
        return requestId;
    }
}
