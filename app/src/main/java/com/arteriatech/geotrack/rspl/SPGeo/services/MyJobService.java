package com.arteriatech.geotrack.rspl.SPGeo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/*import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;*/

public class MyJobService extends Service {
    private static final String TAG = "MyJobService";

  /*  @Override
    public boolean onStartJob(JobParameters jobParameters) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.d(TAG, currentDateTimeString);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled!");
        return false;
    }*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}