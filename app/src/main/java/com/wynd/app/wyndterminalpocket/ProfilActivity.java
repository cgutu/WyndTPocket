package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ProfilActivity extends AppCompatActivity {

    private JSONObject user = new JSONObject();
    private String id, username, email, hash, phone, permission, rest_channel, userID, restId, savedUserID, ID;
    private TextView vUsername, vEmail, vPhone, vPermission, vRest_channel;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ListView mListView;
    private List<String> listItems = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profil);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vUsername = (TextView) findViewById(R.id.username);
        vEmail = (TextView) findViewById(R.id.email);
        vPhone = (TextView) findViewById(R.id.phone);
        mListView = (ListView) findViewById(R.id.listView);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        restId = intent.getStringExtra("restId");
        System.out.println("userID" + userID + "restId "+restId);

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        System.out.println("rest id test " + pref.getString("userID", ""));
        savedUserID = pref.getString("userID", "");

        if(userID == null){
            ID = savedUserID;
        }else{
            ID = userID;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent i = new Intent(ProfilActivity.this, EditUserProfil.class);
                i.putExtra("userID", ID);
                startActivity(i);
            }
        });

        //get user profil
        JsonObjectRequest userProfil = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/user/get/info/"+ID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");
                            System.out.println("response " + response);

                            id = response.getString("id");
                            username = response.getString("username");
                            email = response.getString("email");
                            phone = response.getString("phone");
                            JSONArray userResto = response.getJSONArray("usersInResto");
                            for(int i=0; i<userResto.length(); i++){
                                JSONObject userRestInfo = userResto.getJSONObject(i);
                                permission = userRestInfo.isNull("permissionID") ? "" : userRestInfo.getString("permissionID");
                                rest_channel = userRestInfo.isNull("resaturantChainID") ? "" : userRestInfo.getString("resaturantChainID");

                                if(!permission.isEmpty() && permission.equalsIgnoreCase("1")){
                                    permission = "USER";
                                }else if(!permission.isEmpty() && permission.equalsIgnoreCase("2")){
                                    permission = "ADMIN";
                                }else if(!permission.isEmpty() && permission.equalsIgnoreCase("3")){
                                    permission = "SUPER_ADMIN";
                                }
                                //set listview

                                if(!rest_channel.isEmpty()) {
                                    final JsonObjectRequest restaurantRequest = new JsonObjectRequest
                                            (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/by/id/" + rest_channel, null, new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {

                                                    try {
                                                        response = response.getJSONObject("data");
                                                        System.out.println("response " + response);


                                                        String restaurantName = response.isNull("name") ? "" : response.getString("name");
                                                        listItems.add(permission + " <----> " + restaurantName);
                                                        addItem(listItems);

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

                                    Volley.newRequestQueue(getApplicationContext()).add(restaurantRequest);
                                }
                            }
                            System.out.println("userrestinfo " + permission + " " + rest_channel);


                            vUsername.setText(username);
                            vEmail.setText(email);
                            vPhone.setText(phone);

                            editor = pref.edit();
                            editor.putString("userID", id);
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

        Volley.newRequestQueue(getApplicationContext()).add(userProfil);


    }
    private void addItem(List<String> listItems){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        mListView.setAdapter(adapter);
    }

}
