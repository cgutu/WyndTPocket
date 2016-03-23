package com.wynd.app.wyndterminalpocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoOrder extends AppCompatActivity {

    private String order_ref, desired_delivery;
    private TextView vRef, vStatus, vDelivery, vTimestamp, vImei;
    private List<String> list;
    private ArrayAdapter adapter;
    private ListView listview;
    private String[] test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        order_ref = intent.getStringExtra("order_ref");
        desired_delivery = intent.getStringExtra("order_delivery");

        vRef = (TextView) findViewById(R.id.order_ref);
        vDelivery = (TextView) findViewById(R.id.order_delivery);
        listview = (ListView) findViewById(R.id.listview);


         vRef.setText(order_ref);
        vDelivery.setText(desired_delivery);

        System.out.println("order reference "+order_ref);
        getOrderInfo();
    }
    private void getOrderInfo() {
        JsonObjectRequest deviceRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl + "api/order/get/single/"+order_ref, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            final ArrayList<String> list = new ArrayList<String>();
                            for (int i = 0; i < values.length(); i++) {
                                JSONObject info = values.getJSONObject(i);
                                list.add(info.getString("macadress") + " status: " + info.getString("order_status") + " " + info.getString("status_report_timestamp"));
                            }
                            //Collections.sort(list);
                            final ArrayAdapter adapter = new ArrayAdapter(InfoOrder.this,
                                    android.R.layout.simple_list_item_1, list);
                            listview.setAdapter(adapter);

                            System.out.println("order infos " + values);

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

}
