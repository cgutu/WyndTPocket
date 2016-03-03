package com.wynd.app.wyndterminalpocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ProfilFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener, Restaurants.OnFragmentInteractionListener,
        Users.OnFragmentInteractionListener, Utilisateurs.OnFragmentInteractionListener, HistoriqueFragment.OnFragmentInteractionListener, Parents.OnFragmentInteractionListener,
        TerminalsFragment.OnFragmentInteractionListener{

    private SharedPreferences pref;
    private boolean viewIsAtHome;
    private String userID, parentID, permission, rest_channel, EntityInfo, restID;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                MenuActivity.class));

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.content_frame, new HomeFragment());
            fragmentTransaction.commit();
        }

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        editor = pref.edit();
        userID = pref.getString("myuserID", "");

       /**
        * get user info and store it in a session
        */
        JsonObjectRequest rolesRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/user/get/info/" + userID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");
                            JSONArray userResto = response.getJSONArray("usersInResto");

                            editor.putString("EntityInfo", userResto.toString());
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
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(rolesRequest);

        /**
         * display fragment views
         */

        EntityInfo = pref.getString("EntityInfo", "");
        String s1 = pref.getString("Check", "");

        if(!s1.isEmpty() && s1.equals("inforestaurant")){
            editor = pref.edit();
            editor.putString("Check", "0");
            editor.apply();

            displayView(R.id.nav_slideshow);
        }else if(!s1.isEmpty() && s1.equals("editmonprofil")){
            editor = pref.edit();
            editor.putString("Check", "0");
            editor.apply();

            displayView(R.id.nav_camera);
        }else if(!s1.isEmpty() && s1.equals("exitterminals")){
            editor = pref.edit();
            editor.putString("Check", "0");
            editor.apply();

            displayView(R.id.nav_slideshow);
        }else if(!s1.isEmpty() && s1.equals("infouser")){
            editor = pref.edit();
            editor.putString("Check", "0");
            editor.apply();

            displayView(R.id.nav_slideshow);
        }else if(!s1.isEmpty() && s1.equals("addentity")){
            editor = pref.edit();
            editor.putString("Check", "0");
            editor.apply();

            displayView(R.id.nav_slideshow);
        }else if(!s1.isEmpty() && s1.equals("exitorders")){
            editor = pref.edit();
            editor.putString("Check", "0");
            editor.apply();

            displayView(R.id.nav_slideshow);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (!viewIsAtHome) { //if the current view is not the Home fragment
            displayView(R.id.nav_gallery); //display the Home fragment
        } else {
            moveTaskToBack(true);  //If view is in Home fragment, exit application
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /**
         * logout on click event
         */
        Fragment fragment1 = null;
        switch (id) {
            case R.id.logout:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MenuActivity.this);
                builder1.setMessage("Etes-vous sûr de vouloir vous déconnecter ?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                //clear session& logout
                                editor = pref.edit();
                                editor.clear();
                                editor.apply();

                                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }
                        });

                builder1.setNegativeButton(
                        "Non",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

                return true;
            default:
                fragment1 = new HomeFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction
                        .replace(R.id.content_frame, fragment1);
                transaction.commit();

                return true;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displayView(item.getItemId());
        return true;
    }

    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        switch (viewId) {
            case R.id.nav_gallery:
                fragment = new HomeFragment();
                title  = "Home";
                viewIsAtHome = true;

                break;
            case R.id.nav_camera:
                fragment = new ProfilFragment();
                title  = "Profil";
                viewIsAtHome = false;

                break;
            case R.id.nav_slideshow:

                fragment = new Restaurants();
                title = "Restaurants";

                viewIsAtHome = false;
                break;
            default:
                fragment = new HomeFragment();
                title = "Home";
                viewIsAtHome = true;
                break;

        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        drawer.closeDrawer(GravityCompat.START);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
