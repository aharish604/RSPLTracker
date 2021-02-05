package com.arteriatech.geotrack.rspl.registration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;

import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.mutils.login.LoginActivity;
import com.arteriatech.mutils.login.PasscodeLoginActivity;
import com.arteriatech.mutils.registration.MainMenuBean;
import com.arteriatech.mutils.registration.RegistrationModel;
import com.arteriatech.mutils.registration.UtilRegistrationActivity;
import com.arteriatech.mutils.security.AppLockManager;
import com.arteriatech.mutils.support.PasscodeActivity;
import com.arteriatech.geotrack.rspl.BuildConfig;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.MSFAGEOApplication;
import com.arteriatech.geotrack.rspl.R;
import com.arteriatech.geotrack.rspl.dashboard.MainMenu;
import com.arteriatech.geotrack.rspl.log.LogActivity;

import java.util.ArrayList;

public class RegistrationActivity extends AppCompatActivity {
    private boolean isNotification = false;
   // private ArrayList<String> noPasscodeClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.writeLogInfo("Registration/Login Loading Started");

        RegistrationModel registrationModel = new RegistrationModel();
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
        registrationModel.setRegisterSuccessActivity(MainMenu.class);
        registrationModel.setLogInActivity(LoginActivity.class);
        registrationModel.setAppActionBarIcon(R.mipmap.ic_action_bar_logo);
        registrationModel.setAppLogo(R.drawable.arteria_new_logo_transparent);
        registrationModel.setAppVersionName(BuildConfig.VERSION_NAME);
        registrationModel.setEmainId(getString(R.string.register_support_email));
        registrationModel.setPhoneNo(getString(R.string.register_support_phone));
        registrationModel.setEmailSubject("");//getString(R.string.email_subject)
        registrationModel.setMainActivity(MainMenu.class);
        registrationModel.setRegisterActivity(RegistrationActivity.class);
        registrationModel.setRegisterSuccessActivity(PasscodeActivity.class);
        /*security passcode*/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.getString(UtilConstants.QUICK_PIN, "");
        String permission = sharedPreferences.getString(UtilConstants.QUICK_PIN_ACCESS, "");
        String enablePermission = sharedPreferences.getString(UtilConstants.ENABLE_ACCESS, "");
        registrationModel.setLogInActivity(LoginActivity.class);
        if (UtilConstants.SECURITY_ON.equalsIgnoreCase(permission) && UtilConstants.SECURITY_ON.equalsIgnoreCase(enablePermission)) {
            registrationModel.setPasscodeLoginActivity(PasscodeLoginActivity.class);
        }
        ArrayList<MainMenuBean> mainMenuBeanArrayList = new ArrayList<>();
        MainMenuBean mainMenuBean = new MainMenuBean();
        mainMenuBean.setActivityRedirect(LogActivity.class);
        mainMenuBean.setMenuImage(R.drawable.ic_log_list);
        mainMenuBean.setMenuName("View");
        mainMenuBeanArrayList.add(mainMenuBean);
        registrationModel.setMenuBeen(mainMenuBeanArrayList);
//        registrationModel.setMainMenuActivity(LoginActivity.class);

        MSFAGEOApplication msfaApplication = (MSFAGEOApplication) getApplication();

        SharedPreferences sharedpre = this.getSharedPreferences(Constants.PREFS_NAME,0);
        String userName = sharedpre.getString("username",(String)null);
        try {
            if (TextUtils.isEmpty(userName)) {
                LogManager.writeLogInfo("Registration -UserName Empty");
                String userNameExtra = sharedpre.getString("usernameExtra", "");
                if (!TextUtils.isEmpty(userNameExtra)) {
                    SharedPreferences.Editor editor = sharedpre.edit();
                    editor.putString("username", userNameExtra);
                    editor.apply();
                    userName = sharedpre.getString("username",(String)null);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(TextUtils.isEmpty(userName)){
            SharedPreferences.Editor editor = sharedpre.edit();
            try {
                editor.putInt(Constants.CURRENT_VERSION_CODE, Constants.NewDefingRequestVersion);
                editor.putInt(Constants.INTIALIZEDB, Constants.IntializeDBVersion);
                editor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogManager.writeLogInfo("Registration/Login Ended");
        AppLockManager.getInstance().enableDefaultAppLockIfAvailable(msfaApplication,registrationModel);
        Intent intent = new Intent(RegistrationActivity.this, UtilRegistrationActivity.class);
        intent.putExtra(UtilConstants.RegIntentKey, registrationModel);
        startActivity(intent);
        finish();

    }

}
