package com.arteriatech.geotrack.rspl.asyncTask;

import android.content.Context;
import android.os.AsyncTask;

import com.arteriatech.mutils.common.UIListener;

/**
 * Created by e10769 on 03-03-2017.
 */

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
        /*if (!OfflineManager.isOfflineStoreOpenGeo()) {
            try {
                OfflineManager.openOfflineStoreGeo(mContext, uiListener);
            } catch (OfflineODataStoreException e) {
                e.printStackTrace();
                LogManager.writeLogError(Constants.error_txt + e.getMessage());
            }
        } else {
            if (!TextUtils.isEmpty(refreshList)) {
                try {
                    OfflineManager.refreshGeoStoreSync(mContext, uiListener, Constants.Fresh, refreshList);
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                    LogManager.writeLogError(Constants.error_txt + e.getMessage());
                }
            }else {
                try {
                    OfflineManager.refreshGeoStoreSync(mContext, uiListener, Constants.All, refreshList);
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                    LogManager.writeLogError(Constants.error_txt + e.getMessage());
                }
            }
        }*/
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

    }
}
