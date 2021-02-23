package com.example.angel.carnavigation.Volley;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.angel.carnavigation.Listeners.ListenerRequest;
import com.example.angel.carnavigation.Model.ConfigurationCar;
import com.example.angel.carnavigation.Model.CredentialsCar;
import com.example.angel.carnavigation.Model.LocationCar;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class APIRestVolley {

    private static APIRestVolley apiRest;

    /**
     * Obtener una instance de APIRestVolley si ya esta creada, si no se crea
     *
     * @return APIRestVolley
     */
    public APIRestVolley getInstance() {
        if (apiRest == null) {
            apiRest = new APIRestVolley();
        }
        return apiRest;
    }

    /**
     * Creamos la Utf8JsonRequest pasandole el metodo, la URL, el cuerpo, el listener y el error listener
     * <p>
     *  @param sUrl URL creada en por el controlador
     *  @param cont Instancia del ControllerVolley para devolver los resultados
     */
    public void postLocation(final ListenerRequest listenerRequest, String url, LocationCar locationCar) {
        try {
            JSONObject jsonObject = new JSONObject(new Gson().toJson(locationCar));
            Utf8JsonRequest request = new Utf8JsonRequest(
                    Request.Method.POST,
                    url,
                    jsonObject.toString(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("REQUEST", "Response: " + response.toString(4));
                            } catch (JSONException ex) {
                                Log.e("REQUEST", ex.getMessage() != null ? ex.getMessage() : "JSONException");
                            }
                            processRequestResponse(response, listenerRequest);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("REQUEST", "Error Volley: " + error.toString());
                            if(error.networkResponse != null){
                                processRequestError(listenerRequest, error.networkResponse.statusCode);
                            }
                            else{
                                processRequestError(listenerRequest);
                            }

                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            MyVolleySingleton.
                    getInstance(listenerRequest.getContext()).
                    addToRequestQueue(request, listenerRequest.getContext());

        } catch (Exception e) {
            Log.e("REQUEST", "Error JSON: " + e.toString());
            processRequestError(listenerRequest);
        }
    }

    private void processRequestResponse(JSONObject response, ListenerRequest listenerRequest) {
        ControllerVolley.getInstance().processResponseRequest(listenerRequest);
    }

    private void processRequestError(ListenerRequest listenerRequest) {
        ControllerVolley.getInstance().processErrorRequest(listenerRequest, 500);
    }

    private void processRequestError(ListenerRequest listenerRequest, int statusCode) {
        ControllerVolley.getInstance().processErrorRequest(listenerRequest, statusCode);
    }

    public void postCredentials(final ListenerRequest listenerRequest, String url, CredentialsCar credentials) {
        try {
            JSONObject jsonObject = new JSONObject(new Gson().toJson(credentials));
            Utf8JsonRequest request = new Utf8JsonRequest(
                    Request.Method.POST,
                    url,
                    jsonObject.toString(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("REQUEST", "Response: " + response.toString(4));
                            } catch (JSONException ex) {
                                Log.e("REQUEST", ex.getMessage() != null ? ex.getMessage() : "JSONException");
                            }
                            processRequestResponse(response, listenerRequest);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("REQUEST", "Error Volley: " + error.toString());
                            if(error.networkResponse != null){
                                processRequestError(listenerRequest, error.networkResponse.statusCode);
                            }
                            else{
                                processRequestError(listenerRequest);
                            }
                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            request.setShouldCache(false);
            int minuteTimeOut = 60000;
            request.setRetryPolicy(new DefaultRetryPolicy(
                    minuteTimeOut,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MyVolleySingleton.
                    getInstance(listenerRequest.getContext()).
                    addToRequestQueue(request, listenerRequest.getContext());

        } catch (Exception e) {
            Log.e("REQUEST", "Error JSON: " + e.toString());
            processRequestError(listenerRequest);
        }
    }

    public void postConfiguration(final ListenerRequest listenerRequest, String url, ConfigurationCar configuration) {
        try {
            JSONObject jsonObject = new JSONObject(new Gson().toJson(configuration));
            Utf8JsonRequest request = new Utf8JsonRequest(
                    Request.Method.POST,
                    url,
                    jsonObject.toString(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("REQUEST", "Response: " + response.toString(4));
                            } catch (JSONException ex) {
                                Log.e("REQUEST", ex.getMessage() != null ? ex.getMessage() : "JSONException");
                            }
                            processRequestResponse(response, listenerRequest);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("REQUEST", "Error Volley: " + error.toString());
                            if(error.networkResponse != null){
                                processRequestError(listenerRequest, error.networkResponse.statusCode);
                            }
                            else{
                                processRequestError(listenerRequest);
                            }
                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            request.setShouldCache(false);
            int minuteTimeOut = 60000;
            request.setRetryPolicy(new DefaultRetryPolicy(
                    minuteTimeOut,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MyVolleySingleton.
                    getInstance(listenerRequest.getContext()).
                    addToRequestQueue(request, listenerRequest.getContext());

        } catch (Exception e) {
            Log.e("REQUEST-MM", "Error JSON: " + e.toString());
            processRequestError(listenerRequest);
        }
    }

    /*public void getRoute (final ListenerRequestRoute listenerRequest, String sUrl) {
        try {
            Utf8JsonRequest request = new Utf8JsonRequest(
                    com.android.volley.Request.Method.GET, sUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Respuesta Volley:", response != null ? response.toString() : "Se ha obtenido una respuesta vacia o una respuesta erronea");
                            processRequestResponse(response,listenerRequest);
                        }

                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error:", error != null && error.getMessage() != null ? error.getMessage() : "No se ha obtenido respuesta de error");
                            processRequestError(listenerRequest);
                        }
                    }
            );

            // Añadir petición a la cola
            MyVolleySingleton.
                    getInstance(listenerRequest.getContext()).
                    addToRequestQueue(request, listenerRequest.getContext());        } catch (JsonIOException e) {
            processRequestError(listenerRequest);
        }
    }*/

    /*private void processRequestResponse(JSONObject response, ListenerRequestRoute listenerRequest) {
        ControllerVolley.getInstance().processResponseRequest(listenerRequest, response);
    }

    private void processRequestError(ListenerRequestRoute listenerRequest) {
        ControllerVolley.getInstance().processErrorRequest(listenerRequest);
    }*/
}