package com.wynd.app.wyndterminalpocket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AskAccount extends AppCompatActivity {

    private View mProgressView;
    private View mFormview;
    private EditText username, email, phone, restaurant;
    private String sUsername, sEmail, sPhone, sRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                AskAccount.class));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFormview = findViewById(R.id.add_form);
        mProgressView = findViewById(R.id.login_progress);

        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        restaurant = (EditText) findViewById(R.id.rest);

        Button send = (Button) findViewById(R.id.submit);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForm();
            }
        });

    }
    private void checkForm() {

        // Reset errors.
        username.setError(null);
        email.setError(null);
        phone.setError(null);
        restaurant.setError(null);

        // Store values at the time of the login attempt.
        sUsername = username.getText().toString();
        sEmail = email.getText().toString();
        sPhone = phone.getText().toString();
        sRest = restaurant.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(sUsername)) {
            username.setError(getString(R.string.error_field_required));
            focusView = username;
            cancel = true;
        }
        if(TextUtils.isEmpty(sEmail)){
            email.setError(getString(R.string.error_field_required));
            focusView = email;
            cancel = true;
        }
        if(TextUtils.isEmpty(sPhone)){
            phone.setError(getString(R.string.error_field_required));
            focusView = phone;
            cancel = true;
        }
        if(TextUtils.isEmpty(sRest)){
            restaurant.setError(getString(R.string.error_field_required));
            focusView = restaurant;
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
            sendEmail();

        }


    }
    public void sendEmail(){
        System.out.println("demande compte "+sUsername+sEmail+sPhone+sRest);
        Resources res = getResources();

        String msgTemplate = String.format(res.getString(R.string.new_account), sUsername, sEmail, sPhone, sRest);

        try {
            GmailSender sender = new GmailSender("peestashgirls", "peestash2015");
            sender.sendMail("Demande de nouveau compte",
                    msgTemplate,
                    "peestashgirls@gmail.com",
                    "cgutu@wynd.eu");

            showProgress(false);
            Intent i = new Intent(AskAccount.this, LoginActivity.class);
            startActivity(i);
            String msg="Votre demande a bien été prise en compte !";
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            finish();

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
}
