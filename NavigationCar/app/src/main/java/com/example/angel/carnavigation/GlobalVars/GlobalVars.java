package com.example.angel.carnavigation.GlobalVars;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

public class GlobalVars {
    private static GlobalVars _instance;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Boolean alertAll;
    private Boolean alertAccident;
    private Boolean alertStatus;
    private Boolean alertSpeed;
    private Boolean alertHelp;
    private Boolean alertEvent;
    private Boolean roadLine;
    private String speed;
    private String latitude;
    private String longitude;
    private String latitude_old;
    private String longitude_old;
    private String latitude_dst;
    private String longitude_dst;
    private Boolean showAlert;


    public GlobalVars() {
    }

    public static GlobalVars getInstance(){
        if(_instance == null) {
            _instance = new GlobalVars();
        }
        return _instance;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public GoogleSignInClient getSignInClient() {
        return mGoogleSignInClient;
    }

    public void setSignInClient(GoogleSignInClient mGoogleSignInClient) {
        this.mGoogleSignInClient = mGoogleSignInClient;
    }

    public Boolean getAlertAll() {
        if(alertAll != null){
            return alertAll;
        }
        return true;
    }

    public void setAlertAll(Boolean alertAll) {
        this.alertAll = alertAll;
    }

    public Boolean getAlertAccident() {
        if(alertAccident != null){
            return alertAccident;
        }
        return true;
    }

    public void setAlertAccident(Boolean alertAccident) {
        this.alertAccident = alertAccident;
    }

    public Boolean getAlertStatus() {
        if(alertStatus != null){
            return alertStatus;
        }
        return true;
    }

    public void setAlertStatus(Boolean alertStatus) {
        this.alertStatus = alertStatus;
    }

    public Boolean getAlertSpeed() {
        if(alertSpeed != null){
            return alertSpeed;
        }
        return true;
    }

    public void setAlertSpeed(Boolean alertSpeed) {
        this.alertSpeed = alertSpeed;
    }

    public Boolean getAlertHelp() {
        if(alertHelp != null){
            return alertHelp;
        }
        return true;
    }

    public void setAlertHelp(Boolean alertHelp) {
        this.alertHelp = alertHelp;
    }

    public Boolean getAlertEvent() {
        if(alertEvent != null){
            return alertEvent;
        }
        return true;
    }

    public void setAlertEvent(Boolean alertEvent) {
        this.alertEvent = alertEvent;
    }

    public Boolean getRoadLine() {
        if(roadLine != null){
            return roadLine;
        }
        return true;
    }

    public void setRoadLine(Boolean alertCurve) {
        this.roadLine = alertCurve;
    }

    public String getSpeed() {
        if(speed == null){
            return "0";
        }
        else{
            return speed;
        }

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

    public String getLatitude_dst() {
        if(latitude_dst == null){
            return "0";
        }
        else{
            return latitude_dst;
        }
    }

    public void setLatitude_dst(String latitude_dst) {
        this.latitude_dst = latitude_dst;
    }

    public String getLongitude_dst() {
        if(longitude_dst == null){
            return "0";
        }
        else{
            return longitude_dst;
        }
    }

    public void setLongitude_dst(String longitude_dst) {
        this.longitude_dst = longitude_dst;
    }


    public Boolean getShowAlert() {
        if(showAlert == null){
            return true;
        }
        else{
            return showAlert;
        }
    }

    public void setShowAlert(Boolean showAlert) {
        this.showAlert = showAlert;
    }

    public String getLatitudeOld() {
        return latitude_old;
    }

    public void setLatitudeOld(String latitude_old) {
        this.latitude_old = latitude_old;
    }

    public String getLongitudeOld() {
        return longitude_old;
    }

    public void setLongitudeOld(String longitude_old) {
        this.longitude_old = longitude_old;
    }
}
