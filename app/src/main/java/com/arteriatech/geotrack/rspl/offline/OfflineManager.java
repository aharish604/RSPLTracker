package com.arteriatech.geotrack.rspl.offline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.arteriatech.mutils.common.OfflineError;
import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.Operation;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.common.UtilOfflineManager;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.geotrack.rspl.BuildConfig;
import com.arteriatech.geotrack.rspl.Config;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.R;
import com.arteriatech.geotrack.rspl.SPGeo.database.DatabaseHelperGeo;
import com.arteriatech.geotrack.rspl.SalesPersonBean;
import com.arteriatech.geotrack.rspl.backgroundlocationtracker.TrackerService;
import com.arteriatech.geotrack.rspl.registration.Configuration;
import com.arteriatech.geotrack.rspl.registration.RegistrationActivity;
import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.maf.tools.logon.core.LogonCoreContext;
import com.sap.smp.client.httpc.HttpConversationManager;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataEntitySet;
import com.sap.smp.client.odata.ODataGuid;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.ODataPropMap;
import com.sap.smp.client.odata.ODataProperty;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.impl.ODataEntityDefaultImpl;
import com.sap.smp.client.odata.impl.ODataErrorDefaultImpl;
import com.sap.smp.client.odata.impl.ODataGuidDefaultImpl;
import com.sap.smp.client.odata.impl.ODataPropertyDefaultImpl;
import com.sap.smp.client.odata.offline.ODataOfflineStore;
import com.sap.smp.client.odata.offline.ODataOfflineStoreOptions;
import com.sap.smp.client.odata.offline.ODataOfflineStoreState;
import com.sap.smp.client.odata.store.ODataRequestParamSingle;
import com.sap.smp.client.odata.store.ODataRequestParamSingle.Mode;
import com.sap.smp.client.odata.store.ODataResponseSingle;
import com.sap.smp.client.odata.store.ODataStore;
import com.sap.smp.client.odata.store.impl.ODataRequestParamSingleDefaultImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class OfflineManager {
    public static final String TAG = OfflineManager.class.getSimpleName();
    public static ODataOfflineStore offlineStore;
    public static ODataOfflineStoreOptions options = null;
    public static boolean isOfflineStoreOpen() {
        boolean isOpen = false;
        if (offlineStore != null && ODataOfflineStoreState.ODataOfflineStoreOpen.name().equals(UtilOfflineManager.getStoreState())) {
            isOpen = true;
        }
        return isOpen;
    }

    /**
     * Initialize a new offline odata store
     *
     * @param context the application context
     * @return true if it's initialized, false otherwise
     * @throws OfflineODataStoreException
     */
    public static boolean openOfflineStore(Context context, UIListener uiListener) throws OfflineODataStoreException {
        if (!isOfflineStoreOpen()) {
            try {
                //This instantiate the native UDB libraries which are located in the

                ODataOfflineStore.globalInit();
                //Get application endpoint URL
                LogonCoreContext lgCtx = null;
                try {
                    lgCtx = LogonCore.getInstance().getLogonContext();
                }catch (Throwable e){
                    e.getMessage();
                }
                if(lgCtx!=null) {
                    String endPointURL = lgCtx.getAppEndPointUrl();
                    URL url = new URL(endPointURL);
                    // Define the offline store options.
                    // Connection parameter and credentials and
                    // the application connection id we got at the registration

                    options = new ODataOfflineStoreOptions();
                    options.storeEncryptionKey = Constants.EncryptKey;
                    options.host = url.getHost();
                    options.port = String.valueOf(url.getPort());
                    options.enableHTTPS = lgCtx.isHttps();
                    options.enableRepeatableRequests = true;
                    // the serviceRoot is the backend connector name, which is usually the same
                    // as the application configuration name in the SMP Management Cockpit
                    options.serviceRoot = Configuration.APP_ID;

                    //for Relay enable next line
//				options.urlSuffix = lgCtx.getResourcePath()+"/"+lgCtx.getFarmId();

                    //The logon configurator uses the information obtained in the registration
                    // (i.e endpoint URL, login, etc ) to configure the conversation manager
                   /* IManagerConfigurator configurator =
                            LogonUIFacade.getInstance().getLogonConfigurator(context);*/
                    HttpConversationManager manager = new HttpConversationManager(context);
              //      configurator.configure(manager);
                    options.conversationManager = manager;
                    options.storeName = Constants.STORE_NAME;

                    options.customHeaders.put(Constants.arteria_dayfilter, Constants.NO_OF_DAYS);
//                options.customHeaders.put("Content-Type", "application/atom+xml");
                    String[] DEFINGREQARRAY = Constants.getDefinigReq(context);
                    for (int incReq = 0; incReq < DEFINGREQARRAY.length; incReq++) {
                        String collectionName = DEFINGREQARRAY[incReq];
                        if (collectionName.contains("?")) {
                            String splitCollName[] = collectionName.split("\\?");
                            collectionName = splitCollName[0];
                        }


                        if (collectionName.equalsIgnoreCase(Constants.MerchReviewImages)
                                || collectionName.equalsIgnoreCase(Constants.ExpenseDocuments)) {
                            options.addDefiningRequest(collectionName, DEFINGREQARRAY[incReq], true);
                        } else {
                            options.addDefiningRequest(collectionName, DEFINGREQARRAY[incReq], false);


                        }
                    }

                    offlineStore = new ODataOfflineStore(context);
                    OfflineStoreListner offlineStoreListner = new OfflineStoreListner(uiListener);
                    offlineStore.setOfflineStoreListener(offlineStoreListner);
                    //Assign an Offline
                    offlineStore.setRequestErrorListener(new OfflineErrorListener());

                    offlineStore.openStoreSync(options);
                    return true;
                }else{
                    restartApplication(context);
                    return false;
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        } else {
            return true;
        }
        //END
    }

    private static void restartApplication(final Context mcontext) {

        ((Activity) mcontext).runOnUiThread(new Runnable() {
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        mcontext, R.style.MyTheme);

                builder.setMessage(R.string.was_idle)
                        .setCancelable(false)
                        .setPositiveButton(
                                R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        dialog.cancel();
                                        //  activity.finishAffinity();
                                        ((Activity) mcontext).finishAffinity();
                                        Intent dialogIntent = new Intent(mcontext, RegistrationActivity.class);
                                        mcontext.startActivity(dialogIntent);


                                    }
                                });
                builder.show();
            }
        });


    }

    /*
     * Closes existing or opened Offline store if it is not null
     * @param uiListener the activity that will receive the response to notify the user
     * @throws OfflineODataStoreException
     * */
    public static boolean closeOfflineStore() throws OfflineODataStoreException {
        if (isOfflineStoreOpen()) {
            try {
                UtilOfflineManager.setStoreState(ODataOfflineStoreState.ODataOfflineStoreClosed.name());
                OfflineManager.offlineStore.closeStore();
                return true;
            } catch (ODataException e) {
                LogManager.writeLogError(Constants.offline_store_not_closed + e.getMessage());
                throw new OfflineODataStoreException(e);
            }
        } else {
            return false;
        }
    }

    public static boolean closeOfflineStore(Context context, ODataOfflineStoreOptions options) throws OfflineODataStoreException {
        try {
            UtilOfflineManager.setStoreState(ODataOfflineStoreState.ODataOfflineStoreClosed.name());
            UtilOfflineManager.closeOfflineStore(context, options, OfflineManager.offlineStore, Constants.PREFS_NAME);
            return true;
        } catch (Exception e) {
            LogManager.writeLogError(Constants.offline_store_not_closed + e.getMessage());
            throw new OfflineODataStoreException(e);
        }
    }

    /*
     * refresh offline store data for collections passed as input parameter to this method
     * @param uiListener the activity that will receive the response to notify the user
     * @throws OfflineODataStoreException
     * */
    public static void refreshStoreSync(Context context, UIListener uiListener, String syncType, String defineReq) throws OfflineODataStoreException {

        if (offlineStore != null) {
            try {
                if(syncType.equalsIgnoreCase(Constants.Fresh)){
                    LogManager.writeLogDebug("Download Sync In progress");
                }else if(syncType.equalsIgnoreCase(Constants.ALL)){
                    LogManager.writeLogDebug("All Sync In progress");
                }
                OfflineRefreshListener refreshListener = new OfflineRefreshListener(context, syncType, defineReq, uiListener);

                if (syncType.equals(Constants.Fresh)) {
                    offlineStore.scheduleRefresh(defineReq, refreshListener);
                } else {
                    offlineStore.scheduleRefresh(refreshListener);
                }

            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
    }



    /**
     * returns Login Name (Sales person Name) from offline store based on query
     *
     * @throws OfflineODataStoreException
     */
    public static String getLoginName(String strQry) throws OfflineODataStoreException {

        String conctName = "", mstrLastName = "";
        //Check if the offline oData store is initialized
        if (offlineStore != null) {

            ODataProperty property;
            ODataPropMap properties;
            try {
                //Define the resource path
                String resourcePath = strQry;
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(resourcePath);
                //Send a request to read the Attendances from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineStore.
                        executeRequest(request);

                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();


                    for (ODataEntity entity : entities) {
                        properties = entity.getProperties();
                        property = properties.get(Constants.LastName);
                        mstrLastName = property.getValue().toString();
                        conctName = mstrLastName;
                    }
                }

            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return conctName;

    }

    /**
     * returns Sales persons Mobile number from offline store based on query
     *
     * @throws OfflineODataStoreException
     */
    public static String getSalePersonMobileNo(String strQry) throws OfflineODataStoreException {


        String mobileNoVal = "";
        //Check if the offline oData store is initialized
        if (offlineStore != null) {

            ODataProperty property;
            ODataPropMap properties;
            try {
                //Define the resource path
                String resourcePath = strQry;
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(resourcePath);
                //Send a request to read the Attendances from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineStore.
                        executeRequest(request);

                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();


                    for (ODataEntity entity : entities) {
                        properties = entity.getProperties();

                        property = properties.get(Constants.SalesPersonMobileNo);
                        mobileNoVal = property.getValue().toString();
                    }
                }

            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return mobileNoVal;

    }

    /**
     * Get the conflict errors registered in the ErrorArchive
     *
     * @return List of Errors
     * @throws OfflineODataStoreException
     */
    public static List<OfflineError> getErrorArchive() throws OfflineODataStoreException {
        ArrayList<OfflineError> errorList = new ArrayList<>();
        if (offlineStore != null) {
            OfflineError offlineError;
            ODataProperty property;
            ODataPropMap properties = null;
            try {
                String resourcePath = Constants.ERROR_ARCHIVE_COLLECTION;
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(resourcePath);
                ODataResponseSingle response = (ODataResponseSingle) offlineStore.executeRequest(request);
                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl) response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();
                    for (int k = entities.size() - 1; k >= 0; k--) {
                        ODataEntity entity = entities.get(k);
                        properties = entity.getProperties();
                        property = properties.get(Constants.ERROR_ARCHIVE_ENTRY_MESSAGE);
                        String mStrGetMsg = (String) property.getValue();
                        String errorMsg = "";
                        try {
                            JSONObject jsonObject = new JSONObject(mStrGetMsg);
                            JSONObject errorJson = (JSONObject) jsonObject.get(Constants.error);
                            JSONObject errorMsgJson = (JSONObject) errorJson.get(Constants.message);
                            errorMsg = (String) errorMsgJson.get(Constants.value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        property = properties.get(Constants.ERROR_ARCHIVE_ENTRY_REQUEST_URL);
                        String requestURL = (String) property.getValue();
                        String repalceString = "";
                        if (requestURL != null && !requestURL.equalsIgnoreCase("")) {
                            if (requestURL.indexOf("/") != -1) {
                                repalceString = requestURL.replace("/", "");
                            } else {
                                repalceString = requestURL;
                            }
                        }
                        String errMsg = null;
                        try {
                            errMsg = errorMsg.equalsIgnoreCase("") ? mStrGetMsg : errorMsg;
                        } catch (Exception e) {
                            errMsg = "";
                            e.printStackTrace();
                        }
                        LogManager.writeLogError(repalceString + " : " + errMsg);

                        if (repalceString.contains("guid")) {
                            try {

                                String requiredString = "";
                                requiredString = repalceString.replace(repalceString.substring(repalceString.indexOf("("), repalceString.length()), "");
                                Constants.AL_ERROR_MSG.add(requiredString + " : " + errMsg);
                                Constants.Entity_Set.add(requiredString);
                            } catch (Exception e) {
                                Constants.Entity_Set.add(repalceString);
                                Constants.AL_ERROR_MSG.add(repalceString + " : " + errMsg);
                            }
                        } else {
                            Constants.AL_ERROR_MSG.add(repalceString + " : " + errMsg);
                        }

                        property = properties.get(Constants.ERROR_ARCHIVE_ENTRY_MESSAGE);
                        offlineError = new OfflineError((String) property.getValue());
                        offlineError.setEditResourcePath(entity.getEditResourcePath());
                        deleteErrorArchiveEntity(entity);
                        Log.d(Constants.error_txt1, Constants.error_archive_called_txt);
                    }
                } else {
                    throw new OfflineODataStoreException(Constants.invalid_payload_entityset_expected + response.getPayloadType().name());
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return errorList;
    }


    /**
     * Delete error archive on offline store
     *
     * @throws OfflineODataStoreException
     */
    public static void deleteErrorArchiveEntity(ODataEntity oDataEntity) throws OfflineODataStoreException {
        //BEGIN
        if (offlineStore == null) return;
        try {
            //Get Edit resource path
            ODataPropMap properties = null;
            properties = oDataEntity.getProperties();
            OfflineRequestListener offlineRequestListener = new OfflineRequestListener(
                    Operation.DeleteErrorArchive.getValue(), (String) properties.get(Constants.ERROR_ARCHIVE_ENTRY_REQUEST_METHOD).getValue());
            //Delete the selected error from the ErrorArchive
            offlineStore.executeDeleteEntity(oDataEntity,
                    null);
        } catch (Exception e) {
            throw new OfflineODataStoreException(e);
        }
        //END
    }

    /**
     * Flush request queue for collection on offline request DB
     *
     * @param uiListener the activity that will receive the response to notify the user
     * @throws OfflineODataStoreException
     */
    public static void flushQueuedRequests(UIListener uiListener, String collection) throws
            OfflineODataStoreException {
        //BEGIN
        //Check if the offline oData store has been initialized
        if (offlineStore == null) return;
        try {
            //used to get progress updates of a flush operation
            OfflineFlushListener flushListener = new OfflineFlushListener(uiListener, collection);
            //Assign an Offline Error Archive
            offlineStore.setRequestErrorListener(new OfflineErrorListener());

            //Asynchronously starts sending pending modification request to the server
            offlineStore.scheduleFlushQueuedRequests(flushListener);
        } catch (ODataException e) {
            throw new OfflineODataStoreException(e);
        }
        //END
    }



    /**
     * Flush request queue on offline request DB
     *
     * @param uiListener the activity that will receive the response to notify the user
     * @throws OfflineODataStoreException
     */
    public static void flushQueuedRequests(UIListener uiListener) throws
            OfflineODataStoreException {
        //BEGIN
        //Check if the offline oData store has been initialized
        if (offlineStore == null) return;
        try {
            //used to get progress updates of a flush operation
            OfflineFlushListener flushListener = new OfflineFlushListener(uiListener);
            //Asynchronously starts sending pending modification request to the server
            offlineStore.scheduleFlushQueuedRequests(flushListener);
        } catch (ODataException e) {
            throw new OfflineODataStoreException(e);
        }
        //END
    }

    /**
     * Refresh offline db(store) for selected collection
     *
     * @param uiListener the activity that will receive the response to notify the user
     * @throws OfflineODataStoreException
     */
    public static void refreshRequests(Context context, String collName, UIListener uiListener) throws
            OfflineODataStoreException {
        //Check if the offline oData store has been initialized
        if (offlineStore == null) return;
        try {
            //used to get progress updates of a refresh operation
            OfflineRefreshListener refreshListener = new OfflineRefreshListener(context, Constants.Fresh, collName, uiListener);
            //Asynchronously starts sending pending modification request to the server
            offlineStore.scheduleRefresh(collName, refreshListener);
        } catch (ODataException e) {
            throw new OfflineODataStoreException(e);
        }
        //END
    }
    /**
     * Returns value for selected column name from offline db based on query
     *
     * @throws OfflineODataStoreException
     */
    public static String getValueByColumnName(String mQry, String columnName) throws OfflineODataStoreException {
        String mStrVal = "";
        if (offlineStore != null) {
            try {
                //Define the resource path
                ODataProperty property;
                ODataPropMap properties;
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(mQry);
                //Send a request to read the Distributors from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineStore.
                        executeRequest(request);
                //Check if the response is an error
                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();

                    for (ODataEntity entity : entities) {
                        properties = entity.getProperties();
                        property = properties.get(columnName);
                        mStrVal = (String) property.getValue();
                    }


                } else {
                    throw new OfflineODataStoreException(Constants.invalid_payload_entityset_expected + response.getPayloadType().name());
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return mStrVal;

    }

    /**
     * Returns value for selected column name from offline db based on query
     *
     * @throws OfflineODataStoreException
     */
    public static String getGuidValueByColumnName(String retailerQry, String columnName) throws OfflineODataStoreException {
        String mStrVal = "";
        if (isOfflineStoreOpen()) {
            try {
                //Define the resource path
                ODataProperty property;
                ODataPropMap properties;
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(retailerQry);
                //Send a request to read the Distributors from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineStore.
                        executeRequest(request);
                //Check if the response is an error
                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();

                    for (ODataEntity entity : entities) {
                        properties = entity.getProperties();

                        property = properties.get(columnName);
                        try {
                            ODataGuid mInvoiceGUID = (ODataGuid) property.getValue();
                            mStrVal = mInvoiceGUID.guidAsString36().toUpperCase();
                            //To Remove
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                } else {
                    throw new OfflineODataStoreException(Constants.invalid_payload_entityset_expected + response.getPayloadType().name());
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return mStrVal;

    }
    private static ODataResponseSingle readSingleOdataRequest(String retListQry) throws OfflineODataStoreException {
        //Define the resource path
        ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
        request.setMode(ODataRequestParamSingle.Mode.Read);
        request.setResourcePath(retListQry);
        //Send a request to read the ChannelPartners from the local database
        ODataResponseSingle response = null;
        try {
            response = (ODataResponseSingle) offlineStore.
                    executeRequest(request);
            //Check if the response is an error
            if (response.getPayloadType() == ODataPayload.Type.Error) {
                ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                        response.getPayload();
                throw new OfflineODataStoreException(error.getMessage());
                //Check if the response contains EntitySet
            }
        } catch (Exception e) {
            throw new OfflineODataStoreException(e);
        }
        return response;
    }


    public static String getPartnerTypeID(String query) throws
            OfflineODataStoreException {
        String partnerTypeID = "";
        ODataEntity entity = null;
        if (offlineStore != null) {
            ODataProperty property;
            ODataPropMap properties;

            ODataResponseSingle response = readSingleOdataRequest(query);
            if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                List<ODataEntity> entities = feed.getEntities();
                for (ODataEntity dataEntity : entities){
                    properties = dataEntity.getProperties();
                    property = properties.get(Constants.PartnerTypeID);
                    partnerTypeID = (String) property.getValue();
                    return partnerTypeID;
                }
            }
        }
        return partnerTypeID;
    }

    public static ODataEntity createSyncHistroyEntity(Hashtable hashtable) {
        ODataEntity oDataEntity = null;

        if (hashtable != null) {
            oDataEntity = new ODataEntityDefaultImpl(UtilConstants.getNameSpace(offlineStore) + Constants.SyncHistorysENTITY);

            try {
                offlineStore.allocateProperties(oDataEntity, ODataStore.PropMode.Keys);
            } catch (ODataException e) {
                e.printStackTrace();
            }


            if(hashtable.get(Constants.SyncHisGuid) != null) {
                oDataEntity.getProperties().put(Constants.SyncHisGuid,
                        new ODataPropertyDefaultImpl(Constants.SyncHisGuid, ODataGuidDefaultImpl.initWithString32(hashtable.get(Constants.SyncHisGuid).toString())));
            }

            try {
                if(hashtable.get(Constants.SyncCollection) != null) {
                    oDataEntity.getProperties().put(Constants.SyncCollection,
                            new ODataPropertyDefaultImpl(Constants.SyncCollection, hashtable.get(Constants.SyncCollection)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(hashtable.get(Constants.SyncApplication) != null) {
                    oDataEntity.getProperties().put(Constants.SyncApplication,
                            new ODataPropertyDefaultImpl(Constants.SyncApplication, hashtable.get(Constants.SyncApplication)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(hashtable.get(Constants.SyncHisTime) != null) {
                    oDataEntity.getProperties().put(Constants.SyncHisTime,
                            new ODataPropertyDefaultImpl(Constants.SyncHisTime, hashtable.get(Constants.SyncHisTime)));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(hashtable.get(Constants.SyncDate) != null) {
                    oDataEntity.getProperties().put(Constants.SyncDate,
                            new ODataPropertyDefaultImpl(Constants.SyncDate, Constants.convertDateFormat1(hashtable.get(Constants.SyncDate).toString())));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(hashtable.get(Constants.PartnerId) != null) {
                    oDataEntity.getProperties().put(Constants.PartnerId,
                            new ODataPropertyDefaultImpl(Constants.PartnerId, hashtable.get(Constants.PartnerId)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(hashtable.get(Constants.SyncType) != null) {
                    oDataEntity.getProperties().put(Constants.SyncType,
                            new ODataPropertyDefaultImpl(Constants.SyncType, hashtable.get(Constants.SyncType)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(hashtable.get(Constants.PartnerType) != null) {
                    oDataEntity.getProperties().put(Constants.PartnerType,
                            new ODataPropertyDefaultImpl(Constants.PartnerType, hashtable.get(Constants.PartnerType)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(hashtable.get(Constants.LoginId) != null) {
                    oDataEntity.getProperties().put(Constants.LoginId,
                            new ODataPropertyDefaultImpl(Constants.LoginId, hashtable.get(Constants.LoginId)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                oDataEntity.getProperties().put(Constants.APKVersion, new ODataPropertyDefaultImpl(Constants.APKVersion, BuildConfig.VERSION_NAME));
                oDataEntity.getProperties().put(Constants.APKVersionCode, new ODataPropertyDefaultImpl(Constants.APKVersionCode, String.valueOf(BuildConfig.VERSION_CODE)));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return oDataEntity;
    }
    public static void CreateSyncHistroy(Hashtable hashtable) throws OfflineODataStoreException {
        if (isOfflineStoreOpen()) {
            try {
                //Creates the entity payload
                ODataEntity newEntity = createSyncHistroyEntity(hashtable);
                newEntity.setResourcePath(Constants.SyncHistorys, Constants.SyncHistorys);

                //Send the request to create the new visit in the local database
                //   offlineStore.scheduleCreateEntity(newEntity, Constants.SyncHistroy, new OfflineRequestListener(Operation.Create.getValue(), null, Constants.SyncHistroy), null);
                Log.d("Sync History","insert"+hashtable.get(Constants.SyncHisGuid)+"--"+hashtable.get(Constants.SyncCollection));
                OfflineRequestListener collectionListener = new OfflineRequestListener(Operation.Create.getValue(), null, Constants.SyncHistroy);

/*
                Map<String, String> createHeaders = new HashMap<String, String>();
                createHeaders.put(Constants.RequestID, ssoGUID32);
                createHeaders.put(Constants.RepeatabilityCreation, mStrDateTime);*/
                Map<String, String> createHeaders = new HashMap<String, String>();
                createHeaders.put("OfflineOData.RemoveAfterUpload", "true");



                ODataRequestParamSingle collectionReq = new ODataRequestParamSingleDefaultImpl();
                collectionReq.setMode(ODataRequestParamSingle.Mode.Create);
                collectionReq.setResourcePath(newEntity.getResourcePath());
                collectionReq.setPayload(newEntity);
                collectionReq.getCustomHeaders().putAll(createHeaders);
                offlineStore.scheduleRequest(collectionReq, collectionListener);
            } catch (Exception e) {
                LogManager.writeLogDebug("Create Sync history failed oData ex: "+e.getLocalizedMessage());
                throw new OfflineODataStoreException(e);
            }
        }
        //END
    }


/*    public static int getCount(String strQry) throws OfflineODataStoreException {

        int count = 0;
        //Check if the offline oData store is initialized
        if (offlineStore != null) {

            try {
                //Define the resource path
                String resourcePath = strQry;
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(resourcePath);
                //Send a request to read the Retailer details from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineGeo.
                        executeRequest(request);

                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();
                    count = entities.size();
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return count;

    }*/

    public static List<SalesPersonBean> getSalesPerson(String qry) throws OfflineODataStoreException {
        List<SalesPersonBean> salesPersonBeanList = new ArrayList<>();
        SalesPersonBean salesPersonBean = null;
        if (offlineStore != null) {
            ODataProperty property;
            ODataPropMap properties;
            try {
                //Define the resource path
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);

                request.setResourcePath(qry);
                //Send a request to read the Distributors from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineStore.executeRequest(request);
                //Check if the response is an error
                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();
                    for (ODataEntity entity : entities) {
                        salesPersonBean = new SalesPersonBean();
                        properties = entity.getProperties();
                        salesPersonBean = getSalesPersonData(salesPersonBean, properties);
                        salesPersonBeanList.add(salesPersonBean);
                    }
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return salesPersonBeanList;
    }

    private static SalesPersonBean getSalesPersonData(SalesPersonBean salesPersonBean, ODataPropMap properties) {
        ODataProperty property = properties.get(Constants.SPNo);
        if (property != null) {
            salesPersonBean.setSPNo(property.getValue().toString());
        }

        property = properties.get(Constants.FirstName);
        if (property != null) {
            salesPersonBean.setFirstName(property.getValue().toString());
        }

        property = properties.get(Constants.LastName);
        if (property != null) {
            salesPersonBean.setLastName(property.getValue().toString());
        }

        property = properties.get(Constants.SPCategoryID);
        if (property != null) {
            salesPersonBean.setSPCategoryID(property.getValue().toString());
        }

        property = properties.get(Constants.DesignationID);
        if (property != null) {
            salesPersonBean.setDesignationID(property.getValue().toString());
        }

        property = properties.get(Constants.DesignationDesc);
        if (property != null) {
            salesPersonBean.setDesignationDesc(property.getValue().toString());
        }

        property = properties.get(Constants.Address1);
        if (property != null) {
            salesPersonBean.setAddress1(property.getValue().toString());
        }

        property = properties.get(Constants.Address2);
        if (property != null) {
            salesPersonBean.setAddress2(property.getValue().toString());
        }

        property = properties.get(Constants.Address3);
        if (property != null) {
            salesPersonBean.setAddress3(property.getValue().toString());
        }
        property = properties.get(Constants.Address4);
        if (property != null) {
            salesPersonBean.setAddress4(property.getValue().toString());
        }

        property = properties.get(Constants.PostalCode);
        if (property != null) {
            salesPersonBean.setPostalCode(property.getValue().toString());
        }

        property = properties.get(Constants.EmailID);
        if (property != null) {
            salesPersonBean.setEmailID(property.getValue().toString());
        }

        property = properties.get(Constants.MobileNoSales);
        if (property != null) {
            salesPersonBean.setMobileNo(property.getValue().toString());
        }

        property = properties.get(Constants.ExternalRefID);
        if (property != null) {
            salesPersonBean.setExternalRefID(property.getValue().toString());
        }
        property = properties.get(Constants.SPGUID);
        try {
            ODataGuid spGUID = (ODataGuid) property.getValue();
            salesPersonBean.setSPGUID(spGUID.guidAsString36().toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return salesPersonBean;
    }


    public static void CreateLatLong(Hashtable hashtable, UIListener uiListener, String columnID, Context context,ODataOfflineStore offlineGeo,String Storestate) throws OfflineODataStoreException {
        if (isOfflineStoreOpenGeo(offlineGeo,Storestate)) {
           /* try {
                //Creates the entity payloadi
                ODataEntity newEntity = createLatLongEntity(hashtable, columnID, context);
                //Send the request to create the new visit in the local database
                offlineGeo.scheduleCreateEntity(newEntity, Constants.SPGeos, new OfflineRequestListener(Operation.Create.getValue(), uiListener, Constants.SPGeos), null);
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }*/
            //END

            try {
                //Creates the entity payload
                ODataEntity newEntity = createLatLongEntity(hashtable, columnID, context,offlineGeo);
                newEntity.setResourcePath(Constants.SPGeos, Constants.SPGeos);

                //Send the request to create the new visit in the local database
                //   offlineStore.scheduleCreateEntity(newEntity, Constants.SyncHistroy, new OfflineRequestListener(Operation.Create.getValue(), null, Constants.SyncHistroy), null);
                //      Log.d("Sync History","insert"+hashtable.get(Constants.SyncHisGuid)+"--"+hashtable.get(Constants.SyncCollection));
                OfflineRequestListener collectionListener = new OfflineRequestListener(Operation.Create.getValue(), uiListener, Constants.SPGeos);

/*
                Map<String, String> createHeaders = new HashMap<String, String>();
                createHeaders.put(Constants.RequestID, ssoGUID32);
                createHeaders.put(Constants.RepeatabilityCreation, mStrDateTime);*/
                Map<String, String> createHeaders = new HashMap<String, String>();
                createHeaders.put("OfflineOData.RemoveAfterUpload", "true");


                ODataRequestParamSingle collectionReq = new ODataRequestParamSingleDefaultImpl();
                collectionReq.setMode(ODataRequestParamSingle.Mode.Create);
                collectionReq.setResourcePath(newEntity.getResourcePath());
                collectionReq.setPayload(newEntity);
                collectionReq.getCustomHeaders().putAll(createHeaders);
                offlineGeo.scheduleRequest(collectionReq, collectionListener);
            } catch (Exception e) {
                LogManager.writeLogDebug("Create Sync history failed oData ex: " + e.getLocalizedMessage());
                throw new OfflineODataStoreException(e);
            }
        }
    }

    public static ODataEntity createLatLongEntity(Hashtable hashtable, String columnID, Context context,ODataOfflineStore offlineGeo ) {
        ODataEntity oDataEntity = null;

        if (hashtable != null) {
            oDataEntity = new ODataEntityDefaultImpl(UtilConstants.getNameSpace(offlineGeo) + Constants.SPGEOENTITY);

            try {
                offlineGeo.allocateProperties(oDataEntity, ODataStore.PropMode.Keys);
            } catch (ODataException e) {
                e.printStackTrace();
            }


            oDataEntity.getProperties().put(Constants.GeoGUID,
                    new ODataPropertyDefaultImpl(Constants.GeoGUID, ODataGuidDefaultImpl.initWithString32(hashtable.get(Constants.GeoGUID).toString())));

            if (hashtable.get(Constants.GeoDate) != "") {
                oDataEntity.getProperties().put(Constants.GeoDate,
                        new ODataPropertyDefaultImpl(Constants.GeoDate, UtilConstants.convertDateFormat(hashtable.get(Constants.GeoDate).toString())));
            }

            if (hashtable.get(Constants.GeoTime) != "") {
                oDataEntity.getProperties().put(Constants.GeoTime,
                        new ODataPropertyDefaultImpl(Constants.GeoTime,hashtable.get(Constants.GeoTime)));

            }
            /*if (hashtable.get(Constants.Remarks) != "") {
                oDataEntity.getProperties().put(Constants.Remarks,
                        new ODataPropertyDefaultImpl(Constants.Remarks,hashtable.get(Constants.Remarks)));

            }*/

            try {
                if (hashtable.get(Constants.Distance) != "") {
                    oDataEntity.getProperties().put(Constants.Distance,
                            new ODataPropertyDefaultImpl(Constants.Distance,hashtable.get(Constants.Distance)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (hashtable.get(Constants.DistanceUOM) != "") {
                    oDataEntity.getProperties().put(Constants.DistanceUOM,
                            new ODataPropertyDefaultImpl(Constants.DistanceUOM,hashtable.get(Constants.DistanceUOM)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (hashtable.get(Constants.Longitude) != "" && hashtable.get(Constants.Longitude) != null) {
                oDataEntity.getProperties().put(Constants.Longitude,
                        new ODataPropertyDefaultImpl(Constants.Longitude,hashtable.get(Constants.Longitude)));

            }

            if (hashtable.get(Constants.Latitude) != "" && hashtable.get(Constants.Latitude) != null) {
                oDataEntity.getProperties().put(Constants.Latitude,
                        new ODataPropertyDefaultImpl(Constants.Latitude, hashtable.get(Constants.Latitude)));
            }

            if (hashtable.get(Constants.SPGUID)!=null&&!TextUtils.isEmpty(hashtable.get(Constants.SPGUID).toString())) {
                oDataEntity.getProperties().put(Constants.SPGUID,
                        new ODataPropertyDefaultImpl(Constants.SPGUID, ODataGuidDefaultImpl.initWithString32(hashtable.get(Constants.SPGUID).toString())));
            }

            if (hashtable.get(Constants.LoginID) != "") {
                oDataEntity.getProperties().put(Constants.LoginID,
                        new ODataPropertyDefaultImpl(Constants.LoginID, hashtable.get(Constants.LoginID)));
            }

            if (hashtable.get(Constants.SPNO) != "") {
                oDataEntity.getProperties().put(Constants.SPNO,
                        new ODataPropertyDefaultImpl(Constants.SPNO, hashtable.get(Constants.SPNO)));
            }

            if (hashtable.get(Constants.SPName) != "") {
                oDataEntity.getProperties().put(Constants.SPName,
                        new ODataPropertyDefaultImpl(Constants.SPName, hashtable.get(Constants.SPName)));
            }

            if (hashtable.get(Constants.Reason) != "") {
                oDataEntity.getProperties().put(Constants.Reason,
                        new ODataPropertyDefaultImpl(Constants.Reason, hashtable.get(Constants.Reason)));
            }

            if (hashtable.get(Constants.ReasonDesc) != "") {
                oDataEntity.getProperties().put(Constants.ReasonDesc,
                        new ODataPropertyDefaultImpl(Constants.ReasonDesc, hashtable.get(Constants.ReasonDesc)));
            }

            if (hashtable.get(Constants.BatteryPerc) != "") {
                oDataEntity.getProperties().put(Constants.BatteryPerc,
                        new ODataPropertyDefaultImpl(Constants.BatteryPerc, hashtable.get(Constants.BatteryPerc)));
            }

            try {
                if (hashtable.get(Constants.IMEI1) != "") {
                    oDataEntity.getProperties().put(Constants.IMEI1,
                            new ODataPropertyDefaultImpl(Constants.IMEI1,hashtable.get(Constants.IMEI1)));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (hashtable.get(Constants.IMEI2) != "") {
                    oDataEntity.getProperties().put(Constants.IMEI2,
                            new ODataPropertyDefaultImpl(Constants.IMEI2,hashtable.get(Constants.IMEI2)));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

           /* if (hashtable.get(Constants.Remarks) != "") {
                oDataEntity.getProperties().put(Constants.Remarks,
                        new ODataPropertyDefaultImpl(Constants.Remarks, hashtable.get(Constants.Remarks)));
            }*/

            try {
                if (hashtable.get(Constants.APKVersion) != "") {
                    oDataEntity.getProperties().put(Constants.APKVersion,
                            new ODataPropertyDefaultImpl(Constants.APKVersion,hashtable.get(Constants.APKVersion)));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (hashtable.get(Constants.APKVersionCode) != "") {
                    oDataEntity.getProperties().put(Constants.APKVersionCode,
                            new ODataPropertyDefaultImpl(Constants.APKVersionCode,hashtable.get(Constants.APKVersionCode)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (hashtable.get(Constants.MobileNo11) != "") {
                    oDataEntity.getProperties().put(Constants.MobileNo11,
                            new ODataPropertyDefaultImpl(Constants.MobileNo11,hashtable.get(Constants.MobileNo11)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                oDataEntity.getProperties().put(Constants.OsVersion,
                        new ODataPropertyDefaultImpl(Constants.OsVersion, android.os.Build.VERSION.RELEASE));
//                LogManager.writeLogInfo(android.os.Build.VERSION.RELEASE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                oDataEntity.getProperties().put(Constants.MobileModel,
                        new ODataPropertyDefaultImpl(Constants.MobileModel, Constants.getDeviceName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DatabaseHelperGeo databaseHelper =  DatabaseHelperGeo.getInstance(context);
        databaseHelper.deleteLatLong(columnID);
        return oDataEntity;
    }
    public static boolean isOfflineStoreOpenGeo(ODataOfflineStore offlineGeo,String storeState) {
        boolean isOpen = false;
        try {
            if (offlineGeo != null && ODataOfflineStoreState.ODataOfflineStoreOpen.name().equals(storeState)) {
                isOpen = true;
            }
            return isOpen;
        } catch (Exception e) {
            return isOpen;
        }
    }
    public static boolean closeOfflineStoreGeo(Context context, ODataOfflineStoreOptions
            options,ODataOfflineStore offlineGeo) throws OfflineODataStoreException {
        try {
            TrackerService.setGeoStoreState(ODataOfflineStoreState.ODataOfflineStoreClosed.name());
            UtilOfflineManager.closeOfflineStore(context, options, offlineGeo, Constants.PREFS_NAME);
            return true;
        } catch (Exception e) {
            LogManager.writeLogError(Constants.offline_store_not_closed + e.getMessage());
            throw new OfflineODataStoreException(e);
        }
    }

    public static boolean closeStoreGeo(Context mContext, ODataOfflineStoreOptions options,
                                        String errMsg, ODataOfflineStore offlineStore,
                                        String sharedPrefName, String err_code) {
        boolean isReInitStore = false;

        try {
            if (!errMsg.contains("-100036") && !errMsg.contains("-100097") && !errMsg.contains("-10214")
                    /*&& !errMsg.contains("-10104")*/ && !err_code.contains("-10247") && !err_code.contains("-10001")) {
                isReInitStore = false;
            } else {
                try {
                    TrackerService.setGeoStoreState(ODataOfflineStoreState.ODataOfflineStoreClosed.name());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isReInitStore = UtilOfflineManager.closeOfflineStore(mContext, options, offlineStore, sharedPrefName);
            }
        } catch (Exception var7) {
            LogManager.writeLogError("Error during store close: " + var7.getMessage());
        }

        return isReInitStore;
    }




    public static void refreshRequestsGeo(Context context, String collName, UIListener
            uiListener,ODataOfflineStore offlineGeo) throws
            OfflineODataStoreException {
        //Check if the offline oData store has been initialized
        if (offlineGeo == null) return;
        try {
            //used to get progress updates of a refresh operation
            OfflineRefreshListener refreshListener = new OfflineRefreshListener(context, Constants.Fresh, collName, uiListener);
            //Asynchronously starts sending pending modification request to the server
            offlineGeo.scheduleRefresh(collName, refreshListener);
        } catch (ODataException e) {
            throw new OfflineODataStoreException(e);
        }
        //END
    }

    public static String getLoginID(String logiQry) {
        String loginID = "";
        ODataProperty property;
        ODataPropMap properties;
        List<ODataEntity> entities;
        try {
            entities = UtilOfflineManager.getEntities(OfflineManager.offlineStore, logiQry);
            if (entities != null && !entities.isEmpty()) {
                for (ODataEntity entity : entities) {
                    properties = entity.getProperties();
                    property = properties.get(Constants.LoginID);
                    loginID = property.getValue().toString();
                    break;
                }
            }
        } catch (OfflineODataStoreException e) {
            e.printStackTrace();
        }

        return loginID;
    }

    public static ArrayList<Config> getAuthorizations(Context cntxt) throws OfflineODataStoreException {

        ArrayList<Config> authList = new ArrayList<Config>();
        //Check if the offline oData store is initialized
        if (offlineStore != null) {
            Config authBean;
            ODataProperty property;
            ODataPropMap properties;
            try {
                //Define the resource path
                String resourcePath = Constants.UserProfileAuthSet + "?$filter=Application eq 'PD' and AuthOrgTypeID eq '000011' ";
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(resourcePath);
                //Send a request to read the AUTHORIZATIONS from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineStore.
                        executeRequest(request);
                //Check if the response is an error
                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();
                    //Retrieve the data from the response
                    for (ODataEntity entity : entities) {
                        authBean = new Config();
                        properties = entity.getProperties();
                        property = properties.get(Constants.AuthOrgValue);
                        authBean.setFeature((String) property.getValue());
                        authList.add(authBean);
                    }
                    SharedPreferences sharedPreferences = cntxt.getSharedPreferences(Constants.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                 //   Constants.updateTCodetoSharedPreference(sharedPreferences, editor, authList);


                } else {
                    throw new OfflineODataStoreException(Constants.invalid_payload_entityset_expected + response.getPayloadType().name());
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return authList;

    }

    public static boolean getVisitStatusForCustomerGeo(String strQry,ODataOfflineStore offlineGeo) throws OfflineODataStoreException {

        boolean mBooleanVisitStatus = false;
        //Check if the offline oData store is initialized
        if (offlineGeo != null) {

            try {
                //Define the resource path
                String resourcePath = strQry;
                ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                request.setMode(Mode.Read);
                request.setResourcePath(resourcePath);
                //Send a request to read the Retailer details from the local database
                ODataResponseSingle response = (ODataResponseSingle) offlineGeo.
                        executeRequest(request);

                if (response.getPayloadType() == ODataPayload.Type.Error) {
                    ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                            response.getPayload();
                    throw new OfflineODataStoreException(error.getMessage());
                    //Check if the response contains EntitySet
                } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                    ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                    List<ODataEntity> entities = feed.getEntities();
                    if (entities.size() > 0) {
                        mBooleanVisitStatus = true;
                    }
                }
            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
        return mBooleanVisitStatus;

    }

    public static int getPendingCount(final String strQry) throws OfflineODataStoreException {
        final int[] pendingCount = {0};
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Log.e("Sync History Info:","LOCKED");
                    if (offlineStore != null) {
                        try {
                            //Define the resource path
                            String resourcePath = strQry;
                            ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                            request.setMode(ODataRequestParamSingle.Mode.Read);
                            request.setResourcePath(resourcePath);
                            //Send a request to read the Retailer details from the local database
                            ODataResponseSingle response = (ODataResponseSingle) offlineStore.
                                    executeRequest(request);

                            if (response.getPayloadType() == ODataPayload.Type.Error) {
                                ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                                        response.getPayload();
                                throw new OfflineODataStoreException(error.getMessage());
                                //Check if the response contains EntitySet
                            } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                                ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                                List<ODataEntity> entities = feed.getEntities();
                                if (entities.size() > 0) {
                                    pendingCount[0] = entities.size();
                                }
                            }
                        } catch (Exception e) {
                            try {
                                throw new OfflineODataStoreException(e);
                            } catch (OfflineODataStoreException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Sync History Info","ANR EXCEPTION OCCURRED");
                }finally {
                    Log.e("Sync History Info:","UNLOCKED FINALLY");
                }

            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Check if the offline oData store is initialized
        Log.d("Sync History","count"+pendingCount[0]);
        return pendingCount[0];

    }


    public static int getPendingCountGeo(final String strQry) throws OfflineODataStoreException {
        final int[] pendingCount = {0};
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("Sync History Info:","LOCKED");
                   /* if (offlineGeo != null) {
                        try {
                            //Define the resource path
                            String resourcePath = strQry;
                            ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
                            request.setMode(ODataRequestParamSingle.Mode.Read);
                            request.setResourcePath(resourcePath);
                            //Send a request to read the Retailer details from the local database
                            ODataResponseSingle response = (ODataResponseSingle) offlineGeo.
                                    executeRequest(request);

                            if (response.getPayloadType() == ODataPayload.Type.Error) {
                                ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                                        response.getPayload();
                                throw new OfflineODataStoreException(error.getMessage());
                                //Check if the response contains EntitySet
                            } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                                ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                                List<ODataEntity> entities = feed.getEntities();
                                if (entities.size() > 0) {
                                    pendingCount[0] = entities.size();
                                }
                            }
                        } catch (Exception e) {
                            try {
                                throw new OfflineODataStoreException(e);
                            } catch (OfflineODataStoreException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Sync History Info","ANR EXCEPTION OCCURRED");
                }finally {
                    Log.e("Sync History Info:","UNLOCKED FINALLY");
                }

            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Check if the offline oData store is initialized
        Log.d("Sync History","count"+pendingCount[0]);
        return pendingCount[0];

    }


    public static void flushQueuedRequestsForGeo(UIListener uiListener, String collection,ODataOfflineStore offlineGeo) throws
            OfflineODataStoreException {
        //BEGIN
        //Check if the offline oData store has been initialized
        if (offlineGeo == null) return;
        try {
            //used to get progress updates of a flush operation
            OfflineGeoFlushListener flushListener = new OfflineGeoFlushListener(uiListener, collection);
            //Assign an Offline Error Archive
            offlineGeo.setRequestErrorListener(new OfflineErrorListener());

            //Asynchronously starts sending pending modification request to the server
            offlineGeo.scheduleFlushQueuedRequests(flushListener);
        } catch (ODataException e) {
            throw new OfflineODataStoreException(e);
        }
        //END
    }

    public static void refreshGeoStoreSync(Context context, UIListener uiListener, String syncType, String defineReq,ODataOfflineStore offlineGeo) throws OfflineODataStoreException {

        if (offlineGeo != null) {
            try {
                if(syncType.equalsIgnoreCase(Constants.Fresh)){
                    LogManager.writeLogDebug("Download Sync In progress");
                }else if(syncType.equalsIgnoreCase(Constants.ALL)){
                    LogManager.writeLogDebug("All Sync In progress");
                }
                OfflineGeoRefreshListener refreshListener = new OfflineGeoRefreshListener(context, syncType, defineReq, uiListener);

                if (syncType.equals(Constants.Fresh)) {
                    offlineGeo.scheduleRefresh(defineReq, refreshListener);
                } else {
                    offlineGeo.scheduleRefresh(refreshListener);
                }

            } catch (Exception e) {
                throw new OfflineODataStoreException(e);
            }
        }
    }


}
