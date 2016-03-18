package com.wynd.app.wyndterminalpocket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TerminalsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SharedPreferences pref;
    private Spinner restSpinner, parentSpinner;
    private View rootView;
    String userID, parentID, permission, rest_channel, restID, myuserID;
    private JSONArray chains = new JSONArray();
    private JSONArray terminals = null;
    private String selectedID, EntityInfo;
    private ArrayAdapter<String> dataAdapter;
    private RecyclerView recList;
    private TerminalAdapter ta;
    private List<TerminalInfo> terminal;
    private FloatingActionButton fab;
    private JSONArray infosArray = new JSONArray();
    private JSONArray parents = new JSONArray();
    private NotificationManager mNotificationManager= null;
    private View mProgressView, mListView;

    public TerminalsFragment() {
        // Required empty public constructor
    }

    public static TerminalsFragment newInstance(String param1, String param2) {
        TerminalsFragment fragment = new TerminalsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * get stored informations
         */
        pref = getContext().getSharedPreferences("Infos", 0);
        myuserID =  pref.getString("myuserID", "");
        EntityInfo = pref.getString("EntityInfo", "");

        try{
            infosArray = new JSONArray(EntityInfo);
            JSONObject infoObject;

            for (int j = 0; j < infosArray.length(); j++) {
                infoObject = infosArray.getJSONObject(j);
                final String parentID= infoObject.isNull("res_parent_id") ? "" : infoObject.getString("res_parent_id");

                /**
                 * show parents allowed to see
                 */
                JsonObjectRequest parentRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/all/parents/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    JSONArray values = response.getJSONArray("data");

                                    for(int i=0; i<values.length(); i++){
                                        parents.put(values.getJSONObject(i));
                                    }
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

        rootView = inflater.inflate(R.layout.fragment_terminals, container, false);

        /**
         * init the views
         */
        mListView = rootView.findViewById(R.id.cardList);
        mProgressView = rootView.findViewById(R.id.progress);
        restSpinner = (Spinner) rootView.findViewById(R.id.rest_channel_id);
        parentSpinner = (Spinner) rootView.findViewById(R.id.parent);
        recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        /**
         * create the list adapter
         */
        terminal = new ArrayList<>();
        ta = new TerminalAdapter(terminal);
        recList.setAdapter(ta);

        /**
         * create a new user
         */
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AddTerminal.class);
                i.putExtra("restId", selectedID);
                startActivity(i);
            }
        });

        return rootView;
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private void addList(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Séléctionner un restaurant");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("name");
                list.add("" + name);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        /**
         * add items to spinner list
         */

        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restSpinner.setAdapter(dataAdapter);

        /**
         * adapt views on restaurant selected
         */
        restSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (arg2 == 0) {
                    //afficher tous les terminaux de cette franchise
                    /**
                     * show terminal's informations
                     */
                  //  getTerminalTask();
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("name");
                            if (item.equals(name)) {
                                selectedID = jsonArray.getJSONObject(i).getString("id");
                                /**
                                 * show terminal's selected informations
                                 */
                                getTerminalTaskByChannel();
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
    private List<TerminalInfo> createList(JSONArray jsonArray) {

        List<TerminalInfo> result = new ArrayList<TerminalInfo>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        Log.i("INFO", jsonArray.toString());

        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                TerminalInfo ti = new TerminalInfo();
                JSONObject json_data = jsonArray.getJSONObject(i);

                ti.terminalActive = (json_data.isNull("terminalActive") ? "" : json_data.getString("terminalActive"));
                ti.id = (json_data.isNull("terminalID") ? "" : json_data.getString("terminalID"));
                ti.registerTimestamp = (json_data.isNull("registerTimestamp") ? "" : json_data.getString("registerTimestamp"));
                if( ti.terminalActive.equals("0")){
                    ti.uuid = (json_data.isNull("terminalMacadd") ? "" :  json_data.getString("terminalMacadd")+" (inactive)");
                }else{
                    ti.uuid = (json_data.isNull("terminalMacadd") ? "" :  json_data.getString("terminalMacadd"));
                }

                ti.nb_orders = (json_data.isNull("nb_orders") ? "" :  json_data.getString("nb_orders") + " commandes");
                System.out.println("count orders " + ti.nb_orders);
                ti.restaurant = (json_data.isNull("channelName") ? "" : json_data.getString("channelName"));
                ti.terminalStatus = (json_data.isNull("terminalStatus") ? "" : json_data.getString("terminalStatus"));
                ti.terminalStatusUpdateTime = (json_data.isNull("terminalLastUpdated") ? "" : json_data.getString("terminalLastUpdated"));
                ti.channel_id = (json_data.isNull("channelID") ? "" : json_data.getString("channelID"));

                ti.channel = (json_data.isNull("channelName") ? "" : json_data.getString("channelName"));

                if(!json_data.getString("terminalInfo").isEmpty() && json_data.getString("terminalInfo") != null){
                    String terminalInfo = json_data.isNull("terminalInfo") ? "" : json_data.getString("terminalInfo");
                    JSONObject infoObject = new JSONObject(terminalInfo);

                    ti.terminalUser = (infoObject.isNull("username") ? "" : infoObject.getString("username"));
                    ti.entity_parent = (infoObject.isNull("entity_parent") ? "" : infoObject.getString("entity_parent"));
                    ti.entity_id = (infoObject.isNull("entity_id") ? "" : infoObject.getString("entity_id"));
                    ti.entity_label = (infoObject.isNull("entity_label") ? "" : infoObject.getString("entity_label"));
                    ti.email = (infoObject.isNull("email") ? "" : infoObject.getString("email"));
                    ti.phone = (infoObject.isNull("phone") ? "" : infoObject.getString("phone"));
                    ti.apk_version = (infoObject.isNull("apk_version") ? "" : infoObject.getString("apk_version"));
                }else{
                    ti.terminalUser = "";
                    ti.entity_parent = "";
                    ti.entity_id = "";
                    ti.entity_label = "";
                    ti.email = "";
                    ti.phone = "";
                    ti.apk_version = "";
                }

                /**
                 * configure a time laps for getting ON/OFF
                 */
                try{

                    Date date1 = sdf.parse(currentDateandTime);
                    Date date2 = sdf.parse(ti.terminalStatusUpdateTime);

                    System.out.println("date 1"+date1);
                    System.out.println("date 2"+date2);

                    long diffInMs = date1.getTime() - date2.getTime();
                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long daysInMilli = hoursInMilli * 24;

                    long elapsedDays = diffInMs / daysInMilli;
                    diffInMs = diffInMs % daysInMilli;

                    long elapsedHours = diffInMs / hoursInMilli;
                    diffInMs = diffInMs % hoursInMilli;

                    long elapsedMinutes = diffInMs / minutesInMilli;
                    diffInMs = diffInMs % minutesInMilli;

                    long elapsedSeconds = diffInMs / secondsInMilli;

                    mNotificationManager =
                            (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);


                    System.out.println("elapsedHours "+elapsedHours+" elapsedMinutes "+elapsedMinutes+ " elapsedSeconds "+elapsedSeconds);
                    /*if(ti.terminalStatus.equalsIgnoreCase("0")) {
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(this)
                                        .setSmallIcon(R.drawable.ic_terminal)
                                        .setContentTitle(ti.restaurant +" HS! "+ti.uuid)
                                        .setContentText("OFF depuis " +elapsedDays+"j "+elapsedHours+"h "+ elapsedMinutes + "min" + elapsedSeconds + "s");
                        Intent resultIntent = new Intent(this, Terminals.class);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                        stackBuilder.addParentStack(Terminals.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        mNotificationManager.notify(i, mBuilder.build());
                    }else{
                        mNotificationManager.cancel(i);
                    }*/
                    if(elapsedDays>0) {
                        ti.terminalStatusUpdateTime = elapsedDays + "j " + elapsedHours + "h " + elapsedMinutes + "min "+elapsedSeconds+"s";
                    }else{
                        if(elapsedHours>0) {
                            ti.terminalStatusUpdateTime = elapsedHours + "h " + elapsedMinutes + "min " + elapsedSeconds + "s";
                        }else {
                            if(elapsedMinutes>0) {
                                ti.terminalStatusUpdateTime = elapsedMinutes + "min "+elapsedSeconds+"s";
                            }else {
                                ti.terminalStatusUpdateTime = elapsedSeconds + "s";
                            }
                        }
                    }


                }catch (Exception e){
                    Log.e("Date parsing error", e.toString());
                }

                result.add(ti);
            }

        }catch (JSONException e){
            Log.e("JSON Parsing error", e.toString());
        }

        return result;
    }
    private void addParent(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Séléctionner une franchise");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("parent_name");
                if(!list.contains(name)){
                    list.add("" + name);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * add items to spinner
         */
        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parentSpinner.setAdapter(dataAdapter);


        /**
         * adapt view by item
         */
        parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){
                    //afficher tous les terminaux (peu importe la franchise et le restaurant)
                    /**
                     * show terminal's informations
                     */
                  //  getTerminalTask();
                    restSpinner.setVisibility(View.GONE);
                }else{
                    restSpinner.setVisibility(View.VISIBLE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("parent_name");
                            if(item.equals(name)){
                                final String selectedID = jsonArray.getJSONObject(i).getString("id");

                                final JSONArray entities = new JSONArray();
                                JsonObjectRequest entityRequest = new JsonObjectRequest
                                        (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/by/parent/" + selectedID + "/user/" + myuserID, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    JSONArray values = response.getJSONArray("data");
                                                    for (int i = 0; i < values.length(); i++) {
                                                        JSONObject restaurants = values.getJSONObject(i);
                                                        entities.put(restaurants);

                                                    }
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
    private void getTerminalTask(){

        terminals = new JSONArray();

        /**
         * get all terminals request
         */
        final JsonObjectRequest terminalRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            for (int i = 0; i < values.length(); i++) {

                                final JSONObject terminalObject = values.getJSONObject(i);
                                final String channel = terminalObject.isNull("channelName") ? "" : terminalObject.getString("channelName");

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {

                                        try{
                                            /**
                                             * get orders informations by entity
                                             */
                                            JsonObjectRequest orderRequest = new JsonObjectRequest
                                                    (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/macadd/"+terminalObject.getString("terminalMacadd"), null, new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {

                                                            try {
                                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);

                                                                //date1
                                                                Calendar date1Cal = Calendar.getInstance();
                                                                date1Cal.setTime(new Date());

                                                                int dayDate1Cal = date1Cal.get(Calendar.DAY_OF_MONTH);
                                                                int monthDate1Cal = date1Cal.get(Calendar.MONTH);
                                                                int yearDate1Cal = date1Cal.get(Calendar.YEAR);

                                                                String dayDate1String = String.valueOf(dayDate1Cal);
                                                                String monthDate1String = String.valueOf(monthDate1Cal + 1);
                                                                String yearDate1String = String.valueOf(yearDate1Cal);

                                                                System.out.println("currentdate 1 "+yearDate1String+"-"+monthDate1String+"-"+dayDate1String+"");
                                                                JSONArray orders = new JSONArray();
                                                                JSONArray values = response.getJSONArray("data");
                                                                for(int i=0; i<values.length();i++){
                                                                    JSONObject obj = values.getJSONObject(i);

                                                                    try{
                                                                        // Date date1 = sdf.parse(yearDate1String+"-"+monthDate1String+"-"+dayDate1String);
                                                                        Date date2 = sdf.parse(obj.getString("status_report_timestamp"));
                                                                        Calendar date2Cal = Calendar.getInstance();
                                                                        date2Cal.setTime(date2);

                                                                        int dayDate2Cal = date2Cal.get(Calendar.DAY_OF_MONTH);
                                                                        int monthDate2Cal = date2Cal.get(Calendar.MONTH);
                                                                        int yearDate2Cal = date2Cal.get(Calendar.YEAR);

                                                                        String dayDate2String = String.valueOf(dayDate2Cal);
                                                                        String monthDate2String = String.valueOf(monthDate2Cal + 1);
                                                                        String yearDate2String = String.valueOf(yearDate2Cal);

                                                                        //  date2 = sdf.parse(yearDate2String+"-"+monthDate2String+"-"+dayDate2String);
                                                                        System.out.println("currentdate 2 "+yearDate2String+"-"+monthDate2String+"-"+dayDate2String+"");

                                                                        String firstdate = yearDate1String+"-"+monthDate1String+"-"+dayDate1String;
                                                                        String seconddate = yearDate2String+"-"+monthDate2String+"-"+dayDate2String;
                                                                        System.out.println("date1 "+firstdate);
                                                                        System.out.println("date2 "+seconddate);

                                                                        if(firstdate.equals(seconddate)){
                                                                            Log.i("DATE", "dates are equals");

                                                                            obj.put("order_ref", obj.getString("order_ref"));
                                                                            obj.put("order_status", obj.getString("order_status"));
                                                                            obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                                            obj.put("terminal", obj.getString("macadress"));
                                                                            obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                                            orders.put(obj);
                                                                        }
                                                                    }catch (ParseException e){

                                                                    }

                                                                }
                                                                Integer count = orders.length();
                                                                Log.i("ORDER_INFO", count.toString() + " commandes");
                                                                terminalObject.put("nb_orders", count);

                                                                terminals.put(terminalObject);

                                                                ta = new TerminalAdapter(createList(terminals));
                                                                recList.setAdapter(ta);

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

                                            Volley.newRequestQueue(getActivity()).add(orderRequest);

                                        }catch (JSONException e){

                                        }

                                    }
                                }, 3000);



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
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getActivity()).add(terminalRequest);
    }

    private void getTerminalTaskByChannel(){
        terminals = new JSONArray();

        /**
         * get all terminals request
         */
        final JsonObjectRequest terminalRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            for (int i = 0; i < values.length(); i++) {

                                final JSONObject terminalObject = values.getJSONObject(i);
                                final String channel = terminalObject.isNull("channelName") ? "" : terminalObject.getString("channelName");
                                final String idchannel = terminalObject.isNull("channelID") ? "" : terminalObject.getString("channelID");

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {

                                        try{
                                            /**
                                             * get orders informations by entity
                                             */
                                            JsonObjectRequest orderRequest = new JsonObjectRequest
                                                    (Request.Method.GET, Globales.baseUrl + "/api/order/get/by/macadd/"+terminalObject.getString("terminalMacadd"), null, new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {

                                                            try {
                                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);

                                                                //date1
                                                                Calendar date1Cal = Calendar.getInstance();
                                                                date1Cal.setTime(new Date());

                                                                int dayDate1Cal = date1Cal.get(Calendar.DAY_OF_MONTH);
                                                                int monthDate1Cal = date1Cal.get(Calendar.MONTH);
                                                                int yearDate1Cal = date1Cal.get(Calendar.YEAR);

                                                                String dayDate1String = String.valueOf(dayDate1Cal);
                                                                String monthDate1String = String.valueOf(monthDate1Cal + 1);
                                                                String yearDate1String = String.valueOf(yearDate1Cal);

                                                                System.out.println("currentdate 1 "+yearDate1String+"-"+monthDate1String+"-"+dayDate1String+"");
                                                                JSONArray orders = new JSONArray();
                                                                JSONArray values = response.getJSONArray("data");
                                                                for(int i=0; i<values.length();i++){
                                                                    JSONObject obj = values.getJSONObject(i);

                                                                    try{
                                                                        // Date date1 = sdf.parse(yearDate1String+"-"+monthDate1String+"-"+dayDate1String);
                                                                        Date date2 = sdf.parse(obj.getString("status_report_timestamp"));
                                                                        Calendar date2Cal = Calendar.getInstance();
                                                                        date2Cal.setTime(date2);

                                                                        int dayDate2Cal = date2Cal.get(Calendar.DAY_OF_MONTH);
                                                                        int monthDate2Cal = date2Cal.get(Calendar.MONTH);
                                                                        int yearDate2Cal = date2Cal.get(Calendar.YEAR);

                                                                        String dayDate2String = String.valueOf(dayDate2Cal);
                                                                        String monthDate2String = String.valueOf(monthDate2Cal + 1);
                                                                        String yearDate2String = String.valueOf(yearDate2Cal);

                                                                        //  date2 = sdf.parse(yearDate2String+"-"+monthDate2String+"-"+dayDate2String);
                                                                        System.out.println("currentdate 2 "+yearDate2String+"-"+monthDate2String+"-"+dayDate2String+"");

                                                                        String firstdate = yearDate1String+"-"+monthDate1String+"-"+dayDate1String;
                                                                        String seconddate = yearDate2String+"-"+monthDate2String+"-"+dayDate2String;
                                                                        System.out.println("date1 "+firstdate);
                                                                        System.out.println("date2 "+seconddate);

                                                                        if(firstdate.equals(seconddate)){
                                                                            Log.i("DATE", "dates are equals");

                                                                            obj.put("order_ref", obj.getString("order_ref"));
                                                                            obj.put("order_status", obj.getString("order_status"));
                                                                            obj.put("order_desired_delivery", obj.getString("selected_delivery_time"));
                                                                            obj.put("terminal", obj.getString("macadress"));
                                                                            obj.put("status_report_timestamp", obj.getString("status_report_timestamp"));

                                                                            orders.put(obj);
                                                                        }
                                                                    }catch (ParseException e){

                                                                    }

                                                                }
                                                                Integer count = orders.length();
                                                                Log.i("ORDER_INFO", count.toString() + " commandes");
                                                                terminalObject.put("nb_orders", count);

                                                                if(!idchannel.isEmpty() && idchannel.equalsIgnoreCase(selectedID)){
                                                                    terminals.put(terminalObject);
                                                                }

                                                                showProgress(false);
                                                                ta = new TerminalAdapter(createList(terminals));
                                                                recList.setAdapter(ta);

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

                                            Volley.newRequestQueue(getActivity()).add(orderRequest);

                                        }catch (JSONException e){

                                        }

                                    }
                                }, 3000);



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
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getActivity()).add(terminalRequest);
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
