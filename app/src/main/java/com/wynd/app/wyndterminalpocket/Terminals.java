package com.wynd.app.wyndterminalpocket;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;

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

public class Terminals extends AppCompatActivity {

    private JSONArray terminals = new JSONArray();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String userID, parentID, permission, rest_channel, ID, restId;
    private String clickedChannel;
    private RecyclerView recList;
    private TerminalAdapter ta;
    private List<TerminalInfo> terminal;
    private String channelName, clickedChannelID, EntityInfo, restID;
    private NotificationManager mNotificationManager= null;
    private JSONArray infosArray = new JSONArray();
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminals);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                Terminals.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);

        terminal = new ArrayList<>();
        ta = new TerminalAdapter(terminal);
        recList.setAdapter(ta);

        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        pref = getApplicationContext().getSharedPreferences("Infos", 0);
        userID = pref.getString("userID", "");
        String savedChannel = pref.getString("clickedchannel", "");

        editor = pref.edit();
        editor.putString("Check", "exitterminals");
        editor.apply();

        Intent intent = getIntent();
        clickedChannel = intent.getStringExtra("channel");
        clickedChannelID = intent.getStringExtra("restId");

        if(clickedChannel == null){
            channelName = savedChannel;
        }else{
            channelName = clickedChannel;
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Terminals.this, AddTerminal.class);
                i.putExtra("restId",clickedChannelID);
                startActivity(i);
            }
        });

        EntityInfo = pref.getString("EntityInfo", "");
        JSONArray array = new JSONArray();
        try{
            infosArray = new JSONArray(EntityInfo);
            for (int j = 0; j < infosArray.length(); j++) {
                JSONObject infoObject = infosArray.getJSONObject(j);
                permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");
                array.put(permission);
            }
            int l = array.length();
            for(int i=0; i<l; i++){
                String value = array.getString(i);

                if(value.contains("3")){
                    fab.setVisibility(View.VISIBLE);
                }

            }

        }catch(JSONException e){

        }


        getList.run();


    }
    Runnable getList = new Runnable() {
        @Override
        public void run() {
            getTerminalTask();
            handler = new Handler();
            handler.postDelayed(getList, 60000);
        }
    };
    private void getTerminalTask(){

        terminals = new JSONArray();

        /**
         * get all terminals request
         */
        JsonObjectRequest terminalRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            for (int i = 0; i < values.length(); i++) {

                                JSONObject terminalObject = values.getJSONObject(i);
                                String channel = terminalObject.isNull("channelName") ? "" : terminalObject.getString("channelName");

                                if(!channel.isEmpty() && channel.equalsIgnoreCase(channelName)){
                                        editor = pref.edit();
                                        editor.putString("clickedchannel", channel);
                                        editor.apply();
                                    terminals.put(terminalObject);
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
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Api-User", Globales.API_TERMINAL);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(terminalRequest);
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

                /**
                 * configure a time laps for getting ON/OFF
                 */
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

                    mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    /**
                     * set a notification if the device is getting OFF / disable the notification when the device is getting ON
                     */
                    if(ti.terminalStatus.equalsIgnoreCase("0")) {

//                        Context context = getApplicationContext();
//                        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//                        Intent intent = new Intent(context, MyReceiver.class);
//                        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//                        alarmMgr.set(AlarmManager.RTC_WAKEUP, 0, alarmIntent) ;

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
                    }
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

}
