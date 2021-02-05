package com.arteriatech.geotrack.rspl.online;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.maf.tools.logon.core.LogonCoreContext;
import com.sap.smp.client.httpc.HttpConversationManager;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataEntitySet;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.ODataPropMap;
import com.sap.smp.client.odata.ODataProperty;
import com.sap.smp.client.odata.exception.ODataContractViolationException;
import com.sap.smp.client.odata.exception.ODataNetworkException;
import com.sap.smp.client.odata.exception.ODataParserException;
import com.sap.smp.client.odata.impl.ODataErrorDefaultImpl;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.smp.client.odata.store.ODataRequestParamSingle;
import com.sap.smp.client.odata.store.ODataResponseSingle;
import com.sap.smp.client.odata.store.impl.ODataRequestParamSingleDefaultImpl;

import java.net.URL;
import java.util.List;

public class OnlineManager {
    public static final String TAG = OnlineManager.class.getSimpleName();

    /**
     * Initialize an online OData store for online access
     *
     * @param context used only to access the application context
     * @return true if the online is open and false otherwise
     * @throws OnlineODataStoreException
     */

    public static boolean openOnlineStore(Context context, boolean isForceMetadata) throws OnlineODataStoreException {
        //OnlineOpenListener implements OpenListener interface
        //Listener to be invoked when the opening process of an OnlineODataStore object finishes
        if (isForceMetadata) {
            try {
                OnlineStoreListener openListener = OnlineStoreListener.getInstance();
                OnlineODataStore store = openListener.getStore();
                if (store != null && store.isOpenCache())
                    store.closeCache();
                if (store != null) {
                    store.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Constants.onlineStore = null;
        OnlineStoreListener.instance = null;
        Constants.IsOnlineStoreFailed = false;
        Constants.Error_Msg = "";
        try {
            OnlineStoreListener openListener = OnlineStoreListener.getInstance();
            LogonCoreContext lgCtx = LogonCore.getInstance().getLogonContext();

            //The logon configurator uses the information obtained in the registration
          //  IManagerConfigurator configurator = LogonUIFacade.getInstance().getLogonConfigurator(context);
            HttpConversationManager manager = new HttpConversationManager(context);
           // configurator.configure(manager);
            OnlineODataStore.OnlineStoreOptions onlineOptions = new OnlineODataStore.OnlineStoreOptions();
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME,0);
            String rollType = sharedPreferences.getString(Constants.USERROLE, "");
            if(Constants.getRollID(context) || TextUtils.isEmpty(rollType)) {
                onlineOptions.useCache = true;//if true technical cache is enabled
                onlineOptions.cacheEncryptionKey = Constants.EncryptKey;
            }
            if (ConstantsUtils.getFirstTimeRun(context) == 2) {
                onlineOptions.forceMetadataDownload = true;
            } else {
                onlineOptions.forceMetadataDownload = isForceMetadata;
            }
            //XCSRFTokenRequestFilter implements IRequestFilter
            //Request filter that is allowed to preprocess the request before sending
            XCSRFTokenRequestFilter requestFilter = XCSRFTokenRequestFilter.getInstance(context,lgCtx);
            XCSRFTokenResponseFilter responseFilter = XCSRFTokenResponseFilter.getInstance(context,
                    requestFilter);
            manager.addFilter(requestFilter);
            manager.addFilter(responseFilter);

            try {
                String endPointURL = lgCtx.getAppEndPointUrl();
                URL url = new URL(endPointURL);
                //Method to open a new online store asynchronously

                OnlineODataStore.open(context, url, manager, openListener, onlineOptions);


                //            openListener.waitForCompletion();
                if (openListener.getError() != null) {
                    throw openListener.getError();
                }
            } catch (Exception e) {
                throw new OnlineODataStoreException(e);
            }
            //Check if OnlineODataStore opened successfully

            while (!Constants.IsOnlineStoreFailed) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Constants.IsOnlineStoreFailed = false;


            return Constants.onlineStore != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUserRollInfo(String resourcePath) throws OnlineODataStoreException, ODataContractViolationException, ODataParserException, ODataNetworkException {

        OnlineStoreListener openListener = OnlineStoreListener.getInstance();
        OnlineODataStore store = openListener.getStore();
        ODataProperty property;
        ODataPropMap properties;
        if (store != null) {
            ODataRequestParamSingle request = new ODataRequestParamSingleDefaultImpl();
            request.setMode(ODataRequestParamSingle.Mode.Read);
            request.setResourcePath(resourcePath);
            ODataResponseSingle response = (ODataResponseSingle) store.executeRequest(request);
            //Check if the response is an error
            if (response.getPayloadType() == ODataPayload.Type.Error) {
                ODataErrorDefaultImpl error = (ODataErrorDefaultImpl)
                        response.getPayload();
                throw new OnlineODataStoreException(error.getMessage());
                //Check if the response contains EntitySet
            } else if (response.getPayloadType() == ODataPayload.Type.EntitySet) {
                ODataEntitySet feed = (ODataEntitySet) response.getPayload();
                List<ODataEntity> entities = feed.getEntities();
                //Retrieve the data from the response
                for (ODataEntity entity : entities) {
                    properties = entity.getProperties();
                    property = properties.get(Constants.AuthOrgTypeID);
                    String typeId = property.getValue().toString();
                    if (!TextUtils.isEmpty(typeId) && typeId.equalsIgnoreCase("000014")) {
                        property = properties.get(Constants.AuthOrgValue);
                        if (property != null) {
                            return property.getValue().toString();
                        }
                    }
                }
            }
        }

















          /*
            try {

                ODataRequestParamBatch requestParamBatch = new ODataRequestParamBatchDefaultImpl();
                ODataRequestParamSingle batchItem = new ODataRequestParamSingleDefaultImpl();
                batchItem.setResourcePath(resourcePath);
                batchItem.setMode(ODataRequestParamSingle.Mode.Read);
                requestParamBatch.add(batchItem);
                // Send request synchronously
                ODataResponse oDataResponse = store.executeRequest(requestParamBatch);
                // Get batch response
                if (oDataResponse instanceof ODataResponseBatchDefaultImpl) {
                    ODataResponseBatch batchResponse = (ODataResponseBatch) oDataResponse;
                    List<ODataResponseBatchItem> responses = batchResponse.getResponses();
                    for (ODataResponseBatchItem response : responses) {
                        // Check if batch item is a change set
                        if (response instanceof ODataResponseChangeSetDefaultImpl) {
                            // Todo here multiple batch request will come
                        } else {
                            ODataResponseSingle oDataResponseSingle = (ODataResponseSingleDefaultImpl) response;
                            ODataPayload oDataPayload = oDataResponseSingle.getPayload();
                            if (oDataPayload != null) {
                                if (oDataPayload instanceof ODataError) {
                                    ODataError oError = (ODataError) oDataPayload;
                                    String uiMessage = oError.getMessage();
                                } else {
                                    // TODO Check if batch item is a single READ request
                                    ODataEntitySet feed = (ODataEntitySet) oDataResponseSingle.getPayload();
                                    // Get the list of ODataEntity
                                    List<ODataEntity> entities = feed.getEntities();

//                                    ODataEntity entity = (ODataEntity) oDataResponseSingle.getPayload();
                                    for (ODataEntity entity : entities) {
                                    properties = entity.getProperties();
                                    property = properties.get(Constants.AuthOrgValue);
                                    if (property!=null){
                                        return property.getValue().toString();
                                    }
                                    *//*userLoginBean.setLoginID(property.getValue().toString());
                                    property = properties.get(Constants.Application);
                                    userLoginBean.setApplication(property.getValue().toString());
                                    property = properties.get(Constants.ERPLoginID);
                                    userLoginBean.setERPLoginID(property.getValue().toString());
                                    property = properties.get(Constants.RoleID);
                                    userLoginBean.setRoleID(property.getValue().toString());
                                    property = properties.get(Constants.LoginName);
                                    userLoginBean.setLoginName(property.getValue().toString());
                                    property = properties.get(Constants.RoleDesc);
                                    userLoginBean.setRoleDesc(property.getValue().toString());
                                    property = properties.get(Constants.RoleCatID);
                                    userLoginBean.setRoleCatID(property.getValue().toString());
                                    property = properties.get(Constants.RoleCatDesc);
                                    userLoginBean.setRoleCatDesc(property.getValue().toString());
                                    property = properties.get(Constants.IsActive);
                                    userLoginBean.setIsActive(property.getValue().toString());
                                    property = properties.get(Constants.UserFunction1);
                                    userLoginBean.setUserFunction1ID(property.getValue().toString());
                                    property = properties.get(Constants.UserFunction1Desc);
                                    userLoginBean.setUserFunction1Desc(property.getValue().toString());
                                    property = properties.get(Constants.UserFunction2);
                                    userLoginBean.setUserFunction2ID(property.getValue().toString());
                                    property = properties.get(Constants.UserFunction2Desc);
                                    userLoginBean.setUserFunction2Desc(property.getValue().toString());*//*
                                    }
                                }
                            }
                        }

                    }
                }

            } catch (Exception e) {
                throw new OnlineODataStoreException(e);
            }
        }*/
        return "";
    }

//    public static boolean openOnlineStore(Context context) throws OnlineODataStoreException {
//        //AgencyOpenListener implements OpenListener interface
//        //Listener to be invoked when the opening process of an OnlineODataStore object finishes
//        OnlineStoreListener openListener = OnlineStoreListener.getInstance();
//            LogonCoreContext lgCtx = LogonCore.getInstance().getLogonContext();
//
//            //The logon configurator uses the information obtained in the registration
//            IManagerConfigurator configurator = LogonUIFacade.getInstance().getLogonConfigurator(context);
//            HttpConversationManager manager = new HttpConversationManager(context);
//            configurator.configure(manager);
//
//            //XCSRFTokenRequestFilter implements IRequestFilter
//            //Request filter that is allowed to preprocess the request before sending
//            XCSRFTokenRequestFilter requestFilter = XCSRFTokenRequestFilter.getInstance(lgCtx);
//            XCSRFTokenResponseFilter responseFilter = XCSRFTokenResponseFilter.getInstance(context,
//                    requestFilter);
//            manager.addFilter(requestFilter);
//            manager.addFilter(responseFilter);
//
//            try {
//                String endPointURL = lgCtx.getAppEndPointUrl();
//                URL url = new URL(endPointURL);
//                //Method to open a new online store asynchronously
//
//                OnlineODataStore.open(context, url, manager, openListener, null);
////                openListener.waitForCompletion();
//                if (openListener.getError() != null) {
//                    throw openListener.getError();
//                }
//            } catch(Exception e){
//               throw new OnlineODataStoreException(e);
//           }
//            //Check if OnlineODataStore opened successfully
//            //OnlineODataStore store = openListener.getStore();
//            if (Constants.onlineStore != null) {
//                return true;
//            } else {
//                return false;
//            }
//    }
}
