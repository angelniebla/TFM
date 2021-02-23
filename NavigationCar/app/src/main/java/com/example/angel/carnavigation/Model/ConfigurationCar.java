package com.example.angel.carnavigation.Model;

public class ConfigurationCar {
    private String uid;
    private Boolean alertAccident;
    private Boolean alertStatus;
    private Boolean alertSpeed;
    private Boolean alertHelp;
    private Boolean alertEvent;
    private Boolean alertCurve;

    public ConfigurationCar(String uid, Boolean alertAccident, Boolean alertStatus, Boolean alertSpeed, Boolean alertHelp, Boolean alertEvent, Boolean alertCurve) {
        this.uid = uid;
        this.alertAccident = alertAccident;
        this.alertStatus = alertStatus;
        this.alertSpeed = alertSpeed;
        this.alertHelp = alertHelp;
        this.alertEvent = alertEvent;
        this.alertCurve = alertCurve;
    }
}

