/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arteriatech.geotrack.rspl.backgroundlocationtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.arteriatech.geotrack.rspl.Constants;

public class TrackerBroadcastReceiver extends BroadcastReceiver {

    public TrackerBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Constants.writeLogsToInternalStorage(context,"Tracker Broadcast Receiver after reboot");
        Constants.writeLogsToInternalStorage(context,"Tracker Broadcast Receiver after reboot : "+intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Constants.setScheduleAlaram(context, 0, 0, 05, 0);
            SharedPreferences sharedPerf = context.getSharedPreferences("mSFAGeoPreference", 0);
            SharedPreferences.Editor editor = sharedPerf.edit();
            editor.putBoolean("BS",true );
            editor.apply();
            /*Intent start = new Intent(context, TrackerActivity.class);
            start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(start);*/
        }
    }
}
