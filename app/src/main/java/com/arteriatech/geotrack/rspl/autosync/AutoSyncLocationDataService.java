package com.arteriatech.geotrack.rspl.autosync;

/**
 * Created by E10953 on 05-08-2019.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;


import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.arteriatech.geotrack.rspl.backgroundlocationtracker.TrackerService;
import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.Operation;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.datavault.UtilDataVault;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.mutils.log.TraceLog;
import com.arteriatech.geotrack.rspl.BuildConfig;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.ErrorBean;
import com.arteriatech.geotrack.rspl.MSFAGEOApplication;
import com.arteriatech.geotrack.rspl.R;
import com.arteriatech.geotrack.rspl.SPGeo.database.DatabaseHelperGeo;
import com.arteriatech.geotrack.rspl.SPGeo.database.LocationBean;
import com.arteriatech.geotrack.rspl.asyncTask.RefreshAsyncTask;
import com.arteriatech.geotrack.rspl.offline.OfflineManager;
import com.arteriatech.geotrack.rspl.registration.Configuration;
import com.sap.mobile.lib.request.INetListener;
import com.sap.smp.client.odata.ODataDuration;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.xscript.core.GUID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by E10953 on 02-08-2019.
 */

public class AutoSyncLocationDataService extends JobIntentService implements UIListener {
    public static final int JOB_ID = 301;
    long timestamp;
    private Handler mHandlerDifferentTrd = new Handler();
    public static String TAG = "AutoSyncLocationDataService";
    public static Context sContext;
    private int penReqCount = 0;
    private int mIntPendingCollVal = 0;
    private String[][] invKeyValues = null;
    private ArrayList<String> alAssignColl = new ArrayList<>();
    private ArrayList<String> alFlushColl = new ArrayList<>();
    private int mError = 0;
    ReentrantLock reentrantLock;
    File extraLogPath = null;
    private String csrfToken;
    HttpsURLConnection connection = null;
    List<String> setCookies = new ArrayList<>();
    private SharedPreferences sharedPerf;
    private int pendingPostCount = 0;
    private int successPostCount = 0;


    public static void enqueueWork(Context context, Intent work) {
        sContext = context;
        enqueueWork(context, AutoSyncLocationDataService.class, JOB_ID, work);
    }

