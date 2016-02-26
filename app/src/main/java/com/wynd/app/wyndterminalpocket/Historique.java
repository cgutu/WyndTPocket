package com.wynd.app.wyndterminalpocket;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Historique extends AppCompatActivity {

    private String uuid, id, channel_id;
    private  FloatingActionButton fab;
    private TextView vDate1, vDate2, vTime1, vTime2;
    private BarChart chart;
    private RelativeLayout rlChart;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                Historique.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              loadMap();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uuid = intent.getStringExtra("terminalUuid");
        id = intent.getStringExtra("terminalID");
        channel_id = intent.getStringExtra("channelID");

        System.out.println("terminal info " + uuid + " id " + id + " channelid " + channel_id);

        initViews();
        chart = new BarChart(getApplicationContext());
        rlChart = (RelativeLayout) findViewById(R.id.layoutChart);
        lineChart = new LineChart(getApplicationContext());
    }
    private void loadMap(){

        final String date1 = vDate1.getText().toString();
        final String date2 = vDate2.getText().toString();

        System.out.println("dates "+date1 + " "+date2+ " "+id);


        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/status/history/terminal/"+id+"/"+date1+"/"+date2, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");

                            ArrayList<String> xValues = new ArrayList<String>();
                            ArrayList<Entry> entries = new ArrayList<>();

                            for(int i=0; i<values.length(); i++){
                                String status = values.getJSONObject(i).getString("t_status");
                                String date = values.getJSONObject(i).getString("t_last_seen");
                                System.out.println("date " + date);
                                Float f= Float.parseFloat(status);
                                entries.add(new Entry(f, i));
                                xValues.add(date);
                            }

                            LineDataSet dataset = new LineDataSet(entries, "1 = ON / 0 = OFF");

                            LineData data = new LineData(xValues, dataset);

                            lineChart.setData(data);
                            lineChart.setDescription("Device status");
                            lineChart.setMinimumWidth(1300);
                            lineChart.setMinimumHeight(1200);


                            YAxis mYAxis = lineChart.getAxisLeft();
                            mYAxis.setShowOnlyMinMax(true);
                            mYAxis.setAxisMaxValue(1f);
                            mYAxis.setAxisMinValue(0f);

                            YAxis rAxis = lineChart.getAxisRight();
                            rAxis.setShowOnlyMinMax(true);
                            rAxis.setAxisMaxValue(1f);
                            rAxis.setAxisMinValue(0f);
//                            mYAxis.setDrawAxisLine(false);
//                            mYAxis.setDrawGridLines(false);
//                            mYAxis.setStartAtZero(false);

                            XAxis xAxis = lineChart.getXAxis();
                            xAxis.setTextColor(Color.RED);
                            xAxis.setSpaceBetweenLabels(4);
                            dataset.setColors(new int[]{R.color.red}, getApplicationContext());

                            lineChart.animateXY(3000, 3000);
                            data.setHighlightEnabled(true);

                            lineChart.setTouchEnabled(true);


                            if(lineChart.getParent()!=null)
                                ((ViewGroup)lineChart.getParent()).removeView(lineChart); // <- fix
                            rlChart.addView(lineChart);


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
                params.put("Api-User", Globales.API_TERMINAL);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void initViews(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String data = sdf.format(new Date());

        vDate1 = (TextView) findViewById(R.id.date1);
        vDate2 = (TextView) findViewById(R.id.date2);

        vDate1.setText(data);
        vDate2.setText(data);

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
}
