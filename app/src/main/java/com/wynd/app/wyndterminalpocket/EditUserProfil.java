package com.wynd.app.wyndterminalpocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import org.apache.http.client.methods.HttpDelete;
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

public class EditUserProfil extends AppCompatActivity {

    private String userID, permissionID, restID, myuserID;
    private EditText username, password, email, permission, phone, rest_channel;
    private Button submit;
    private String Username, Password, Email, Permission, Phone, Rest_channel, message;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    protected CharSequence[] restaurants;

    protected ArrayList<CharSequence> selectedEntity = new ArrayList<CharSequence>();
    List<String> list = new ArrayList<String>();
    List<String> selectedItem;
    private Button selectEntityButton;
    private JSONObject channels = new JSONObject();
    private JSONArray restovspermission = new JSONArray();
    private Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profil);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                EditUserProfil.class));

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        deleteBtn = (Button) findViewById(R.id.delete);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        myuserID = pref.getString("myuserID", "");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //get user informations
        JsonObjectRequest userRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/user/get/info/"+userID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");
                            restovspermission = new JSONArray();
                            username.setText(response.isNull("username") ? "" : response.getString("username"));
                            password.setText(response.isNull("hash") ? "" : response.getString("hash"));
                            email.setText(response.isNull("email") ? "" : response.getString("email"));
                            phone.setText(response.isNull("phone") ? "" : response.getString("phone"));

                            JSONArray usersInResto = response.getJSONArray("usersInResto");
                            for(int i=0; i<usersInResto.length(); i++){
                                JSONObject infos = usersInResto.getJSONObject(i);
                                String permissionID = infos.isNull("permissionID") ? "" :  infos.getString("permissionID");
                                String restID = infos.isNull("resaturantChainID") ? "" :  infos.getString("resaturantChainID");

                                channels = new JSONObject();
                                channels.put("restid", restID);
                                channels.put("permission", permissionID);
                                restovspermission.put(channels);
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

        Volley.newRequestQueue(getApplicationContext()).add(userRequest);

        //on submit, the admin can update user's profil
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Username = username.getText().toString();
                Email = email.getText().toString();
                Password = password.getText().toString();
                Phone = phone.getText().toString();

                try {
                    Password = AeSimpleSHA1.SHA1(Password);
                    new EditTask().execute();

                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                  Log.e("Error sha1 ", e.toString());
                }

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(EditUserProfil.this);
                builder1.setMessage("Etes-vous sûr de vouloir supprimer cet utilisateur ?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                new DeleteTask().execute();

                            }
                        });

                builder1.setNegativeButton(
                        "Non",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
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
            String json = "";

            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http put method
                HttpPut httpPut = new HttpPut(Globales.baseUrl+"api/user/edit/"+userID);
                nameValuePairs.add(new BasicNameValuePair("new_username", Username));
                nameValuePairs.add(new BasicNameValuePair("new_secret", Password));
                nameValuePairs.add(new BasicNameValuePair("new_email", Email));
                nameValuePairs.add(new BasicNameValuePair("new_phone", Phone));
                nameValuePairs.add(new BasicNameValuePair("permission", Permission));
                nameValuePairs.add(new BasicNameValuePair("new_restovspermission", restovspermission.toString()));



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

                String result = finalResult.getString("result");
                if (!result.isEmpty() && result.equals("success")) {

                    Toast.makeText(getApplicationContext(), "Mise à jour effectuée", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditUserProfil.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }
    class MyDelete extends HttpPost{
        public MyDelete(String url){
            super(url);
        }
        @Override
        public String getMethod() {
            return "DELETE";
        }

    }
    private class DeleteTask extends AsyncTask<Void, Void, InputStream> {
        int i;
        String result = null;
        InputStream is = null;
        List<NameValuePair> nameValuePairs;
        JSONArray jsonArray = new JSONArray();


        protected InputStream doInBackground(Void... params) {

            //setting nameValuePairs
            nameValuePairs = new ArrayList<NameValuePair>(1);
            String json = "";

            try {

                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http put method
                MyDelete httpDelete = new MyDelete(Globales.baseUrl+"api/user/delete");
                nameValuePairs.add(new BasicNameValuePair("to_delete_user_id", userID));
                nameValuePairs.add(new BasicNameValuePair("delete_requester_id", myuserID));

                httpDelete.setHeader("Api-User", Globales.API_USER);
                httpDelete.setHeader("Api-Hash", Globales.API_HASH);

                httpDelete.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //getting the response
                HttpResponse response = httpClient.execute(httpDelete);

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

                String result = finalResult.getString("result");
                if (!result.isEmpty() && result.equals("success")) {

                    Toast.makeText(getApplicationContext(), "L'utilisateur a bien été supprimé", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditUserProfil.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                }
            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }



}