    private void extralogToStorage1(String data) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String currentDateandTime = sdf.format(new Date());
                File extraLogPath = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                        UtilConstants.getCurrentDate()+".txt");
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
                    Log.e("SyncHistoryFragment", "Log file error", e);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void extralogToStorage(String data) {
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

    // This describes what will happen when service is triggered
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d("DEBUG", "AutoSyncLocationDataService triggered");
        sContext = AutoSyncLocationDataService.this;
        timestamp = System.currentTimeMillis();
        // Extract additional values from the bundle
        String val = intent.getStringExtra("foo");
        sharedPerf = getSharedPreferences(Constants.PREFS_NAME, 0);


        mHandlerDifferentTrd.post(new Runnable() {
            public void run() {
                try {
                    try {
                        //    if (!ConstantsUtils.isMyServiceRunning(AutoSyncLocationDataService.class, AutoSyncLocationDataService.this))
//                            Constants.iSAutoSync = false;
                    } catch (ExceptionInInitializerError e) {
                        e.printStackTrace();
                    } catch (NoClassDefFoundError e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "auto sync location run: started");
                    extralogToStorage("AutoSyncStarted");
                    //  LogManager.writeLogInfo(sContext.getString(R.string.auto_sync_location_trigger));
                    Constants.mErrorCount = 0;
//                    if (!Constants.isDayStartSyncEnbled)
//                        LogManager.writeLogInfo(mContext.getString(R.string.auto_sync_trigger));
                    if (!Constants.isSync && !Constants.iSAutoSync) {
                        Constants.isSync = false;
                        Constants.isLocationSync = true;
                        Constants.iSAutoSync = true;
                        if (UtilConstants.isNetworkAvailable(sContext)) {
                            if (!Constants.isDayStartSyncEnbled)
                                LogManager.writeLogInfo(sContext.getString(R.string.auto_sync_location_started));
                            Constants.mApplication = (MSFAGEOApplication) sContext.getApplicationContext();
                            sharedPerf = getSharedPreferences(Constants.PREFS_NAME, 0);
                            String SPGuid = sharedPerf.getString("SPGUID", "");

                            if(!TextUtils.isEmpty(SPGuid)) {
                               SharedPreferences sharedPerfProcess = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
                                String GEOSTRTTME =sharedPerf.getString(getString(R.string.geo_start_time),"8");
                                String GEOENDTME =sharedPerf.getString(getString(R.string.geo_end_time),"20");
                                Calendar cal = Calendar.getInstance();
                                SimpleDateFormat fmt = new SimpleDateFormat("HH");
                                int cur_time = Integer.parseInt(fmt.format(cal.getTime()));
                                if (cur_time >= Integer.parseInt(GEOSTRTTME)  && cur_time<Integer.parseInt(GEOENDTME))
                                    postCapturedGeoData();
                                else
                                    ConstantsUtils.stopAlarmManagerByID(AutoSyncLocationDataService.this, AutoSyncDataLocationAlarmReceiver.class,AutoSyncDataLocationAlarmReceiver.REQUEST_CODE);
                            }
                            else{
                                extralogToStorage("SPGUID is empty service stopped");
                                Constants.iSAutoSync = false;
                                Constants.isLocationSync = false;

                                SharedPreferences mPrefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
                                try {
                                    SharedPreferences.Editor editor = mPrefs.edit();
                                    editor.putString(getString(R.string.geo_start_time), "2");
                                    editor.putString(getString(R.string.geo_end_time), "2");
                                    editor.apply();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                stopService(new Intent(AutoSyncLocationDataService.this, TrackerService.class));
                                stopSelf();
                            }

//                            postCapturedGeoData();

                            //  onUpdateSync(sContext, AutoSyncLocationDataService.this, null);
                        } else {
                            extralogToStorage(sContext.getString(R.string.auto_sync_not_perfrom_due_to_no_network));
                            LogManager.writeLogInfo(sContext.getString(R.string.auto_sync_not_perfrom_due_to_no_network));
                            Constants.iSAutoSync = false;
                            Constants.isLocationSync = false;
                            Constants.mErrorCount++;
                            // setCallBackToUI(true, sContext.getString(R.string.no_network_conn),null);
                        }
                    } else {
                        Log.d(TAG, "run: stoped started");
                        if (Constants.iSAutoSync) {
                            LogManager.writeLogInfo(sContext.getString(R.string.location_sync_auto_sync_not_perfrom));
                        } else {
                            if (!Constants.isDayStartSyncEnbled)
                                LogManager.writeLogInfo(sContext.getString(R.string.sync_prog_auto_sync_not_perfrom_location));
                        }
                        extralogToStorage(sContext.getString(R.string.location_sync_auto_sync_not_perfrom));
                        Constants.mErrorCount++;
                        //    setCallBackToUI(true, mContext.getString(R.string.alert_auto_sync_is_progress),null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    extralogToStorage("Autto Sync location error" + e.getMessage());
                    LogManager.writeLogInfo("Autto Sync location error" + e.getMessage());
                    Constants.mErrorCount++;
                    // setCallBackToUI(true, e.getMessage(),null);
                } catch (ExceptionInInitializerError e) {
                    e.printStackTrace();
                } catch (NoClassDefFoundError e) {
                    e.printStackTrace();
                }
            }
        });


        // Extract the receiver passed into the service
        // ResultReceiver rec = intent.getParcelableExtra("receiver");
        // Send result to activity
        //  sendResultValue(rec, val);
        // Let's also create notification
        //  createNotification(val);
    }

    private void postCapturedGeoData() {
        (new Thread(new Runnable() {
            public void run() {
                makeCSRFToken();
            }
        })).start();

    }

    private String postDataToServer(final URL url, final String userName, final String psw, final String csrfToken, final String body, final String doc_id, List<String> setCookies) {
        String result = null;
        // InputStream stream = null;
        HttpsURLConnection connection = null;
        successPostCount++;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            String userCredentials = userName + ":" + psw;
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes("UTF-8"), 2);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("x-smp-appid", "com.arteriatech.geotracker");
            connection.setRequestProperty("X-CSRF-Token", csrfToken);
            for (int i = 0; i < setCookies.size(); i++) {
                connection.addRequestProperty("Cookie", setCookies.get(i));
            }
            connection.addRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            //connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.close();
            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 400 && responseCode != 201) {
                extralogToStorage("HTTP error code : " + responseCode + " - " + doc_id);
                throw new IOException("HTTP error code: " + responseCode);
            }
            if (responseCode == 201) {
                Log.i("Geo", "postedSuccefully");
                extralogToStorage("postedSuccefully : " + doc_id);
                DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(AutoSyncLocationDataService.this);
                databaseHelper.deleteLatLong(doc_id);
            }

                  /*  if (responseCode == 200) {
                        stream = connection.getInputStream();
                        if (stream != null) {
                            //   result = readResponse(stream);
                        }
                    } else {
                        stream = connection.getErrorStream();
                        if (stream != null) {
                            // result = readResponse(stream);
                        }
                    }*/
        } catch (Exception var14) {
            extralogToStorage("Auto Sync Location Data Service : " + var14.toString() + " - " + doc_id);
            var14.printStackTrace();
        } finally {
                   /* if (stream != null) {

                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/

		/*	if (connection != null) {
				connection.disconnect();
			}*/
            if (pendingPostCount == successPostCount) {
                Constants.iSAutoSync = false;
                try {
                    ConstantsUtils.stopAlarmManagerByID(this, AutoSyncDataLocationAlarmReceiver.class, AutoSyncDataLocationAlarmReceiver.REQUEST_CODE);
                    ConstantsUtils.startAutoSyncLocation(this, true);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }


        return result;

    }

    private void makeCSRFToken() {
//                String host = "https://mobile-acf7a3df7.hana.ondemand.com/com.arteriatech.geotracker";
        String host = "https://" + Configuration.server_Text + "/" + Configuration.APP_ID;
        String url = host + "/SPGeos";
        //String url1 = host + "/ValidateSPIMEI";
        /*353410103024281*/

        try {
            String result = getPuserIdUtilsReponse(new URL(host), Configuration.UserName, Configuration.Password);
        } catch (IOException var17) {
            Constants.iSAutoSync = false;
            var17.printStackTrace();
        } catch (Exception var18) {
            Constants.iSAutoSync = false;
            var18.printStackTrace();
        }
    }


    public String getPuserIdUtilsReponse(final URL url, final String userName, final String psw) throws IOException {
        String result = "";
        List<String> setCookies = new ArrayList<>();
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            String userCredentials = userName + ":" + psw;
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes("UTF-8"), 2);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("x-smp-appid", "com.arteriatech.geotracker");
            connection.setRequestProperty("x-smp-enduser", userName);
            connection.setRequestProperty("X-CSRF-Token", "Fetch");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();

            connection.getResponseMessage();
            InputStream stream = null;

            if (responseCode != 200) {
                throw new IOException("HTTP error code: " + responseCode);
            } else if (responseCode == 200) {
                csrfToken = connection.getHeaderField("X-CSRF-Token");
                setCookies.addAll(connection.getHeaderFields().get("Set-Cookie"));
            }

        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

//                String host = "https://mobile-acf7a3df7.hana.ondemand.com/com.arteriatech.geotracker/SPGeos";
        String host = "https://" + Configuration.server_Text + "/" + Configuration.APP_ID + "/SPGeos";
        List<JSONObject> bodyObject = getDataFromSqliteDB(AutoSyncLocationDataService.this);
        try {
            pendingPostCount = bodyObject.size();
        } catch (Exception e) {
            Constants.iSAutoSync = false;
            e.printStackTrace();
        }
        if (pendingPostCount == 0) Constants.iSAutoSync = false;
        try {
            for (JSONObject body : bodyObject) {
                String doc_id = "";
                try {
                    doc_id = body.getString("DOC_ID");
                    body.remove("DOC_ID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                postDataToServer(new URL(host), Configuration.UserName, Configuration.Password, csrfToken, body.toString(), doc_id,setCookies);
            }
        } catch (MalformedURLException e) {
            Constants.iSAutoSync = false;
            e.printStackTrace();
        }


        return result;
    }

    private List<JSONObject> getDataFromSqliteDB(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        List<JSONObject> listofbodyObject = new ArrayList<>();
        sharedPerf = getSharedPreferences(Constants.PREFS_NAME, 0);
        String SPGuid = sharedPerf.getString("SPGUID", "");
        String imeiSim1 = sharedPerf.getString("IMEISIM1", "");
        String imeiSim2 = sharedPerf.getString("IMEISIM2", "");
        String mobileNo = sharedPerf.getString("MobileNo", "");
        String serviceProvider1 = sharedPerf.getString("ServiceProvider1", "");
        String serviceProvider2 = sharedPerf.getString("ServiceProvider2", "");
        String serviceProvider3 = sharedPerf.getString("ServiceProvider3", "");
        String serviceProvider4 = sharedPerf.getString("ServiceProvider4", "");
        //logStatusToStorage("Step:8 getDataFromSqliteDB ");
        LocationBean locationBean = null;
        ArrayList<LocationBean> alLocationBeans = new ArrayList<>();
        DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(context);
        Cursor cursor = databaseHelper.getDataLatLong();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                locationBean = new LocationBean();
                locationBean.setCOLUMN_ID(cursor.getString(0));
                locationBean.setColumnLat(cursor.getString(3));
                locationBean.setColumnLong(cursor.getString(4));
                locationBean.setColumnStartdate(cursor.getString(5));
                locationBean.setColumnStarttime(cursor.getString(6));

//                DateTime date = DateTime.parse("04/02/2011 20:27:05",
//                        DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss"));
//                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss a");
                SimpleDateFormat sdf1 = new SimpleDateFormat("HH-mm-ss");
                String time = "";
                try {
                    Date d = DateFormat.getDateTimeInstance().parse(cursor.getString(9));
                    time = sdf1.format(d);

                } catch (ParseException ex) {
                    ex.printStackTrace();
                    Log.v("Exception", ex.getLocalizedMessage());
                }

//                locationBean.setCOLUMN_Status(cursor.getString(6));
//                locationBean.setColumnTempno(cursor.getString(7));
                locationBean.setColumnTimestamp(time);
                locationBean.setCOLUMN_AppVisibility(cursor.getString(10));
                locationBean.setCOLUMN_BATTERYLEVEL(cursor.getString(11));
                try {
                    locationBean.setCOLUMN_DISTANCE(cursor.getString(12));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                locationBean.setCOLUMN_Loc(cursor.getString(13));
                locationBean.setCOLUMN_PhoneStatePermsn(cursor.getString(14));
                locationBean.setCOLUMN_ExtStoragePermsn(cursor.getString(15));
                locationBean.setCOLUMN_GPSEnabled(cursor.getString(16));
                locationBean.setCOLUMN_AutoTimeZone(cursor.getString(17));
                locationBean.setCOLUM_NDeviceAdmnPermsn(cursor.getString(18));
                locationBean.setCOLUMN_MockLocation(cursor.getString(19));
                locationBean.setCOLUMN_PhoneRestartInd(cursor.getString(20));
                locationBean.setCOLUMN_AppInstallStatus(cursor.getString(21));
                locationBean.setCOLUMN_AccuracyLevel(cursor.getString(22));
                locationBean.setCOLUMN_GPSStatus(cursor.getString(23));
                locationBean.setCOLUMNXMLTemp(cursor.getString(24));
                alLocationBeans.add(locationBean);
            }
            //  logStatusToStorage("Step:9 Location list size"+String.valueOf(alLocationBeans.size()));
            JSONObject bodyObject = new JSONObject();
            for (int i = 0; i < alLocationBeans.size(); i++) {
                bodyObject = new JSONObject();
                locationBean = alLocationBeans.get(i);
                try {
                    GUID guid = GUID.newRandom();
                    bodyObject.put("GeoGUID", guid.toString().toUpperCase());
                    if(!TextUtils.isEmpty(SPGuid))
                        bodyObject.put("SPGUID", SPGuid);
                    else
                        bodyObject.put("SPGUID", "00000000-0000-0000-0000-000000000000");
                    bodyObject.put("SPGUID", SPGuid);
                    bodyObject.put("GeoDate", locationBean.getColumnStartdate());
                    if (!TextUtils.isEmpty(locationBean.getColumnTimestamp())) {
                        ODataDuration startDuration = Constants.getTimeAsODataDurationConvertionLocation(locationBean.getColumnTimestamp());
                        //   latlonghashtable.put(Constants.GeoTime, startDuration);
                        bodyObject.put("GeoTime", startDuration);

                    }


                    try {
                        extralogToStorage1("BackGround Get data from SQLite lat : "+UtilConstants.round(Double.parseDouble(locationBean.getColumnLat()), 12)+", long :"+
                                UtilConstants.round(Double.parseDouble( locationBean.getColumnLong()), 12)+", Geo Date : "+locationBean.getColumnStartdate()+" , Geo Time : "+locationBean.getColumnTimestamp());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }


                    bodyObject.put("Latitude", String.valueOf(UtilConstants.round(Double.parseDouble(locationBean.getColumnLat()), 12)));
                    bodyObject.put("Longitude",String.valueOf(UtilConstants.round(Double.parseDouble( locationBean.getColumnLong()), 12)));
                    try {
                        bodyObject.put("AppInstallStatus", locationBean.getCOLUMN_AppInstallStatus());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("AppLocPermission", locationBean.getCOLUMN_Loc());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("DeviceAdmnPermsn", locationBean.getCOLUM_NDeviceAdmnPermsn());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ExtStoragePermsn", locationBean.getCOLUMN_ExtStoragePermsn());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    try {
                        bodyObject.put("AutoTimeZone", locationBean.getCOLUMN_AutoTimeZone());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("GPSEnabled", locationBean.getCOLUMN_GPSEnabled());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
//                        bodyObject.put("GPSStatus", locationBean.getCOLUMN_GPSStatus());
                        bodyObject.put("GPSStatus", locationBean.getCOLUMN_GPSStatus());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("AccuracyLevel", locationBean.getCOLUMN_AccuracyLevel());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("MockLocation", locationBean.getCOLUMN_MockLocation());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("PhoneRestartInd", locationBean.getCOLUMN_PhoneRestartInd());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("PhoneStatePermsn", locationBean.getCOLUMN_PhoneStatePermsn());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ServiceProvider1", serviceProvider1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        bodyObject.put("ServiceProvider2", serviceProvider2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ServiceProvider3", serviceProvider3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ServiceProvider4", serviceProvider4);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (!TextUtils.isEmpty(locationBean.getCOLUMN_DISTANCE())) {
                            bodyObject.put("Distance", locationBean.getCOLUMN_BATTERYLEVEL());
                        } else {
                            bodyObject.put("Distance", "0.0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (!TextUtils.isEmpty(locationBean.getCOLUMN_BATTERYLEVEL())) {
                            bodyObject.put("BatteryPerc", locationBean.getCOLUMN_BATTERYLEVEL());
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (!TextUtils.isEmpty(locationBean.getCOLUMNXMLTemp())) {
                            bodyObject.put("ZZGeoXml", locationBean.getCOLUMNXMLTemp());
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    //bodyObject.put("Distance", locationBean.getCOLUMN_DISTANCE());
                    bodyObject.put("DistanceUOM", "M");
                    bodyObject.put("MobileNo", mobileNo);
                    bodyObject.put("IMEI1", androidId);
                    bodyObject.put("IMEI2", androidId);
                    bodyObject.put("DOC_ID", locationBean.getCOLUMN_ID());
                    bodyObject.put(Constants.OsVersion, android.os.Build.VERSION.RELEASE);
                    try {
                        bodyObject.put("OSVersionCode", String.valueOf(Build.VERSION.SDK_INT));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    bodyObject.put(Constants.MobileModel, Constants.getDeviceName());
                    //bodyObject.put("BatteryPerc", locationBean.getCOLUMN_BATTERYLEVEL());
                    //bodyObject.put("IMEI1", "353410103024123");
                    bodyObject.put("APKVersion", BuildConfig.VERSION_NAME);
                    bodyObject.put("APKVersionCode", String.valueOf(BuildConfig.VERSION_CODE));
                    listofbodyObject.add(bodyObject);
//                    databaseHelper.deleteLatLong(locationBean.getCOLUMN_ID());


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            // updataLatLong(alLocationBeans, context, rListener,offlineGeo,state);
        } else {
           /* if (cursor != null)
                LocationMonitoringService.locationLog(" SQL db record count " + cursor.getCount());*/
        }
        return listofbodyObject;
    }

    private void onUpdateSync(final Context mContext, final UIListener uiListener, final INetListener iNetListener) {
        try {
            penReqCount = 0;
            mIntPendingCollVal = 0;
          /*  ArrayList<Object> objectArrayLists = SyncSelectionActivity.getPendingInvList(mContext);
            if (!objectArrayLists.isEmpty()) {
                mIntPendingCollVal = (int) objectArrayLists.get(0);
                invKeyValues = (String[][]) objectArrayLists.get(1);
            }

            if (mIntPendingCollVal > 0) {

            } else {*/
//                mIntPendingCollVal = 0;
                /*invKeyValues = null;
                ArrayList<Object> objectArrayList = SyncSelectionActivity.getPendingCollList(mContext,true);
                if (!objectArrayList.isEmpty()) {
                    mIntPendingCollVal = (int) objectArrayList.get(0);
                    invKeyValues = (String[][]) objectArrayList.get(1);
//                    cancelSOCount=(int[]) objectArrayList.get(2);
                }*/

//            }
            penReqCount = 0;

           /* if (!OfflineManager.isOfflineStoreOpen()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OfflineManager.openOfflineStore(mContext, new UIListener() {
                                @Override
                                public void onRequestError(int operation, Exception exception) {
                                    ErrorBean errorBean = Constants.getErrorCode(operation, exception, mContext);

                                    Constants.iSAutoSync = false;
                                    Constants.mErrorCount++;
                                    setCallBackToUI(true, Constants.makeMsgReqError(errorBean.getErrorCode(), mContext, false), null);
                                }


                                @Override
                                public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                                    if (OfflineManager.isOfflineStoreOpen()) {
                                        try {
                                            OfflineManager.getAuthorizations(mContext);
                                        } catch (OfflineODataStoreException e) {
                                            e.printStackTrace();
                                        }
                                        Constants.mErrorCount = 0;
                                        Constants.iSAutoSync = false;
                                        setCallBackToUI(true, "", null);
                                    }
                                }
                            });
                        } catch (OfflineODataStoreException e) {
                            e.printStackTrace();
                            Constants.iSAutoSync = false;
                            LogManager.writeLogError(Constants.error_txt + e.getMessage());
                        }
                    }
                }).start();

            } else {*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                 /*   if (OfflineManager.isOfflineStoreOpenGeo()) {
                        if (reentrantLock == null) {
                            reentrantLock = new ReentrantLock();
                        }
                        try {
                            Log.e("TrackService REENTRANT:", "LOCKED");
                            reentrantLock.lock();
                            Constants.getDataFromSqliteDB(getApplicationContext(),null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("TrackService EXCEPTION", "ANR EXCEPTION OCCURRED");
                        } finally {
                            if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
                                reentrantLock.unlock();
                            }
                            Log.e("TrackService REENTRANT:", "UNLOCKED FINALLY");
                        }
                        postData(uiListener, iNetListener);
                    }else{
                        LogManager.writeLogInfo("Autto Sync location Service Offline Store not opened");

                    }*/
                    postData(uiListener, iNetListener);
                }
            }).start();
//            }


        } catch (Exception e) {
            e.printStackTrace();
            Constants.iSAutoSync = false;
            Constants.isLocationSync = false;
            LogManager.writeLogInfo("Autto Sync location" + e.getMessage());
            //     setCallBackToUI(true, e.getMessage(), null);
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
        }
    }

    private void postData(UIListener uiListener, INetListener iNetListener) {
        try {
//            if (OfflineManager.offlineStore.getRequestQueueIsEmpty() && mIntPendingCollVal == 0) {
           /* if (mIntPendingCollVal == 0) {
                LogManager.writeLogInfo(mContext.getString(R.string.no_req_to_update_sap));
                Constants.iSAutoSync = false;
                setCallBackToUI(true, mContext.getString(R.string.no_req_to_update_sap), null);
                *//*if (UtilConstants.isNetworkAvailable(mContext)) {
                    alAssignColl.addAll(Constants.getDefinigReqList(mContext));
                    onAllSync(mContext);
                } else {
                    Constants.iSAutoSync = false;
                    Constants.mErrorCount++;
                    setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
                }*//*
            } else {*/
            alAssignColl.clear();
            alFlushColl.clear();
            uploadSynchistory();

          /*  ArrayList<String> allAssignColl = SyncSelectionActivity.getRefreshListAuto(sContext);
            if (!allAssignColl.isEmpty()) {
                alAssignColl.addAll(allAssignColl);
                alFlushColl.addAll(allAssignColl);
            }*/

           /* if (OfflineManager.offlineGeo != null){
                if (!OfflineManager.offlineGeo.getRequestQueueIsEmpty()) {
                    if (UtilConstants.isNetworkAvailable(sContext)) {
                        try {
                            if (OfflineManager.getVisitStatusForCustomerGeo(Constants.SPGeos + Constants.isLocalFilterQry)) {
                                Constants.updateStartSyncTime(AutoSyncLocationDataService.this, Constants.Auto_Sync, Constants.StartSync);

                                try {
                                    OfflineManager.flushQueuedRequestsForGeo(new UIListener() {
                                        @Override
                                        public void onRequestError(int i, Exception e) {

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
                                                        TraceLog.e(Constants.SyncOnRequestSuccess, e);
                                                    }
                                                } else {
                                                    Constants.isSync = false;

                                                }
                                            } else if (operation == Operation.OfflineRefresh.getValue()) {
                                                try {
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

                                                uploadSynchistory();
                                            }
                                            //  refreshData();
                                        }
                                    }, Constants.SPGeos);
                                } catch (OfflineODataStoreException e) {
                                    e.printStackTrace();
                                }
                                // OfflineManager.flushQueuedRequestsForGeo(uiListener, Constants.SPGeos);
                            } else {

                                Constants.mErrorCount++;
                                //  setCallBackToUI(true, mContext.getString(R.string.no__loc_req_to_update_sap), null);
                                LogManager.writeLogInfo(sContext.getString(R.string.no__loc_req_to_update_sap));
                                LogManager.writeLogInfo(sContext.getString(R.string.auto_location_sync_end));
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
                            LogManager.writeLogInfo(sContext.getString(R.string.data_conn_lost_during_sync) + e.getMessage());
                        }
                    } else {
                        Constants.iSAutoSync = false;
                        Constants.isLocationSync = false;
                        Constants.mErrorCount++;
                        // setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
                        LogManager.writeLogInfo(sContext.getString(R.string.data_conn_lost_during_sync));
                    }
                } else {
                    Constants.updateStartSyncTime(AutoSyncLocationDataService.this, Constants.Auto_Sync, Constants.StartSync);
                    uploadSynchistory();
                    Constants.iSAutoSync = false;
                    Constants.isLocationSync = false;
                    Constants.mErrorCount++;
                    // setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
                    LogManager.writeLogInfo(sContext.getString(R.string.data_conn_lost_during_sync));
                }
        }else{
                Constants.iSAutoSync = false;
                Constants.isLocationSync = false;
                extralogToStorage("Auto Sync Started but Offline geo is null");
            }*/
                /*if (mIntPendingCollVal > 0) {
                    if (UtilConstants.isNetworkAvailable(mContext)) {
                        onlineStoreOpen = false;
                        Constants.mBoolIsReqResAval = true;
                        Constants.mBoolIsNetWorkNotAval = false;
                        Constants.onlineStore = null;

                        tokenFlag = false;
                        Constants.x_csrf_token = "";
                        Constants.ErrorCode = 0;
                        Constants.ErrorNo = 0;
                        Constants.ErrorName = "";
                        Constants.ErrorNo_Get_Token = 0;
                        Constants.IsOnlineStoreFailed = false;
                        OnlineStoreListener.instance = null;
                        try {
                            onlineStoreOpen = OnlineManager.openOnlineStore(mContext);
                        } catch (OnlineODataStoreException e) {
                            e.printStackTrace();
                        }
                        if (onlineStoreOpen) {
                            *//*SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
                            if (sharedPreferences.getString(Constants.isInvoiceCreateKey, "").equalsIgnoreCase(Constants.isInvoiceTcode)) {
                                onLoadToken(mContext);
                                if (tokenFlag) {
                                    if (Constants.x_csrf_token != null && !Constants.x_csrf_token.equalsIgnoreCase("")) {
                                        try {
                                            new PostDataFromDataValt(mContext, uiListener, invKeyValues, iNetListener).execute();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Constants.iSAutoSync = false;
                                        Constants.mErrorCount++;
                                        setCallBackToUI(true, Constants.makeMsgReqError(-2, mContext, true), null);
                                    }
                                } else {
                                    Constants.iSAutoSync = false;
                                    Constants.mErrorCount++;
                                    setCallBackToUI(true, Constants.makeMsgReqError(Constants.ErrorNo_Get_Token, mContext, true), null);
                                }
                            } else {*//*
                                try {
                                    new PostDataFromDataValt(mContext, uiListener, invKeyValues, iNetListener).execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
//                            }
                        } else {
                            Constants.iSAutoSync = false;
                            Constants.mErrorCount++;
                            setCallBackToUI(true, Constants.makeMsgReqError(Constants.ErrorNo, mContext, false), null);
                        }


                    } else {
                        Constants.iSAutoSync = false;
                        Constants.mErrorCount++;
                        setCallBackToUI(true, mContext.getString(R.string.no_network_conn), null);
                        LogManager.writeLogInfo(mContext.getString(R.string.no_network_conn));
                    }
                }*/ /*else if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                    if (UtilConstants.isNetworkAvailable(mContext)) {
                        try {
                            new FlushDataAsyncTask(this, alFlushColl).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Constants.iSAutoSync = false;
                        Constants.mErrorCount++;
                        setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
                        LogManager.writeLogInfo(mContext.getString(R.string.data_conn_lost_during_sync));
                    }
                } else {
                    if (!UtilConstants.isNetworkAvailable(mContext)) {
                        Constants.iSAutoSync = false;
                        Constants.mErrorCount++;
                        setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
                        LogManager.writeLogInfo(mContext.getString(R.string.data_conn_lost_during_sync));
                    } else {
                        onAllSync(mContext);
                    }
                }*/
        } catch (Exception e) {
            e.printStackTrace();
            Constants.mErrorCount++;
            //  setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync), null);
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
        }
    }

    private void uploadSynchistory() {
        try {
            if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                try {
                    Constants.updateStartSyncTime(AutoSyncLocationDataService.this, Constants.Auto_Sync, Constants.StartSync);

                    OfflineManager.flushQueuedRequests(new UIListener() {
                        @Override
                        public void onRequestError(int i, Exception e) {

                        }

                        @Override
                        public void onRequestSuccess(int operation, String s) throws ODataException, OfflineODataStoreException {
                            if (operation == Operation.OfflineFlush.getValue()) {
                                if (UtilConstants.isNetworkAvailable(getApplicationContext())) {
                                    try {
//                        OfflineManager.refreshRequests(getApplicationContext(), concatCollectionStr, SyncSelectionActivity.this);
                                        new RefreshAsyncTask(getApplicationContext(), Constants.SyncHistorys, this).execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        TraceLog.e(Constants.SyncOnRequestSuccess, e);
                                    }
                                } else {
                                    Constants.isSync = false;

                                }
                            } else if (operation == Operation.OfflineRefresh.getValue()) {
                                try {
                                    OfflineManager.getAuthorizations(sContext);
                                } catch (OfflineODataStoreException e) {
                                    e.printStackTrace();
                                }
                                //  Constants.setBirthdayListToDataValut(sContext);
                                alAssignColl.add(Constants.SyncHistorys);

                                Constants.updateSyncTime(alAssignColl, sContext, Constants.Auto_Sync);
                                String syncTime = Constants.getSyncHistoryddmmyyyyTime();


                                Constants.events.updateStatus(Constants.SYNC_TABLE,
                                        Constants.SyncHistorys, Constants.TimeStamp, syncTime
                                );

                                //   Constants.deleteDeviceMerchansisingFromDataVault(sContext);
                                setUI();
                            }
                        }
                    }, Constants.SyncHistorys);
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                }
            }
        } catch (ODataException e) {
            e.printStackTrace();
        }
    }


    // Send result to activity using ResultReceiver
    private void sendResultValue(ResultReceiver rec, String val) {
        // To send a message to the Activity, create a pass a Bundle
        Bundle bundle = new Bundle();
        bundle.putString("resultValue", "My Result Value. You Passed in: " + val + " with timestamp: " + timestamp);
        // Here we call send passing a resultCode and the bundle of extras
        rec.send(Activity.RESULT_OK, bundle);
    }

    // Construct compatible notification
    private void createNotification(String val) {

       /* // Construct pending intent to serve as action for notification item
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("message", "Launched via notification with message: " + val + " and timestamp " + timestamp);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // Create notification
        String longText = "Intent service has a new message with: " + val + " and a timestamp of: " + timestamp;
        Notification noti =
                new NotificationCompat.Builder(this, DemoApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Result!")
                        .setContentText("Simple Intent service has a new message")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(longText))
                        .setContentIntent(pIntent)
                        .build();

        // Hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIF_ID, noti);*/
    }

    @Override
    public void onRequestError(int operation, Exception exception) {
        ErrorBean errorBean = Constants.getErrorCode(operation, exception, sContext);
        try {
            if (errorBean.hasNoError()) {
                mError++;
                penReqCount++;
                Constants.mBoolIsReqResAval = true;
                Constants.mErrorCount++;

                if ((operation == Operation.Create.getValue()) && (penReqCount == mIntPendingCollVal)) {
                    final String mErrorMsg = "";
                    Constants.iSAutoSync = false;
                    Constants.isLocationSync = false;
                    setErrorUI(mErrorMsg, errorBean);
                    //old code
                   /* try {
                        if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                            if (UtilConstants.isNetworkAvailable(mContext)) {
                                try {
                                    new FlushDataAsyncTask(this, alFlushColl).execute();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            } else {
                                Constants.iSAutoSync = false;
                                Constants.mErrorCount++;
                                setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync),null);
                            }
                        } else {
                            if (UtilConstants.isNetworkAvailable(mContext)) {
                                alAssignColl.clear();
                                alAssignColl.addAll(Constants.getDefinigReqList(mContext));
                                onAllSync(mContext);
                            } else {
                                Constants.iSAutoSync = false;
                                Constants.mErrorCount++;
                                setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync),null);
                            }
                        }
                    } catch (ODataException e3) {
                        e3.printStackTrace();
                    }*/

                }

                if (operation == Operation.OfflineFlush.getValue()) {
//                    if (UtilConstants.isNetworkAvailable(mContext)) {
//                        alAssignColl.clear();
//                        alAssignColl.addAll(Constants.getDefinigReqList(mContext));
//                        onAllSync(mContext);
//                    } else {
                    Constants.iSAutoSync = false;
                    Constants.isLocationSync = false;
                    Constants.mErrorCount++;
                    //  setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync),null);
//                    }
                } else if (operation == Operation.OfflineRefresh.getValue()) {
                    Constants.iSAutoSync = false;
                    Constants.isLocationSync = false;
                    Constants.mErrorCount++;
                    final String mErrorMsg = "";
                    setErrorUI(mErrorMsg, errorBean);

                   /* try {
                        new SyncGeoAsyncTask(mContext, new MessageWithBooleanCallBack() {
                            @Override
                            public void clickedStatus(boolean clickedStatus, String errorMsg, ErrorBean errorBean) {
                                Log.d("clickedStatus Req", clickedStatus+"");

                            }
                        }, Constants.All).execute();
                    } catch (Exception e) {
                        setErrorUI(mErrorMsg,errorBean);
                        e.printStackTrace();
                    }*/


                }
            } else {
                Constants.mBoolIsNetWorkNotAval = true;
                Constants.mBoolIsReqResAval = true;
                if (Constants.iSAutoSync) {
                    Constants.iSAutoSync = false;
                }
                Constants.isLocationSync = false;
                Constants.mErrorCount++;

                if (errorBean.isStoreFailed()) {
                    OfflineManager.offlineStore = null;
                    OfflineManager.options = null;
                    openStore(errorBean);
                } else {
                    //  setCallBackToUI(true, Constants.makeMsgReqError(errorBean.getErrorCode(), mContext, false),errorBean);
                }

            }
        } catch (Exception e) {
            Constants.mBoolIsNetWorkNotAval = true;
            Constants.mBoolIsReqResAval = true;
            if (Constants.iSAutoSync) {
                Constants.iSAutoSync = false;
            }
            Constants.isLocationSync = false;
            Constants.mErrorCount++;
            //    setCallBackToUI(true, Constants.makeMsgReqError(errorBean.getErrorCode(), mContext, false),null);
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestSuccess(int operation, String s) throws ODataException, OfflineODataStoreException {
        try {
            Log.d(TAG, "onRequestSuccess: ");
            if (operation == Operation.Create.getValue() && mIntPendingCollVal > 0) {
                Constants.mBoolIsReqResAval = true;
              /*  if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.CollList)) {
                    Constants.removeDeviceDocNoFromSharedPref(sContext, Constants.CollList, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SalesOrderDataValt)) {
                    Constants.removeDeviceDocNoFromSharedPref(sContext, Constants.SalesOrderDataValt, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOUpdate)) {
                    Constants.removeDeviceDocNoFromSharedPref(sContext, Constants.SOUpdate, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOCancel)) {
                    Constants.removeDeviceDocNoFromSharedPref(sContext, Constants.SOCancel, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.Expenses)) {
                    Constants.removeDeviceDocNoFromSharedPref(sContext, Constants.Expenses, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.MTPDataValt)) {
                    Constants.removeDeviceDocNoFromSharedPref(sContext, Constants.MTPDataValt, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.RTGSDataValt)) {
                    Constants.removeDeviceDocNoFromSharedPref(sContext, Constants.RTGSDataValt, invKeyValues[penReqCount][0]);
                }
*/
                UtilDataVault.storeInDataVault(invKeyValues[penReqCount][0], "");
                penReqCount++;
            }
            if ((operation == Operation.Create.getValue()) && (penReqCount == mIntPendingCollVal)) {
                setUI();
                //old code
               /* try {
                    if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                        if (UtilConstants.isNetworkAvailable(mContext)) {
                            try {
                                new FlushDataAsyncTask(this, alFlushColl).execute();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        } else {
                            Constants.iSAutoSync = false;
                            setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync),null);
                        }
                    } else {
                        if (UtilConstants.isNetworkAvailable(mContext)) {
                            alAssignColl.clear();
                            alAssignColl.addAll(Constants.getDefinigReqList(mContext));
                            onAllSync(mContext);
                        } else {
                            Constants.iSAutoSync = false;
                            setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync),null);
                        }
                    }

                } catch (ODataException e) {
                    e.printStackTrace();
                }*/

            } else if (operation == Operation.OfflineFlush.getValue()) {
                if (UtilConstants.isNetworkAvailable(sContext)) {
//                    alAssignColl.clear();
//                    alAssignColl.addAll(Constants.getDefinigReqList(mContext));
                    onAllSync(sContext);
                } else
                    LogManager.writeLogInfo(sContext.getString(R.string.auto_location_sync_end));
                Constants.iSAutoSync = false;
                Constants.isLocationSync = false;
                // setCallBackToUI(true, mContext.getString(R.string.data_conn_lost_during_sync),null);
//                }
            } else if (operation == Operation.OfflineRefresh.getValue()) {

                try {
                    OfflineManager.getAuthorizations(sContext);
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                }
                //    Constants.setBirthdayListToDataValut(sContext);
                Constants.updateSyncTime(alAssignColl, sContext, Constants.Auto_Sync);
                //    Constants.deleteDeviceMerchansisingFromDataVault(sContext);
                setUI();
//                Constants.setAppointmentNotification(mContext);
//                if(alAssignColl.contains(Constants.RoutePlans) || alAssignColl.contains(Constants.ChannelPartners) || alAssignColl.contains(Constants.Visits)) {
//                    Constants.alTodayBeatRet.clear();
//                    Constants.TodayTargetRetailersCount = Constants.getVisitTargetForToday();
//                    Constants.TodayActualVisitRetailersCount = Constants.getVisitedRetailerCount(Constants.alTodayBeatRet);
//                }
//                if(alAssignColl.contains(Constants.SSSOs) || alAssignColl.contains(Constants.Targets)) {
//                    Constants.loadingTodayAchived(mContext,Constants.alTodayBeatRet);
//                }

               /* try {
                    new SyncGeoAsyncTask(mContext, new MessageWithBooleanCallBack() {
                        @Override
                        public void clickedStatus(boolean clickedStatus, String errorMsg, ErrorBean errorBean) {
                            Log.d("clickedStatus Req", clickedStatus+"");
                            setUI();
                        }
                    }, Constants.All).execute();
                } catch (Exception e) {
                    setUI();
                    e.printStackTrace();
                }*/
                /*Constants.iSAutoSync = false;

                String mErrorMsg = "";
                if (Constants.AL_ERROR_MSG.size() > 0) {
                    mErrorMsg = Constants.convertALBussinessMsgToString(Constants.AL_ERROR_MSG);
                }

                if (mErrorMsg.equalsIgnoreCase("")) {
                    setCallBackToUI(true, mContext.getString(R.string.error_occured_during_post),null);
                } else {
                    setCallBackToUI(true, mErrorMsg,null);
                }*/

            }
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
        }

    }

    private void setErrorUI(String mErrorMsg, ErrorBean errorBean) {
        if (Constants.AL_ERROR_MSG.size() > 0) {
            mErrorMsg = Constants.convertALBussinessMsgToString(Constants.AL_ERROR_MSG);
        }
        if (mErrorMsg.equalsIgnoreCase("")) {
            // setCallBackToUI(true, errorBean.getErrorMsg(),null);
        } else {
            // setCallBackToUI(true, mErrorMsg,null);
        }
    }

    private void openStore(final ErrorBean errorBean) {
        if (!OfflineManager.isOfflineStoreOpen()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OfflineManager.openOfflineStore(sContext, new UIListener() {
                            @Override
                            public void onRequestError(int operation, Exception exception) {
                                ErrorBean errorBean = Constants.getErrorCode(operation, exception, sContext);

                                Constants.iSAutoSync = false;
                                Constants.isLocationSync = false;
                                Constants.mErrorCount++;
                                // setCallBackToUI(true, Constants.makeMsgReqError(errorBean.getErrorCode(), mContext, false), null);
                            }


                            @Override
                            public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                                if (OfflineManager.isOfflineStoreOpen()) {
                                    try {
                                        OfflineManager.getAuthorizations(sContext);
                                    } catch (OfflineODataStoreException e) {
                                        e.printStackTrace();
                                    }
                                    Constants.mErrorCount = 0;
                                    // setCallBackToUI(true, "", null);
                                }
                            }
                        });
                    } catch (OfflineODataStoreException e) {
                        //  setCallBackToUI(true, Constants.makeMsgReqError(errorBean.getErrorCode(), mContext, false), errorBean);
                        LogManager.writeLogError(Constants.error_txt + e.getMessage());
                    } catch (ExceptionInInitializerError e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } else {
            // setCallBackToUI(true, Constants.makeMsgReqError(errorBean.getErrorCode(), mContext, false), errorBean);
        }
    }

    private void onAllSync(Context mContext) {
//        new AllSyncAsyncTask(mContext, this, new ArrayList<String>()).execute();
        String syncCollection = UtilConstants.getConcatinatinFlushCollectios(alAssignColl);
        if (!TextUtils.isEmpty(syncCollection)) {
            new RefreshAsyncTask(mContext, syncCollection, this).execute();
        } else {
            //  setCallBackToUI(true, "No offline data to post", null);
        }
    }

    private void setUI() {
        try {
            Constants.iSAutoSync = false;
            Constants.isLocationSync = false;

            String mErrorMsg = "";
            if (Constants.AL_ERROR_MSG.size() > 0) {
                mErrorMsg = Constants.convertALBussinessMsgToString(Constants.AL_ERROR_MSG);
            }

            if (mErrorMsg.equalsIgnoreCase("")) {
                //  setCallBackToUI(true, mContext.getString(R.string.error_occured_during_post),null);
            } else {
                //setCallBackToUI(true, mErrorMsg,null);
            }
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
        }
    }
}

