package com.wynd.app.wyndterminalpocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Created by cgutu on 01/03/16.
 */
public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {

        Intent background = new Intent(context, BackgroundService.class);
        context.startService(background);

    }
}
