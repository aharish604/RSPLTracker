package com.arteriatech.geotrack.rspl.online;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.OnlineODataStoreException;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.mutils.log.TraceLog;
import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataEntitySet;
import com.sap.smp.client.odata.ODataError;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.ODataPropMap;
import com.sap.smp.client.odata.ODataProperty;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.store.ODataRequestExecution;
import com.sap.smp.client.odata.store.ODataRequestListener;
import com.sap.smp.client.odata.store.ODataResponse;
import com.sap.smp.client.odata.store.ODataResponseBatch;
import com.sap.smp.client.odata.store.ODataResponseBatchItem;
import com.sap.smp.client.odata.store.ODataResponseSingle;
import com.sap.smp.client.odata.store.impl.ODataResponseChangeSetDefaultImpl;

import java.util.List;
import java.util.Map;

/**
 * Created by e10769 on 02-05-2017.
 */

public class OnlineRequestListener implements ODataRequestListener {

    private static String TAG = "OnlineRequestListener";
    private final int SUCCESS = 0;
    private final int ERROR = -1;
    private UIListener uiListener;
    private String autoSync;
    private int operation;
    private Handler uiHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == SUCCESS) {
                // Notify the Activity the is complete
                String key = (String) msg.obj;
                TraceLog.d("requestsuccess - status message key" + key);
                try {
//                    if (autoSync != null && autoSync.equalsIgnoreCase("AutoSync")) {
//                        UpdatePendingRequests.getInstance().onRequestSuccess(operation, key);
//                    } else {
                    uiListener.onRequestSuccess(operation, key);
//                    }

                } catch (ODataException e) {
                    e.printStackTrace();
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == ERROR) {
                Exception e = (Exception) msg.obj;
//                if (autoSync != null && autoSync.equalsIgnoreCase("AutoSync")) {
//                    UpdatePendingRequests.getInstance().onRequestError(operation, e);
//                } else {
                uiListener.onRequestError(operation, e);
//                }

            }
        }
    };

    public OnlineRequestListener(int operation, UIListener uiListener) {
        super();
        this.operation = operation;
        this.uiListener = uiListener;
    }

    public OnlineRequestListener(int operation, String autoSync) {
        super();
        this.operation = operation;
        this.autoSync = autoSync;
    }

    /*****************
     * Methods that implements ODataRequestListener interface
     *****************/

    @Override
    public void requestCacheResponse(ODataRequestExecution request) {
        TraceLog.scoped(this).d("requestCacheResponse");

        ODataProperty property;
        ODataPropMap properties;
        //Verify request’s response is not null. Request is always not null
        if (request.getResponse() != null) {


            if (request.getResponse().isBatch()) {

            } else {
//Parse the response
                ODataResponseSingle response = (ODataResponseSingle) request.getResponse();
                if (response != null) {
                    //Get the response payload
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    if (feed != null) {
                        //Get the list of ODataEntity
                        List<ODataEntity> entities = feed.getEntities();
                        //Loop to retrieve the information from the response
                        for (ODataEntity entity : entities) {
                            //Obtain the properties you want to display in the screen
                            properties = entity.getProperties();
                            property = properties.get("");
                        }
                        //TODO - Send content to the screen
                    }
                }
            }

        }

    }


    @Override
    public void requestFailed(ODataRequestExecution request, ODataException e) {
        try {
            TraceLog.scoped(this).d("requestFailed");
            if (request != null && request.getResponse() != null) {
                if (request.getResponse().isBatch()) {

                } else {
                    ODataPayload payload = ((ODataResponseSingle) request.getResponse()).getPayload();
                    if (payload != null && payload instanceof ODataError) {
                        ODataError oError = (ODataError) payload;
                        TraceLog.d("requestFailed - status message " + oError.getMessage());
                        ConstantsUtils.APPROVALERRORMSG=oError.getMessage();
                        LogManager.writeLogError("Error :" + oError.getMessage());
                        notifyErrorToListener(new Exception(oError.getMessage()));
                        return;
                    }
                }
            }
            notifyErrorToListener(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void requestFinished(ODataRequestExecution request) {
        TraceLog.scoped(this).d("requestFinished");
    }

    @Override
    public void requestServerResponse(ODataRequestExecution request) {
        try {
            TraceLog.scoped(this).d("requestServerResponse");
            if (request != null && request.getResponse() != null) {
                if (request.getResponse().isBatch()) {
                    try {
                        ODataResponse oDataResponse = request.getResponse();
                        if (oDataResponse != null) {

                            ODataResponseBatch batchResponse = (ODataResponseBatch) oDataResponse;

                            List<ODataResponseBatchItem> responses = batchResponse.getResponses();

                            for (ODataResponseBatchItem response : responses) {

                                if (response instanceof ODataResponseChangeSetDefaultImpl) {

                                    ODataResponseChangeSetDefaultImpl changesetResponse = (ODataResponseChangeSetDefaultImpl) response;

                                    List<ODataResponseSingle> singles = changesetResponse.getResponses();

                                    for (ODataResponseSingle singleResponse : singles) {
                                        // Get individual response

                                        ODataPayload payload = singleResponse.getPayload();
                                        if (payload != null) {

                                            if (payload instanceof ODataError) {

                                                ODataError oError = (ODataError) payload;

                                                notifyErrorToListener(new OnlineODataStoreException(oError.getMessage()));
                                                break;
                                            } else {
                                                TraceLog.d("requestsuccess - status message before success");
                                                notifySuccessToListener(null);
                                            }
                                        } else {
                                            TraceLog.d("requestsuccess - status message before success");
                                            notifySuccessToListener(null);
                                        }
                                    }
                                }

                            }


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {

                    ODataResponseSingle response = (ODataResponseSingle) request.getResponse();
                    Map<ODataResponse.Headers, String> headerMap = response.getHeaders();
                    String code = headerMap.get(ODataResponse.Headers.Code);
                    TraceLog.d("requestServerResponse - status code " + code);
                    String eTag = headerMap.get(ODataResponse.Headers.ETag);
                    if (!TextUtils.isEmpty(eTag)) {
                        notifySuccessToListener(eTag);
                        return;
                    } else {

                    }


                    TraceLog.d("requestsuccess - status message before success");
                    notifySuccessToListener(null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestStarted(ODataRequestExecution request) {
        TraceLog.scoped(this).d("requestStarted");
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
        TraceLog.e("OnlineRequestListener::notifyError", exception);
    }

}
