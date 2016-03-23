package com.wynd.app.wyndterminalpocket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

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
         * @main go to login after 5s
         */
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                /**
                 * @user clear stored data if exists
                 */
                pref = getApplicationContext().getSharedPreferences("Infos", 0); // 0 - for private mode
                /**
                 * redirect interface if user already connected
                 */
                String userID = pref.getString("myuserID", "");
                String EntityInfo = pref.getString("EntityInfo", "");
                System.out.println("userID stored " + userID);
                System.out.println("EntityInfo stored " + EntityInfo);
                if(!userID.isEmpty() && !EntityInfo.isEmpty()){

                    /**
                     * Set the API HASH
                     */
                    try {

                        Globales.API_HASH = AeSimpleSHA1.SHA1(Globales.hash);

                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        Log.e("Error sha1 API_HASH", e.toString());
                    }

                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    editor = pref.edit();
                    editor.clear();
                    editor.apply();

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }


            }
        }, 1000);
    }
}
