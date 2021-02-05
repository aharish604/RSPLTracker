package com.arteriatech.geotrack.rspl.synchistoryInfo;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arteriatech.geotrack.rspl.autosync.AutoSyncDataLocationAlarmReceiver;
import com.arteriatech.mutils.adapter.AdapterInterface;
import com.arteriatech.mutils.adapter.SimpleRecyclerViewAdapter;
import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.Operation;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.interfaces.DialogCallBack;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.mutils.log.TraceLog;
import com.arteriatech.mutils.store.OnlineODataInterface;
import com.arteriatech.mutils.sync.SyncHistoryModel;
import com.arteriatech.mutils.upgrade.AppUpgradeConfig;
import com.arteriatech.geotrack.rspl.BuildConfig;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.arteriatech.geotrack.rspl.ErrorBean;
import com.arteriatech.geotrack.rspl.R;
import com.arteriatech.geotrack.rspl.SPGeo.database.DatabaseHelperGeo;
import com.arteriatech.geotrack.rspl.SPGeo.database.LocationBean;
import com.arteriatech.geotrack.rspl.SyncHist;
import com.arteriatech.geotrack.rspl.asyncTask.RefreshAsyncTask;
import com.arteriatech.geotrack.rspl.interfaces.SyncHistoryCallBack;
import com.arteriatech.geotrack.rspl.offline.OfflineManager;
import com.arteriatech.geotrack.rspl.registration.Configuration;
import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.maf.tools.logon.core.LogonCoreException;
import com.sap.smp.client.odata.ODataDuration;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataError;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.store.ODataRequestExecution;
import com.sap.smp.client.odata.store.ODataRequestParamSingle;
import com.sap.smp.client.odata.store.ODataResponseSingle;
import com.sap.smp.client.odata.store.impl.ODataRequestParamSingleDefaultImpl;
import com.sap.xscript.core.GUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HttpsURLConnection;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncHistoryInfoFragment extends Fragment implements UIListener,SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, CollectionSyncInterface, OnlineODataInterface {

    ArrayList<String> tempCPList = new ArrayList<>();
    int updateCancelSOCount = 0;
    int cancelSoPos = 0;
    int mIntPendingCollVal = 0;
    Hashtable dbHeadTable;
    ArrayList<HashMap<String, String>> arrtable;
    String[][] invKeyValues;
    ArrayList<String> alAssignColl = new ArrayList<>();
    ArrayList<String> alFlushColl = new ArrayList<>();
    String concatCollectionStr = "";
    String concatFlushCollStr = "";
    String endPointURL = "";
    String appConnID = "";
    String syncType = "";
    boolean onlineStoreOpen = false;
    PendingCountAdapter pendingCountAdapter;
    private RecyclerView recycler_view_His, rvSyncTime;
    private int pendingCount = 0;
    private boolean mBoolIsNetWorkNotAval = false;
    private boolean mBoolIsReqResAval = false;
    private boolean isBatchReqs = false;
    private boolean tokenFlag = false;
    private int penReqCount = 0;
    private ProgressDialog syncProgDialog = null;
    private boolean dialogCancelled = false;
    private int mError = 0;
    private int pendingPostCount = 0;
    private int successPostCount = 0;
    private List<PendingCountBean> pendingCountBeanList = new ArrayList<>();
    private ArrayList<SyncHistoryModel> syncHistoryModelList = new ArrayList<>();
    private ImageView ivUploadDownload, ivSyncAll;
    private TextView tvPendingCount, tvPendingStatus;
    private NestedScrollView nestedScroll;
    private LinearLayout cvUpdatePending;
    private SimpleRecyclerViewAdapter<SyncHistoryModel> simpleUpdateHistoryAdapter;
    int[] cancelSOCount = new int[0];
    private boolean isClickable = false;
    private DatabaseHelperGeo databaseHelper = null;
    private ReentrantLock reentrantLock = null;
    private int responseCount = 0,sqlDbCount = 0;
    SwipeRefreshLayout swipeRefresh;
  //  GetDeviceInfoInterface service;




    public SyncHistoryInfoFragment() {
        // Required empty public constructor
    }

    private static int getPendingListSize(Context mContext) {
        int size = 0;
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);

        Set<String> set = new HashSet<>();

        set = sharedPreferences.getStringSet(Constants.CollList, null);
        if (set != null && !set.isEmpty()) {
            size = size + set.size();
        }

//		set = sharedPreferences.getStringSet(Constants.SOList, null);
        set = sharedPreferences.getStringSet(Constants.SalesOrderDataValt, null);
        if (set != null && !set.isEmpty()) {
            size = size + set.size();
        }

        set = sharedPreferences.getStringSet(Constants.SOUpdate, null);
        if (set != null && !set.isEmpty()) {
            size = size + set.size();
        }

        set = sharedPreferences.getStringSet(Constants.SOCancel, null);
        if (set != null && !set.isEmpty()) {
            size = size + set.size();
        }

        set = sharedPreferences.getStringSet(Constants.Expenses, null);
        if (set != null && !set.isEmpty()) {
            size = size + set.size();
        }
        set = sharedPreferences.getStringSet(Constants.MTPDataValt, null);
        if (set != null && !set.isEmpty()) {
            size = size + set.size();
        }
        set = sharedPreferences.getStringSet(Constants.RTGSDataValt, null);
        if (set != null && !set.isEmpty()) {
            size = size + set.size();
        }
        return size;
    }

    public static ArrayList<Object> getPendingCollList(Context mContext, boolean isFromAutoSync) {
        ArrayList<Object> objectsArrayList = new ArrayList<>();
        int mIntPendingCollVal = 0;
        String[][] invKeyValues = null;
        Set<String> set = new HashSet<>();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
        invKeyValues = new String[getPendingListSize(mContext)][2];
        if (!isFromAutoSync) {
            set = sharedPreferences.getStringSet(Constants.CollList, null);
            if (set != null && !set.isEmpty()) {
                Iterator itr = set.iterator();
                while (itr.hasNext()) {
                    invKeyValues[mIntPendingCollVal][0] = itr.next().toString();
                    invKeyValues[mIntPendingCollVal][1] = Constants.CollList;
                    mIntPendingCollVal++;
                }
            }
        }

        set = sharedPreferences.getStringSet(Constants.SalesOrderDataValt, null);
        if (set != null && !set.isEmpty()) {
            Iterator itr = set.iterator();
            while (itr.hasNext()) {
                invKeyValues[mIntPendingCollVal][0] = itr.next().toString();
                invKeyValues[mIntPendingCollVal][1] = Constants.SalesOrderDataValt;
                mIntPendingCollVal++;
            }
        }
        if (!isFromAutoSync) {
            set = sharedPreferences.getStringSet(Constants.MTPDataValt, null);
            if (set != null && !set.isEmpty()) {
                Iterator itr = set.iterator();
                while (itr.hasNext()) {
                    invKeyValues[mIntPendingCollVal][0] = itr.next().toString();
                    invKeyValues[mIntPendingCollVal][1] = Constants.MTPDataValt;
                    mIntPendingCollVal++;
                }
            }
        }
        if (!isFromAutoSync) {
            set = sharedPreferences.getStringSet(Constants.RTGSDataValt, null);
            if (set != null && !set.isEmpty()) {
                Iterator itr = set.iterator();
                while (itr.hasNext()) {
                    invKeyValues[mIntPendingCollVal][0] = itr.next().toString();
                    invKeyValues[mIntPendingCollVal][1] = Constants.RTGSDataValt;
                    mIntPendingCollVal++;
                }
            }
        }
        int cancelSOSize = 0;
        int i = 0;
        int[] cancelSOCount = new int[0];
        if (!isFromAutoSync) {
            set = sharedPreferences.getStringSet(Constants.SOCancel, null);


            cancelSOSize = (set != null && !set.isEmpty()) ? set.size() : 0;

            set = sharedPreferences.getStringSet(Constants.SOUpdate, null);
            int changeSOSize = 0;
            changeSOSize = (set != null && !set.isEmpty()) ? set.size() : 0;

            cancelSOCount = new int[cancelSOSize + changeSOSize];

            set = sharedPreferences.getStringSet(Constants.SOCancel, null);
            if (set != null && !set.isEmpty()) {
                Iterator itr = set.iterator();
//            cancelSOCount = new int[set.size()];
                while (itr.hasNext()) {
                    invKeyValues[mIntPendingCollVal][0] = itr.next().toString();
                    invKeyValues[mIntPendingCollVal][1] = Constants.SOCancel;

                    String store = null;
                    try {
                        store = LogonCore.getInstance().getObjectFromStore(invKeyValues[mIntPendingCollVal][0].toString());
                    } catch (LogonCoreException e) {
                        e.printStackTrace();
                    }


                    //Fetch object from data vault
                    ArrayList<HashMap<String, String>> arrtable = null;
                    try {

                        JSONObject fetchJsonHeaderObject = new JSONObject(store);
                        String itemsString = fetchJsonHeaderObject.getString(Constants.SalesOrderItems);
                        arrtable = UtilConstants.convertToArrayListMap(itemsString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (arrtable != null)
                        cancelSOCount[i] = arrtable.size() + 1;
                    i++;
                    mIntPendingCollVal++;
                }
            }
        }
        if (!isFromAutoSync) {
            set = sharedPreferences.getStringSet(Constants.SOUpdate, null);
            if (set != null && !set.isEmpty()) {
                Iterator itr = set.iterator();
//            cancelSOCount = new int[set.size()];
                while (itr.hasNext()) {
                    invKeyValues[mIntPendingCollVal][0] = itr.next().toString();
                    invKeyValues[mIntPendingCollVal][1] = Constants.SOUpdate;

                    String store = null;
                    try {
                        store = LogonCore.getInstance().getObjectFromStore(invKeyValues[mIntPendingCollVal][0].toString());
                    } catch (LogonCoreException e) {
                        e.printStackTrace();
                    }


                    ArrayList<HashMap<String, String>> arrtable = null;
                    try {

                        JSONObject fetchJsonHeaderObject = new JSONObject(store);
                        String itemsString = fetchJsonHeaderObject.getString(Constants.SalesOrderItems);
                        arrtable = UtilConstants.convertToArrayListMap(itemsString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (arrtable != null)
                        cancelSOCount[i] = arrtable.size() + 1;
                    i++;
                    mIntPendingCollVal++;
                }
            }
        }
        if (!isFromAutoSync) {
            set = sharedPreferences.getStringSet(Constants.Expenses, null);
            if (set != null && !set.isEmpty()) {
                Iterator itr = set.iterator();
                while (itr.hasNext()) {
                    invKeyValues[mIntPendingCollVal][0] = itr.next().toString();
                    invKeyValues[mIntPendingCollVal][1] = Constants.Expenses;
                    mIntPendingCollVal++;
                }
            }
        }

        if (mIntPendingCollVal > 0) {
            Arrays.sort(invKeyValues, new ArrayComarator());
            objectsArrayList.add(mIntPendingCollVal);
            objectsArrayList.add(invKeyValues);
            objectsArrayList.add(cancelSOCount);
        }

        return objectsArrayList;

    }

    @Override
    public void responseSuccess(ODataRequestExecution oDataRequestExecution, List<ODataEntity> list, Bundle bundle) {
        String type = bundle != null ? bundle.getString(Constants.BUNDLE_RESOURCE_PATH) : "";
        Log.d("responseSuccess", "responseSuccess: " + type);
        if (!isBatchReqs) {
            switch (type) {
                case Constants.RouteSchedules:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRequestSuccess(Operation.Update.getValue(), "");
                        }
                    });
                    break;
                case Constants.CollectionPlan:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRequestSuccess(Operation.Update.getValue(), "");
                        }
                    });
                    break;
            }
            isBatchReqs = true;
        }
    }

    @Override
    public void responseFailed(final ODataRequestExecution request, String s, Bundle bundle) {
        Log.d("SyncError", "responseFailed: " + s);
        if (!isBatchReqs) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TraceLog.scoped(this).d(Constants.RequestFailed);
                    if (request != null && request.getResponse() != null) {
                        ODataPayload payload = ((ODataResponseSingle) request.getResponse()).getPayload();
                        if (payload != null && payload instanceof ODataError) {
                            ODataError oError = (ODataError) payload;
                            TraceLog.d(Constants.RequestFailed_status_message + oError.getMessage());
                            try {
                                ODataRequestParamSingle oDataResponseSingle = (ODataRequestParamSingleDefaultImpl) request.getRequest();
                                ODataEntity oDataEntity = (ODataEntity) oDataResponseSingle.getPayload();
                                Constants.Entity_Set.add(oDataEntity.getResourcePath());
                            } catch (Exception e3) {
                                e3.printStackTrace();
                            }
                            LogManager.writeLogError(Constants.Error + " :" + oError.getMessage());
                            Constants.AL_ERROR_MSG.add(oError.getMessage());
                           // onRequestError(Operation.Update.getValue(), new com.rspl.sf.msfa.store.OnlineODataStoreException(oError.getMessage()));
                            return;
                        }
                    }
                    onRequestError(Operation.Update.getValue(), null);
                }
            });
            isBatchReqs = true;
        }
    }



    @Override
    public void onRefresh() {

       refreshpage();



    }

    private void refreshpage() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Write whatever to want to do after delay specified (1 sec)
                Log.d("Handler", "Running Handler");
                try {
                    pendingCount = 0;
                    pendingCountBeanList.clear();
                    pendingCountBeanList.addAll(getRecordInfo(getActivity()));
                    pendingCountAdapter.notifyDataSetChanged();
                    simpleUpdateHistoryAdapter.refreshAdapter(syncHistoryModelList);
                    pendingCount += databaseHelper.getSqlLocationDataCount();
                    tvPendingCount.setText(String.valueOf(pendingCount));
                    if (pendingCount > 0) {
                        cvUpdatePending.setVisibility(View.VISIBLE);
                        tvPendingStatus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.RejectedColor));
                    } else {
                        cvUpdatePending.setVisibility(View.GONE);
                        tvPendingStatus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ApprovedColor));
                    }
                    swipeRefresh.setRefreshing(false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 2000);
    }

    public static class ArrayComarator implements Comparator<String[]> {

        @Override
        public int compare(String s1[], String s2[]) {
            BigInteger i1 = null;
            BigInteger i2 = null;
            try {
                i1 = new BigInteger(s1[0]);
            } catch (NumberFormatException e) {
            }

            try {
                i2 = new BigInteger(s2[0]);
            } catch (NumberFormatException e) {
            }

            if (i1 != null && i2 != null) {
                return i1.compareTo(i2);
            } else {
                return s1[0].compareTo(s2[0]);
            }
        }

    }

    public static ArrayList<String> getRefreshList(Context context) {
        ArrayList<String> alAssignColl = new ArrayList<>();
       /* try {


            if (OfflineManager.getVisitStatusForCustomerGeo(Constants.SPGeos + Constants.isLocalFilterQry)) {
                    alAssignColl.add(Constants.SPGeos);
                }

                if (OfflineManager.getVisitStatusForCustomerGeo(Constants.SyncHistorys + Constants.isLocalFilterQry)) {
                    alAssignColl.add(Constants.SyncHistorys);
                }

        } catch (OfflineODataStoreException e) {
            e.printStackTrace();
        }*/
        return alAssignColl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sync_history_info, container, false);
        recycler_view_His = view.findViewById(R.id.recycler_view_His);
        rvSyncTime = view.findViewById(R.id.rvSyncTime);
        ivUploadDownload = view.findViewById(R.id.ivUploadDownload);
        tvPendingStatus = view.findViewById(R.id.tvPendingStatus);
        cvUpdatePending = view.findViewById(R.id.cvUpdatePending);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
