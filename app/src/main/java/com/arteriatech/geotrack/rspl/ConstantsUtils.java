package com.arteriatech.geotrack.rspl;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.arteriatech.geotrack.rspl.BuildConfig;
import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.geotrack.rspl.autosync.AutoSyncDataLocationAlarmReceiver;
import com.arteriatech.geotrack.rspl.backgroundlocationtracker.TrackerService;
import com.arteriatech.geotrack.rspl.offline.OfflineManager;

import java.math.BigDecimal;

/**
 * Created by e10769 on 27-04-2017.
 */

public class ConstantsUtils {

    public static final int ITEM_MAX_LENGTH = 6;
    public static final int ITEM_MAX_LENGTH_3 = 3;
    public static final String EXTRA_ARRAY_LIST = "arrayList";
    public static final String MAXCLMDOC = "MAXCLMDOC";
    public static final String MAXREGDOC = "MAXREGDOC";
    public static final String ZDMS_SCCLM = "ZDMS_SCCLM";
    public static final String EXTRA_FROM = "comingFrom";
    public static final String DISC_PERCENTAGE = "Disc %";
    public static final String FREE_QTY = "Free Qty";
    public static final String TargetBasedID = "TargetBasedID";
    public static final String Training = "Training";
    public static final String Meeting = "Meeting";
    public static final String B = "B";
    public static final String C = "C";
    public static final int ACTIVITY_RESULT_FILTER = 850;
    public static final int ACTIVITY_RESULT_MATERIAL = 750;
    public static final int ADD_MATERIAL = 30;
    public static final int SO_CREATE_SINGLE_MATERIAL = 31;
    public static final int SO_EDIT_SINGLE_MATERIAL = 32;
    public static final int SO_SINGLE_MATERIAL = 33;
    public static final int SO_VIEW_SELECTED_MATERIAL = 34;
    public static final int SO_MULTIPLE_MATERIAL = 4;
    public static final int SO_CREATE_ACTIVITY = 1;
    public static final int SO_CREATE_CC_ACTIVITY = 2;
    public static final int SO_EDIT_ACTIVITY = 3;
    public static final int SO_APPROVAL_EDIT_ACTIVITY = 36;
    /*session type*/
    public static final int NO_SESSION = 0;// session passing only  app header
    public static final int SESSION_HEADER = 1;// session passing only  app header
    public static final int SESSION_QRY = 2;// session passing only qry
    public static final int SESSION_QRY_HEADER = 3;// session passing both app header and qry
    public static final String BannerDesc = "BannerDesc";
    public static final String ProductCatDesc = "ProductCatDesc";
    public static final String DISC_AMOUNT = "Disc Amount";
    public static final String ProductCatID = "ProductCatID";
    public static final String ROUTE_INSTANCE_ID = "routeInstanceId";
    public static final String ROUTE_ENTITY_KEY = "routeEntityKey";
    public static final int SWIPE_REFRESH_DISTANCE = 300;
    public static final int SWIPE_REFRESH_DISABLE = 999999;
    public static final int DATE_SETTINGS_REQUEST_CODE = 998;
    public static final String EXTRA_COMING_FROM = "comingFrom";
    public static final String MONTH_CURRENT = "CurrentMonth";
    public static final String MONTH_NEXT = "NextMonth";
    public static final String MTP_SUBORDINATE = "mtpSubOrdinate";
    public static final String MTP_SUBORDINATE_CURRENT = "mtpSubOrdinateCurrent";
    public static final String MTP_SUBORDINATE_NEXT = "mtpSubOrdinateNext";
    public static final String RTGS_SUBORDINATE = "RTGSSubOrdinate";
    public static final String RTGS_SUBORDINATE_CURRENT = "RTGSSubOrdinateCurrent";
    public static final String RTGS_SUBORDINATE_NEXT = "RTGSSubOrdinateNext";
    public static final String MTP_APPROVAL = "mtpApproval";
    public static final String MONTH_TODAY = "Today";
    public static final String EXTRA_DATE = "extraDate";
    public static final String EXTRA_SPGUID = "extraSPGUID";
    public static final String EXTRA_ExternalRefID = "extraExternalRefID";
    public static final String EXTRA_ISASM_LOGIN = "extraIsASMLogin";
    public static final String EXTRA_POS = "extraPOS";
    private static final String MC = "MC";
    private static final String DAYEND = "DAYEND";
    public static String ApprovalStatusID = "ApprovalStatusID";
    public static String APPROVALERRORMSG = "";
    public static int SO_RESULT_CODE = 2300;
    public static Toast toast = null;
    public static String messageToToast = "";
    public static PendingIntent alarmPendingIntent;


