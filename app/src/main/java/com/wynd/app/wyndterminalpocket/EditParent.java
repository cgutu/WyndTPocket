package com.wynd.app.wyndterminalpocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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

public class EditParent extends AppCompatActivity {

    private String myuserID, name, address, email, phone, parentID, status;
    private SharedPreferences pref;
    private JSONArray entities = new JSONArray();
    private TextView vName, vAddress, vEmail, vPhone;
    private Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_parent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                EditParent.class));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForm();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getApplicationContext(),
                Parents.class));
        pref = getApplicationContext().getSharedPreferences("Infos", 0);

        myuserID = pref.getString("myuserID", "");
        Intent i = getIntent();
        parentID = i.getStringExtra("parentID");
        status = i.getStringExtra("parentStatus");

        vName = (TextView) findViewById(R.id.name);
        vEmail = (TextView) findViewById(R.id.email);
        vPhone = (TextView) findViewById(R.id.phone);
        vAddress = (TextView) findViewById(R.id.address);
        deleteBtn = (Button) findViewById(R.id.delete);

        if(status.equals("0")){
            deleteBtn.setText("Activer");
        }else{
            deleteBtn.setText("Désactiver");
        }

        JsonObjectRequest entityRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/all/parents/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            for(int i=0; i<values.length();i++){
                                JSONObject parent = values.getJSONObject(i);
                                if(parent.getString("id").equals(parentID)){
                                    vName.setText(parent.getString("parent_name"));
                                    vEmail.setText(parent.getString("parent_email"));
                                    vPhone.setText(parent.getString("parent_phone"));
                                    vAddress.setText(parent.getString("parent_address"));
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
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(entityRequest);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(deleteBtn.getText().toString().equals("Activer")){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(EditParent.this);
                    builder1.setMessage("Etes-vous sûr de vouloir activer cette franchise ?");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Oui",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    new RetreiveTask().execute();

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
                }else{
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(EditParent.this);
                    builder1.setMessage("Etes-vous sûr de vouloir désactiver cette franchise ?");
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
            }
        });
    }

    private void checkForm(){
        name = vName.getText().toString();
        email = vEmail.getText().toString();
        phone = vPhone.getText().toString();
        address = vAddress.getText().toString();
        new EditTask().execute();
    }
    //edit task to do
    private class EditTask extends AsyncTask<Void, Void, InputStream> {
        int i;
        String result = null;
        InputStream is = null;
        List<NameValuePair> nameValuePairs;
        JSONArray jsonArray = new JSONArray();


        protected InputStream doInBackground(Void... params) {

            //setting nameValuePairs
            nameValuePairs = new ArrayList<NameValuePair>(1);
            JSONObject jsonObject = new JSONObject();
            String json = "";

            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http put method
                HttpPut httpPut = new HttpPut(Globales.baseUrl+"api/restaurant/edit/parent/"+myuserID);
                nameValuePairs.add(new BasicNameValuePair("parent_id", parentID));
                nameValuePairs.add(new BasicNameValuePair("new_res_name", name));
                nameValuePairs.add(new BasicNameValuePair("new_res_phone", phone));
                nameValuePairs.add(new BasicNameValuePair("new_res_address", address));
                nameValuePairs.add(new BasicNameValuePair("new_res_email", email));

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
                    Intent intent = new Intent(EditParent.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                }


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }
    //delete task
    class MyDelete extends HttpPost {
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
                MyDelete httpDelete = new MyDelete(Globales.baseUrl+"api/restaurant/parent/delete");
                nameValuePairs.add(new BasicNameValuePair("td_parent_id", parentID));
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

                    Toast.makeText(getApplicationContext(), "La franchise a bien été désactivée", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditParent.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                }


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }
    private class RetreiveTask extends AsyncTask<Void, Void, InputStream> {
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

                HttpPut httpPut = new HttpPut(Globales.baseUrl+"api/restaurant/parent/retrive");
                nameValuePairs.add(new BasicNameValuePair("td_parent_id", parentID));
                nameValuePairs.add(new BasicNameValuePair("delete_requester_id", myuserID));

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
                String msg = finalResult.getString("message");

                if (!result.isEmpty() && result.equals("success")) {

                    Toast.makeText(getApplicationContext(), "La franchise a bien été activée", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditParent.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                }


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
            }



        }
    }

}
