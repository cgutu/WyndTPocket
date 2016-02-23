package com.wynd.app.wyndterminalpocket;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
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
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoriqueFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoriqueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoriqueFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private String uuid, id, channel_id, userID, parentID, permission, rest_channel, myuserID, EntityInfo, selectedID, terminalID;
    private FloatingActionButton fab;
    private TextView vDate1, vDate2, vTime1, vTime2;
    private View rootView;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private JSONArray infosArray = new JSONArray();
    private JSONArray parents = new JSONArray();
    private JSONArray datas = new JSONArray();
    private Spinner restSpinner, parentSpinner, deviceSpinner;
    private ArrayAdapter<String> dataAdapter;
    private JSONArray terminals = new JSONArray();
    private BarChart chart;
    private LineChart lineChart;

    private RelativeLayout rlChart;

    public HistoriqueFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoriqueFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoriqueFragment newInstance(String param1, String param2) {
        HistoriqueFragment fragment = new HistoriqueFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getActivity(),
                HistoriqueFragment.class));

        pref = getContext().getSharedPreferences("Infos", 0);

        userID = pref.getString("userID", "");
        parentID = pref.getString("parentID", "");
        permission = pref.getString("roles", "");
        rest_channel = pref.getString("rest_channel", "");
        myuserID =  pref.getString("myuserID", "");

        EntityInfo = pref.getString("EntityInfo", "");

        try{
            infosArray = new JSONArray(EntityInfo);
            JSONObject infoObject;

            for (int j = 0; j < infosArray.length(); j++) {
                infoObject = infosArray.getJSONObject(j);
                final String parentID= infoObject.isNull("res_parent_id") ? "" : infoObject.getString("res_parent_id");

                System.out.println("parentID "+parentID);

                //show parents which I am allow to see
                JsonObjectRequest parentRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl+"api/user/get/parent/info/"+parentID+"/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    JSONArray values = response.getJSONArray("data");

                                    for(int i=0; i<values.length(); i++){
                                        parents.put(values.getJSONObject(i));
                                    }

                                    System.out.println("parents " + parents);
                                    addParent(parents);
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

                Volley.newRequestQueue(getContext()).add(parentRequest);
            }
        }catch (JSONException e){

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_historique, container, false);

        initViews();
        restSpinner = (Spinner) rootView.findViewById(R.id.rest_channel_id);
        parentSpinner = (Spinner) rootView.findViewById(R.id.parent);
        deviceSpinner = (Spinner) rootView.findViewById(R.id.device);
        rlChart = (RelativeLayout) rootView.findViewById(R.id.layoutChart);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chart.invalidate();
                loadMap();
            }
        });


       // chart = (BarChart) rootView.findViewById(R.id.chart);

        chart = new BarChart(getActivity());
        lineChart = new LineChart(getActivity());
        return rootView;
    }

    private void loadMap(){

        //getDate
        rlChart.setVisibility(View.VISIBLE);

        final String date1 = vDate1.getText().toString();
       final String date2 = vDate2.getText().toString();

        System.out.println("dates "+date1 + " "+date2+ " "+terminalID);

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/status/history/terminal/"+terminalID+"/"+date1+"/"+date2, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");

                            ArrayList<String> xValues = new ArrayList<String>();
                            ArrayList<BarEntry> entries = new ArrayList<>();

                            //first date, la plus petite t_last_seen
                            //end date, la plus grand t_last_seen
                            try{
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                                Date startDate = formatter.parse(date1);
                                Date endDate = formatter.parse(date2);
                                Calendar start = Calendar.getInstance();
                                start.setTime(startDate);
                                Calendar end = Calendar.getInstance();
                                end.setTime(endDate);

                                for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
                                    // Do your job here with `date`.
                                    System.out.println(date);
                                    xValues.add(""+date);
                                }
                            }catch (ParseException e){

                            }
                            for(int i=0; i<xValues.size(); i++){
                                entries.add(new BarEntry(1, i));
                            }
