package com.wynd.app.wyndterminalpocket;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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

    private String myuserID, channelID, restId;
    private RecyclerView recList;
    private OrderAdapter ra;
    private List<OrderInfo> order;
    private Spinner deviceSpinner, statusSpinner;
    private JSONArray terminals = new JSONArray();
    private ArrayAdapter<String> dataAdapter;
    private TextView total;
    private JSONArray orders = new JSONArray();
    private String terminalIMEI = "", selectedStatus="";
    private TextView vDate1, vDate2, vTime1, vTime2, vDateOne, vTimeOne;
    private RelativeLayout rl;
    private Button btnPeriods;
    private View promptView;
    private CheckBox bydate, byperiod;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

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

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        myuserID = pref.getString("myuserID", "");
        String savedChannel = pref.getString("clickedchannel", "");

        editor = pref.edit();
        editor.putString("Check", "exitorders");
        editor.apply();

        total = (TextView) findViewById(R.id.total_orders);
        deviceSpinner = (Spinner) findViewById(R.id.device);
        statusSpinner = (Spinner) findViewById(R.id.status);

        Intent intent = getIntent();
        channelID = intent.getStringExtra("restId");

        if(channelID == null){
            restId = savedChannel;
        }else{
            restId = channelID;
        }

        checkOrders();

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
                promptView = inflater.inflate(R.layout.dialog_layout, null);
                initViews();
                getTerminals();
                byStatus();
                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder1.setView(inflater.inflate(R.layout.dialog_layout, null));
                builder1.setCancelable(true);
                builder1.setTitle("Trier les commandes");

                bydate = (CheckBox) promptView.findViewById(R.id.bydate);
                byperiod = (CheckBox) promptView.findViewById(R.id.byperiod);
                final LinearLayout period1 = (LinearLayout) promptView.findViewById(R.id.period1);
                final LinearLayout period2 = (LinearLayout) promptView.findViewById(R.id.period2);
                final LinearLayout onedate = (LinearLayout) promptView.findViewById(R.id.onlyonedate);
                final LinearLayout between = (LinearLayout) promptView.findViewById(R.id.between);

                byperiod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(byperiod.isChecked()){
                            bydate.setEnabled(false);
                            deviceSpinner.setEnabled(false);
                            statusSpinner.setEnabled(false);
                            period1.setVisibility(View.VISIBLE);
                            period2.setVisibility(View.VISIBLE);
                            between.setVisibility(View.VISIBLE);
                        }else{
                            deviceSpinner.setEnabled(true);
                            statusSpinner.setEnabled(true);
                            bydate.setEnabled(true);
                            period1.setVisibility(View.GONE);
                            period2.setVisibility(View.GONE);
                            between.setVisibility(View.GONE);
                        }
                    }
                });
                bydate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(bydate.isChecked()){
                            deviceSpinner.setEnabled(false);
                            statusSpinner.setEnabled(false);
                            byperiod.setEnabled(false);
                            onedate.setVisibility(View.VISIBLE);
                        }else{
                            deviceSpinner.setEnabled(true);
                            statusSpinner.setEnabled(true);
                            byperiod.setEnabled(true);
                            onedate.setVisibility(View.GONE);
                        }
                    }
                });

                builder1.setPositiveButton(
                        "Voir",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(byperiod.isChecked()){
                                    getPeriodOrders();
                                }else if(bydate.isChecked()){
                                    getOrdersbyDate();
                                }
                                dialog.dismiss();
                            }
                        });
