package com.wynd.app.wyndterminalpocket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
                    JsonObjectRequest terminalRequest = new JsonObjectRequest
                            (Request.Method.GET, Globales.baseUrl + "api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    terminals = new JSONArray();
                                    try {
                                        JSONArray values = response.getJSONArray("data");
                                        for (int i = 0; i < values.length(); i++) {

                                            JSONObject object = values.getJSONObject(i);
                                            terminals.put(object);
                                        }
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

                    Volley.newRequestQueue(getContext()).add(terminalRequest);
                } else {

                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("name");
                            if (item.equals(name)) {
                                selectedID = jsonArray.getJSONObject(i).getString("id");
                                terminals = new JSONArray();

                                /**
                                 * show terminal's selected informations
                                 */
                                JsonObjectRequest userRequest = new JsonObjectRequest
                                        (Request.Method.GET, Globales.baseUrl + "api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    JSONArray values = response.getJSONArray("data");
                                                    for (int i = 0; i < values.length(); i++) {

                                                        JSONObject object = values.getJSONObject(i);
                                                        if(selectedID.equals(object.getString("channelID"))){
                                                            terminals.put(object);
                                                        }
                                                    }
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
    private List<TerminalInfo> createList(JSONArray jsonArray) {

        List<TerminalInfo> result = new ArrayList<TerminalInfo>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

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


                ti.restaurant = (json_data.isNull("channelName") ? "" : json_data.getString("channelName"));
                ti.terminalStatus = (json_data.isNull("terminalStatus") ? "" : json_data.getString("terminalStatus"));
                ti.terminalStatusUpdateTime = (json_data.isNull("terminalLastUpdated") ? "" : json_data.getString("terminalLastUpdated"));
                ti.channel_id = (json_data.isNull("channelID") ? "" : json_data.getString("channelID"));

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

                //time configuration
                try{

                    Date date1 = sdf.parse(currentDateandTime);
                    Date date2 = sdf.parse(ti.terminalStatusUpdateTime);

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

//                    mNotificationManager =
//                            (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//                    if(ti.terminalStatus.equalsIgnoreCase("0")) {
//                        NotificationCompat.Builder mBuilder =
//                                new NotificationCompat.Builder(getActivity())
//                                        .setSmallIcon(R.drawable.ic_terminal)
//                                        .setContentTitle(ti.restaurant +" HS! "+ti.uuid)
//                                        .setContentText("OFF depuis " +elapsedDays+"j "+elapsedHours+"h "+ elapsedMinutes + "min" + elapsedSeconds + "s");
//                        Intent resultIntent = new Intent(getActivity(), TerminalsFragment.class);
//
//                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
//                        stackBuilder.addParentStack(TerminalsFragment.class);
//                        stackBuilder.addNextIntent(resultIntent);
//                        PendingIntent resultPendingIntent =
//                                stackBuilder.getPendingIntent(
//                                        0,
//                                        PendingIntent.FLAG_UPDATE_CURRENT
//                                );
//                        mBuilder.setContentIntent(resultPendingIntent);
//
//                        mNotificationManager.notify(i, mBuilder.build());
//                    }else{
//                        mNotificationManager.cancel(i);
//                    }
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
                    Log.e("Time error", e.toString());
                }

                result.add(ti);
            }

        }catch (JSONException e){
            Log.e("JSON parsing error", e.toString());
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
                    JsonObjectRequest terminalRequest = new JsonObjectRequest
                            (Request.Method.GET, Globales.baseUrl + "api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    terminals = new JSONArray();
                                    try {
                                        JSONArray values = response.getJSONArray("data");
                                        for (int i = 0; i < values.length(); i++) {

                                            JSONObject object = values.getJSONObject(i);
                                            terminals.put(object);
                                        }
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

                    Volley.newRequestQueue(getContext()).add(terminalRequest);
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
}
