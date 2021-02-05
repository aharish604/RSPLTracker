package com.arteriatech.geotrack.rspl.online;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.R;
import com.arteriatech.geotrack.rspl.interfaces.AsyncTaskCallBack;
import com.sap.smp.client.odata.online.OnlineODataStore;

/**
 * Created by e10860 on 11/13/2017.
 */

public class OpenOnlineManagerStore extends AsyncTask<String, Boolean, Boolean> {
    Context mContext;
    boolean isOnlineStoreOpened = false;
    private AsyncTaskCallBack asyncTaskCallBack;
    private String errorMessage = "";

    public OpenOnlineManagerStore(Context mContext, AsyncTaskCallBack asyncTaskCallBack) {
        this.mContext = mContext;
        this.asyncTaskCallBack = asyncTaskCallBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        OnlineStoreListener openListener = OnlineStoreListener.getInstance();
        OnlineODataStore store = openListener.getStore();
        if (store != null) {
            isOnlineStoreOpened = true;
        } else {
            try {
                isOnlineStoreOpened = OnlineManager.openOnlineStore(mContext, false);
            } catch (OnlineODataStoreException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            }
        }


        return isOnlineStoreOpened;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);
        if (asyncTaskCallBack != null) {
            if (!isOnlineStoreOpened && TextUtils.isEmpty(errorMessage))
                try {
                    if(Constants.Error_Msg.equalsIgnoreCase("")){
                        errorMessage = mContext.getString(R.string.alert_sync_cannot_be_performed);
                    }else{
                        if(Constants.Error_Msg.contains("401")){
                            errorMessage = Constants.PasswordExpiredMsg;
                        }else{
                            errorMessage = Constants.Error_Msg;
                        }

                    }
                } catch (Exception e) {
                    errorMessage = mContext.getString(R.string.alert_sync_cannot_be_performed);
                    e.printStackTrace();
                }

            asyncTaskCallBack.onStatus(isOnlineStoreOpened, errorMessage);
        }
    }
}
