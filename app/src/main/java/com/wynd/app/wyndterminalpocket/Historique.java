package com.wynd.app.wyndterminalpocket;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Historique extends AppCompatActivity {

    private String uuid, id, channel_id;
    private  FloatingActionButton fab;
    private TextView vDate1, vDate2, vTime1, vTime2;

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uuid = intent.getStringExtra("terminalUuid");
        id = intent.getStringExtra("terminalID");
        channel_id = intent.getStringExtra("channelID");

        System.out.println("terminal info " + uuid + " id " + id + " channelid " + channel_id);

        initViews();
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
        vTime1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimePickerFragment();
                // TODO Auto-generated method stub
                TimePickerFragment tme = TimePickerFragment.newInstance();
                tme.setCallBack(onTime);
                tme.show(getSupportFragmentManager().beginTransaction(), "TimePickerFragment");
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
        vTime2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimePickerFragment();
                // TODO Auto-generated method stub
                TimePickerFragment tme = TimePickerFragment.newInstance();
                tme.setCallBack(onTime2);
                tme.show(getSupportFragmentManager().beginTransaction(), "TimePickerFragment");
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
    TimePickerDialog.OnTimeSetListener onTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


            vTime1.setText(hourOfDay+":"+minute);
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
    TimePickerDialog.OnTimeSetListener onTime2 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            vTime2.setText(hourOfDay+":"+minute);
        }

    };
}
