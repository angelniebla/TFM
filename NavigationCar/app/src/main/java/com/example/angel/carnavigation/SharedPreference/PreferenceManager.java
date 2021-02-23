package com.example.angel.carnavigation.SharedPreference;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final String FIREBASE_USER_ID = "_userId";
    private final String FIREBASE_TOKEN_MESSAGING_ID = "_tokenId";
    private final String LANGUAGE = "_language";


    private String _userId;
    private String _tokenId;
    private String _language;

    private static PreferenceManager _instance;
    private SharedPreferences sharedPreference;

    public PreferenceManager() {
    }

    public void initPreference(Context ctx){
        if(_instance == null){
            _instance =  new PreferenceManager();
            _instance.sharedPreference = ctx.getSharedPreferences("PreferenceManager",Context.MODE_PRIVATE);
        }
    }

    public static PreferenceManager getInstance(){
        return _instance;
    }

    public String getUserId() {
        if(_userId == null){
            _userId = sharedPreference.getString(FIREBASE_USER_ID, "");
        }
        return _userId;
    }

    public void setUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(FIREBASE_USER_ID, userId );
        editor.commit();
        _userId = userId;
    }

    public String getTokenId() {
        if(_tokenId == null){
            _tokenId = sharedPreference.getString(FIREBASE_TOKEN_MESSAGING_ID, "");
        }
        return _tokenId;
    }

    public void setTokenId(String tokenId) {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(FIREBASE_TOKEN_MESSAGING_ID, tokenId );
        editor.commit();
        _tokenId = tokenId;
    }

    public String getLanguage() {
        if(_language == null){
            _language = sharedPreference.getString(LANGUAGE, "EN");
        }
        return _language;
    }

    public void setLanguage(String language) {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(LANGUAGE, language );
        editor.commit();
        _language = language;
    }

}
