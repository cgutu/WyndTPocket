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
    private Button fab;
    private TextView vDate1, vDate2, vTime1, vTime2;
    private LinearLayout rlChart;
    private LineChart lineChart;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                Historique.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        errorText = (TextView) findViewById(R.id.error);
        fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorText.setVisibility(View.GONE);
              loadMap();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uuid = intent.getStringExtra("terminalUuid");
        id = intent.getStringExtra("terminalID");
        channel_id = intent.getStringExtra("channelID");

        initViews();
        rlChart = (LinearLayout) findViewById(R.id.layoutChart);
        lineChart = new LineChart(getApplicationContext());
    }
    private void loadMap(){


        rlChart.setVisibility(View.VISIBLE);

        final String date1 = vDate1.getText().toString();
        final String date2 = vDate2.getText().toString();

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/status/history/terminal/"+id+"/"+date1+"/"+date2, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if(response.getString("result").equals("success")) {

                                JSONArray values = response.getJSONArray("data");

                                ArrayList<String> xAxis = new ArrayList<>();
                                ArrayList<LineDataSet> dataSets = null;
                                ArrayList<Entry> valueSet1 = new ArrayList<>();

                                for (int j = 0; j < values.length(); j++) {
                                    String status = values.getJSONObject(j).getString("status");
                                    String date = values.getJSONObject(j).getString("timestamp");
                                    Float f = Float.parseFloat(status);
                                    xAxis.add(date);
                                    Entry v1e1 = new Entry(f, j);
                                    valueSet1.add(v1e1);
                                }

                                LineDataSet barDataSet1 = new LineDataSet(valueSet1, "Status");
                                barDataSet1.setColor(Color.WHITE);

                                barDataSet1.setDrawCubic(false);
                                barDataSet1.setDrawCircleHole(false);
                                barDataSet1.setLineWidth(1.8f);
                                barDataSet1.setCircleSize(3.6f);
                                barDataSet1.setHighLightColor(Color.RED);
                                barDataSet1.setValueTextColor(Color.WHITE);
                                barDataSet1.disableDashedLine();

                                dataSets = new ArrayList<>();
                                dataSets.add(barDataSet1);

                                XAxis xAxis1 = lineChart.getXAxis();
                                xAxis1.setDrawGridLines(false);

                                YAxis yAxis = lineChart.getAxisLeft();
                                yAxis.setDrawGridLines(false);

                                LineData data = new LineData(xAxis, dataSets);
                                lineChart.setData(data);
                                lineChart.setDescription("Chart");
                                lineChart.animateXY(2000, 2000);
                                lineChart.setMinimumWidth(1200);
                                lineChart.setMinimumHeight(1200);
                                lineChart.setGridBackgroundColor(Color.GRAY);
                                lineChart.invalidate();
                                lineChart.setPadding(10, 10, 10, 10);

                                lineChart.setScaleEnabled(true);
                                lineChart.setPinchZoom(true);
                                //lineChart.getAxisRight().setEnabled(false);

                                lineChart.setVisibility(View.VISIBLE);
                                if (lineChart.getParent() != null)
                                    ((ViewGroup) lineChart.getParent()).removeView(lineChart); // <- fix
                                rlChart.addView(lineChart);

                            }else{
                                lineChart.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                                errorText.setError("Veuillez choisir une autre date");
                                errorText.setTextColor(Color.RED);
                                View focusView = errorText;
                                focusView.requestFocus();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        lineChart.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setError("Veuillez choisir une autre date");
                        errorText.setTextColor(Color.RED);
                        View focusView = errorText;
                        focusView.requestFocus();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        //Volley.newRequestQueue(getApplicationContext()).add(request);
        ApplicationController.getInstance().addToRequestQueue(request, "request");
    }
    private void initViews(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat stm = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        String time = stm.format(new Date());
        String data = sdf.format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = sdf.format(cal.getTime());
        vDate1 = (TextView) findViewById(R.id.date1);
        vTime1 = (TextView) findViewById(R.id.time1);
        vDate2 = (TextView) findViewById(R.id.date2);
        vTime2 = (TextView) findViewById(R.id.time2);

        vDate1.setText(yesterday);
        vTime1.setText(time);
        vDate2.setText(data);
        vTime2.setText(time);

        vTime1.setVisibility(View.GONE);
        vTime2.setVisibility(View.GONE);
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
}
