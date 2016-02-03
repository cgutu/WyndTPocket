package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoOfRestaurant extends AppCompatActivity {

    private TextView Name, Email, Phone, Channel;
    private Button managers;
    private String restId, ID, savedRestId;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_of_restaurant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Name = (TextView) findViewById(R.id.name);
        Email = (TextView) findViewById(R.id.email);
        Phone = (TextView) findViewById(R.id.phone);
        Channel = (TextView) findViewById(R.id.channel);

      //  managers = (Button) findViewById(R.id.managers);

        Intent intent = getIntent();
        restId = intent.getStringExtra("restId");
        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        savedRestId = pref.getString("restId", "");

        if(restId == null){
            ID = savedRestId;
        }else{
            ID = restId;
        }

        //get info of clicked restaurant
        JsonObjectRequest getRestaurant = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/by/id/"+ID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");
                            System.out.println("response " + response);

                            Name.setText(response.isNull("name") ? "" : response.getString("name"));
                            Email.setText(response.isNull("email") ? "" : response.getString("email"));
                            Phone.setText(response.isNull("phone") ? "" : response.getString("phone"));
                            Channel.setText(response.isNull("channel") ? "" : response.getString("channel"));

                            editor = pref.edit();
                            editor.putString("restId", response.getString("id"));
                            editor.apply();

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

                System.out.println("api infos sent" + Globales.API_USER + " "+Globales.API_HASH);
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(getRestaurant);

//        managers.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), UsersActivity.class);
//                intent.putExtra("restId", restId);
//                startActivity(intent);
//            }
//        });

        editor = pref.edit();
        editor.putString("Check", "inforestaurant");
        editor.apply();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(InfoOfRestaurant.this, EditRestaurant.class);
                i.putExtra("restId", ID);
                startActivity(i);
            }
        });

    }

}