//                            for(int i=0; i<values.length(); i++){
//                                datas.put(values.getJSONObject(i));
//                                String status = values.getJSONObject(i).getString("t_status");
//                                Float f= Float.parseFloat(status);
////                                if(status.equals("0")){
////                                    entries.add(new BarEntry(f, i));
////                                    xValues.add(values.getJSONObject(i).getString("t_last_seen"));
////                                }
//                            }
                            System.out.println("map data "+datas);

                            BarDataSet dataset = new BarDataSet(entries, "# 1 = ON / 0 = OFF");

                            BarData data = new BarData(xValues, dataset);

                            chart.setData(data);
                            chart.setDescription("Device status");
                            chart.setMinimumWidth(1200);
                            chart.setMinimumHeight(1000);

                            if(chart.getParent()!=null)
                                ((ViewGroup)chart.getParent()).removeView(chart); // <- fix
                            rlChart.addView(chart);

 //                           ArrayList<Entry> entries = new ArrayList<>();
//                            entries.add(new Entry(0f, 0));
//                            entries.add(new Entry(0f, 1));
//                            entries.add(new Entry(1f, 2));
//                            entries.add(new Entry(0f, 3));
//                            entries.add(new Entry(1f, 4));
//                            entries.add(new Entry(1f, 5));

//                            ArrayList<String> labels = new ArrayList<String>();
//
//                            for(int i=0; i<values.length(); i++){
//                                datas.put(values.getJSONObject(i));
//                                String status = values.getJSONObject(i).getString("t_status");
//                                Float f= Float.parseFloat(status);
//                                entries.add(new Entry(f, i));
//
//                                labels.add(values.getJSONObject(i).getString("t_last_seen"));
//                            }
//
//                            LineDataSet dataset = new LineDataSet(entries, "# of Calls");
//
//                            LineData data = new LineData(labels, dataset);
//                            lineChart.setData(data); // set the data and list of lables into chart
//
//                            lineChart.setMinimumWidth(1400);
//                            lineChart.setMinimumHeight(1000);
//                            if(lineChart.getParent()!=null)
//                                ((ViewGroup)lineChart.getParent()).removeView(lineChart); // <- fix
//                            rlChart.addView(lineChart);


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

                System.out.println("api infos sent" + Globales.API_TERMINAL + " "+Globales.API_HASH);
                params.put("Api-User", Globales.API_TERMINAL);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getContext()).add(request);
    }
    private void initViews(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat stm = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        String time = stm.format(new Date());
        String data = sdf.format(new Date());

        vDate1 = (TextView) rootView.findViewById(R.id.date1);
       // vTime1 = (TextView) rootView.findViewById(R.id.time1);
        vDate2 = (TextView) rootView.findViewById(R.id.date2);
        //vTime2 = (TextView) rootView.findViewById(R.id.time2);

        vDate1.setText(data);
      //  vTime1.setText(time);
        vDate2.setText(data);
      //  vTime2.setText(time);

        vDate1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerFragment();
                // TODO Auto-generated method stub
                DatePickerFragment dte = DatePickerFragment.newInstance();
                dte.setCallBack(onDate);
                dte.show(getFragmentManager().beginTransaction(), "DatePickerFragment");
            }
        });
//        vTime1.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                new TimePickerFragment();
//                // TODO Auto-generated method stub
//                TimePickerFragment tme = TimePickerFragment.newInstance();
//                tme.setCallBack(onTime);
//                tme.show(getFragmentManager().beginTransaction(), "TimePickerFragment");
//            }
//        });
        vDate2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerFragment();
                // TODO Auto-generated method stub
                DatePickerFragment dte = DatePickerFragment.newInstance();
                dte.setCallBack(onDate2);
                dte.show(getFragmentManager().beginTransaction(), "DatePickerFragment");
            }
        });
