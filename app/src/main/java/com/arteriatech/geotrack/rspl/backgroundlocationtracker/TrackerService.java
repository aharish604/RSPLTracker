/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arteriatech.geotrack.rspl.backgroundlocationtracker;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.arteriatech.geotrack.rspl.R;
import com.arteriatech.geotrack.rspl.dashboard.MainMenu;
import com.arteriatech.mutils.BuildConfig;
import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.location.LocationUtils;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.mutils.registration.RegistrationModel;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.arteriatech.geotrack.rspl.ErrorBean;
import com.arteriatech.geotrack.GetDeviceInfoInterface;
import com.arteriatech.geotrack.rspl.SPGeo.database.DatabaseHelperGeo;
import com.arteriatech.geotrack.rspl.SPGeo.database.LocationBean;
import com.arteriatech.geotrack.rspl.appmanager.AppManager;
import com.arteriatech.geotrack.rspl.autosync.AutoSyncDataLocationAlarmReceiver;
import com.arteriatech.geotrack.rspl.database.EventDataSqlHelper;
import com.arteriatech.geotrack.rspl.database.EventUserDetail;
import com.arteriatech.geotrack.rspl.interfaces.MessageWithBooleanCallBack;
import com.arteriatech.geotrack.rspl.offline.OfflineErrorListener;
import com.arteriatech.geotrack.rspl.offline.OfflineGeoFlushListener;
import com.arteriatech.geotrack.rspl.offline.OfflineGeoRefreshListener;
import com.arteriatech.geotrack.rspl.offline.OfflineManager;
import com.arteriatech.geotrack.rspl.offline.OfflineStoreGeoListener;
import com.arteriatech.geotrack.rspl.registration.Configuration;
import com.arteriatech.geotrack.rspl.registration.RegistrationActivity;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
/*import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;*/
import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.maf.tools.logon.core.LogonCoreContext;
import com.sap.maf.tools.logon.core.LogonCoreException;
import com.sap.maf.tools.logon.core.LogonCoreListener;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.offline.ODataOfflineStore;
import com.sap.smp.client.odata.offline.ODataOfflineStoreOptions;
import com.sybase.persistence.DataVault;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class TrackerService extends Service implements LocationListener, LogonCoreListener {

    public static final String STATUS_INTENT = "status";
    // to check internet background starts
    static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    // to check internet background ends
    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 2;
    private static final int FOREGROUND_SERVICE_ID = 1;
    private static final int CONFIG_CACHE_EXPIRY = 600;  // 10 minutes.
    private static TrackerService instance = null;
    File path = null, extraLogPath = null;
    LatLng origin, destination;
    Polyline line;
    private GoogleApiClient mGoogleApiClient = null;
  //  private DatabaseReference mFirebaseTransportRef;
  //  private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private LinkedList<Map<String, Object>> mTransportStatuses = new LinkedList<>();
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private PowerManager.WakeLock mWakelock = null;
    private String currentDate = "";
    private String currentDateTimeString = "";
    private String mStrbatterLevel = "";
    private String mStrDistance = "";
    private String SMALLESTDISPLACEMENT = "";
    private String GEOSTRTTME = "";
    private String GEOENDTME = "";
    private int timeInterval = 60;
    private String doc_no = "";
    private int FASTEST_INTERVAL = 2000; // use whatever suits you
    private Location currentLocation = null;
    private long locationUpdatedAt = Long.MIN_VALUE;
    private SharedPreferences mPrefs;
    private AlertDialog.Builder builder;
    private AlertDialog alert = null;
    private SharedPreferences sharedPreferences = null;
    public static ODataOfflineStore offlineGeo;
    public static ODataOfflineStoreOptions optionsGeo = null;
    private static String geoStoreState;
    private boolean pingPong = false;
    private int Geocount = 0;
    EventDataSqlHelper events;
    public static SQLiteDatabase EventUserHandler;
    NotificationChannel channelGeo = null;
    Notification notification = null;
    NotificationManager managerGeo = null;
    private boolean offlinestoreOpen = false;
    private Handler mServiceHandler;

    private Handler heartBeathandler;
    private PowerManager hbPowerManager;
    private Location lastMockLocation;
    private int numGoodReadings;
    private boolean isMocking=false;
    private PendingIntent wakeupIntent;
    private String gpsStatusDL="0";//gps status for dataLog
    private String dateAsFileName = "";
    private final Runnable heartbeat = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void run() {
            try {
                extralogToStorage( "Heart keep live");
                if (hbPowerManager != null && hbPowerManager.isDeviceIdleMode()) {
                    Log.i("Poking location service","");
                    try {
                        wakeupIntent.send();
                    } catch (SecurityException | PendingIntent.CanceledException e) {
                        Log.i("Heartkeeplive failed", e.getMessage());
                    }
                }
                long secondsElapsed = 0;
                try {
                    secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - locationUpdatedAt);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_FINE_LOCATION") != 0 ||
                        ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_COARSE_LOCATION") != 0 ||
                    !LocationUtils.isGPSEnabled(TrackerService.this) ||
                        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(TrackerService.this) != 0) {
                    extralogToStorage( "check Self Permission is not enabled insert lat long zero");
                    insetLatLongWithZero();
                }/*else if(secondsElapsed>120){
                    extralogToStorage( "elapsed seconds is greater than 120 seconds insert lat long zero");
                    insetLatLongWithZero();
                }*/
                else if(mGoogleApiClient==null || !mGoogleApiClient.isConnected()){
                    extralogToStorage( "Google Api Client is null or Google Api Client is not connectec stop service");
                    stopSelf();
                }
                }finally {
                if (heartBeathandler != null) {
                    heartBeathandler.postDelayed(heartbeat, timeInterval*1000);
                }

            }
        }
    };


    private  boolean isLocationPlausible(Location location) {
        if (location == null) return false;

        boolean isMock =  (Build.VERSION.SDK_INT >= 18 && location.isFromMockProvider());
        if (isMock) {
            lastMockLocation = location;
            numGoodReadings = 0;
        } else
            numGoodReadings = Math.min(numGoodReadings + 1, 1000000); // Prevent overflow

        // We only clear that incident record after a significant show of good behavior
        if (numGoodReadings >= 20) lastMockLocation = null;

        // If there's nothing to compare against, we have to trust it
        if (lastMockLocation == null) return true;

        // And finally, if it's more than 1km away from the last known mock, we'll trust it
        double d = location.distanceTo(lastMockLocation);
        return (d > 1000);
    }


    public static String getGeoStoreState() {
        return geoStoreState;
    }

    public static void setGeoStoreState(String geoStoreState) {
        TrackerService.geoStoreState = geoStoreState;
    }

    // thread
    ReentrantLock reentrantLock;
    private GoogleApiClient.ConnectionCallbacks mLocationRequestCallback = new GoogleApiClient
            .ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            extralogToStorage("Service mGoogleApiClient connected");

            LocationRequest request = new LocationRequest();
            //request.setInterval(mFirebaseRemoteConfig.getLong("LOCATION_REQUEST_INTERVAL"));
            request.setInterval(timeInterval * 1000);
            request.setFastestInterval(timeInterval * 1000);
            Log.d(TAG, "onConnected: timeInterval :" + timeInterval);
            // request.setSmallestDisplacement(Integer.parseInt(SMALLESTDISPLACEMENT));
            // request.setFastestInterval(mFirebaseRemoteConfig.getLong("LOCATION_REQUEST_INTERVAL_FASTEST"));
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_FINE_LOCATION") != 0 && ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
                //   locationLog("Please grant permission for Location in app settings");
            } else {
                //  locationLog("Location Tracking Permission Granted");

            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    request, TrackerService.this);
            setStatusMessage(R.string.tracking);

            // Hold a partial wake lock to keep CPU awake when the we're tracking location.
            try {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                if (powerManager != null) {
                    mWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                    mWakelock.acquire();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onConnectionSuspended(int reason) {
            // TODO: Handle gracefully
        }
    };

    public void locationLog(String message) {
        Log.d(TAG, Constants.LOCATION_LOG + message);
        LogManager.writeLogError(Constants.LOCATION_LOG + message);
    }

    public TrackerService() {
    }

    private Context receiverContext;

    public TrackerService(Context context) {
        this.receiverContext = context;
    }

    public static boolean isInstanceCreated() {
        return instance != null;
    }


    private final GetDeviceInfoInterface.Stub mBinder = new GetDeviceInfoInterface.Stub() {
        @Override
        public String getSerialNumber() throws RemoteException {
            return Build.SERIAL;
        }

        @Override
        public int getVersionCodes() throws RemoteException {

            return BuildConfig.VERSION_CODE;
        }
    };



        @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    LogonCore logonCore = null;
  //  private LogonUIFacade mLogonUIFacade;
    RegistrationModel registrationModel = new RegistrationModel();


    private void initLogonCore(Context mContext, RegistrationModel registrationModel) {
        try {
            this.logonCore = LogonCore.getInstance();
          /*  this.mLogonUIFacade = LogonUIFacade.getInstance();
            this.mLogonUIFacade.init(this, mContext, registrationModel.getAppID());*/
            this.logonCore.setLogonCoreListener(this);
            this.logonCore.init(this, registrationModel.getAppID());

            try {
                if (!this.logonCore.isStoreAvailable()) {
                    this.logonCore.createStore((String)null, false);
                }
            } catch (LogonCoreException var4) {
                var4.printStackTrace();
            }
        } catch (Exception var5) {
            LogManager.writeLogError(this.getClass().getSimpleName() + ".initLogonCore: " + var5.getMessage());
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //Session.setmGlobalReceiverCallback(this);
        //MSFAGEOApplication.getInstance().setConnectivityListener(TrackerService.this);
        // Hold a partial wake lock to keep CPU awake when the we're tracking location.
        try {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powerManager != null) {
                mWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                mWakelock.acquire();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        hbPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        heartBeathandler = new Handler();
        if (heartBeathandler != null) {
            heartBeathandler.postDelayed(heartbeat, timeInterval*1000);
        }
        wakeupIntent = PendingIntent.getBroadcast(getBaseContext(), 0,
                new Intent("com.android.internal.location.ALARM_WAKEUP"), 0);


        try {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String currentDateandTime = sdf.format(new Date());
            }catch (Exception e){
                e.printStackTrace();
            }

            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            mServiceHandler = new Handler(handlerThread.getLooper());
        }catch (Exception e){
            e.printStackTrace();
        }

        sharedPreferences = null;
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
        EventUserDetail eventDataSqlHelper = new EventUserDetail(this);
       EventUserHandler = eventDataSqlHelper.getWritableDatabase();
        events = new EventDataSqlHelper(TrackerService.this);
       /* try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        intializeRegistrationModel();
        initLogonCore(TrackerService.this, registrationModel);
        try {
            String password = sharedPreferences.getString("username", "");
            logonCore.unlockStore(password);
        } catch (LogonCoreException var6) {
            var6.printStackTrace();
        }
           openGeoOfflineStore();
*/
        SMALLESTDISPLACEMENT = sharedPreferences.getString(getString(R.string.geo_smallest_displacement), "50");
        if (TextUtils.isEmpty(SMALLESTDISPLACEMENT))
            SMALLESTDISPLACEMENT = "50";


        GEOSTRTTME = sharedPreferences.getString(getString(R.string.geo_start_time), "8");
        if (TextUtils.isEmpty(GEOSTRTTME))
            GEOSTRTTME = "8";
        GEOENDTME = sharedPreferences.getString(getString(R.string.geo_end_time), "20");
        if (TextUtils.isEmpty(GEOENDTME))
            GEOENDTME = "20";
        timeInterval = sharedPreferences.getInt(getString(R.string.geo_location_interval_time), 59);

        dateAsFileName = UtilConstants.getCurrentDate();
        path = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                dateAsFileName+".txt");
        extraLogPath = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                dateAsFileName+".txt");
        /*if (!path.exists()) {
            try {
                path.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        if (!extraLogPath.exists()) {
            try {
                extraLogPath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        extralogToStorage("Service triggere oncreate");


        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        buildNotification();
        setStatusMessage(R.string.connecting);
        if (LocationUtils.isGPSEnabled(TrackerService.this) && GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(TrackerService.this) == 0) {
            if (ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_FINE_LOCATION") == 0 && ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
                /*try {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.timer_flag,false);
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                startLocationTracking();
                gpsStatusDL="1";
                Constants.isFlagVisiable = false;
                extralogToStorage("Service isGPSEnabled");

//                startTimer("");
            } else {
                //   locationLog("Location permission not enabled");
                Log.d("Permissions","E/D"+ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_FINE_LOCATION")+"---"+
                        ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_COARSE_LOCATION"));
                extralogToStorage( "on create : check Self Permission is not enabled insert lat long zero");
                insetLatLongWithZero();
              //  stopSelf();
            }
        } else {
            gpsStatusDL="0";
//            startTimer("");
            // locationLog("GPS not enabled");
       //     stopSelf();
        }



       /* mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
                mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);*/

  /*      mPrefs = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
        String email = mPrefs.getString(getString(R.string.email), "");
        String password = mPrefs.getString(getString(R.string.password), "");*/
        //  authenticate(email, password);
        registerReceiver(receiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
//        registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
//        registerReceiver(receiver, new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED"));
        /*int cur_time = 0;
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("HH");
            cur_time = Integer.parseInt(fmt.format(cal.getTime()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (cur_time >= Integer.parseInt(GEOSTRTTME) && cur_time < Integer.parseInt(GEOENDTME)) {
            startTimer("");
        }*/

    }

    private void intializeRegistrationModel() {
        registrationModel.setAppID(Configuration.APP_ID);
        registrationModel.setHttps(Configuration.IS_HTTPS);
        registrationModel.setPassword(Configuration.pwd_text);
        registrationModel.setPort(Configuration.port_Text);
        registrationModel.setSecConfig(Configuration.secConfig_Text);
        registrationModel.setServerText(Configuration.server_Text);
        registrationModel.setShredPrefKey(Constants.PREFS_NAME);
        registrationModel.setFormID(Configuration.farm_ID);
        registrationModel.setSuffix(Configuration.suffix);

        registrationModel.setDataVaultFileName(Constants.DataVaultFileName);
        registrationModel.setOfflineDBPath(Constants.offlineDBPath);
        registrationModel.setOfflineReqDBPath(Constants.offlineReqDBPath);
        registrationModel.setIcurrentUDBPath(Constants.icurrentUDBPath);
        registrationModel.setIbackupUDBPath(Constants.ibackupUDBPath);
        registrationModel.setIcurrentRqDBPath(Constants.icurrentRqDBPath);
        registrationModel.setIbackupRqDBPath(Constants.ibackupRqDBPath);
        //noPasscodeClasses.add(MainMenu.class.getName());
        // registrationModel.setNoPasscodeActivity(noPasscodeClasses);
        registrationModel.setAppActionBarIcon(R.mipmap.ic_action_bar_logo);
        registrationModel.setAppLogo(R.drawable.arteria_new_logo_transparent);
        registrationModel.setAppVersionName(BuildConfig.VERSION_NAME);
        registrationModel.setEmainId(getString(R.string.register_support_email));
        registrationModel.setPhoneNo(getString(R.string.register_support_phone));
        registrationModel.setEmailSubject("");//getString(R.string.email_subject)
        registrationModel.setRegisterActivity(RegistrationActivity.class);

//        registrationModel.setMainMenuActivity(LoginActivity.class);
    }

    private void openGeoOfflineStore() {
        try {
            Constants.updateStartSyncTime(this, Constants.Sync_All, Constants.StartSync);
            new SyncGeoAsyncTask(this, new MessageWithBooleanCallBack() {
                @Override
                public void clickedStatus(boolean isopened, String errorMsg, ErrorBean errorBean) {
                    Log.d("openGeoOfflineStore", isopened + "");
                    //setUI();
                }
            }, Constants.All).execute();
        } catch (Exception e) {
            //  setUI();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        int cur_time = 0;
        super.onDestroy();
        try {
            // Set activity title to not tracking.
            setStatusMessage(R.string.tracking_stopped);
            Log.d("Permissions","E/D"+ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_FINE_LOCATION")+"---"+
                    ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_COARSE_LOCATION"));




                // Stop the persistent notification.
            //     mNotificationManager.cancel(NOTIFICATION_ID);

            // Stop receiving location updates.
            logStatusToStorage("R.string.tracking_stopped");

            try {
                //   locationLog("Tracking service Stopped");
                if (mGoogleApiClient != null) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, TrackerService.this);
                }
                if(broadcastReceiver!=null) {
                    unregisterReceiver(broadcastReceiver);
                }
                if(receiver!=null) {
                    unregisterReceiver(receiver);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Release the wakelock
            if (mWakelock != null) {
                mWakelock.release();
            }
            /*try {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.timer_flag,false);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            instance = null;
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("HH");
            cur_time = Integer.parseInt(fmt.format(cal.getTime()));
            try {
                mServiceHandler.removeCallbacksAndMessages(null);
            }catch (Exception e){
                e.printStackTrace();
            }


        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            logStatusToStorage("Destroy_method_final_block");
            if (cur_time >= Integer.parseInt(GEOSTRTTME) && cur_time < Integer.parseInt(GEOENDTME)) {
                Constants.setScheduleAlaram(this, 0, 00, 00, 0);
                logStatusToStorage("Servcice_rescheduled");
//                startTimer("");
            } else {
                logStatusToStorage("Servcice_Killed_time_exceeds");
            }

        }


    }

    /*@Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        int cur_time = 0;
        try {
            stopSelf();
            // Set activity title to not tracking.
            setStatusMessage(R.string.tracking_stopped);
            Log.d("Permissions","E/D"+ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_FINE_LOCATION")+"---"+
                    ActivityCompat.checkSelfPermission(TrackerService.this, "android.permission.ACCESS_COARSE_LOCATION"));




            // Stop the persistent notification.
            //     mNotificationManager.cancel(NOTIFICATION_ID);

            // Stop receiving location updates.
            logStatusToStorage("onTaskRemoved : R.string.tracking_stopped");

            try {
                //   locationLog("Tracking service Stopped");
                if (mGoogleApiClient != null) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, TrackerService.this);
                }
                if(broadcastReceiver!=null) {
                    unregisterReceiver(broadcastReceiver);
                }
                if(receiver!=null) {
                    unregisterReceiver(receiver);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Release the wakelock
            if (mWakelock != null) {
                mWakelock.release();
            }
            *//*try {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.timer_flag,false);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }*//*
            instance = null;
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("HH");
            cur_time = Integer.parseInt(fmt.format(cal.getTime()));
            try {
                mServiceHandler.removeCallbacksAndMessages(null);
            }catch (Exception e){
                e.printStackTrace();
            }


        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            logStatusToStorage("onTaskRemoved : Destroy_method_final_block");
            if (cur_time >= Integer.parseInt(GEOSTRTTME) && cur_time < Integer.parseInt(GEOENDTME)) {
                Constants.setScheduleAlaram(this, 0, 00, 00, 0);
                logStatusToStorage("onTaskRemoved : Servcice_rescheduled");
//                startTimer("");
            } else {
                logStatusToStorage("onTaskRemoved : Servcice_Killed_time_exceeds");
            }

        }
    }*/

    private void authenticate(String email, String password) {
       /* final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.i(TAG, "authenticate: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            fetchRemoteConfig();
                            //  loadPreviousStatuses();
                        } else {
                            Toast.makeText(TrackerService.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            stopSelf();
                        }
                    }
                });*/
    }

    private void fetchRemoteConfig() {
       /* long cacheExpiration = CONFIG_CACHE_EXPIRY;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Remote config fetched");
                        mFirebaseRemoteConfig.activateFetched();
                    }
                });*/
    }

    /**
     * Loads previously stored statuses from Firebase, and once retrieved,
     * start location tracking.
     */
    private void loadPreviousStatuses() {
       /* String transportId = mPrefs.getString(getString(R.string.transport_id), "");
        FirebaseAnalytics.getInstance(this).setUserProperty("transportID", transportId);
        String path = getString(R.string.firebase_path) + transportId;
        mFirebaseTransportRef = FirebaseDatabase.getInstance().getReference(path);
        mFirebaseTransportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    for (DataSnapshot transportStatus : snapshot.getChildren()) {
                        mTransportStatuses.add(Integer.parseInt(transportStatus.getKey()),
                                (Map<String, Object>) transportStatus.getValue());
                    }
                }
                startLocationTracking();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // TODO: Handle gracefully
            }
        });*/
    }

    /**
     * Starts location tracking by creating a Google API client, and
     * requesting location updates.
     */
    private void startLocationTracking() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mLocationRequestCallback)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        extralogToStorage("Service mGoogleApiClient connection requested");

    }

    /**
     * Determines if the current location is approximately the same as the location
     * for a particular status. Used to check if we'll add a new status, or
     * update the most recent status of we're stationary.
     */
    private boolean locationIsAtStatus(Location location, int statusIndex) {
        if (mTransportStatuses.size() <= statusIndex) {
            return false;
        }
        Map<String, Object> status = mTransportStatuses.get(statusIndex);
        Location locationForStatus = new Location("");
        locationForStatus.setLatitude((double) status.get("lat"));
        locationForStatus.setLongitude((double) status.get("lng"));
//        float distance = location.distanceTo(locationForStatus);
        float distance = distanceCalculation(location.getLatitude(), location.getLongitude(), (double) status.get("lat"), (double) status.get("lng"));
        try {
            mStrDistance = String.valueOf(distance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // LogManager.writeLogInfo("Distance from status is " + String.valueOf(distance));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.format("Distance from status %s is %sm", statusIndex, distance));
      //  extralogToStorage("Distance between last lat/long" + distance + "::" + location.getLatitude() + location.getLongitude());
//        return distance < Integer.parseInt(SMALLESTDISPLACEMENT);
        //return distance<1;
        return false;
    }

    private float distanceCalculation(double lat1, double lon1, double lat2, double lon2) {

        origin = new LatLng(lat2, lon2);
        destination = new LatLng(lat1, lon1);
        float distance = 0.0f;
        int Radius = 6371;// radius of earth in Km
//        13.0169005,77.6689547
//        13.0171067,77.6694075
        double dLat = Math.toRadians(lat1 - lat2);
        double dLon = Math.toRadians(lon1 - lon2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
//        LogManager.writeLogInfo("Distance in KM : " + String.valueOf(kmInDec));
        double meter = valueResult * 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
//        LogManager.writeLogInfo("Distance in MM : " + String.valueOf(meterInDec));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        distance = (float) (meter);
        //  build_retrofit_and_get_response("driving");
        return distance;
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);

    }

    private int getBatteryLevel() {
        Intent batteryStatus = registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int batteryLevel = -1;
        int batteryScale = 1;
        int battery = 0;
        if (batteryStatus != null) {
            batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, batteryLevel);
            batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, batteryScale);
        }
        try {
            battery = (int) (batteryLevel / (float) batteryScale * 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return /*batteryLevel / (float) batteryScale * 100*/battery;
    }

    private void logStatusToStorage(String data) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                path = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                        dateAsFileName+".txt");
                if (!path.exists()) {
                    try {
                        path.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (path.exists()) {

                        BufferedWriter writer = new BufferedWriter(new FileWriter(path.getAbsolutePath(), true));
                        writer.write(data);
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

    private void extralogToStorage(String data) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String currentDateandTime = sdf.format(new Date());
                extraLogPath = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                        dateAsFileName+".txt");
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

    private void shutdownAndScheduleStartup(int when) {
        Log.i(TAG, "overnight shutdown, seconds to startup: " + when);
        com.google.android.gms.gcm.Task task = new OneoffTask.Builder()
                .setService(TrackerTaskService.class)
                .setExecutionWindow(when, when + 60)
                .setUpdateCurrent(true)
                .setTag(TrackerTaskService.TAG)
                .setRequiredNetwork(com.google.android.gms.gcm.Task.NETWORK_STATE_ANY)
                .setRequiresCharging(false)
                .build();
        GcmNetworkManager.getInstance(this).schedule(task);
        stopSelf();
    }

    /**
     * Pushes a new status to Firebase when location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        if(!isMocking){
        /*try {
            if(mBinder!=null && mBinder.isBinderAlive())
                mBinder.getVersionCodes();
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/

      /*  if(offlineGeo!=null && !OfflineManager.isOfflineStoreOpenGeo(offlineGeo,geoStoreState)) {
            DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(TrackerService.this);
            int count = databaseHelper.getSqlLocationDataCount();
            if(count>6 && !offlinestoreOpen) {
                openGeoOfflineStore();
            }
        }else{
            Log.e("TS onlocationChange:", "storeopened");
        }
*/

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                final StatusBarNotification[] activeNotifications;
                if (managerGeo != null) {
                    activeNotifications = managerGeo.getActiveNotifications();
                    // [END get_active_notifications]
                    // Since the notifications might include a summary notification remove it from the count if
                    // it is present.
                    if (activeNotifications != null && activeNotifications.length <= 0)
                        buildNotification();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String currentDateandTime = sdf.format(new Date());
        if (location != null) {

            int latitude = (int) location.getLatitude();
            int longitude = (int) location.getLongitude();
            if (latitude == 0 || longitude == 0) {
                LogManager.writeLogInfo("0 value for lat :" + latitude + ", or long" + longitude);
            }
            logStatusToStorage(currentDateandTime + "::" + "0 value for lat :" + location.getLatitude() + ", or long" + location.getLongitude());
            //com.arteriatech.mutils.log.LogManager.writeLogInfo("0 value for lat :" + latitude + ", or long" + longitude);

            boolean updateLocationandReport = false;
            if (currentLocation == null) {
                currentLocation = location;
                locationUpdatedAt = System.currentTimeMillis();
                updateLocationandReport = true;
            } else {
                long secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - locationUpdatedAt);
                // extralogToStorage(currentDateandTime+"::"+"secondsElapsed"+secondsElapsed);
                if (secondsElapsed >= 180) {
                    SimpleDateFormat fmt = new SimpleDateFormat("HH");
                    Calendar cal = Calendar.getInstance();
                    int cur_time = Integer.parseInt(fmt.format(cal.getTime()));
                    if (cur_time >= Integer.parseInt(GEOENDTME)) {
                        stopSelf();
                        Constants.setScheduleAlaram(TrackerService.this, Integer.parseInt(GEOSTRTTME), 0, 00, 1);
                    } else {
                        stopSelf();
                        Constants.setScheduleAlaram(this, 0, 0, 10, 0);
                    }
                } else if (secondsElapsed >= TimeUnit.MILLISECONDS.toSeconds(FASTEST_INTERVAL)) {
                    // check location accuracy here
                    currentLocation = location;
                    locationUpdatedAt = System.currentTimeMillis();
                    updateLocationandReport = true;
                }
            }

            //  if (updateLocationandReport) {
            //send your location to server
            //   logStatusToStorage(TAG + "Step:1" + location.getLatitude() + "--" + location.getLongitude());
            doc_no = (System.currentTimeMillis() + "");
            Date dateMillSec = new Date();
            currentDateTimeString = DateFormat.getDateTimeInstance().format(dateMillSec);
            Log.d(TAG, "DateandTime : " + currentDateTimeString);
            currentDate = new SimpleDateFormat("yyyy-MM-dd").format(dateMillSec);
            Log.d(TAG, "Date : " + currentDate);

            try {
                mStrbatterLevel = String.valueOf(getBatteryLevel());
                double bateryValue = Double.parseDouble(mStrbatterLevel);
                mStrbatterLevel = String.valueOf(bateryValue);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("HH");
            SimpleDateFormat fmt1 = new SimpleDateFormat("MM");
            int cur_time1 = Integer.parseInt(fmt1.format(cal.getTime()));
            int cur_time = Integer.parseInt(fmt.format(cal.getTime()));
            if (cur_time >= Integer.parseInt(GEOENDTME) || cur_time < Integer.parseInt(GEOSTRTTME)) {
            /*ServiceStartStopBean startStopBean = new ServiceStartStopBean(currentDateTimeString, "ServiceStop");
            databaseHelper.createRecordService(startStopBean);*/
                    /*try {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(Constants.timer_flag,false);
                        editor.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                Constants.isFlagVisiable = false;
                try {
                    if (broadcastReceiver != null) {
                        unregisterReceiver(broadcastReceiver);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (receiver != null) {
                        unregisterReceiver(receiver);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (cur_time >= Integer.parseInt(GEOENDTME)) {
                    Constants.setScheduleAlaram(TrackerService.this, Integer.parseInt(GEOSTRTTME), 0, 00, 1);
                } else if (cur_time < Integer.parseInt(GEOSTRTTME))
                    Constants.setScheduleAlaram(TrackerService.this, Integer.parseInt(GEOSTRTTME), 0, 00, 0);


//                    stoptimertask();
                ConstantsUtils.stopAlarmManagerByID(TrackerService.this, AutoSyncDataLocationAlarmReceiver.class, AutoSyncDataLocationAlarmReceiver.REQUEST_CODE);
                stopSelf();
            } else {
                //      logStatusToStorage(TAG + "Step:2 Before Distance Checking" + location.getLatitude() + "--" + location.getLongitude());
                if (!locationIsAtStatus(location, 0)) {
                    //  logStatusToStorage(TAG + "Step:3 After distance Checking" + location.getLatitude() + "--" + location.getLongitude());
                    Map<String, Object> transportStatus = new HashMap<>();
                    transportStatus.put("lat", location.getLatitude());
                    transportStatus.put("lng", location.getLongitude());
                    transportStatus.put("time", new Date().getTime());

                    try {
                        transportStatus.put("power", getBatteryLevel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    while (mTransportStatuses.size() >= 1) {
                        mTransportStatuses.removeLast();
                    }
                    mTransportStatuses.addFirst(transportStatus);
                    // We push the entire list at once since each key/index changes, to
                    // minimize network requests.
                    //   mFirebaseTransportRef.setValue(mTransportStatuses);
                    //    logStatusToStorage(TAG + "Step:4 Before store location" + location.getLatitude() + "--" + location.getLongitude());
                    //    locationLog("battery percentage received");
                    if (!TextUtils.isEmpty(mStrDistance)) {
                        double distance = Double.parseDouble(mStrDistance);
                        distance = ConstantsUtils.decimalRoundOff(BigDecimal.valueOf(distance), 2).doubleValue();
                        mStrDistance = String.valueOf(distance);
                    } else {
                        mStrDistance = "0.0";
                    }
                    LocationBean locationBean = new LocationBean("", "", location.getLatitude() + "", location.getLongitude() + "", currentDate + "T00:00:00", currentDateTimeString, "X", doc_no, currentDateTimeString, "false", mStrbatterLevel, mStrDistance);
                    //    logStatusToStorage(TAG + "Step:5 location bean created" + location.getLatitude() + "--" + location.getLongitude());
                    DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(TrackerService.this);
                    if (ConstantsUtils.isAutomaticTimeZone(this)) {
                        isMocking = !isLocationPlausible(location);
                        extralogToStorage(makeDataLog(locationBean));
                        try {
                            getAddress(this,location.getLatitude(),location.getLongitude(),locationBean);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        if (latitude != 0 && longitude != 0) {
                            if(!databaseHelper.getTabelRecordExist(currentDateTimeString)) {
                                databaseHelper.createRecord(locationBean);
                            }else {
                                extralogToStorage("Record already exist with "+currentDateTimeString);
                            }
                        }
                    } else {
                        extralogToStorage( R.string.dateTime_not_enable+" insert lat long zero");
                        insetLatLongWithZero();
                        showDialog(getString(R.string.dateTime_not_enable));
//                        Toast.makeText(getApplicationContext(), "Data Not Inserted", Toast.LENGTH_LONG).show();
                    }

                    // logStatusToStorage(TAG + "Step:6 Record Created" + location.getLatitude() + "--" + location.getLongitude());
                    //   locationLog("battery percentage received inserted in DB old/new Value(" + mStrbatterLevel + ")");

                    //  batteryLevel(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
/*
        LocationBean locationBean = new LocationBean("", "", lat + "", lng + "", currentDate + "T00:00:00", "Time", "X", doc_no, currentDateTimeString, mStrAppBackground,mStrbatterLevel);
        databaseHelper.createRecord(locationBean);*/

//        diconectLocation();
                    //  logStatusToStorage(TAG + "Step:7 Bfr get from Sqlite " + location.getLatitude() + "--" + location.getLongitude());

                    //    storeLocation(location);
                }

          /*      if(!pingPong){
                    if (offlineGeo != null && OfflineManager.isOfflineStoreOpenGeo(offlineGeo,geoStoreState)) {
                        try {
                         //   if (!offlineGeo.getRequestQueueIsEmpty()) {
                                if (UtilConstants.isNetworkAvailable(this)) {
                                    try {
                                        if (OfflineManager.getVisitStatusForCustomerGeo(Constants.SPGeos + Constants.isLocalFilterQry, offlineGeo)) {
                                            //   Constants.updateStartSyncTime(this, Constants.Auto_Sync, Constants.StartSync);

                                            pingPong = true;
                                            try {
                                                OfflineManager.flushQueuedRequestsForGeo(new UIListener() {
                                                    @Override
                                                    public void onRequestError(int i, Exception e) {
                                                        pingPong = false;
                                                    }

                                                    @Override
                                                    public void onRequestSuccess(int operation, String s) throws ODataException, OfflineODataStoreException {
                                                        if (operation == Operation.OfflineFlush.getValue()) {
                                                            if (UtilConstants.isNetworkAvailable(getApplicationContext())) {
                                                                try {
                                                                    //                        OfflineManager.refreshRequests(getApplicationContext(), concatCollectionStr, SyncSelectionActivity.this);
                                                                    new RefreshGeoAsyncTask(getApplicationContext(), Constants.SPGeos, this).execute();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } else {
                                                                Constants.isSync = false;

                                                            }
                                                        } else if (operation == Operation.OfflineRefresh.getValue()) {
                                                            pingPong = false;
                                                            final String syncTime = Constants.getSyncHistoryddmmyyyyTime();
                                                            Constants.updateStatus(EventUserHandler,Constants.SYNC_TABLE, Constants.TimeStamp, syncTime,Constants.SPGeos);

                                                   *//* try {
                                                        OfflineManager.getAuthorizations(sContext);
                                                    } catch (OfflineODataStoreException e) {
                                                        e.printStackTrace();
                                                    }
                                                    //  Constants.setBirthdayListToDataValut(sContext);
                                                    alAssignColl.add(Constants.SPGeos);
                                                    Constants.updateSyncTime(alAssignColl, sContext, Constants.Auto_Sync);
                                                    String syncTime = Constants.getSyncHistoryddmmyyyyTime();


                                                    Constants.events.updateStatus(Constants.SYNC_TABLE,
                                                            Constants.SPGeos, Constants.TimeStamp, syncTime
                                                    );
                                                    //   Constants.deleteDeviceMerchansisingFromDataVault(sContext);
                                                    setUI();
                                                    alAssignColl.clear();
                                                    Constants.updateStartSyncTime(AutoSyncLocationDataService.this, Constants.Auto_Sync, Constants.StartSync);

                                                    uploadSynchistory();*//*
                                                        }
                                                        //  refreshData();
                                                    }
                                                }, Constants.SPGeos, offlineGeo);
                                            } catch (OfflineODataStoreException e) {
                                                e.printStackTrace();
                                            }
                                            // OfflineManager.flushQueuedRequestsForGeo(uiListener, Constants.SPGeos);
                                        } else {

                                            Constants.mErrorCount++;
                                            //  setCallBackToUI(true, mContext.getString(R.string.no__loc_req_to_update_sap), null);
                                            //    LogManager.writeLogInfo(sContext.getString(R.string.no__loc_req_to_update_sap));
                                            //    LogManager.writeLogInfo(sContext.getString(R.string.auto_location_sync_end));
                                            Constants.iSAutoSync = false;
                                            Constants.isLocationSync = false;
                                        }
                                        //                        new FlushDataAsyncTask(this, alFlushColl).execute();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Constants.iSAutoSync = false;
                                        Constants.isLocationSync = false;
                                        Constants.mErrorCount++;
                                        // setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync)+e.getMessage(), null);
                                        //       LogManager.writeLogInfo(sContext.getString(R.string.data_conn_lost_during_sync) + e.getMessage());
                                    }
                                } else {
                                    Constants.iSAutoSync = false;
                                    Constants.isLocationSync = false;
                                    Constants.mErrorCount++;
                                    // setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
                                    // LogManager.writeLogInfo(sContext.getString(R.string.data_conn_lost_during_sync));
                                }
                            *//*} else {
                 *//**//*  Constants.updateStartSyncTime(AutoSyncLocationDataService.this, Constants.Auto_Sync, Constants.StartSync);
                        uploadSynchistory();
                        Constants.iSAutoSync = false;
                        Constants.isLocationSync = false;
                        Constants.mErrorCount++;
                        // setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
                        LogManager.writeLogInfo(sContext.getString(R.string.data_conn_lost_during_sync));*//**//*
                            }*//*
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Constants.iSAutoSync = false;
                        Constants.isLocationSync = false;
                        extralogToStorage("Auto Sync Started but Offline geo is null");
                    }
                }*/

                if (UtilConstants.isNetworkAvailable(TrackerService.this)) {
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(TrackerService.this);
                                if(databaseHelper.getSqlLocationDataCount()>=20) {
                                    ConstantsUtils.stopAlarmManagerByID(TrackerService.this, AutoSyncDataLocationAlarmReceiver.class, AutoSyncDataLocationAlarmReceiver.REQUEST_CODE);
                                    ConstantsUtils.startAutoSyncLocation(TrackerService.this, true);
                                }

                                if (reentrantLock == null) {
                                    reentrantLock = new ReentrantLock();
                                }
                                try {
                                    Log.e("TrackService REENTRANT:", "LOCKED");
                                    reentrantLock.lock();
                                    if (OfflineManager.isOfflineStoreOpenGeo(offlineGeo, geoStoreState)) {
                                        Log.e("TrackService REENTRANT:", "storeOpened");
                                        Constants.getDataFromSqliteDB(TrackerService.this, null, offlineGeo, geoStoreState);
                                    } else {
                                        Log.e("TrackService REENTRANT:", "storeNotOpened");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("TrackService EXCEPTION", "ANR EXCEPTION OCCURRED");
                                } finally {
                                    if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
                                        reentrantLock.unlock();
                                    }
                                    Log.e("TrackService REENTRANT:", "UNLOCKED FINALLY");
                                }
                            }
                        }).start();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }


                NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo();
                boolean connected = info != null && info.isConnectedOrConnecting();
                setStatusMessage(connected ? R.string.tracking : R.string.not_tracking);

            }
            //}
        }
/*

        long hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int startupSeconds = (int) (mFirebaseRemoteConfig.getDouble("SLEEP_HOURS_DURATION") * 3600);
        if (hour == mFirebaseRemoteConfig.getLong("SLEEP_HOUR_OF_DAY")) {
            shutdownAndScheduleStartup(startupSeconds);
            return;
        }
*/

    }else{
            extralogToStorage("MockLocatoion is on insert lat long zero");
            locationUpdatedAt = System.currentTimeMillis();
            insetLatLongWithZero();
        }

    }

    private void insetLatLongWithZero(){
        doc_no = (System.currentTimeMillis() + "");
        Date dateMillSec = new Date();
        currentDateTimeString = DateFormat.getDateTimeInstance().format(dateMillSec);
        Log.d(TAG, "DateandTime : " + currentDateTimeString);
        currentDate = new SimpleDateFormat("yyyy-MM-dd").format(dateMillSec);
        Log.d(TAG, "Date : " + currentDate);
        mStrDistance="0.0";
        try {
            mStrbatterLevel = String.valueOf(getBatteryLevel());
            double bateryValue = Double.parseDouble(mStrbatterLevel);
            mStrbatterLevel = String.valueOf(bateryValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocationBean locationBean = new LocationBean("", "", "0.0" + "", "0.0" + "", currentDate + "T00:00:00", currentDateTimeString, "X", doc_no, currentDateTimeString, "false", mStrbatterLevel, "0.0");
        //    logStatusToStorage(TAG + "Step:5 location bean created" + location.getLatitude() + "--" + location.getLongitude());
        DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(TrackerService.this);

        extralogToStorage( makeDataLog(locationBean));
        if(!databaseHelper.getTabelRecordExist(currentDateTimeString)) {
            databaseHelper.createRecord(locationBean);
        }else {
            extralogToStorage("Record already exist with "+currentDateTimeString);
        }
    }
    private String makeDataLog(LocationBean locationBean) {
        String dataLogString = "";
        String ml="",dmp="",ais="",bs="",adt="",loc="",ph="",st="",locMode="";;
        if(isMocking) ml="1";
        else ml="0";

        Log.i("MockLocation","falg"+AppManager.isMockSettingsON(TrackerService.this)+"  "+isMocking);
      /*  if(AppManager.isDMP(this)) dmp="1";
        else dmp="0";*/
        if(!sharedPreferences.getBoolean("AIS",false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("AIS", true);
            editor.apply();
            ais = "1";
        }else{
            ais = "0";
        }
        if(sharedPreferences.getBoolean("BS",false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("BS", false);
            editor.apply();
            bs = "1";
        }else{
            bs = "0";
        }
        if (LocationUtils.isGPSEnabled(TrackerService.this)) gpsStatusDL = "1";
        else gpsStatusDL="0";


        if (ConstantsUtils.isAutomaticTimeZone(this)) adt="1";
        else adt="0";

        int storage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int location = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int telephone = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (location != PackageManager.PERMISSION_GRANTED) loc="0";
        else loc="1";
        if (telephone != PackageManager.PERMISSION_GRANTED) ph="0";
        else ph="1";
        if (storage != PackageManager.PERMISSION_GRANTED) st="0";
        else st="1";

        try {
            locMode = AppManager.getLocationMode(TrackerService.this);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPerf = getSharedPreferences("mSFAGeoPreference", MODE_MULTI_PROCESS);
        Log.i("dmp_status",sharedPerf.getString("dmp_status","0"));
        dataLogString = "Data Log: lat- "+locationBean.getColumnLat()+" Long- "+locationBean.getColumnLong()+" ML- "+ ml +" DMP- "
                +sharedPerf.getString("dmp_status","0")+" AIS- "+ais+" BS- "+bs+" GPS- "+gpsStatusDL+" ADT- "+
                adt+" LOC- "+loc+" PH- "+ph+" ST -"+st+" LocMode- "+locMode;
        try {
            dataLogString = dataLogString + " DateTime - "+locationBean.getColumnTimestamp();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        locationBean.setCOLUMN_Loc(loc);
        locationBean.setCOLUMN_PhoneStatePermsn(ph);
        locationBean.setCOLUMN_ExtStoragePermsn(st);
        locationBean.setCOLUMN_GPSEnabled(gpsStatusDL);
        locationBean.setCOLUMN_AutoTimeZone(adt);
        locationBean.setCOLUM_NDeviceAdmnPermsn(sharedPerf.getString("dmp_status","0"));
        locationBean.setCOLUMN_MockLocation(ml);
        locationBean.setCOLUMN_AppInstallStatus(ais);
        locationBean.setCOLUMN_PhoneRestartInd(bs);
        locationBean.setCOLUMN_AccuracyLevel(locMode);
        locationBean.setCOLUMN_GPSStatus(gpsStatus());
        Log.e(TAG, "Data_Log.."+ dataLogString);
        return dataLogString;
    }
    private String gpsStatus() {
        String gpsStatus="";
        final LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsStatus = "1";
            } else {
                gpsStatus ="2";
            }
            if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(getBaseContext(), "android.permission.ACCESS_FINE_LOCATION") != 0) {
                gpsStatus ="2";
            }
        } catch (IllegalArgumentException e) {
            gpsStatus ="3";

        } catch (Exception e2) {
            gpsStatus ="3";

        }
        return gpsStatus;
    }

    private void storeLocation(Location location) {
        Log.d(TAG, "Sending info...Latitide :" + location.getLatitude() + " Longtidude :" + location.getLongitude());

       /* Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/

//        this.latitude = lat;
//        this.longitude = lng;
//        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
//        Date date = new Date();
//        String currentDateTimeString = null;
//        try {
//            currentDateTimeString = dateFormat.format(date);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Log.d(TAG, currentDateTimeString);
    /*    boolean mBoolAppBackGround = isAppIsInBackground(getApplicationContext());
        if (mBoolAppBackGround) {
            mStrAppBackground = "Background";
        } else {
            mStrAppBackground = "Foreground";
        }*/
       /* if(isAppRunning(getApplicationContext(), "tracklocation.devdeeds.com.tracklocationproject")){
            mStrAppBackground = "Foreground";
        }else{
            mStrAppBackground = "Background";
        }*/


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
                //  locationLog("battery percentage received");
                LocationBean locationBean = new LocationBean("", "", lat + "", lng + "", currentDate + "T00:00:00", "Time", "X", doc_no, currentDateTimeString, "false", mStrbatterLevel, mStrDistance);
                DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(context);
                databaseHelper.createRecord(locationBean);
                //   locationLog("battery percentage received inserted in DB old/new Value(" + mStrbatterLevel + "/" + newbatteryValue + ")");
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }


    private void buildNotification() {

        String NOTIFICATION_CHANNEL_ID = "com.arteriatech.geotrack.rspl";
        String channelName = "My Background Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelGeo = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            channelGeo.setLightColor(Color.BLUE);
            channelGeo.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            managerGeo = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert managerGeo != null;
            managerGeo.createNotificationChannel(channelGeo);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notification= notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_app_launcher)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setShowWhen(false)
                    .build();
            startForeground(FOREGROUND_SERVICE_ID, notification);
        } else {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainMenu.class), PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_app_launcher)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle("App is running in background")
                    .setOngoing(true)
                    .setContentIntent(resultPendingIntent);
            startForeground(FOREGROUND_SERVICE_ID, mNotificationBuilder.build());
        }


    }


    /**
     * Sets the current status message (connecting/tracking/not tracking).
     */
    private void setStatusMessage(int stringId) {
      /*  mNotificationBuilder.setContentText(getString(stringId));
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());*/
        Log.e(TAG, getString(stringId));

        // Also display the status message in the activity.
      /*  Intent intent = new Intent(STATUS_INTENT);
        intent.putExtra(getString(R.string.status), stringId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/
    }
    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already","running");
                return true;
            }
        }
        Log.i("Service not","running");
        return false;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                 //   stoptimertask();
                    if(isMyServiceRunning(TrackerService.class,TrackerService.this)) {
                        stopSelf();
                        Constants.setScheduleAlaram(TrackerService.this, Integer.parseInt(GEOSTRTTME), 0, 10, 0);
                    }else{
                        Constants.setScheduleAlaram(TrackerService.this, Integer.parseInt(GEOSTRTTME), 0, 10, 0);
                    }
                    Toast.makeText(context, "ON", Toast.LENGTH_SHORT).show();
                    gpsStatusDL="1";
//                    makeDataLog(0.0,0.0);
                   // stoptimertask();
                } else {
                 //   startTimer("");
                    gpsStatusDL="0";
//                    makeDataLog(0.0,0.0);
                  //  startTimer("GPS off");
                  //  Toast.makeText(context, "OFF", Toast.LENGTH_SHORT).show();
                }
//                Intent pushIntent = new Intent(context, TrackerService.class);
//                context.startService(pushIntent);
            }

            /*else if(intent.getAction().matches("android.net.conn.CONNECTIVITY_CHANGE") || intent.getAction().matches("android.net.wifi.WIFI_STATE_CHANGED")){
                if (connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected() || connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                    Toast.makeText(context, "ON", Toast.LENGTH_SHORT).show();
                    stoptimertask();
                }else{
                    startTimer("Internet off");
                    Toast.makeText(context, "OFF", Toast.LENGTH_SHORT).show();
                }
            }*//*else if(intent.getAction().matches("android.net.wifi.WIFI_STATE_CHANGED")){
                if (connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                    Toast.makeText(context, "ON", Toast.LENGTH_SHORT).show();
//                    stoptimertask();
                }else{
//                    startTimer();
                    Toast.makeText(context, "OFF", Toast.LENGTH_SHORT).show();
//                    new MainMenu().showDialog();
                }
            }*/
        }
    };

    public void showDialog(final String type) {
        if (receiverContext == null) {
            receiverContext = this;
        }
        try {
           /* WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View mView = mInflater.inflate(R.layout.aboutus_activity, null);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
            );

            wm.addView(mView, params);*/
            if (Constants.alert == null || !Constants.alert.isShowing()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Settings.canDrawOverlays(receiverContext)) {
//                        if (alert == null || !alert.isShowing()) {
                        builder = new AlertDialog.Builder(receiverContext);
                        builder.setTitle("Geo Tracking");
                        builder.setIcon(R.mipmap.ic_app_launcher);
                        builder.setMessage(type);
//                builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Do something
                                dialog.dismiss();
                                if (type.equalsIgnoreCase(receiverContext.getString(R.string.gps_not_enable))) {
                                    Intent I = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    receiverContext.startActivity(I);
                                } else if (type.equalsIgnoreCase(receiverContext.getString(R.string.dateTime_not_enable))) {
                                    Intent I = new Intent(
                                            Settings.ACTION_DATE_SETTINGS);
                                    I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    receiverContext.startActivity(I);
                                } else if (type.equalsIgnoreCase(receiverContext.getString(R.string.internet_not_enable))) {
                                    try {
                                        internetSetting();
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                }
                                /*Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                                intent.putExtra("enabled", true);
                                sendBroadcast(intent);*/

                            }
                        });
                        Constants.alert = builder.create();
                        Constants.alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        Constants.alert.setCancelable(false);
                        Constants.alert.show();
//                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    if (Settings.canDrawOverlays(receiverContext)) {
//                        if (alert == null || !alert.isShowing()) {
                        builder = new AlertDialog.Builder(receiverContext);
                        builder.setTitle("Geo Tracking");
                        builder.setIcon(R.mipmap.ic_app_launcher);
                        builder.setMessage(type);
//                builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Do something
                                dialog.dismiss();
                                if (type.equalsIgnoreCase(receiverContext.getString(R.string.gps_not_enable))) {
                                    Intent I = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    receiverContext.startActivity(I);
                                } else if (type.equalsIgnoreCase(receiverContext.getString(R.string.dateTime_not_enable))) {
                                    Intent I = new Intent(
                                            Settings.ACTION_DATE_SETTINGS);
                                    I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    receiverContext.startActivity(I);
                                } else if (type.equalsIgnoreCase(receiverContext.getString(R.string.internet_not_enable))) {
                                    try {
                                        internetSetting();
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                }
                                /*Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                                intent.putExtra("enabled", true);
                                sendBroadcast(intent);*/

                            }
                        });
                        Constants.alert = builder.create();
                        Constants.alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        Constants.alert.setCancelable(false);
                        Constants.alert.show();
