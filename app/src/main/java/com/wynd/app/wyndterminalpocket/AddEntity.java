package com.wynd.app.wyndterminalpocket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddEntity extends AppCompatActivity {

    private View mProgressView;
    private View mFormview;
    private EditText vName, vEmail, vPhone, vChannel, vAddress;
    private String email, name, phone, channel, address, parent, userID, message;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String userInfo, parentID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mFormview = findViewById(R.id.add_form);
        mProgressView = findViewById(R.id.login_progress);
        vName = (EditText) findViewById(R.id.name);
        vEmail = (EditText) findViewById(R.id.email);
        vChannel = (EditText) findViewById(R.id.channel);
        vPhone = (EditText) findViewById(R.id.phone);
        vAddress = (EditText) findViewById(R.id.address);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkForm();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        userID = pref.getString("myuserID", "");

        Intent i = getIntent();
        parentID = i.getStringExtra("parentID");

        System.out.println("parentID "+parentID);


    }
    private void checkForm(){

        vName.setError(null);
        vEmail.setError(null);
        vPhone.setError(null);
        vAddress.setError(null);
        vChannel.setError(null);

        boolean cancel = false;
        View focusView = null;

        name = vName.getText().toString();
        email = vEmail.getText().toString();
        phone = vPhone.getText().toString();
        address = vAddress.getText().toString();
        channel = vChannel.getText().toString();


        if (TextUtils.isEmpty(name) ) {
            vName.setError(getString(R.string.error_field_required));
            focusView = vName;
            cancel = true;
        }else if (TextUtils.isEmpty(email) ) {
            vEmail.setError(getString(R.string.error_field_required));
            focusView = vEmail;
            cancel = true;
        }else if (TextUtils.isEmpty(phone) ) {
            vPhone.setError(getString(R.string.error_field_required));
            focusView = vPhone;
            cancel = true;
        }else if (TextUtils.isEmpty(address) ) {
            vAddress.setError(getString(R.string.error_field_required));
            focusView = vAddress;
            cancel = true;
        }else if (TextUtils.isEmpty(channel) ) {
            vChannel.setError(getString(R.string.error_field_required));
            focusView = vChannel;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);

            new AddEntityTask().execute();
        }
    }

    private class AddEntityTask extends AsyncTask<Void, Void, InputStream> {
        int i;
        String result = null;
        InputStream is = null;
        List<NameValuePair> nameValuePairs;
        JSONArray jsonArray = new JSONArray();


        protected InputStream doInBackground(Void... params) {

            //setting nameValuePairs
            nameValuePairs = new ArrayList<NameValuePair>(1);
            System.out.println("do in background add entity task "+name+email+phone+address+channel+parent+userID);


            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http post method
                HttpPost httpPost = new HttpPost(Globales.baseUrl+"api/restaurant/add");

                nameValuePairs.add(new BasicNameValuePair("res_name", name));
                nameValuePairs.add(new BasicNameValuePair("res_email", email));
                nameValuePairs.add(new BasicNameValuePair("res_address", address));
                nameValuePairs.add(new BasicNameValuePair("res_phone", phone));
                nameValuePairs.add(new BasicNameValuePair("res_parent_id", parentID));
                nameValuePairs.add(new BasicNameValuePair("rest_channel", channel));
                nameValuePairs.add(new BasicNameValuePair("user_id", userID));

                httpPost.setHeader("Api-User", Globales.API_USER);
                httpPost.setHeader("Api-Hash", Globales.API_HASH);
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //getting the response
                HttpResponse response = httpClient.execute(httpPost);

                //setting up the entity
                HttpEntity entity = response.getEntity();

                //setting up the content inside the input stream reader
                is = entity.getContent();

            } catch (ClientProtocolException e) {

                Log.e("ClientProtocole", "Log_tag");
                String msg = "Erreur client protocole";
                message = "Erreur client protocole";


            } catch (IOException e) {
                Log.e("Log_tag", "IOException");
                e.printStackTrace();
                String msg = "Erreur IOException";
                message = "Erreur IOException";

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

                int i = 0;

                String result = finalResult.getString("result");
                System.out.println("result " + result);

                if (!result.isEmpty() && result.equals("success")) {
                    JSONObject jsonObject = finalResult.getJSONObject("data");
                    System.out.println("data " + jsonObject);

                    Toast.makeText(getApplicationContext(), "Entité ajouté", Toast.LENGTH_LONG).show();

                    showProgress(false);
                    Intent intent = new Intent(AddEntity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    showProgress(false);
                    vName.setError("Impossible d'ajouter cette entité");
                    vName.requestFocus();
                }

                System.out.println("result " + result);


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
                showProgress(false);
                vName.setError(getString(R.string.error_connexion));
                vName.requestFocus();
            }



        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFormview.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormview.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormview.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mFormview.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
