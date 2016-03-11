package com.wynd.app.wyndterminalpocket;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
    private String terminalIMEI = "", selectedStatus="";
    private TextView vDate1, vDate2, vTime1, vTime2;
    private RelativeLayout rl;
    private Button btnPeriods;

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
        rl = (RelativeLayout) findViewById(R.id.period);
        btnPeriods = (Button) findViewById(R.id.btnPeriods);
        initViews();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //rl.setVisibility(View.VISIBLE);
                orders = new JSONArray();
                deviceSpinner.setVisibility(View.VISIBLE);
                statusSpinner.setVisibility(View.VISIBLE);
                getTerminals();
                byStatus();
            }
        });
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

        checkOrders();

        btnPeriods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPeriodOrders();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_orders, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Orders.this);
                LayoutInflater inflater = Orders.this.getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder1.setView(inflater.inflate(R.layout.dialog_layout, null));
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Valider",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                builder1.setNegativeButton(
                        "Annuler",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
//                Intent intent = new Intent(this, SettingsActivity.class);
//                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void getTerminals() {
        JsonObjectRequest deviceRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            terminals = new JSONArray();
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
    }
    private List<OrderInfo> createList(JSONArray jsonArray) {

        final List<OrderInfo> result = new ArrayList<OrderInfo>();

        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                final OrderInfo ui = new OrderInfo();

                JSONObject json_data = jsonArray.getJSONObject(i);

                ui.order_reference = (json_data.isNull("order_ref") ? "" : json_data.getString("order_ref"));
                ui.order_status = (json_data.isNull("order_status") ? "" : json_data.getString("order_status"));
                if(ui.order_status.equals("1")){
                    ui.order_status = "Acceptée";
                }else if(ui.order_status.equals("-1")){
                    ui.order_status = "Refusée";
                }else if(ui.order_status.equals("2")){
                    ui.order_status = "Préparée";
                }else if(ui.order_status.equals("3")){
                    ui.order_status = "Délivrée";
                }else if(ui.order_status.equals("4")){
                    ui.order_status = "Prête";
                }else if(ui.order_status.equals("0")){
                    ui.order_status = "Reçue";
                }
                ui.order_desired_delivery = (json_data.isNull("order_desired_delivery") ? "" : json_data.getString("order_desired_delivery"));
                ui.reporting_terminal_id = (json_data.isNull("terminal") ? "" : json_data.getString("terminal"));
                ui.status_report_timestamp = (json_data.isNull("status_report_timestamp") ? "" : json_data.getString("status_report_timestamp"));

                result.add(ui);

            }

        }catch (JSONException e){
            Log.e("JSON parsing error", e.toString());
        }


        return result;
    }
    private void addTerminal(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Terminal");
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
                    System.out.println("selected nothing device spinner "+selectedStatus + " "+terminalIMEI);
                    //ici, afficher les commandes par status et vider le terminalIMEI
                    terminalIMEI = "";

                   if(!selectedStatus.isEmpty()) {
                       orders = new JSONArray();
                       /**
                        * get orders informations by status
                        */
                       JsonObjectRequest orderRequest = new JsonObjectRequest
                               (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/status/" + selectedStatus, null, new Response.Listener<JSONObject>() {
                                   @Override
                                   public void onResponse(JSONObject response) {

                                       try {
                                           JSONArray values = response.getJSONArray("data");
                                           Log.i("ORDER_INFO", values.toString());

                                           for (int i = 0; i < values.length(); i++) {
                                               JSONObject obj = values.getJSONObject(i);
                                               obj.put("order_ref", obj.getString("order_ref"));
                                               obj.put("order_status", obj.getString("order_status"));
                                               obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                               obj.put("terminal", obj.getString("macadress"));
                                               obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                               orders.put(obj);
                                           }
                                           if (orders.length() == 0) {
                                               total.setText("Aucune commande");
                                           } else {
                                               total.setText("TOTAL : " + orders.length() + " commandes");
                                           }
                                           ra = new OrderAdapter(createList(orders));
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
                               Map<String, String> params = new HashMap<String, String>();
                               params.put("Api-User", Globales.API_USER);
                               params.put("Api-Hash", Globales.API_HASH);

                               return params;
                           }
                       };

                       Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
                   }else{
                       System.out.println("checkOrders device spinner " + selectedStatus + " " + terminalIMEI);
                       checkOrders();
                   }
                }else{
                    if (item != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                String name = jsonArray.getJSONObject(i).getString("terminalMacadd");
                                if(item.equals(name)){

                                    terminalIMEI = name;
                                    System.out.println("selected selectedStatus "+selectedStatus);
                                    if(!selectedStatus.isEmpty()){
                                        System.out.println("selected by status & by terminal devicespinner "+selectedStatus+terminalIMEI);
                                        orders = new JSONArray();
                                        /**
                                         * get orders informations by terminal and by status
                                         */
                                        JsonObjectRequest orderRequest = new JsonObjectRequest
                                                (Request.Method.GET, Globales.baseUrl + "api/order/get/by/mns/"+terminalIMEI+"/"+selectedStatus, null, new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {

                                                        try {
                                                            JSONArray values = response.getJSONArray("data");
                                                            Log.i("ORDER_INFO", values.toString());

                                                            for(int i=0; i<values.length();i++){
                                                                JSONObject obj = values.getJSONObject(i);
                                                                obj.put("order_ref", obj.getString("order_ref"));
                                                                obj.put("order_status", obj.getString("order_status"));
                                                                obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                                obj.put("terminal", obj.getString("macadress"));
                                                                obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                                orders.put(obj);

                                                            }
                                                            if(orders.length() == 0){
                                                                total.setText("Aucune commande");
                                                            }else{
                                                                total.setText("TOTAL : " + orders.length() + " commandes");
                                                            }
                                                            ra = new OrderAdapter(createList(orders));
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
                                                Map<String, String> params = new HashMap<String, String>();
                                                params.put("Api-User", Globales.API_USER);
                                                params.put("Api-Hash", Globales.API_HASH);

                                                return params;
                                            }
                                        };

                                        Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
                                    }else{
                                        selectedStatus = "";
                                        System.out.println("selected by terminal "+terminalIMEI);
                                        orders = new JSONArray();
                                        /**
                                         * get orders informations by terminal
                                         */
                                        JsonObjectRequest orderRequest = new JsonObjectRequest
                                                (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/macadd/"+terminalIMEI, null, new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {

                                                        try {
                                                            JSONArray values = response.getJSONArray("data");
                                                            Log.i("ORDER_INFO", values.toString());

                                                            for(int i=0; i<values.length();i++){
                                                                JSONObject obj = values.getJSONObject(i);
                                                                obj.put("order_ref", obj.getString("order_ref"));
                                                                obj.put("order_status", obj.getString("order_status"));
                                                                obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                                obj.put("terminal", obj.getString("macadress"));
                                                                obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                                orders.put(obj);

                                                            }
                                                            if(orders.length() == 0){
                                                                total.setText("Aucune commande");
                                                            }else{
                                                                total.setText("TOTAL : " + orders.length() + " commandes");
                                                            }
                                                            ra = new OrderAdapter(createList(orders));
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
                                                Map<String, String> params = new HashMap<String, String>();
                                                params.put("Api-User", Globales.API_USER);
                                                params.put("Api-Hash", Globales.API_HASH);

                                                return params;
                                            }
                                        };

                                        Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
                                    }
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
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

        list.add(0, "Status");
        list.add(1, "Reçue");
        list.add(2, "Acceptée");
        list.add(3, "Préparée");
        list.add(4, "Prête");
        list.add(5, "Délivrée");
        list.add(6, "Refusée");

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
                    System.out.println("selected nothing status spinner "+selectedStatus + " "+terminalIMEI);
                    //ici afficher les commandes par terminal et vider le status
                    selectedStatus = "";

                    if(!terminalIMEI.isEmpty()){
                        orders = new JSONArray();
                        /**
                         * get orders informations by terminal
                         */
                        JsonObjectRequest orderRequest = new JsonObjectRequest
                                (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/macadd/"+terminalIMEI, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {
                                            JSONArray values = response.getJSONArray("data");
                                            Log.i("ORDER_INFO", values.toString());

                                            for(int i=0; i<values.length();i++){
                                                JSONObject obj = values.getJSONObject(i);
                                                obj.put("order_ref", obj.getString("order_ref"));
                                                obj.put("order_status", obj.getString("order_status"));
                                                obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                obj.put("terminal", obj.getString("macadress"));
                                                obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                orders.put(obj);

                                            }
                                            if(orders.length() == 0){
                                                total.setText("Aucune commande");
                                            }else{
                                                total.setText("TOTAL : " + orders.length() + " commandes");
                                            }
                                            ra = new OrderAdapter(createList(orders));
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
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Api-User", Globales.API_USER);
                                params.put("Api-Hash", Globales.API_HASH);

                                return params;
                            }
                        };

                        Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
                    }else {
                        System.out.println("checkOrders status spinner " + selectedStatus + " " + terminalIMEI);
                        checkOrders();
                    }

                }else{
                    if (item != null) {
                        if(item.equals("Refusée")){
                            selectedStatus = "-1";
                        }else if(item.equals("Acceptée")){
                            selectedStatus = "1";
                        } else if(item.equals("Préparée")){
                            selectedStatus = "2";
                        } else if(item.equals("Délivrée")){
                            selectedStatus = "3";
                        }else if(item.equals("Prête")){
                            selectedStatus = "4";
                        }else if(item.equals("Reçue")){
                            selectedStatus = "0";
                        }else{
                            selectedStatus = "";
                        }
                        if(!terminalIMEI.isEmpty()){
                            System.out.println("selected by status & by terminal statusspinner "+selectedStatus+terminalIMEI);
                            orders = new JSONArray();
                            /**
                             * get orders informations by terminal and by status
                             */
                            JsonObjectRequest orderRequest = new JsonObjectRequest
                                    (Request.Method.GET, Globales.baseUrl + "api/order/get/by/mns/"+terminalIMEI+"/"+selectedStatus, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {
                                                JSONArray values = response.getJSONArray("data");
                                                Log.i("ORDER_INFO", values.toString());

                                                for(int i=0; i<values.length();i++){
                                                    JSONObject obj = values.getJSONObject(i);
                                                    obj.put("order_ref", obj.getString("order_ref"));
                                                    obj.put("order_status", obj.getString("order_status"));
                                                    obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                    obj.put("terminal", obj.getString("macadress"));
                                                    obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                    orders.put(obj);

                                                }
                                                if(orders.length() == 0){
                                                    total.setText("Aucune commande");
                                                }else{
                                                    total.setText("TOTAL : " + orders.length() + " commandes");
                                                }
                                                ra = new OrderAdapter(createList(orders));
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
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("Api-User", Globales.API_USER);
                                    params.put("Api-Hash", Globales.API_HASH);

                                    return params;
                                }
                            };

                            Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
                        }else{
                            terminalIMEI = "";
                            System.out.println("selected by status "+selectedStatus);
                            orders = new JSONArray();
                            /**
                             * get orders informations by status
                             */
                            JsonObjectRequest orderRequest = new JsonObjectRequest
                                    (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/status/"+selectedStatus, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {
                                                JSONArray values = response.getJSONArray("data");
                                                Log.i("ORDER_INFO", values.toString());

                                                for(int i=0; i<values.length();i++){
                                                    JSONObject obj = values.getJSONObject(i);
                                                    obj.put("order_ref", obj.getString("order_ref"));
                                                    obj.put("order_status", obj.getString("order_status"));
                                                    obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                    obj.put("terminal", obj.getString("macadress"));
                                                    obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                    orders.put(obj);
                                                }
                                                if(orders.length() == 0){
                                                    total.setText("Aucune commande");
                                                }else{
                                                    total.setText("TOTAL : " + orders.length() + " commandes");
                                                }
                                                ra = new OrderAdapter(createList(orders));
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
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("Api-User", Globales.API_USER);
                                    params.put("Api-Hash", Globales.API_HASH);

                                    return params;
                                }
                            };

                            Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
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
    private void checkOrders(){
        total.setVisibility(View.VISIBLE);
        if(selectedStatus.equals("") && terminalIMEI.equals("")){

            /**
             * get all orders by entity
             */
            JsonObjectRequest orderRequest = new JsonObjectRequest
                    (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/entity/5", null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            orders = new JSONArray();
                            try {
                                JSONArray values = response.getJSONArray("data");
                                Log.i("ORDER_INFO", values.toString());

                                for(int i=0; i<values.length();i++){
                                    JSONObject obj = values.getJSONObject(i);
                                    obj.put("order_ref", obj.getString("order_ref"));
                                    obj.put("order_status", obj.getString("order_status"));
                                    obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                    obj.put("terminal", obj.getString("macadress"));
                                    obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                    orders.put(obj);
                                }
                               // getSortedList(orders);

                                if(orders.length() == 0){
                                    total.setText("Aucune commande");
                                }else{
                                    total.setText("TOTAL : " + orders.length() + " commandes");
                                }
                                ra = new OrderAdapter(createList(orders));
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
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Api-User", Globales.API_USER);
                    params.put("Api-Hash", Globales.API_HASH);

                    return params;
                }
            };

            Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
        }
    }
    public class SortByDate implements Comparator<JSONObject> {
        @Override
        public int compare(JSONObject lhs, JSONObject rhs) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
                Date date1 = sdf.parse(lhs.getString("status_report_timestamp"));
                Date date2 = sdf.parse(rhs.getString("status_report_timestamp"));

                System.out.println("orders dates " + date1.getTime() + date2.getTime());
               return date1.getTime()> date2.getTime() ? 1 : (date1.getTime() < date2.getTime() ? -1 : 0);
               // return (date1.getTime() > date2.getTime() ? -1 : 1);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;



        }
    }
    public JSONArray getSortedList(JSONArray array) throws JSONException {
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getJSONObject(i));
        }
        Collections.sort(list, new SortByDate());

        JSONArray resultArray = new JSONArray(list);

        return resultArray;
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private void initViews(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat stm = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        String time = stm.format(new Date());
        String data = sdf.format(new Date());

        vDate1 = (TextView) findViewById(R.id.date1);
        vTime1 = (TextView) findViewById(R.id.time1);
        vDate2 = (TextView) findViewById(R.id.date2);
        vTime2 = (TextView) findViewById(R.id.time2);

        vDate1.setText(data);
        vTime1.setText(time);
        vDate2.setText(data);
        vTime2.setText(time);

        vDate1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerFragment();
                // TODO Auto-generated method stub
                DatePickerFragment dte = DatePickerFragment.newInstance();
                dte.setCallBack(onDate);
                dte.show(getSupportFragmentManager().beginTransaction(), "DatePickerFragment");
            }
        });

        vDate2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerFragment();
                // TODO Auto-generated method stub
                DatePickerFragment dte = DatePickerFragment.newInstance();
                dte.setCallBack(onDate2);
                dte.show(getSupportFragmentManager().beginTransaction(), "DatePickerFragment");
            }
        });
        vTime1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerFragment();
                // TODO Auto-generated method stub
                TimePickerFragment dte = TimePickerFragment.newInstance();
                dte.setCallBack(onTime);
                dte.show(getSupportFragmentManager().beginTransaction(), "TimePickerFragment");
            }
        });

        vTime2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerFragment();
                // TODO Auto-generated method stub
                TimePickerFragment dte = TimePickerFragment.newInstance();
                dte.setCallBack(onTime2);
                dte.show(getSupportFragmentManager().beginTransaction(), "TimePickerFragment");
            }
        });

    }

    DatePickerDialog.OnDateSetListener onDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            vDate1.setText(sdf.format(newDate.getTime()));
        }
    };

    DatePickerDialog.OnDateSetListener onDate2 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            vDate2.setText(sdf.format(newDate.getTime()));
        }
    };
    TimePickerDialog.OnTimeSetListener onTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            vTime1.setText(hourOfDay+":"+minute);
        }
    };

    TimePickerDialog.OnTimeSetListener onTime2 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            vTime2.setText(hourOfDay+":"+minute);
        }
    };
    private void getPeriodOrders(){
        final String date1 = vDate1.getText().toString();
        final String date2 = vDate2.getText().toString();

        final String time1 = vTime1.getText().toString();
        final String time2 = vTime2.getText().toString();

        System.out.println("dates " + date1 + time1+ date2  + time2);
        /**
         * get all orders by entity
         */
        JsonObjectRequest orderRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/entity/5", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        orders = new JSONArray();
                        try {
                            JSONArray values = response.getJSONArray("data");
                            Log.i("ORDER_INFO", values.toString());
                            for(int i=0; i<values.length();i++){
                                    JSONObject obj = values.getJSONObject(i);

                                    String datetime1 = date1+" "+time1;
                                    String datetime2 = date2+" "+time2;

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                                    Date first = sdf.parse(datetime1);
                                    Date last = sdf.parse(datetime2);

                                    String order_date = obj.getString("status_report_timestamp");
                                    Date orderDate = sdf.parse(order_date);

                                    //afficher toutes les commandes reçues entre first et last
                                    if(orderDate.getTime()>=first.getTime() && orderDate.getTime()<=last.getTime() && first.getTime()<last.getTime())
                                    {
                                        System.out.println("order dates " + orderDate);
                                        obj.put("order_ref", obj.getString("order_ref"));
                                        obj.put("order_status", obj.getString("order_status"));
                                        obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                        obj.put("terminal", obj.getString("macadress"));
                                        obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                        orders.put(obj);
                                    }
                            }
                            if(orders.length() == 0){
                                total.setText("Aucune commande");
                            }else{
                                total.setText("TOTAL : " + orders.length() + " commandes");
                            }
                            ra = new OrderAdapter(createList(orders));
                            recList.setAdapter(ra);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
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

        Volley.newRequestQueue(getApplicationContext()).add(orderRequest);
    }
}
