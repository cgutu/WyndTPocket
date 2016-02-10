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

    private String userID;
    private EditText username, password, email, permission, phone, rest_channel;
    private Button submit;
    private String Username, Password, Email, Permission, Phone, Rest_channel, message;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_profil);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = (EditText) findViewById(R.id.username);
       // password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
//        permission = (EditText) findViewById(R.id.permission);
//        rest_channel = (EditText) findViewById(R.id.restchannel);
       // submit = (Button) findViewById(R.id.submit);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        System.out.println("userID " + userID);

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        editor = pref.edit();
        editor.putString("Check", "editmonprofil");
        editor.apply();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //get user informations
        JsonObjectRequest userRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/user/get/info/"+userID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");
                            System.out.println("response " + response);

                            username.setText(response.isNull("username") ? "" : response.getString("username"));
                           // password.setText(response.isNull("hash") ? "" : response.getString("hash"));
                            Password = (response.isNull("hash") ? "" : response.getString("hash"));
                            Permission = response.isNull("permission") ? "" : response.getString("permission");
                            if(Permission.equals("ADMIN")){
                                Permission = "2";
                            }else if(Permission.equals("USER")){
                                Permission = "1";
                            }else if(Permission.equals("SUPER_ADMIN")){
                                Permission = "3";
                        }
                            email.setText(response.isNull("email") ? "" : response.getString("email"));
                            phone.setText(response.isNull("phone") ? "" : response.getString("phone"));
                            Rest_channel = response.isNull("rest_channel") ? "" : response.getString("rest_channel");



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

        Volley.newRequestQueue(getApplicationContext()).add(userRequest);

        //on submit, the admin can update user's profil
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
        //Password = password.getText().toString();
        Phone = phone.getText().toString();

        //                try {
//                    Password = AeSimpleSHA1.SHA1(Password);
//                    System.out.println("SHA1 user password " + Password);
//                    new EditTask().execute();
//
//                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
//                    System.out.println("Error sha1 " + e);
//                }

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
        JSONArray jsonArray = new JSONArray();


        protected InputStream doInBackground(Void... params) {

            //setting nameValuePairs
            nameValuePairs = new ArrayList<NameValuePair>(1);
            System.out.println("do in background edit task " + Password);
            JSONObject jsonObject = new JSONObject();
            String json = "";

            try {
 //               jsonObject.put("username", Username);
//                jsonObject.put("secret", Password);
//                jsonObject.put("email", Email);
//                jsonObject.put("phone", Phone);
//                jsonObject.put("permission",Permission);
//                jsonObject.put("rest_channel_id",Rest_channel);
//
//                json = jsonObject.toString();

                System.out.println("submit "+Username+Email+Password+Phone+Permission+Rest_channel);
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http put method
                HttpPut httpPut = new HttpPut(Globales.baseUrl+"api/user/edit/"+userID);
                nameValuePairs.add(new BasicNameValuePair("username", Username));
                nameValuePairs.add(new BasicNameValuePair("secret", Password));
                nameValuePairs.add(new BasicNameValuePair("email", Email));
                nameValuePairs.add(new BasicNameValuePair("phone", Phone));
                nameValuePairs.add(new BasicNameValuePair("permission", Permission));
                nameValuePairs.add(new BasicNameValuePair("rest_channel_id", Rest_channel));


                //StringEntity se = new StringEntity(json);
                //se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

//                httpPut.addHeader("content-type", "application/x-www-form-urlencoded");
//                httpPut.setHeader("Accept", "application/json");

                httpPut.setHeader("Api-User", Globales.API_USER);
                httpPut.setHeader("Api-Hash", Globales.API_HASH);
                // httpPut.setEntity(se);
                httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //getting the response
                HttpResponse response = httpClient.execute(httpPut);

                //setting up the entity
                HttpEntity entity = response.getEntity();

                //setting up the content inside the input stream reader
                is = entity.getContent();
                System.out.println("is "+is);

            } catch (Exception e) {
                System.out.println("Error http put "+e.toString() + e.getLocalizedMessage());
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
                System.out.println("total: " + json);
                JSONTokener tokener = new JSONTokener(json);
                JSONObject finalResult = new JSONObject(tokener);

                int i = 0;
                System.out.println("result: " + finalResult);
                String result = finalResult.getString("result");
                String msg = finalResult.getString("message");
                System.out.println("result: " + result + " message: "+msg);

                if (!result.isEmpty() && result.equals("success")) {
                    JSONObject jsonObject = finalResult.getJSONObject("data");
                    System.out.println("result " + jsonObject);

                    Toast.makeText(getApplicationContext(), "Mise à jour effectuée", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditMyProfil.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                }


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }
}

