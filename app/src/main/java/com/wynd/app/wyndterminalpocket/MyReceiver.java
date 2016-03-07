package com.wynd.app.wyndterminalpocket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cgutu on 01/03/16.
 */
public class MyReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID_01 = 1;
    private String uuid;

    @Override
    public void onReceive (Context context, Intent intent) {

        Intent background = new Intent(context, BackgroundService.class);
        context.startService(background);

        //postNotification(context);
    }

    public void postNotification(final Context c) {
        Context context = c.getApplicationContext();
        Intent intent01 = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent01 = PendingIntent.getActivity(c, 1, intent01, 0);
        final NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        JsonObjectRequest terminalRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/all", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray values = response.getJSONArray("data");
                            for (int i = 0; i < values.length(); i++) {

                                JSONObject terminalObject = values.getJSONObject(i);
                                if(terminalObject.getString("terminalStatus").equals("0")){
                                    uuid = terminalObject.isNull("terminalMacadd") ? "" : terminalObject.getString("terminalMacadd");
                                    String channel = terminalObject.isNull("channelName") ? "" : terminalObject.getString("channelName");

                                    System.out.println("c "+uuid);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
                                    builder.setSmallIcon(R.drawable.ic_terminal);
                                    builder.setContentIntent(pendingIntent01);
                                    builder.setAutoCancel(true);
                                    builder.setContentTitle(channel);
                                    builder.setContentText(" "+uuid+" est OFF");
                                    //builder.setSubText("click here");

                                    notificationManager.notify(i, builder.build());
                                    try {
                                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        Ringtone r = RingtoneManager.getRingtone(c, notification);
                                        r.play();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    notificationManager.cancel(i);
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
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Api-User", Globales.API_USER);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(c).add(terminalRequest);
    }
}
