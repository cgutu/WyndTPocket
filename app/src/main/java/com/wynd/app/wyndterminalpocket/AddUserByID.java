package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 03/02/16.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddUserByID extends AppCompatActivity {

    private String restId;
    private String message;
    private EditText mUsernameView, mEmailView, mPasswordView, mPhoneView;
    private String email, username, phone, password, permission, savedRestId, ID, myuserID, role;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private View mProgressView;
    private View mFormview;
    private JSONArray chains = new JSONArray();

    protected CharSequence[] colours = { "Red", "Green", "Blue", "Yellow", "Orange", "Purple" };
    protected CharSequence[] restaurants;
    protected CharSequence[] permissions = { "ADMIN", "USER" };

    protected ArrayList<CharSequence> selectedEntity = new ArrayList<CharSequence>();
    protected ArrayList<CharSequence> selectedPermission = new ArrayList<CharSequence>();
    private Button selectEntityButton, selectPermissionBtn;
    private JSONArray names = new JSONArray();
    List<String> list = new ArrayList<String>();
    List<String> selectedItem, selectedItemPermission;
    JSONObject channels;
    JSONArray itemsArray;
    private JSONArray EntityInfo = new JSONArray();
    private JSONArray infosArray = new JSONArray();
    private String entityInfo, restID;
    private TextView addText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_userbyid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                AddUserByID.class));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mFormview = findViewById(R.id.add_form);
        mProgressView = findViewById(R.id.login_progress);
        addText = (TextView) findViewById(R.id.addtext);

        Intent intent = getIntent();

        restId = intent.getStringExtra("restId");

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        System.out.println("rest id test " + pref.getString("restId", ""));
        savedRestId = pref.getString("restId", "");
        myuserID = pref.getString("myuserID", "");
        role = pref.getString("ROLE", "");
        entityInfo = pref.getString("EntityInfo", "");

        if(restId == null){
            ID = savedRestId;
        }else{
            ID = restId;
        }

        System.out.println("adduser for " + restId+ " or "+ID);

        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mEmailView = (EditText) findViewById(R.id.email);
        mPhoneView = (EditText) findViewById(R.id.phone);


        // btnSubmit = (Button) findViewById(R.id.submit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkForm();

            }
        });
        try {
            infosArray = new JSONArray(entityInfo);
            for (int j = 0; j < infosArray.length(); j++) {
                JSONObject infoObject = infosArray.getJSONObject(j);
                permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");

                if (permission.equals("3")) {
                    //I can only create administators
                    addText.setText("Créer un administrateur");
                } else if (permission.equals("2")) {
                    //I can only create users
                    addText.setText("Créer un utilisateur");
                }

            }
        }catch (JSONException e){

        }

    }


    private void checkForm(){

        mUsernameView.setError(null);
        mPasswordView.setError(null);
        mEmailView.setError(null);
        mPhoneView.setError(null);

        boolean cancel = false;
        View focusView = null;

        username = mUsernameView.getText().toString();
        email = mEmailView.getText().toString();
        phone = mPhoneView.getText().toString();
        password = mPasswordView.getText().toString();

        try{
            itemsArray = new JSONArray();
            channels = new JSONObject();
            channels.put("restid", ID);

            try{
                infosArray = new JSONArray(entityInfo);
                for (int j = 0; j < infosArray.length(); j++) {
                    JSONObject infoObject = infosArray.getJSONObject(j);
                    permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                    restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");

                    if(permission.equals("3")){
                        //I can only create administators
                        addText.setText("Créer un administrateur");
                        channels.put("permission", "2");
                    }else if(permission.equals("2")){
                        //I can only create users
                        addText.setText("Créer un utilisateur");
                        channels.put("permission", "1");
                    }

                }
                itemsArray.put(channels);

            }catch(JSONException e){

            }

        }catch (JSONException e){

        }

        System.out.println("itemsArray" + itemsArray);


        if (TextUtils.isEmpty(username) ) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }else if (TextUtils.isEmpty(password) ) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }else if (TextUtils.isEmpty(email) ) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }else if (TextUtils.isEmpty(phone) ) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);

            new AddUserTask().execute();
        }
    }

    private class AddUserTask extends AsyncTask<Void, Void, InputStream> {
        int i;
        String result = null;
        InputStream is = null;
        List<NameValuePair> nameValuePairs;
        JSONArray jsonArray = new JSONArray();


        protected InputStream doInBackground(Void... params) {

            //setting nameValuePairs
            nameValuePairs = new ArrayList<NameValuePair>(1);
            System.out.println("selected item "+channels);
            System.out.println("do in background adduser task "+ID +" "+username+" "+password+" "+email+"");


            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http post method
                HttpPost httpPost = new HttpPost(Globales.baseUrl+"api/user/add");

                nameValuePairs.add(new BasicNameValuePair("new_username", username));
                nameValuePairs.add(new BasicNameValuePair("new_secret", password));
                nameValuePairs.add(new BasicNameValuePair("new_email", email));
                nameValuePairs.add(new BasicNameValuePair("new_phone", phone));
                nameValuePairs.add(new BasicNameValuePair("userid", myuserID));
                nameValuePairs.add(new BasicNameValuePair("new_restovspermission", itemsArray.toString()));

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

                    System.out.println("Utilisateur ajouté ");
                    Toast.makeText(getApplicationContext(), "Utilisateur ajouté", Toast.LENGTH_LONG).show();

                    showProgress(false);

                    Intent intent = new Intent(AddUserByID.this, UsersActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    showProgress(false);
                    mUsernameView.setError("Impossible d'ajouter ce contact");
                    mUsernameView.requestFocus();
                }

                System.out.println("result " + result);


            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
                showProgress(false);
                mUsernameView.setError(getString(R.string.error_connexion));
                mUsernameView.requestFocus();
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
