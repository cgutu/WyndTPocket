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

/**
 * Created by cgutu on 01/03/16.
 */
public class MyReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID_01 = 1;

    @Override
    public void onReceive (Context context, Intent intent) {

        postNotification(context);
    }

    public void postNotification(Context c) {
        Context context = c.getApplicationContext();
        Intent intent01 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent01 = PendingIntent.getActivity(c, 1, intent01, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
        builder.setSmallIcon(R.drawable.ic_terminal);
        builder.setContentIntent(pendingIntent01);
        builder.setAutoCancel(true);
        builder.setContentTitle("title");
        builder.setContentText("test");
        builder.setSubText("click here");
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_01, builder.build());
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(c, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