//        vTime2.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                new TimePickerFragment();
//                // TODO Auto-generated method stub
//                TimePickerFragment tme = TimePickerFragment.newInstance();
//                tme.setCallBack(onTime2);
//                tme.show(getFragmentManager().beginTransaction(), "TimePickerFragment");
//            }
//        });
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
//    TimePickerDialog.OnTimeSetListener onTime = new TimePickerDialog.OnTimeSetListener() {
//        @Override
//        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//
//
//            vTime1.setText(hourOfDay+":"+minute);
//        }
//
//    };
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
//    TimePickerDialog.OnTimeSetListener onTime2 = new TimePickerDialog.OnTimeSetListener() {
//        @Override
//        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//            vTime2.setText(hourOfDay+":"+minute);
//        }
//
//    };

    private void addParent(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Select franchise");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("resturant_name");
                if(!list.contains(name)){
                    list.add("" + name);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("list " + "listist.size() : " + list.size());

        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parentSpinner.setAdapter(dataAdapter);


        parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub

                Object item = arg0.getItemAtPosition(arg2);
                System.out.println("item "+item + " position "+arg2);
                if(arg2 == 0){
                    restSpinner.setVisibility(View.GONE);
                    deviceSpinner.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                    rlChart.setVisibility(View.GONE);
                }else{
                    restSpinner.setVisibility(View.VISIBLE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("resturant_name");
                            if(item.equals(name)){
                                final String selectedID = jsonArray.getJSONObject(i).getString("id");
                                //autocompleter la liste des restaurants après avoir sélctionner le parent

                                //get entity of selected parent
                                final JSONArray entities = new JSONArray();
                                JsonObjectRequest entityRequest = new JsonObjectRequest
                                        (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/by/parent/"+selectedID+"/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    JSONArray values = response.getJSONArray("data");
                                                    for (int i = 0; i < values.length(); i++) {
                                                        JSONObject restaurants = values.getJSONObject(i);
                                                        entities.put(restaurants);

                                                    }
                                                    System.out.println("entities " + entities);
                                                    addList(entities);

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

                                        System.out.println("api infos sent" + Globales.API_USER + " " + Globales.API_HASH);
                                        params.put("Api-User", Globales.API_USER);
                                        params.put("Api-Hash", Globales.API_HASH);

                                        return params;
                                    }
                                };

                                Volley.newRequestQueue(getContext()).add(entityRequest);


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
    private void addList(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Select a restaurant");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("name");
                list.add("" + name);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("list " + "listist.size() : " + list.size());

        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restSpinner.setAdapter(dataAdapter);


        restSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){
                    deviceSpinner.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                    rlChart.setVisibility(View.GONE);
                }else{
                    deviceSpinner.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                    rlChart.setVisibility(View.GONE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("name");
                            if(item.equals(name)){
                                System.out.println("name ok "+item);
                                selectedID = jsonArray.getJSONObject(i).getString("id");
                                System.out.println("item id "+selectedID);

                                terminals = new JSONArray();
                                //get user of selected restaurant
                                JsonObjectRequest userRequest = new JsonObjectRequest
                                        (Request.Method.GET, Globales.baseUrl + "api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    JSONArray values = response.getJSONArray("data");
                                                    System.out.println("response " + response);

                                                    for (int i = 0; i < values.length(); i++) {

                                                        JSONObject terminal = values.getJSONObject(i);
                                                        if(!terminal.getString("channelID").isEmpty() && terminal.getString("channelID").equals(selectedID)){
                                                            terminals.put(terminal);
                                                        }
                                                    }
                                                    System.out.println("terminals " + terminals);
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

                                        System.out.println("api infos sent" + Globales.API_USER + " " + Globales.API_HASH);
                                        params.put("Api-User", Globales.API_USER);
                                        params.put("Api-Hash", Globales.API_HASH);

                                        return params;
                                    }
                                };

                                Volley.newRequestQueue(getContext()).add(userRequest);
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
    private void addTerminal(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Select a terminal");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("terminalMacadd");
                list.add("" + name);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("list " + "listist.size() : " + list.size());

        dataAdapter = new ArrayAdapter<String>(getActivity(),
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
                   //map visibility gone
                    fab.setVisibility(View.GONE);
                    rlChart.setVisibility(View.GONE);
                }else{
                    //map visibility visible
                    fab.setVisibility(View.VISIBLE);
                    rlChart.setVisibility(View.GONE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("terminalMacadd");
                            if(item.equals(name)){
                                System.out.println("name ok " + item);
                                terminalID = jsonArray.getJSONObject(i).getString("terminalID");
                                System.out.println("item id " + selectedID);
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
}
