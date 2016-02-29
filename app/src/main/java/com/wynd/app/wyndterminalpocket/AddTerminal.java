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
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddTerminal extends AppCompatActivity {

    private String restId;
    private String myuserID;
    private EditText vImei;
    private String imei;
    private SharedPreferences pref;
    private View mProgressView;
    private View mFormview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_terminal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                AddTerminal.class));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mFormview = findViewById(R.id.add_form);
        mProgressView = findViewById(R.id.login_progress);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        String username = pref.getString("username", "");
        myuserID = pref.getString("myuserID", "");

        Intent intent = getIntent();
        restId = intent.getStringExtra("restId");
        vImei = (EditText) findViewById(R.id.imei);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkForm();

            }
        });

    }
    private void checkForm(){

        vImei.setError(null);

        boolean cancel = false;
        View focusView = null;

        imei = vImei.getText().toString();

        if (TextUtils.isEmpty(imei) ) {
            vImei.setError(getString(R.string.error_field_required));
            focusView = vImei;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);

            new AddTerminalTask().execute();
        }
    }


    private class AddTerminalTask extends AsyncTask<Void, Void, InputStream> {
        int i;
        String result = null;
        InputStream is = null;
        List<NameValuePair> nameValuePairs;
        JSONArray jsonArray = new JSONArray();


        protected InputStream doInBackground(Void... params) {

            //setting nameValuePairs
            nameValuePairs = new ArrayList<NameValuePair>(1);
            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http post method
                HttpPost httpPost = new HttpPost(Globales.baseUrl+"api/terminal/add");

                nameValuePairs.add(new BasicNameValuePair("name", imei));
                nameValuePairs.add(new BasicNameValuePair("channelid", restId));
                nameValuePairs.add(new BasicNameValuePair("user_id", myuserID));


                httpPost.setHeader("Api-User", Globales.API_TERMINAL);
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
            } catch (IOException e) {
                Log.e("Log_tag", "IOException");
                e.printStackTrace();
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
                    Toast.makeText(getApplicationContext(), "Terminal ajouté", Toast.LENGTH_LONG).show();

                    showProgress(false);
                    Intent intent = new Intent(AddTerminal.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    showProgress(false);
                    vImei.setError(getString(R.string.error_invalid_user_pwd));
                    vImei.requestFocus();
                }

            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
                showProgress(false);
                vImei.setError(getString(R.string.error_connexion));
                vImei.requestFocus();
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
