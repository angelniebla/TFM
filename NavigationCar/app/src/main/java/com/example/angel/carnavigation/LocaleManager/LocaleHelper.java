package com.example.angel.carnavigation.LocaleManager;

import android.content.Context;
import android.content.res.Configuration;

import com.example.angel.carnavigation.SharedPreference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper{

    public static Context onAttach(Context context) {
        new PreferenceManager().initPreference(context);
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {

        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        return updateResources(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {

        return PreferenceManager.getInstance().getLanguage();
    }

    private static void persist(Context context, String language) {

        PreferenceManager.getInstance().setLanguage(language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        //configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }
}