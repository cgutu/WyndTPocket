package com.wynd.app.wyndterminalpocket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * LoginActivity - the login form
 * @author Cornelia Gutu
 * @version 1.0
 */
public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mdp;
    private String username, password, userID, parentID, permission, rest_channel;

    private SharedPreferences.Editor editor;
    private SharedPreferences pref;
    private String message;
    private Button askaccount;
    private JSONArray EntityInfo = new JSONArray();
    /**
     * set up the login form
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                LoginActivity.class));

        /**
         * Set the API HASH
         */
        try {

            Globales.API_HASH = AeSimpleSHA1.SHA1(Globales.hash);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            Log.e("Error sha1 API_HASH", e.toString());
        }

        /**
         * get stored user infos
         */
        pref = getApplicationContext().getSharedPreferences("Infos", 0); // 0 - for private mode

        /**
         * Set up the login form.
         */
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mUserView = (AutoCompleteTextView) findViewById(R.id.username);
        mdp = (TextView) findViewById(R.id.mdp);
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


        /**
         * lost password
         */
        mdp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MdpOublie.class);
                startActivity(i);
            }
        });

        /**
         * check fields
         */
        Button mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });




        //if no account, go to another form
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

        // Check for a valid password
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

            /**
             * convert password to Sha1
             */
            try {
                password = AeSimpleSHA1.SHA1(password);

            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                Log.e("Error sha1 password", e.toString());
            }



            new LoginTask().execute();

        }


    }
    /**
     * @POST - background task for login
     * @parameters username & secret
     */
    private class LoginTask extends AsyncTask<Void, Void, InputStream> {
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

                    JSONTokener tokener = new JSONTokener(json);
                    JSONObject finalResult = new JSONObject(tokener);

                    String result = finalResult.getString("result");

                    if (!result.isEmpty() && result.equals("success")) {
                        JSONObject jsonObject = finalResult.getJSONObject("data");
                        userID = jsonObject.isNull("user_id") ? "" : jsonObject.getString("user_id");

                        editor = pref.edit();
                        editor.putString("username", username);
                        editor.putString("myuserID", userID);
                        editor.apply();

                        /**
                         * @GET - get user info
                         * @return res_parent_id
                         * @return permissionID
                         * @return resaturantChainID
                         */

                        JsonObjectRequest rolesRequest = new JsonObjectRequest
                                (Request.Method.GET, Globales.baseUrl+"api/user/get/info/" + userID, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // the response is already constructed as a JSONObject!
                                        try {
                                            response = response.getJSONObject("data");
                                            JSONArray userResto = response.getJSONArray("usersInResto");
                                            for(int i=0; i<userResto.length(); i++){
                                                JSONObject userRestInfo = userResto.getJSONObject(i);
                                                EntityInfo.put(userRestInfo);
                                            }
                                            editor = pref.edit();
                                            editor.putString("EntityInfo", EntityInfo.toString());
                                            editor.apply();
                                            editor.commit();

                                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                            startActivity(intent);
                                            showProgress(false);
                                            finish();


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

                        Volley.newRequestQueue(getApplicationContext()).add(rolesRequest);

                    } else {
                        showProgress(false);
                        mPasswordView.setError(result);
                        mPasswordView.requestFocus();
                    }

                    Log.i("LOGIN", ""+total);
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

