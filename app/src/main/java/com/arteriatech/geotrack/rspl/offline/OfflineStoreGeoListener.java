package com.arteriatech.geotrack.rspl.offline;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.Operation;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.mutils.log.TraceLog;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.backgroundlocationtracker.TrackerService;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.offline.ODataOfflineStore;
import com.sap.smp.client.odata.offline.ODataOfflineStoreListener;
import com.sap.smp.client.odata.offline.ODataOfflineStoreNotification;
import com.sap.smp.client.odata.offline.ODataOfflineStoreState;

/**
 * Created by e10526 on 23-03-2018.
 */

public class OfflineStoreGeoListener implements ODataOfflineStoreListener {

    private UIListener uiListener;
    private int operation;

    private final int SUCCESS = 0;
    private final int ERROR = -1;

    private Handler uiHandler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == SUCCESS) {
                // Notify the Activity the is complete
                String key = (String) msg.obj;
                try {
                    uiListener.onRequestSuccess(operation, key);
                } catch (ODataException e) {
                    e.printStackTrace();
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == ERROR) {
                Exception e = (Exception) msg.obj;
                uiListener.onRequestError(operation, e);
            }
        }
    };


    public OfflineStoreGeoListener(UIListener uiListener) {
        super();
        this.operation = Operation.GetStoreOpen.getValue();
        this.uiListener = uiListener;
    }

    /*****************
     * Utils Methods
     *****************/


    /**
     * Notify the OnlineUIListener that the request was successful.
     */
    protected void notifySuccessToListener(String key) {
        Message msg = uiHandler.obtainMessage();
        msg.what = SUCCESS;
        msg.obj = key;
        uiHandler.sendMessage(msg);

    }

    /**
     * Notify the OnlineUIListener that the request has an error.
     *
     * @param exception an Exception that denotes the error that occurred.
     */
    protected void notifyErrorToListener(Exception exception) {
        Message msg = uiHandler.obtainMessage();
        msg.what = ERROR;
        msg.obj = exception;
        uiHandler.sendMessage(msg);
        TraceLog.e(Constants.OfflineStoreOpenFailed+" :", exception);
        LogManager.writeLogError(Constants.OfflineStoreOpenedFailed+" : "+ exception.getMessage());

    }



    @Override
    public void offlineStoreStateChanged(ODataOfflineStore oDataOfflineStore, ODataOfflineStoreState oDataOfflineStoreState) {
        TrackerService.setGeoStoreState(oDataOfflineStoreState.name());
        TraceLog.d(Constants.OfflineStoreStateChanged+" :" + oDataOfflineStoreState.toString());
    }

    @Override
    public void offlineStoreOpenFailed(ODataOfflineStore oDataOfflineStore, ODataException e) {
        TraceLog.e(Constants.OfflineStoreOpenFailed+" :", e);
        notifyErrorToListener(e);

    }

    @Override
    public void offlineStoreOpenFinished(ODataOfflineStore oDataOfflineStore) {
        TraceLog.d(Constants.OfflineStoreOpenFinished+" :" + oDataOfflineStore.toString());
        notifySuccessToListener(null);

    }

    @Override
    public void offlineStoreNotification(ODataOfflineStore oDataOfflineStore, ODataOfflineStoreNotification oDataOfflineStoreNotification) {

    }
}
