package com.wynd.app.wyndterminalpocket;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cgutu on 25/01/16.
 */
public class Globales extends Application{

    public static String baseUrl = "http://5.196.44.136/wyndTapi/";
    public static final String API_USER = "admin";
    public static final String hash="secret";
    public static final String API_TERMINAL= "terminal";
    public static String API_HASH = "";

    private static JSONArray EntityInfo = new JSONArray();
    private static RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public static JSONArray getInfo(String userID) {

        JsonObjectRequest rolesRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "api/user/get/info/" + userID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            response = response.getJSONObject("data");
                            System.out.println("response " + response);
                            EntityInfo = response.getJSONArray("usersInResto");
                            System.out.println("result " + EntityInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                System.out.println("api infos sent" + Globales.API_USER + " " + Globales.API_HASH);
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        mRequestQueue.add(rolesRequest);

        return EntityInfo;
    }
}
