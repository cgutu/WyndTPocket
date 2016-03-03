package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Orders extends AppCompatActivity {

    private String myuserID, channelID;
    private RecyclerView recList;
    private OrderAdapter ra;
    private List<OrderInfo> order;
    private Spinner deviceSpinner, statusSpinner;
    private JSONArray terminals = new JSONArray();
    private ArrayAdapter<String> dataAdapter;
    private TextView total;
    private JSONArray orders = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getApplicationContext(),
                Orders.class));

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        order = new ArrayList<>();
        ra = new OrderAdapter(order);
        recList.setAdapter(ra);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Infos", 0);
        myuserID = pref.getString("myuserID", "");

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Check", "exitorders");
        editor.apply();

        total = (TextView) findViewById(R.id.total_orders);
        deviceSpinner = (Spinner) findViewById(R.id.device);
        statusSpinner = (Spinner) findViewById(R.id.status);

        Intent intent = getIntent();
        channelID = intent.getStringExtra("restId");

        JsonObjectRequest deviceRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");

                            System.out.println(" data "+values);
                            for (int i = 0; i < values.length(); i++) {

                                JSONObject terminal = values.getJSONObject(i);
                                if(!terminal.getString("channelID").isEmpty() && terminal.getString("channelID").equals(channelID)){
                                    terminals.put(terminal);
                                }
                            }
                            System.out.println(" terminals "+terminals);
                            addTerminal(terminals);

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

        Volley.newRequestQueue(getApplicationContext()).add(deviceRequest);

        orders = new JSONArray();
        try {

            for (int i = 0; i < 10; i++) {

                JSONObject obj = new JSONObject();
                obj.put("order_ref", "CW1603-0695");
                obj.put("order_status", "Accepted");
                obj.put("order_desired_delivery", "2016-03-03 15h15");
                obj.put("terminal", "353007060012380");

                JSONObject obj2 = new JSONObject();
                obj2.put("order_ref", "CW1603-0694");
                obj2.put("order_status", "Refused");
                obj2.put("order_desired_delivery", "2016-03-04 15h15");
                obj2.put("terminal", "359523062314081");

                orders.put(obj);
                orders.put(obj2);

            }
            total.setText("TOTAL : "+orders.length() + " commandes");
            ra = new OrderAdapter(createList(orders));
            recList.setAdapter(ra);
        }catch (JSONException e){

        }

        byStatus();


        /**
         * get order informations
         */
        /*
        JsonObjectRequest orderRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/all/parents/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            for(int i=0; i<values.length();i++){
                                JSONObject parent = values.getJSONObject(i);

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

        Volley.newRequestQueue(getApplicationContext()).add(orderRequest);*/
    }

    private List<OrderInfo> createList(JSONArray jsonArray) {

        final List<OrderInfo> result = new ArrayList<OrderInfo>();

        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                final OrderInfo ui = new OrderInfo();

                JSONObject json_data = jsonArray.getJSONObject(i);

                ui.order_reference = (json_data.isNull("order_ref") ? "" : json_data.getString("order_ref"));
                ui.order_status = (json_data.isNull("order_status") ? "" : json_data.getString("order_status"));
                ui.order_desired_delivery = (json_data.isNull("order_desired_delivery") ? "" : json_data.getString("order_desired_delivery"));
                ui.reporting_terminal_id = (json_data.isNull("terminal") ? "" : json_data.getString("terminal"));
                result.add(ui);


            }

        }catch (JSONException e){
            Log.e("JSON parsing error", e.toString());
        }


        return result;
    }
    private void addTerminal(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Filtrer par terminal");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("terminalMacadd");
                list.add("" + name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(dataAdapter);

        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){
                    orders = new JSONArray();
                    try {

                        for (int i = 0; i < 10; i++) {

                            JSONObject obj = new JSONObject();
                            obj.put("order_ref", "CW1603-0695");
                            obj.put("order_status", "Accepted");
                            obj.put("order_desired_delivery", "2016-03-03 15h15");
                            obj.put("terminal", "353007060012380");

                            JSONObject obj2 = new JSONObject();
                            obj2.put("order_ref", "CW1603-0694");
                            obj2.put("order_status", "Refused");
                            obj2.put("order_desired_delivery", "2016-03-04 15h15");
                            obj2.put("terminal", "359523062314081");

                            orders.put(obj);
                            orders.put(obj2);

                        }
                        total.setText("TOTAL : "+orders.length() + " commandes");
                        ra = new OrderAdapter(createList(orders));
                        recList.setAdapter(ra);
                    }catch (JSONException e){

                    }
                }else{
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("terminalMacadd");
                            if(item.equals(name)){
                               String terminalID = jsonArray.getJSONObject(i).getString("terminalID");

                                //afficher les commandes arrivées sur ce terminal
                                orders = new JSONArray();
                                try {

                                    for (int j = 0; j < 5; j++) {

                                        JSONObject obj = new JSONObject();
                                        obj.put("order_ref", "CW1603-0695");
                                        obj.put("order_status", "Accepted");
                                        obj.put("order_desired_delivery", "2016-03-03 15h15");
                                        obj.put("terminal", "353007060012380");

                                        orders.put(obj);
                                    }
                                    total.setText("TOTAL : "+orders.length() + " commandes");
                                    ra = new OrderAdapter(createList(orders));
                                    recList.setAdapter(ra);

                                }catch (JSONException e){

                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

    }
    private void byStatus(){

        List<String> list = new ArrayList<String>();

        list.add(0, "Filtrer par status");
        list.add(1, "Accepted");
        list.add(2, "Refused");

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(dataAdapter);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){
                    orders = new JSONArray();
                    try {

                        for (int i = 0; i < 10; i++) {

                            JSONObject obj = new JSONObject();
                            obj.put("order_ref", "CW1603-0695");
                            obj.put("order_status", "Accepted");
                            obj.put("order_desired_delivery", "2016-03-03 15h15");
                            obj.put("terminal", "353007060012380");

                            JSONObject obj2 = new JSONObject();
                            obj2.put("order_ref", "CW1603-0694");
                            obj2.put("order_status", "Refused");
                            obj2.put("order_desired_delivery", "2016-03-04 15h15");
                            obj2.put("terminal", "359523062314081");

                            orders.put(obj);
                            orders.put(obj2);

                        }
                        total.setText("TOTAL : "+orders.length() + " commandes");
                        ra = new OrderAdapter(createList(orders));
                        recList.setAdapter(ra);
                    }catch (JSONException e){

                    }
                }else{
                }
                if (item != null) {
                            if(item.equals("Refused")){

                                //afficher toutes les commandes réfusées
                                orders = new JSONArray();
                                try {

                                    for (int j = 0; j < 5; j++) {

                                        JSONObject obj = new JSONObject();
                                        obj.put("order_ref", "CW1603-0695");
                                        obj.put("order_status", "Refused");
                                        obj.put("order_desired_delivery", "2016-03-03 15h15");
                                        obj.put("terminal", "353007060012380");

                                        orders.put(obj);
                                    }
                                    total.setText("TOTAL : "+orders.length() + " commandes");
                                    ra = new OrderAdapter(createList(orders));
                                    recList.setAdapter(ra);

                                }catch (JSONException e){

                                }
                            }else if(item.equals("Accepted")){

                                //afficher toutes les commandes acceptées
                                orders = new JSONArray();
                                try {

                                    for (int j = 0; j < 5; j++) {

                                        JSONObject obj = new JSONObject();
                                        obj.put("order_ref", "CW1603-0695");
                                        obj.put("order_status", "Accepted");
                                        obj.put("order_desired_delivery", "2016-03-03 15h15");
                                        obj.put("terminal", "353007060012380");

                                        orders.put(obj);
                                    }
                                    total.setText("TOTAL : "+orders.length() + " commandes");
                                    ra = new OrderAdapter(createList(orders));
                                    recList.setAdapter(ra);

                                }catch (JSONException e){

                                }
                            }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

    }
}
