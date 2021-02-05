package com.arteriatech.geotrack.rspl.systemdeviceadmin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.interfaces.DialogCallBack;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.R;
import com.arteriatech.geotrack.rspl.ValidateIMEIActivity;
import com.arteriatech.geotrack.rspl.dashboard.MainMenu;

import static com.arteriatech.geotrack.rspl.Constants.writeLogsToInternalStorage;

public class MainActivity extends Activity implements OnCheckedChangeListener {
    private static final String CNAME = MainActivity.class.getSimpleName();

    private ToggleButton toggleButton;
    private String androidId = "";
    private boolean isResumed = false;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private int requestPermissionCode = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTheme(R.style.AppThemeNoActionBar);
        toggleButton = (ToggleButton) super.findViewById(R.id.toggle_device_admin);
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
                writeLogsToInternalStorage(MainActivity.this,"Device Admin Activity Permission Granted");
                init();
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
                    writeLogsToInternalStorage(MainActivity.this,"Device Admin Activity Permission Granted");
                    init();
                } else {
                    verifyStoragePermissions(this);
                }
                return;
            }
        }
    }



    public void init() {
        androidId = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        DeviceAdminUtil.initDPM(this);
        DeviceAdminUtil.initComponent(this, DeviceAdminDemoReceiver.class);

        boolean admin = DeviceAdminUtil.isDeviceAdmin();
        Log.i(CNAME, "admin : " + admin);
        toggleButton.setChecked(admin);
        toggleButton.setOnCheckedChangeListener(this);

        if (!DeviceAdminUtil.isDeviceAdmin()) {

            // Activate device administration
            writeLogsToInternalStorage(MainActivity.this,"Device Admin Disabled");

            DeviceAdminUtil.registerDeviceAdmin(this, DeviceAdminUtil.DEVICE_ADMIN_REQUEST);
        } else {
            writeLogsToInternalStorage(MainActivity.this,"Device Admin Permission Enabled");
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
            boolean isFirstValidation = sharedPreferences.getBoolean(Constants.isFirstTimeValidation, false);
            String validatedDeviceID = sharedPreferences.getString(Constants.ValidatedDate,"");
         //   if (!isFirstValidation || (!TextUtils.isEmpty(validatedDeviceID) && !validatedDeviceID.equalsIgnoreCase(UtilConstants.getCurrentDate())) ) {
            if (!isFirstValidation) {
                writeLogsToInternalStorage(MainActivity.this,"Device Admin deviceID Not Validated");
                UtilConstants.dialogBoxWithCallBack(MainActivity.this, "Device ID", androidId, "Validate", "Share Id", false, new DialogCallBack() {
                    @Override
                    public void clickedStatus(boolean b) {
                        if (b) {
                            Intent intentLogView = new Intent(MainActivity.this, ValidateIMEIActivity.class);
                            startActivity(intentLogView);
                            finish();
                        } else {
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Device ID");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Device ID - " + androidId);
                            startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        }
                    }
                });

            } else {
                writeLogsToInternalStorage(MainActivity.this,"Device Admin Already Device ID Validated");
                Intent intentLogView = new Intent(this, MainMenu.class);
                startActivity(intentLogView);
                finish();
            }
        }
		/*} else {
			DeviceAdminUtil.unregisterDeviceAdmin();
			Log.i(CNAME, "Device Admin Disabled");
			Toast.makeText(this, "Device Admin Disabled", Toast.LENGTH_SHORT).show();
		}*/
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		/*if (isChecked) {
			if (DeviceAdminUtil.isDeviceAdmin())
				return;

			// Activate device administration
			DeviceAdminUtil.registerDeviceAdmin(this, DeviceAdminUtil.DEVICE_ADMIN_REQUEST);
		} else {
			DeviceAdminUtil.unregisterDeviceAdmin();
			Log.i(CNAME, "Device Admin Disabled");
			Toast.makeText(this, "Device Admin Disabled", Toast.LENGTH_SHORT).show();
		}*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DeviceAdminUtil.DEVICE_ADMIN_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(CNAME, "Administration enabled!");
                    //	Toast.makeText(this, "Administration enabled!", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
                    boolean isFirstValidation = sharedPreferences.getBoolean(Constants.isFirstTimeValidation, false);
                    if (!isFirstValidation) {
                        writeLogsToInternalStorage(MainActivity.this,"Device Admin deviceID Not Validated");
                        UtilConstants.dialogBoxWithCallBack(MainActivity.this, "Device ID", androidId, "Validate", "Share Id", false, new DialogCallBack() {
                            @Override
                            public void clickedStatus(boolean b) {
                                if (b) {
                                    Intent intentLogView = new Intent(MainActivity.this, ValidateIMEIActivity.class);
                                    startActivity(intentLogView);
                                    finish();
                                } else {
                                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                    sharingIntent.setType("text/plain");
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Device ID");
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Device ID - " + androidId);
                                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                                }
                            }
                        });
                    } else {
                        writeLogsToInternalStorage(MainActivity.this,"Device Admin deviceID  Validated");
                        Intent intentLogView = new Intent(this, MainMenu.class);
                        startActivity(intentLogView);
                        finish();
                    }
                } else {
                    Log.i(CNAME, "Administration enable FAILED!");
                    toggleButton.setChecked(false);
                }

                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isResumed) {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
            boolean isFirstValidation = sharedPreferences.getBoolean(Constants.isFirstTimeValidation, false);
            if (!isFirstValidation) {
                UtilConstants.dialogBoxWithCallBack(MainActivity.this, "Device ID", androidId, "Validate", "Share Id", false, new DialogCallBack() {
                    @Override
                    public void clickedStatus(boolean b) {
                        if (b) {
                            Intent intentLogView = new Intent(MainActivity.this, ValidateIMEIActivity.class);
                            startActivity(intentLogView);
                            finish();
                        } else {
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Device ID");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Device ID - " + androidId);
                            startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        }
                    }
                });

            }
        }
    }

}
