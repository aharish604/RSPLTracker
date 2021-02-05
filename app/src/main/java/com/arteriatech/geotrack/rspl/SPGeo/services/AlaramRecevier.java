package com.arteriatech.geotrack.rspl.SPGeo.services;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.arteriatech.geotrack.rspl.SPGeo.database.DatabaseHelperGeo;
import com.arteriatech.geotrack.rspl.SPGeo.database.ServiceStartStopBean;
import com.arteriatech.geotrack.rspl.backgroundlocationtracker.TrackerService;
/*import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.rspl.geotracker.registration.RegistrationActivity;*/


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class AlaramRecevier extends BroadcastReceiver {
    private static final String JOB_TAG = "MyJobService";
  //  private FirebaseJobDispatcher mDispatcher;
    File extraLogPath = null;
    private int mJobId = 999;


    public AlaramRecevier() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, TrackerService.class));
        }else{
            context.startService(new Intent(context, TrackerService.class));

        }
        ConstantsUtils.startAutoSyncLocation(context,true);

        DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(context);
        Date dateMillSec = new Date();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(dateMillSec);
        ServiceStartStopBean startStopBean = new ServiceStartStopBean(currentDateTimeString, "ServiceStart");
        databaseHelper.createRecordService(startStopBean);
//        databaseHelper.getData();
        Log.d(TAG, "Job Started Clicked");
//        mDispatcher.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL,60*10, 60*15);
       /* mDispatcher.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL, 60 * 5, 60 * 6);//Repeat interval time
        Job myJob = mDispatcher.newJobBuilder()
                .setService(LocationMonitoringService.class)
                .setTag(JOB_TAG)
                .setRecurring(true)
//                .setTrigger(Trigger.executionWindow(60*10, 60*15))
                .setTrigger(Trigger.executionWindow(60 * 5, 60 * 6))//First trigger time
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
//                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
//                .setTrigger(Constants.periodicTrigger(60 * 15, 1)) // repeated every 20 seconds with 1 second of tollerance
                .build();

        mDispatcher.mustSchedule(myJob);*/
    //    ConstantsUtils.startAutoSync(context,true);
      /*  try {
            Intent intentLogView = new Intent(context, RegistrationActivity.class);
            context.startActivity(intentLogView);
        }catch (Exception e){
            e.printStackTrace();
        }*/
      try {
          extralogToStorage("AlaramRecevier triggered to start service",context);
      }catch (Exception e){
          e.printStackTrace();
      }

       /* // wrap your stuff in a componentName
        ComponentName mServiceComponent = new ComponentName(context, TrackerService.class);
// set up conditions for the job
        JobInfo task = new JobInfo.Builder(mJobId, mServiceComponent)
                //.setPeriodic(mIntervalMillis)
                .setRequiresCharging(true) // default is "false"
               // .setRequiredNetworkCapabilities(JobInfo.NetworkType.UNMETERED) // Parameter may be "ANY", "NONE" (=default) or "UNMETERED"
                .build();
// inform the system of the job
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(task);*/



    }
    private void extralogToStorage(String data, Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String currentDateandTime = sdf.format(new Date());

                extraLogPath = new File(Environment.getExternalStoragePublicDirectory(""),
                        "transport-tracker-service.txt");
                if (!extraLogPath.exists()) {
                    try {
                        extraLogPath.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (extraLogPath.exists()) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(extraLogPath.getAbsolutePath(), true));
                        writer.write(currentDateandTime + "::" + data);
                        writer.newLine();
                        writer.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Log file error", e);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    /*@Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(this);
        Date dateMillSec = new Date();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(dateMillSec);
        ServiceStartStopBean startStopBean = new ServiceStartStopBean(currentDateTimeString, "ServiceStart");
        databaseHelper.createRecordService(startStopBean);
        databaseHelper.getData();
        Log.d(TAG, "Job Started Clicked");
//        mDispatcher.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL,60*10, 60*15);
        mDispatcher.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL, 60 * 5, 60 * 6);//Repeat interval time
        Job myJob = mDispatcher.newJobBuilder()
                .setService(LocationMonitoringService.class)
                .setTag(JOB_TAG)
                .setRecurring(true)
//                .setTrigger(Trigger.executionWindow(60*10, 60*15))
                .setTrigger(Trigger.executionWindow(60 * 5, 60 * 6))//First trigger time
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
//                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
//                .setTrigger(Constants.periodicTrigger(60 * 15, 1)) // repeated every 20 seconds with 1 second of tollerance
                .build();

        mDispatcher.mustSchedule(myJob);
    }*/
}