//
//                builder1.setNegativeButton(
//                        "Annuler",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });

                AlertDialog alert11 = builder1.create();

                alert11.setView(promptView);
                alert11.show();
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

        list.add(0, "Par terminal");
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
                       /**
                        * get orders informations by status
                        */
                       JsonObjectRequest orderRequest = new JsonObjectRequest
                               (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/status/" + selectedStatus, null, new Response.Listener<JSONObject>() {
                                   @TargetApi(Build.VERSION_CODES.KITKAT)
                                   @Override
                                   public void onResponse(JSONObject response) {

                                       try {
                                           orders = new JSONArray();
                                           JSONArray values = response.getJSONArray("data");
                                           Log.i("ORDER_INFO", values.toString());

                                           for (int i = 0; i < values.length(); i++) {
                                               JSONObject obj = values.getJSONObject(i);
                                               JSONObject newobj = new JSONObject();
                                               newobj.put("order_ref", obj.getString("order_ref"));
                                               newobj.put("order_status", obj.getString("order_status"));
                                               newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                               newobj.put("terminal", obj.getString("macadress"));
                                               newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                               if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                                   Integer status = Integer.parseInt(obj.getString("order_status"));
                                                   String  ref = obj.getString("order_ref");
                                                   for(int j=0; j<orders.length(); j++){
                                                       JSONObject oneorder = orders.getJSONObject(j);
                                                       Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                                       String oldRef = oneorder.getString("order_ref");
                                                       if(status > oldStatus && oldRef.equals(ref)){
                                                           orders.remove(j);
                                                           orders.put(newobj);
                                                           System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                       }else if(status == -1 && oldRef.equals(ref)){
                                                           orders.remove(j);
                                                           orders.put(newobj);
                                                           System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                       }else if(status == 3 && oldRef.equals(ref)){
                                                           orders.remove(j);
                                                           orders.put(newobj);
                                                           System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                       }
                                                   }
                                               }else{
                                                   orders.put(newobj);
                                               }

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
                                        /**
                                         * get orders informations by terminal and by status
                                         */
                                        JsonObjectRequest orderRequest = new JsonObjectRequest
                                                (Request.Method.GET, Globales.baseUrl + "api/order/get/by/mns/"+terminalIMEI+"/"+selectedStatus, null, new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {

                                                        try {
                                                            orders = new JSONArray();
                                                            JSONArray values = response.getJSONArray("data");
                                                            Log.i("ORDER_INFO", values.toString());

                                                            for(int i=0; i<values.length();i++){
                                                                JSONObject obj = values.getJSONObject(i);
                                                                JSONObject newobj = new JSONObject();
                                                                newobj.put("order_ref", obj.getString("order_ref"));
                                                                newobj.put("order_status", obj.getString("order_status"));
                                                                newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                                newobj.put("terminal", obj.getString("macadress"));
                                                                newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                                if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                                                    Integer status = Integer.parseInt(obj.getString("order_status"));
                                                                    String  ref = obj.getString("order_ref");
                                                                    for(int j=0; j<orders.length(); j++){
                                                                        JSONObject oneorder = orders.getJSONObject(j);
                                                                        Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                                                        String oldRef = oneorder.getString("order_ref");
                                                                        if(status > oldStatus && oldRef.equals(ref)){
                                                                            orders.remove(j);
                                                                            orders.put(newobj);
                                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                                        }else if(status == -1 && oldRef.equals(ref)){
                                                                            orders.remove(j);
                                                                            orders.put(newobj);
                                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                                        }else if(status == 3 && oldRef.equals(ref)){
                                                                            orders.remove(j);
                                                                            orders.put(newobj);
                                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                                        }
                                                                    }
                                                                }else{
                                                                    orders.put(newobj);
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
                                        /**
                                         * get orders informations by terminal
                                         */
                                        JsonObjectRequest orderRequest = new JsonObjectRequest
                                                (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/macadd/"+terminalIMEI, null, new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {

                                                        try {
                                                            orders = new JSONArray();

                                                            JSONArray values = response.getJSONArray("data");
                                                            Log.i("ORDER_INFO", values.toString());

                                                            for(int i=0; i<values.length();i++){
                                                                JSONObject obj = values.getJSONObject(i);
                                                                JSONObject newobj = new JSONObject();
                                                                newobj.put("order_ref", obj.getString("order_ref"));
                                                                newobj.put("order_status", obj.getString("order_status"));
                                                                newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                                newobj.put("terminal", obj.getString("macadress"));
                                                                newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                                if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                                                    Integer status = Integer.parseInt(obj.getString("order_status"));
                                                                    String  ref = obj.getString("order_ref");
                                                                    for(int j=0; j<orders.length(); j++){
                                                                        JSONObject oneorder = orders.getJSONObject(j);
                                                                        Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                                                        String oldRef = oneorder.getString("order_ref");
                                                                        if(status > oldStatus && oldRef.equals(ref)){
                                                                            orders.remove(j);
                                                                            orders.put(newobj);
                                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                                        }else if(status == -1 && oldRef.equals(ref)){
                                                                            orders.remove(j);
                                                                            orders.put(newobj);
                                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                                        }else if(status == 3 && oldRef.equals(ref)){
                                                                            orders.remove(j);
                                                                            orders.put(newobj);
                                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                                        }
                                                                    }
                                                                }else{
                                                                    orders.put(newobj);
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

        list.add(0, "Par status");
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
                        /**
                         * get orders informations by terminal
                         */
                        JsonObjectRequest orderRequest = new JsonObjectRequest
                                (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/macadd/"+terminalIMEI, null, new Response.Listener<JSONObject>() {
                                    @TargetApi(Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {
                                            orders = new JSONArray();
                                            JSONArray values = response.getJSONArray("data");
                                            Log.i("ORDER_INFO", values.toString());

                                            for(int i=0; i<values.length();i++){
                                                JSONObject obj = values.getJSONObject(i);
                                                JSONObject newobj = new JSONObject();
                                                newobj.put("order_ref", obj.getString("order_ref"));
                                                newobj.put("order_status", obj.getString("order_status"));
                                                newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                newobj.put("terminal", obj.getString("macadress"));
                                                newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                                    Integer status = Integer.parseInt(obj.getString("order_status"));
                                                    String  ref = obj.getString("order_ref");
                                                    for(int j=0; j<orders.length(); j++){
                                                        JSONObject oneorder = orders.getJSONObject(j);
                                                        Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                                        String oldRef = oneorder.getString("order_ref");
                                                        if(status > oldStatus && oldRef.equals(ref)){
                                                            orders.remove(j);
                                                            orders.put(newobj);
                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                        }else if(status == -1 && oldRef.equals(ref)){
                                                            orders.remove(j);
                                                            orders.put(newobj);
                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                        }else if(status == 3 && oldRef.equals(ref)){
                                                            orders.remove(j);
                                                            orders.put(newobj);
                                                            System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                        }
                                                    }
                                                }else{
                                                    orders.put(newobj);
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
                            /**
                             * get orders informations by terminal and by status
                             */
                            JsonObjectRequest orderRequest = new JsonObjectRequest
                                    (Request.Method.GET, Globales.baseUrl + "api/order/get/by/mns/"+terminalIMEI+"/"+selectedStatus, null, new Response.Listener<JSONObject>() {
                                        @TargetApi(Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {
                                                orders = new JSONArray();
                                                JSONArray values = response.getJSONArray("data");
                                                Log.i("ORDER_INFO", values.toString());

                                                for(int i=0; i<values.length();i++){
                                                    JSONObject obj = values.getJSONObject(i);
                                                    JSONObject newobj = new JSONObject();
                                                    newobj.put("order_ref", obj.getString("order_ref"));
                                                    newobj.put("order_status", obj.getString("order_status"));
                                                    newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                    newobj.put("terminal", obj.getString("macadress"));
                                                    newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                    if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                                        Integer status = Integer.parseInt(obj.getString("order_status"));
                                                        String  ref = obj.getString("order_ref");
                                                        for(int j=0; j<orders.length(); j++){
                                                            JSONObject oneorder = orders.getJSONObject(j);
                                                            Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                                            String oldRef = oneorder.getString("order_ref");
                                                            if(status > oldStatus && oldRef.equals(ref)){
                                                                orders.remove(j);
                                                                orders.put(newobj);
                                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                            }else if(status == -1 && oldRef.equals(ref)){
                                                                orders.remove(j);
                                                                orders.put(newobj);
                                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                            }else if(status == 3 && oldRef.equals(ref)){
                                                                orders.remove(j);
                                                                orders.put(newobj);
                                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                            }
                                                        }
                                                    }else{
                                                        orders.put(newobj);
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
                            /**
                             * get orders informations by status
                             */
                            JsonObjectRequest orderRequest = new JsonObjectRequest
                                    (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/status/"+selectedStatus, null, new Response.Listener<JSONObject>() {
                                        @TargetApi(Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {
                                                orders = new JSONArray();
                                                JSONArray values = response.getJSONArray("data");
                                                Log.i("ORDER_INFO", values.toString());

                                                for(int i=0; i<values.length();i++){
                                                    JSONObject obj = values.getJSONObject(i);
                                                    JSONObject newobj = new JSONObject();
                                                    newobj.put("order_ref", obj.getString("order_ref"));
                                                    newobj.put("order_status", obj.getString("order_status"));
                                                    newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                    newobj.put("terminal", obj.getString("macadress"));
                                                    newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                    if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                                        Integer status = Integer.parseInt(obj.getString("order_status"));
                                                        String  ref = obj.getString("order_ref");
                                                        for(int j=0; j<orders.length(); j++){
                                                            JSONObject oneorder = orders.getJSONObject(j);
                                                            Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                                            String oldRef = oneorder.getString("order_ref");
                                                            if(status > oldStatus && oldRef.equals(ref)){
                                                                orders.remove(j);
                                                                orders.put(newobj);
                                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                            }else if(status == -1 && oldRef.equals(ref)){
                                                                orders.remove(j);
                                                                orders.put(newobj);
                                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                            }else if(status == 3 && oldRef.equals(ref)){
                                                                orders.remove(j);
                                                                orders.put(newobj);
                                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                            }
                                                        }
                                                    }else{
                                                        orders.put(newobj);
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
                    (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/entity/"+restId, null, new Response.Listener<JSONObject>() {
                        @TargetApi(Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                orders = new JSONArray();
                                JSONArray values = response.getJSONArray("data");
                                Log.i("ORDER_INFO", values.toString());
                                editor = pref.edit();
                                editor.putString("clickedchannel", restId);
                                editor.apply();
                                for(int i=0; i<values.length();i++){
                                    JSONObject obj = values.getJSONObject(i);
                                    JSONObject newobj = new JSONObject();
                                    newobj.put("order_ref", obj.getString("order_ref"));
                                    newobj.put("terminal", obj.getString("macadress"));
                                    newobj.put("order_status", obj.getString("order_status"));
                                    newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));
                                    newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));

                                    if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                        Integer status = Integer.parseInt(obj.getString("order_status"));
                                        String  ref = obj.getString("order_ref");
                                        for(int j=0; j<orders.length(); j++){
                                            JSONObject oneorder = orders.getJSONObject(j);
                                            Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                            String oldRef = oneorder.getString("order_ref");
                                            if(status > oldStatus && oldRef.equals(ref)){
                                                orders.remove(j);
                                                orders.put(newobj);
                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                            }else if(status == -1 && oldRef.equals(ref)){
                                                orders.remove(j);
                                                orders.put(newobj);
                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                            }else if(status == 3 && oldRef.equals(ref)){
                                                orders.remove(j);
                                                orders.put(newobj);
                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                            }
                                        }
                                    }else{
                                        orders.put(newobj);
                                    }

                                }

                                System.out.println("orders "+orders.toString());
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

        vDateOne = (TextView) promptView.findViewById(R.id.onedate);
        vDate1 = (TextView) promptView.findViewById(R.id.date1);
        vTime1 = (TextView) promptView.findViewById(R.id.time1);
        vDate2 = (TextView) promptView.findViewById(R.id.date2);
        vTime2 = (TextView) promptView.findViewById(R.id.time2);

        deviceSpinner = (Spinner) promptView.findViewById(R.id.device);
        statusSpinner = (Spinner) promptView.findViewById(R.id.status);

        vDateOne.setText(data);
        vDate1.setText(data);
        vTime1.setText(time);
        vDate2.setText(data);
        vTime2.setText(time);

        vDateOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerFragment();
                // TODO Auto-generated method stub
                DatePickerFragment dte = DatePickerFragment.newInstance();
                dte.setCallBack(onDateOne);
                dte.show(getSupportFragmentManager().beginTransaction(), "DatePickerFragment");
            }
        });
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
    DatePickerDialog.OnDateSetListener onDateOne = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            vDateOne.setText(sdf.format(newDate.getTime()));
        }
    };
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
            int hour = hourOfDay-1;
            vTime1.setText(hour+":"+minute);
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
                (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/entity/"+restId, null, new Response.Listener<JSONObject>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONObject response) {
                        orders = new JSONArray();
                        try {
                            JSONArray values = response.getJSONArray("data");
                            Log.i("ORDER_INFO", values.toString());
                            for(int i=0; i<values.length();i++){
                                    JSONObject obj = values.getJSONObject(i);

                                JSONObject newobj = new JSONObject();

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
                                        newobj.put("order_ref", obj.getString("order_ref"));
                                        newobj.put("order_status", obj.getString("order_status"));
                                        newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                        newobj.put("terminal", obj.getString("macadress"));
                                        newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                        if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                            Integer status = Integer.parseInt(obj.getString("order_status"));
                                            String  ref = obj.getString("order_ref");
                                            for(int j=0; j<orders.length(); j++){
                                                JSONObject oneorder = orders.getJSONObject(j);
                                                Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                                String oldRef = oneorder.getString("order_ref");
                                                if(status > oldStatus && oldRef.equals(ref)){
                                                    orders.remove(j);
                                                    orders.put(newobj);
                                                    System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                }else if(status == -1 && oldRef.equals(ref)){
                                                    orders.remove(j);
                                                    orders.put(newobj);
                                                    System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                }else if(status == 3 && oldRef.equals(ref)){
                                                    orders.remove(j);
                                                    orders.put(newobj);
                                                    System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                                }
                                            }
                                        }else{
                                            orders.put(newobj);
                                        }

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
    private void getOrdersbyDate(){
        final String date1 = vDateOne.getText().toString();
        /**
         * get all orders by entity
         */
        JsonObjectRequest orderRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/entity/"+restId, null, new Response.Listener<JSONObject>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONObject response) {
                        orders = new JSONArray();
                        try {
                            JSONArray values = response.getJSONArray("data");
                            for(int i=0; i<values.length();i++){
                                JSONObject obj = values.getJSONObject(i);
                                JSONObject newobj = new JSONObject();

                                String datetime1 = date1;

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                                Date first = sdf.parse(datetime1);

                                String order_date = obj.getString("status_report_timestamp");
                                Date orderDate = sdf.parse(order_date);

                                //afficher toutes les commandes passées ce jour
                                if(first.getTime()==orderDate.getTime())
                                {
                                    newobj.put("order_ref", obj.getString("order_ref"));
                                    newobj.put("order_status", obj.getString("order_status"));
                                    newobj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                    newobj.put("terminal", obj.getString("macadress"));
                                    newobj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                    if (orders.toString().contains("\"order_ref\":\""+obj.getString("order_ref")+"\"")){
                                        Integer status = Integer.parseInt(obj.getString("order_status"));
                                        String  ref = obj.getString("order_ref");
                                        for(int j=0; j<orders.length(); j++){
                                            JSONObject oneorder = orders.getJSONObject(j);
                                            Integer oldStatus = Integer.parseInt(oneorder.getString("order_status"));
                                            String oldRef = oneorder.getString("order_ref");
                                            if(status > oldStatus && oldRef.equals(ref)){
                                                orders.remove(j);
                                                orders.put(newobj);
                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                            }else if(status == -1 && oldRef.equals(ref)){
                                                orders.remove(j);
                                                orders.put(newobj);
                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                            }else if(status == 3 && oldRef.equals(ref)){
                                                orders.remove(j);
                                                orders.put(newobj);
                                                System.out.println("status orders " + oldRef + " "+ref+" "+ status);
                                            }
                                        }
                                    }else{
                                        orders.put(newobj);
                                    }

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
