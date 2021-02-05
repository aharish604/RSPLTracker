package com.arteriatech.geotrack.rspl.appmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;

import java.lang.reflect.Method;
import java.util.List;

public class AppManager {
    private static AppManager appManager;
    public final String PREF_GCM_ID = "USER_GCM_ID";
    private final String TAG = " AppManager ";
    /* access modifiers changed from: private */
    public String mGCMRegistrationId = "";


    public void getDataInterval() {
    }

    public void getHandlerInterval() {
    }

    public void getLocationDistance() {
    }

    public void getLocationInterval() {
    }

    private AppManager() {
    }

    public static AppManager getInstance() {
        if (appManager == null) {
            appManager = new AppManager();
        }
        return appManager;
    }

    public static boolean isOnline(Context context) {
        try {
            return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected();
        } catch (Exception unused) {
            return false;
        }
    }

    public boolean isConnected(Context context) {
        try {
            return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected();
        } catch (Exception unused) {
            return false;
        }
    }


    public static String getLocationMode(Context mContext) throws SettingNotFoundException {
        String locationMode="";
        switch (Secure.getInt(mContext.getContentResolver(), "location_mode")) {
            case 0:
                locationMode="0";
                break;
            case 1:
                locationMode="1";
                break;
            case 2:
                locationMode="2";
                break;
            case 3:
                locationMode="3";
                break;
        }

        return locationMode;
    }

    public boolean isDataConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Method declaredMethod = Class.forName(connectivityManager.getClass().getName()).getDeclaredMethod("getMobileDataEnabled", new Class[0]);
            declaredMethod.setAccessible(true);
            return ((Boolean) declaredMethod.invoke(connectivityManager, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean isMockSettingsON(Context context) {
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
        //return !Secure.getString(context.getContentResolver(), "mock_location").equals("0");
    }
    public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            // Get Permissions
            String[] requestedPermissions = packageInfo.requestedPermissions;

            if (requestedPermissions != null) {
                for (int i = 0; i < requestedPermissions.length; i++) {
                    if (requestedPermissions[i]
                            .equals("android.permission.ACCESS_MOCK_LOCATION")
                            && !applicationInfo.packageName.equals(context.getPackageName())) {
                        count++;
                    }
                }
            }
        }

        if (count > 0)
            return true;
        return false;
    }

    /*public static boolean isDMP(TrackerService trackerService) {
        return DeviceAdminUtil.isDeviceAdmin();
    }
*/


    @SuppressLint({"NewApi"})
    private boolean getAutoState(String str, Context context) {
        boolean z = false;
        try {
            if (Global.getInt(context.getContentResolver(), str) > 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }
}
