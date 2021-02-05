package com.arteriatech.geotrack.rspl.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.offline.OfflineManager;


/**
 * Created by e10769 on 03-03-2017.
 */

public class RefreshAsyncTask extends AsyncTask<String, Boolean, Boolean> {
    private Context mContext;
    private String refreshList;
    private UIListener uiListener;

    public RefreshAsyncTask(Context context, String refreshList, UIListener uiListener) {
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
        if (!OfflineManager.isOfflineStoreOpen()) {
            try {
                OfflineManager.openOfflineStore(mContext, uiListener);
            } catch (OfflineODataStoreException e) {
                e.printStackTrace();
                LogManager.writeLogError(Constants.error_txt + e.getMessage());
            }
        } else {
            if (!TextUtils.isEmpty(refreshList)) {
                try {
                    OfflineManager.refreshStoreSync(mContext, uiListener, Constants.Fresh, refreshList);
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                    LogManager.writeLogError(Constants.error_txt + e.getMessage());
                }
            }else {
                try {
                    OfflineManager.refreshStoreSync(mContext, uiListener, Constants.All, refreshList);
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
