package com.arteriatech.geotrack.rspl.asyncTask;

import android.content.Context;
import android.os.AsyncTask;

import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.interfaces.MessageWithBooleanCallBack;
import com.sap.smp.client.odata.offline.ODataOfflineStore;


/**
 * Created by e10526 on 23-03-2018.
 */

public class SyncGeoAsyncTask extends AsyncTask<String, Boolean, Boolean> {
    private Context mContext;
    private MessageWithBooleanCallBack dialogCallBack = null;
    boolean onlineStoreOpen = false;
    private String mSyncType="";
    private ODataOfflineStore geoStore;
    public SyncGeoAsyncTask(Context context, MessageWithBooleanCallBack dialogCallBack, String mSyncType,ODataOfflineStore offlineGeo) {
        this.mContext = context;
        this.dialogCallBack = dialogCallBack;
        this.mSyncType = mSyncType;
        this.geoStore = offlineGeo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        onlineStoreOpen = false;
        /*try {
                    Log.d("BeforeCallMustSell Req", UtilConstants.getSyncHistoryddmmyyyyTime());
                    try {
                        if(!OfflineManager.isOfflineStoreOpenGeo(geoStore)) {
                            try {
                                OfflineManager.openOfflineStoreGeo(mContext, new UIListener() {
                                    @Override
                                    public void onRequestError(int i, Exception e) {
                                        Log.d("opOffStoreMS onReqError", UtilConstants.getSyncHistoryddmmyyyyTime());
                                        setCallBackToUI(true,"");
                                    }

                                    @Override
                                    public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                                        Log.d("opOffStoreMS onReqSuc", UtilConstants.getSyncHistoryddmmyyyyTime());
                                        List<String> alString=new ArrayList<>();
                                        alString.add(Constants.SPGeos);
                                        Constants.updateSyncTime(alString,mContext,Constants.Sync_All);
                                        Constants.events.inserthistortTable(Constants.SYNC_TABLE, "",
                                                Constants.Collections, Constants.SPGeos);
                                        setCallBackToUI(true,"");
                                    }
                                });
                            } catch (OfflineODataStoreException e) {
                                onlineStoreOpen =true;
                                LogManager.writeLogError(Constants.error_txt + e.getMessage());
                            }

                        }else{
                        if(mSyncType.equalsIgnoreCase(Constants.Fresh) || mSyncType.equalsIgnoreCase(Constants.All)){
                            try {
                                if (UtilConstants.isNetworkAvailable(mContext)) {
                                    OfflineManager.refreshRequestsGeo(mContext, Constants.SPGeos, new UIListener() {
                                        @Override
                                        public void onRequestError(int operation, Exception exception) {
                                            ErrorBean errorBean = Constants.getErrorCode(operation, exception,mContext);
                                            try {
                                                if (!errorBean.hasNoError()) {
                                                    if (errorBean.getErrorCode() == Constants.Resource_not_found) {
                                                        UtilConstants.closeStore(mContext,
                                                                OfflineManager.optionsGeo, errorBean.getErrorMsg(),
                                                                OfflineManager.offlineGeo, Constants.PREFS_NAME,"");
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Log.d("refReqMust onReqError", UtilConstants.getSyncHistoryddmmyyyyTime());
                                            setCallBackToUI(true,"");
                                        }

                                        @Override
                                        public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                                            Log.d("refReqMust onReqError", UtilConstants.getSyncHistoryddmmyyyyTime());
                                            setCallBackToUI(true,"");
                                        }
                                    });
                                }else{
                                    onlineStoreOpen =true;
                                }
                            } catch (OfflineODataStoreException e) {
                                onlineStoreOpen =true;
                                TraceLog.e("Sync::onRequestSuccess", e);
                            }
                        }else{
                            onlineStoreOpen =true;
                        }
                        }
                    } catch (Exception e) {
                        onlineStoreOpen =true;
                        e.printStackTrace();
                    }
                    Log.d("AfterCallMustSell Req", UtilConstants.getSyncHistoryddmmyyyyTime());
        } catch (Exception e) {
            onlineStoreOpen =true;
            e.printStackTrace();
        }*/
        return onlineStoreOpen;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(aBoolean) {
            setCallBackToUI(aBoolean,Constants.makeMsgReqError(Constants.ErrorNo,mContext,false));
        }

    }

    private void setCallBackToUI(boolean status, String error_Msg){
        if (dialogCallBack!=null){
            dialogCallBack.clickedStatus(status,error_Msg,null);
        }
    }

}
