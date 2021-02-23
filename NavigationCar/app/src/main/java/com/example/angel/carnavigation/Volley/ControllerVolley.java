package com.example.angel.carnavigation.Volley;

import com.example.angel.carnavigation.GlobalVars.GlobalVars;
import com.example.angel.carnavigation.Listeners.ListenerRequest;
import com.example.angel.carnavigation.Model.ConfigurationCar;
import com.example.angel.carnavigation.Model.CredentialsCar;
import com.example.angel.carnavigation.Model.LocationCar;
import com.example.angel.carnavigation.SharedPreference.PreferenceManager;

public class ControllerVolley {

    private static ControllerVolley controller;
    private static ListenerRequest actualListener;
    private static APIRestVolley apiRest = new APIRestVolley().getInstance();
    private static GlobalVars gVars = new GlobalVars().getInstance();
    //private static String URL = "http://34.201.245.254/";
    private static String URL = "http://192.168.0.29/";

    /**
     * Constructor de la clase ControllerVolley
     */
    public ControllerVolley() {

    }

    /**
     * Obtener una instancia de ControllerVolley si ya esta creada, si no se crea
     * @return ControllerVoller
     */
    public static ControllerVolley getInstance(){
        if(controller == null){
            controller = new ControllerVolley();
        }
        return controller;
    }

    public ListenerRequest getActualListener() {
        return actualListener;
    }

    public static void updateLocation(ListenerRequest listener, String speed, String latitude, String longitude){
        actualListener = listener;
        String sUrl = URL + "car/";
        String uid = PreferenceManager.getInstance().getUserId();
        apiRest.postLocation(listener, sUrl, new LocationCar(uid,speed,latitude,longitude,latitude,longitude));
    }

    void processResponseRequest(ListenerRequest listenerRequest) {
        listenerRequest.requestCompleted();
    }

    void processErrorRequest(ListenerRequest listenerRequest, int statusCode){
        listenerRequest.requestError(statusCode);
    }

    public static void sendCredentials (ListenerRequest listener){
        actualListener = listener;
        String sUrl = URL + "credential/";
        String uid = PreferenceManager.getInstance().getUserId();
        String tokenId = PreferenceManager.getInstance().getTokenId();
        apiRest.postCredentials(listener, sUrl, new CredentialsCar(uid,tokenId));
    }

    public static void sendConfiguration (ListenerRequest listener){
        actualListener = listener;
        String sUrl = URL + "configuration/";
        String uid = PreferenceManager.getInstance().getUserId();
        Boolean alertAcccident = gVars.getAlertAccident();
        Boolean alertStatus  = gVars.getAlertStatus();
        Boolean alertSpeed = gVars.getAlertSpeed();
        Boolean alertHelp = gVars.getAlertHelp();
        Boolean alertEvent = gVars.getAlertEvent();
        Boolean alertCurve = gVars.getRoadLine();
        apiRest.postConfiguration(listener, sUrl, new ConfigurationCar(uid,alertAcccident,alertStatus,alertSpeed,alertHelp,alertEvent,alertCurve));
    }

    /*public static void getRoute(ListenerRequestRoute listener , LatLng origin, LatLng dest){
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //String key = "key=AIzaSyCJl2jz_kpq5w-MxQzIZd669o7nq_i1-TE";
        String parameters = str_origin + "&" + str_dest;
        String output = "json";
        String sUrl = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyD2DuLFQK0T-b7VgrMfMOJuFe1lvVvo0YM";
        apiRest.getRoute(listener, sUrl);
    }*/

    /*void processResponseRequest(ListenerRequestRoute listenerRequest, JSONObject response) {
        listenerRequest.requestCompleted(response);
    }

    void processErrorRequest(ListenerRequestRoute listenerRequest){
        listenerRequest.requestError();
    }*/


}
