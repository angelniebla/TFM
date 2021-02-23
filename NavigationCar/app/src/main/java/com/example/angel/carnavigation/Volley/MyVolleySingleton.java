package com.example.angel.carnavigation.Volley;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MyVolleySingleton {
    private static MyVolleySingleton singleton;
    private RequestQueue requestQueue;

    private MyVolleySingleton(Context context) {
        requestQueue = getRequestQueue(context);
    }

    /**
     * Obtener una instancia de MyVolleySingleton si ya esta creada, si no se crea
     * @return MyVolleySingleton
     */
    public static synchronized MyVolleySingleton getInstance(Context context) {
        if (singleton == null) {
            singleton = new MyVolleySingleton(context);
        }
        return singleton;
    }
    /**
     * Comprobamos que el objecto ya existe y si no lo creamos
     *
     * @param context contexto de la applicacion
     * @return una referencia a RequestQueue
     */
    public RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }
    /**
     * Toma un tipo Request y lo añade a la cola de solicitudes
     *
     * @param req la Request que se quiere añadir a la cola
     * @return el contexto de la aplicacion
     */

    public  void addToRequestQueue(Request req, Context context) {
        getRequestQueue(context).add(req);
    }
}
