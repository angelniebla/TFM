package com.example.angel.carnavigation.Volley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Utf8JsonRequest extends JsonRequest<JSONObject> {

    /**
     * Constructor Utf8JsonRequest
     *
     * @param method metodo usado en la consulta al servicio
     * @param url URL del servicio
     * @param requestBody cuerpo de la llamada al servicio
     * @param listener listener del JSONObject
     * @param errorListener errorListener del Response
     */
    public Utf8JsonRequest(int method, String url, String requestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    /**
     * Realizacion de la llamda HTTP
     *
     * @param response
     * @return Response<JSONObject>
     */
    @Override
    public Response<JSONObject> parseNetworkResponse (NetworkResponse response) {
        try {

            String utf8String = new String(response.data, "UTF-8");
            return Response.success(new JSONObject(utf8String), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));

        } catch (JSONException e) {
            return Response.error(new ParseError(e));

        }catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}