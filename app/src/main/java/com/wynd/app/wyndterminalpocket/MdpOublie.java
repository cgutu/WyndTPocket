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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class MdpOublie extends AppCompatActivity {

    private EditText vEmail, vEntity;
    private Button btn;
    private String email, entity;
    private View mProgressView;
    private View mFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdp_oublie);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                MdpOublie.class));

        Button fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForm();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFormView = findViewById(R.id.form);
        mProgressView = findViewById(R.id.login_progress);

        vEmail = (EditText) findViewById(R.id.email);
        vEntity = (EditText) findViewById(R.id.entity);

    }
    private void checkForm() {

        // Reset errors.
        vEmail.setError(null);
        vEntity.setError(null);

        // Store values at the time of the login attempt.
        email = vEmail.getText().toString();
        entity = vEntity.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(email)) {
            vEmail.setError(getString(R.string.error_field_required));
            focusView = vEmail;
            cancel = true;
        }
        if (TextUtils.isEmpty(entity)) {
            vEntity.setError(getString(R.string.error_field_required));
            focusView = vEmail;
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
        //send notification
        System.out.println("email "+email + entity);
        Resources res = getResources();

        String msgTemplate = String.format(res.getString(R.string.email_template), email, entity);

        try {
            GmailSender sender = new GmailSender("peestashgirls", "peestash2015");
            sender.sendMail("Merci de m'envoyer un nouveau mot de passe",
                    msgTemplate,
                    "peestashgirls@gmail.com",
                    "cgutu@wynd.eu");

            showProgress(false);
            Intent i = new Intent(MdpOublie.this, LoginActivity.class);
            startActivity(i);
            String msg="Votre demande a bien été prise en compte !";
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            finish();

        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
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

            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
