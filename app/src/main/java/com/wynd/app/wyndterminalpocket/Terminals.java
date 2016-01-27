package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Terminals extends AppCompatActivity {

    private JSONArray terminals = new JSONArray();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String userID, parentID, permission, rest_channel, ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminals);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //if permission is CHAIN_ADMIN
        pref = getApplicationContext().getSharedPreferences("Infos", 0);

        userID = pref.getString("userID", "");
        parentID = pref.getString("parentID", "");
        permission = pref.getString("roles", "");
        rest_channel = pref.getString("rest_channel", "");

        editor = pref.edit();
        editor.putString("Check", "exitterminals");
        editor.apply();


        if(!permission.isEmpty() && permission.equals("CHAIN_ADMIN")){
            //get all restaurants
            JsonObjectRequest terminalRequest = new JsonObjectRequest
                    (Request.Method.GET, Globales.baseUrl+"api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                JSONArray values = response.getJSONArray("data");
                                System.out.println("response "+response);

                                for (int i = 0; i < values.length(); i++) {

                                    JSONObject restaurants = values.getJSONObject(i);
                                    terminals.put(restaurants);

                                }
                                System.out.println("terminals " + terminals);


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
                    Map<String, String>  params = new HashMap<String, String>();

                    System.out.println("api infos sent" + Globales.API_TERMINAL + " "+Globales.API_HASH);
                    params.put("Api-User", Globales.API_TERMINAL);
                    params.put("Api-Hash", Globales.API_HASH);

                    return params;
                }
            };

            Volley.newRequestQueue(getApplicationContext()).add(terminalRequest);
        }

    }

}
