package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersActivity extends AppCompatActivity {

    private JSONArray users = new JSONArray();
    List<String> username, email, phone, rest_channel, permission;
    private RecyclerView recList;
    private UserAdapter ra;
    private Spinner restSpinner;
    private Button btnSubmit;
    private static String ROLE_ADMIN = "ADMIN";
    private static String ROLE_USER = "USER";
    private static String ROLE_SUPER_ADMIN = "SUPER ADMIN";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String savedRestId;
    private String ID;
    private List<UserInfo> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        final String restId;
        restId = intent.getStringExtra("restId");
        System.out.println("test click " + restId);

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        System.out.println("rest id test " + pref.getString("restId", ""));
        savedRestId = pref.getString("restId", "");

        if(restId == null){
            ID = savedRestId;
        }else{
            ID = restId;
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent i = new Intent(UsersActivity.this, AddUser.class);
                i.putExtra("restId",ID);
                startActivity(i);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        user = new ArrayList<>();
        ra = new UserAdapter(user);
        recList.setAdapter(ra);

        JsonObjectRequest userRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/user/in_restaurant/"+ID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            System.out.println("response " + response);

                            for (int i = 0; i < values.length(); i++) {

                                JSONObject restaurants = values.getJSONObject(i);
                                users.put(restaurants);

                            }


                            System.out.println("users " + users);
                            ra = new UserAdapter(createList(users));
                            recList.setAdapter(ra);


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

                System.out.println("api infos sent" + Globales.API_USER + " "+Globales.API_HASH);
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(userRequest);
    }

    private List<UserInfo> createList(JSONArray jsonArray) {

        List<UserInfo> result = new ArrayList<UserInfo>();

        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                UserInfo ui = new UserInfo();
                JSONObject json_data = jsonArray.getJSONObject(i);

                ui.id = (json_data.isNull("id") ? "" : UserInfo.ID_PREFIX +  json_data.getString("id"));
                ui.username = (json_data.isNull("username") ? "" : UserInfo.USERNAME_PREFIX +  json_data.getString("username"));
                ui.email = (json_data.isNull("email") ? "" : UserInfo.EMAIL_PREFIX +  json_data.getString("email"));
                ui.phone = (json_data.isNull("phone") ? "" : UserInfo.PHONE_PREFIX +  json_data.getString("phone"));
                ui.rest_channel = (json_data.isNull("rest_channel") ? "" : UserInfo.RESTCHANNEL_PREFIX +  json_data.getString("rest_channel"));

                String permission = (json_data.isNull("permission") ? "" : json_data.getString("permission"));

                if(!permission.isEmpty() && permission.equals("2")){
                    ui.permission = ROLE_ADMIN;
                }else if(!permission.isEmpty() && permission.equals("1")){
                    ui.permission = ROLE_USER;
                }else{
                    ui.permission = ROLE_SUPER_ADMIN;
                }


                editor = pref.edit();
                editor.putString("restId", json_data.getString("rest_channel"));
                editor.putString("userID", json_data.getString("id"));
                editor.apply();
                result.add(ui);
            }

        }catch (JSONException e){
            System.out.println("Erreur json "+e);
        }

        return result;
    }

}
