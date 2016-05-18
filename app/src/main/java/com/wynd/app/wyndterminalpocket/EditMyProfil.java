package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditMyProfil extends AppCompatActivity {

    private String myuserID;
    private EditText username, password, email, permission, phone, rest_channel;
    private Button submit;
    private String Username, Password, Email, Permission, Phone, Rest_channel, message;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private JSONArray restovspermission = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_profil);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                EditMyProfil.class));
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Intent intent = getIntent();
        myuserID = intent.getStringExtra("userID");

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        editor = pref.edit();
        editor.putString("Check", "editmonprofil");
        editor.apply();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * get user informations
         */
        JsonObjectRequest userRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/user/get/info/"+myuserID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");
                            username.setText(response.isNull("username") ? "" : response.getString("username"));
                            Password = (response.isNull("hash") ? "" : response.getString("hash"));
                            email.setText(response.isNull("email") ? "" : response.getString("email"));
                            phone.setText(response.isNull("phone") ? "" : response.getString("phone"));

                            JSONArray usersInResto = response.getJSONArray("usersInResto");
                            for(int i=0; i<usersInResto.length(); i++){
                                JSONObject info =new JSONObject();
                                info.put("restid", usersInResto.getJSONObject(i).getString("resaturantChainID"));
                                info.put("permission", usersInResto.getJSONObject(i).getString("permissionID"));
                                restovspermission.put(info);
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
                Map<String, String>  params = new HashMap<String, String>();

                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        //Volley.newRequestQueue(getApplicationContext()).add(userRequest);
        ApplicationController.getInstance().addToRequestQueue(userRequest, "userRequest");


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkForm();

            }
        });
    }
    private void checkForm(){

        username.setError(null);
        email.setError(null);
        phone.setError(null);

        boolean cancel = false;
        View focusView = null;

        Username = username.getText().toString();
        Email = email.getText().toString();
        Phone = phone.getText().toString();

        if (TextUtils.isEmpty(Username) ) {
            username.setError(getString(R.string.error_field_required));
            focusView = username;
            cancel = true;
        }else if (TextUtils.isEmpty(Email) ) {
            email.setError(getString(R.string.error_field_required));
            focusView = email;
            cancel = true;
        }else if (TextUtils.isEmpty(Phone) ) {
            phone.setError(getString(R.string.error_field_required));
            focusView = phone;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
             new EditTask().execute();
        }
    }
    private class EditTask extends AsyncTask<Void, Void, InputStream> {
        int i;
        String result = null;
        InputStream is = null;
        List<NameValuePair> nameValuePairs;

        protected InputStream doInBackground(Void... params) {

            //setting nameValuePairs
            nameValuePairs = new ArrayList<NameValuePair>(1);
            Log.i("INFOS", restovspermission.toString());

            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http put method
                HttpPut httpPut = new HttpPut(Globales.baseUrl+"api/user/edit/"+myuserID);
                nameValuePairs.add(new BasicNameValuePair("new_username", Username));
                nameValuePairs.add(new BasicNameValuePair("new_secret", Password));
                nameValuePairs.add(new BasicNameValuePair("new_email", Email));
                nameValuePairs.add(new BasicNameValuePair("new_phone", Phone));
                nameValuePairs.add(new BasicNameValuePair("new_restovspermission", restovspermission.toString()));

                httpPut.setHeader("Api-User", Globales.API_USER);
                httpPut.setHeader("Api-Hash", Globales.API_HASH);
                httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));

                //getting the response
                HttpResponse response = httpClient.execute(httpPut);

                //setting up the entity
                HttpEntity entity = response.getEntity();

                //setting up the content inside the input stream reader
                is = entity.getContent();

            } catch (Exception e) {
                Log.i("Error http put", "" + e.toString());
            }

            return is;
        }


        protected void onPreExecute() {

        }

        protected void onPostExecute(InputStream is) {

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder total = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    total.append(line + "\n");
                }
                is.close();
                String json = total.toString();
                JSONTokener tokener = new JSONTokener(json);
                JSONObject finalResult = new JSONObject(tokener);

                Log.i("PUT", "total "+total + " json "+json);
                String result = finalResult.getString("result");
                if (!result.isEmpty() && result.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Mise à jour effectuée", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditMyProfil.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    Toast.makeText(getApplicationContext(), "ERROR "+total, Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }

        }
    }
}

