package com.arteriatech.geotrack.rspl.SPGeo.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.SPGeo.database.DatabaseHelperGeo;
import com.arteriatech.geotrack.rspl.SPGeo.database.LocationBean;
/*import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;*/
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by devdeeds.com on 27-09-2017.
 */

public class LocationMonitoringService extends Service {
    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    private static final String TAG = LocationMonitoringService.class.getSimpleName();
    private static final String JOB_TAG = "MyJobService";
   // JobParameters jobParametersGlobal = null;
    GoogleApiClient mLocationClient = null;
    LocationRequest mLocationRequest = new LocationRequest();
  //  private FirebaseJobDispatcher mDispatcher;
    private HashMap<String, String> mapTable;
    private Runnable runnable;
    private Handler handler = new Handler();
    private LocationManager locationManager;
    //    private String latitude = "";
//    private String longitude = "";
    private String currentDate = "";
    private String currentDateTimeString = "";
    private String doc_no = "";
    private String mStrAppBackground = "";


    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest.setInterval(Constants.LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);


        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }*/
    private String mStrbatterLevel = "";

    /*@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * LOCATION CALLBACKS
     */
   /* @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.d(TAG, "== Error On onConnected() Permission not granted");
            //Permission not granted by user so cancel the further execution.

            onLocationFailed("Location permission not granted");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

       *//* Log.d(TAG, "Location update started ..............: ");
        if (this.runnable != null) {
            this.handler.removeCallbacks(this.runnable);
            this.runnable = null;
        }

        Log.d(TAG, "runnable started");
        this.runnable = new Runnable() {
            public void run() {
                jobFinished(jobParametersGlobal, true);
                LogManager.writeLogInfo(getString((R.string.unable_to_get_location)));
            }

        };
        this.handler.postDelayed(this.runnable, 5000L);*//*
        Log.d(TAG, "Connected to Google API");
    }*/

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
   /* @Override
    public void onConnectionSuspended(int i) {
        onLocationFailed("Connection Suspended");
        Log.d(TAG, "Connection suspended");
    }

    //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");


        if (location != null) {
            Log.d(TAG, "== location != null");

            //Send result to activities
//            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }

    }*/

    public static void locationLog(String message) {
        Log.d(TAG, Constants.LOCATION_LOG + message);
        LogManager.writeLogError(Constants.LOCATION_LOG + message);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

   /* private void onLocationFailed(String errMsg) {
        Log.d(TAG, "Error :" + errMsg);
        jobFinished(jobParametersGlobal, true);

    }*/

   /* @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
        onLocationFailed("Failed to connect to Google API");

    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        Log.d("LocationServiceCapture", "Job Repeating Clicked");
//        Constants.isAlarmScheduled=false;
      //  mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
//        initializeLocationManager();

       /* DatabaseHelperGeo databaseHelper = new DatabaseHelperGeo(this);
        Date dateMillSec = new Date();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(dateMillSec);
        ServiceStartStopBean startStopBean = new ServiceStartStopBean(currentDateTimeString, "ServiceRepeating");
        databaseHelper.createRecordService(startStopBean);*/
//        mTimer = new Timer();
//        mTimer.schedule(new TimerTaskToGetLocation(),5,notify_interval);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessageToUI(String lat, String lng) {

        Log.d(TAG, "Sending info...Latitide :" + lat + " Longtidude :" + lng);

       /* Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/

//        this.latitude = lat;
//        this.longitude = lng;
        doc_no = (System.currentTimeMillis() + "");

        Date dateMillSec = new Date();
        currentDateTimeString = DateFormat.getDateTimeInstance().format(dateMillSec);
        Log.d(TAG, "DateandTime : " + currentDateTimeString);
        currentDate = new SimpleDateFormat("yyyy-MM-dd").format(dateMillSec);
        Log.d(TAG, "Date : " + currentDate);

//        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
//        Date date = new Date();
//        String currentDateTimeString = null;
//        try {
//            currentDateTimeString = dateFormat.format(date);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Log.d(TAG, currentDateTimeString);
        boolean mBoolAppBackGround = isAppIsInBackground(getApplicationContext());
        if (mBoolAppBackGround) {
            mStrAppBackground = "Background";
        } else {
            mStrAppBackground = "Foreground";
        }
       /* if(isAppRunning(getApplicationContext(), "tracklocation.devdeeds.com.tracklocationproject")){
            mStrAppBackground = "Foreground";
        }else{
            mStrAppBackground = "Background";
        }*/
        batteryLevel(lat, lng);
/*
        LocationBean locationBean = new LocationBean("", "", lat + "", lng + "", currentDate + "T00:00:00", "Time", "X", doc_no, currentDateTimeString, mStrAppBackground,mStrbatterLevel);
        databaseHelper.createRecord(locationBean);*/

//        diconectLocation();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("HH");
        SimpleDateFormat fmt1 = new SimpleDateFormat("MM");
        int cur_time1 = Integer.parseInt(fmt1.format(cal.getTime()));
        int cur_time = Integer.parseInt(fmt.format(cal.getTime()));
        if (cur_time >= 20) {
            /*ServiceStartStopBean startStopBean = new ServiceStartStopBean(currentDateTimeString, "ServiceStop");
            databaseHelper.createRecordService(startStopBean);*/
            stopAndFlushJobs();
          //  Constants.setScheduleAlaram(this, 8, 00, 00, 1);


        } else {
            locationLog("service rescheduled");
//            Constants.isAlarmScheduled=false;
            //  jobFinished(jobParametersGlobal, false);
        }
      //  Constants.getDataFromSqliteDB(this,null);
    }

    private void stopAndFlushJobs() {
     /*   jobFinished(jobParametersGlobal, false);
        mDispatcher.cancel(JOB_TAG);*/
        Log.d("LocationServiceCapture", "Job Stopped Clicked");
//        Constants.isAlarmScheduled=true;
        locationLog("service stopped and rescheduled");
        stopService(new Intent(this, LocationMonitoringService.class));
    }


    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            if (runningProcesses != null && runningProcesses.size() > 0) {
                for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String activeProcess : processInfo.pkgList) {
                            if (activeProcess.equals(context.getPackageName())) {
                                isInBackground = false;
                            }
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

   /* @Override
    public boolean onStartJob(JobParameters jobParameters) {
        this.jobParametersGlobal = jobParameters;


        locationLog("service started");
       *//* mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();



        mLocationRequest.setInterval(Constants.LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);


        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


        mLocationRequest.setPriority(priority);
        mLocationClient.connect();*//*
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(getBaseContext(),"Unable to start your service. Please enable your location permission",Toast.LENGTH_LONG).show();
//            jobFinished(jobParametersGlobal, true);
//        }else {
        new LocationUsingGoogleAPi(LocationMonitoringService.this, new LocationServiceInterface() {
            public void location(boolean status, Location location, String errorMsg, int errorCode, int currentAttempt) {
                boolean isNetworkAvailable = isNetworkAvailable(LocationMonitoringService.this);
                if (status) {
                    Log.d("LocationUtils", "latitude: " + location.getLatitude() + " longitude: " + location.getLongitude() + " Accuracy :" + location.getAccuracy());
                    locationLog("location captured successfully");
                    locationLog("LocationUtils latitude: " + location.getLatitude() + " longitude: " + location.getLongitude() + " Accuracy :" + location.getAccuracy());
                    sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                } else {
                    locationLog("location failed");
                    if (errorCode == 502) {
                        LogManager.writeLogError("Location :Unable to connect google play service");
                    } else if (errorCode == 504) {
                        LogManager.writeLogError("Location :Connection with google play service is suspended");
                    } else if (errorCode == 508) {
                        String networkMsg = isNetworkAvailable ? "with mobile data" : "without mobile data ";
                        LogManager.writeLogError("Location :Unable to get location from google play service " + networkMsg);
                    } else {
                        LogManager.writeLogError("Location :other google play service error " + errorMsg);
                    }
                    jobFinished(jobParametersGlobal, true);
                    *//*if (LocationUtils.isGPSEnabled(LocationMonitoringService.this) && LocationUtils.isHighAccuracy(LocationMonitoringService.this)) {
                        Location locations = UtilConstants.getLocationNoDialog(LocationMonitoringService.this);
                        if (locations != null) {
                            LocationModel locationModelx = new LocationModel();
                            locationModelx.setLocation(locations);
                            locationModelx.setInternetAvailable(isNetworkAvailable);
                            locationModelx.setLocationFrom("L");
                            if (locationInterface != null) {
                                locationInterface.location(true, locationModelx, "", 0);
                                return;
                            }
                        } else {
                            jobFinished(jobParametersGlobal, true);
                            LogManager.writeLogError("Location :Unable to get location from Location Manager");
                        }*//*


                }


            }
        }, 1);
//        }

        return true;

    }
*/
   /* @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled!");

       *//* DatabaseHelperGeo databaseHelper = new DatabaseHelperGeo(this);
        Date dateMillSec = new Date();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(dateMillSec);
        ServiceStartStopBean startStopBean = new ServiceStartStopBean(currentDateTimeString, "ServiceStopped");
        databaseHelper.createRecordService(startStopBean);*//*
//        diconectLocation();
        return false;
    }
*/
    private void diconectLocation() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.disconnect();
        }
    }

    private void initializeLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void batteryLevel(final String lat, final String lng) {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

                int batteryLevel = -1;
                int batteryScale = 1;
                if (intent != null) {
                    batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, batteryLevel);
                    batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, batteryScale);
                }
                float newbatteryValue = batteryLevel / (float) batteryScale * 100;


                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                mStrbatterLevel = String.valueOf(level);
                locationLog("battery percentage received");
                LocationBean locationBean = new LocationBean("", "", lat + "", lng + "", currentDate + "T00:00:00", "Time", "X", doc_no, currentDateTimeString, mStrAppBackground, mStrbatterLevel,"");
                DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(context);
                databaseHelper.createRecord(locationBean);
                locationLog("battery percentage received inserted in DB old/new Value(" + mStrbatterLevel + "/" + newbatteryValue + ")");
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

}



