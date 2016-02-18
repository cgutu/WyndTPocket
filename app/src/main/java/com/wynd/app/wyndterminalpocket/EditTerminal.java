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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditTerminal extends AppCompatActivity {

    private String channel, uuid, id, channel_id, phone, myuserID, EntityInfo, restName, channelParent, selectedID;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private TextView vChannel;
    private Spinner parentSpinner, entitySpinner;
    private JSONArray entities = new JSONArray();
    private ArrayAdapter<String> dataAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_terminal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        id = intent.getStringExtra("terminalID");
        uuid = intent.getStringExtra("terminalUuid");
        channel_id = intent.getStringExtra("channelID");

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        myuserID = pref.getString("myuserID", "");
        EntityInfo = pref.getString("EntityInfo", "");

        entitySpinner = (Spinner) findViewById(R.id.entity);
        TextView vUUID = (TextView) findViewById(R.id.uuid);
        vUUID.setText(uuid);

        //get restaurant by channelid
        JsonObjectRequest getRestaurant = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/all/chains/"+myuserID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            System.out.println("response " + response);

                            for(int i=0; i<values.length(); i++){
                                JSONObject entity = values.getJSONObject(i);
                                entities.put(entity);
                            }
                            addList(entities);

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

                checkForm();

            }
        });
    }

    private void checkForm()
    {
        channel_id = selectedID;


        if (channel_id.equals("") ) {
           Toast.makeText(getApplicationContext(), "Veuillez séléctionner une restaurant ", Toast.LENGTH_LONG).show();
        }else{
            new EditTask().execute();
        }


    }

    private void addList(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Select a restaurant");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("name");
                list.add(name);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("list " + "listist.size() : " + list.size());

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entitySpinner.setAdapter(dataAdapter);

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("name");
                if(jsonArray.getJSONObject(i).getString("id").equals(channel_id)){
                    entitySpinner.setSelection(list.indexOf(name));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        entitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){
                    selectedID = "";
                }else{
                    if (item != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                String name = jsonArray.getJSONObject(i).getString("name");
                                if(item.equals(name)){

                                    //get selected channel
                                    selectedID = jsonArray.getJSONObject(i).getString("id");

                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

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
                HttpPut httpPut = new HttpPut(Globales.baseUrl+"api/terminal/edit");
                nameValuePairs.add(new BasicNameValuePair("t_id", id));
                nameValuePairs.add(new BasicNameValuePair("new_channelid", channel_id));
                nameValuePairs.add(new BasicNameValuePair("user_id", myuserID));


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

                    Toast.makeText(getApplicationContext(), "Modification effectuée", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditTerminal.this, Terminals.class);
                    startActivity(intent);
                    finish();

                }


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }

}