//        swipeRefresh.setRefreshing(false);
        swipeRefresh.setOnRefreshListener(this);

        nestedScroll = view.findViewById(R.id.nestedScroll);
        ivSyncAll = view.findViewById(R.id.ivSyncAll);
        ivUploadDownload.setOnClickListener(SyncHistoryInfoFragment.this);
        ivSyncAll.setOnClickListener(SyncHistoryInfoFragment.this);
        recycler_view_His.setHasFixedSize(false);
        rvSyncTime.setHasFixedSize(false);
        databaseHelper = DatabaseHelperGeo.getInstance(this.getActivity());
        sqlDbCount = databaseHelper.getSqlLocationDataCount();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
        ViewCompat.setNestedScrollingEnabled(recycler_view_His, false);
        recycler_view_His.setLayoutManager(linearLayoutManager);
        linearLayoutManager = new LinearLayoutManager(this.getActivity());
        ViewCompat.setNestedScrollingEnabled(rvSyncTime, false);
        rvSyncTime.setLayoutManager(linearLayoutManager);
        pendingCountAdapter = new PendingCountAdapter(pendingCountBeanList, getActivity(), this);
        recycler_view_His.setAdapter(pendingCountAdapter);
        setSyncTimeAdapter();
//        pendingCountBeanList = getRecordInfo();
        initRecyclerView();
        ConstantsUtils.focusOnView(nestedScroll);
      //  initservice();


        return view;
    }

   /* private void initservice() {
        Intent i = new Intent();
        i.setClassName(getContext().getPackageName(), TrackerService.class.getName());
        boolean bindResult = getContext().bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        Log.i("SyncHistory", "initService() bindResult: " + bindResult);
    }*/

   /* private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = GetDeviceInfoInterface.Stub.asInterface(boundService);
         //   Toast.makeText(getContext(), "AIDL service connected", Toast.LENGTH_LONG).show();
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
        //    Toast.makeText(getContext(), "AIDL service disconnected", Toast.LENGTH_LONG).show();
        }
    };*/
    private void setSyncTimeAdapter() {
        simpleUpdateHistoryAdapter = new SimpleRecyclerViewAdapter<SyncHistoryModel>(getActivity(), R.layout.item_history_time, new AdapterInterface<SyncHistoryModel>() {
            @Override
            public void onItemClick(SyncHistoryModel o, View view, int i) {

            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i, View view) {
                return new HistoryTimeVH(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i, SyncHistoryModel o) {
                ((HistoryTimeVH) viewHolder).tvEntityName.setText(o.getCollections());
                ((HistoryTimeVH) viewHolder).tvSyncTime.setText(o.getTimeStamp());
            }
        }, null, null);
        rvSyncTime.setAdapter(simpleUpdateHistoryAdapter);
    }

    private void postCapturedGeoData() {
        (new Thread(new Runnable() {
            public void run() {
                makeCSRFToken();
            }
        })).start();

    }
    private String csrfToken;
    HttpsURLConnection connection = null;
    List<String> setCookies= new ArrayList<>();
    private void makeCSRFToken() {
//                String host = "https://mobile-acf7a3df7.hana.ondemand.com/com.arteriatech.geotracker";
        String host = "https://"+ Configuration.server_Text+"/"+Configuration.APP_ID;
        String url = host + "/SPGeos";
        //String url1 = host + "/ValidateSPIMEI";
        /*353410103024281*/

        try {
            String result = getPuserIdUtilsReponse(new URL(host), Configuration.UserName, Configuration.Password);
        } catch (final IOException var17) {
            Constants.isSync = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialogue();
                    showAlert(var17.toString());
                }
            });
            var17.printStackTrace();
        } catch (final Exception var18) {
            Constants.isSync = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialogue();
                    showAlert(var18.toString());
                }
            });
            var18.printStackTrace();
        }
    }

    private void syncConfigTypeSet(final String userName, final String psw){
        String host = "https://"+ Configuration.server_Text+"/"+Configuration.APP_ID;
        String strURL = host + "/"+Constants.ConfigTypesetTypes;
        URL url = null;
        try {
            url = new URL(strURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            connection = (HttpsURLConnection)url.openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            String userCredentials = userName + ":" + psw;
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes("UTF-8"), 2);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("x-smp-appid", "com.arteriatech.geotracker");
            connection.setRequestProperty("x-smp-enduser", userName);
            connection.setRequestProperty("X-CSRF-Token", "Fetch");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            connection.getResponseMessage();
            InputStream stream = null;
            if (responseCode != 200) {
                throw new IOException("HTTP error code: " + responseCode);
            }else if(responseCode==200){

            }
        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    public String getPuserIdUtilsReponse(final URL url, final String userName, final String psw) throws IOException {
        String result="";

        try {
            connection = (HttpsURLConnection)url.openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            String userCredentials = userName + ":" + psw;
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes("UTF-8"), 2);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("x-smp-appid", "com.arteriatech.geotracker");
            connection.setRequestProperty("x-smp-enduser", userName);
            connection.setRequestProperty("X-CSRF-Token", "Fetch");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();

            connection.getResponseMessage();
            InputStream stream = null;

            if (responseCode != 200) {
                throw new IOException("HTTP error code: " + responseCode);
            }else if(responseCode==200){
                csrfToken = connection.getHeaderField("X-CSRF-Token");
                setCookies.addAll(connection.getHeaderFields().get("Set-Cookie"));
            }

        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

//                String host = "https://mobile-acf7a3df7.hana.ondemand.com/com.arteriatech.geotracker/SPGeos";
        String host = "https://"+ Configuration.server_Text+"/"+Configuration.APP_ID+"/SPGeos";
        List<JSONObject> bodyObject = getDataFromSqliteDB(getActivity());
        try {
            pendingPostCount = bodyObject.size();
        } catch (Exception e) {
            Constants.isSync=false;
            e.printStackTrace();
        }
        if(pendingPostCount==0){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Constants.isSync = false;
                    hideProgressDialogue();
                    showAlert(getString(R.string.msg_sync_successfully_completed));
                    refreshData();
                }
            });
        }
        try {
            for(JSONObject body :bodyObject) {
                String doc_id = "";
                try {
                    doc_id = body.getString("DOC_ID");
                    body.remove("DOC_ID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                postDataToServer(new URL(host), Configuration.UserName, Configuration.Password, csrfToken, body.toString(),doc_id);
            }
        } catch (final MalformedURLException e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Constants.isSync = false;
                    hideProgressDialogue();
                    showAlert(e.getMessage());
                    refreshData();
                }
            });
            e.printStackTrace();
        }
        return result;
    }

    private String postDataToServer(final URL url, final String userName, final String psw, final String csrfToken, final String body, final String doc_id) {
        String result = null;
        // InputStream stream = null;
        successPostCount++;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            String userCredentials = userName + ":" + psw;
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes("UTF-8"), 2);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("x-smp-appid", "com.arteriatech.geotracker");
            connection.setRequestProperty("X-CSRF-Token", csrfToken);
            for (int i = 0; i < setCookies.size(); i++) {
                connection.addRequestProperty("Cookie", setCookies.get(i));
            }
            connection.addRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            //connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.close();
            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 400 && responseCode != 201) {
//                extralogToStorage("HTTP error code : "+ responseCode + " - "+ doc_id);
                throw new IOException("HTTP error code: " + responseCode);
            }
            if(responseCode==201){
                Log.i("Geo","postedSuccefully");
//                extralogToStorage("postedSuccefully : " + doc_id);
                DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(getActivity());
                databaseHelper.deleteLatLong(doc_id);
            }

                  /*  if (responseCode == 200) {
                        stream = connection.getInputStream();
                        if (stream != null) {
                            //   result = readResponse(stream);
                        }
                    } else {
                        stream = connection.getErrorStream();
                        if (stream != null) {
                            // result = readResponse(stream);
                        }
                    }*/
        } catch (Exception var14) {
//            extralogToStorage("Auto Sync Location Data Service : "+ var14.toString() + " - " + doc_id);
            var14.printStackTrace();
        } finally {
                   /* if (stream != null) {

                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/

		/*	if (connection != null) {
				connection.disconnect();
			}*/
		    if(pendingPostCount==successPostCount){
		        getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Constants.isSync = false;
                        hideProgressDialogue();
                        showAlert(getString(R.string.msg_sync_successfully_completed));
                        refreshData();
                        try {
                            ConstantsUtils.stopAlarmManagerByID(getActivity(), AutoSyncDataLocationAlarmReceiver.class, AutoSyncDataLocationAlarmReceiver.REQUEST_CODE);
                            ConstantsUtils.startAutoSyncLocation(getActivity(), true);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }


        return result;

    }

    private List<JSONObject>  getDataFromSqliteDB(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        List<JSONObject> listofbodyObject = new ArrayList<>();
        SharedPreferences sharedPerf = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        String SPGuid = sharedPerf.getString("SPGUID","");
        String imeiSim1 = sharedPerf.getString("IMEISIM1","");
        String imeiSim2 = sharedPerf.getString("IMEISIM2","");
        String mobileNo = sharedPerf.getString("MobileNo","");
        String serviceProvider1 = sharedPerf.getString("ServiceProvider1","");
        String serviceProvider2 = sharedPerf.getString("ServiceProvider2","");
        String serviceProvider3 = sharedPerf.getString("ServiceProvider3","");
        String serviceProvider4 = sharedPerf.getString("ServiceProvider4","");
        //logStatusToStorage("Step:8 getDataFromSqliteDB ");
        LocationBean locationBean = null;
        ArrayList<LocationBean> alLocationBeans = new ArrayList<>();
        DatabaseHelperGeo databaseHelper = DatabaseHelperGeo.getInstance(context);
        Cursor cursor = databaseHelper.getDataLatLong();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                locationBean = new LocationBean();
                locationBean.setCOLUMN_ID(cursor.getString(0));
                locationBean.setColumnLat(cursor.getString(3));
                locationBean.setColumnLong(cursor.getString(4));
                locationBean.setColumnStartdate(cursor.getString(5));
                locationBean.setColumnStarttime(cursor.getString(6));

//                DateTime date = DateTime.parse("04/02/2011 20:27:05",
//                        DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss"));
//                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss a");
                SimpleDateFormat sdf1 = new SimpleDateFormat("HH-mm-ss");
                String time = "";
                try {
                    Date d = DateFormat.getDateTimeInstance().parse(cursor.getString(9));
                    time = sdf1.format(d);

                } catch (ParseException ex) {
                    ex.printStackTrace();
                    Log.v("Exception", ex.getLocalizedMessage());
                }

//                locationBean.setCOLUMN_Status(cursor.getString(6));
//                locationBean.setColumnTempno(cursor.getString(7));
                locationBean.setColumnTimestamp(time);
                locationBean.setCOLUMN_AppVisibility(cursor.getString(10));
                locationBean.setCOLUMN_BATTERYLEVEL(cursor.getString(11));
                try {
                    locationBean.setCOLUMN_DISTANCE(cursor.getString(12));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                locationBean.setCOLUMN_Loc(cursor.getString(13));
                locationBean.setCOLUMN_PhoneStatePermsn(cursor.getString(14));
                locationBean.setCOLUMN_ExtStoragePermsn(cursor.getString(15));
                locationBean.setCOLUMN_GPSEnabled(cursor.getString(16));
                locationBean.setCOLUMN_AutoTimeZone(cursor.getString(17));
                locationBean.setCOLUM_NDeviceAdmnPermsn(cursor.getString(18));
                locationBean.setCOLUMN_MockLocation(cursor.getString(19));
                locationBean.setCOLUMN_PhoneRestartInd(cursor.getString(20));
                locationBean.setCOLUMN_AppInstallStatus(cursor.getString(21));
                locationBean.setCOLUMN_AccuracyLevel(cursor.getString(22));
                locationBean.setCOLUMN_GPSStatus(cursor.getString(23));
                locationBean.setCOLUMNXMLTemp(cursor.getString(24));
                alLocationBeans.add(locationBean);
            }
            //  logStatusToStorage("Step:9 Location list size"+String.valueOf(alLocationBeans.size()));
            JSONObject bodyObject = new JSONObject();
            for(int i =0;i<alLocationBeans.size();i++){
                bodyObject = new JSONObject();
                locationBean = alLocationBeans.get(i);
                try {
                    GUID guid = GUID.newRandom();
                    bodyObject.put("GeoGUID", guid.toString().toUpperCase());
                    bodyObject.put("SPGUID", SPGuid);
                    bodyObject.put("GeoDate",locationBean.getColumnStartdate() );
                    if (!TextUtils.isEmpty(locationBean.getColumnTimestamp())) {
                        ODataDuration startDuration = Constants.getTimeAsODataDurationConvertionLocation(locationBean.getColumnTimestamp());
                        //   latlonghashtable.put(Constants.GeoTime, startDuration);
                        bodyObject.put("GeoTime",startDuration );

                    }


                    extralogToStorage(" Get data from SQLite lat : "+UtilConstants.round(Double.parseDouble(locationBean.getColumnLat()), 12)+", long :"+
                            UtilConstants.round(Double.parseDouble( locationBean.getColumnLong()), 12)+", Geo Date : "+locationBean.getColumnStartdate()+" , GeoTime : "+locationBean.getColumnTimestamp());
                    bodyObject.put("Latitude", String.valueOf(UtilConstants.round(Double.parseDouble(locationBean.getColumnLat()), 12)));
                    bodyObject.put("Longitude",String.valueOf(UtilConstants.round(Double.parseDouble( locationBean.getColumnLong()), 12)));
                    try {
                        bodyObject.put("AppInstallStatus", locationBean.getCOLUMN_AppInstallStatus());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("AppLocPermission", locationBean.getCOLUMN_Loc());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("DeviceAdmnPermsn", locationBean.getCOLUM_NDeviceAdmnPermsn());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ExtStoragePermsn", locationBean.getCOLUMN_ExtStoragePermsn());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    try {
                        bodyObject.put("AutoTimeZone", locationBean.getCOLUMN_AutoTimeZone());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("GPSEnabled", locationBean.getCOLUMN_GPSEnabled());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
//                        bodyObject.put("GPSStatus", locationBean.getCOLUMN_GPSStatus());
                        bodyObject.put("GPSStatus", "2");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("AccuracyLevel", locationBean.getCOLUMN_AccuracyLevel());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    try {
                        bodyObject.put("MockLocation", locationBean.getCOLUMN_MockLocation());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("PhoneRestartInd", locationBean.getCOLUMN_PhoneRestartInd());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("PhoneStatePermsn", locationBean.getCOLUMN_PhoneStatePermsn());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ServiceProvider1", serviceProvider1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        bodyObject.put("ServiceProvider2", serviceProvider2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ServiceProvider3", serviceProvider3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        bodyObject.put("ServiceProvider4", serviceProvider4);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (!TextUtils.isEmpty(locationBean.getCOLUMN_DISTANCE())) {
                            bodyObject.put("Distance", locationBean.getCOLUMN_BATTERYLEVEL());
                        }
                        else {
                            bodyObject.put("Distance", "0.0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (!TextUtils.isEmpty(locationBean.getCOLUMNXMLTemp())) {
                            bodyObject.put("ZZGeoXml", locationBean.getCOLUMNXMLTemp());
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (!TextUtils.isEmpty(locationBean.getCOLUMN_BATTERYLEVEL())) {
                            bodyObject.put("BatteryPerc", locationBean.getCOLUMN_BATTERYLEVEL());
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    //bodyObject.put("Distance", locationBean.getCOLUMN_DISTANCE());
                    bodyObject.put("DistanceUOM", "M");
                    bodyObject.put("MobileNo", mobileNo);
                    bodyObject.put("IMEI1", androidId);
                    bodyObject.put("IMEI2", androidId);
                    bodyObject.put("DOC_ID", locationBean.getCOLUMN_ID());
                    bodyObject.put(Constants.OsVersion, android.os.Build.VERSION.RELEASE);
                    try {
                        bodyObject.put("OSVersionCode",  String.valueOf(Build.VERSION.SDK_INT));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    bodyObject.put(Constants.MobileModel, Constants.getDeviceName());
                    //bodyObject.put("BatteryPerc", locationBean.getCOLUMN_BATTERYLEVEL());
                    //bodyObject.put("IMEI1", "353410103024123");
                    bodyObject.put("APKVersion", BuildConfig.VERSION_NAME);
                    bodyObject.put("APKVersionCode",  String.valueOf(BuildConfig.VERSION_CODE));
                    listofbodyObject.add(bodyObject);
//                    databaseHelper.deleteLatLong(locationBean.getCOLUMN_ID());


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            // updataLatLong(alLocationBeans, context, rListener,offlineGeo,state);
        } else {
           /* if (cursor != null)
                LocationMonitoringService.locationLog(" SQL db record count " + cursor.getCount());*/
        }
        return listofbodyObject;
    }

    private void initRecyclerView() {
        pendingCount = 0;
        pendingCountBeanList.clear();
        pendingCountBeanList.addAll(getRecordInfo(getActivity()));
        pendingCountAdapter.notifyDataSetChanged();
        simpleUpdateHistoryAdapter.refreshAdapter(syncHistoryModelList);
        pendingCount += databaseHelper.getSqlLocationDataCount();
        tvPendingCount.setText(String.valueOf(pendingCount));
        if (pendingCount > 0) {
            cvUpdatePending.setVisibility(View.VISIBLE);
            tvPendingStatus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.RejectedColor));
        } else {
            cvUpdatePending.setVisibility(View.GONE);
            tvPendingStatus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ApprovedColor));
        }
        /*if(sqlDbCount>0)
            moveDataSqlToOfflineDB();*/
    }

    private void moveDataSqlToOfflineDB() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (reentrantLock==null){
                        reentrantLock = new ReentrantLock();
                    }
                    try {
                        Log.e("Main Menu REENTRANT:","LOCKED");
                        reentrantLock.lock();
                        /*Constants.getDataFromSqliteDB(getActivity(), new UIListener() {
                            @Override
                            public void onRequestError(int i, Exception e) {
                                responseCount++;
                                if(responseCount>=sqlDbCount)
                                    checkAndRefresh();
                            }

                            @Override
                            public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                                responseCount++;
                                if(responseCount>=sqlDbCount)
                                    checkAndRefresh();
                            }
                        });*/
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Main Menu EXCEPTION","ANR EXCEPTION OCCURRED");
                    }finally {
                        if (reentrantLock!=null&&reentrantLock.isHeldByCurrentThread())reentrantLock.unlock();
                        Log.e("Main Menu REENTRANT:","UNLOCKED FINALLY");
                    }
                }
            }).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void checkAndRefresh() {
        if(pendingCountBeanList!=null && pendingCountBeanList.size()>0)
            for(PendingCountBean countBeanData : pendingCountBeanList)
                countBeanData.setShowProgress(false);

        pendingCountAdapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);

    }

    private List<PendingCountBean> getRecordInfo(Context mContext) {
        pendingCount = 0;
        syncHistoryModelList.clear();
        syncHistoryModelList.addAll(getAllRecords());
        try {
            Collections.sort(syncHistoryModelList, new Comparator<SyncHistoryModel>() {
                public int compare(SyncHistoryModel one, SyncHistoryModel other) {
                    return one.getCollections().compareTo(other.getCollections());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        PendingCountBean countBean = null;
        int count = 0;
        List<PendingCountBean> pendingCountBeans = new ArrayList();
        Set<String> set = new HashSet<>();
     //   SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
        ArrayList<String> alTempList = new ArrayList<>();
        List<PendingCountBean> temppendingList = new ArrayList();
        List<PendingCountBean> tempNonpendingList = new ArrayList();
        ArrayList<String> alCollectionList = null;
        for (int k = 0; k < syncHistoryModelList.size(); k++) {
            alCollectionList = new ArrayList<>();
            SyncHistoryModel historyModel = syncHistoryModelList.get(k);
            try {
                /* if ((historyModel.getCollections().equalsIgnoreCase(Constants.SyncHistorys)) && !alTempList.contains(Constants.SyncHistorys)) {
                    count = 0;
                    alTempList.add("SyncHistorys");
                    countBean = new PendingCountBean();
                    count = OfflineManager.getPendingCount(Constants.SyncHistorys + "?$filter= sap.islocal() ");
                    alCollectionList.add(Constants.SyncHistorys);
                    if (count > 0) {
                        pendingCount = pendingCount + count;
                        countBean.setCollection("Sync Historys");
                        countBean.setCount(count);
                        countBean.setSyncTime(historyModel.getTimeStamp());
                        countBean.setAlCollectionList(alCollectionList);
                        temppendingList.add(countBean);
                    } else {
                        countBean.setCollection("Sync Historys");
                        countBean.setCount(count);
                        countBean.setSyncTime(historyModel.getTimeStamp());
                        countBean.setAlCollectionList(alCollectionList);
                        tempNonpendingList.add(countBean);
                    }
                }else if ((historyModel.getCollections().equalsIgnoreCase(Constants.SPGeos)) && !alTempList.contains(Constants.SPGeos)) {
                    count = 0;
                    alTempList.add("SPGeos");
                    alCollectionList.add("SPGeos");
                    countBean = new PendingCountBean();
                    if(sqlDbCount>0) countBean.setShowProgress(true);
                    else countBean.setShowProgress(false);
                 //   count = OfflineManager.getPendingCountGeo(Constants.SPGeos + "?$filter= sap.islocal() ");
                    count += databaseHelper.getSqlLocationDataCount();
                    if (count > 0) {
                        pendingCount = pendingCount + count;
                        countBean.setCollection("Geo");
                        countBean.setCount(count);
                        countBean.setSyncTime(historyModel.getTimeStamp());
                        countBean.setAlCollectionList(alCollectionList);
                        temppendingList.add(countBean);
                    } else {
                        countBean.setCollection("Geo");
                        countBean.setCount(count);
                        countBean.setSyncTime(historyModel.getTimeStamp());
                        countBean.setAlCollectionList(alCollectionList);
                        tempNonpendingList.add(countBean);
                    }
                }else*/ if ((historyModel.getCollections().equalsIgnoreCase(Constants.ConfigTypsetTypeValues)) && !alTempList.contains(Constants.ConfigTypsetTypeValues)) {
                     count = 0;
                     alTempList.add("ConfigTypsetTypeValues");
                     countBean = new PendingCountBean();
//                     count = OfflineManager.getPendingCount(Constants.ConfigTypsetTypeValues + "?$filter= sap.islocal() ");
                     alCollectionList.add(Constants.ConfigTypsetTypeValues);
                     if (count > 0) {
                         pendingCount = pendingCount + count;
                         countBean.setCollection("Config Values");
                         countBean.setCount(count);
                         countBean.setSyncTime(historyModel.getTimeStamp());
                         countBean.setAlCollectionList(alCollectionList);
                         temppendingList.add(countBean);
                     } else {
                         countBean.setCollection("Config Values");
                         countBean.setCount(count);
                         countBean.setSyncTime(historyModel.getTimeStamp());
                         countBean.setAlCollectionList(alCollectionList);
                         tempNonpendingList.add(countBean);
                     }
                 }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(tempNonpendingList, new Comparator<PendingCountBean>() {
            public int compare(PendingCountBean one, PendingCountBean other) {
                return one.getCollection().compareTo(other.getCollection());
            }
        });

        Collections.sort(temppendingList, new Comparator<PendingCountBean>() {
            public int compare(PendingCountBean one, PendingCountBean other) {
                return one.getCollection().compareTo(other.getCollection());
            }
        });
        pendingCountBeans.addAll(temppendingList);
        pendingCountBeans.addAll(tempNonpendingList);
        return pendingCountBeans;
    }

    private void onUpdateSync() {

        Constants.Entity_Set.clear();
        Constants.AL_ERROR_MSG.clear();
        mBoolIsNetWorkNotAval = false;
        isBatchReqs = false;
        mBoolIsReqResAval = true;
        updateCancelSOCount = 0;
        cancelSoPos = 0;
        try{
            penReqCount = 0;
            mIntPendingCollVal = 0;
            invKeyValues = null;
            cancelSOCount = new int[0];
            ArrayList<Object> objectArrayList = getPendingCollList(getActivity(), false);
            if (!objectArrayList.isEmpty()) {
                mIntPendingCollVal = (int) objectArrayList.get(0);
                invKeyValues = (String[][]) objectArrayList.get(1);
                cancelSOCount = (int[]) objectArrayList.get(2);
            }

           /* if (OfflineManager.offlineStore.getRequestQueueIsEmpty() && mIntPendingCollVal == 0) {
                initRecyclerView();
                UtilConstants.showAlert(getString(R.string.no_req_to_update_sap), getActivity());
            } else {*/
            alAssignColl.clear();
            alFlushColl.clear();
            ArrayList<String> allAssignColl = getRefreshList(getActivity());
            if (!allAssignColl.isEmpty()) {
                alAssignColl.addAll(allAssignColl);
                alFlushColl.addAll(allAssignColl);
            }
            if (Constants.iSAutoSync || Constants.isBackGroundSync) {
                if (Constants.iSAutoSync) {
                    showAlert(getString(R.string.alert_auto_sync_is_progress));
                } else if (Constants.isBackGroundSync) {
                    showAlert(getString(R.string.alert_backgrounf_sync_is_progress));
                }
            } else {
                if (mIntPendingCollVal > 0) {

                    if (!alAssignColl.contains(Constants.ConfigTypsetTypeValues))
                        alAssignColl.add(Constants.ConfigTypsetTypeValues);
                    if (UtilConstants.isNetworkAvailable(getActivity())) {
                        Constants.updateStartSyncTime(getActivity(), Constants.UpLoad, Constants.StartSync);
                        Constants.isSync = true;
                        onPostOnlineData();
                    } else {
                        showAlert(getString(R.string.no_network_conn));
                    }
                } else if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                    if (UtilConstants.isNetworkAvailable(getActivity())) {
                        Constants.updateStartSyncTime(getActivity(), Constants.UpLoad, Constants.StartSync);
                        onPostOfflineData();
                    } else {
                        showAlert(getString(R.string.no_network_conn));
                    }
                }

//                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*private void checkPendingReqIsAval() {
        try {
            mIntPendingCollVal = 0;
            invKeyValues = null;
            ArrayList<Object> objectArrayList = getPendingCollList(getActivity(), false);
            if (!objectArrayList.isEmpty()) {
                mIntPendingCollVal = (int) objectArrayList.get(0);
                invKeyValues = (String[][]) objectArrayList.get(1);
            }

            penReqCount = 0;


            alAssignColl.clear();
            alFlushColl.clear();
            concatCollectionStr = "";
            concatFlushCollStr = "";
            ArrayList<String> allAssignColl = getRefreshList(getActivity());
            if (!allAssignColl.isEmpty()) {
                alAssignColl.addAll(allAssignColl);
                alFlushColl.addAll(allAssignColl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/


    private void onPostOnlineData() {
        try {
      //      new AsyncPostDataValutData().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestError(int operation, Exception ex) {
        isClickable =false;
        if (ex != null)
            ex.printStackTrace();
        final ErrorBean errorBean = Constants.getErrorCode(operation, ex, getActivity());
        try {
            if (errorBean.hasNoError()) {
                mBoolIsReqResAval = true;
//                ConstantsUtils.printErrorLog(ex.getMessage());
                if (dialogCancelled == false && !Constants.isStoreClosed) {

                    if (operation == Operation.Update.getValue() && mIntPendingCollVal > 0) {
                        updateCancelSOCount++;
                        updateCancelSOCount = 0;
                        cancelSoPos++;
                        String popUpText = "";
                        if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOCancel))
                            popUpText = "Sales order # " + invKeyValues[penReqCount][0] + " cancellation failed.";
                        else
                            popUpText = "Sales order # " + invKeyValues[penReqCount][0] + " changed failed.";
                        LogManager.writeLogInfo(popUpText);
                    }
                    penReqCount++;

                    if ((operation == Operation.Create.getValue() || operation == Operation.Update.getValue()) && (penReqCount == mIntPendingCollVal)) {

                        try {
                            if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                                closingProgressDialog();
                                if (UtilConstants.isNetworkAvailable(getActivity())) {
                                    try {
                                        new AsyncPostOfflineData().execute();
                                    } catch (Exception e2) {
                                        e2.printStackTrace();
                                    }
                                } else {
                                    Constants.isSync = false;
                                    showAlert(getString(R.string.data_conn_lost_during_sync));                                }
                            } else {
                                if (UtilConstants.isNetworkAvailable(getActivity())) {
                                    try {
                                        for (int incVal = 0; incVal < alAssignColl.size(); incVal++) {
                                            if (incVal == 0 && incVal == alAssignColl.size() - 1) {
                                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                                            } else if (incVal == 0) {
                                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                                            } else if (incVal == alAssignColl.size() - 1) {
                                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                                            } else {
                                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                                            }
                                        }
                                        new RefreshAsyncTask(getActivity(), concatCollectionStr, this).execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        TraceLog.e(Constants.SyncOnRequestSuccess, e);
                                    }
                                } else {
                                    Constants.isSync = false;
                                    closingProgressDialog();
                                    showAlert(getString(R.string.data_conn_lost_during_sync));                                }
                            }

                        } catch (ODataException e3) {
                            e3.printStackTrace();
                        }
                    }

                }

                if (operation == Operation.OfflineFlush.getValue()) {
                    if (UtilConstants.isNetworkAvailable(getActivity())) {
                        try {
                            for (int incVal = 0; incVal < alAssignColl.size(); incVal++) {
                                if (incVal == 0 && incVal == alAssignColl.size() - 1) {
                                    concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                                } else if (incVal == 0) {
                                    concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                                } else if (incVal == alAssignColl.size() - 1) {
                                    concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                                } else {
                                    concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                                }
                            }
                            new RefreshAsyncTask(getActivity(), concatCollectionStr, this).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                            TraceLog.e(Constants.SyncOnRequestSuccess, e);
                        }
                    } else {
                        Constants.isSync = false;
                        closingProgressDialog();
                        showAlert(getString(R.string.data_conn_lost_during_sync));
                    }
                } else if (operation == Operation.OfflineRefresh.getValue()) {
                    updatingSyncTime();
                    Constants.isSync = false;
                    String mErrorMsg = "";
                    if (Constants.AL_ERROR_MSG.size() > 0) {
                        mErrorMsg = Constants.convertALBussinessMsgToString(Constants.AL_ERROR_MSG);
                    }
                    final String finalMErrorMsg = mErrorMsg;
                    Constants.updateSyncTime(alAssignColl, getActivity(), Constants.DownLoad, new SyncHistoryCallBack() {
                        @Override
                        public void displaySuccessMessage() {
                            closingProgressDialog();
                            if (finalMErrorMsg.equalsIgnoreCase("")) {
                                if(errorBean.getErrorMsg().contains("invalid authentication")){
                                    Constants.customAlertDialogWithScroll(getActivity(), errorBean.getErrorMsg());
                                }else{
                                    showAlert(errorBean.getErrorMsg());
                                }

                            } else {
                                Constants.customAlertDialogWithScroll(getActivity(), finalMErrorMsg);
                            }
                        }
                    });

                } else if (operation == Operation.GetStoreOpen.getValue()) {
                    mBoolIsReqResAval = true;
                    mBoolIsNetWorkNotAval = true;
                    closingProgressDialog();
                    Constants.isSync = false;
                    showAlert(errorBean.getErrorMsg());
                }
            } else if (errorBean.isStoreFailed()) {
                if (UtilConstants.isNetworkAvailable(getActivity())) {
                    mBoolIsReqResAval = true;
                    mBoolIsNetWorkNotAval = true;
                    Constants.isSync = true;
                    closingProgressDialog();
                    onSyncAll();
                } else {
                    mBoolIsReqResAval = true;
                    mBoolIsNetWorkNotAval = true;
                    Constants.isSync = false;
                    closingProgressDialog();
                    Constants.displayMsgReqError(errorBean.getErrorCode(), getActivity());
                }
            } else {
                mBoolIsReqResAval = true;
                mBoolIsNetWorkNotAval = true;
                Constants.isSync = false;
                closingProgressDialog();
                Constants.displayMsgReqError(errorBean.getErrorCode(), getActivity());
            }
        } catch (Exception e) {
            mBoolIsReqResAval = true;
            mBoolIsNetWorkNotAval = true;
            Constants.isSync = false;
            closingProgressDialog();
            Constants.displayMsgReqError(errorBean.getErrorCode(), getActivity());
        }
    }

    @Override
    public void onRequestSuccess(int operation, String key)  {
        isClickable =false;
        if (dialogCancelled == false && !Constants.isStoreClosed) {
            if (operation == Operation.Create.getValue() && mIntPendingCollVal > 0) {
                mBoolIsReqResAval = true;
             /*   if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.CollList)) {
                    Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.CollList, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SalesOrderDataValt)) {
                    Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.SalesOrderDataValt, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOUpdate)) {
                    Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.SOUpdate, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOCancel)) {
                    Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.SOCancel, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.Expenses)) {
                    Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.Expenses, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.MTPDataValt)) {
                    Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.MTPDataValt, invKeyValues[penReqCount][0]);
                } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.RTGSDataValt)) {
                    Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.RTGSDataValt, invKeyValues[penReqCount][0]);
                }

                UtilDataVault.storeInDataVault(invKeyValues[penReqCount][0], "");*/

                penReqCount++;
            }
            if (operation == Operation.Update.getValue() && mIntPendingCollVal > 0) {
                mBoolIsReqResAval = true;
                updateCancelSOCount++;
                if (cancelSOCount.length == 0 || updateCancelSOCount == cancelSOCount[cancelSoPos]) {
                    updateCancelSOCount = 0;
                    cancelSoPos++;

                  /*  if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.CollList)) {
                        Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.CollList, invKeyValues[penReqCount][0]);
                    } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SalesOrderDataValt)) {
                        Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.SalesOrderDataValt, invKeyValues[penReqCount][0]);
                    } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOUpdate)) {
                        Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.SOUpdate, invKeyValues[penReqCount][0]);
                    } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOCancel)) {
                        Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.SOCancel, invKeyValues[penReqCount][0]);
                    } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.Expenses)) {
                        Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.Expenses, invKeyValues[penReqCount][0]);
                    } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.MTPDataValt)) {
                        Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.MTPDataValt, invKeyValues[penReqCount][0]);
                    } else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.RTGSDataValt)) {
                        Constants.removeDeviceDocNoFromSharedPref(getActivity(), Constants.RTGSDataValt, invKeyValues[penReqCount][0]);
                    }

                    UtilDataVault.storeInDataVault(invKeyValues[penReqCount][0], "");*/
                   /* String popUpText = "";
                    if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOCancel))
                        popUpText = "Sales order # " + invKeyValues[penReqCount][0] + " cancelled successfully.";
                    else if (invKeyValues[penReqCount][1].equalsIgnoreCase(Constants.SOUpdate))
                        popUpText = "Sales order # " + invKeyValues[penReqCount][0] + " changed successfully.";
                    LogManager.writeLogInfo(popUpText);*/

                    penReqCount++;
                }
            }
            if ((operation == Operation.Create.getValue() || operation == Operation.Update.getValue()) && (penReqCount == mIntPendingCollVal)) {
                try {
                    if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                        closingProgressDialog();
                        if (UtilConstants.isNetworkAvailable(getActivity())) {
                            try {
                                new AsyncPostOfflineData().execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Constants.isSync = false;
                            showAlert(getString(R.string.data_conn_lost_during_sync));
                        }
                    } else {
                        if (UtilConstants.isNetworkAvailable(getActivity())) {
                            try {
                                for (int incVal = 0; incVal < alAssignColl.size(); incVal++) {
                                    if (incVal == 0 && incVal == alAssignColl.size() - 1) {
                                        concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                                    } else if (incVal == 0) {
                                        concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                                    } else if (incVal == alAssignColl.size() - 1) {
                                        concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                                    } else {
                                        concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                                    }
                                }
                                new RefreshAsyncTask(getActivity(), concatCollectionStr, this).execute();
//                                OfflineManager.refreshRequests(getApplicationContext(), concatCollectionStr, SyncSelectionActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                                TraceLog.e(Constants.SyncOnRequestSuccess, e);
                            }
                        } else {
                            Constants.isSync = false;
                            closingProgressDialog();
                            showAlert(getString(R.string.data_conn_lost_during_sync));
                        }
                    }

                } catch (ODataException e) {
                    e.printStackTrace();
                }

            } else if (operation == Operation.OfflineFlush.getValue()) {
                if (UtilConstants.isNetworkAvailable(getActivity())) {
                    try {
                        for (int incVal = 0; incVal < alAssignColl.size(); incVal++) {
                            if (incVal == 0 && incVal == alAssignColl.size() - 1) {
                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                            } else if (incVal == 0) {
                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                            } else if (incVal == alAssignColl.size() - 1) {
                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal);
                            } else {
                                concatCollectionStr = concatCollectionStr + alAssignColl.get(incVal) + ", ";
                            }
                        }
//                        OfflineManager.refreshRequests(getApplicationContext(), concatCollectionStr, SyncSelectionActivity.this);
                        new RefreshAsyncTask(getActivity(), concatCollectionStr, this).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                        TraceLog.e(Constants.SyncOnRequestSuccess, e);
                    }
                } else {
                    Constants.isSync = false;
                    closingProgressDialog();
                    showAlert(getString(R.string.data_conn_lost_during_sync));
                }
            } else if (operation == Operation.OfflineRefresh.getValue()) {
                refreshData();
                /*
                try {
                    if (!OfflineManager.offlineGeo.getRequestQueueIsEmpty()) {
                        try {
                            OfflineManager.flushQueuedRequestsForGeo(new UIListener() {
                                @Override
                                public void onRequestError(int i, Exception e) {

                                }

                                @Override
                                public void onRequestSuccess(int operation, String s) throws ODataException, OfflineODataStoreException {
                                    if (operation == Operation.OfflineFlush.getValue()) {
                                        if (UtilConstants.isNetworkAvailable(getActivity())) {
                                            try {
//                        OfflineManager.refreshRequests(getApplicationContext(), concatCollectionStr, SyncSelectionActivity.this);
                                                new RefreshGeoAsyncTask(getActivity(), Constants.SPGeos, this).execute();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                TraceLog.e(Constants.SyncOnRequestSuccess, e);
                                            }
                                        } else {
                                            Constants.isSync = false;
                                            closingProgressDialog();
                                            showAlert(getString(R.string.data_conn_lost_during_sync));
                                        }
                                    } else if (operation == Operation.OfflineRefresh.getValue()) {
                                        refreshData();
                                    }
                                }
                            }, Constants.SPGeos);
                        } catch (OfflineODataStoreException e) {
                            e.printStackTrace();
                        }
                    } else {
                        refreshData();

                    }
                } catch (ODataException e) {
                    e.printStackTrace();
                }*/


            } else if (operation == Operation.GetStoreOpen.getValue() && OfflineManager.isOfflineStoreOpen()) {
                Constants.isSync = false;
              //  new NotificationSetClass(getActivity());
               // ConstantsUtils.startAutoSync(getActivity(), false);
                try {
                    OfflineManager.getAuthorizations(getActivity());
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                }
                Constants.setSyncTime(getActivity());
                closingProgressDialog();
                showAlert(getString(R.string.msg_sync_successfully_completed));
            }
        }
    }

    private void refreshData() {
        try {
            OfflineManager.getAuthorizations(getActivity());
        } catch (OfflineODataStoreException e) {
            e.printStackTrace();
        }

        setAppointmentNotification();
        updatingSyncTime();
        Constants.isSync = false;
        String mErrorMsg = "";
        if (Constants.AL_ERROR_MSG.size() > 0) {
            mErrorMsg = Constants.convertALBussinessMsgToString(Constants.AL_ERROR_MSG);
        }

        final String finalMErrorMsg = mErrorMsg;
        Constants.updateSyncTime(alAssignColl, getActivity(), Constants.DownLoad, new SyncHistoryCallBack() {
            @Override
            public void displaySuccessMessage() {
                closingProgressDialog();
                if (mError == 0) {
                  //  ConstantsUtils.serviceReSchedule(getActivity(), true);
                    UtilConstants.dialogBoxWithCallBack(getActivity(), "", getString(R.string.msg_sync_successfully_completed), getString(R.string.ok), "", false, new DialogCallBack() {
                        @Override
                        public void clickedStatus(boolean b) {
                            AppUpgradeConfig.getUpdateAvlUsingVerCode(OfflineManager.offlineStore, getActivity(), BuildConfig.APPLICATION_ID, false);
                        }
                    });
                } else {
                    if (finalMErrorMsg.equalsIgnoreCase("")) {
                        showAlert(getString(R.string.error_occured_during_post));
                    } else {
                        Constants.customAlertDialogWithScroll(getActivity(), finalMErrorMsg);
                    }
                }
            }
        });
    }

    /*private void setStoreOpenUI() {
        closingProgressDialog();
        UtilConstants.showAlert(getString(R.string.msg_offline_store_success),
                getActivity());
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivUploadDownload:
                /*try {
                    Toast.makeText(getContext(), "VersionCode: " + service.getVersionCodes(), Toast.LENGTH_LONG).show();
                } catch (RemoteException e) {
                    Log.i("Synchistory", "service.getVersionCodes() failed with: " + e);
                    e.printStackTrace();
                }*/
                if (UtilConstants.isNetworkAvailable(getActivity())) {
                    if (!Constants.isPullDownSync&&!Constants.isBackGroundSync&&!Constants.iSAutoSync) {
                        if (!isClickable) {
                            isClickable = true;
                            Constants.isSync=true;
                            Constants.iSAutoSync = false;
                            syncType = Constants.UpLoad;
                            showProgressDialogue();
                            postCapturedGeoData();
                        }
                    }else{
                        if (Constants.isPullDownSync||Constants.iSAutoSync||Constants.isBackGroundSync) {
                            if (Constants.iSAutoSync){
                                showAlert(getString(R.string.alert_auto_sync_is_progress));
                            }else{
                                showAlert(getString(R.string.alert_backgrounf_sync_is_progress));
                            }
                        }
                    }
                }else{
                    showAlert(getString(R.string.no_network_conn));
                }
                break;
            case R.id.ivSyncAll:
//                onAllSync();
                break;
        }
    }

    private void onBackPressed() {
        getActivity().finish();
    }

    private void closingProgressDialog() {
        try {
            syncProgDialog.dismiss();
            syncProgDialog = null;
            isClickable = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        concatCollectionStr = "";
        initRecyclerView();
    }


    private void onPostOfflineData() {
        Constants.isSync = true;
        try {
            new AsyncPostOfflineData().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayProgressDialog() {
        try {
            syncProgDialog
                    .show();
            syncProgDialog
                    .setCancelable(true);
            syncProgDialog
                    .setCanceledOnTouchOutside(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialogCancelled = false;
    }

    private static String readResponse(InputStream stream) throws IOException {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line).append('\n');
        }
        return buffer.toString();
    }


    private boolean getConfigTypeSetValues(Context mContext,String userName, String psw) {
        boolean isSuccesfull = false;
        String resultJson = "";
        String host = "https://" + Configuration.server_Text + "/" + Configuration.APP_ID;
        String url1 = host + "/ConfigTypsetTypeValues?$filter=Typeset eq 'SP' &$format=json";
        URL url = null;
        try {
            url = new URL(url1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            connection = (HttpsURLConnection) url.openConnection();

            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(Configuration.connectionTimeOut);
            connection.setConnectTimeout(Configuration.connectionTimeOut);
            String userCredentials = userName + ":" + psw;
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes("UTF-8"), 2);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("x-smp-appid", "com.arteriatech.geotracker");
            connection.setRequestProperty("x-smp-enduser", userName);
            connection.setRequestProperty("X-CSRF-Token", "Fetch");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();

            connection.getResponseMessage();
            InputStream stream = null;

            if (responseCode != 200) {
                throw new IOException("HTTP error code: " + responseCode);
            } else if (responseCode == 200) {
               /* csrfToken = connection.getHeaderField("X-CSRF-Token");
                setCookies.addAll(connection.getHeaderFields().get("Set-Cookie"));*/
                stream = connection.getInputStream();
                if (stream != null) {
                    resultJson = readResponse(stream);
                }
                if (!TextUtils.isEmpty(resultJson)) {
                    JSONObject jsonObject = new JSONObject(resultJson);
                    JSONObject jsonObject1 = new JSONObject(jsonObject.getString("d"));
                    JSONArray jsonArray = jsonObject1.optJSONArray("results");
                    String startTime = "", endTime = "", displacement = "";
                    int timeInterval = 60;
                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("GEOSTRTTME"))
                                startTime = jsonArray.getJSONObject(i).getString("TypeValue");
                            else if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("GEOENDTME"))
                                endTime = jsonArray.getJSONObject(i).getString("TypeValue");
                            else if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("TIMEINTRVL")) {
                                timeInterval = jsonArray.getJSONObject(i).getInt("TypeValue");
                            } else if (jsonArray.getJSONObject(i).getString("Types").equalsIgnoreCase("DISPDIST"))
                                displacement = jsonArray.getJSONObject(i).getString("TypeValue");
                        }
                        SharedPreferences mPrefs = mContext.getSharedPreferences(Constants.PREFS_NAME,mContext.MODE_MULTI_PROCESS);
                        try {
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString(getString(R.string.geo_start_time), startTime);
                            editor.putString(getString(R.string.geo_end_time), endTime);
                            editor.putInt(getString(R.string.geo_location_interval_time), timeInterval);
                            editor.putString(getString(R.string.geo_smallest_displacement), displacement);
                            editor.apply();
                            isSuccesfull = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // editor.putString(Constants.MobileNo,spGuid);

                    }
                }
            }

        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return isSuccesfull;
    }

    @Override
    public void onUploadDownload(boolean isUpload, final PendingCountBean countBean, String syncType) {

        if (UtilConstants.isNetworkAvailable(getActivity())) {
           // Constants.requestConfigTypesetValues(getContext(),Configuration.UserName, Configuration.Password);
            (new Thread(new Runnable() {
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            showProgressDialogue();
                        }
                    });
                    getConfigTypeSetValues(getContext(), Configuration.UserName, Configuration.Password);
                    ConstantsUtils.serviceReSchedule(getContext());
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            hideProgressDialogue();
                        }
                    });
                }
            })).start();
        } else {
            ConstantsUtils.showAlert(getString(R.string.data_conn_lost_during_sync), getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isClickable = false;
                    dialog.cancel();
                }
            });
        }
    }

   /* private void assignCollToArrayList() {
        alAssignColl.clear();
        concatCollectionStr = "";
        alAssignColl.addAll(SyncUtils.getAllSyncValue(getActivity()));
        concatCollectionStr = UtilConstants.getConcatinatinFlushCollectios(alAssignColl);
    }*/

   /* private void onAllSync() {
        if (UtilConstants.isNetworkAvailable(getActivity())) {
            onSyncAll();
        } else {
            UtilConstants.showAlert(getActivity().getString(R.string.no_network_conn), getActivity());
        }
    }*/

    private void onSyncAll() {
        try {
            Constants.AL_ERROR_MSG.clear();
            Constants.Entity_Set.clear();
            Constants.isSync = true;
            dialogCancelled = false;
            Constants.isStoreClosed = false;

            statAsyncTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void statAsyncTask() {
        syncProgDialog = new ProgressDialog(getActivity(), R.style.ProgressDialogTheme);
        syncProgDialog.setMessage(getString(R.string.msg_sync_progress_msg_plz_wait));
        syncProgDialog.setCancelable(true);
        syncProgDialog.setCanceledOnTouchOutside(false);
        syncProgDialog.show();

        syncProgDialog
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface Dialog) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity(), R.style.MyTheme);
                        builder.setMessage(R.string.do_want_cancel_sync)
                                .setCancelable(false)
                                .setPositiveButton(
                                        R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface Dialog,
                                                    int id) {
                                                dialogCancelled = true;
                                                isClickable =false;
                                                onBackPressed();
                                            }
                                        })
                                .setNegativeButton(
                                        R.string.no,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface Dialog,
                                                    int id) {

                                                displayProgressDialog();

                                            }
                                        });
                        builder.show();
                    }
                });
        assignCollToArrayList();
        //new AllSyncAsyncTask(getActivity(), this, new ArrayList<String>()).execute();
    }

    private void assignCollToArrayList() {
        alAssignColl.clear();
        concatCollectionStr = "";
        alAssignColl.addAll(Constants.getDefinigReqList(getActivity()));
    }

    /*public class LoadingData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncProgDialog = new ProgressDialog(getActivity(), R.style.ProgressDialogTheme);
            syncProgDialog.setMessage(getString(R.string.msg_sync_progress_msg_plz_wait));
            syncProgDialog.setCancelable(true);
            syncProgDialog.setCanceledOnTouchOutside(false);
            syncProgDialog.show();

            syncProgDialog
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface Dialog) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    getActivity(), R.style.MyTheme);
                            builder.setMessage(R.string.do_want_cancel_sync)
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            R.string.yes,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface Dialog,
                                                        int id) {
                                                    dialogCancelled = true;
                                                    onBackPressed();
                                                }
                                            })
                                    .setNegativeButton(
                                            R.string.no,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface Dialog,
                                                        int id) {

                                                    displayProgressDialog();

                                                }
                                            });
                            builder.show();
                        }
                    });
        }

        @Override
        protected Void doInBackground(Void... params) {
//            Constants.printLogInfo("check store is opened or not");
            if (!OfflineManager.isOfflineStoreOpen()) {
//                Constants.printLogInfo("check store is failed");
                try {
                    OfflineManager.openOfflineStore(getActivity(), SyncHistoryInfoFragment.this);
                } catch (OfflineODataStoreException e) {
                    LogManager.writeLogError(Constants.error_txt + e.getMessage());
                }
            } else {
                Constants.isStoreClosed = false;
                assignCollToArrayList();
//                Constants.printLogInfo("check store is opened");
                try {
                    OfflineManager.refreshStoreSync(getActivity().getApplicationContext(), SyncHistoryInfoFragment.this, Constants.ALL, "");
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }*/




    private void showProgressDialogue(){
        syncProgDialog = new ProgressDialog(getActivity(), R.style.ProgressDialogTheme);
        syncProgDialog.setMessage(getString(R.string.updating_data_plz_wait));
        syncProgDialog.setCancelable(false);
        syncProgDialog.setCanceledOnTouchOutside(false);
        syncProgDialog.show();
    }

    private void hideProgressDialogue(){
        if(syncProgDialog!=null && syncProgDialog.isShowing()){
            syncProgDialog.dismiss();
        }
    }

    public class AsyncPostOfflineData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncProgDialog = new ProgressDialog(getActivity(), R.style.ProgressDialogTheme);
            syncProgDialog.setMessage(getString(R.string.updating_data_plz_wait));
            syncProgDialog.setCancelable(false);
            syncProgDialog.setCanceledOnTouchOutside(false);
            syncProgDialog.show();
            syncProgDialog
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface Dialog) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    getActivity(), R.style.MyTheme);
                            builder.setMessage(R.string.do_want_cancel_sync)
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            R.string.yes,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface Dialog,
                                                        int id) {
                                                    dialogCancelled = true;
                                                    isClickable =false;
                                                    onBackPressed();
                                                }
                                            })
                                    .setNegativeButton(
                                            R.string.no,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface Dialog,
                                                        int id) {

                                                    displayProgressDialog();

                                                }
                                            });
                            builder.show();
                        }
                    });

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                concatFlushCollStr="";
                for (int incVal = 0; incVal < alFlushColl.size(); incVal++) {
                    if (incVal == 0 && incVal == alFlushColl.size() - 1) {
                        concatFlushCollStr = concatFlushCollStr + alFlushColl.get(incVal);
                    } else if (incVal == 0) {
                        concatFlushCollStr = concatFlushCollStr + alFlushColl.get(incVal) + ", ";
                    } else if (incVal == alFlushColl.size() - 1) {
                        concatFlushCollStr = concatFlushCollStr + alFlushColl.get(incVal);
                    } else {
                        concatFlushCollStr = concatFlushCollStr + alFlushColl.get(incVal) + ", ";
                    }
                }
                try {
                    if (!OfflineManager.offlineStore.getRequestQueueIsEmpty()) {
                        try {
                            dialogCancelled = false;
                            OfflineManager.flushQueuedRequests(SyncHistoryInfoFragment.this, concatFlushCollStr);
                        } catch (OfflineODataStoreException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (ODataException e) {
                    e.printStackTrace();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    /*private List<SyncHistoryModel> getRecordInfo() {
        List<SyncHistoryModel> syncHistoryModelList = (new SyncHistoryDB(this.getActivity())).getAllRecord();
        List<SyncHistoryModel> duplicateSyncHistoryModelList = new ArrayList();
        ArrayList<String> alEntity = new ArrayList<>();
        for (int k=0; k<syncHistoryModelList.size(); k++){
            SyncHistoryModel historyModel = syncHistoryModelList.get(k);

            if((historyModel.getCollections().equalsIgnoreCase("RoutePlans") || historyModel.getCollections().equalsIgnoreCase("RouteSchedulePlans") || historyModel.getCollections().equalsIgnoreCase("RouteSchedules")) && !alEntity.contains("Beat")){
                SyncHistoryModel model = new SyncHistoryModel();
                model.setCollections("Beat");
                model.setTimeStamp(historyModel.getTimeStamp());
                duplicateSyncHistoryModelList.add(model);
                alEntity.add("Beat");
            }else if((historyModel.getCollections().equalsIgnoreCase("ChannelPartners") || historyModel.getCollections().equalsIgnoreCase("CPDMSDivisons") ) && !alEntity.contains("Retailers")){
                SyncHistoryModel model = new SyncHistoryModel();
                model.setCollections("Retailers");
                model.setTimeStamp(historyModel.getTimeStamp());
                duplicateSyncHistoryModelList.add(model);
                alEntity.add("Retailers");

            }else if((historyModel.getCollections().equalsIgnoreCase("SSSOs")  || historyModel.getCollections().equalsIgnoreCase("SSSOItemDetails")) && !alEntity.contains("Sales Order")){
                SyncHistoryModel model = new SyncHistoryModel();
                model.setCollections("Sales Order");
                model.setTimeStamp(historyModel.getTimeStamp());
                duplicateSyncHistoryModelList.add(model);
                alEntity.add("Sales Order");
            }else if((historyModel.getCollections().equalsIgnoreCase("FinancialPostings") || historyModel.getCollections().equalsIgnoreCase("FinancialPostingItemDetails")) && !alEntity.contains("Collections")){
                SyncHistoryModel model = new SyncHistoryModel();
                model.setCollections("Collections");
                model.setTimeStamp(historyModel.getTimeStamp());
                duplicateSyncHistoryModelList.add(model);
                alEntity.add("Collections");
            }else if((historyModel.getCollections().equalsIgnoreCase("Visits") || historyModel.getCollections().equalsIgnoreCase("VisitActivities")) && !alEntity.contains("Visits")){
                SyncHistoryModel model = new SyncHistoryModel();
                model.setCollections("Visits");
                model.setTimeStamp(historyModel.getTimeStamp());
                duplicateSyncHistoryModelList.add(model);
                alEntity.add("Visits");
            }else if((historyModel.getCollections().equalsIgnoreCase("Attendances")) && !alEntity.contains("Attendances")){
                SyncHistoryModel model = new SyncHistoryModel();
                model.setCollections("Attendances");
                model.setTimeStamp(historyModel.getTimeStamp());
                duplicateSyncHistoryModelList.add(model);
                alEntity.add("Attendances");
            }
        }

        Collections.sort(duplicateSyncHistoryModelList, new Comparator<SyncHistoryModel>() {
            @Override
            public int compare(SyncHistoryModel historyModel, SyncHistoryModel historyMode2) {
                return historyModel.getCollections().compareTo(historyMode2.getCollections());
            }
        } );
        return duplicateSyncHistoryModelList;
    }*/
    /*private class OpenOfflineStore extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncProgDialog = new ProgressDialog(getActivity(), R.style.ProgressDialogTheme);
            syncProgDialog.setMessage(getString(R.string.app_loading));
            syncProgDialog.setCancelable(false);
            syncProgDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                try {
                    if (!OfflineManager.isOfflineStoreOpen()) {
                        try {
                            OfflineManager.openOfflineStore(getActivity(), SyncHistoryInfoFragment.this);
                        } catch (OfflineODataStoreException e) {
                            LogManager.writeLogError(Constants.error_txt + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (InterruptedException e) {
                LogManager.writeLogError(Constants.error_txt + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }*/


    /*
   ToDo Update Last Sync time into DB table
    */
    private void updatingSyncTime() {
        if (!Constants.syncHistoryTableExist()) {
            try {
                Constants.createSyncDatabase(getActivity());  // create sync history table
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            /*String syncTime = Constants.getSyncHistoryddmmyyyyTime();
            for (int incReq = 0; incReq < alAssignColl.size(); incReq++) {
                String colName = alAssignColl.get(incReq);
                if (colName.contains("?$")) {
                    String splitCollName[] = colName.split("\\?");
                    colName = splitCollName[0];
                }
                Constants.events.updateStatus(Constants.SYNC_TABLE,
                        colName, Constants.timeStamp, syncTime
                );
            }*/
            //  Constants.updateSyncTime(alAssignColl,getActivity(),Constants.DownLoad);
        } catch (Exception exce) {
            exce.printStackTrace();
            LogManager.writeLogError(Constants.sync_table_history_txt + exce.getMessage());
        }
    }
    @Override
    public void onResume() {
        Log.e("DEBUG", "onResume of SynHistory Fragment");
        super.onResume();
//        refreshpage();

    }
    private void setAppointmentNotification() {
     //   new NotificationSetClass(getActivity());

    }

    private ArrayList<SyncHistoryModel> getAllRecords(){
        ArrayList<SyncHistoryModel> syncHistoryModels = new ArrayList<>();
        Cursor syncHistCursor = SyncHist.getInstance().findAllSyncHist();

        if (syncHistCursor!=null && syncHistCursor.getCount() > 0) {
            while (syncHistCursor.moveToNext()) {
                SyncHistoryModel syncHistoryModel = new SyncHistoryModel();
                syncHistoryModel.setCollections(syncHistCursor.getString(syncHistCursor
                        .getColumnIndex(Constants.Collections)));
                syncHistoryModel.setTimeStamp(syncHistCursor
                        .getString(syncHistCursor
                                .getColumnIndex(Constants.TimeStamp)));
                syncHistoryModels.add(syncHistoryModel);
            }
        }
        return syncHistoryModels;
    }

    private void showAlert(String message){
        ConstantsUtils.showAlert(message, getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isClickable=false;
                dialog.cancel();
            }
        });
    }
    private void extralogToStorage(String data) {
        try {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String currentDateandTime = sdf.format(new Date());
                File extraLogPath = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                        UtilConstants.getCurrentDate()+".txt");
                if (!extraLogPath.exists()) {
                    try {
                        extraLogPath.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (extraLogPath.exists()) {

                        BufferedWriter writer = new BufferedWriter(new FileWriter(extraLogPath.getAbsolutePath(), true));
                        writer.write(currentDateandTime + "::" + data);
                        writer.newLine();
                        writer.close();
                    }
                } catch (Exception e) {
                    Log.e("SyncHistoryFragment", "Log file error", e);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
