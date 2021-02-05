package com.arteriatech.geotrack.rspl.autosync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by E10953 on 05-08-2019.
 */

public class AutoSyncDataLocationAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 30001;

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DEBUG", "AutoSyncDataLocationAlarmReceiver triggered");

        Intent i = new Intent(context, AutoSyncLocationDataService.class);
        i.putExtra("foo", "alarm!!");
        // i.putExtra("receiver", MySimpleReceiver.setupServiceReceiver(context));
        AutoSyncLocationDataService.enqueueWork(context, i);
    }
}
