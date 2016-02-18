package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRestaurant extends AppCompatActivity {

    private String name, email, phone, channel, myuserID, restId, parent_id, address;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private TextView vName, vEmail, vPhone, vChannel, vAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_restaurant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        String username = pref.getString("username", "");
        myuserID = pref.getString("myuserID", "");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vName = (TextView) findViewById(R.id.name);
        vEmail = (TextView) findViewById(R.id.email);
        vPhone = (TextView) findViewById(R.id.phone);
        vAddress = (TextView) findViewById(R.id.address);
        vChannel = (TextView) findViewById(R.id.restchannel);

        Intent intent = getIntent();
        restId = intent.getStringExtra("restId");

        //get info of clicked restaurant
        JsonObjectRequest getRestaurant = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/by/id/"+restId, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");
                            System.out.println("response " + response);

                            vName.setText(response.isNull("name") ? "" : response.getString("name"));
                            vEmail.setText(response.isNull("email") ? "" : response.getString("email"));
                            vPhone.setText(response.isNull("phone") ? "" : response.getString("phone"));
                            vChannel.setText(response.isNull("channel") ? "" : response.getString("channel"));
                            vAddress.setText(response.isNull("address") ? "" : response.getString("address"));
                            parent_id = response.isNull("ChannelParentID") ? "" : response.getString("ChannelParentID");

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = vName.getText().toString();
                email = vEmail.getText().toString();
                phone = vPhone.getText().toString();
                channel = vChannel.getText().toString();
                address = vAddress.getText().toString();

                new EditTask().execute();
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
            System.out.println("do in background edit task ");
            JSONObject jsonObject = new JSONObject();
            String json = "";

            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http put method
                HttpPut httpPut = new HttpPut(Globales.baseUrl+"api/restaurant/edit/"+myuserID);
                nameValuePairs.add(new BasicNameValuePair("res_id", restId));
                nameValuePairs.add(new BasicNameValuePair("new_res_name", name));
                nameValuePairs.add(new BasicNameValuePair("new_res_phone", phone));
                nameValuePairs.add(new BasicNameValuePair("new_res_address", address));
                nameValuePairs.add(new BasicNameValuePair("new_res_email", email));
                nameValuePairs.add(new BasicNameValuePair("new_res_parent_id", parent_id));
                nameValuePairs.add(new BasicNameValuePair("new_channel_name", channel));

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

                    Toast.makeText(getApplicationContext(), "Mise à jour effectuée", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditRestaurant.this, InfoOfRestaurant.class);
                    startActivity(intent);
                    finish();

                }


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }
}
