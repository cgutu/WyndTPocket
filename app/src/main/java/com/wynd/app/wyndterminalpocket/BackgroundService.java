package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 04/03/16.
 */
import android.app.*;
import android.content.*;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BackgroundService extends Service {

    private boolean isRunning;
    private Context context;
    private Thread backgroundThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
    }

    private Runnable myTask = new Runnable() {
        public void run() {
            postNotification(context);
            stopSelf();
        }
    };

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }
    public void postNotification(final Context c) {

        System.out.println("notificate the user on background");
        Context context = c.getApplicationContext();

        SharedPreferences pref = context.getSharedPreferences("Infos", 0);
        String EntityInfo = pref.getString("EntityInfo", "");
        try{
            JSONArray infosArray = new JSONArray(EntityInfo);

            for (int j = 0; j < infosArray.length(); j++) {
                final JSONObject info = infosArray.getJSONObject(j);

                Intent intent01 = new Intent(context, MenuActivity.class);
                final PendingIntent pendingIntent01 = PendingIntent.getActivity(c, 1, intent01, 0);
                final NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
                builder.setSmallIcon(R.drawable.ic_terminal);
                builder.setContentIntent(pendingIntent01);

                JsonObjectRequest terminalRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl + "api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    JSONArray values = response.getJSONArray("data");
                                    for (int i = 0; i < values.length(); i++) {

                                        JSONObject terminalObject = values.getJSONObject(i);
                                        System.out.println("repeating notification");

                                        if(terminalObject.getString("channelID").equals(info.getString("resaturantChainID"))){
                                            if (terminalObject.getString("terminalStatus").equals("0")) {
                                                String uuid = terminalObject.isNull("terminalMacadd") ? "" : terminalObject.getString("terminalMacadd");
                                                String channel = terminalObject.isNull("channelName") ? "" : terminalObject.getString("channelName");
                                                String terminalStatusUpdateTime = (terminalObject.isNull("terminalLastUpdated") ? "" : terminalObject.getString("terminalLastUpdated"));

                                                /**
                                                 * configure a time laps for getting ON/OFF
                                                 */
                                                try {

                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    String currentDateandTime = sdf.format(new Date());
                                                    Date date1 = sdf.parse(currentDateandTime);
                                                    Date date2 = sdf.parse(terminalStatusUpdateTime);

                                                    System.out.println("date 1" + date1);
                                                    System.out.println("date 2" + date2);

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

                                                  //  builder.setAutoCancel(true);
                                                    builder.setContentTitle(channel + " HS ! " + uuid);
                                                    builder.setContentText("OFF depuis " + elapsedDays + "j " + elapsedHours + "h " + elapsedMinutes + "min " + elapsedSeconds + "s");

                                                    notificationManager.notify(i, builder.build());
                                                    try {
                                                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                        Ringtone r = RingtoneManager.getRingtone(c, notification);
                                                        r.play();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

//                                                   //send email
//                                                    Resources res = getResources();
//
//                                                    String msgTemplate = String.format(res.getString(R.string.device_off), username, uuid, channel);
//
//                                                    try {
//                                                        GmailSender sender = new GmailSender("peestashgirls", "peestash2015");
//                                                        sender.sendMail(channel + ": Terminal "+uuid + " déconnecté",
//                                                                msgTemplate,
//                                                                "peestashgirls@gmail.com",
//                                                                "cgutu@wynd.eu");
//
//                                                    } catch (Exception e) {
//                                                        Log.e("SendMail", e.getMessage(), e);
//                                                    }
//
//                                                    //send sms
//
//                                                    SmsManager smsManager = SmsManager.getDefault();
//                                                    smsManager.sendTextMessage("+33612491829", null, ""+channel +" : Terminal "+uuid+" déconnecté", null, null);
//                                                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();

                                                } catch (Exception e) {
                                                    Log.e("Date parsing error", e.toString());
                                                }
                                            } else {
                                                notificationManager.cancel(i);
                                            }
                                        }else{
                                        }

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
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Api-User", "admin");
                        params.put("Api-Hash", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4");

                        return params;
                    }
                };

               // Volley.newRequestQueue(c).add(terminalRequest);
                ApplicationController.getInstance().addToRequestQueue(terminalRequest, "terminalRequest");

            }
        }catch (JSONException e){

        }


    }


}