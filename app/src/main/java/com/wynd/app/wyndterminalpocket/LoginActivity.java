package com.wynd.app.wyndterminalpocket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity {


    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String username, password, userID, parentID, permission, rest_channel;

    private SharedPreferences.Editor editor;
    private SharedPreferences pref;
    private String message;
    private Button askaccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = getApplicationContext().getSharedPreferences("Infos", 0); // 0 - for private mode
        editor = pref.edit();

        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        try {

            Globales.API_HASH = AeSimpleSHA1.SHA1(Globales.hash);
            System.out.println("SHA1 API_HASH " + Globales.API_HASH);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            System.out.println("Error sha1 " + e);
        }

        askaccount = (Button) findViewById(R.id.askaccount);
        askaccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, AskAccount.class);
                startActivity(i);
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        username = mUserView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            showProgress(true);

            try {
                password = AeSimpleSHA1.SHA1(password);
                System.out.println("SHA1 password " + password);


            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                System.out.println("Error sha1 " + e);
            }



            new LoginTask().execute();

        }


    }
    private class LoginTask extends AsyncTask<Void, Void, InputStream> {
        int i;
        String result = null;
        InputStream is = null;
        List<NameValuePair> nameValuePairs;
        JSONArray jsonArray = new JSONArray();


        protected InputStream doInBackground(Void... params) {

                //setting nameValuePairs
                nameValuePairs = new ArrayList<NameValuePair>(1);
                System.out.println("do in background login task "+password+" "+Globales.API_HASH );

                try {
                    //Setting up the default http client
                    HttpClient httpClient = new DefaultHttpClient();

                    //setting up the http post method
                    HttpPost httpPost = new HttpPost(Globales.baseUrl+"api/user/login");
                    nameValuePairs.add(new BasicNameValuePair("username", username));
                    nameValuePairs.add(new BasicNameValuePair("secret", password));
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
                    System.out.println("total: " + json);
                    JSONTokener tokener = new JSONTokener(json);
                    JSONObject finalResult = new JSONObject(tokener);

                    int i = 0;

                    String result = finalResult.getString("result");
                    System.out.println("result " + result);

                    if (!result.isEmpty() && result.equals("success")) {
                        JSONObject jsonObject = finalResult.getJSONObject("data");
                        userID = jsonObject.getString("user_id");
                        parentID = jsonObject.getString("parent_id");

                        editor.putString("username", username);
                        editor.putString("myuserID", userID);
                        editor.putString("parentID", parentID);
                        editor.apply();


                        //check if user is ADMIN
                        JsonObjectRequest rolesRequest = new JsonObjectRequest
                                (Request.Method.GET, Globales.baseUrl+"api/user/get/info/" + userID, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // the response is already constructed as a JSONObject!
                                        try {
                                            response = response.getJSONObject("data");
                                            System.out.println("response " + response);

                                            permission = response.isNull("permission") ? "" : response.getString("permission");
                                            rest_channel = response.isNull("rest_channel") ? "" : response.getString("rest_channel");
                                            System.out.println("user permission " + permission + " rest_channel "+rest_channel);
                                            editor.putString("roles", permission);
                                            editor.putString("rest_channel", rest_channel);
                                            editor.apply();


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

                                System.out.println("api infos sent" + Globales.API_USER + " " + Globales.API_HASH);
                                params.put("Api-User", Globales.API_USER);
                                params.put("Api-Hash", Globales.API_HASH);

                                return params;
                            }
                        };

                        Volley.newRequestQueue(getApplicationContext()).add(rolesRequest);

                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intent);
                        showProgress(false);
                        finish();
                    } else {
                        showProgress(false);
                        mPasswordView.setError(getString(R.string.error_invalid_user_pwd));
                        mPasswordView.requestFocus();
                    }

                    System.out.println("result " + result);


                } catch (Exception e) {
                    Log.i("tagconvertstr", "" + e.toString());
                    showProgress(false);
                    mUserView.setError(getString(R.string.error_connexion));
                    mUserView.requestFocus();
                }



        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}

