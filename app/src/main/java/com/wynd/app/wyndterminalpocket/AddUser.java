package com.wynd.app.wyndterminalpocket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class AddUser extends AppCompatActivity{

    private EditText mUsernameView, mEmailView, mPasswordView, mPhoneView;
    private String email, username, phone, password, userID;
    private View mProgressView;
    private View mFormview;
    private JSONArray chains = new JSONArray();
    protected CharSequence[] restaurants;
    protected ArrayList<CharSequence> selectedEntity = new ArrayList<CharSequence>();
    private Button selectEntityButton;
    List<String> listItems = new ArrayList<String>();
    List<String> selectedItem;
    JSONObject channels;
    JSONArray itemsArray;
    private JSONArray parents = new JSONArray();
    private Spinner parentSpinner, permissionSpinner;
    private ArrayAdapter<String> dataAdapter;
    private String itemPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                AddUser.class));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        /**
         * set views
         */
        mFormview = findViewById(R.id.add_form);
        mProgressView = findViewById(R.id.login_progress);
        parentSpinner = (Spinner) findViewById(R.id.parent);
        permissionSpinner = (Spinner) findViewById(R.id.permission);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Infos", 0);
        userID = pref.getString("myuserID", "");

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Check", "userlist");
        editor.apply();

        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mEmailView = (EditText) findViewById(R.id.email);
        mPhoneView = (EditText) findViewById(R.id.phone);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkForm();

            }
        });

        /**
         * load parents
         */
        JsonObjectRequest parentRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/all/parents/user/"+userID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");

                            for(int i=0; i<values.length(); i++){
                                parents.put(values.getJSONObject(i));
                            }
                            addParent(parents);
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

        Volley.newRequestQueue(getApplicationContext()).add(parentRequest);

        selectEntityButton = (Button) findViewById(R.id.addrest);
        selectEntityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectEntityDialog();
            }
        });

        /**
         * set permission spinner
         */
        List<String> list = new ArrayList<String>();
        list.add("Séléctionner une permission");
        list.add("USER");
        list.add("ADMIN");
        //list.add("SUPER ADMIN");
        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        permissionSpinner.setAdapter(dataAdapter);

        listItems = new ArrayList<String>();

        permissionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){
                   itemPermission = "";
                }else{
                    if (item != null) {
                        if(item.toString().equals("USER")){
                            itemPermission = "1";
                        }else if(item.toString().equals("ADMIN")){
                            itemPermission = "2";
                        }else{
                            itemPermission = "3";
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

    }

    protected void showSelectEntityDialog() {


        restaurants = listItems.toArray(new CharSequence[listItems.size()]);

        boolean[] checkedEntities = new boolean[restaurants.length];

        int count = restaurants.length;

        for(int i = 0; i < count; i++)

            checkedEntities[i] = selectedEntity.contains(restaurants[i]);

        DialogInterface.OnMultiChoiceClickListener entitiesDialogListener = new DialogInterface.OnMultiChoiceClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                if(isChecked)

                    selectedEntity.add(restaurants[which]);

                else

                    selectedEntity.remove(restaurants[which]);

                onChangeSelectedEntity();

            }

        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Séléctionner un restaurant");

        builder.setMultiChoiceItems(restaurants, checkedEntities, entitiesDialogListener);
        AlertDialog dialog = builder.create();
        dialog.show();


    }
    protected void onChangeSelectedEntity() {

        selectedItem = new ArrayList<String>();

        StringBuilder stringBuilder = new StringBuilder();

        for(CharSequence entity : selectedEntity){
            stringBuilder.append(entity + ",");
        }

        if(stringBuilder.toString().isEmpty()){
            selectEntityButton.setText("Veuillez sélectionner au moins un restaurant");
            selectEntityButton.setTextColor(Color.RED);
            parentSpinner.setEnabled(true);
        }else{
            selectEntityButton.setTextColor(Color.BLACK);
            selectEntityButton.setText(stringBuilder.toString());
            parentSpinner.setEnabled(false);
        }

    }

    /**
     * verify form
     */
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
        String items = selectEntityButton.getText().toString();
        selectedItem = Arrays.asList(items.split(","));

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

        try{

            itemsArray = new JSONArray();
            for(int i=0; i<selectedItem.size(); i++){
                String selectedName = selectedItem.get(i);
                for(int j=0; j<chains.length(); j++){
                    String name = chains.getJSONObject(j).getString("name");
                    if(selectedName.equalsIgnoreCase(name)){
                        channels = new JSONObject();
                        channels.put("restid", chains.getJSONObject(j).getString("id"));
                        channels.put("permission", itemPermission);
                    }
                }
                Log.i("items", channels.toString());
                itemsArray.put(channels);
            }
        }catch (JSONException e){

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
            try {
                //Setting up the default http client
                HttpClient httpClient = new DefaultHttpClient();

                //setting up the http post method
                HttpPost httpPost = new HttpPost(Globales.baseUrl+"api/user/add");

                nameValuePairs.add(new BasicNameValuePair("new_username", username));
                nameValuePairs.add(new BasicNameValuePair("new_secret", password));
                nameValuePairs.add(new BasicNameValuePair("new_email", email));
                nameValuePairs.add(new BasicNameValuePair("new_phone", phone));
                nameValuePairs.add(new BasicNameValuePair("userid", userID));
                nameValuePairs.add(new BasicNameValuePair("new_restovspermission", itemsArray.toString()));

                httpPost.setHeader("Api-User", Globales.API_USER);
                httpPost.setHeader("Api-Hash", Globales.API_HASH);
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));

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

                Log.i("Result", result);

                if (!result.isEmpty() && result.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Utilisateur ajouté", Toast.LENGTH_LONG).show();

                    showProgress(false);
                    Resources res = getResources();

                    String msgTemplate = String.format(res.getString(R.string.user_created), username, email, password);
                    sendEmail(msgTemplate);
                    Intent intent = new Intent(AddUser.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    showProgress(false);
                    mUsernameView.setError("Impossible d'ajouter ce contact");
                    mUsernameView.requestFocus();
                }

            } catch (Exception e) {
                Log.i("tagconvertstr", "" + e.toString());
                showProgress(false);
                mUsernameView.setError(getString(R.string.error_connexion));
                mUsernameView.requestFocus();
            }
        }
    }
    public void sendEmail(String msgTemplate){

        try {
            GmailSender sender = new GmailSender("peestashgirls", "peestash2015");
            sender.sendMail("Demande de nouveau compte",
                    msgTemplate,
                    "peestashgirls@gmail.com",
                    email);

        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
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
    private void addParent(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Séléctionner une franchise");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("parent_name");
                if(!list.contains(name)){
                    list.add("" + name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parentSpinner.setAdapter(dataAdapter);

        listItems = new ArrayList<String>();

        parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){

                    selectEntityButton.setVisibility(View.GONE);
                    permissionSpinner.setVisibility(View.GONE);
                }else{
                    selectEntityButton.setVisibility(View.VISIBLE);
                    permissionSpinner.setVisibility(View.VISIBLE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("parent_name");
                            if(item.equals(name)){
                                final String selectedID = jsonArray.getJSONObject(i).getString("id");

                                /**
                                 * complete entities list
                                 */
                                JsonObjectRequest entityRequest = new JsonObjectRequest
                                        (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/by/parent/"+selectedID+"/user/"+userID, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    listItems = new ArrayList<String>();
                                                    JSONArray values = response.getJSONArray("data");
                                                    for (int i = 0; i < values.length(); i++) {
                                                        listItems.add(values.getJSONObject(i).getString("name"));
                                                        chains.put(values.getJSONObject(i));
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

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


    }

}
