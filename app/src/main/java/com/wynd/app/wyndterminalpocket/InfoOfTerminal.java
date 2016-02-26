package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class InfoOfTerminal extends AppCompatActivity {

    private String channel, uuid, id, channel_id, phone, EntityInfo, status;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private JSONArray infosArray = new JSONArray();
    private TextView vUuid, vRestaurant, vPhone, vChannel, vEmail, vUser, vApk, vStatus;
    private JSONObject terminal = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_of_terminal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                InfoOfTerminal.class));

        vUuid = (TextView) findViewById(R.id.uuid);
        vPhone = (TextView) findViewById(R.id.phone);
        vRestaurant = (TextView) findViewById(R.id.restaurantname);
        vChannel = (TextView) findViewById(R.id.channel);
        vEmail = (TextView) findViewById(R.id.email);
        vApk = (TextView) findViewById(R.id.apk);
        vUser = (TextView) findViewById(R.id.user);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uuid = intent.getStringExtra("terminalUuid");
        id = intent.getStringExtra("terminalID");
        channel_id = intent.getStringExtra("channelID");

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        EntityInfo = pref.getString("EntityInfo", "");

        String savedChannelID = pref.getString("channelID","");
        String savedTerminalID = pref.getString("terminalID","");
        String savedUUID = pref.getString("terminalUuid","");

        if(channel_id == null){
            channel_id = savedChannelID;
        }
        if(uuid == null){
            uuid = savedUUID;
        }
        if(id == null){
            id = savedTerminalID;
        }
        try {
            infosArray = new JSONArray(EntityInfo);
            for (int j = 0; j < infosArray.length(); j++) {
                JSONObject infoObject = infosArray.getJSONObject(j);
                String permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");

                if (permission.equals("3")) {
                    //I can edit terminal
                    fab.setVisibility(View.VISIBLE);
                } else {
                    //I can not edit terminal
                    fab.setVisibility(View.GONE);
                }

            }
        }catch (JSONException e){

        }

        //get info of clicked terminal
        JsonObjectRequest getTerminal = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/info/by/"+id, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray terminals = response.getJSONArray("data");
                            for(int i =0; i<terminals.length(); i++){
                                JSONObject id = terminals.getJSONObject(i);
                                    vUuid.setText(uuid);
                                    vChannel.setText(id.getString("channelName"));

                                    status = id.getString("terminalActive");

                                    String terminalInfo = id.isNull("terminalInfo") ? "" : id.getString("terminalInfo");
                                    if(!terminalInfo.isEmpty()){
                                        JSONObject infoObject = new JSONObject(terminalInfo);
                                        vPhone.setText(infoObject.getString("phone"));
                                        vRestaurant.setText(infoObject.getString("entity_label"));
                                        vEmail.setText(infoObject.getString("email"));
                                        vApk.setText(infoObject.getString("apk_version"));
                                        vUser.setText(infoObject.getString("username"));
                                    }


                            }
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
                params.put("Api-User", Globales.API_TERMINAL);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(getTerminal);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor = pref.edit();
                editor.putString("channelID", channel_id);
                editor.putString("terminalUuid", uuid);
                editor.putString("terminalID", id);
                editor.apply();

                Intent i = new Intent(InfoOfTerminal.this, EditTerminal.class);
                i.putExtra("terminalID", id);
                i.putExtra("terminalUuid", uuid);
                i.putExtra("channelID", channel_id);
                i.putExtra("status", status);
                startActivity(i);
            }
        });


    }

}