    public static void setProgressColor(Context mContext, SwipeRefreshLayout swipeRefresh) {
        swipeRefresh.setDistanceToTriggerSync(ConstantsUtils.SWIPE_REFRESH_DISTANCE);
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
    }

    public static String getLastSeenDateFormat(Context context, long smsTimeInMilis) {
        return UtilConstants.getLastSeenDateFormat(context, smsTimeInMilis);
    }

    public static ProgressDialog showProgressDialog(Context mContext) {
        ProgressDialog pdLoadDialog = null;
        try {
            pdLoadDialog = new ProgressDialog(mContext, R.style.ProgressDialogTheme);
            pdLoadDialog.setMessage(mContext.getString(R.string.app_loading));
            pdLoadDialog.setCancelable(false);
            pdLoadDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdLoadDialog;
    }


    public static ProgressDialog showProgressDialog(Context mContext, String message) {
        ProgressDialog pdLoadDialog = null;
        try {
            pdLoadDialog = new ProgressDialog(mContext, R.style.ProgressDialogTheme);
            pdLoadDialog.setMessage(message);
            pdLoadDialog.setCancelable(false);
            pdLoadDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdLoadDialog;
    }


    public static void initActionBarView(final AppCompatActivity mActivity, Toolbar toolbar, boolean homeUpEnabled) {
        mActivity.setSupportActionBar(toolbar);
        if (homeUpEnabled) {
            final Drawable upArrow = mActivity.getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(mActivity.getResources().getColor(R.color.dimgray), PorterDuff.Mode.SRC_ATOP);
            mActivity.getSupportActionBar().setHomeAsUpIndicator(upArrow);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
//        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
//        mActivity.getSupportActionBar().setIcon(R.drawable.ic_msfa);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        mActivity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mActivity.getResources().getColor(R.color.dimgray)));
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.actionbar_center_img_lay, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mActivity.getSupportActionBar().setDisplayShowCustomEnabled(true);
        mActivity.getSupportActionBar().setCustomView(view, params);
    }

    /*actionbar center image*/
    public static void initActionBarView(AppCompatActivity mActivity, Toolbar toolbar, boolean homeUpEnabled, String title, int appIcon) {
        com.arteriatech.mutils.actionbar.ActionBarView.initActionBarView(mActivity, toolbar, homeUpEnabled, title, appIcon, 0);
    }

    public static void printDebugLog(String message) {
        Log.d("mSFADebuLog-RSPL", "debuLog : " + message);
//        LogManager.writeLogDebug("debuLog : " + message);
    }


    public static void displayErrorDialog(Context mContext, String message) {
        if (!TextUtils.isEmpty(message)) {
            UtilConstants.dialogBoxWithCallBack(mContext, "", message, mContext.getString(R.string.ok), "", false, null);
        } else {
            UtilConstants.dialogBoxWithCallBack(mContext, "", mContext.getString(R.string.msg_no_network), mContext.getString(R.string.ok), "", false, null);
        }
    }

    public static int getFirstTimeRun(Context mContext) {
        SharedPreferences sp = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
        int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int lastVersionCode = sp.getInt(Constants.KEY_FIRST_TIME_RUN, -1);
        if (lastVersionCode == -1) result = 0; else
            result = (lastVersionCode == currentVersionCode) ? 1 : 2;
        sp.edit().putInt(Constants.KEY_FIRST_TIME_RUN, currentVersionCode).apply();
        return result;
    }


    public static void serviceReSchedule(Context context) {
        context.stopService(new Intent(context, TrackerService.class));
          if (!isMyServiceRunning(TrackerService.class, context))
                    context.startService(new Intent(context, TrackerService.class));
    }

    public static boolean isAutomaticTimeZone(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return android.provider.Settings.Global.getInt(mContext.getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0) == 1;
//                return Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(mContext.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    public static void showAutoDateSetDialog(final Activity mContext) {
        UtilConstants.dialogBoxWithCallBack(mContext, "", mContext.getString(R.string.autodate_change_msg), mContext.getString(R.string.autodate_change_btn), "", false, new com.arteriatech.mutils.interfaces.DialogCallBack() {
            @Override
            public void clickedStatus(boolean b) {
                mContext.startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), DATE_SETTINGS_REQUEST_CODE);
            }
        });
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass, Context mContext) {
        try {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static void stopAlarmManagerByID(Context sContetx, Class<?> cls, int requestID) {
        Intent intent = new Intent(sContetx, cls);
        AlarmManager alarmManager = (AlarmManager) sContetx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(sContetx.getApplicationContext(), requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (pendingIntent != null) {
            pendingIntent.cancel();
            alarmManager.cancel(pendingIntent);
        }

       /* myIntent = new Intent(SetActivity.this, AlarmActivity.class);
        pendingIntent = PendingIntent.getActivity(CellManageAddShowActivity.this,
                id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);*/


    }

    public static BigDecimal decimalRoundOff(BigDecimal bigDecimalValue, int numberOfDigitsAfterDecimalPoint) {
        bigDecimalValue = bigDecimalValue.setScale(numberOfDigitsAfterDecimalPoint,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimalValue;
    }
    public static String getLocationAutoSyncTimeInMin() {
        try {
            //time in minutes
            return OfflineManager.getValueByColumnName(Constants.ConfigTypsetTypeValues + "?$filter=" + Constants.Typeset + " eq '" +
                    Constants.SF + "' and " + Constants.Types + " eq '" + Constants.GEOAUTOSYN + "' &$top=1", Constants.TypeValue);
        } catch (OfflineODataStoreException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static void startAutoSyncLocation(Context mContext, boolean isForceReset) {
        try {
            if (ConstantsUtils.isAutomaticTimeZone(mContext)) {
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
                Constants.isSync = false;
                String autoSyncTime = ConstantsUtils.getLocationAutoSyncTimeInMin();  /*"15"*/
                // autoSyncTime = "2";
                ;
                if (TextUtils.isEmpty(autoSyncTime)) {
                    autoSyncTime = "5";
                }
                if (isForceReset || !sharedPreferences.getString("LocationServiceAutoSync", "").equalsIgnoreCase(autoSyncTime)) {
                    if (!TextUtils.isEmpty(autoSyncTime)) {
                        // UpdatePendingLatLongRequest.getInstance(null).callSchedule(autoSyncTime);
                        Intent intent = new Intent(mContext.getApplicationContext(), AutoSyncDataLocationAlarmReceiver.class);
                        // Create a PendingIntent to be triggered when the alarm goes off
                        alarmPendingIntent = PendingIntent.getBroadcast(mContext, AutoSyncDataLocationAlarmReceiver.REQUEST_CODE,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        // Setup periodic alarm every 5 seconds
                        long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
                        int intervalMillis = 1000 * 60 * Integer.parseInt(autoSyncTime); // as of API 19, alarm manager will be forced up to 60000 to save battery
                        AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        // See https://developer.android.com/training/scheduling/alarms.html
                        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, alarmPendingIntent);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("LocationServiceAutoSync", autoSyncTime);
                        editor.apply();
                    }
                }
            }else{
                LogManager.writeLogError("Auto Location Sync not started because date is not valid ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.writeLogError(Constants.error_txt + e.getMessage());
        }
    }

    public static void displayLongToast(Context mContext, String message) {
        try {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void focusOnView(final NestedScrollView nestedScroll) {
        nestedScroll.post(new Runnable() {
            @Override
            public void run() {
                nestedScroll.scrollTo(0, 0);
            }
        });
    }

    public static androidx.appcompat.app.AlertDialog.Builder showAlert(String message, Context context, DialogInterface.OnClickListener listener) {
        androidx.appcompat.app.AlertDialog.Builder builder = null;
        try {
            builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.MyTheme);
            builder.setMessage(message).setCancelable(false).setPositiveButton("Ok", listener);
            builder.show();
        } catch (Exception var3) {
            var3.printStackTrace();
        }
        return builder;
    }


}

