package com.example.angel.carnavigation.Model;

import java.util.Calendar;
import java.util.Date;

public class LocationCar {
    private String _id;
    private String speed;
    private String latitude;
    private String longitude;
    private String latitude_old;
    private String longitude_old;
    private Date currentTime;

    public LocationCar(String uid ,String speed, String latitude, String longitude, String latitude_old, String longitude_old) {
        this._id = uid;
        this.speed = speed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.latitude_old = latitude_old;
        this.longitude_old = longitude_old;
        this.currentTime = Calendar.getInstance().getTime();
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

