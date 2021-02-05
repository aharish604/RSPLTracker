package com.arteriatech.geotrack.rspl;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.interfaces.DialogCallBack;
import com.arteriatech.geotrack.rspl.dashboard.MainMenu;
import com.arteriatech.geotrack.rspl.registration.Configuration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.arteriatech.geotrack.rspl.Constants.writeLogsToInternalStorage;

public class ValidateIMEIActivity extends AppCompatActivity {
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_PHONE_STATE
    };
    private int requestPermissionCode = 1;
    HttpsURLConnection connection = null;
    private ProgressDialog pdLoadDialog = null;
    private SharedPreferences sharedPerf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_imei);
        sharedPerf = getSharedPreferences(Constants.PREFS_NAME, 0);
        verifyStoragePermissions(this);


    }

    public void verifyStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if we have write permission
            int storage = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int location = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
            int camera = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            int telephone = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);
            if (storage != PackageManager.PERMISSION_GRANTED || location != PackageManager.PERMISSION_GRANTED || camera != PackageManager.PERMISSION_GRANTED || telephone != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        requestPermissionCode
                );
            } else {
                validateImeiFromServer();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int reqcode = requestCode;
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    validateImeiFromServer();

                } else {
                    verifyStoragePermissions(this);
                }
                return;
            }
        }
    }


    private void validateImeiFromServer() {
        if (UtilConstants.isNetworkAvailable(this)) {
            showProgressDialog();

            (new Thread(new Runnable() {
                public void run() {
                    String imeiSIM1 = "";
                    String imeiSIM2 = "";
                    String androidId = "";
                    String serviceProvider1 = "";
                    String serviceProvider2 = "";
                    String serviceProvider3 = "";
                    String serviceProvider4 = "";

                    try {
                        int telephone = ActivityCompat.checkSelfPermission(ValidateIMEIActivity.this, Manifest.permission.READ_PHONE_STATE);
                        if (telephone == PackageManager.PERMISSION_GRANTED) {
                            TelephonyManager telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
                            List<String> carrierNames = new ArrayList<>();
                            try {
                                carrierNames = getNetworkOperator(ValidateIMEIActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            for (int i = 0; i < carrierNames.size(); i++) {
                                if (i == 0) {
                                    serviceProvider1 = carrierNames.get(i);
                                } else if (i == 1) {
                                    serviceProvider2 = carrierNames.get(i);
                                } else if (i == 2) {
                                    serviceProvider3 = carrierNames.get(i);
                                } else if (i == 3) {
                                    serviceProvider4 = carrierNames.get(i);
                                }
                            }
                            try {
                                imeiSIM1 = telephonyManager.getDeviceId(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                imeiSIM2 = telephonyManager.getDeviceId(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                SharedPreferences.Editor editor = sharedPerf.edit();
                                editor.putString("IMEISIM1", imeiSIM1);
                                editor.putString("IMEISIM2", imeiSIM2);
                                editor.putString("ServiceProvider1", serviceProvider1);
                                editor.putString("ServiceProvider2", serviceProvider2);
                                editor.putString("ServiceProvider3", serviceProvider3);
                                editor.putString("ServiceProvider4", serviceProvider4);
                                editor.apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    String host = "https://mobile-a0abd1907.hana.ondemand.com/com.arteriatech.geotracker";
                    String host = "https://" + Configuration.server_Text + "/" + Configuration.APP_ID;

                    //	String url = host + "/UserAuthSet?$filter=Application%20eq%20%27PD%27";
                    //String url = host + "/ValidateSPIMEI?$filter=IMEI1%20eq%20%27123456789%27";
                    androidId = Settings.Secure.getString(ValidateIMEIActivity.this.getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                 //   String url = host + "/ValidateSPIMEI/?$format=json&IMEI1='" + androidId + "'";
                    String url = host + "/ValidateSPIMEI/?$format=json&IMEI1='" + androidId + "'&IMEI2='" + androidId + "'";

                    // String url1 = host + "/SPGeos";
                    /*353410103024281*/

                    try {
                        boolean isValidUser = false;
                        writeLogsToInternalStorage(ValidateIMEIActivity.this,"Validating Device ID Query"+url);

                        isValidUser = validateDeviceIMEINo(new URL(url), Configuration.UserName, Configuration.Password);
//                        isValidUser=false;

                        //   boolean isValidUser = validateDeviceIMEINo(new URL(url1), "T000000", "RSPLSystem@NonProd");
                        hideProgressDialog();
                        if (!isValidUser) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeLogsToInternalStorage(ValidateIMEIActivity.this,getString(R.string.androidid_error));

                                    showExitAlertMesssage(getString(R.string.androidid_error));
                                }
                            });


                        } else {
                            writeLogsToInternalStorage(ValidateIMEIActivity.this,"Device ID is Mapped");
                            //   Constants.requestConfigTypesetValues(ValidateIMEIActivity.this,Configuration.UserName, Configuration.Password);
                            getConfigTypeSetValues(Configuration.UserName, Configuration.Password);
                            /*String startTime = "8", endTime = "22", displacement = "22";
                            int timeInterval = 59;
                            SharedPreferences mPrefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
                            try {
                                SharedPreferences.Editor editor = mPrefs.edit();
                                editor.putString(getString(R.string.geo_start_time), startTime);
                                editor.putString(getString(R.string.geo_end_time), endTime);
                                editor.putInt(getString(R.string.geo_location_interval_time), timeInterval);
                                editor.putString(getString(R.string.geo_smallest_displacement), displacement);
                                editor.apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }*/
                            Intent intentLogView = new Intent(ValidateIMEIActivity.this, MainMenu.class);
                            startActivity(intentLogView);
                            finish();


                        }
                        //	String geo = postGeo(new URL(url1), Configuration.UserName, Configuration.Password,bodyObject.toString());
                        //String jsonValue =
                   /* if (!TextUtils.isEmpty(result)) {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject jsonObject1 = new JSONObject(jsonObject.getString("d"));
                        JSONArray jsonArray = jsonObject1.optJSONArray("results");
                        if (jsonArray != null && jsonArray.length() > 0) {
                            String puserID = jsonArray.getJSONObject(0).getString("SPGuid");
                        }
                    }*/
                    } catch (IOException var17) {
                        var17.printStackTrace();
                    } catch (Exception var18) {
                        var18.printStackTrace();
                    }
                }
            })).start();
        } else {
            Toast.makeText(this, "Network not available ,You'r in Offline", Toast.LENGTH_LONG).show();
        }
    }

    private void getConfigTypeSetValues(String userName, String psw) {
        writeLogsToInternalStorage(ValidateIMEIActivity.this,"Getting ConfigTypeSetValues"+userName+"-"+psw);

        String resultJson = "";
        String host = "https://" + Configuration.server_Text + "/" + Configuration.APP_ID;
        String url1 = host + "/ConfigTypsetTypeValues?$filter=Typeset eq 'SP' &$format=json";
        writeLogsToInternalStorage(ValidateIMEIActivity.this,"Getting ConfigTypeSetValues"+url1);
        URL url = null;
        try {
            url = new URL(url1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(Configuration.connectionTimeOut);
            connection.setConnectTimeout(Configuration.connectionTimeOut);
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
            writeLogsToInternalStorage(ValidateIMEIActivity.this,"Getting ConfigTypeSetValues respCode"+responseCode);

            connection.getResponseMessage();
            InputStream stream = null;

            if (responseCode != 200) {
                throw new IOException("HTTP error code: " + responseCode);
            } else if (responseCode == 200) {
               /* csrfToken = connection.getHeaderField("X-CSRF-Token");
                setCookies.addAll(connection.getHeaderFields().get("Set-Cookie"));*/
                stream = connection.getInputStream();
                if (stream != null) {
                    resultJson = readResponse(stream);
                }
                if (!TextUtils.isEmpty(resultJson)) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    JSONObject jsonObject1 = new JSONObject(jsonObject.getString("d"));
                    JSONArray jsonArray = jsonObject1.optJSONArray("results");
                    String startTime = "", endTime = "", displacement = "";
                    int timeInterval = 60;
                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("GEOSTRTTME"))
                                startTime = jsonArray.getJSONObject(i).getString("TypeValue");
                            else if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("GEOENDTME"))
                                endTime = jsonArray.getJSONObject(i).getString("TypeValue");
                            else if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("TIMEINTRVL")) {
                                timeInterval = jsonArray.getJSONObject(i).getInt("TypeValue");
                            } else if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("DISPDIST"))
                                displacement = jsonArray.getJSONObject(i).getString("TypeValue");
                        }
                        SharedPreferences mPrefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
                        try {
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString(getString(R.string.geo_start_time), startTime);
                            editor.putString(getString(R.string.geo_end_time), endTime);
                            editor.putInt(getString(R.string.geo_location_interval_time), timeInterval);
                            editor.putString(getString(R.string.geo_smallest_displacement), displacement);
                            editor.apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        writeLogsToInternalStorage(ValidateIMEIActivity.this,"Getting ConfigTypeSetValues response"+startTime+"-"+endTime+"-"+timeInterval+"-"+displacement);

                        // editor.putString(Constants.MobileNo,spGuid);

                    }
                }
            }

        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private void showExitAlertMesssage(String message) {
        UtilConstants.dialogBoxWithCallBack(ValidateIMEIActivity.this, "", message, getString(R.string.ok), "", false, new DialogCallBack() {
            @Override
            public void clickedStatus(boolean b) {
                if (b) {
                    finishAffinity();
                }
            }
        });
    }

    private void hideProgressDialog() {
        if (pdLoadDialog != null && pdLoadDialog.isShowing()) pdLoadDialog.dismiss();
    }

    private void showProgressDialog() {
        pdLoadDialog = new ProgressDialog(this, R.style.UtilsDialogTheme);
        pdLoadDialog.setMessage(getString(R.string.validation_androidID));
        pdLoadDialog.setCancelable(false);
        pdLoadDialog.show();
    }

    private boolean validateDeviceIMEINo(URL url, String userName, String psw) {
        boolean isVaildUser = false;
        String resultJson = "";
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(Configuration.connectionTimeOut);
            connection.setConnectTimeout(Configuration.connectionTimeOut);
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
                if (responseCode == 200) {
                    stream = connection.getInputStream();
                    if (stream != null) {
                        resultJson = readResponse(stream);
                    }
                } else {
                    stream = connection.getErrorStream();
                    if (stream != null) {
                        resultJson = readResponse(stream);
                    }
                }
                if (!TextUtils.isEmpty(resultJson)) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    JSONObject jsonObject1 = new JSONObject(jsonObject.getString("d"));
                    JSONArray jsonArray = jsonObject1.optJSONArray("results");
                    if (jsonArray != null && jsonArray.length() > 0) {
                        String spGuid = jsonArray.getJSONObject(0).getString("SPGuid");
                        String mobileNo = jsonArray.getJSONObject(0).getString("MobileNo");
                        //  String mobileNumber = jsonArray.getJSONObject(0).getString("SPGuid");
                        if (!TextUtils.isEmpty(spGuid) && !spGuid.equalsIgnoreCase("00000000-0000-0000-0000-000000000000")) {
                        if (!TextUtils.isEmpty(spGuid)) {
                            try {
                                SharedPreferences.Editor editor = sharedPerf.edit();
                                editor.putString("SPGUID", spGuid);
                                editor.putString("MobileNo", mobileNo);
                                editor.putBoolean(Constants.isFirstTimeValidation, true);
                                editor.putString(Constants.ValidatedDate, UtilConstants.getCurrentDate());
                                editor.apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            writeLogsToInternalStorage(ValidateIMEIActivity.this,"Validating Device ID Success resp SPGuid & MobNo"+spGuid+"-"+mobileNo);
                            // editor.putString(Constants.MobileNo,spGuid);
                            isVaildUser = true;
                        } }else isVaildUser = false;
                    }
                }


            }

        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
			/*if (connection != null) {
				connection.disconnect();
			}*/

        }


        return isVaildUser;
    }

    private static String readResponse(InputStream stream) throws IOException {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder buffer = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line).append('\n');
        }

        return buffer.toString();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int code = requestCode;

        /* else if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(MainMenu.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainMenu.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                startStep2(null);
            } else {
//                Toast.makeText(getBaseContext(),"Unable to start your service. Please enable your location permission",Toast.LENGTH_LONG).show();
            }
        }*/
//        TODo For location capturing
        /*else if(requestCode == REQUEST_GPS_PERMISSION){
            startStep3();
        }else if(requestCode == ConstantsUtils.DATE_SETTINGS_REQUEST_CODE){
            startBackGroundService();
        }*/
       /* if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                // You have permission
//                showDialog();
            }
        }*/
    }

    private List<String> getNetworkOperator(final Context context) {
        // Get System TELEPHONY service reference
        List<String> carrierNames = new ArrayList<>();
        try {
            final String permission = Manifest.permission.READ_PHONE_STATE;
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) && (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)) {
                final List<SubscriptionInfo> subscriptionInfos;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    subscriptionInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
                    for (int i = 0; i < subscriptionInfos.size(); i++) {
                        carrierNames.add(subscriptionInfos.get(i).getCarrierName().toString());
                    }
                }
            } else {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                // Get carrier name (Network Operator Name)
                carrierNames.add(telephonyManager.getNetworkOperatorName());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return carrierNames;
    }
}