//                        }
                    }
                } else {
//                    if (alert == null || !alert.isShowing()) {
                    builder = new AlertDialog.Builder(receiverContext);
                    builder.setTitle("Geo Tracking");
                    builder.setIcon(R.mipmap.ic_app_launcher);
                    builder.setMessage(type);
//                builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //Do something
                            dialog.dismiss();
                            if (type.equalsIgnoreCase(receiverContext.getString(R.string.gps_not_enable))) {
                                Intent I = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                receiverContext.startActivity(I);
                            } else if (type.equalsIgnoreCase(receiverContext.getString(R.string.dateTime_not_enable))) {
                                Intent I = new Intent(
                                        Settings.ACTION_DATE_SETTINGS);
                                I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                receiverContext.startActivity(I);
                            } else if (type.equalsIgnoreCase(receiverContext.getString(R.string.internet_not_enable))) {
                                try {
                                    internetSetting();
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                                /*Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                                intent.putExtra("enabled", true);
                                sendBroadcast(intent);*/

                        }
                    });
                    Constants.alert = builder.create();
                    Constants.alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    Constants.alert.setCancelable(false);
                    Constants.alert.show();
//                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void internetSetting() throws Throwable {
        try {
            Intent I = null;
            /**
             * ACTION_DATA_USAGE_SETTINGS Added in API level 28 (Android P)
             */
            I = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
            I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            receiverContext.startActivity(I);
        } catch (Exception e) {

            /**
             * ACTION_DATA_ROAMING_SETTINGS Added in API level 3 hence it work for all version lower to 28 API Level.
             */
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                receiverContext.startActivity(intent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    Timer timer = null;
    TimerTask timerTask = null;
    final Handler handler = new Handler();

    public void startTimer(String type) {
            //set a new Timer
            if (timer == null) {
                timer = new Timer();

                //initialize the TimerTask's job
                initializeTimerTask(type);

                //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms
                timer.schedule(timerTask, 5000, timeInterval*1000); //
            }
    }

    public void alterDialogueWithSound() {
       /* int resID=getResources().getIdentifier("justsaying", "raw", getPackageName());
        final MediaPlayer mp = MediaPlayer.create(TrackerService.this, resID);
        mp.start();*/
        try {
            MediaPlayer player = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
            player.start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timerTask.cancel();
            timer = null;
            timerTask = null;
        }
    }

    public void initializeTimerTask(final String type) {
        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        insetLatLongWithZero();
                    }
                });
            }
        };
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String value = intent.getAction();
            switch (value) {
                case Intent.ACTION_TIME_TICK:
                    try {
                        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                                .getActiveNetworkInfo();
                       // boolean connected = info != null && info.isConnected();
                        boolean connected = info != null && info.isConnectedOrConnecting();
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            alterDialogueWithSound();
                            showDialog(context.getString(R.string.gps_not_enable));
                        } else if (!connected) {
                            alterDialogueWithSound();
                            showDialog(context.getString(R.string.internet_not_enable));
                        } else if (!ConstantsUtils.isAutomaticTimeZone(context)) {
                            alterDialogueWithSound();
                            showDialog(context.getString(R.string.dateTime_not_enable));
                        }

                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat fmt = new SimpleDateFormat("HH");
                        int cur_time = Integer.parseInt(fmt.format(cal.getTime()));

                        if (cur_time >= Integer.parseInt(GEOENDTME)) {
                            Constants.setScheduleAlaram(TrackerService.this, Integer.parseInt(GEOSTRTTME), 0, 00, 1);
                            stopSelf();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
//                    Toast.makeText(context, "Time Ticking", Toast.LENGTH_LONG).show();
                    break;
            }
        }

    };
    /*private Handler handlerGPS=null;
    Runnable runnableGPS = new Runnable() {
        @Override
        public void run() {
            LocationBean locationBean = new LocationBean("", "", "0.0" + "", "0.0" + "", currentDate + "T00:00:00", "Time", "X", doc_no, currentDateTimeString, "false", mStrbatterLevel, mStrDistance);
            //    logStatusToStorage(TAG + "Step:5 location bean created" + location.getLatitude() + "--" + location.getLongitude());
            DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(TrackerService.this);
            if (ConstantsUtils.isAutomaticTimeZone(TrackerService.this)) {
                extralogToStorage( makeDataLog(0.0,0.0));
                databaseHelper.createRecord(locationBean);
            } else {
                extralogToStorage("Date_time_notmatched::");
                showDialog(getString(R.string.dateTime_not_enable));
//                        Toast.makeText(getApplicationContext(), "Data Not Inserted", Toast.LENGTH_LONG).show();
            }
        }
    };
    public void insertGSPISNotEnabled(){
        handlerGPS = new Handler();
        handlerGPS.postDelayed(runnableGPS,timeInterval*1000);
    }*/

    public BroadcastReceiver getInstance() {
        return broadcastReceiver;
    }

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    public static void refreshGeoStoreSync(Context context, UIListener uiListener, String syncType, String defineReq) throws OfflineODataStoreException {

        if (offlineGeo != null) {
            try {
                if (syncType.equalsIgnoreCase(Constants.Fresh)) {
                    LogManager.writeLogDebug("Download Sync In progress");
                } else if (syncType.equalsIgnoreCase(Constants.ALL)) {
                    LogManager.writeLogDebug("All Sync In progress");
                }
                OfflineGeoRefreshListener refreshListener = new OfflineGeoRefreshListener(context, syncType, defineReq, uiListener);

                if (syncType.equals(Constants.Fresh)) {
                    offlineGeo.scheduleRefresh(defineReq, refreshListener);
                } else {
                    offlineGeo.scheduleRefresh(refreshListener);
                }

            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
    }

    public static void flushQueuedRequestsForGeo(UIListener uiListener, String collection) throws
            OfflineODataStoreException {
        //BEGIN
        //Check if the offline oData store has been initialized
        if (offlineGeo == null) return;
        try {
            //used to get progress updates of a flush operation
            OfflineGeoFlushListener flushListener = new OfflineGeoFlushListener(uiListener, collection);
            //Assign an Offline Error Archive
            offlineGeo.setRequestErrorListener(new OfflineErrorListener());

            //Asynchronously starts sending pending modification request to the server
            offlineGeo.scheduleFlushQueuedRequests(flushListener);
        } catch (ODataException e) {
            throw new OfflineODataStoreException(e);
        }
        //END
    }

    public boolean openOfflineStoreGeo(Context context, UIListener uiListener) throws
            OfflineODataStoreException {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!OfflineManager.isOfflineStoreOpenGeo(offlineGeo,geoStoreState)) {
            try {
                //This instantiate the native UDB libraries which are located in the
                System.out.println("Service started 2");
                ODataOfflineStore.globalInit();
                //Get application endpoint URL
                  /* LogonCoreContext lgCtx = LogonCore.getInstance().getLogonContext();
                String endPointURL = "https://"+Configuration.server_Text + "/" + Configuration.APP_ID;
                URL url = new URL(endPointURL);*/

                LogonCoreContext lgCtx = LogonCore.getInstance().getLogonContext();
                String endPointURL = lgCtx.getAppEndPointUrl();
                URL url = new URL(endPointURL);

                // Define the offline store options.
                // Connection parameter and credentials and
                // the application connection id we got at the registration
                System.out.println("Service started 3");
                optionsGeo = new ODataOfflineStoreOptions();
                optionsGeo.storeEncryptionKey = Constants.EncryptKey;
                optionsGeo.host = url.getHost();
                optionsGeo.port = String.valueOf(url.getPort());
                optionsGeo.enableHTTPS = Configuration.IS_HTTPS;
                optionsGeo.enableRepeatableRequests = true;
                System.out.println("Service started 4");
                // the serviceRoot is the backend connector name, which is usually the same
                // as the application configuration name in the SMP Management Cockpit
                optionsGeo.serviceRoot = Configuration.SPGEO;

               /* //for Relay enable next line
                if (lgCtx.getResourcePath() != null && !lgCtx.getResourcePath().equalsIgnoreCase("")) {
                    optionsGeo.urlSuffix = lgCtx.getResourcePath() + "/" + lgCtx.getFarmId();
                }*/

                //The logon configurator uses the information obtained in the registration
                // (i.e endpoint URL, login, etc ) to configure the conversation manager
                /*IManagerConfigurator configurator =
                        LogonUIFacade.getInstance().getLogonConfigurator(context);
                HttpConversationManager manager = new HttpConversationManager(context);
                configurator.configure(manager);
                optionsGeo.conversationManager = manager;*/
                optionsGeo.storeName = Constants.STORE_NAMEGEO;

                optionsGeo.customHeaders.put(Constants.arteria_dayfilter, Constants.NO_OF_DAYS);
                String collectionName = Constants.SPGeos;
                if (collectionName.equalsIgnoreCase(Constants.MerchReviewImages)) {
                    optionsGeo.addDefiningRequest(collectionName, collectionName, true);
                } else {
                    optionsGeo.addDefiningRequest(collectionName, collectionName, false);
                }

                offlineGeo = new ODataOfflineStore(context);
                OfflineStoreGeoListener offlineStoreListner = new OfflineStoreGeoListener(uiListener);
                offlineGeo.setOfflineStoreListener(offlineStoreListner);
                //Assign an Offline
                offlineGeo.setRequestErrorListener(new OfflineErrorListener());

                offlineGeo.openStoreSync(optionsGeo);
                return true;
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        } else {
            return true;
        }
        //END
    }

    @Override
    public void registrationFinished(boolean b, String s, int i, DataVault.DVPasswordPolicy dvPasswordPolicy) {

    }

    @Override
    public void deregistrationFinished(boolean b) {

    }

    @Override
    public void backendPasswordChanged(boolean b) {

    }

    @Override
    public void applicationSettingsUpdated() {

    }

    @Override
    public void traceUploaded() {

    }

    public class SyncGeoAsyncTask extends AsyncTask<String, Boolean, Boolean> {
        private Context mContext;
        private MessageWithBooleanCallBack dialogCallBack = null;
        boolean onlineStoreOpen = false;
        private String mSyncType = "";

        public SyncGeoAsyncTask(Context context, MessageWithBooleanCallBack dialogCallBack, String mSyncType) {
            this.mContext = context;
            this.dialogCallBack = dialogCallBack;
            this.mSyncType = mSyncType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            onlineStoreOpen = false;
            offlinestoreOpen = true;

            try {
                Log.d("BeforeCallMustSell Req", UtilConstants.getSyncHistoryddmmyyyyTime());
                try {
                    if (!OfflineManager.isOfflineStoreOpenGeo(offlineGeo,geoStoreState)) {
                        try {
                            pingPong=true;
                            openOfflineStoreGeo(mContext, new UIListener() {
                                @Override
                                public void onRequestError(int i, Exception e) {
                                    Log.d("opOffStoreMS onReqError", UtilConstants.getSyncHistoryddmmyyyyTime());
                                    setCallBackToUI(false, "");
                                    pingPong=false;
                                    offlinestoreOpen = false;
                                }

                                @Override
                                public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                                    Log.d("opOffStoreMS onReqSuc", UtilConstants.getSyncHistoryddmmyyyyTime());
                                    List<String> alString = new ArrayList<>();
                                    alString.add(Constants.SPGeos);
                                    Constants.updateSyncTime(alString, mContext, Constants.Sync_All);
                                    Constants.insertHistoryDB(EventUserHandler,Constants.SYNC_TABLE, Constants.Collections,Constants.SPGeos);
                                  /*  events.inserthistortTable(Constants.SYNC_TABLE, "",
                                            Constants.Collections, Constants.SPGeos);*/
                                    final String syncTime = Constants.getSyncHistoryddmmyyyyTime();
                                    Constants.updateStatus(EventUserHandler,Constants.SYNC_TABLE, Constants.TimeStamp, syncTime,Constants.SPGeos);

                                    setCallBackToUI(true, "");
                                    pingPong=false;
                                    offlinestoreOpen = false;

                                }
                            });
                        } catch (OfflineODataStoreException e) {
                            onlineStoreOpen = true;
                            LogManager.writeLogError(Constants.error_txt + e.getMessage());
                        }

                    } else {
                        offlinestoreOpen = false;
                        if (mSyncType.equalsIgnoreCase(Constants.Fresh) || mSyncType.equalsIgnoreCase(Constants.All)) {
                            try {
                                if (UtilConstants.isNetworkAvailable(mContext)) {
                                    OfflineManager.refreshRequestsGeo(mContext, Constants.SPGeos, new UIListener() {
                                        @Override
                                        public void onRequestError(int operation, Exception exception) {
                                            ErrorBean errorBean = Constants.getErrorCode(operation, exception, mContext);
                                            try {
                                                if (!errorBean.hasNoError()) {
                                                    if (errorBean.getErrorCode() == Constants.Resource_not_found) {
                                                        UtilConstants.closeStore(mContext,
                                                                optionsGeo, errorBean.getErrorMsg(),
                                                                offlineGeo, Constants.PREFS_NAME, "");
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Log.d("refReqMust onReqError", UtilConstants.getSyncHistoryddmmyyyyTime());
                                            setCallBackToUI(false, "");
                                        }

                                        @Override
                                        public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                                            Log.d("refReqMust onReqError", UtilConstants.getSyncHistoryddmmyyyyTime());
                                            setCallBackToUI(true, "");
                                        }
                                    }, offlineGeo);
                                } else {
                                    onlineStoreOpen = true;
                                }
                            } catch (OfflineODataStoreException e) {
                                onlineStoreOpen = true;
                            }
                        } else {
                            onlineStoreOpen = true;
                        }
                    }
                } catch (Exception e) {
                    onlineStoreOpen = true;
                    e.printStackTrace();
                }
                Log.d("AfterCallMustSell Req", UtilConstants.getSyncHistoryddmmyyyyTime());
            } catch (Exception e) {
                onlineStoreOpen = true;
                e.printStackTrace();
            }
            return onlineStoreOpen;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                setCallBackToUI(aBoolean, Constants.makeMsgReqError(Constants.ErrorNo, mContext, false));
            }

        }

        private void setCallBackToUI(boolean status, String error_Msg) {
            if (dialogCallBack != null) {
                dialogCallBack.clickedStatus(status, error_Msg, null);
            }
        }


    }

    public class RefreshGeoAsyncTask extends AsyncTask<String, Boolean, Boolean> {
        private Context mContext;
        private String refreshList;
        private UIListener uiListener;

        public RefreshGeoAsyncTask(Context context, String refreshList, UIListener uiListener) {
            this.mContext = context;
            this.refreshList = refreshList;
            this.uiListener = uiListener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (!OfflineManager.isOfflineStoreOpenGeo(offlineGeo,geoStoreState)) {
                try {
                    openOfflineStoreGeo(mContext, uiListener);
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                    LogManager.writeLogError(Constants.error_txt + e.getMessage());
                }
            } else {
                if (!TextUtils.isEmpty(refreshList)) {
                    try {
                        OfflineManager.refreshGeoStoreSync(mContext, uiListener, Constants.Fresh, refreshList, offlineGeo);
                    } catch (OfflineODataStoreException e) {
                        e.printStackTrace();
                        LogManager.writeLogError(Constants.error_txt + e.getMessage());
                    }
                } else {
                    try {
                        OfflineManager.refreshGeoStoreSync(mContext, uiListener, Constants.All, refreshList, offlineGeo);
                    } catch (OfflineODataStoreException e) {
                        e.printStackTrace();
                        LogManager.writeLogError(Constants.error_txt + e.getMessage());
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

        }
    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE,LocationBean locationBean){
        //Set Address
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null && addresses.size() > 0) {
                String AddressLine1 = addresses.get(0).getAddressLine(0);
                locationBean.setCOLUMNAddresslineTemp(AddressLine1);

                String Premises1 = addresses.get(0).getPremises();
                locationBean.setCOLUMNPremisesTemp(Premises1);

                String SubThoroughfare1 = addresses.get(0).getSubThoroughfare();
                locationBean.setCOLUMNSubThoroughfareTemp(SubThoroughfare1);

                String Thoroughfare1 = addresses.get(0).getThoroughfare();
                locationBean.setCOLUMNThroughfareTemp(Thoroughfare1);

                String Url = addresses.get(0).getUrl();

                String Locality1 = addresses.get(0).getLocality();
                locationBean.setCOLUMNLocalityTemp(Locality1);

                String SubLocality1 = addresses.get(0).getSubLocality();
                locationBean.setCOLUMNSubLocalityTemp(SubLocality1);

                String SubAdminArea1 = addresses.get(0).getSubAdminArea();
                locationBean.setCOLUMNSubAdminAreaTemp(SubAdminArea1);

                String AdminArea1 = addresses.get(0).getAdminArea();
                locationBean.setCOLUMNAdminAreaTemp(AdminArea1);

                String PostalCode1 = addresses.get(0).getPostalCode();
                locationBean.setCOLUMNPostalCodeTemp(PostalCode1);

                String CountryName1 = addresses.get(0).getCountryName();
                locationBean.setCOLUMNCoutryTemp(CountryName1);

                String FeatureName1 = addresses.get(0).getFeatureName(); // Only if available else return NULL
                String Phone1 = addresses.get(0).getPhone(); // Only if available else return NULL
                Bundle Extras = addresses.get(0).getExtras(); // Only if available else return NULL

                extralogToStorage("Geo XMl Captured");

                locationBean.setCOLUMNXMLTemp(formXMLString(AddressLine1,Premises1,SubThoroughfare1,Thoroughfare1,Locality1,SubLocality1,
                        SubAdminArea1,AdminArea1,PostalCode1,CountryName1,LATITUDE,LONGITUDE));
            }

        } catch (IOException e) {
            e.printStackTrace();

            extralogToStorage("Geo XMl Captured with Exception:"+e.toString());

        }
    }

    public String formXMLString(String AddressLine,String Premises,String SubThoroughfare,String Thoroughfare
            ,String Locality,String SubLocality,String SubAdminArea,String AdminArea,String PostalCode,String CountryName, double LATITUDE, double LONGITUDE){

        String formXMLString = "";
        String value = " \"1.0\" ";

        formXMLString = formXMLString+"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+"\n";
        formXMLString = formXMLString+"<GeocodeResponse>"+"\n";
        formXMLString = formXMLString+"<status>OK</status>"+"\n";
        formXMLString = formXMLString+"<result>"+"\n";
        formXMLString = formXMLString+"<type>administrative_area_level_2</type>"+"\n";
        formXMLString = formXMLString+"<type>political</type>"+"\n";
        formXMLString = formXMLString+"<type>sublocality</type>"+"\n";
        formXMLString = formXMLString+"<type>sublocality_level_1</type>"+"\n";
        formXMLString = formXMLString+"<formatted_address>"+AddressLine+"</formatted_address>"+"\n";

        formXMLString = formXMLString+"<address_component>"+"\n";
        formXMLString = formXMLString+"<long_name>"+SubLocality+"</long_name>"+"\n";
        formXMLString = formXMLString+"<short_name>"+SubLocality+"</short_name>"+"\n";
        formXMLString = formXMLString+"<type>political</type>"+"\n";
        formXMLString = formXMLString+"<type>sublocality</type>"+"\n";
        formXMLString = formXMLString+"<type>sublocality_level_1</type>"+"\n";
        formXMLString = formXMLString+"</address_component>"+"\n";

        formXMLString = formXMLString+"<address_component>"+"\n";
        formXMLString = formXMLString+"<long_name>"+Locality+"</long_name>"+"\n";
        formXMLString = formXMLString+"<short_name>"+Locality+"</short_name>"+"\n";
        formXMLString = formXMLString+"<type>political</type>"+"\n";
        formXMLString = formXMLString+"<type>locality</type>"+"\n";
        formXMLString = formXMLString+"</address_component>"+"\n";

        formXMLString = formXMLString+"<address_component>"+"\n";
        formXMLString = formXMLString+"<long_name>"+SubAdminArea+"</long_name>"+"\n";
        formXMLString = formXMLString+"<short_name>"+SubAdminArea+"</short_name>"+"\n";
        formXMLString = formXMLString+"<type>administrative_area_level_2</type>"+"\n";
        formXMLString = formXMLString+"<type>political</type>"+"\n";
        formXMLString = formXMLString+"</address_component>"+"\n";

        formXMLString = formXMLString+"<address_component>"+"\n";
        formXMLString = formXMLString+"<long_name>"+AdminArea+"</long_name>"+"\n";
        formXMLString = formXMLString+"<short_name>"+AdminArea+"</short_name>"+"\n";
        formXMLString = formXMLString+"<type>administrative_area_level_1</type>"+"\n";
        formXMLString = formXMLString+"<type>political</type>"+"\n";
        formXMLString = formXMLString+"</address_component>"+"\n";

        formXMLString = formXMLString+"<address_component>"+"\n";
        formXMLString = formXMLString+"<long_name>"+CountryName+"</long_name>"+"\n";
        formXMLString = formXMLString+"<short_name>"+CountryName+"</short_name>"+"\n";
        formXMLString = formXMLString+"<type>country</type>"+"\n";
        formXMLString = formXMLString+"<type>political</type>"+"\n";
        formXMLString = formXMLString+"</address_component>"+"\n";

        formXMLString = formXMLString+"<address_component>"+"\n";
        formXMLString = formXMLString+"<long_name>"+PostalCode+"</long_name>"+"\n";
        formXMLString = formXMLString+"<short_name>"+PostalCode+"</short_name>"+"\n";
        formXMLString = formXMLString+"<type>postal_code</type>"+"\n";
        formXMLString = formXMLString+"</address_component>"+"\n";

        formXMLString = formXMLString+"<geometry>"+"\n";
        formXMLString = formXMLString+"<location>"+"\n";
        formXMLString = formXMLString+"<lat>"+LATITUDE+"</lat>"+"\n";
        formXMLString = formXMLString+"<lng>"+LONGITUDE+"</lng>"+"\n";
        formXMLString = formXMLString+"</location>"+"\n";
        formXMLString = formXMLString+"</geometry>"+"\n";
        formXMLString = formXMLString+"</result>"+"\n";
        formXMLString = formXMLString+"</GeocodeResponse>";

        return formXMLString;
    }
}
