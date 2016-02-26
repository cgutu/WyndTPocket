package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/** 
 * Dashboard
 * @author Cornelia Gutu
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                MainActivity.class));

        /**
         * @user clear stored data if exists
         */
        pref = getApplicationContext().getSharedPreferences("Infos", 0); // 0 - for private mode
        editor = pref.edit();
        editor.clear();
        editor.apply();

        /**
         * @main go to login after 5s
         */
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);

                finish();
            }
        }, 1000);

    }
}
