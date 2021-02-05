package com.arteriatech.geotrack.rspl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.system.ErrnoException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arteriatech.geotrack.rspl.BuildConfig;
import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.common.Operation;
import com.arteriatech.mutils.common.UIListener;
import com.arteriatech.mutils.common.UtilConstants;
import com.arteriatech.mutils.location.LocationInterface;
import com.arteriatech.mutils.location.LocationModel;
import com.arteriatech.mutils.location.LocationServiceInterface;
import com.arteriatech.mutils.location.LocationUsingGoogleAPI;
import com.arteriatech.mutils.location.LocationUtils;
import com.arteriatech.mutils.log.LogManager;
import com.arteriatech.mutils.registration.RegistrationModel;
import com.arteriatech.geotrack.rspl.SPGeo.database.DatabaseHelperGeo;
import com.arteriatech.geotrack.rspl.SPGeo.database.LocationBean;
import com.arteriatech.geotrack.rspl.SPGeo.services.AlaramRecevier;
import com.arteriatech.geotrack.rspl.SPGeo.services.LocationMonitoringService;
import com.arteriatech.geotrack.rspl.autosync.AutoSyncDataLocationAlarmReceiver;
import com.arteriatech.geotrack.rspl.database.EventDataSqlHelper;
import com.arteriatech.geotrack.rspl.interfaces.SyncHistoryCallBack;
import com.arteriatech.geotrack.rspl.log.ExternalLogViewBean;
import com.arteriatech.geotrack.rspl.offline.OfflineManager;
import com.arteriatech.geotrack.rspl.registration.Configuration;
import com.arteriatech.geotrack.rspl.registration.RegistrationActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.maf.tools.logon.core.LogonCoreContext;
import com.sap.maf.tools.logon.core.LogonCoreException;
import com.sap.mobile.lib.parser.IODataEntry;
import com.sap.mobile.lib.parser.IODataSchema;
import com.sap.mobile.lib.parser.IODataServiceDocument;
import com.sap.mobile.lib.parser.Parser;
import com.sap.smp.client.odata.ODataDuration;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataGuid;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.exception.ODataNetworkException;
import com.sap.smp.client.odata.impl.ODataDurationDefaultImpl;
import com.sap.smp.client.odata.offline.ODataOfflineException;
import com.sap.smp.client.odata.offline.ODataOfflineStore;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.smp.client.odata.store.ODataRequestChangeSet;
import com.sap.smp.client.odata.store.ODataRequestParamBatch;
import com.sap.smp.client.odata.store.ODataRequestParamSingle;
import com.sap.smp.client.odata.store.impl.ODataRequestChangeSetDefaultImpl;
import com.sap.smp.client.odata.store.impl.ODataRequestParamBatchDefaultImpl;
import com.sap.smp.client.odata.store.impl.ODataRequestParamSingleDefaultImpl;
import com.sap.xscript.core.GUID;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HttpsURLConnection;

import static com.arteriatech.geotrack.rspl.offline.OfflineManager.offlineStore;


import static com.arteriatech.geotrack.rspl.ConstantsUtils.alarmPendingIntent;

public class Constants {
    public static final String CLBASE = "CLBASE";
    public static final String INVST = "INVST";
    public static final String INTENT_EXTRA_DEALER_STOCK_BEAN = "dealer_stock_bean";
    public static final String INTENT_EXTRA_MATERIAL_LIST = "material_list";
    public static final String EXTRA_NOTIFICATION = "notificationData";
    public static final String EXTRA_VIEW_ID = "notificationViewId";
    public static final String KEY_FIRST_TIME_RUN = "firstTimeRun";
    public static final String APP_UPGRADE_TYPESET_VALUE = "MSFA";
    public static final Handler handler = new Handler();
    public static final String timer_flag = "timer_flag";
    public static final String KEY_FIRST_TIME_RUN_DashBroad = "firstTimeRunDashBroad";
    public static final int LOCATION_INTERVAL = 60 * 1000;
    public static final int FASTEST_LOCATION_INTERVAL = LOCATION_INTERVAL / 2;
    public static final int MAX_WAIT_TIME = LOCATION_INTERVAL * 5;
    public static final String GeoDate = "GeoDate";
    public static final String GeoTime = "GeoTime";
    public static final String GeoGUID = "GeoGUID";
    /* Odata Queries */
    public static final String GEOENABLE = "GEOENABLE";
    public static final String SPNO = "SPNO";
    public static final String Reason = "Reason";
    public static final String ReasonDesc = "ReasonDesc";
    public static final String BatteryPerc = "BatteryPerc";
    public static final String APKVersion = "APKVersion";
    public static final String APKVersionCode = "APKVersionCode";
    public static final String MobileNo11 = "MobileNo";
    public static final String OsVersion = "OSVersion";
    public static final String MobileModel = "MobileModel";
    public static final String IMEI1 = "IMEI1";
    public static final String IMEI2 = "IMEI2";
    public static final String SPGeos = "SPGeos";
    public static final String EXTRA_CUSTOMER_NO = "customerNo";
    public static final String EXTRA_CUSTOMER_NAME = "customerName";
    public static final String EXTRA_TITLE = "extraTitle";
    public static final String LOG_TABLE = "log";
    public static final String SYNC_TABLE = "SyncTable";
    public static final String PROSPECTED_TABLE = "ProspectedCustomer";
    public static final String OUTSTANDINGAGE_TABLE = "OutstandingAge";
    public static final String SCHEME_TABLE = "Schemes";
    public static final String DEALER_TABLE = "DEALERBEHAVIOUR";
    public static final String ORDER_INFO_TABLE = "ORDERINFO";
    public static final String PRICE_INFO_TABLE = "PRICEINFO";
    public static final String STOCK_INFO_TABLE = "STOCKINFO";
    public static final String POP_INFO_TABLE = "POPINFO";
    public static final String TRADE_INFO_TABLE = "TRADEINFO";
    public static final String TRADE_INFO_CUSTOMER_TECH_TEAM_TABLE = "TRADEINFOCUSTOMERTECHTEAM";
    public static final String DEALER_TARGET_VS_ACHIVEMENT_TABLE = "DealerTargetVsAchivement";
    public static final String SALES_TARGET_VS_ACHIVEMENT_TABLE = "SalesTargetVsAchivement";
    public static final String STATRTEND_TABLE = "StartEnd";
    public static final String STOCKLIST = "StockList";
    public static final String PriceList = "PriceList";
    public static final String FocusedProducts = "FocusedProducts";
    public static final String SegmentedMaterials = "SegmentedMaterials";
    public static final String EXTRA_POS = "extraPOS";
    public static final String EXTRA_SO_CREATE_TITLE = "SO_CREATE";
    public static final String EXTRA_SO_CREDIT_LIMIT = "SO_CREATE_CREDIT_LIMIT";
    public static final String VHELP_MODELID_ENTITY_TYPE = "EntityType eq 'SO'";
    public static final String VHELP_MODELID_ENTITY_TYPE_CHANNELPART = "EntityType eq 'ChannelPartner'";
    public static final String SHADECARDCUST = "ShadeCardCust";
    public static final String CUSTOMERCOMPLAINTS = "CustomerComplaints";
    public static final String DEALERSTOCKENTRY = "DealerStockEntry";
    public static final String DealerStocks = "DealerStocks";
    public static final String DealerStockItemDetails = "DealerStockItemDetails";
    public static final String DEALERSTOCKCONFIGURE = "DealerStockConfigure";
    public static final String RELATIONSHIPCALL = "RelationshipCall";
    public static final String ALLOC_STOCK_LIST = "AllocStockList";
    public static final String CUSTOMERTARGETS = "CustomerTargets";
    public static final String OVERALLSUMMARY = "OverAllSummary";
    public static final String ChequeBounceSummary = "ChequeBounceSummary";
    public static final String CreditNotes = "CreditNotes";
    public static final String PartnerFunctions = "PartnerFunctions";
    public static final String DerivedSecSales = "DerivedSecSales";
    public static final String CreateEditSO = "CreateEditSO";
    public static final String CompStocks = "CompStocks";
    public static final String CompStockItemDetails = "CompStockItemDetails";
    public static final String CompMasters = "CompMasters";
    public static final ArrayList<String> matGrpArrList = new ArrayList<String>();
    public static final String UserProfiles = "UserProfiles";
    public static final String EXTRA_BEAN = "onBean";
    public static final String ForwardindAgents = "ForwardingAgents";
    public static final String ShippingPoints = "ShippingPoints";
    public static final String CustSlsAreas = "CustSlsAreas";
    public static final String CONFIGURATIONS = "Configurations";
    public static final String PlantStorLocs = "PlantStorLocs";
    public static final String processFieldId = "ID";
    public static final String processFieldDesc = "Description";
    public static final String SalesDistrictCode = "SalesDistrict";
    public static final String SalesDistrictDesc = "SalesDistrictDesc";
    public static final String Stocks = "Stocks";
    //------>This id our testing purpose added based on route plan approval all levels(12-08-2015)
    public static final String LOGIN_ID_NAME = "userLevel";
    public static final String PREFS_NAME = "mSFAGeoPreference";
    public static final String AUTH_NAME = "Auth";
    public static final String RequirementDate = "RequirementDate";
    public static final String TransportationPlanDate = "TransportationPlanDate";
    public static final String MaterialAvailDate = "MaterialAvailDate";
    public static final String str_0000 = "0000";
    public static final String str_000000 = "000000";
    public static final String TextCategory = "TextCategory";
    public static final String SSINVOICES = "SSInvoices";
    public static final String SO_ORDER_HEADER = "SalesOrders";
    public static final String COMPETITORSTOCK = "CompetitorStock";
    public static final String CompetitorStocks = "CompetitorStocks";
    public static final String PRICINGLISTTABLE = "PricingList";
    public static final String INCENTIVETRACKINGTABLE = "IncentiveTracking";
    public static final String MaterialRgb = "RegularShades";
    public static final String upcomingShades = "upcomingShades";
    // public static int INDEX_TEMP_NEW[] = null;
    public final static String PROPERTY_APPLICATION_ID = "d:ApplicationConnectionId";
    public static final String SOItemSchedules = "SOItemSchedules";
    public static final String FeedbackList = "FeedbackList";
    public static final String PrimaryDealerStockCreate = "Dealer Stock Create";
    public static final String DealerStockID = "07";
    //Customer
    public static final String KEY_ROLL_ID = "customerRollIdKey";
    public static final String KEY_LOGIN_NAME = "cLoginNameKey";
    /*bundle*/
    public static final String BUNDLE_RESOURCE_PATH = "resourcePath";
    public static final String BUNDLE_OPERATION = "operationBundle";
    public static final String BUNDLE_REQUEST_CODE = "requestCodeBundle";
    public static final String BUNDLE_SESSION_ID = "sessionIdBundle";
    public static final String BUNDLE_SESSION_REQUIRED = "isSessionRequired";
    public static final String BUNDLE_SESSION_URL_REQUIRED = "isSessionTOUrlRequired";
    public static final String BUNDLE_SESSION_TYPE = "sessionTypeBundle";
    public static final String BUNDLE_IS_BATCH_REQUEST = "isBatchRequestBundle";
    public static final String STORE_DATA_INTO_TECHNICAL_CACHE = "storeDataIntoTechnicalCache";
    public static final String BUNDLE_READ_FROM_TECHNICAL_CACHE = "readFromTechnicalCacheBundle";
    public static final String Tasks = "Tasks";
    public static final String MasterCountDBs = "MasterCountDBs";
    public static final String TransactionCountDBs = "TransactionCountDBs";
    public static final String UnloadingPoint = "UnloadingPoint";
    public static final String ReceivingPoint = "ReceivingPoint";
    public static final String CURRENT_VERSION_CODE = "currentVersionCode";
    public static final String INTIALIZEDB = "intializedb";
    //for ID4/DEV
    public static final String OutstandingInvoices = "OutstandingInvoices";
    public static final String OutstandingInvoiceItemDetails = "OutstandingInvoiceItemDetails";
    public static final String OutstandingInvoiceItems = "OutstandingInvoiceItems";

    //    public static  String NavCustNo = "";
//    public static  String NavCPUID = "";
//    public static  String NavComingFrom = "";
//    public static  String NavCustName = "";
//    public static  String NavCPGUID32 = "";
    public static final String Complaints = "Complaints";
    public static final String RouteScheduleSPs = "RouteScheduleSPs";
    public static final String STORE_NAME = "mSFAGeo_Offline";
    public static final String STORE_NAMEGEO = "mSFA_Offline_Geo";
    public static final String backupDBPath = "mSFAGeo_Offline.udb";
    public static final String backuprqDBPath = "mSFAGeo_Offline.rq.udb";
    public static final String CUSTOMERS = "Customers";
    public static final String CustomerNo = "CustomerNo";
    public static final String CustomerPO = "CustomerPO";
    public static final String CustomerPODate = "CustomerPODate";
    public static final String SalesArea = "SalesArea";
    public static final String AmtPastDue = "AmtPastDue";
    public static final String AmtCurrentDue = "AmtCurrentDue";
    public static final String Amt31To60 = "Amt31To60";
    public static final String Amt61To90 = "Amt61To90";
    public static final String Amt91To120 = "Amt91To120";
    public static final String AmtOver120 = "AmtOver120";
    public static final String TradePotential = "TradePotential";
    public static final String NonTradePotential = "NonTradePotential";
    public static final String BgPotential = "BgPotential";
    public static final String TypeOfConstruction = "TypeOfConstruction";
    public static final String StageOfConstruction = "StageOfConstruction";
    public static final String BrandUTCLCheck = "BrandUTCLCheck";
    public static final String BrandACCCheck = "BrandACCCheck";
    public static final String BrandOCLCheck = "BrandOCLCheck";
    public static final String ConfigType = "ConfigType";
    public static final String ActivityConducted = "ActivityConducted";
    public static final String TechnicalDate = "TechnicalDate";
    public static final String City = "City";
    public static final String MobileNumber = "MobileNumber";
    public static final String MailId = "MailId";
    public static final String CustDOB = "CustDOB";
    //	public static final String Anniversary = "Anniversary";
    public static final String SpouseDOB = "SpouseDOB";
    public static final String Child1DOB = "Child1DOB";
    public static final String Child2DOB = "Child2DOB";
    public static final String Child3DOB = "Child3DOB";
    public static final String Child1Name = "Child1Name";
    public static final String Child2Name = "Child2Name";
    public static final String Child3Name = "Child3Name";
    public static final String MaterialGroupID = "MaterialGroupID";
    public static final String MaterialGroupDesc = "MaterialGroupDesc";
    public static final String DbStock = "DbStock";
    public static final String MaterialNo = "MaterialNo";
    public static final String MatGrpDesc = "MatGrpDesc";
    public static final String UspMustSell = "UspMustSell";
    public static final String UspFocused = "UspFocused";
    public static final String UspNew = "UspNew";
    public static final String UspDesc = "UspDesc";
    public static final String MaterialDesc = "MaterialDesc";
    public static final String DepotStock = "DepotStock";
    public static final String BannerDesc = "BannerDesc";
    public static final String MaterialGroup = "MaterialGroup";
    public static final String MaterialGrpDesc = "MaterialGrpDesc";
    public static final String TargetItemGUID = "TargetItemGUID";
    public static final String MatGroupDesc = "MatGroupDesc";
    public static final String ItemCategory = "ItemCategory";
    public static final String DevCollAmount = "DevCollAmount";
    public static final String RouteSchedules = "RouteSchedules";
    public static final String RouteSchedulePlans = "RouteSchedulePlans";
    public static final String TYPE = "Type";
    public static final String VALUE = "Value";
    public static final String DESCRIPTION = "Description";
    public static final String EntityType = "EntityType";
    public static final String IsDefault = "IsDefault";
    public static final String AppntRmnDur = "AppntRmnDur";
    public static final String PropName = "PropName";
    public static final String ID = "ID";
    public static final String CustomerCreditLimits = "CustomerCreditLimits";
    public static final String TRDGRPTYPE = "TRDGRPTYPE";
    public static final String DISPDIST = "DISPDIST";
    public static final String DSPPRCNO0 = "DSPPRCNO0";
    public static final String DSPMATNO = "DSPMATNO";
    public static final String EVLTYP = "EVLTYP";
    public static final String TypeValue = "TypeValue";
    public static final String Typeset = "Typeset";
    public static final String Types = "Types";
    public static final String Typesname = "Typesname";
    public static final String PROP_ATTTYP = "ATTTYP";
    public static final String PROP_ACTTYP = "ACTTYP";
    public static final String PROP_MER_TYPE = "RVWTYP";
    public static final String SSSOs = "SSSOs";
    public static final String SOItemDetails = "SOItemDetails";
    public static final String ConfigTypsetTypeValues = "ConfigTypsetTypeValues";
    public static final String ConfigTypesetTypes = "ConfigTypesetTypes";
    public static final String RoutePlans = "RoutePlans";
    public static final String Bucket1 = "Bucket1";
    public static final String Bucket2 = "Bucket2";
    public static final String Bucket3 = "Bucket3";
    public static final String Bucket4 = "Bucket4";
    public static final String Bucket5 = "Bucket5";
    public static final String Bucket6 = "Bucket6";
    public static final String Bucket7 = "Bucket7";
    public static final String Bucket8 = "Bucket8";
    public static final String Bucket9 = "Bucket9";
    public static final String Bucket10 = "Bucket10";
    public static final String ONETIMESHIPTO = "OneTimeShipTo";
    public static final String SalesOfficeDesc = "SalesOfficeDesc";
    public static final String EXTRA_SESSION_REQUIRED = "isSessionRequired";
    public static final String CHECK_ADD_MATERIAL_ITEM = "checkAddItem";
    public static final String EXTRA_HEADER_BEAN = "onHeaderBean";
    public static final String GRStatusID = "GRStatusID";
    public static final String SalesOff = "SalesOff";
    public static final String CountryCode = "CountryCode";
    public static final String PriDiscAmt = "PriDiscAmt";
    public static final String PriDiscPerc = "PriDiscPerc";
    public static final String PreSalesDocCatDesc = "PreSalesDocCatDesc";
    public static final String ExpiryDate = "ExpiryDate";
    public static final String SalesDistDesc = "SalesDistDesc";
    //SyncHistory
    public static final String SyncHistorysENTITY = ".SyncHistory";
    public static final String ALLOCSTOCKLIST = "AllocStockList";
    public static final String ORG_MONTHS[] = {"Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public static final String NEW_MONTHSCODE[] = {"11", "12", "01", "02",
            "03", "04", "05", "06", "07", "08", "09", "10"};
    public static final String CONFIG_TABLE = "Config2";
    public static final String AUTHORIZATION_TABLE = "Authorizations";
    public static final String SPACE = "%20";
    public static final String ERROR_ARCHIVE_COLLECTION = "ErrorArchive";
    public static final String ERROR_ARCHIVE_ENTRY_REQUEST_METHOD = "RequestMethod";
    public static final String ERROR_ARCHIVE_ENTRY_REQUEST_BODY = "RequestBody";
    public static final String ERROR_ARCHIVE_ENTRY_HTTP_CODE = "HTTPStatusCode";
    public static final String ERROR_ARCHIVE_ENTRY_MESSAGE = "Message";
    public static final String ERROR_ARCHIVE_ENTRY_CUSTOM_TAG = "CustomTag";
    public static final String ERROR_ARCHIVE_ENTRY_REQUEST_URL = "RequestURL";
    public static final String PERSISTEDMETADATA = "metadata";
    public static final String PERSISTEDSERVICEDOC = "servicedoc";
    public static final String PERSISTEDFEEDS = "feeds";
    public static final String TOWN = "TownDistributorList";
    public static final String TRADES = "Trades";
    public static final String FJPLIST = "FJPList";
    public static final String ROUTES = "Routes";
    public static final String OUTLETS = "Outlets";
    public static final String VILLAGELIST = "VillageList";
    public static final String COLLECTIONS = "Collections";
    public static final String COMPETITOR = "CompetitorStocks";
    public static final String COMPETITORITEMS = "CompetitorStockItems";
    public static final String SECONDARYSALES = "SecondarySales";
    public static final String COUNTERSALES = "CounterSales";
    public static final String TERTIARYSALES = "TertiarySales";
    public static final String TERTIARYCOMPETITORS = "TertiaryCompetitors";
    public static final String PRODUCTGROUPS = "ProductGroups";
    public static final String COMPITITORPRODUCTGROUP = "CompProductGrps";
    public static final String RECEIPT_TABLE = "ReceiptTable";
    public static final String SALESORDTYPES = "OrderTypes";
    public static final String SALESAREAS = "SaleAreas";
    public static final String PAYMENTTERMS = "PaymentTerms";
    public static final String CREDITLIMIT = "CreditLimits";
    public static final String CustomerLatLong = "CustomerLatLong";
    public static final String ChangePassword = "ChangePassword";
    public static final String CompetitorMasters = "CompetitorMasters";
    public static final String TEXT_CATEGORY_SET = "TextCategorySet";
    public static final String CONFIGURATION = "Configurations";
    public static final String ValueHelps = "ValueHelps";
    public static final String SALES_ORDER_DELIVERIES = "SalesOrderDeliveries";
    public static final String DELIVERY_STATUS = "DeliveryStatus";
    public static final String PRODUCTPRICES = "ProductPrices";
    public static final String SECONDARYCOMPETITORS = "SecondaryCompetitors";
    public static final String SCHEMESMATERIALS = "SchemeMaterials";
    public static final String POPORDERLISTS = "PopOrderLists";
    public static final String SIGN_BOARD_REQUESTS = "SignBoardRequests";
    public static final String MATERIALS = "Materials";
    public static final String OUTLETFLIST = "OutletF4List";
    public static final String CUSTOMER_MATERIALS = "CustomerMaterials";
    public static final String ACTVITYF4 = "ActivityF4List";
    public static final String CustomerPerformances = "CustomerPerformances";
    public static final String MATERIALLIST = "MaterialList";
    public static final String INVOICE_HEADER = "InvoiceHeaders";
    public static final String INVOICE_ITEM = "InvoiceItems";
    public static final String INVOICEDELIVERIES = "InvoiceDeliveries";
    public static final String ACTIVITY_HDR = "JourneyCycles";
    public static final String REPORTDEALER = "ReportDealerTable";
    public static final String REPORTDEALER_ITEM = "ReportDealeritemTable";
    public static final String REPORTDEALER_COMMENTS = "ReportDealerCommentTable";
    public static final String SO_TEST = "SalesOrders";
    public static final String SO_ITEM_TEST = "Test";
    public static final String MEETINGS = "Meetings";
    public static final String DEALERMEETINGS = "DealerMeetings";
    public static final String POPMATERIALS = "PopMaterials";
    public static final String SOSIMULATELIST = "SoSimulateList";
    public static final String SO_ORDER_SCHEMES = "SalesOrderSchemes";
    public static final String COLL_TARGETS = "CollectionTargets";
    public static final String SALES_TARGETS = "SalesTargets";
    public static final String DLR_OFFTAKE = "DealerOfftakes";
    public static final String DLR_PREFS = "DealerPerfs";
    public static final String CONTACTPERSON = "ContactPersons";
    public static final String CUSTSALESAREAS = "CustSalesAreas";
    public static final String SALESAREAORDTYPES = "SalesAreaOrdTypes";
    public static final String Attendances = "Attendances";
    public static final String Visits = "Visits";
    public static final String ChannelPartners = "ChannelPartners";
    public static final String CPDMSDivisions = "CPDMSDivisions";
    public static final String FinancialPostings = "FinancialPostings";
    public static final String FinancialPostingItemDetails = "FinancialPostingItemDetails";
    public static final String FinancialPostingItems = "FinancialPostingItems";

    //----->ID4 and ID6 HCPMS
   /* public static String server_Text = "mobile-a4597c6af.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "s0012486235", pwd_text = "Sap@0517";*/

//-----> ID6 HCPMS
  /*  public static String server_Text = "mobile-a4597c6af.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "s0012486235", pwd_text = "Sap@0917";*/

    //-----> ID4 HCPMS
 /*   public static String server_Text = "mobile-ab64db6e6.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "s0012486235", pwd_text = "Sap@0917";*/

    //grasim dev ID
 /* public static String server_Text = "mobile-a84ecce64.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "s0012486235", pwd_text = "Sap@0517";*/

    //grasim qua ID
    /*public static String server_Text = "mobile-aa1a539f6.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "s0012486235", pwd_text = "Sap@0517";*/


    //SS Demo Account
//    public static String server_Text = "mobile-ac89cf43a.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
//            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
//            secConfig_Text = "GW", userName_text = "P383751", pwd_text = "Sap@0517";


    //----->Maihar HCPMS DEV
   /* public static String server_Text = "mobile-c810f2bda.ap1.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
    secConfig_Text = "GW", userName_text = "P000003", pwd_text = "Welcome1";*/


    //----->Maihar HCPMS New - DEV
    /*public static String server_Text = "mobile-c33d0a1c5.ap1.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "P000003", pwd_text = "Welcome2";*/


    //----->Maihar HCPMS New - QA
    /*public static String server_Text = "mobile-cadb43466.ap1.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "P000003", pwd_text = "Welcome1";*/

    //----->Maihar HCPMS New - PRD
    /*public static String server_Text = "mobile-c4b62c619.ap1.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
            secConfig_Text = "GW", userName_text = "P000003", pwd_text = "Welcome1";*/


//    ----->Maihar HCPMS QA
//    public static String server_Text = "mobile-cf6081b4c.ap1.hana.ondemand.com", port_Text = "443", cmpnyId_Text = "0",
//            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
//            secConfig_Text = "GW", userName_text = "P000003", pwd_text = "Welcome1";

    // emami dev hcpms
//    public static String server_Text="mobile-ac89cf43a.hana.ondemand.com", port_Text="443", cmpnyId_Text="0",
//			 client_Text="", actCode_Text="", loginUser_Text="",appID_Text = "",
//			 secConfig_Text = "GW",userName_text="p383751",pwd_text="JayaVel@56";

////	 //----->ID4

//    public static String server_Text = "172.25.12.10", port_Text = "8080", cmpnyId_Text = "0",
//            client_Text = "", actCode_Text = "", loginUser_Text = "", appID_Text = "",
//            secConfig_Text = "mSFA_GW1", userName_text = "ss_fos01", pwd_text = "welcome1";

    //----->Emami smp dev
//	public static String server_Text="221.134.108.20", port_Text="8080", cmpnyId_Text="0",
//			client_Text="", actCode_Text="", loginUser_Text="",appID_Text = "",
//			secConfig_Text = "com.arteriatech.mSecSales",userName_text="18194-1",pwd_text="welcome1";

    ////	 //----->ID4 QA

//		 public static String server_Text="172.25.12.10", port_Text="8080", cmpnyId_Text="0",
//			 client_Text="", actCode_Text="", loginUser_Text="",appID_Text = "",
//			 secConfig_Text = "com.arteriatech.mSecSalesQA",userName_text="ss_fos01",pwd_text="welcome1";


    ////	 //----->ID6 QA
//	public static String server_Text="172.25.12.10", port_Text="8080", cmpnyId_Text="0",
//			client_Text="", actCode_Text="", loginUser_Text="",appID_Text = "",
//			secConfig_Text = "com.arteriatech.mSecSalesQA",userName_text="ss_fos01",pwd_text="welcome1";

// --->ID4 Relay server configuration
//	 public static String server_Text="aprins07", port_Text="80", cmpnyId_Text="0",
//			 client_Text="", actCode_Text="", loginUser_Text="",appID_Text = "",
//			 secConfig_Text = "mSecSales",userName_text="900143",pwd_text="welcome1",farm_ID = "ART.Rly.mSFA",
//	 suffix="rs17/client/rs.dll";

    //JK Hcpms Dev
//	public static String server_Text="mobile-cedb1a002.ap1.hana.ondemand.com", port_Text="443", cmpnyId_Text="0",
//			client_Text="", actCode_Text="", loginUser_Text="",appID_Text = "",
//			secConfig_Text = "GW",userName_text="P000001",pwd_text="Welcome_1";

    //for MAF LOGON End

    //App id
    //JK HCPMS
//    public static String APP_ID = "com.arteria.mSFA";

    //ID4
//    public static String APP_ID = "mSFA_GW";
    public static final String RetailerSummarySet = "RetailerSummarySet";
//    ID6 Hcpms
//    public static String APP_ID = "com.arteriatech.mSFAQA";


    //Maihar hcpms Dev/QA
//    public static String APP_ID = "com.arteriatech.mSFA";


//    //emami dev hcpms
//	public static String APP_ID = "com.arteriatech.mSFAEmami";

    // QAS Emami hcpms SS_FOS02
//	public static String APP_ID = "com.arteriatech.mSFAQA";

    // QAS Emami hcpms SS_FOS01
//	public static String APP_ID = "com.arteriatech.mSFAQA1";


//	public static String APP_ID = "com.arteria.mSecSales";

    //ID6-QA
//	public static String APP_ID = "com.arteriatech.mSecSalesQA";

    //HCPMS SS_FOS02
//	public static String APP_ID = "com.arteriatech.mSFAQA2";

//	public static String APP_ID = "com.arteria.secSales";

    //----->Emami smp dev
//	public static String APP_ID = "com.arteriatech.mSecSales";


    //Test for himatsingka
//    public static String server_Text="mobile-cd4e24a7c.ap1.hana.ondemand.com", port_Text="443", cmpnyId_Text="0",
//            client_Text="", actCode_Text="", loginUser_Text="",appID_Text = "",
//            secConfig_Text = "GW",userName_text="P000107",pwd_text="Welcome1";
//    public static String APP_ID = "com.himatsingka.mAuditConnect";
    public static final String SPChannelEvaluationList = "MSPChannelEvaluationList";
    public static final String RequestID = "RequestID";
    public static final String RepeatabilityCreation = "RepeatabilityCreation";
    public static final String AttributeTypesetTypes = "AttributeTypesetTypes";

    //for ID6/QA
//	public static final String OutstandingInvoices = "Invoices";
//	public static final String OutstandingInvoiceItemDetails = "SSInvoiceItemDetails";
//	public static final String OutstandingInvoiceItems = "SSInvoiceItems";
    public static final String SFINVOICES = "Invoices";
    public static final String SSInvoiceItemDetails = "SSInvoiceItemDetails";
    public static final String SSInvoiceItemSerials = "SSInvoiceItemSerialNos";
    public static final String CompetitorInfos = "CompetitorInfos";
    public static final String SPStockItemDetails = "SPStockItemDetails";
    public static final String SPStockItemSNos = "SPStockItemSNos";
    public static final String SPStockItems = "SPStockItems";
    public static final String UserProfileAuthSet = "UserProfileAuthSet";
    public static final String Performances = "MPerformances";
    public static final String RetailerActivationStatusSet = "RetailerActivationStatusSet";
    public static final String CEFStatusID = "CEFStatusID";
    public static final String Status111BID = "Status111BID";
    public static final String Status222ID = "Status222ID";
    public static final String SubsMSIDN = "SubsMSIDN";
    public static final String Targets = "Targets";
    public static final String KPISet = "KPISet";
    public static final String KPIItems = "KPIItems";
    public static final String TargetItems = "TargetItems";
    public static final String Month = "Month";
    public static final String Year = "Year";
    public static final String Period = "Period";
    public static final String KPIGUID = "KPIGUID";
    public static final String KPICode = "KPICode";
    public static final String KPIName = "Name";
    public static final String TargetQty = "TargetQty";
    public static final String ActualQty = "ActualQty";
    public static final String TargetValue = "TargetValue";
    public static final String ActualValue = "ActualValue";
    public static final String ActaulValue = "ActaulValue";
    public static final String TargetGUID = "TargetGUID";
    public static final String CalculationBase = "CalculationBase";
    public static final String CalculationSource = "CalculationSource";
    public static final String KPIFor = "KPIFor";
    public static final String RollUpTo = "RollUpTo";
    public static final String RollupStatus = "RollupStatus";
    public static final String RollupStatusDesc = "RollupStatusDesc";
    public static final String KPICategory = "KPICategory";
    public static final String Periodicity = "Periodicity";
    public static final String PeriodicityDesc = "PeriodicityDesc";
    public static final String CEFStatusDesc = "CEFStatusDesc";
    public static final String Status111BDesc = "Status111BDesc";
    public static final String Status222Desc = "Status222Desc";
    public static final String PartnerGUID = "PartnerGUID";
    public static final String PartnerNo = "PartnerNo";
    public static final String PartnerName = "PartnerName";
    public static final String Refersh = "Attendances,Invoices,SSInvoiceItemDetails,FinancialPostings,FinancialPostingItemDetails";
    public static final String CPStockItemDetails = "CPStockItemDetails";
    public static final String CPStockItemSnos = "CPStockItemSnos";
    public static final String CPStockItems = "CPStockItems";
    public static final String DAYTARGETS = "DayTarget";
    public static final String MONTHTARGETS = "MonthlyTarget";
    public static final String CHEQUESUMMARY = "ChequeSummary";
    public static final String PAINTERVISIT = "PainterVisit";
    public static final String QUARTERTARGETS = "QuarterlyTarget";
    public static final String DEALERWISETARGETS = "DealerWiseTarget";
    public static final String DEALERWISETARGETSVALUE = "DealerWiseTargetValue";
    public static final String STOCKOVERVIEWS = "StockOverviews";
    public static final String AUTHORIZATIONS = "Authorizations";
    public static final String DEALERREQUEST = "DealerRequests";
    public static final String CITYCODES = "CityCodes";
    public static final String SALESRETURNINVOICES = "SalesReturnInvoices";
    public static final String SALESCOLLECTIONDATA = "SaleCollectionDatas";
    public static final String STOCKVALUEDATA = "StockValueDatas";
    public static final String PRODUCTDESKDATA = "PrdDeskDatas";
    public static final String ACTIVITYS = "Activities";
    public static final String OUTLETTYPES = "OutletTypes";
    public static final String OUTLETCATEGORIES = "OutletCategories";
    public static final String OUTLETCLASSES = "OutletClasses";
    public static final String BATCHBLOCKLIST = "BatchBlockList";
    public static final String EXCLUDEDMATERIALLIST = "ExcludedMaterialList";
    public static final String FOCUSPRODUCTLIST = "FocusProductList";
    public static final String VISITLIST = "VisitList";
    public static final String FOCUSPRODREASONLIST = "FocusProdReasons";
    public static final String MATERIALSTOCK = "MaterialStocks";
    public static final String RECEIPT = "Receipt";
    public static final String VISITTYPECONFIG = "VisitTypeConfig";
    public static final String CREDITLIMITTABLE = "CustomerCreditLimits";
    public static final String MerchandisingReviews = "MerchandisingReviews";
    public static final String RETILERIMGTABLE = "RetailerImgTable";
    public static final String AGEINGREPORT = "AgeingReport";
    public static final String CREDITNOTE = "CreditNote";
    public static final String Leads = "Leads";
    public static final String Surveys = "Surveys";
    public static final String SurveyQuestions = "SurveyQuestions";
    public static final String SurveyQuestionOptions = "SurveyQuestionOptions";
    public static final String MATERIALSTOCKQTY = "MaterialStock";
    public static final String DEALERWISESECSALES = "DealerWiseSecSales";
    public static final String COMPLAINTSTRACKING = "ComplaintsTracking";
    public static final String SALESORDER = "SOs";
    public static final String SALESORDERITEMS = "SalesOrderItems";
    public static final String SALESORDERITEMSDETAILS = "SalesOrderItemDetails";
    public static final String BrandPerforms = "BrandPerforms";
    public static final String Trends = "Trends";
    public static final String InvoiceItemDetails = "InvoiceItemDetails";
    public static final String PlantStocks = "PlantStocks";
    public static final String UserSalesPersons = "UserSalesPersons";
    public static final String CustomerComplaints = "CustomerComplaints";
    public static final String Feedbacks = "Feedbacks";
    public static final String FeedbackItemDetails = "FeedbackItemDetails";
    public static final String MerchReviews = "MerchReviews";
    public static final String MerchReviewImages = "MerchReviewImages";
    public static final String MerchReviewsAssociativeType = "MerchReview_MerchReviewImage";
    public static final String VisitSurveys = "VisitSurveys";
    public static final String VisitSurveyResults = "VisitSurveyResults";
    public static final String CollectionLists = "CollectionLists";
    public static final String Schemes = "Schemes";
    public static final String Tariffs = "Tariffs";
    public static final String ExpenseEntryItemDetails = "ExpenseEntryItemDetails";
    public static final String ExpenseEntryImages = "ExpenseEntryImages";
    public static final String LeadItemDetails = "LeadItemDetails";
    public static final String ShadeCards = "ShadeCards";
    public static final String TargetMatGrpCustomers = "TargetMatGrpCustomers";
    public static final String ShadeCardFeedbacks = "ShadeCardFeedbacks";
    public static final String DishonourChqs = "DishonourChqs";
    public static final String DepotTargets = "DepotTargets";
    public static final String DishonourChqItemDetails = "DishonourChqItemDetails";
    public static final String DishonourCheques = "DishonourCheques";
    public static final String SalesOrderSummary = "SalesOrderSummary";
    public static final String CollectionAmtSummary = "CollectionAmtSummary";
    public static final String InvoiceSummary = "InvoiceSummary";
    public static final String VisitActivities = "VisitActivities";
    public static final String ActivitySummarys = "ActivitySummarys";
    public static final String SalesHierarchies = "SalesHierarchies";
    public static final String PasswordChanges = "PasswordChanges";
    public static final String arteria_session_header = "x-arteria-loginid";
    public static final int DATE_DIALOG_ID = 0;
    //Anns : Constants
    public static final String ErrorInParser = "Error in initializing the parser!";
    public static final String ODATA_METADATA_COMMAND = "$metadata";
    public static final String ATOM_CONTENT_TYPE = "application/atom+xml";
    public static final String HTTP_CONTENT_TYPE = "content-type";
    public static final String ODATA_TOP_FILTER = "$top=";
    public static final String ODATA_FILTER = "$filter=";
    public static final String RequestFlushResponse = "requestFlushResponse - status code ";
    public static final String OfflineStoreRequestFailed = "offlineStoreRequestFailed";
    public static final String PostedSuccessfully = "posted successfully";
    public static final String SynchronizationCompletedSuccessfully = "Synchronization completed successfully";
    public static final String OfflineStoreFlushStarted = "offlineStoreFlushStarted";
    public static final String OfflineStoreFlushFinished = "offlineStoreFlushFinished";
    public static final String OfflineStoreFlushSucceeded = "offlineStoreFlushSucceeded";
    public static final String OfflineStoreFlushFailed = "offlineStoreFlushFailed";
    public static final String FlushListenerNotifyError = "FlushListener::notifyError";
    public static final String OfflineStoreRefreshStarted = "OfflineStoreRefreshStarted";
    public static final String OfflineStoreRefreshSucceeded = "OfflineStoreRefreshSucceeded";
    public static final String OfflineStoreRefreshFailed = "OfflineStoreRefreshFailed";
    public static final String ALL = "ALL";
    public static final String MerchandisingSnapshot = "Merchandising Snapshot";
    public static final String RequestCacheResponse = "requestCacheResponse";
    public static final String RequestFailed = "requestFailed";
    public static final String Status_message = "status message";
    public static final String Status_code = "status code";
    public static final String RequestFinished = "requestFinished";
    public static final String RequestServerResponse = "requestServerResponse";
    public static final String BeforeReadRequestServerResponse = "Before Read requestServerResponse";
    public static final String BeforeReadentity = "Before Read entity";
    public static final String AfterReadentity = "After Read entity";
    public static final String RequestStarted = "requestStarted";
    public static final String OfflineRequestListenerNotifyError = "OfflineRequestListener::notifyError";
    public static final String ErrorWhileRequest = "Error while request";
    public static final String TimeStamp = "TimeStamp";
    public static final String Error = "Error";
    public static final String SyncTableHistory = "Sync table(History)";
    public static final String CollList = "CollList";
    public static final String SyncOnRequestSuccess = "Sync::onRequestSuccess";
    public static final String SubmittingDeviceCollectionsPleaseWait = "Submitting device collections, please wait";
    public static final String ORDER_TYPE = "ORDERTYPE";
    public static final String ORDER_TYPE_DESC = "ORDERTYPE_DESC";
    public static final String SALESAREA = "SALESAREA";
    public static final String SALESAREA_DESC = "SALESAREADESC";
    public static final String SOLDTO = "SOLDTO";
    public static final String SOLDTONAME = "SOLDTONAME";
    public static final String SHIPPINTPOINT = "SHIPPINTPOINT";
    public static final String SHIPPINTPOINTDESC = "SHIPPINTPOINTDESC";
    public static final String SHIPTO = "SHIPTO";
    public static final String SHIPTONAME = "SHIPTONAME";
    public static final String FORWARDINGAGENT = "FORWARDINGAGENT";
    public static final String FORWARDINGAGENTNAME = "FORWARDINGAGENTNAME";
    public static final String PLANT = "PLANT";
    public static final String PLANTDESC = "PLANTDSEC";
    public static final String INCOTERM1 = "INCOTERM1";
    public static final String INCOTERM1DESC = "INCOTERM1DESC";
    public static final String INCOTERM2 = "INCOTERM2";
    public static final String SALESDISTRICT = "SALESDISTRICT";
    public static final String SALESDISTRICTDESC = "SALESDISTRICTDESC";
    public static final String ROUTE = "ROUTE";
    public static final String ROUTEDESC = "ROUTEDESC";
    public static final String MEANSOFTRANSPORT = "MEANSOFTRANSPORT";
    public static final String MEANSOFTRANSPORTDESC = "MEANSOFTRANSPORTDESC";
    public static final String STORAGELOC = "STORAGELOC";
    public static final String CUSTOMERPO = "CUSTOMERPO";
    public static final String CUSTOMERPODATE = "CUSTOMERPODATE";
    public static final String Collection = "Collection";
    public static final String Merchendising_Snap = "Merchendising Snapshot";
    public static final String IMGTYPE = "JPEG";
    public static final String OfflineStoreOpenFailed = "offlineStoreOpenFailed";
    public static final String OfflineStoreOpenedFailed = "Offline store opened failed";
    public static final String OfflineStoreStateChanged = "offlineStoreStateChanged";
    public static final String OfflineStoreOpenFinished = "offlineStoreOpenFinished";
    public static final String Requestsuccess_status_message_key = "requestsuccess - status message key";
    public static final String RequestFailed_status_message = "requestFailed - status message ";
    public static final String RequestServerResponseStatusCode = "requestServerResponse - status code";
    public static final String FeedbackCreated = "Feedback created";
    public static final String RequestsuccessStatusMessageBeforeSuccess = "requestsuccess - status message before success";
    public static final String OnlineRequestListenerNotifyError = "OnlineRequestListener::notifyError";
    public static final String HTTP_HEADER_SUP_APPCID = "X-SUP-APPCID";
    public static final String HTTP_HEADER_SMP_APPCID = "X-SMP-APPCID";
    public static final String[][] billAges = {{"00", "01", "02", "03", "04"}, {"All", "0 - 30 Days", "31 - 60 Days", "61 - 90 Days", "> 90 Days"}};
    public static final String SalesPersonName = "SalesPersonName";
    public static final String DeviceCollectionsText = "Device Collections";
    public static final String ItemsText = "ITEMS";
    public static final String H = "H";
    public static final String All = "All";
    public static final String Invoices = "Invoices";
    public static final String MatCode = "MatCode";
    public static final String MatDesc = "MatDesc";
    public static final String Qty = "Qty";
    public static final String SSInvoice = "SSInvoice";
    public static final String InvList = "InvList";
    public static final String SnapshotList = "Snapshot List";
    public static final String plain_text = "plain/text";
    public static final String send_email = "Send your email in:";
    public static final String error_txt = "Error :";
    public static final String LOCATION_LOG = "Location : ";
    public static final String whatsapp_packagename = "com.whatsapp";
    public static final String whatsapp_conv_packagename = "com.whatsapp.Conversation";
    public static final String whatsapp_domainname = "@s.whatsapp.net";
    public static final String jid = "jid";
    public static final String sms_txt = "sms:";
    public static final String tel_txt = "tel:";
    public static final String[] beatsArray = {"All"};
    public static final String AdhocList = "AdhocList";
    public static final String comingFrom = "ComingFrom";
    public static final String red_hex_color_code = "#D32F2F";
    public static final String salesPersonName = "SalesPersonName";
    public static final String salesPersonMobileNo = "SalesPersonMobileNo";
    public static final String statusID_03 = "03";
    public static final String dtFormat_ddMMyyyywithslash = "dd/MM/yyyy";
    public static final String X = "X";
    public static final String offlineStoreRequestFailed = "offlineStoreRequestFailed";
    public static final String isPasswordSaved = "isPasswordSaved";
    public static final String isDeviceRegistered = "isDeviceRegistered";
    public static final String appEndPoint_Key = "appEndPoint";
    public static final String pushEndPoint_Key = "pushEndPoint";
    public static final String RetDetails = "RetDetails";
    public static final String RetailerList = "RetailerList";
    public static final String Retailer = "Retailer";
    public static final String NAVFROM = "NAVFROM";
    public static final String getSyncHistory = "getSyncHistory: ";
    public static final String time_stamp = "Time Stamp";
    public static final String[] syncMenu = {"All", "Download", "Upload", "Sync History"};
    public static final String isLocalFilterQry = "?$filter= sap.islocal() ";
    public static final String device_reg_failed_txt = "Device registration failed";
    public static final String SHOWNOTIFICATION = "SHOWNOTIFICATION";
    public static final String timeStamp = "TimeStamp";
    public static final String sync_table_history_txt = "Sync table(History)";
    public static final String ITEM_TXT = "ITEMS";
    public static final String SecondarySOCreate = "Secondary SO Create";
    public static final String PrimarySOCreate = "Primary SO Create";
    public static final String SOItems = "SOItems";
    public static final String arteria_dayfilter = "x-arteria-daysfilter";
    public static final String RouteType = "RouteType";
    public static final String BeatPlan = "BeatPlan";
    public static final String NonFieldWork = "NonFieldWork";
    public static final String sync_req_sucess_txt = "Sync::onRequestSuccess";
    public static final String collection = "Collection";
    public static final String entityType = "EntityType";
    public static final String savePass = "savePass";
    public static final String offlineDBPath = "/data/com.arteriatech.geotrack.rspl/files/mSFAGeo_Offline.udb";
    public static final String offlineReqDBPath = "/data/com.arteriatech.geotrack.rspl/files/mSFAGeo_Offline.rq.udb";
    public static final String isFirstTimeReg = "isFirstTimeReg";
    public static final String isFirstRegistration = "isFirstRegistration";
    public static final String isReIntilizeDB = "isReIntilizeDB";
    public static final String[] todayIconArray = {"Start", "Beat Plan", "My Targets",
            "Schemes", "Depot Stock", "Day Summary",
            "Expense Entry",
            "Visual Aid", "Adhoc Visit", "Alerts", "Expense Entry", "Expense List",/*"Schemes", "Price Update","Dealerwise Target","My Targets"
           ,"Oustanding Summary","Dealer Behaviour",*/"SO Approval", "Product Pricing", "Plant Stock", "MTP"};
    public static final String[] reportIconArray = {"Customers", "Prospective Customer List", "Appointment"};
    public static final String[] admintIconArray = {"Sync", "Log", ""};
    public static final String BeatType = "BeatType";
    public static final String RouteList = "RouteList";
    public static final String OtherRouteList = "OtherRouteList";
    public static final String VisitType = "VisitType";
    public static final String OtherRouteGUID = "OtherRouteGUID";
    public static final String OtherRouteName = "OtherRouteName";
    public static final String VisitCatID = "VisitCatID";
    public static final String AdhocVisitCatID = "02";
    public static final String BeatVisitCatID = "01";
    public static final String OtherBeatVisitCatID = "02";
    public static final String CustomerList = "CustomerList";
    public static final String ProspectiveCustomerList = "ProspectiveCustomerList";

    //	public static String ParentTypeID = "ParentTypeID";
    public static final String Address = "Address";
    public static final String Visit = "Visit";
    public static final String Reports = "Reports";
    public static final String Summary = "Summary";
    public static final String default_txt = "default";
    public static final String logon_finished_appcid = "onLogonFinished: appcid:";
    public static final String logon_finished_aendpointurl = "onLogonFinished: endpointurl:";
    public static final String isFromNotification = "isFromNotification";
    public static final String username = "username";
    public static final String usernameExtra = "usernameExtra";
    public static final String VisitSeqId = "VisitSeqId";
    public static final String RouteBased = "RouteBased";
    public static final String full_Day = "Full Day";
    public static final String first_half = "1st Half";
    public static final String second_half = "2nd Half";
    public static final String[][] arrWorkType = {{"01", "02"}, {"Full Day", "Split"}};
    public static final String DeviceStatus = "DeviceStatus";
    public static final String InvDate = "InvDate";
    public static final String InvAmount = "InvAmount";
    public static final String DeviceNo = "DeviceNo";
    public static final String RetailerNo = "RetailerNo";
    public static final String FFDA33 = "#FFDA33";
    public static final String EntitySet = "EntitySet";
    public static final String T = "T";
    public static final String offline_store_not_closed = "Offline store not closed: ";
    public static final String invalid_payload_entityset_expected = "Invalid payload:EntitySet expected but got ";
    public static final String None = "None";
    public static final String str_00 = "00";
    public static final String str_01 = "01";
    public static final String str_04 = "04";
    public static final String str_false = "false";
    public static final String str_0 = "0";
    public static final String error_txt1 = "Error";
    public static final String error_archive_called_txt = "Error Arcive is called";
    public static final String error = "error";
    public static final String message = "message";
    public static final String CollectionHeaderTable = "CollectionHeaderTable";
    public static final String value = "value";
    public static final String error_during_offline_close = "Error during store close: ";
    public static final String icurrentUDBPath = "/data/com.arteriatech.geotrack.rspl/files/mSFAGeo_Offline.udb";
    public static final String ibackupUDBPath = "mSFAGeo_Offline.udb";
    public static final String icurrentRqDBPath = "/data/com.arteriatech.geotrack.rspl/files/mSFAGeo_Offline.rq.udb";
    public static final String ibackupRqDBPath = "mSFAGeo_Offline.rq.udb";
    public static final String icurrentDBPath = "/data/com.arteriatech.geotrack.rspl/files/mSFAGeo_Offline.rq.udb";
    public static final String ibackupDBPath = "mSFAGeo_Offline.rq.udb";
    public static final String error_creating_sync_db = "Registration:createSyncDatabase Error while creating sync database";
    public static final String error_in_collection = "Error in Collection :";
    public static final String RetName = "RetName";
    public static final String RetID = "RetID";
    public static final String delete_from = "DELETE FROM ";
    public static final String create_table = "create table IF NOT EXISTS ";
    public static final String EventsData = "EventsData";
    public static final String on_Create = "onCreate:";
    public static final String RTGS = "RTGS";
    public static final String NEFT = "NEFT";
    public static final String DD = "DD";
    public static final String Cheque = "Cheque";
    public static final String Margin = "Margin";
    public static final String WholeSalesLandingPrice = "WholeSalesLandingPrice";
    public static final String ConsumerOffer = "ConsumerOffer";
    public static final String TradeOffer = "TradeOffer";
    public static final String ShelfLife = "ShelfLife";
    public static final String SOList = "SOList";
    public static final String CustomerComplaintsCreate = "Consumer Complaints Create";
    public static final String DeviceMechindising = "DeviceMechindising";
    public static final String NonDeviceMechindising = "NonDeviceMechindising";
    public static final String MerchList = "MerchList";
    public static final String VendorNo = "VendorNo";
    public static final String PersonnelNo = "PersonnelNo";
    public static final String VendorName = "VendorName";
    public static final String PersonnelName = "PersonnelName";
    public static final String CustomerNumber = "CustomerNumber";
    public static final String CustomerName = "CustomerName";
    public static final String Street = "Street";
    public static final String Email = "Email";
    public static final String Telephone1 = "Telephone1";
    public static final String Telephone2 = "Telephone2";
    public static final String Feature = "Feature";
    public static final String DelvNo = "DelvNo";
    public static final String StoNo = "StoNo";
    public static final String DeliveryDate = "DeliveryDate";
    public static final String IssueingPlant = "IssueingPlant";
    public static final String Value = "Value";
    public static final String Type = "Type";
    public static final String CustomerAccount = "CustomerAccount";
    public static final String DocumentNbr = "DocumentNbr";
    public static final String PostingDate = "PostingDate";
    public static final String SalesOrdNo = "SalesOrdNo";
    public static final String DocDate = "DocDate";
    public static final String PlantName = "PlantName";
    public static final String MaterialCode = "MaterialCode";
    public static final String Unrestricted = "Unrestricted";
    public static final String PlantID = "PlantID";
    public static final String StorageLoc = "StorageLoc";
    public static final String StorageLocDesc = "StorageLocDesc";
    public static final String OrderTypeText = "OrderTypeText";
    public static final String SoldToNo = "SoldToNo";
    public static final String ShipToNo = "ShipToNo";
    public static final String SalesOrg = "SalesOrg";
    public static final String DistChannel = "DistChannel";
    public static final String Division = "Division";
    public static final String IncoTerms1Text = "IncoTerms1Text";
    public static final String CustomerPo = "CustomerPo";
    public static final String SalesItemNo = "SalesItemNo";
    public static final String MaterialText = "MaterialText";
    public static final String DelvQty = "DelvQty";
    public static final String UnitOfMeasure = "UnitOfMeasure";
    public static final String DeliveryNo = "DeliveryNo";
    public static final String DocumentDate = "DocumentDate";
    public static final String ShipPoint = "ShipPoint";
    public static final String IssueQuantity = "IssueQuantity";
    public static final String WarehouseNo = "WarehouseNo";
    public static final String SalesOrderNo = "SalesOrderNo";
    public static final String ActualGiDate = "ActualGiDate";
    public static final String WarehouseNoTxt = "WarehouseNoTxt";
    public static final String CurrencyKey = "CurrencyKey";
    public static final String DelvItem = "DelvItem";
    public static final String StoItem = "StoItem";
    public static final String BalanceQty = "BalanceQty";
    public static final String Customer = "Customer";
    public static final String PaymentTerm = "PaymentTerm";
    public static final String PaymentTermDesc = "PaymentTermDesc";
    public static final String PaymentTermCode = "PaymentTermCode";
    public static final String Inco1 = "Inco1";
    public static final String Inco2 = "Inco2";
    public static final String ShippPoint = "ShippPoint";
    public static final String ShipPointDesc = "ShipPointDesc";
    public static final String ShippingPoint = "ShippingPoint";
    public static final String BatchInd = "BatchInd";
    public static final String UnitOfMeasureText = "UnitOfMeasureText";
    public static final String NetValue = "NetValue";
    public static final String StorLocDesc = "StorLocDesc";
    public static final String DelvPlant = "DelvPlant";
    public static final String CustPartnerNo = "CustPartnerNo";
    public static final String GrNo = "GrNo";
    public static final String IssuePlant = "IssuePlant";
    public static final String GrItemNo = "GrItemNo";
    public static final String Material = "Material";
    public static final String ReceivedQty = "ReceivedQty";
    public static final String ReceiptDate = "ReceiptDate";
    public static final String MatCondCat = "MatCondCat";
    public static final String PgiIndicator = "PgiIndicator";
    public static final String UomText = "UomText";
    public static final String SalesOrderItemNo = "SalesOrderItemNo";
    public static final String ActualQuantity = "ActualQuantity";
    public static final String PaymentDescription = "PaymentDescription";
    public static final String CompanyCode = "CompanyCode";
    public static final String StatusUpdate = "StatusUpdate";
    public static final String TaxRate = "TaxRate";
    public static final String Amounts = "Amount";
    public static final String CustomerPoDate = "CustomerPoDate";
    public static final String IncoTerms1 = "IncoTerms1";
    public static final String IncoTerms2 = "IncoTerms2";
    public static final String NetPrice = "NetPrice";
    public static final String BatchNo = "BatchNo";
    public static final String ShelExpDate = "ShelExpDate";
    public static final String ManfDate = "ManfDate";
    public static final String PaymentTermsText = "PaymentTermsText";
    public static final String PaymentTerms = "PaymentTerms";
    public static final String SalesOrders = "SalesOrders";
    public static final String SalesOrderItems = "SalesOrderItems";
    public static final String SalesOrderDataValt = "SalesOrderDataValt";
    public static final String NOTIFICATION_ITEM = "fromNotificationItem";
    public static final String MTPDataValt = "MTPDataValt";
    public static final String EXTRA_COME_FROM = "comeFrom";
    public static final String Plant = "Plant";
    public static final String ShippingTypeID = "ShippingTypeID";
    public static final String Payterm = "Payterm";
    public static final String Incoterm1 = "Incoterm1";
    public static final String Incoterm2 = "Incoterm2";
    public static final String MeansOfTranstyp = "MeansOfTranstyp";
    public static final String MeansOfTranstypDesc = "MeansOfTranstypDesc";
    public static final String SalesGroup = "SalesGroup";
    public static final String StorLoc = "StorLoc";
    public static final String UOMNO0 = "UOMNO0";
    public static final String AccountingDocNumber = "AccountingDocNumber";
    public static final String PartnerCustomerNo = "PartnerCustomerNo";
    public static final String OrderQty = "OrderQty";
    public static final String PartnerFunctionDesc = "PartnerFunctionDesc";
    public static final String RegionID = "RegionID";
    public static final String RegionDesc = "RegionDesc";
    public static final String CountryDesc = "CountryDesc";
    public static final String ECCNo = "ECCNo";
    public static final String CSTNo = "CSTNo";
    public static final String LSTNo = "LSTNo";
    public static final String ExciseRegNo = "ExciseRegNo";
    public static final String ServiceTaxRegNo = "ServiceTaxRegNo";
    public static final String CreditExposure = "CreditExposure";
    public static final String CreditLimitUsed = "CreditLimitUsed";
    public static final String AnnualSales = "AnnualSales";
    public static final String AnnualSalesYear = "AnnualSalesYear";
    public static final String ORDTY = "ORDTY";
    public static final String SPORTY = "SPORTY";
    public static final String PlantDesc = "PlantDesc";
    public static final String PaytermDesc = "PaytermDesc";
    public static final String Incoterm1Desc = "Incoterm1Desc";
    public static final String SalesAreaDesc = "SalesAreaDesc";
    public static final String PartnerFunctionID = "PartnerFunctionID";
    public static final String GSTIN = "GSTIN";
    public static final String ShippingConditionID = "ShippingConditionID";
    public static final String ShippingConditionDesc = "ShippingConditionDesc";
    public static final String DeliveringPlantID = "DeliveringPlantID";
    public static final String DeliveringPlantDesc = "DeliveringPlantDesc";
    public static final String TransportationZoneID = "TransportationZoneID";
    public static final String TransportationZoneDesc = "TransportationZoneDesc";
    public static final String Incoterms1ID = "Incoterms1ID";
    public static final String Incoterms1Desc = "Incoterms1Desc";
    public static final String Incoterms2 = "Incoterms2";
    public static final String PaymentTermID = "PaymentTermID";
    public static final String CreditControlAreaDesc = "CreditControlAreaDesc";
    public static final String CustomerGrpID = "CustomerGrpID";
    public static final String SH = "SH";
    public static final String PartnerTypeID = "PartnerTypeID";
    public static final String PartnerCustomerName = "PartnerCustomerName";
    public static final String Recievables = "Recievables";
    public static final String SpecialLiabilities = "SpecialLiabilities";
    public static final String SalesValue = "SalesValue";
    public static final String CreditLimitUsedPerc = "CreditLimitUsedPerc";
    public static final String dtFormat_ddMMyyyy = "dd/MM/yyyy";
    public static final String CreditControlAreaID = "CreditControlAreaID";
    public static final String CreditControlDesc = "CreditControlDesc";
    public static final String MaterialByCustomers = "MaterialByCustomers";
    public static final int NAVIGATE_TO_PARENT_ACTIVITY = 99;
    public static final int NAVIGATE_TO_CHILD_ACTIVITY = 99;
    public static final String SFSO = "SFSO";
    public static final String TextIDDesc = "TextIDDesc";
    public static final String SOTexts = "SOTexts";
    public static final String ONETIMESHP = "ONETIMESHP";
    public static final String SOS_ENTITY = ".SO";
    public static final String SOS_ITEM_DETAILS_ENTITY = ".SOItemDetail";
    public static final String SOS_ITEM_SCHEDULE_ENTITY = ".SOItemSchedule";
    public static final String SOS_ITEM_CONDITION_ITEM_DETAILS_ENTITY = ".SOConditionItemDetail";
    public static final String SOS_ITEM_CONDITION_ENTITY = ".SOCondition";
    public static final String SOS_SO_TEXT_ENTITY = ".SOText";
    public static final String RE = "RE";
    public static final String LIST = "lists";
    public static final String CUSTOMER_ENTITY = ".Customer";
    public static final String HDRNTTXTID = "HDRNTTXTID";
    public static final String SOS_PARTNER_FUNCTIONS_ENTITY = ".SOPartnerFunction";
    public static final String SOPartnerFunctions = "SOPartnerFunctions";
    public static final String EXTRA_SO_HEADER = "Header";
    public static final String EXTRA_SO_ITEM_LIST = "itemList";
    public static final String EXTRA_Is_Simulated = "isSimulated";
    public static final String ExpenseFreq = "ExpenseFreq";
    public static final String ExpenseDaily = "000010";
    public static final String ExpenseMonthly = "000030";
    public static final String ExpenseType = "ExpenseType";
    public static final String ExpenseTypeDesc = "ExpenseTypeDesc";
    public static final String ExpenseItemType = "ExpenseItemType";
    public static final String ExpenseItemTypeDesc = "ExpenseItemTypeDesc";
    public static final String ExpenseFreqDesc = "ExpenseFreqDesc";
    public static final String ExpenseItemCat = "ExpenseItemCat";
    public static final String ExpenseItemCatDesc = "ExpenseItemCatDesc";
    public static final String DefaultItemCat = "DefaultItemCat";
    public static final String DefaultItemCatDesc = "DefaultItemCatDesc";
    public static final String AmountCategory = "AmountCategory";
    public static final String AmountCategoryDesc = "AmountCategoryDesc";
    public static final String MaxAllowancePer = "MaxAllowancePer";
    public static final String ExpenseQuantityUom = "ExpenseQuantityUom";
    public static final String ItemFieldSet = "ItemFieldSet";
    public static final String ItemFieldSetDesc = "ItemFieldSetDesc";
    public static final String Allowance = "Allowance";
    public static final String IsSupportDocReq = "IsSupportDocReq";
    public static final String IsRemarksReq = "IsRemarksReq";
    public static final String ExpenseGUID = "ExpenseGUID";
    public static final String FiscalYear = "FiscalYear";
    public static final String ExpenseNo = "ExpenseNo";
    public static final String ExpenseDate = "ExpenseDate";
    public static final String ExpenseItemGUID = "ExpenseItemGUID";
    public static final String ExpeseItemNo = "ExpeseItemNo";
    public static final String BeatGUID = "BeatGUID";
    public static final String ConvenyanceMode = "ConvenyanceMode";
    public static final String ConvenyanceModeDs = "ConvenyanceModeDs";
    public static final String Distance = "Distance";
    public static final String BeatDistance = "BeatDistance";
    public static final String ConveyanceAmt = "ConveyanceAmt";
    public static final String ExpenseDocumentID = "ExpenseDocumentID";
    public static final String DocumentTypeID = "DocumentTypeID";
    public static final String DocumentTypeDesc = "DocumentTypeDesc";
    public static final String DocumentStatusID = "DocumentStatusID";
    public static final String DocumentStatusDesc = "DocumentStatusDesc";
    public static final String DocumentMimeType = "DocumentMimeType";
    public static final String DocumentSize = "DocumentSize";
    public static final String ExpenseConfigs = "ExpenseConfigs";
    public static final String ExpenseAllowances = "ExpenseAllowances";
    public static final String isFirstTimeValidation = "isFirstTimeValidation";
    public static final int TAKE_PICTURE = 190;
    public static final String UserCustomers = "UserCustomers";
    public final static String TABLE_NAME = "PriceUpdate"; // name of table
    public final static String Price_ID = "_id";
    public final static String master_brand = "master_brand";
    public final static String brand = "brand";
    public final static String BP_EX = "BP_EX";
    public final static String BP_For = "BP_For";
    public final static String WSP = "WSP";
    public final static String RSP = "RSP";
    public final static String date = "todays_date";
    public static final String str_03 = "03";
    public static final String str_05 = "05";
    public static final String AmtDue = "AmtDue";
    public static final String DocumentNo = "DocumentNo";
    public static final String CollectionTypeID = "CollectionTypeID";
    public static final String PaymentMethodID = "PaymentMethodID";
    public static final String PaymentMethodDesc = "PaymentMethodDesc";
    public static final String CollectionTypeDesc = "CollectionTypeDesc";
    public static final String InvoicedAmount = "InvoicedAmount";
    public static final String CollectedAmount = "CollectedAmount";
    public static final String OpenAmount = "OpenAmount";
    public static final String InvoiceTypeDesc = "InvoiceTypeDesc";
    public static final String InvoiceType = "InvoiceType";
    public static final String InvoiceTypDesc = "InvoiceTypDesc";
    public static final String SOS_SO_TASK_ENTITY = ".Task";
    public static final int PERMISSION_REQUEST_CODE = 110;
    public static final String InvoicePartnerFunctions = "InvoicePartnerFunctions";
    public static final String ConditionCatDesc = "ConditionCatDesc";
    public static final String ConditionCatID = "ConditionCatID";
    public static final String EXTRA_SO_DETAIL = "openSODetails";
    public static final String STORAGELOCDESC = "STORAGELOCDESC";
    public static final String MTPList = "MTPList";
    public static final String Brand = "Brand";
    public static final String TECHNICAL_HEADER_DETAILS = "header_details";
    public static final String EXTRA_COLLECTION_DETAIL = "CollectionDetails";
    /* COllection plan properties*/
    public static final String CollectionPlan = "CollectionPlans";
    public static final String CollectionPlanItemDetails = "CollectionPlanItemDetails";
    public static final String Fiscalyear = "Fiscalyear";
    public static final String RTGSDataValt = "RTGSDataValt";
    public static final int STORAGE_PERMISSION_CONSTANT = 890;
    public static String fromNotificationDetail = "fromNotificationDetail";
    public static AlertDialog alert = null;
    public static Timer timer = null;
    public static TimerTask timerTask = null;
    public static boolean isFlagVisiable = false;
    public static boolean isStoreOpened = false;
    /*SPGeo*/
    /*DashBroad Online Store*/
    public static OnlineODataStore onlineStoreDashBroad = null;
    public static String DashBroad_Error_Msg = "";
    public static String DashBoards = "DashBoards";
    public static Boolean IsOnlineStoreFailedDashBroad = false;
    public static String SPGEOENTITY = ".SPGEO";
    public static String Total_Order_Value_KEY = "Total_Order_Value_KEY";
    public static String Last_Relese_Date = "29-09-2017 21:10";
    public static String About_Version = "3.0.0.1h";
    public static EventDataSqlHelper events;
    public static int SO_LIST_POS = 2;
    public static int SO_LIST_POS_3 = 3;
    public static int SO_LIST_POS_4 = 4;
    public static int SO_LIST_POS_5 = 5;
    public static String EXTRA_SO_BEAN = "extraSOBean";
    public static String EXTRA_FROM_CC = "isFromCC";
    public static String EXTRA_SO_TITLE = "actionBarTitle";
    public static String EXTRA_SO_INSTANCE_ID = "instanceID";
    public static String EXTRA_SO_NO = "extraSONo";
    public static String CUSTOMERNUMBER = "";
    public static String CUSTOMERNAME = "";
    public static boolean isInvoicesCountDone = false;
    public static boolean isInvoicesItemsCountDone = false;
    public static boolean isAuthDone = false;
    public static String ComingFromCreateSenarios = "";
    public static boolean isSync = false;
    public static boolean isBackGroundSync = false;
    public static boolean isPullDownSync = false;
    public static boolean isLocationSync = false;
    public static String CollDate = "CollDate";
    public static String FISDocNo = "FISDocNo";
    public static Boolean isAlertRecordsAvailable = false;
    public static String ForwarAgentCode = "FrwadgAgent";
    public static String ForwarAgentDesc = "FrwadgAgentName";
    public static String USERROLE = "UserRole";
    public static String isRollResponseGot = "isRollResponseGot";
    public static SQLiteDatabase EventUserHandler;
    public static boolean devicelogflag = false;
    public static boolean importdbflag = false;
    public static boolean FlagForUpdate = false;
    public static boolean FlagForSecurConnection = false;
    public static MSFAGEOApplication mApplication = null;
    public static boolean FlagForSyncAllUpdate = false;
    public static boolean FlagErrorLogAllSync = false;
    public static String DATABASE_NAME = "mSFAAIRCEL.db";
    public static String DATABASE_REGISTRATION_TABLE = "registrationtable";
    public static String APPS_NAME = "mSFAAIRCEL";
    public static String AppName_Key = "AppName";
    public static String UserName_Key = "username";
    public static String Customers = "Customers";
    public static String SONo = "SONo";
    public static String LoginID = "LoginID";
    public static String SOs = "SOs";
    public static String TaskHistorys = "TaskHistorys";
    public static String ActionName = "ActionName";
    public static String TaskStatusID = "TaskStatusID";
    public static String PerformedByName = "PerformedByName";
    public static String Timestamp = "Timestamp";
    public static String TotalAmount = "TotalAmount";
    public static String TotalMTPCount = "TotalMTPCount";
    public static String TotalSOCount = "TotalSOCount";
    public static String YES = "YES";
    public static String SegmentId = "SegmentId";
    public static String SegmentDesc = "SegmentDesc";
    public static String BrandsCategories = "BrandsCategories";
    public static String OrderMaterialGroups = "OrderMaterialGroups";
    public static String Brands = "Brands";
    public static String MaterialCategories = "MaterialCategories";
    public static String BrandID = "BrandID";
    public static String Materials = "Materials";
    public static String BrandDesc = "BrandDesc";
    public static String MaterialCategoryID = "MaterialCategoryID";
    public static String MaterialCategoryDesc = "MaterialCategoryDesc";
    public static String DMSDivision = "DMSDivision";
    public static String DMSDivisionDesc = "DMSDivisionDesc";
    public static String Category = "Category";
    public static String CRS_SKU_GROUP = "CRS SKU Group";
    public static String OrderMaterialGroupDesc = "OrderMaterialGroupDesc";
    public static String OrderMaterialGroupID = "OrderMaterialGroupID";
    public static String Others = "Others";
    public static String EncryptKey = "welcome1";
    public static String collections[] = null;
    public static Parser parser = null;
    public static IODataServiceDocument serviceDocument = null;
    public static IODataSchema schema = null;
    public static List<IODataEntry> entries = null;
    public static String Table[] = null;
    public static String clumsName[] = null;
    public static String serviceDoc = null;
    public static String cookies = "";
    public static String metaDoc = null;
    public static String x_csrf_token = "";
    public static String ABOUTVERSION = "3.0";
    public static String ABOUTDATE = "Nov 13,2015, 23:59:00";
    public static int autoduration = 30;
    public static String USERTYPE = "T";
    public static String CollAmount = "";
    public static String SyncTime = "11";
    public static volatile boolean iSAutoSync = false;
    // public static LiteMessagingClient lm = null;
    // public static LiteUserManager lurm = null;
    public static int autoSyncDur = 360;
    public static boolean crashlogflag = false;
    public static double MaterialUnitPrice = 0.0, MaterialNetAmount = 0.0, InvoiceTotalAmount = 0.0, InvoiceUnitPrice = 0.0;
    public static String ReferenceTypeID = "ReferenceTypeID";
    public static String ReferenceTypeDesc = "ReferenceTypeDesc";
    public static String Name = "Name";
    public static String CPUID = "CPUID";
    public static String BankName = "BankName";
    public static String Fresh = "Fresh";
    public static String CRDCTL = "CRDCTL";
    public static boolean BoolMoreThanOneRoute = false;
    public static String SFInvoiceItemDetails = "InvoiceItemDetails";
    public static String ActualInvQty = "ActualInvQty";
    public static String titleHistory = "History";
    public static String titlePending = "Pending Sync";
    public static String Status = "Status";
    public static String ViisitCPNo = "ViisitCPNo";
    public static String STATUS = "Status";
    public static String CustomerPartnerFunctions = "CustomerPartnerFunctions";
    public static String CustomerSalesAreas = "CustomerSalesAreas";
    public static String MaterialSaleAreas = "MaterialSaleAreas";
    public static String SOConditionItemDetails = "SOConditionItemDetails";
    public static String DelvStatus = "DelvStatus";
    public static String DelvStatusId = "DelvStatusID";
    public static String TypesName = "TypesName";
    public static String DELVST = "DELVST";
    public static String RejReason = "RejReason";
    public static String RejReasonDesc = "RejReasonDesc";
    public static String SOUpdate = "SOUpdate";
    public static String SaleOffDesc = "SaleOffDesc";
    public static String DelvStatusID = "DelvStatusID";
    public static String DelvStatusDesc = "DelvStatusDesc";
    public static String DiscountPer = "DiscountPer";
    public static String OpenQty = "OpenQty";
    public static String OwnStock = "OwnStock";
    public static String ItemCatDesc = "ItemCatDesc";
    public static String SaleGrpDesc = "SaleGrpDesc";
    public static String REJRSN = "REJRSN";
    public static String SOChange = "SOChange";
    public static String comingFromChange = "comingFromChange";
    public static String comingFromVal = "";
    public static String SOCancel = "SOCancel";
    public static String isSOChangeEnabled = "isSOChangeEnabled";
    public static String isSOCancelEnabled = "isSOCancelEnabled";
    public static String isRetailerListEnabled = "isRetailerListEnabled";
    public static String isSOWithSingleItemEnabled = "isSOWithSingleItemEnabled";
    public static String FeedbackSubTypeID = "FeedbackSubTypeID";
    public static String FeedbackSubTypeDesc = "FeedbackSubTypeDesc";
    public static String FeedbackSubType = "FeedbackSubType";
    public static String FeedbackID = "03";
    public static String Feedback = "Feedback";
    public static String FeedBackGuid = "FeedBackGuid";
    public static String FeedbackDesc = "FeedbackDesc";
    public static String NotPurchasedType = "000004";
    public static String AsOnDate = "AsOnDate";
    public static String InvoiceQty = "InvoiceQty";
    public static String PaymentStatus = "PaymentStatus";
    public static String ROLL_ID_CSTMR = "000003";
    public static String InstanceID = "InstanceID";
    public static String EntityKeyID = "EntityKeyID";
    public static String EntityKey = "EntityKey";
    public static String EntityDate1 = "EntityDate1";
    public static String EntityKeyDesc = "EntityKeyDesc";
    public static String EntityValue1 = "EntityValue1";
    public static String EntityCurrency = "EntityCurrency";
    public static String PriorityNumber = "PriorityNumber";
    public static String EntityAttribute1 = "EntityAttribute1";
    public static String EntityAttribute5 = "EntityAttribute5";
    public static String EntityAttribute6 = "EntityAttribute6";
    public static String EntityAttribute7 = "EntityAttribute7";
    public static String PasswordExpiredMsg = "User is locked or password expired. Click on Change to Password change in Settings or Please Contact Channel team";
    public static String IDPACCNAME = "IDPACCNAME";
    public static String DOMAINNAME = "DOMAINNAME";
    public static String SPID = "SPID";
    public static String SITENAME = "SITENAME";
    public static String TRGURL = "TRGURL";
    public static String SRCURL = "SRCURL";
    public static String FGTURL = "FGTURL";
    public static String RejectedStatusID = "04";
    public static int NewDefingRequestVersion = 29;
    public static int IntializeDBVersion = 3;
    public static HashMap<String, String> httpErrorCodes = new HashMap<String, String>();
    //for MAF LOGON Start
    public static String appConID_Text = "", appEndPoint_Text = "",
            pushEndPoint_Text;
    //ID4 HCPMS
    //public static String APP_ID = "com.arteriatech.mSFADev";
    public static String APP_ID = "com.arteriatech.mSFA";
    //ID4`
    public static String FeedbackEntity = ".Feedback";
    public static String VISITACTIVITYENTITY = ".VisitActivity";
    public static String FeedbackItemDetailEntity = ".FeedbackItemDetail";
    public static String VISITENTITY = ".Visit";
    public static String CUSTOMERENTITY = ".Customer";
    public static String ATTENDANCEENTITY = ".Attendance";
    public static String MERCHINDISINGENTITY = ".MerchReview";
    public static String MERCHINDISINGITEMENTITY = ".MerchReviewImage";
    public static String ChannelPartnerEntity = ".ChannelPartner";
    public static String InvoiceEntity = ".SSInvoice";
    public static String InvoiceItemEntity = ".SSInvoiceItemDetail";
    public static String InvoiceSerialNoEntity = ".SSInvoiceItemSerialNo";
    public static String FinancialPostingsEntity = ".FinancialPosting";
    public static String FinancialPostingsItemEntity = ".FinancialPostingItemDetail";
    public static String CompetitorInfoEntity = ".CompetitorInfo";
    public static String SPStockSNosEntity = ".SPStockItemSNo";
    public static String CPStockItemEntity = ".CPStockItem";
    public static String ComplaintEntity = ".Complaint";
    public static String STOCK_ENTITY = ".Stock";
    public static String RouteScheduleEntity = ".RouteSchedule";
    public static String RouteSchedulePlanEntity = ".RouteSchedulePlan";
    public static String RouteScheduleSPEntity = ".RouteScheduleSP";
    public static String BaseUOM = "BaseUOM";
    public static String CustomerStock = "CustomerStock";
    public static String NO_OF_DAYS = "0";
    public static String SalesPersons = "SalesPersons";
    public static String CollectionEntity = ".Collection";
    public static String CollectionItemEntity = ".CollectionItemDetail";
    public static String ProdCatg = "ProdCatg";
    public static String ProdCatgDesc = "ProdCatgDesc";
    public static String SkuGroup = "SkuGroup";
    public static String SkuGroupDesc = "SkuGroupDesc";
    public static String Banner = "Banner";
    public static String InvoiceNo = "InvoiceNo";
    public static String InvoiceTypeID = "InvoiceTypeID";
    public static String UnitPrice = "UnitPrice";
    public static String NetAmount = "NetAmount";
    public static String CollectionAmount = "CollectionAmount";
    public static String ShipToName = "ShipToName";
    public static String ShipTo = "ShipTo";
    public static String ReferenceNo = "ReferenceNo";
    public static String GrossAmount = "GrossAmount";
    public static String InvoiceNumber = "";
    public static String FIPDocumentNumber = "";
    public static ODataGuid VisitActivityRefID = null;
    public static String CompetitorName = "CompName";
    public static String CompetitorGUID = "CompGUID";
    public static String PerformanceTypeID = "PerformanceTypeID";
    public static String AttendanceTypeH1 = "AttendanceTypeH1";
    public static String AttendanceTypeH2 = "AttendanceTypeH2";
    public static String AutoClosed = "AutoClosed";
    public static String PerformanceOnIDDesc = "PerformanceOnIDDesc";
    public static String Material_Catgeory = "MaterialCategory";
    public static String DbBatch = "Batch";
    public static String ManufacturingDate = "ManufacturingDate";
    public static String Material_No = "MaterialNo";
    public static String Material_Desc = "MaterialDesc";
    public static String BaseUom = "BaseUom";
    public static String BasePrice = "BasePrice";
    public static String SPStockItemGUID = "SPStockItemGUID";
    public static String SPSNoGUID = "SPSNoGUID";
    public static String SerialNoTo = "SerialNoTo";
    public static String SerialNoFrom = "SerialNoFrom";
    public static String Option = "Option";
    public static String StockTypeID = "StockTypeID";
    public static String QAQty = "QAQty";
    public static String UnrestrictedQty = "UnrestrictedQty";
    public static String BlockedQty = "BlockedQty";
    public static String PrefixLength = "PrefixLength";
    public static String Zzindicator = "Zzindicator";
    public static String EvaluationTypeID = "EvaluationTypeID";
    public static String ReportOnID = "ReportOnID";
    public static String QtyTarget = "QtyTarget";
    public static String QtyLMTD = "QtyLMTD";
    public static String QtyMTD = "QtyMTD";
    public static String QtyMonthlyGrowth = "QtyMonthlyGrowth";
    public static String QtyMonth1PrevPerf = "QtyMonth1PrevPerf";
    public static String QtyMonth2PrevPerf = "QtyMonth2PrevPerf";
    public static String QtyMonth3PrevPerf = "QtyMonth3PrevPerf";
    public static String AmtTarget = "AmtTarget";
    public static String AmtLMTD = "AmtLMTD";
    public static String AmtMTD = "AmtMTD";
    public static String AmtMonth1PrevPerf = "AmtMonth1PrevPerf";
    public static String AmtMonthlyGrowth = "AmtMonthlyGrowth";
    public static String AmtMonth2PrevPerf = "AmtMonth2PrevPerf";
    public static String AmtMonth3PrevPerf = "AmtMonth3PrevPerf";
    public static String PerformanceOnID = "PerformanceOnID";
    public static String PerformanceGUID = "PerformanceGUID";
    public static String QtyLastYearMTD = "QtyLastYearMTD";
    public static String AmtLastYearMTD = "AmtLastYearMTD";
    public static double RCVStockValueDouble = 0.0;
    public static double SIMStockValue = 0.0;
    public static String StockValue = "StockValue";
    public static String CPStockItemGUID = "CPStockItemGUID";
    public static String ComplaintCategory = "ComplaintCategory";
    public static String RschGuid = "RschGuid";
    public static String RouteSchGUID = "RouteSchGUID";
    public static String VisitCPGUID = "VisitCPGUID";
    public static String VisitCPName = "VisitCPName";
    public static String SalesPersonID = "SalesPersonID";
    public static String ShortName = "ShortName";
    public static String RoutId = "RoutId";
    public static String SequenceNo = "SequenceNo";
    public static String DayOfWeek = "DayOfWeek";
    public static String DayOfMonth = "DayOfMonth";
    public static String DayDashBoardList = "DayDashBoardList";
    public static String MonthDashBoardList = "MonthDashBoardList";
    public static String SharMonthDashBoardList = "SharMonthDashBoardList";
    public static String DOW = "DOW";
    public static String DOM = "DOM";
    public static String SalesOrderEntity = ".SO";
    public static String SalesOrderItemEntity = ".SOItemDetail";
    public static boolean isImgCapAtGmLevel = false;
    public static boolean isRunning = false;
    public static boolean isStoreClosed = false;
    public static Boolean IsOnlineStoreFailed = false;
    public static OnlineODataStore onlineStore = null;
    public static ArrayList<String> selectedPositionsDemon = new ArrayList<String>();
    public static ArrayList<String> selectedPositionsProm = new ArrayList<String>();
    public static HashMap<String, String> selectedMatGrpStatusDemon = new HashMap<String, String>();
    public static HashMap<String, String> selectedMatGrpStatusPrompt = new HashMap<String, String>();
    public static String collectionName[] = null;
    public static boolean isCustContactLists;
    public static boolean isCustomerLists;
    public static boolean FlagForSyncError = false;
    public static String resSO = "";
    public static String reqSO = "";
    public static String CREATEREQUEST = "Create_Request";
    public static String UPDATEREQUEST = "Update_Request";
    public static String STOREOPENREQUEST = "Store_Open_Request";
    public static String DELETEREQUEST = "Delete_Request";
    public static String UPLOADFILEREQUEST = "Create_Request";
    public static String FLUSHREQUEST = "Flush_Request";
    public static String REFRESHREQUEST = "Refresh_Request";
    public static HashMap<String, Object> MapEntityVal = new HashMap<String, Object>();
    public static String COLLECTIONHDRS = "CollectionHdrs";
    public static String COLLECTIONITEMS = "CollectionItems";
    public static String OPEN_INVOICE_LIST = "OpenInvList";
    public static String INVOICES = "Invoices";
    public static String VISITACTIVITIES = "VisitActivities";
    public static String INVOICESSERIALNUMS = "InvoiceItmSerNumList";
    public static String ENDLONGITUDE = "EndLongitude";
    public static String REMARKS = "Remarks";
    public static String VISITKEY = "VisitGUID";
    public static String ROUTEPLANKEY = "RoutePlanGUID";
    public static String LOGINID = "LoginID";
    public static String DATE = "Date";
    ;
    public static String VISITTYPE = "VisitType";
    public static String CUSTOMERNO = "CustomerNo";
    public static String REASON = "Reason";
    public static String STARTDATE = "StartDate";
    public static String STARTTIME = "StartTime";
    public static String STARTLATITUDE = "StartLatitude";
    public static String STARTLONGITUDE = "StartLongitude";
    public static String ENDTIME = "EndTime";
    public static String ENDDATE = "EndDate";
    public static String ENDLATITUDE = "EndLatitude";
    public static String ETAG = "ETAG";
    public static String VisitActivityGUID = "VisitActivityGUID";
    public static String VisitGUID = "VisitGUID";
    public static String ActivityType = "ActivityType";
    public static String ActivityTypeDesc = "ActivityTypeDesc";
    public static String ActivityRefID = "ActivityRefID";
    public static boolean flagforexportDB;
    public static String Validity = "Validity";
    public static String Benefits = "Benefits";
    public static String Price = "Price";
    public static String ItemNo = "ItemNo";
    public static String SchemeDesc = "SchemeDesc";
    public static String SchemeGuid = "SchemeGuid";
    public static String ReviewDate = "ReviewDate";
    public static String CPTypeID = "CPTypeID";
    public static String SPGuid = "SPGuid";
    public static String EntityAttribute4 = "EntityAttribute4";
    public static String SoldToCPGUID = "SoldToCPGUID";
    public static String ShipToCPGUID = "ShipToCPGUID";
    public static String SoldToTypeID = "SoldToTypeID";
    public static String ShipToTypeID = "ShipToTypeID";
    public static String CPName = "CPName";
    public static String Address1 = "Address1";
    public static String CountryID = "CountryID";
    //	public static String Country = "Country";
    public static String BTSCircle = "BTSCircle";
    public static String DesignationID = "DesignationID";
    public static String DesignationDesc = "DesignationDesc";
    public static String DistrictDesc = "DistrictDesc";
    public static String CityDesc = "CityDesc";
    public static String CityID = "CityID";
    public static String DistrictID = "DistrictID";
    public static String VisitNavigationFrom = "";
    public static String BirthDayAlertsKey = "BirthDayAlertsKey";
    public static String BirthDayAlertsDate = "BirthDayAlertsDate";
    public static String DBStockKey = "DBStockKey";
    public static String DBStockKeyDate = "DBStockKeyDate";
    public static String District = "District";
    public static String StateID = "StateID";
    public static String Landmark = "Landmark";
    public static String PostalCode = "PostalCode";
    public static String SalesPersonMobileNo = "MobileNo";
    public static String MobileNo = "Mobile1";
    public static String CPMobileNo = "CPMobileNo";
    public static String EmailID = "EmailID";
    public static String ExternalRefID = "ExternalRefID";
    public static String DOB = "DOB";
    public static String PAN = "PAN";
    public static String VATNo = "VATNo";
    public static String TIN = "TIN";
    public static String OwnerName = "OwnerName";
    public static String OutletName = "Name";
    public static String RetailerProfile = "Group1";
    public static String Group2 = "Group2";
    public static String Latitude = "Latitude";
    public static String Longitude = "Longitude";
    public static String SetResourcePath = "SetResourcePath";
    public static String PartnerMgrGUID = "PartnerMgrGUID";
    public static String OtherCustGuid = "OtherCustGuid";
    public static String CPGUID32 = "CPGUID32";
    public static String CPGUID = "CPGUID";
    public static String CPGuid = "CPGuid";
    public static String SyncHistorys = "SyncHistorys";
    public static String UserPartners = "UserPartners";
    public static String OINVAG = "OINVAG";
    public static String AccountGrp = "AccountGrp";
    public static String Anniversary = "Anniversary";
    public static String ApprovedAt = "ApprovedAt";
    public static String ApprovedBy = "ApprovedBy";
    public static String ApprovedOn = "ApprovedOn";
    public static String ApprvlStatusDesc = "ApprvlStatusDesc";
    public static String ApprvlStatusID = "ApprvlStatusID";
    public static String ChangedAt = "ChangedAt";
    public static String ChangedOn = "ChangedOn";
    public static String Country = "Country";
    public static String CountryName = "CountryName";
    public static String CPStock = "CPStock";
    public static String CPTypeDesc = "CPTypeDesc";
    public static String EvaluationTypeDesc = "EvaluationTypeDesc";
    public static String CreatedAt = "CreatedAt";
    public static String CreditDays = "CreditDays";
    public static String CreditLimit = "CreditLimit";
    public static String Totaldebit = "TotDebitBal";
    public static String Group1Desc = "Group1Desc";
    public static String Group2Desc = "Group2Desc";
    public static String Group3 = "Group3";
    public static String Group3Desc = "Group3Desc";
    public static String Group4 = "Group4";
    public static String Group4Desc = "Group4Desc";
    public static String IsKeyCP = "IsKeyCP";
    public static String Landline = "Landline";
    public static String Mobile2 = "Mobile2";
    public static String ParentTypDesc = "ParentTypDesc";
    public static String ParentTypeID = "ParentTypeID";
    public static String PartnerMgrName = "PartnerMgrName";
    public static String PartnerMgrNo = "PartnerMgrNo";
    public static String SalesGroupID = "SalesGroupID";
    public static String SalesGrpDesc = "SalesGrpDesc";
    public static String SalesOffDesc = "SalesOffDesc";
    public static String SalesOfficeID = "SalesOfficeID";
    public static String SearchTerm = "SearchTerm";
    public static String StateDesc = "StateDesc";
    public static String StatusDesc = "StatusDesc";
    public static String TownID = "TownID";
    public static String UOM = "UOM";
    public static String ZoneDesc = "ZoneDesc";
    public static String ZoneID = "ZoneID";
    public static String SIMStockUOM = "";
    public static String str_02 = "02";
    public static String InvoiceHisNo = "InvoiceNo";
    public static String InvoiceDate = "InvoiceDate";
    public static String InvoiceAmount = "InvoiceAmount";
    public static String InvoiceAmount1 = "GrossAmount";
    public static String InvoiceStatus = "InvoiceStatus";
    public static String InvoiceGUID = "InvoiceGUID";
    public static String OutAmount = "OutAmount";
    public static String SoldToName = "SoldToName";
    public static String SoldToID = "SoldToID";
    public static String TypesValue = "TypeValue";
    public static String PassedFrom = "PassedFrom";
    public static String CPNo = "CPNo";
    public static String RetailerName = "Name";
    public static String Address2 = "Address2";
    public static String Address3 = "Address3";
    public static String Address4 = "Address4";
    public static String TownDesc = "TownDesc";
    public static String ParentID = "ParentID";
    public static String ParentName = "ParentName";
    public static String StatusID = "StatusID";
    public static String StatusIdRetailer = "01";
    public static String VisitSeq = "VisitSeq";
    public static String Description = "Description";
    public static String EXTRA_COMPLAINT_BEAN = "ExtraComplaintBean";
    public static String CategoryId = "CategoryId";
    public static String VoiceBalance = "VoiceBalance";
    public static String DataBalance = "DataBalance";
    public static String Last111Date = "Last111Date";
    public static String OutstandingAmt = "OutstandingAmt";
    public static String LastInvAmt = "LastInvAmt";
    public static String NewLaunchedProduct = "New Launched Product";
    public static String MustSellProduct = "Must Sell Product";
    public static String FocusedProduct = "Focused Product";
    public static final String[] reportsArray = {SALESORDER,
            "Collection History", "Outstanding", MustSellProduct, FocusedProduct, NewLaunchedProduct,
            SnapshotList, "Invoices", "Credit Status", "Merchandising Snapshot List", "Feedback List", "Dealer Trends",
            "Complaint List", SALESORDER, "Invoice History", "Outstanding", "Distributor Trend", "ROs", "ROs",};
    public static String SalesOrderCreate = "Sales Order Create";
    public static String StockGuid = "StockGuid";
    public static String MerchReviewGUID = "MerchReviewGUID";
    public static String SPNo = "SPNo";
    public static String SPName = "SPName";
    public static String SPGUID = "SPGUID";
    public static String DistanceUOM = "DistanceUOM";
    public static String AppVisibility = "AppVisibility";
    public static String MerchReviewType = "MerchReviewType";
    public static String MerchReviewTypeDesc = "MerchReviewTypeDesc";
    public static String MerchReviewTime = "MerchReviewTime";
    public static String CreatedBy = "CreatedBy";
    public static String CrdtCtrlArea = "CrdtCtrlArea";
    public static String CrdtCtrlAreaDs = "CrdtCtrlAreaDesc";
    public static String CreatedOn = "CreatedOn";
    public static String ChangedBy = "ChangedBy";
    public static String TestRun = "TestRun";
    public static String SPCategoryDesc = "SPCategoryDesc";
    public static String FeedbackNo = "FeedbackNo";
    public static String FeebackGUID = "FeebackGUID";
    public static String FeedbackType = "FeedbackType";
    public static String FeedbackTypeDesc = "FeedbackTypeDesc";
    public static String SPCategoryID = "SPCategoryID";
//    public static String Location = "Location";
    public static String Location1 = "Location1";
    public static String BTSID = "BTSID";
    public static String Testrun = "Testrun";
    public static String FeebackItemGUID = "FeebackItemGUID";
    public static String MerchReviewDate = "MerchReviewDate";
    public static String MerchReviewLat = "MerchReviewLat";
    public static String MerchReviewLong = "MerchReviewLong";
    public static String MerchImageGUID = "MerchImageGUID";
    public static String ImageMimeType = "ImageMimeType";
    public static String ImageSize = "ImageSize";
    public static String Image = "Image";
    public static String ImagePath = "ImagePath";
    public static String ImageByteArray = "ImageByteArray";
    public static String DocumentStore = "DocumentStore";
    public static String FileName = "FileName";
    public static String PlannedDate = "PlannedDate";
    public static String PlannedStartTime = "PlannedStartTime";
    public static String PlannedEndTime = "PlannedEndTime";
    public static String VisitTypeID = "VisitTypeID";
    public static String VisitTypeDesc = "VisitTypeDesc";
    public static String VisitDate = "VisitDate";
    public static String ProposedRoute = "ProposedRoute";
    public static String ApprovedRoute = "ApprovedRoute";
    public static String RouteID = "RouteID";
    public static String RouteDesc = "RouteDesc";
    public static String RoutePlanKey = "RoutePlanKey";
    public static String PaymentStatusID = "PaymentStatusID";
    public static String PaymentModeID = "PaymentModeID";
    public static String PaymentMode = "PaymentMode";
    public static String PaymentModeDesc = "PaymentModeDesc";
    public static String PaymetModeDesc = "PaymetModeDesc";
    public static String BranchName = "BranchName";
    public static String InstrumentNo = "InstrumentNo";
    public static String InstrumentDate = "InstrumentDate";
    public static String BankID = "BankID";
    public static String Remarks = "Remarks";
    public static String Currency = "Currency";
    public static String Amount = "Amount";

    public static String[] getDefinigReq(Context context) {//TODO Sp need to add and need to increase the version
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        String loginId = sharedPreferences.getString("username", "");
        String rollType = sharedPreferences.getString(USERROLE, "");
        String[] DEFINGREQARRAY = {
                        "UserProfileAuthSet?$filter=Application%20eq%20%27PD%27",
                        "SalesPersons", UserSalesPersons,SyncHistorys,
                        "ConfigTypesetTypes?$filter=Typeset eq 'DELVST' or Typeset eq 'INVST' " +
                                "or Typeset eq 'REJRSN' or Typeset eq 'UOMNO0' or Typeset eq 'OINVAG' or Typeset eq 'SOITST' or Typeset eq 'EVLTYP' or Typeset eq 'CRDCTL' or Typeset eq 'RODLST' or Typeset eq 'ROGRST'",
                        "ConfigTypsetTypeValues?$filter=Typeset eq 'PD' or " +
                                "Typeset eq 'ATTTYP' or Typeset eq 'RVWTYP' or Typeset eq 'FIPRTY' " +
                                "or Typeset eq 'ACTTYP' or Typeset eq 'SF' or Typeset eq 'SC' or Typeset eq 'SS' or " +
                                "Typeset eq 'SP' "

                };
                return DEFINGREQARRAY;
            }





   /* public static String getLoginName() {

        String loginQry = Constants.SalesPersons;
        String mStrLoginName = "";
        try {
            mStrLoginName = OfflineManager.getLoginName(loginQry);
        } catch (OfflineODataStoreException e) {
            LogManager.writeLogError(Constants.error_txt + e.getMessage());
        }

        return mStrLoginName;
    }*/

    /* public static String getSalesPeronMobileNo() {

         String loginQry = Constants.SalesPersons + "?$select=" + Constants.SalesPersonMobileNo + " ";
         String mStrMobNo = "";
         try {
             mStrMobNo = OfflineManager.getSalePersonMobileNo(loginQry);
         } catch (OfflineODataStoreException e) {
             LogManager.writeLogError(Constants.error_txt + e.getMessage());
         }

         return mStrMobNo;
     }*/
    public static String FIPGUID = "FIPGUID";
    public static String FIPDocType = "FIPDocType";
    public static String FIPDate = "FIPDate";
    public static String FIPDocNo = "FIPDocNo";
    public static String FIPAmount = "FIPAmount";
    public static String DebitCredit = "DebitCredit";
    public static String ParentNo = "ParentNo";
    public static String SPFirstName = "SPFirstName";
    public static String Tax1Amt = "Tax1Amt";
    public static String Tax2Amt = "Tax2Amt";
    public static String Tax3Amt = "Tax3Amt";
    public static String Tax1Percent = "Tax1Percent";
    public static String Tax2Percent = "Tax2Percent";
    public static String Tax3Percent = "Tax3Percent";
    public static String ReferenceUOM = "ReferenceUOM";
    public static String RetOrdNo = "RetOrdNo";
    public static String OrderReasonID = "OrderReasonID";
    public static String OrderReasonDesc = "OrderReasonDesc";
    public static String State = "State";
    public static String SalesDist = "SalesDist";
    public static String Route = "Route";
    public static String SplProcessing = "SplProcessing";
    public static String SplProcessingDesc = "SplProcessingDs";
    public static String MatFrgtGrp = "MatFrgtGrp";
    public static String MatFrgtGrpDesc = "MatFrgtGrpDs";
    public static String SyncHisGuid = "SyncHisGuid";
    public static String SyncCollection = "Collection";
    public static String SyncApplication = "Application";
    public static String SyncDate = "SyncDate";
    public static String SyncHisTime = "SyncTime";
    public static String SyncTypeDesc = "SyncTypeDesc";
    public static String SyncType = "SyncType";
    public static String SyncHistroy = "SyncHistorys";
    static HttpsURLConnection connection = null;


    //    public static boolean onGpsCheckCustomMessage(final Context context, String message) {
//        UtilConstants.canGetLocation(context);
//        if (!UtilConstants.canGetLocation(context)) {
//            AlertDialog.Builder gpsEnableDlg = new AlertDialog.Builder(context, R.style.MyTheme);
//            gpsEnableDlg
//                    .setMessage(message);
//            gpsEnableDlg.setPositiveButton("Enable",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(
//                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                            context.startActivity(intent);
//                        }
//                    });
//            // on pressing cancel button
//            gpsEnableDlg.setNegativeButton("Cancel",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//            // Showing Alert Message
//            gpsEnableDlg.show();
//        }
//        return GpsTracker.isGPSEnabled;
//    }
    public static String PartnerId = "PartnerId";
    public static String PartnerType = "PartnerType";
    public static String Sync_All = "000001";
    //    public static String All_DownLoad = "000002";
    public static String DownLoad = "000002";
    public static String UpLoad = "000003";
    public static String Auto_Sync = "000004";
    public static String EndSync = "End";
    public static String StartSync = "Start";
    public static String FIPItemGUID = "FIPItemGUID";
    public static String ReferenceID = "ReferenceID";
    public static String ReferenceDate = "ReferenceDate";
    public static String BalanceAmount = "BalanceAmount";
    public static String ClearedAmount = "ClearedAmount";
    public static String FIPItemNo = "FIPItemNo";
    public static String FirstName = "FirstName";
    public static String SalesOffice = "SalesOffice";
    public static String LastName = "LastName";
    public static String AttendanceGUID = "AttendanceGUID";
    public static String StartDate = "StartDate";
    public static String StartTime = "StartTime";
    public static String EndTime = "EndTime";
    public static String StartLat = "StartLat";
    public static String StartLong = "StartLong";
    public static String EndDate = "EndDate";
    public static String EndLat = "EndLat";
    public static String EndLong = "EndLong";
    public static String Etag = "Etag";
    public static String TextCategoryID = "TextCategoryID";
    public static String TextCategoryTypeID = "TextCategoryTypeID";
    public static String TextCategoryDesc = "TextCategoryDesc";
    public static String TextCategoryTypeDesc = "TextCategoryTypeDesc";
    public static String Text = "Text";
    public static String InvoiceHisMatNo = "MaterialNo";
    public static String InvoiceHisMatDesc = "MaterialDesc";
    public static String InvoiceHisAmount = "GrossAmount";
    public static String InvoiceHisQty = "Quantity";
    public static String CompName = "CompName";
    public static String CompGUID = "CompGUID";
    public static String CompInfoGUID = "CompInfoGUID";
    public static String Earnings = "Earnings";
    public static String SchemeAmount = "SchemeAmount";
    public static String SchemeName = "SchemeName";
    public static String SchemeGUID = "SchemeGUID";
    public static String ValidFromDate = "ValidFromDate";
    public static String ValidToDate = "ValidToDate";
    public static String MatGrp1Amount = "MatGrp1Amount";
    public static String MatGrp2Amount = "MatGrp2Amount";
    public static String MatGrp3Amount = "MatGrp3Amount";
    public static String MatGrp4Amount = "MatGrp4Amount";
    public static String UpdatedOn = "UpdatedOn";
    public static String PurchaseQty = "PurchaseQty";
    public static String PurchaseAmount = "PurchaseAmount";
    public static String DealerName = "DealerName";
    public static String DealerCode = "DealerCode";
    public static String DealerType = "DealerType";
    public static String MTDValue = "MTDValue";
    public static String OrderToRecivive = "OrderToRecivive";
    public static String DateofDispatch = "DateofDispatch";
    public static String TradeDate = "TradeDate";
    public static String PriceDate = "PriceDate";
    public static String BrandName = "BrandName";
    public static String HDPE = "HDPE";
    public static String PaperBag = "PaperBag";
    public static String PriceType = "PriceType";
    public static String diaryCheck = "diaryCheck";
    public static String chitPadCheck = "chitPadCheck";
    public static String bannerCheck = "bannerCheck";
    public static String AmountOne = "AmountOne";
    public static String DateOne = "DateOne";
    public static String AmountTwo = "AmountTwo";
    public static String DateTwo = "DateTwo";
    public static String AmountThree = "AmountThree";
    public static String DateThree = "DateThree";
    public static String AmountFour = "AmountFour";
    public static String DateFour = "DateFour";
    public static boolean collCreate = false;
    public static boolean CEFCreate = false;
    public static boolean returnOrdeCreate = false;
    public static boolean beatlist = false;
    public static HashMap<String, Integer> mapCount = new HashMap<String, Integer>();
    public static HashMap<String, String> MapSpinnerSelectedValue = new HashMap<String, String>();
    public static HashMap<String, String> MapRejectionReason = new HashMap<String, String>();
    public static HashMap<String, String> MapRoutePlanReason = new HashMap<String, String>();
    public static HashMap<String, String> MapApprovalReason = new HashMap<String, String>();
    public static HashMap<String, Integer> MapApprovalStatusIndexValue = new HashMap<String, Integer>();
    public static String ReturnDealer = "";
    public static int count = 0;
    public static String RETURNORDERMATERIAL = "MDStockSerList";
    public static String[] retailer_names = null;
    public static String[] retailer_codes = null;
    public static Map<String, List<String>> matSer = new HashMap<String, List<String>>();
    public static String selOutletCode = null;
    public static String selOutletName = null;
    public static String RegError = null;
    public static String uniqueId = "";
    public static String UserID = "";
    public static String APP_PKG_NME = "";
    public static String host = null;
    public static String port = null;
    public static String CLIENT = "";
    public static String customerCodeName = null;
    public static String customerCode = null;
    public static String outletCode = null;
    public static String outletCodeName = null;
    public static String Dealer = "DEALER";
    public static String Dealer_Synckey = "";
    public static String Dealer_Name = "";
    public static String Dealer_date = "";
    public static String Dealer_Id = "";
    public static int PLANNED_VISIT = 0;
    public static int ACT_VISIT = 6;
    public static double TODAY_ACH_TARG = 0;
    public static int MONTH_ACH = 0;
    public static double GrosPrice = 0.0;
    public static double Vat = 0.0;
    // sync time
    public static String SYNC_START_TIME = "";
    public static String SYNC_END_TIME = "";
    public static String CREDIT_LIMIT_SYNC_TIME = "";
    public static String STOCKOVERVIEW_SYNC_TIME = "";
    // public static boolean isTerminateSync = false;
    public static boolean flagUpt = false;
    public static boolean chkdata = true;
    public static boolean isReg = false;
    public static boolean isRegError = false;
    public static boolean isSavePassChk = false;
    public static boolean iSAutoSyncStarted1 = false;
    public static boolean iSInsideCreate = false;
    public static boolean iSAttendancesync = false;
    public static boolean iSStartsync = false;
    public static boolean iSClosesync = false;
    public static boolean iSDealersync = false;
    public static boolean iSOutletsync = false;
    public static boolean iSFjpsync = false;
    public static boolean iSSignboardsync = false;
    public static boolean iSVisitsync = false;
    public static boolean iSDealerVisitsync = false;
    public static boolean iSSOCreatesync = false;
    public static boolean isSOCreateTstSync = false;
    public static boolean isSOCreateTstSync1 = false;
    public static boolean iSoutletCreatesync = false;
    public static boolean iSstartCreatesync = false;
    public static boolean iSCloseCreatesync = false;
    public static boolean iSServiceCreated = false;
    public static boolean iSMetaDocCreated = false;
    public static boolean iSClose = false;
    public static boolean iSStart = false;
    public static boolean iSfirstStart = true;
    public static boolean iSfirstStarted = false;
    public static boolean iSfirstclosed = false;
    public static boolean isCollectionsync = false;
    public static boolean isBatchmatstocksync = false;
    public static boolean iSfjpvisit = false;
    public static boolean iSclosevisit = false;
    public static boolean iSclose = false;
    public static boolean isupdatespinnervisit = false;
    public static boolean isupdateclosevisit = false;
    public static boolean iSfirstclose = false;
    public static boolean issolist = false;
    public static boolean isoutletlist = false;
    public static boolean isactivitylist = false;
    public static double latitude, longitude;
    public static boolean isInvoiceVisit = false;
    public static SQLiteDatabase dbCnt;
    public static int beforPendingcount = 0;
    public static int afterPendingcount = 0;
    public static Hashtable<String, String> hashtable;
    public static Hashtable<String, String> headerValues;
    public static Hashtable<String, String> itemValues;
    public static Hashtable[] itemValues_Responce = null;
    ;
    public static Cursor cursor;
    public static String DATABASE_PATH = "";
    public static Context ctx;
    public static String UserNameSyc = "";
    public static int NoOfItems = 0;
    public static int lastQty = 0;
    public static double totalUnitPrice = 0.0;
    public static String matNo = null;
    public static Hashtable INVOICEITEM = null;
    public static boolean iSItemview = false;
    public static Vector checkedSerialNo = new Vector();
    public static Vector beatRetailor = new Vector();
    public static ArrayList<String> list1 = new ArrayList<String>();
    public static ArrayList<String> enterEditTextValList = new ArrayList<String>();
    public static Hashtable<String, String> mapEnteredTextsHashTable = new Hashtable<String, String>();
    public static Hashtable<String, String> mapEnteredPricesHashTable = new Hashtable<String, String>();
    public static Hashtable<String, String> mapEnteredMaterialDescHashTable = new Hashtable<String, String>();
    public static Hashtable<String, String> mapEnteredMaterialGroupHashTable = new Hashtable<String, String>();
    public static Hashtable<String, String> mapEnteredBrandHashTable = new Hashtable<String, String>();
    public static Hashtable<String, String> mapEnteredMatrialUOMHashTable = new Hashtable<String, String>();
    public static HashMap<String, String> mapCheckedStateHashMap = new HashMap<String, String>();
    public static HashMap<String, String> mapEnteredTextsHashMap = new HashMap<String, String>();
    public static HashMap<String, String> mapEnteredPricesHashMap = new HashMap<String, String>();
    public static HashMap<String, String> dealerStockEnteredQtyHashMap = new HashMap<String, String>();
    public static HashMap<String, String> dealerStockMatAndDescHashMap = new HashMap<String, String>();
    public static HashMap<String, String> dealerStockMatAndBrandHashMap = new HashMap<String, String>();
    public static HashMap<String, String> dealerStockEnteredPurchasedQtyHashMap = new HashMap<String, String>();
    public static HashMap<String, String> dealerStockVerfiedQtyHashMap = new HashMap<String, String>();
    public static HashMap<String, String> dealerStockUOMHashMap = new HashMap<String, String>();
    public static ArrayList<String> serialnumlist = new ArrayList<String>();
    public static HashMap<String, Double> InvoiceCreateVat = new HashMap<String, Double>();
    public static HashMap<String, Double> InvoiceCreateGross = new HashMap<String, Double>();
    public static HashMap<String, Double> MapVat = new HashMap<String, Double>();
    public static HashMap<String, Double> EXTRAVat = new HashMap<String, Double>();
    public static String DeviceTble = "Devicecollection";
    public static int congSel = 0;
    public static int congList = 0;
    public static Hashtable SALESORDER_COMMENTS = null;
    public static Hashtable SALESORDER_HEADER = null;
    public static Hashtable[] SALESORDER_ITEMS = null;
    public static Hashtable SALESORDER_RESPONCE = null;
    public static Hashtable[] SALESORDER__HEADER_RESPONCE = null;
    public static Hashtable[] SALESORDER__ITEM_RESPONCE = null;
    public static Hashtable[] SALESORDER_SCHEMES = null;
    public static Hashtable[] ITEMS = null;
    public static Hashtable[] BATCH = null;
    public static Hashtable[] BATCH_COMMENTS = null;
    public static Hashtable fjpVlaues = null;
    public static ArrayList<String> matList = new ArrayList<String>();
    public static ArrayList<ArrayList<String>> batchList = new ArrayList<ArrayList<String>>();
    public static ArrayList<String> focusMatBean = new ArrayList<String>();
    public static ArrayList<String> withoutSelMat = new ArrayList<String>();
    public static ArrayList<String> list = new ArrayList<String>();
    public static ArrayList<String> matCodeDecList = new ArrayList<String>();
    public static ArrayList<String> matDesclist = new ArrayList<String>();
    public static ArrayList<String> selectmatlist = new ArrayList<String>();
    public static ArrayList<String> selectbatchlist = new ArrayList<String>();
    // for temarary storage
    public static ArrayList SALESORDER_CHECK_TEMP = null;
    public static int INDEX_TEMP[] = null;
    public static int INDEX_TEMP1[] = null;
    public static int INDEX_TEMP_NEW[] = null;
    public static int INDEX_TEMP_IN[] = null;
    public static int FOCUS_MATERIAL[] = null;
    public static boolean closeFlag = false;
    public static boolean issaveclose = false;
    public static Boolean isChequeRequired = false;
    public static String[][] matDesc = null;
    public static String FROM_PER = "";
    public static String FROM_PER1 = "";
    public static String TO_PER = "";
    // for visit storage
    public static Hashtable VISIT_HEADER = null;
    public static Hashtable[] VISIT_ITEMS = null;
    public static Hashtable[][] VISIT2_ITEMS = null;
    public static Hashtable INVERTER_QUANTITY = null;
    public static Hashtable[] DISTRIBUTOR_ITEMS = null;
    public static Hashtable[] COMPENTITOR_ITEMS = null;
    public static Hashtable[] MATERIALBATCHITEMS = null;
    public static int lengthofdealer = 0;
    public static int lengthofproducts = 0;
    public static int lengthofdistributor = 0;
    // for Dealer visit storage
    public static Hashtable DEALERVISIT_HEADER = null;
    public static Hashtable[] DEALER_ITEMS = null;
    public static int lengthofdealeritems = 0;
    public static Hashtable DEALERVISIT_POP = null;
    public static Hashtable[] DEALERCOMPENTITOR_ITEMS = null;
    public static int lengthofdealercomp = 0;
    public static boolean isSalesTargetSync = false;
    public static boolean iscollTargetSync = false;
    public static boolean isdlrofftakeSync = false;
    public static boolean isdlrprefSync = false;
    public static boolean issoitemSync = false;
    // for star image
    public static boolean is_accounts = false;
    public static boolean is_product_price = false;
    public static boolean isstock = false;
    public static boolean is_sales_order = false;
    public static boolean is_invoice = false;
    public static boolean is_collections = false;
    public static boolean is_activity = false;
    public static boolean is_target = false;
    public static Hashtable<String, String> HashTableSerialNoAllocatedQty = new Hashtable<String, String>();
    public static String AuthOrgValue = "AuthOrgValue";
    public static String AuthOrgTypeID = "AuthOrgTypeID";
    public static String AuthOrgValDesc = "AuthOrgValueDesc";
    public static String AuthOrgTypeDesc = "AuthOrgTypeDesc";
    public static String StockOwner = "StockOwner";
    public static String SO_RESPONCE_ORDNO = "";
    public static String SO_RESQUEST_ORDNO = "";
    public static String OUTLET_RESPONCENO = "";
    public static String OUTLET_RESQUESTNO = "";
    public static Map<String, List<String>> focusmaterials = new HashMap<String, List<String>>();
    public static Hashtable[] SALESORDER_FOCUSMATERIALS = null;
    public static String selectedOutletCode = "";
    public static String selectedOutletDesc = "";
    public static boolean isMaterDataSyncEnable, isFocuPrdSyncEnable,
            isCollectionSyncEnable, isFJPSyncEnable, isActSyncEnable,
            isBatchBlockSyncEnable, isExcMaterialSyncEnable,
            isMatStockSyncEnable, isOutstandSyncEnable, isSOSyncEnable,
            isStartCloseSyncEnable, isAuthSyncEnable, isVisitSyncEnable, isMaterialSyncEnable,
            isSTOSyncEnable, isSalesOrderSyncEnable, isDeliverySyncEnable,
            isInvoiceSync, isStockSyncEnable,
            isCollSyncEnable, isVisitStartSyncEnable;
    public static Hashtable[] SERIALNUMS;
    public static String SubOrdinates = "SubOrdinates";
    public static String CustomerComplaintTxts = "CustomerComplaintTxts";
    public static String RoutePlanApprovals = "RoutePlanApprovals";
    public static String BUSINESSCALKEYNO = "";
    public static Date dateFrom;
    public static Date dateTo;
    public static boolean OrderCreated = false;
    public static boolean ReturnOrderCreated = false;
    public static boolean collectionUpdated = false;
    public static boolean snapshotTaken = false;
    public static boolean bussinessCallSavedSucessfully = false;
    public static boolean relationshipCallSavedSuccessfully = false;
    public static boolean ShadeCardSuccessfully = false;
    public static boolean CustomerComplaintsSavedSuccessfully = false;
    public static boolean DealerStockEnteredSuccessfully = false;
    public static boolean CompetitorStockSuccessfully = false;
    public static HashMap<String, Boolean> mapAllDone = new HashMap<String, Boolean>();
    public static String retailerIDSelected = "";
    public static String retailerNameSelected = "";
    public static String mobileNo = "";
    public static String address1 = "";
    public static String address2 = "";
    public static int selectednumber = 0;
    public static boolean newProduct = false;
    public static boolean FocusProduct = false;
    public static boolean MustSell = false;
    public static String RRETAILERMOBILENO = "";
    public static String RRETAILERFITSTADDRESS = "";
    public static String RRETAILERSECONDADDRESS = "";
    public static String MaterialGrpAndCode = "MaterialGrpAndCode";
    public static String BalanceConfirmationHeader = "BalanceConfirmationHeader";
    public static String BalanceConfirmationItems = "BalanceConfirmationItems";
    public static String BalanceConfirmations = "BalanceConfirmations";
    public static String BalConfirmItemDetaills = "BalConfirmItemDetails";
    public static boolean isCreateFlag = false;
    public static boolean isSOCountDone = false;
    public static boolean isAppEndPointDone = false;
    public static boolean isMetaDataDone = false;
    public static boolean isSOItmCountDone = false;
    public static boolean isMaterialsCountDone = false;
    public static boolean isPriceListCountDone = false;
    public static String OutstandingSummary = "OutstandingSummary";
    public static String CustomerwiseOSs = "CustomerwiseOSs";
    public static String Promotion = "Promotion";
    public static String BrandPerformanc = "BrandPerformance";
    public static String ErrorMsg = "";
    public static String newMPNo = "";
    public static String MerchndisingKeyNo = "";
    public static String VisitKeyNo = "";
    public static String VisitTypeNo = "";
    public static String VisitStartKeyNo = "";
    public static String VisitStartKeyNoCurrentDealerNo = "";
    public static String Collections = "Collections";
    public static String CollectionItemDetails = "CollectionItemDetails";
    public static String History = "History";
    public static String PendingSync = "Pending Sync";
    public static String Merchindising = "Merchandising";
    public static String DeviceMerchindising = "Device Merchandising";
    public static String SyncGroup = "SyncGroup";
    public static String MasterPainter = "MasterPainter";
    public static String reqExpensesNo;
    public static String resExpensesNo;
    public static String resLeadProjectNo;
    public static String reqLeadProjectNo;
    public static String MerchandisingReview = "MerchandisingReview";
    public static String reqMerchandisingNo;
    public static String resMerchandisingNo;
    public static String resBusinessCallNo;
    public static String reqBusinessCallNo;
    public static String reqRelationShipCallNo;
    public static String resRelationShipCallNo;
    public static String reqCustomerComplaintNo;
    public static String resCustomerComplaintNo;
    public static String collectionresDocNo = "";
    public static String collectionreqDocNo = "";
    public static String reqMasterPainterNo = "";
    public static String resMasterPainterNo = "";
    public static String reqAttendanceID = "";
    public static String resAttendanceID = "";
    public static boolean SoCreateSeaniro = false;
    public static double latitudeValue;
    public static double longitudeValue;
    public static boolean isCollListsAuthEnabled;
    public static String CustomerComplaintNo = "";
    public static boolean isMerchSyncEnable;
    public static String PreviousMaterialGrp = "";
    public static String ExpenseEntrys = "ExpenseEntrys";
    public static boolean isCustPerFormances, isAttencesSync;
    public static boolean isShadeCardEnabled;
    public static boolean isTargetsEnabled;
    public static String reqShadeCardFeedBackNo;
    public static String resShadeCardFeedBackNo;
    public static boolean isDishonourSyncEnable;
    public static String reqLeadChangeProjectNo;
    public static String resLeadChangeProjectNo;
    public static String resMatSO;
    public static String reqMatSO;
    public static String OfficerEmployeeCode = "OfficerEmployeeCode";
    public static String CounterName = "CounterName";
    public static String LongitudeAndLatitude = "LongitudeAndLatitude";
    public static String CounterType = "CounterType";
    public static String ContactPerson = "ContactPerson";
    public static String PCMobileNo = "PCMobileNo";
    public static String ProspectecCustomerAddress = "ProspectecCustomerAddress";
    public static String PCDistrict = "PCDistrict";
    public static String Taluka = "Taluka";
    public static String PinCode = "PinCode";
    public static String PCcity = "City";
    public static String Block = "Block";
    public static String TotalTradePottential = "TotalTradePottential";
    public static String TotalNonTradePottential = "TotalNonTradePottential";
    public static String PottentialAvailable = "PottentialAvailable";
    public static String UTCL = "UTCL";
    public static String OCL = "OCL";
    public static String LAF = "LAF";
    public static String ACC = "ACC";
    public static String POPDistributed = "POPDistributed";
    public static String PCRemarks = "PCRemarks";
    public static String OACustomerNo = "CustomerNo";
    public static String OACustomerName = "CustomerName";
    public static String OACityName = "CityName";
    public static String OATelephone1 = "Telephone1";
    public static String OADistChannel = "DistChannel";
    public static String OASecurityDeposit = "SecurityDeposit";
    public static String OACreditLimit = "CreditLimit";
    public static String OATotalDebitBal = "TotalDebitBal";
    public static String OA0_7Days = "SevenDays";
    public static String OA7_15Days = "FifteenDays";
    public static String OA15_30Days = "ThirtyDays";
    public static String OA30_45Days = "FortyfiveDays";
    public static String OA45_60Days = "SixtyDays";
    public static String OA60_90Days = "NintyDays";
    public static String OA90_120Days = "OneTwentyDays";
    public static String OA120_180Days = "OneEightyDays";
    public static String OA180Days = "OneEightyPlusDays";
    public static String OAPastDays = "PastDays";
    public static String OACurrentDays = "CurrentDays";
    public static String OA3160Days = "ThirtyoneDays";
    public static String OA6190Days = "SixtyoneDays";
    public static String OA91120Days = "NintyoneDays";
    public static String OA120Days = "OneTwentyPlusDays";
    public static String TADealerNo = "DealerNo";
    public static String TADealerName = "DealerName";
    public static String TADealerCity = "DealerCity";
    public static String TACurMonthTraget = "CurrentMonthTraget";
    public static String TAProrataTraget = "ProrataTraget";
    public static String TASaleACVD = "SaleACVD";
    public static String TAProrataAchivement = "ProrataAchivement";
    public static String TABalanceQty = "BalanceQty";
    public static String TADailyTarget = "DailyTarget";
    public static String TADepotNo = "DepotNo";
    public static String TADepotName = "DepotName";
    public static String Mobile1 = "Mobile1";
    public static ArrayList<HashMap> soItem = new ArrayList<HashMap>();
    public static HashMap selBrand = new HashMap();
    public static ArrayList<HashMap> soDaySummary = new ArrayList<HashMap>();
    public static ArrayList<HashMap> collDaySummary = new ArrayList<HashMap>();
    public static ArrayList<HashMap> invDaySummary = new ArrayList<HashMap>();
    public static String VisitSurveyNo;
    public static String FocusedCustomers = "FocusedCustomers";
    public static String LeadNo = "";
    public static String NewPwd = "";
    public static String CreateOperation = "Create";
    public static String ReadOperation = "Read";
    public static String UpdateOperation = "Update";
    public static String DeleteOperation = "Delete";
    public static String QueryOperation = "Query";
    //new 28112016 Ramu
    public static String Route_Plan_No = "";
    public static String Route_Plan_Desc = "";
    public static String Route_Plan_Key = "";
    public static String Visit_Type = "";
    public static String CustomerType = "";
    public static String VISIT_TYPE = "VISIT_TYPE";
    public static String PlannedRoute = "PlannedRoute";
    public static String PlannedRouteName = "PlannedRouteName";
    public static String PlanedCustomerName = "CustomerName";
    public static int MAX_LENGTH = 100;
    public static String WeeklyOffDesc = "WeeklyOffDesc";
    public static String Error_Msg = "";
    /*error code*/
    public static int ErrorCode = 0;
    public static int ErrorNo = 0;
    public static int ErrorNo_Get_Token = 0;
    public static String ErrorName = "";
    public static String NetworkError_Name = "NetworkError";
    public static String Comm_error_name = "Communication error";
    public static String Network_Name = "Network";
    public static String Unothorized_Error_Name = "401";
    public static String Max_restart_reached = "Maximum restarts reached";
    public static int Network_Error_Code = 101;
    public static int Comm_Error_Code = 110;
    public static int UnAuthorized_Error_Code = 401;
    public static int UnAuthorized_Error_Code_Offline = -10207;
    public static int Network_Error_Code_Offline = -10205;
    public static int Unable_to_reach_server_offline = -10208;
    public static int Resource_not_found = -10210;
    public static int Unable_to_reach_server_failed_offline = -10204;
    public static String Executing_SQL_Commnd_Error = "10001";
    public static int Execu_SQL_Error_Code = -10001;
    public static int Store_Def_Not_matched_Code = -10247;
    public static String Store_Defining_Req_Not_Matched = "10247";
    public static String Invalid_Store_Option_Value = "InvalidStoreOptionValue";
    public static int Build_Database_Failed_Error_Code1 = -100036;
    public static int Build_Database_Failed_Error_Code2 = -100097;
    public static int Build_Database_Failed_Error_Code3 = -10214;
    public static String RFC_ERROR_CODE_100027 = "100027";
    public static String RFC_ERROR_CODE_100029 = "100029";
    public static String ZZForwarAgentCode = "ZZFrwadgAgent";
    public static String OtherRouteGUIDVal = "";
    public static String OtherRouteNameVal = "";
    public static int[] IconVisibiltyReportFragment = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static int[] IconPositionReportFragment = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static String isSOCreateKey = "isSOCreate";
    public static String isSOCreateTcode = "/ARTEC/SS_SOCRET";
    public static String isCollCreateEnabledKey = "isCollCreateEnabled";
    public static String isCollCreateTcode = "/ARTEC/SF_COLLCRT";
    public static String isMerchReviewKey = "isMerCreateEnabled";
    public static String isMerchReviewTcode = "/ARTEC/SS_MERRVW";
    public static String isMerchReviewListKey = "isMerCreateListEnabled";
    public static String isMerchReviewListTcode = "/ARTEC/SS_MERRVWLST";
    public static String isMustSellKey = "isMustSellEnabled";
    public static String isMustSellTcode = "/ARTEC/MC_MSTSELL";
    public static String isFocusedProductKey = "isFocusedProductEnabled";
    public static String isFocusedProductTcode = "/ARTEC/SS_FOCPROD";
    public static String isNewProductKey = "isNewProductEnabled";
    public static String isNewProductTcode = "/ARTEC/SS_NEWPROD";
    public static String isDBStockKey = "isDBStockEnabled";
    public static String isDBStockTcode = "/ARTEC/SS_DBSTK";
    public static String isCompInfoEnabled = "isCompInfoEnabled";
    public static String isCompInfoTcode = "/ARTEC/SS_COMPINFO";
    public static String isSOApprovalKey = "isSOApprovalEnabled";
    public static String isSOApprovalTcode = "/ARTEC/SF_SOAPRL";
    public static String isPlantStockKey = "isPlantStockEnabled";
    public static String isPlantStockTcode = "/ARTEC/SF_PLNTSTK";
    public static String isMatPriceKey = "isMatPriceKey";
    public static String isMatpriceTcode = "/ARTEC/SF_MATPRICE";
    public static String isMTPApprovalKey = "isMTPApprovalKey";
    public static String isMTPApprovalTcode = "/ARTEC/SF_MTP_APRL";
    public static boolean BoolTodayBeatLoaded = false;
    public static boolean BoolOtherBeatLoaded = false;
    public static String ClosingeDay = "ClosingeDay";
    public static String Today = "Today";
    public static String PreviousDay = "PreviousDay";
    public static String ClosingeDayType = "ClosingeDayType";
    public static String SSSOGuid = "SSSOGuid";
    public static String OrderNo = "OrderNo";
    public static String OrderType = "OrderType";
    public static String OrderTypeDesc = "OrderTypeDesc";
    public static String OrderDate = "OrderDate";
    public static String EntryTime = "EntryTime";
    public static String DmsDivision = "DmsDivision";
    public static String DmsDivisionDesc = "DmsDivisionDesc";
    public static String PONo = "PONo";
    public static String PODate = "PODate";
    public static String FromCPGUID = "FromCPGUID";
    public static String FromCPNo = "FromCPNo";
    public static String FromCPName = "FromCPName";
    public static String FromCPTypId = "FromCPTypId";
    public static String FromCPTypDs = "FromCPTypDs";
    public static String SoldToUID = "SoldToUID";
    public static String SoldToDesc = "SoldToDesc";
    public static String SoldToType = "SoldToType";
    public static String SoldToTypDs = "SoldToTypDs";
    public static String ShipToIdCPGUID = "ShipToIdCPGUID";
    public static String ShipToUID = "ShipToUID";
    public static String ShipToDesc = "ShipToDesc";
    public static String ShipToType = "ShipToType";
    public static String ShipToTypDs = "ShipToTypDs";
    public static String GrossAmt = "GrossAmt";
    public static String Freight = "Freight";
    public static String TAX = "TAX";
    public static String Tax = "Tax";
    public static String Discount = "Discount";
    public static String TaxAmount = "TaxAmount";
    public static String CPType = "CPType";
    public static String SoldToId = "SoldToId";
    public static String ShipToParty = "ShipToParty";
    public static String ShipToPartyName = "ShipToPartyName";
    public static String SSSOItemGUID = "SSSOItemGUID";
    public static String OrderMatGrp = "OrderMatGrp";
    public static String OrderMatGrpDesc = "OrderMatGrpDesc";
    public static String Quantity = "Quantity";
    public static String AlternateWeight = "AlternateWeight";
    public static String Uom = "Uom";
    public static String HigherLevelItemno = "HigherLevelItemno";
    public static String IsfreeGoodsItem = "IsfreeGoodsItem";
    public static String RefdocItmGUID = "RefdocItmGUID ";
    public static String Batch = "Batch";
    public static String MRP = "MRP";
    public static String LandingPrice = "LandingPrice";
    public static String SecDiscount = "SecDiscount ";
    public static String PriDiscount = "PriDiscount";
    public static String CashDiscount = "CashDiscount";
    public static String LoginId = "LoginId";
    public static String CustomerCompCreateID = "10";
    public static String ComplaintType = "ComplaintType";
    public static String ComplaintNo = "ComplaintNo";
    public static String ComplaintGUID = "ComplaintGUID";
    public static String ComplaintPriorityID = "ComplaintPriorityID";
    public static String ComplaintPriorityDesc = "ComplaintPriorityDesc";
    public static String MaterialGrp = "MaterialGrp";
    public static String ComplaintDate = "ComplaintDate";
    public static String ComplaintStatusID = "ComplaintStatusID";
    public static String ComplaintStatusDesc = "ComplaintStatusDesc";
    public static String MFD = "MFD";
    public static String SchFreeMatGrpGUID = "SchFreeMatGrpGUID";
    public static String ComplaintCategoryID = "ComplaintCategoryID";
    public static String ComplainCategoryDesc = "ComplainCategoryDesc";
    public static String ComplaintTypeDesc = "ComplaintTypeDesc";
    public static String ComplaintTypeID = "ComplaintTypeID";
    public static String strErrorWithColon = "Error : ";
    public static String SystemKPI = "SystemKPI";
    public static String SO_Cust_QRY = "";
    public static ArrayList<String> alRetailersGuid = new ArrayList<>();
    public static ArrayList<String> alCustomers = new ArrayList<>();
    public static int TAB_POS_1 = 1;
    public static int TAB_POS_2 = 2;
    public static String EXTRA_SSRO_GUID = "extraSSROguid";
    public static String EXTRA_TAB_POS = "extraTabPos";
    public static String EXTRA_ORDER_DATE = "extraDate";
    public static String EXTRA_ORDER_IDS = "extraIDS";
    public static String EXTRA_ORDER_AMOUNT = "extraAmount";
    public static String EXTRA_ORDER_SATUS = "extraStatus";
    public static String EXTRA_ORDER_CURRENCY = "extraCurrency";
    public static HashSet<String> mSetTodayRouteSch = new HashSet<>();
    public static String RoutSchScope = "RoutSchScope";
    public static String InvoiceItems = "InvoiceItems";
    public static String PaymentMethod = "";
    public static String IssuingBank = "";
    public static int selectedIndex = 0;
    public static String ShippingTypeDesc = "ShippingTypeDesc";
    public static String TransporterID = "TransporterID";
    public static String TransporterName = "TransporterName";
    public static String PartnerVendorName = "PartnerVendorName";
    public static String PartnerVendorNo = "PartnerVendorNo";
    public static String Region = "Region";
    public static String StocksList = "StocksList";
    public static String CompetitorMasterInfo = "CompetitorMasterInfo";
    public static String EXTRA_CUSTOMER_REGION = "extraCustomerRegion";
    public static String ConditionAmtPer = "ConditionAmtPer";
    public static String ConditionAmtPerUOM = "ConditionAmtPerUOM";
    public static String ConditionTypeDesc = "ConditionTypeDesc";
    public static String ConditionBaseValue = "ConditionBaseValue";
    public static String ConditionValue = "ConditionValue";
    public static String TextID = "TextID";
    public static String DelSchLineNo = "DelSchLineNo";
    public static String ConfirmedQty = "ConfirmedQty";
    public static String RequiredQty = "RequiredQty";
    public static String SO_ORDER_VALUE = "";
    public static String SchemeID = "SchemeID";
    public static String ValidFrom = "ValidFrom";
    public static String ValidTo = "ValidTo";
    public static String DocumentID = "DocumentID";
    public static String DocumentSt = "DocumentStore";
    public static String Application = "Application";
    public static String Application1 = "Application";
    public static String Active = "Active";
    public static String Total = "Total";
    public static String Open = "Open";
    public static String OrderValue = "OrderValue";
    public static String DocumentLink = "DocumentLink";
    public static String DocumentName = "FileName";
    public static String FolderName = "VisualVid";
    public static String RoleID = "RoleID";
    public static String LoginName = "LoginName";
    public static String RoleDesc = "RoleDesc";
    public static String RoleCatID = "RoleCatID";
    public static String RoleCatDesc = "RoleCatDesc";
    public static String IsActive = "IsActive";
    public static String ERPLoginID = "ERPLoginID";
    public static String UserFunction1 = "UserFunction1";
    public static String UserFunction1Desc = "UserFunction1Desc";
    public static String UserFunction2 = "UserFunction2";
    public static String UserFunction2Desc = "UserFunction2Desc";
    public static String PotentialType = "PotentialType";
    public static String ChannelPartner = "ChannelPartner";
    public static String CampaignExpense = "CampaignExpense";
    public static String MobileNoSales = "MobileNo";
    public static String EXTRA_SCHEME_IS_SECONDTIME = "isSecondTime";
    public static String SC = "SC";
    public static String SS = "SS";
    public static String[][] customerArrData = null;
    public static String isMyTargetsEnabled = "isMyTargetsEnabled";
    public static String isMyTargetsTcode = "/ARTEC/SF_MYTRGTS";
    public static String isMatarialwiseEnabled = "isMatarialwiseEnabled";
    public static String isMatarialwiseTcode = "/ARTEC/SF_TRGT_MAT";
    public static String isStartCloseEnabled = "isStartCloseEnabled";
    public static String isStartCloseTcode = "/ARTEC/SF_ATTND";
    public static String isOutstandingEnabled = "isOutstandingHistory";
    public static String isOutStandingTcode = "/ARTEC/SF_OUTSTND";
    public static String isRouteEnabled = "isRouteEnabled";
    public static String isRoutePlaneTcode = "/ARTEC/SF_ROUTPLAN";
    public static String isInvHistoryEnabled = "isInvHistory";
    public static String isInvoiceHistoryTcode = "/ARTEC/SF_INVHIS";
    public static String isSOListEnabled = "isSOListEnabled";
    public static String isSOListTcode = "/ARTEC/SF_SOLIST";
    public static String isDaySummaryEnabled = "isDaySummaryEnabled";
    public static String isAdhocVisitEnabled = "isAdhocVisitEnabled";
    public static String isAlertsEnabled = "isAlertsEnabled";
    public static String isExpenseEntryEnabled = "isExpenseEntryEnabled";
    public static String isExpenseListEnabled = "isExpenseListEnabled";
    public static String isVisitSummaryEnabled = "isVisitSummaryEnabled";
    public static String isDigitalProductEnabled = "isDigitalProductEnabled";
    public static String isDaySummaryTcode = "/ARTEC/SF_DAYSMRY";
    public static String isAdhocVistTcode = "/ARTEC/SF_ADHOCVST";
    public static String isAlertTcode = "/ARTEC/SF_ALRT";
    public static String isExpEnteryTcode = "/ARTEC/SF_EXPCRT";
    public static String isExpListTcode = "/ARTEC/SF_EXPLIST";
    public static String isVisualAidsTcode = "/ARTEC/SF_VSULAID";
    public static String isVisitSummaryTcode = "/ARTEC/SF_VISTSMRY";
    public static String isCustomerListTcode = "/ARTEC/SF_CUST_LST";
    public static String isCustomerListEnabled = "isCustomerListEnabled";
    public static String isRTGSTcode = "/ARTEC/SF_COLLPLN_CRT";
    public static String isRTGSEnabled = "isRTGSEnabled";
    public static String isProspectiveCustomerListEnabled = "isProspectiveCustomerListEnabled";
    public static String isProspectiveCustomerListTcode = "/ARTEC/SF_PROSCUST_LST";
    public static String isSchemeKey = "isSchemeEnabled";
    public static String isSchemeTcode = "/ARTEC/SF_SCHEMES";
    public static String isDSREntryEnabled = "isDsrEntryEnabled";
    public static String isDSREntryTcode = "/ARTEC/SF_DSR_CRT";
    public static String isROListKey = "isROListEnabled";
    public static String isROLisTcode = "/ARTEC/SF_ROLIST";
    public static String isROListItemKey = "isROItemListEnabled";
    public static String isROLisItemTcode = "/ARTEC/SF_ROLIST01";
    public static String isRetailerEnabled = "isRetailerEnabled";
    public static String isRetailerTcode = "/ARTEC/SF_CP_LST";
    public static String isMTPEnabled = "isMTPEnabled";
    public static String isMTPTcode = "/ARTEC/SF_MTP_CRT";
    public static String isDealerBehaviourEnabled = "isDealerBehaviourEnabled";
    public static String isDealerBehaviourTcode = "/ARTEC/SF_SPCP_EVAL";
    public static String isMTPSubOrdinateEnabled = "isMTPSubOrdinateEnabled";
    public static String isMTPSubOrdinateTcode = "/ARTEC/SF_MTP_SUBLST";
    public static String isRTGSSubOrdinateEnabled = "isRTGSSubOrdinateEnabled";
    public static String isRTGSSubOrdinateTcode = "/ARTEC/SF_COLLPLN_SUBLST";
    public static String EXTRA_ARRAY_LIST = "arrayList";
    public static String Y = "Y";
    public static String N = "N";
    public static String WindowDisplayID = "11";
    public static String WindowDisplayClaimID = "13";
    public static String WindowDisplayValueHelp = "WindowDisplay";
    public static String CameraPackage = "android.media.action.IMAGE_CAPTURE";
    public static String MAXEXPALWD = "MAXEXPALWD";
    public static String MAXEXPALWM = "MAXEXPALWM";
    public static String SF = "SF";
    public static String AUTOSYNC = "AUTOSYNC";
    public static String GEOAUTOSYN = "GEOAUTOSYN ";
    public static String Expenses = "Expenses";
    public static String ExpenseItemDetails = "ExpenseItemDetails";
    public static String ExpenseDocuments = "ExpenseDocuments";
    public static String ExpenseEntity = ".Expense";
    public static String ExpenseItemEntity = ".ExpenseItemDetail";
    public static String ExpenseItemDocumentEntity = ".ExpenseItemDetail_ExpenseDocuments";
    public static String PlantStock = "PlantStocks";
    public static Bundle SOBundleExtras = null;
    public static boolean isDayStartSyncEnbled = false;
    public static int mErrorCount = 0;
    public static ArrayList<String> AL_ERROR_MSG = new ArrayList<>();
    public static Set<String> Entity_Set = new HashSet<>();
    public static SQLiteDatabase database;
    public static String DecisionKey = "DecisionKey";
    public static String Comments = "Comments";
    public static boolean mBoolIsReqResAval = false;
    public static boolean mBoolIsNetWorkNotAval = false;
    public static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    public static List<ODataEntity> oDataEntity = null;
    public static List<ODataEntity> oDataEntityRegion = null;
    public static List<ODataEntity> oDataEntityZone = null;
    public static List<ODataEntity> oDataEntitySD = null;
    public static List<ODataEntity> oDataEntityDist = null;
    public static String Geo1 = "Geo1";
    public static String Geo2 = "Geo2";
    public static ODataEntity oDataEntityRetailer = null;
    public static String IncoTerm1 = "IncoTerm1";
    public static String IncoTerm2 = "IncoTerm2";
    public static String IncoTerm1Desc = "IncoTerm1Desc";
    public static String PaymentTermDes = "PaymentTermDes";
    public static String isInvoiceItemsEnabled = "isInvoiceItemsEnabled";
    public static String InvoiceConditions = "InvoiceConditions";
    public static String ConditionAmt = "ConditionAmt";
    public static String isMaterialEnabled = "isMaterialEnabled";
    public static String SOITST = "SOITST";
    public static String RSFRJN = "RSFRJN";
    public static String SOConditions = "SOConditions";
    public static String ConditionPricingDate = "ConditionPricingDate";
    public static String CondCurrency = "CondCurrency";
    public static String ConditionTypeID = "ConditionTypeID";
    public static String ConditionAmount = "ConditionAmount";
    public static String NetWeight = "NetWeight";
    public static String NetWeightUom = "NetWeightUom";
    public static String TotalQuantity = "TotalQuantity";
    public static String QuantityUom = "QuantityUom";
    public static String NetWeightUOM = "NetWeightUOM";
    public static String RejectStatus = "02";
    public static String ApprovalStatus01 = "01";
    public static String RouteSchPlanGUID = "RouteSchPlanGUID";
    public static String SalesDistrict = "SalesDistrict";
    public static String WeekOfMonth = "WeekOfMonth";
    public static String ActivityID = "ActivityID";
    public static String ActivityDesc = "ActivityDesc";
    public static String RouteSchSPGUID = "RouteSchSPGUID";
    public static String IS_UPDATE = "isUpdate";
    public static String ApprovalStatusDs = "ApprovalStatusDs";
    public static String ApprovalStatus = "ApprovalStatus";
    public static String TLSD = "TLSD";
    public static String Conv_Mode_Type_Other = "0000000001";
    public static String CollectionPlanItem = "CollectionPlanItems";
    public static String COllectionPlanDate = "CollectionPlanDate";
    public static String CollectionPlanGUID = "CollectionPlanGUID";
    public static String CollectionPlanItemGUID = "CollectionPlanItemGUID";
    public static String CollectionType = "CollectionType";
    public static String PlannedValue = "PlannedValue";
    public static String AchievedValue = "AchievedValue";
    public static String SalesDistrictID = "SalesDistrictID";
    public static String ReturnOrders = "ReturnOrders";
    public static String ReturnOrderItemDetails = "ReturnOrderItemDetails";
    public static String ReturnOrderItems = "ReturnOrderItems";
    public static String CollectionPlanDate = "CollectionPlanDate";
    public static String CollectionPlanEntity = ".CollectionPlan";
    public static String CollectionPlanItemDetailEntity = ".CollectionPlanItemDetail";
    public static String KeyNo = "KeyNo";
    public static String KeyValue = "KeyValue";
    public static String KeyType = "KeyType";
    public static String DataVaultData = "DataVaultData";
    public static String DataVaultFileName = "mSFAGeoDataVault.txt";
    public static HashMap<String, ArrayList<String>> mapMatGrpBasedOnUOM = new HashMap<>();
    public static HashMap<String, String> mapMatGrpByMaterial = new HashMap<>();
    public static boolean isComingFromDashBoard = false;
    public static ReentrantLock reentrantLock = null;
    public static String colName = "";
    public static String StrSPGUID32 = "";
    public static String parternTypeID = "";
    public static AlertDialog.Builder builder = null;
    public static boolean writeDebug = false;
    static ArrayList<Hashtable<String, String>> itemtable = null;
    private static volatile String SALES_PERSON_GUID = "";
    private static HashMap<String, String> mapTable;
    private static int HOUR_PM = 0;
    private static int ZERO_MINUTES = 0;
    public static boolean ReIntilizeStore = false;
    public static String ValidatedDate = "ValidatedDate";



    public static void storeInDataVault(String docNo, String jsonHeaderObjectAsString) {
        try {
            LogonCore.getInstance().addObjectToStore(docNo, jsonHeaderObjectAsString);
        } catch (LogonCoreException var3) {
            var3.printStackTrace();
        }

    }

    public static final void createDB(SQLiteDatabase db) {
        String sql = "create table if not exists "
                + Constants.DATABASE_REGISTRATION_TABLE
                + "( username  text, password   text,repassword text,themeId text,mainView text);";
        Log.d("EventsData", "onCreate: " + sql);
        db.execSQL(sql);
    }

    public static final void insertHistoryDB(SQLiteDatabase db, String tblName, String clmname, String value) {
        String sql = "INSERT INTO " + tblName + "( " + clmname + ") VALUES('"
                + value + "') ;";
        db.execSQL(sql);
    }

    public static final void updateStatus(SQLiteDatabase db, String tblName, String clmname, String value, String inspectionLot) {
        String sql = "UPDATE " + tblName + " SET  " + clmname + "='" + value
                + "' Where Collections = '" + inspectionLot + "';";
        db.execSQL(sql);
    }

    public static final void createTable(SQLiteDatabase db, String tableName, String clumsname) {
        try {
            String sql = Constants.create_table + tableName
                    + " ( " + clumsname + ", Status text );";
            Log.d(Constants.EventsData, Constants.on_Create + sql);
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void deleteTable(SQLiteDatabase db, String tableName) {
        try {
            String delSql = Constants.delete_from + tableName;
            db.execSQL(delSql);

        } catch (Exception e) {
            System.out.println("createTableKey(EventDataSqlHelper): " + e.getMessage());
        }
    }

    public static boolean getRollID(Context context) {
        boolean rollID = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        String rollType = sharedPreferences.getString(Constants.USERROLE, "");
        if (rollType.equalsIgnoreCase("Z5")) {
            rollID = true;
        }
        return rollID;
    }

    public static boolean restartApp(Activity activity) {
        LogonCoreContext lgCtx1 = null;
        try {
            lgCtx1 = LogonCore.getInstance().getLogonContext();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lgCtx1 == null) {

            SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.PREFS_NAME, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isAppRestart", true);
            editor.commit();
            Log.e("Restart", "Called");
            activity.finishAffinity();
            Intent dialogIntent = new Intent(activity, RegistrationActivity.class);
            activity.startActivity(dialogIntent);
        } else {
            return false;

        }
        return true;
    }

    public static boolean syncHistoryTableExist() {
        return Constants.events.syncHistoryTableExist(Constants.SYNC_TABLE);
    }
    public static void createSyncDatabase(Context context) {
        Hashtable hashtable = new Hashtable<>();
        hashtable.put(Constants.SyncGroup, "");
        hashtable.put(Constants.Collections, "");
        hashtable.put(Constants.TimeStamp, "");
        try {
            Constants.events.crateTableConfig(Constants.SYNC_TABLE, hashtable);
            getSyncHistoryTable(context);
        } catch (Exception e) {
            LogManager.writeLogError(Constants.error_creating_sync_db
                    + e.getMessage());
        }
    }

    /*Sync History table for Sync*/
    public static void getSyncHistoryTable(Context context) {
        String[] definingReqArray = Constants.getDefinigReq(context);
        for (int i = 0; i < definingReqArray.length; i++) {
            String colName = definingReqArray[i];
            if (colName.contains("?$")) {
                String splitCollName[] = colName.split("\\?");
                colName = splitCollName[0];
            }
            try {
                Constants.events.inserthistortTable(Constants.SYNC_TABLE, "",
                        Constants.Collections, colName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

       // createprospectedCustomerDatabase();
    }
    public static ErrorBean getErrorCode(int operation, Exception exception, Context context) {
        ErrorBean errorBean = new ErrorBean();
        try {
            int errorCode = 0;
            boolean hasNoError = true;
            if ((operation == Operation.Create.getValue())) {

                try {
                    // below error code getting from online manger (While posting data vault data)
//                    errorCode = ((ErrnoException) ((ODataNetworkException) exception).getCause().getCause()).errno;
                    Throwable throwables = (((ODataNetworkException) exception).getCause()).getCause().getCause();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (throwables instanceof ErrnoException) {
                            errorCode = ((ErrnoException) throwables).errno;
                        } else {
                            if (exception.getMessage().contains(Constants.Unothorized_Error_Name)) {
                                errorCode = Constants.UnAuthorized_Error_Code;
                                hasNoError = false;
                            } else if (exception.getMessage().contains(Constants.Comm_error_name)) {
                                hasNoError = false;
                                errorCode = Constants.Comm_Error_Code;
                            } else if (exception.getMessage().contains(Constants.Network_Name)) {
                                hasNoError = false;
                                errorCode = Constants.Network_Error_Code;
                            } else {
                                Constants.ErrorNo = 0;
                            }
                        }
                    } else {
                        try {
                            if (exception.getMessage() != null) {
                                if (exception.getMessage().contains(Constants.Unothorized_Error_Name)) {
                                    errorCode = Constants.UnAuthorized_Error_Code;
                                    hasNoError = false;
                                } else if (exception.getMessage().contains(Constants.Comm_error_name)) {
                                    hasNoError = false;
                                    errorCode = Constants.Comm_Error_Code;
                                } else if (exception.getMessage().contains(Constants.Network_Name)) {
                                    hasNoError = false;
                                    errorCode = Constants.Network_Error_Code;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    if (errorCode != Constants.UnAuthorized_Error_Code) {
                        if (errorCode == Constants.Network_Error_Code || errorCode == Constants.Comm_Error_Code) {
                            hasNoError = false;
                        } else {
                            hasNoError = true;
                        }
                    }
                } catch (Exception e1) {
                    if (exception.getMessage().contains(Constants.Unothorized_Error_Name)) {
                        errorCode = Constants.UnAuthorized_Error_Code;
                        hasNoError = false;
                    } else {
                        Constants.ErrorNo = 0;
                    }
                }
                LogManager.writeLogError("Error : [" + errorCode + "]" + exception.getMessage());

            } else if (operation == Operation.OfflineFlush.getValue() || operation == Operation.OfflineRefresh.getValue() || operation == Operation.GetRequest.getValue()) {
                try {
//                    if (exception instanceof ODataContractViolationException){
//                        errorCode = ((ODataOfflineException) ((ODataContractViolationException) exception).getCause()).getCode();
//                    }else {
                    // below error code getting from offline manger (While posting flush and refresh collection)
                    errorCode = ((ODataOfflineException) ((ODataNetworkException) exception).getCause()).getCode();
//                    }
                    // Display popup for Communication and Unauthorized errors
                    if (errorCode == Constants.Network_Error_Code_Offline
                            || errorCode == Constants.UnAuthorized_Error_Code_Offline
                            || errorCode == Constants.Unable_to_reach_server_offline
                            || errorCode == Constants.Resource_not_found
                            || errorCode == Constants.Unable_to_reach_server_failed_offline) {

                        hasNoError = false;
                    } else {
                        hasNoError = true;
                    }

                } catch (Exception e) {
                    try {
                        String mStrErrMsg = exception.getCause().getLocalizedMessage();
                        if (mStrErrMsg.contains(Executing_SQL_Commnd_Error)) {
                            hasNoError = false;
                            errorCode = -10001;
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                if (errorCode != 0) {
                    LogManager.writeLogError("Error : [" + errorCode + "]" + exception.getMessage());
                }
            } else if (operation == Operation.GetStoreOpen.getValue()) {
                // below error code getting from offline manger (While posting flush and refresh collection)
                try {
//                    if (exception instanceof ODataContractViolationException){
//                        errorCode = ((ODataOfflineException) ((ODataContractViolationException) exception).getCause()).getCode();
//                    }else {
                    errorCode = ((ODataOfflineException) ((ODataNetworkException) exception).getCause()).getCode();
//                    }
                    // Display popup for Communication and Unauthorized errors
                    if (errorCode == Constants.Network_Error_Code_Offline
                            || errorCode == Constants.UnAuthorized_Error_Code_Offline
                            || errorCode == Constants.Unable_to_reach_server_offline
                            || errorCode == Constants.Resource_not_found
                            || errorCode == Constants.Unable_to_reach_server_failed_offline) {

                        hasNoError = false;
                    } else {
                        hasNoError = true;
                    }
                } catch (Exception e) {
                    try {
                        String mStrErrMsg = exception.getCause().getLocalizedMessage();
                        if (mStrErrMsg.contains(Store_Defining_Req_Not_Matched)) {
                            hasNoError = false;
                            errorCode = -10247;
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }

            errorBean.setErrorCode(errorCode);
            if (exception.getMessage() != null && !exception.getMessage().equalsIgnoreCase("")) {
                errorBean.setErrorMsg(exception.getMessage());
            } else {
                errorBean.setErrorMsg(context.getString(R.string.unknown_error));
            }

            errorBean.setHasNoError(hasNoError);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (errorBean.getErrorMsg().contains(Constants.Build_Database_Failed_Error_Code1 + "")
                || errorBean.getErrorMsg().contains(Constants.Build_Database_Failed_Error_Code2 + "")
                || errorBean.getErrorMsg().contains(Constants.Build_Database_Failed_Error_Code3 + "")
                || errorBean.getErrorCode() == Constants.Execu_SQL_Error_Code
                || errorBean.getErrorCode() == Constants.Store_Def_Not_matched_Code) {
            if (errorBean.getErrorMsg().contains("500")
                    || errorBean.getErrorMsg().contains(Constants.RFC_ERROR_CODE_100029)
                    || errorBean.getErrorMsg().contains(Constants.RFC_ERROR_CODE_100027)) {
                errorBean.setStoreFailed(false);
            } else {
                errorBean.setStoreFailed(true);
            }

        } else {
            errorBean.setStoreFailed(false);
        }


//        }
        if (errorBean.isStoreFailed()) {
            try {
                UtilConstants.closeStore(context,
                        OfflineManager.options, errorBean.getErrorMsg() + "",
                        offlineStore, Constants.PREFS_NAME, errorBean.getErrorCode() + "");

            } catch (Exception e) {
                e.printStackTrace();
            }
            Constants.Entity_Set.clear();
            Constants.AL_ERROR_MSG.clear();
            offlineStore = null;
            OfflineManager.options = null;
        }
        errorBean.setErrorMsg(makecustomHttpErrormessage(errorBean.getErrorMsg()));
        int httperrorCode = makecustomHttpErrorCode(errorBean.getErrorMsg());
        if (httperrorCode != 0)
            errorBean.setErrorCode(httperrorCode);
        return errorBean;
    }
    public static int makecustomHttpErrorCode(String error_msg) {
        httphashmaperrorcodes();
        String httperrorcode = "";
        int code = 0;
        if (!TextUtils.isEmpty(error_msg) && error_msg.contains("HTTP code")) {
            List<String> errorList = Arrays.asList(error_msg.split(","));
            if (errorList.size() > 0) {

                for (String data : errorList) {
                    Iterator<String> keySetIterator = httpErrorCodes.keySet().iterator();
                    while (keySetIterator.hasNext()) {
                        String key = keySetIterator.next();
                        if (data.contains(key)) {
                            httperrorcode = httpErrorCodes.get(key) + "-" + key;
                            break;
                        }

                    }
                }

            }


        }
        if (!TextUtils.isEmpty(httperrorcode)) {
            try {
                code = Integer.parseInt(httperrorcode);
            } catch (Exception e) {
                e.printStackTrace();
                code = 0;
            }
        }
        return code;

    }

    public static String makecustomHttpErrormessage(String error_msg) {
        String[] DEFINGREQARRAY = {"Attendances",
                "UserProfileAuthSet?$filter=Application%20eq%20%27PD%27",
                //"SPGeos",/*SyncHistorys,UserPartners,*/
                "Customers", KPISet, Targets, TargetItems, KPIItems,
                "SalesPersons", OrderMaterialGroups, Brands, PlantStocks, UserSalesPersons,
                CustomerPartnerFunctions,
                "MPerformances?$filter= PerformanceTypeID eq '000006' and AggregationLevelID eq '01'",
                "CustomerCreditLimits", UserCustomers, CustomerSalesAreas, "MaterialSaleAreas",
                "Alerts?$filter=Application eq 'PD'",
                "Invoices", "InvoiceItemDetails", InvoicePartnerFunctions,
                "VisitActivities", "Visits", "InvoiceItems", "InvoiceConditions",
                "RoutePlans", "RouteSchedulePlans", "RouteSchedules",
                "CollectionPlans", "CollectionPlanItems", CollectionPlanItemDetails,
                "SOItems", "SOs", "SOConditions", "SOItemDetails", "SOTexts?$filter=TextCategory eq 'H' or TextCategory eq 'I'",
                "MSPChannelEvaluationList?$filter=ApplicationID eq 'SF'",
                ReturnOrderItems, ReturnOrderItemDetails, ReturnOrders,
                Collections, Stocks,
                "ConfigTypesetTypes?$filter=Typeset eq 'DELVST' or Typeset eq 'INVST' " +
                        "or Typeset eq 'REJRSN' or Typeset eq 'UOMNO0' or Typeset eq 'OINVAG' or Typeset eq 'SOITST' or Typeset eq 'EVLTYP' or Typeset eq 'CRDCTL' or Typeset eq 'RODLST' or Typeset eq 'ROGRST'",
                "ConfigTypsetTypeValues?$filter=Typeset eq 'PD' or " +
                        "Typeset eq 'ATTTYP' or Typeset eq 'RVWTYP' or Typeset eq 'FIPRTY' " +
                        "or Typeset eq 'ACTTYP' or Typeset eq 'SF' or Typeset eq 'SC' or Typeset eq 'SS' or Typeset eq 'SP' ",
                "MaterialByCustomers",
                "ValueHelps?$filter=ModelID eq 'SFGW_ALL' and (EntityType eq 'ExpenseItemDetail' " +
                        "or EntityType eq 'SO' or EntityType eq 'ExpenseConfig' or EntityType eq 'Campaign' or EntityType eq 'ChannelPartner' or EntityType eq 'Feedback' or EntityType eq 'Complaints' or EntityType eq 'Collection' or EntityType eq 'Attendance') and (PropName eq 'Plant' " +
                        "or PropName eq 'Location' or PropName eq 'CampaignStatus' or PropName eq 'ConvenyanceMode' or PropName eq 'OrderType' " +
                        "or PropName eq 'Incoterm1' or PropName eq 'CampaignType' or PropName eq 'BudgetType' or PropName eq 'CampaignVenue' or PropName eq 'DMSDiv' or PropName eq 'BlockID' or PropName eq 'CampaignExpenses' or PropName eq 'ConstructionType' or PropName eq 'ConstructionStageID' or PropName eq 'Payterm' or PropName eq 'CPTypeID' or PropName eq 'PotentialType' or PropName eq 'FeedbackType' or PropName eq 'FeedbackSubType' or PropName eq 'ShippingTypeID' or PropName eq 'MeansOfTranstyp' or PropName eq 'CollectionTypeID' or PropName eq 'PaymentMethodID' or PropName eq 'SalesDistrict' or PropName eq 'Route' or PropName eq 'SplProcessing' or PropName eq 'PriceList' or PropName eq 'MatFrgtGrp' or PropName eq 'StorageLoc') "

        };
        httphashmaperrorcodes();
        if (!TextUtils.isEmpty(error_msg) && error_msg.contains("HTTP code")) {
            String make_message = "";
            List<String> errorList = Arrays.asList(error_msg.split(","));
            if (errorList.size() > 0) {
                for (int i = 0; i < DEFINGREQARRAY.length; i++) {
                    if (error_msg.contains(DEFINGREQARRAY[i])) {
                        make_message = DEFINGREQARRAY[i] + ":";
                        break;
                    }
                }
                String httperrormsg = "";
                for (String data : errorList) {
                    Iterator<String> keySetIterator = httpErrorCodes.keySet().iterator();
                    while (keySetIterator.hasNext()) {
                        String key = keySetIterator.next();
                        if (data.contains(key)) {
                            httperrormsg = httpErrorCodes.get(key) + "-" + key;
                            break;
                        }

                    }
                }
                if (!TextUtils.isEmpty(httperrormsg))
                    make_message += httperrormsg + " Please contact channel team";
                else make_message = error_msg;
            } else {
                make_message = error_msg;
            }
            return make_message;

        }
        return error_msg;

    }


    public static void httphashmaperrorcodes() {
        //500 series
        httpErrorCodes.put("500", "Connection Timeout");
        httpErrorCodes.put("501", "Not Implemented");
        httpErrorCodes.put("502", "Bad Gateway");
        httpErrorCodes.put("503", "Service Unavailable");
        httpErrorCodes.put("504", "Gateway Timeout");
        httpErrorCodes.put("505", "HTTP Version Not Supported");
        httpErrorCodes.put("506", "Variant Also Negotiates");
        httpErrorCodes.put("507", "Insufficient Storage");
        httpErrorCodes.put("508", "Loop Detected");
        httpErrorCodes.put("509", "Unassigned");
        httpErrorCodes.put("510", "Not Extended");
        httpErrorCodes.put("511", "Network Authentication Required");
        //400 series
        httpErrorCodes.put("400", "Bad Request");
        httpErrorCodes.put("401", "Unauthorized");
        httpErrorCodes.put("402", "Payment Required");
        httpErrorCodes.put("403", "Forbidden");
        httpErrorCodes.put("404", "Not Found");
        httpErrorCodes.put("405", "Method Not Allowed");
        httpErrorCodes.put("406", "Not Acceptable");
        httpErrorCodes.put("407", "Proxy Authentication Required");
        httpErrorCodes.put("408", "Request Timeout");
        httpErrorCodes.put("409", "Conflict");
        httpErrorCodes.put("410", "Gone");
        httpErrorCodes.put("411", "Length Required");
        httpErrorCodes.put("412", "Precondition Failed");
        httpErrorCodes.put("413", "Payload Too Large");
        httpErrorCodes.put("414", "URI Too Long");
        httpErrorCodes.put("415", "Unsupported Media Type");
        httpErrorCodes.put("416", "Range Not Satisfiable");
        httpErrorCodes.put("417", "Expectation Failed");
        httpErrorCodes.put("421", "Misdirected Request");
        httpErrorCodes.put("422", "Unprocessable Entity");
        httpErrorCodes.put("423", "Locked");
        httpErrorCodes.put("424", "Failed Dependency");
        httpErrorCodes.put("425", "Too Early");
        httpErrorCodes.put("426", "Upgrade Required");
        httpErrorCodes.put("427", "Unassigned");
        httpErrorCodes.put("428", "Precondition Required");
        httpErrorCodes.put("429", "Too Many Requests");
        httpErrorCodes.put("430", "Unassigned");
        httpErrorCodes.put("431", "Request Header Fields Too Large");
        httpErrorCodes.put("451", "Unavailable For Legal Reasons");
    }

    public static String checkUnknownNetworkerror(String errorMsg,Context mcontext) {
        String customErrorMsg="";
        if(!TextUtils.isEmpty(errorMsg)) {
            if (errorMsg.contains("10346"))
                customErrorMsg = mcontext.getString(R.string.error_10346);
            else if (errorMsg.contains("10349"))
                customErrorMsg = mcontext.getString(R.string.error_10349);
            else if (errorMsg.contains("10348"))
                customErrorMsg = mcontext.getString(R.string.error_10348);
            else if (errorMsg.contains("10345"))
                customErrorMsg = mcontext.getString(R.string.error_10345);
            else if (errorMsg.contains("10065"))
                customErrorMsg = mcontext.getString(R.string.error_10065);
        }
        return customErrorMsg;
    }
    public static String getSPGUID(final String columnName) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SALES_PERSON_GUID = OfflineManager.getGuidValueByColumnName(Constants.UserSalesPersons + "?$select=" + columnName, columnName);
                } catch (OfflineODataStoreException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return SALES_PERSON_GUID;
    }

    public static boolean getSyncHistoryColl(Context context) {
        boolean check = Arrays.asList(Constants.getDefinigReq(context)).contains(Constants.SyncHistorys);
        LogManager.writeLogInfo("Checking Sync history:" + String.valueOf(check));
        return check;
    }

    public static String getSyncHistoryddmmyyyyTime() {
        String currentDateTimeString1 = (String) android.text.format.DateFormat.format("dd/MM/yyyy", new Date());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String currentDateTimeString2 = dateFormat.format(new Date());
        String currentDateTimeString = currentDateTimeString1 + "T" + currentDateTimeString2;
        return currentDateTimeString1 + " " + currentDateTimeString2;
    }

    public static void updateSyncTime(final List<String> alAssignColl, final Context context, final String syncType) {
        String strSPGUID = Constants.getSPGUID(Constants.SPGUID);
//        String StrSPGUID32 = "";
//        String parternTypeID = "";

        if (!TextUtils.isEmpty(strSPGUID)) {
            StrSPGUID32 = strSPGUID.replaceAll("-", "");
            try {
                parternTypeID = OfflineManager.getPartnerTypeID(Constants.UserPartners + "?$filter= PartnerID eq'" + StrSPGUID32 + "'");
            } catch (OfflineODataStoreException e) {
                e.printStackTrace();
                LogManager.writeLogInfo(" updating sync history : exception" + e.getLocalizedMessage());
            }
        }

        final boolean checkSyncHistoryColl = getSyncHistoryColl(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        final String loginId = sharedPreferences.getString("username", "");
        final String syncTime = Constants.getSyncHistoryddmmyyyyTime();
        final ODataRequestParamBatch requestParamBatch = new ODataRequestParamBatchDefaultImpl();
        LogManager.writeLogInfo(" updating sync history :" + syncTime);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (reentrantLock == null) {
                    reentrantLock = new ReentrantLock();
                }
                reentrantLock.lock();
                Log.e("Sync Histroy REENTRANT:", "LOCKED");
                try {
//                    itemtable = new ArrayList<>();
                    for (int incReq = 0; incReq < alAssignColl.size(); incReq++) {
                        colName = alAssignColl.get(incReq);
                        if (colName.contains("?$")) {
                            String splitCollName[] = colName.split("\\?");
                            colName = splitCollName[0];
                            LogManager.writeLogInfo(" collection names :" + colName);
                        }
                        Constants.events.updateStatus(Constants.SYNC_TABLE,
                                colName, Constants.TimeStamp, syncTime
                        );


                        if (checkSyncHistoryColl) {
                            try {
//                                Constants.createSyncHistory(colName, syncTime, syncType, StrSPGUID32, parternTypeID, loginId);
                                Hashtable hashtable = Constants.createSyncHistoryBatch(colName, syncTime, syncType, StrSPGUID32, parternTypeID, loginId);
                                Log.d("SH Offline","insert RefGuid:-"+hashtable.get(Constants.SyncHisGuid)+"--"+colName);

                                ODataEntity channelPartnerEntity = null;
                                try {
                                    channelPartnerEntity = OfflineManager.createSyncHistroyEntity(hashtable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                int id = incReq + 1;
                                String contentId = String.valueOf(id);
                                ODataRequestParamSingle batchItem = new ODataRequestParamSingleDefaultImpl();
                                // Create change set
                                batchItem.setPayload(channelPartnerEntity);
                                batchItem.setMode(ODataRequestParamSingle.Mode.Create);
                                batchItem.setResourcePath(Constants.SyncHistroy);
                                batchItem.setContentID(contentId);
                             /*   HashMap<String, String> map = new HashMap<>();
                                map.put("OfflineOData.RemoveAfterUpload", "true");
                                batchItem.getCustomHeaders().putAll(map);*/
                                // batchItem.setOptions(map);

                                Map<String, String> createHeaders = new HashMap<String, String>();
                                createHeaders.put("OfflineOData.RemoveAfterUpload", "true");
                                batchItem.getCustomHeaders().putAll(createHeaders);

                                ODataRequestChangeSet changeSetItem = new ODataRequestChangeSetDefaultImpl();
                                changeSetItem.add(batchItem);
                                try {
                                    requestParamBatch.add(changeSetItem);
                                } catch (ODataException e) {
                                    e.printStackTrace();
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        offlineStore.executeRequest(requestParamBatch);
                    } catch (Exception e) {
                        try {
                            throw new OfflineODataStoreException(e);
                        } catch (OfflineODataStoreException e1) {
                            e1.printStackTrace();
                        }
                    }
                    try {
                        updateStartSyncTime(context, syncType, Constants.EndSync);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
                        reentrantLock.unlock();
                    }
                    Log.e("Sync Histroy EXCEPTION", "ANR EXCEPTION OCCURRED");
                } finally {
                    if (reentrantLock != null && reentrantLock.isHeldByCurrentThread())
                        reentrantLock.unlock();
                    Log.e("Sync Histroy REENTRANT:", "UNLOCKED FINALLY");
                }
            }
        }).start();

    }

    public static Hashtable createSyncHistoryBatch(String collectionName, String syncTime, String syncType, String StrSPGUID32, String parternTypeID, String loginId) {
        Hashtable hshtable = new Hashtable();
        try {
            Thread.sleep(100);

            if (collectionName.equalsIgnoreCase("ConfigTypsetTypeValues") && syncType.equals(Constants.UpLoad)) {
                syncType = Constants.DownLoad;
            }

            GUID guid = GUID.newRandom();
            hshtable.put(Constants.SyncHisGuid, guid.toString().toUpperCase());
            if (!collectionName.equals("") && collectionName != null) {
                hshtable.put(Constants.SyncCollection, collectionName);
            }
            hshtable.put(Constants.SyncApplication, BuildConfig.APPLICATION_ID);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
            String time = "";
            String strDate = "";
            try {
                Date date = dateFormat.parse(syncTime);
                strDate = dateFormat.format(date);
                time = timeFormat.format(date.parse(syncTime));

            } catch (ParseException ex) {
                ex.printStackTrace();
                Log.v("Exception", ex.getLocalizedMessage());
            }
            ODataDuration startDuration = null;
            try {
                if (!time.isEmpty()) {
                    startDuration = Constants.getTimeAsODataDurationConvertion(time);
                    hshtable.put(Constants.SyncHisTime, startDuration);
                } else {
                    hshtable.put(Constants.SyncHisTime, startDuration);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            hshtable.put(Constants.SyncDate, strDate);

            hshtable.put(Constants.SyncType, syncType);

            hshtable.put(Constants.PartnerId, StrSPGUID32);
            hshtable.put(Constants.PartnerType, parternTypeID);
            hshtable.put(Constants.LoginId, loginId);
//            hshtable.put(Constants.Remarks,getDeviceName() + " (" + mapTable.get(Constants.AppVisibility) + ")");
//            OfflineManager.CreateSyncHistroy(hshtable);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hshtable;
    }
    public static ODataDuration getTimeAsODataDurationConvertion(String timeString) {
        List<String> timeDuration = Arrays.asList(timeString.split("-"));
        int hour = Integer.parseInt((String) timeDuration.get(0));
        int minute = Integer.parseInt((String) timeDuration.get(1));
        int seconds = Integer.parseInt((String) timeDuration.get(2));
        ODataDurationDefaultImpl oDataDuration = null;

        try {
            oDataDuration = new ODataDurationDefaultImpl();
            oDataDuration.setHours(hour);
            oDataDuration.setMinutes(minute);
            oDataDuration.setSeconds(BigDecimal.valueOf((long) seconds));
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return oDataDuration;
    }

    public static void updateStartSyncTime(Context context, final String syncType, final String syncMsg) {
        String strSPGUID = Constants.getSPGUID(Constants.SPGUID);
//        String StrSPGUID32 = "";
//        String parternTypeID = "";

        if (!TextUtils.isEmpty(strSPGUID)) {
            StrSPGUID32 = strSPGUID.replaceAll("-", "");
            try {
                parternTypeID = OfflineManager.getPartnerTypeID(Constants.UserPartners + "?$filter= PartnerID eq'" + StrSPGUID32 + "'");
            } catch (OfflineODataStoreException e) {
                e.printStackTrace();
            }
        }

        final boolean checkSyncHistoryColl = getSyncHistoryColl(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        final String loginId = sharedPreferences.getString("username", "");
        String syncTime = "";
        if (syncMsg.equalsIgnoreCase(Constants.StartSync)) {
            syncTime = Constants.getSyncHistoryddmmyyyyTime();
        } else {
            syncTime = Constants.getSyncHistoryddmmyyyyTimeDelay();
        }
        final String finalSyncTime = syncTime;

        if (Constants.writeDebug)
            LogManager.writeLogDebug("Dashboard refresh Sync time : db update : " + syncTime);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (reentrantLock == null) {
                    reentrantLock = new ReentrantLock();
                }
                reentrantLock.lock();
                Log.e("Sync Histroy REENTRANT:", "LOCKED");
                String startColl = "";
                try {
                    try {
                        if (syncMsg.equalsIgnoreCase(Constants.StartSync)) {
                            if (syncType.equalsIgnoreCase(Constants.Sync_All)) {
                                startColl = "All Download Start";
                            } else if (syncType.equalsIgnoreCase(Constants.DownLoad)) {
                                startColl = "Download Start";
                            } else if (syncType.equalsIgnoreCase(Constants.UpLoad)) {
                                startColl = "Upload Start";
                            } else if (syncType.equalsIgnoreCase(Constants.Auto_Sync)) {
                                startColl = "Auto Sync Start";
                            }
                        } else {
                            if (syncType.equalsIgnoreCase(Constants.Sync_All)) {
                                startColl = "All Download End";
                            } else if (syncType.equalsIgnoreCase(Constants.DownLoad)) {
                                startColl = "Download End";
                            } else if (syncType.equalsIgnoreCase(Constants.UpLoad)) {
                                startColl = "Upload End";
                            } else if (syncType.equalsIgnoreCase(Constants.Auto_Sync)) {
                                startColl = "Auto Sync End";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (checkSyncHistoryColl) {
                        try {
                            Constants.createSyncHistory(startColl, finalSyncTime, syncType, StrSPGUID32, parternTypeID, loginId);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
                        reentrantLock.unlock();
                    }
                    if (Constants.writeDebug)
                        LogManager.writeLogInfo("Sync  Execption :" + e.getLocalizedMessage());
                    Log.e("Sync  Execption", "ANR EXCEPTION OCCURRED");
                } finally {
                    if (reentrantLock != null && reentrantLock.isHeldByCurrentThread())
                        reentrantLock.unlock();
                    Log.e("Sync Histroy REENTRANT:", "UNLOCKED FINALLY");
                }
            }
        }).start();

    }
    public static String getSyncHistoryddmmyyyyTimeDelay() {
        String currentDateTimeString1 = (String) android.text.format.DateFormat.format("dd/MM/yyyy", new Date());
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, 2);
        Date date = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String currentDateTimeString2 = dateFormat.format(date);

        String currentDateTimeString = currentDateTimeString1 + "T" + currentDateTimeString2;
        return currentDateTimeString1 + " " + currentDateTimeString2;
    }
    public static void createSyncHistory(String collectionName, String syncTime, String syncType, String StrSPGUID32, String parternTypeID, String loginId) {
        try {
            Thread.sleep(100);
            if (collectionName.equalsIgnoreCase("ConfigTypsetTypeValues") && syncType.equals(Constants.UpLoad)) {
                syncType = Constants.DownLoad;
            }

            GUID guid = GUID.newRandom();
            Hashtable hashtable = new Hashtable();
            hashtable.put(Constants.SyncHisGuid, guid.toString().toUpperCase());
            if (!collectionName.equals("") && collectionName != null) {
                hashtable.put(Constants.SyncCollection, collectionName);

                if (Constants.writeDebug)
                    LogManager.writeLogDebug("Create Sync history : " + collectionName);
            }
            hashtable.put(Constants.SyncApplication, BuildConfig.APPLICATION_ID);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
            String time = "";
            String strDate = "";
            try {
                Date date = dateFormat.parse(syncTime);
                strDate = dateFormat.format(date);
                time = timeFormat.format(date.parse(syncTime));

            } catch (ParseException ex) {
                ex.printStackTrace();
                Log.v("Exception", ex.getLocalizedMessage());

                LogManager.writeLogDebug("Creating Time exception:" + ex.getLocalizedMessage());
            }
            ODataDuration startDuration = null;
            try {
                if (!time.isEmpty()) {
                    startDuration = Constants.getTimeAsODataDurationConvertion(time);
                    hashtable.put(Constants.SyncHisTime, startDuration);
                } else {
                    hashtable.put(Constants.SyncHisTime, startDuration);
                }


            } catch (Exception e) {
                e.printStackTrace();
                LogManager.writeLogDebug("Creating startDuration exception:" + e.getLocalizedMessage());
            }
            hashtable.put(Constants.SyncDate, strDate);

            hashtable.put(Constants.SyncType, syncType);

            hashtable.put(Constants.PartnerId, StrSPGUID32);
            hashtable.put(Constants.PartnerType, parternTypeID);
            hashtable.put(Constants.LoginId, loginId);
//            hashtable.put(Constants.Remarks,getDeviceName() + " (" + mapTable.get(Constants.AppVisibility) + ")");
            OfflineManager.CreateSyncHistroy(hashtable);
//            itemtable.add(hashtable);
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.writeLogDebug("Create Sync history failed: " + e.getLocalizedMessage());
        }
    }
    public static String makeMsgReqError(int errorCode, Context context, boolean isInvError) {
        String mStrErrorMsg = "";

        if (!isInvError) {
            if (errorCode == Constants.UnAuthorized_Error_Code || errorCode == Constants.UnAuthorized_Error_Code_Offline) {
                mStrErrorMsg = context.getString(R.string.auth_fail_plz_contact_admin, errorCode + "");
            } else if (errorCode == Constants.Unable_to_reach_server_offline || errorCode == Constants.Network_Error_Code_Offline) {
                mStrErrorMsg = context.getString(R.string.data_conn_lost_during_sync_error_code, errorCode + "");
            } else if (errorCode == Constants.Resource_not_found) {
                mStrErrorMsg = context.getString(R.string.techincal_error_plz_contact, errorCode + "");
            } else if (errorCode == Constants.Unable_to_reach_server_failed_offline) {
                mStrErrorMsg = context.getString(R.string.comm_error_server_failed_plz_contact, errorCode + "");
            } else {
                mStrErrorMsg = context.getString(R.string.data_conn_lost_during_sync_error_code, errorCode + "");
            }
        } else {
            if (errorCode == 4) {
                mStrErrorMsg = context.getString(R.string.auth_fail_plz_contact_admin, Constants.UnAuthorized_Error_Code + "");
            } else if (errorCode == 3) {
                mStrErrorMsg = context.getString(R.string.data_conn_lost_during_sync_error_code, Constants.Network_Error_Code + "");
            } else {
                mStrErrorMsg = context.getString(R.string.data_conn_lost_during_sync_error_code, Constants.Network_Error_Code + "");
            }
        }

        return mStrErrorMsg;
    }
    public static void displayMsgReqError(int errorCode, Context context) {
        if (errorCode == Constants.UnAuthorized_Error_Code || errorCode == Constants.UnAuthorized_Error_Code_Offline) {
            if (errorCode == Constants.UnAuthorized_Error_Code_Offline) {
                String errorMessage = Constants.PasswordExpiredMsg;
                UtilConstants.showAlert(errorMessage, context);
            } else {
                UtilConstants.showAlert(context.getString(R.string.auth_fail_plz_contact_admin, errorCode + ""), context);
            }
//            UtilConstants.showAlert(context.getString(R.string.auth_fail_plz_contact_admin,errorCode+""), context);
        } else if (errorCode == Constants.Unable_to_reach_server_offline || errorCode == Constants.Network_Error_Code_Offline) {
            UtilConstants.showAlert(context.getString(R.string.data_conn_lost_during_sync_error_code, errorCode + ""), context);
        } else if (errorCode == Constants.Resource_not_found) {
            UtilConstants.showAlert(context.getString(R.string.techincal_error_plz_contact, errorCode + ""), context);
        } else if (errorCode == Constants.Unable_to_reach_server_failed_offline) {
            UtilConstants.showAlert(context.getString(R.string.comm_error_server_failed_plz_contact, errorCode + ""), context);
        } else {
            UtilConstants.showAlert(context.getString(R.string.data_conn_lost_during_sync_error_code, errorCode + ""), context);
        }
    }

    @SuppressLint("NewApi")
    public static String getSyncType(Context context, String collectionName, String operation) {
        String mStrSyncType = "4";
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        String sharedVal = sharedPreferences.getString(collectionName, "");
        if (!sharedVal.equalsIgnoreCase("")) {
            if (operation.equalsIgnoreCase(CreateOperation)) {
                if (sharedVal.substring(0, 1).equalsIgnoreCase("0")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(0, 1).equalsIgnoreCase("1")) {
                    mStrSyncType = "1";
                } else if (sharedVal.substring(0, 1).equalsIgnoreCase("2")) {
                    mStrSyncType = "2";
                } else if (sharedVal.substring(0, 1).equalsIgnoreCase("3")) {
                    mStrSyncType = "3";
                } else if (sharedVal.substring(0, 1).equalsIgnoreCase("4")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(0, 1).equalsIgnoreCase("5")) {
                    mStrSyncType = "5";
                }
            } else if (operation.equalsIgnoreCase(ReadOperation)) {
                if (sharedVal.substring(1, 2).equalsIgnoreCase("0")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(1, 2).equalsIgnoreCase("1")) {
                    mStrSyncType = "1";
                } else if (sharedVal.substring(1, 2).equalsIgnoreCase("2")) {
                    mStrSyncType = "2";
                } else if (sharedVal.substring(1, 2).equalsIgnoreCase("3")) {
                    mStrSyncType = "3";
                } else if (sharedVal.substring(1, 2).equalsIgnoreCase("4")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(1, 2).equalsIgnoreCase("5")) {
                    mStrSyncType = "5";
                }

            } else if (operation.equalsIgnoreCase(UpdateOperation)) {
                if (sharedVal.substring(2, 3).equalsIgnoreCase("0")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(2, 3).equalsIgnoreCase("1")) {
                    mStrSyncType = "1";
                } else if (sharedVal.substring(2, 3).equalsIgnoreCase("2")) {
                    mStrSyncType = "2";
                } else if (sharedVal.substring(2, 3).equalsIgnoreCase("3")) {
                    mStrSyncType = "3";
                } else if (sharedVal.substring(2, 3).equalsIgnoreCase("4")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(2, 3).equalsIgnoreCase("5")) {
                    mStrSyncType = "5";
                }
            } else if (operation.equalsIgnoreCase(DeleteOperation)) {
                if (sharedVal.substring(3, 4).equalsIgnoreCase("0")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(3, 4).equalsIgnoreCase("1")) {
                    mStrSyncType = "1";
                } else if (sharedVal.substring(3, 4).equalsIgnoreCase("2")) {
                    mStrSyncType = "2";
                } else if (sharedVal.substring(3, 4).equalsIgnoreCase("3")) {
                    mStrSyncType = "3";
                } else if (sharedVal.substring(3, 4).equalsIgnoreCase("4")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(3, 4).equalsIgnoreCase("5")) {
                    mStrSyncType = "5";
                }
            } else if (operation.equalsIgnoreCase(QueryOperation)) {
                if (sharedVal.substring(4, 5).equalsIgnoreCase("0")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(4, 5).equalsIgnoreCase("1")) {
                    mStrSyncType = "1";
                } else if (sharedVal.substring(4, 5).equalsIgnoreCase("2")) {
                    mStrSyncType = "2";
                } else if (sharedVal.substring(4, 5).equalsIgnoreCase("3")) {
                    mStrSyncType = "3";
                } else if (sharedVal.substring(4, 5).equalsIgnoreCase("4")) {
                    mStrSyncType = "4";
                } else if (sharedVal.substring(4, 5).equalsIgnoreCase("5")) {
                    mStrSyncType = "5";
                }
            }
        } else {
            mStrSyncType = "4";
        }


        return mStrSyncType;
    }

    public static void setSyncTime(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME,
                0);
        if (settings.getBoolean(Constants.isReIntilizeDB, false)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.isReIntilizeDB, false);
            editor.commit();
            try {
                try {
                    Constants.createSyncDatabase(context);  // create sync history table
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<String> DEFINGREQARRAY = Arrays.asList(Constants.getDefinigReq(context));
               /* String syncTime = UtilConstants.getSyncHistoryddmmyyyyTime();
                String[] DEFINGREQARRAY = Constants.getDefinigReq(context);


                for (int incReq = 0; incReq < DEFINGREQARRAY.length; incReq++) {
                    String colName = DEFINGREQARRAY[incReq];
                    if (colName.contains("?$")) {
                        String splitCollName[] = colName.split("\\?");
                        colName = splitCollName[0];
                    }

                    Constants.events.updateStatus(Constants.SYNC_TABLE,
                            colName, Constants.TimeStamp, syncTime
                    );
                }*/
                Constants.updateSyncTime(DEFINGREQARRAY, context, Constants.Sync_All);
            } catch (Exception exce) {
                LogManager.writeLogError(Constants.sync_table_history_txt + exce.getMessage());
            }
        }
    }
    public static void getDataFromSqliteDB(Context context, UIListener rListener, ODataOfflineStore offlineGeo,String state) {
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
                alLocationBeans.add(locationBean);
            }
            //  logStatusToStorage("Step:9 Location list size"+String.valueOf(alLocationBeans.size()));
            updataLatLong(alLocationBeans, context, rListener,offlineGeo,state);
        } else {
            if (cursor != null)
                LocationMonitoringService.locationLog(" SQL db record count " + cursor.getCount());
        }
    }

    public static void updataLatLong(ArrayList<LocationBean> alLocationBeans, final Context context, final UIListener uListener, ODataOfflineStore offlineGeo, String state) {

        UIListener listener = new UIListener() {
            @Override
            public void onRequestError(int i, Exception e) {
                Log.i("LocationCapture", "Lat-Long(Error)");
            }

            @Override
            public void onRequestSuccess(int i, String s) throws ODataException, OfflineODataStoreException {
                Log.i("LocationCapture", "Lat-Long(Stored Successfully)");
                //  logStatusToStorage("Step Final Location Stored");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (reentrantLock == null) {
                            reentrantLock = new ReentrantLock();
                        }
                        try {
                            Log.e("REENTRANT:", "LOCKED");
                            reentrantLock.lock();
                       //     int qry = OfflineManager.getCount(Constants.SPGeos);
                            // LocationMonitoringService.locationLog("Offline DB Count"+String.valueOf(qry));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("FOUND EXCEPTION", "ANR EXCEPTION OCCURRED");
                        } finally {
                            if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
                                reentrantLock.unlock();
                            }
                            Log.e("REENTRANT:", "UNLOCKED FINALLY");
                        }
                    }
                }).start();
            }
        };

        // logStatusToStorage("Step :10 Adding all Location to map");
        if (alLocationBeans != null && alLocationBeans.size() > 0) {

            String mStrSPGUID = "";
            String mobileNo="";
            try {
                SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
                 mStrSPGUID = sharedPreferences.getString("SPGUIDID", "");
                 mobileNo = sharedPreferences.getString("SPMobileNo", "");
            }catch (Exception e){
                e.printStackTrace();
            }
            Log.d("CreateLatLong","SPGUID/Mobile "+mStrSPGUID+"-"+mobileNo);


            /*List<SalesPersonBean> salesPersonBeanList = new ArrayList<>();
            SalesPersonBean salesPersonBean = null;
            try {
                salesPersonBeanList = OfflineManager.getSalesPerson(qry);
                if (salesPersonBeanList.size() > 0 && salesPersonBeanList != null) {
                    salesPersonBean = salesPersonBeanList.get(0);
                }
                if (salesPersonBean != null) {
                    mobileNo = salesPersonBean.getMobileNo();
                }

            } catch (OfflineODataStoreException e) {
                e.printStackTrace();
            }*/
            String imeiSIM1 = "";
            String imeiSIM2 = "";

            try {
                int telephone = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
                if (telephone == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

                    try {
                        imeiSIM1 = telephonyManager.getDeviceId(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        imeiSIM2 = telephonyManager.getDeviceId(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final ODataRequestParamBatch requestParamBatch = new ODataRequestParamBatchDefaultImpl();

            for (int k = 0; k < alLocationBeans.size(); k++) {
                mapTable = new HashMap<>();
                LocationBean locationBean = alLocationBeans.get(k);
                String columnID = locationBean.getCOLUMN_ID();
                mapTable.put(Constants.Latitude, locationBean.getColumnLat());
                mapTable.put(Constants.Longitude, locationBean.getColumnLong());
                mapTable.put(Constants.GeoDate, locationBean.getColumnStartdate());
                mapTable.put(Constants.GeoTime, locationBean.getColumnTimestamp());
                mapTable.put(Constants.SPGUID, mStrSPGUID);
                mapTable.put(Constants.BatteryPerc, locationBean.getCOLUMN_BATTERYLEVEL());
//                mapTable.put(Constants.Remarks, locationBean.getCOLUMN_DISTANCE());
                try {
                    mapTable.put(Constants.Distance, locationBean.getCOLUMN_DISTANCE());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mapTable.put(Constants.DistanceUOM, "M");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mapTable.put(Constants.APKVersion, BuildConfig.VERSION_NAME);
                    mapTable.put(Constants.APKVersionCode, String.valueOf(BuildConfig.VERSION_CODE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (!TextUtils.isEmpty(mobileNo)) {
                        mapTable.put(Constants.MobileNo11, mobileNo);
                    } else {
                        mapTable.put(Constants.MobileNo11, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mapTable.put(Constants.IMEI1, imeiSIM1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mapTable.put(Constants.IMEI2, imeiSIM2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                mapTable.put(Constants.AppVisibility, locationBean.getCOLUMN_AppVisibility());

                Hashtable hashtable = new Hashtable();
                if (uListener != null)
                    hashtable =  createLatLong(mapTable, context, uListener, columnID,offlineGeo,state);
                else
                    hashtable =   createLatLong(mapTable, context, listener, columnID,offlineGeo,state);
                try {
//                                Constants.createSyncHistory(colName, syncTime, syncType, StrSPGUID32, parternTypeID, loginId);
                    ODataEntity channelPartnerEntity = null;
                    try {
                      //  OfflineManager.CreateLatLong(hashtable, uiListener, columnID, context,offlineGeo,State);

                        channelPartnerEntity = OfflineManager.createLatLongEntity(hashtable, columnID, context,offlineGeo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int id = k + 1;
                    String contentId = String.valueOf(id);
                    ODataRequestParamSingle batchItem = new ODataRequestParamSingleDefaultImpl();
                    // Create change set
                    batchItem.setPayload(channelPartnerEntity);

                    batchItem.setMode(ODataRequestParamSingle.Mode.Create);
                    batchItem.setResourcePath(Constants.SPGeos);

                    batchItem.setContentID(contentId);
                               /* HashMap<String, String> map = new HashMap<>();
                                map.put("OfflineOData.RemoveAfterUpload", "true");*/
                    // batchItem.setOptions(map);



                    Map<String, String> createHeaders = new HashMap<String, String>();
                    createHeaders.put("OfflineOData.RemoveAfterUpload", "true");
                    batchItem.getCustomHeaders().putAll(createHeaders);


                    ODataRequestChangeSet changeSetItem = new ODataRequestChangeSetDefaultImpl();
                    changeSetItem.add(batchItem);

                    try {
                        requestParamBatch.add(changeSetItem);
                        //requestParamBatch.getCustomHeaders().putAll(map);
                    } catch (ODataException e) {
                        LogManager.writeLogError("LN 8631"+e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    LogManager.writeLogError("LN 8637"+e.getMessage());
                }

            }


            try {
                offlineGeo.executeRequest(requestParamBatch);
            } catch (Exception e) {
                LogManager.writeLogError("LN 8644" + e.getMessage());
                try {
                    throw new OfflineODataStoreException(e);
                } catch (OfflineODataStoreException e1) {
                    LogManager.writeLogError("LN 8648" + e.getMessage());
                    e1.printStackTrace();
                }
            }



                    // logStatusToStorage("Step :11 Adding all Location to map ends");
        }
    }
    public static Hashtable createLatLong(Map<String, String> mapTable, Context context, UIListener uiListener, String columnID,ODataOfflineStore offlineGeo,String State) {
        Hashtable latlonghashtable = new Hashtable();

        try {
            Thread.sleep(100);

            GUID guid = GUID.newRandom();
            latlonghashtable.put(Constants.GeoGUID, guid.toString().toUpperCase());
            int tempLongitude = 0;
            int tempLatitude = 0;
            if (mapTable.get(Constants.Longitude) != null && !TextUtils.isEmpty(mapTable.get(Constants.Longitude))) {
                try {
                    tempLongitude = (int) Double.parseDouble(mapTable.get(Constants.Longitude));
                    if (tempLongitude != 0) {
                        latlonghashtable.put(Constants.Longitude, BigDecimal.valueOf(UtilConstants.round(Double.parseDouble(mapTable.get(Constants.Longitude)), 12)));
                    }
                } catch (NumberFormatException e) {
                    tempLongitude = 0;
                    LogManager.writeLogInfo("Exception Long : " + mapTable.get(Constants.Longitude));
//                    latlonghashtable.put(Constants.Longitude, BigDecimal.valueOf(Double.parseDouble("98.00")));
                    e.printStackTrace();
                }
            }
            if (mapTable.get(Constants.Latitude) != null && !TextUtils.isEmpty(mapTable.get(Constants.Latitude))) {
//                UtilConstants.round(Double.parseDouble(mapTable.get(Constants.Latitude)), 12);
                try {
                    tempLatitude = (int) Double.parseDouble(mapTable.get(Constants.Latitude));
                    if (tempLatitude != 0) {
                        latlonghashtable.put(Constants.Latitude, BigDecimal.valueOf(UtilConstants.round(Double.parseDouble(mapTable.get(Constants.Latitude)), 12)));
                    }
                } catch (NumberFormatException e) {
                    tempLatitude = 0;
                    LogManager.writeLogInfo("Exception Lat : " + mapTable.get(Constants.Latitude));
//                    latlonghashtable.put(Constants.Latitude, BigDecimal.valueOf(Double.parseDouble("99.00")));
                    e.printStackTrace();
                }
            }
            latlonghashtable.put(Constants.GeoDate, mapTable.get(Constants.GeoDate));
            if (!TextUtils.isEmpty(mapTable.get(Constants.GeoTime))) {
                ODataDuration startDuration = Constants.getTimeAsODataDurationConvertionLocation(mapTable.get(Constants.GeoTime));
                latlonghashtable.put(Constants.GeoTime, startDuration);
            }
//            latlonghashtable.put(Constants.GeoTime,mapTable.get(Constants.GeoTime));
            latlonghashtable.put(Constants.SPGUID, mapTable.get(Constants.SPGUID));
            latlonghashtable.put(Constants.LoginID, "");
            latlonghashtable.put(Constants.SPNO, "");
            latlonghashtable.put(Constants.SPName, "");
            latlonghashtable.put(Constants.Reason, "");
            latlonghashtable.put(Constants.ReasonDesc, "");
//            latlonghashtable.put(Constants.Remarks,mapTable.get(Constants.Remarks));
            try {
                if (!TextUtils.isEmpty(mapTable.get(Constants.Distance))) {
                    latlonghashtable.put(Constants.Distance, ConstantsUtils.decimalRoundOff(BigDecimal.valueOf(Double.parseDouble(mapTable.get(Constants.Distance))), 2));
                } else {
                    latlonghashtable.put(Constants.Distance, ConstantsUtils.decimalRoundOff(BigDecimal.valueOf(Double.parseDouble("0.00")), 2));
                }
            } catch (Exception e) {
                e.printStackTrace();
                latlonghashtable.put(Constants.Distance, ConstantsUtils.decimalRoundOff(BigDecimal.valueOf(Double.parseDouble("0.00")), 2));
            }
            latlonghashtable.put(Constants.DistanceUOM, mapTable.get(Constants.DistanceUOM));
            try {
                if (!TextUtils.isEmpty(mapTable.get(Constants.BatteryPerc))) {
                    latlonghashtable.put(Constants.BatteryPerc, ConstantsUtils.decimalRoundOff(BigDecimal.valueOf(Double.parseDouble(mapTable.get(Constants.BatteryPerc))), 2));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }


            try {
            if(!TextUtils.isEmpty(mapTable.get(Constants.IMEI1))) {
                latlonghashtable.put(Constants.IMEI1, mapTable.get(Constants.IMEI1));
            }else{
                latlonghashtable.put(Constants.IMEI1, "");

            }
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                if (!TextUtils.isEmpty(mapTable.get(Constants.IMEI2))) {
                    latlonghashtable.put(Constants.IMEI2, mapTable.get(Constants.IMEI2));
                } else {
                    latlonghashtable.put(Constants.IMEI2, "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(mapTable.get(Constants.APKVersion))) {
                    latlonghashtable.put(Constants.APKVersion, mapTable.get(Constants.APKVersion));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(mapTable.get(Constants.APKVersionCode))) {
                    latlonghashtable.put(Constants.APKVersionCode, mapTable.get(Constants.APKVersionCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(mapTable.get(Constants.MobileNo11))) {
                    latlonghashtable.put(Constants.MobileNo11, mapTable.get(Constants.MobileNo11));
                } else {
                    latlonghashtable.put(Constants.MobileNo11, mapTable.get(Constants.MobileNo11));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            latlonghashtable.put(Constants.Remarks,getDeviceName() + " (" + mapTable.get(Constants.AppVisibility) + ")");
          /*  if (tempLatitude != 0 && tempLongitude != 0) {
                OfflineManager.CreateLatLong(latlonghashtable, uiListener, columnID, context,offlineGeo,State);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return latlonghashtable;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    public static String getLocationAutoSyncTimeInMin() {
        try {
            //time in minutes
            return OfflineManager.getValueByColumnName(Constants.ConfigTypsetTypeValues + "?$filter=" + Constants.Typeset + " eq '" +
                    Constants.SF + "' and " + Constants.Types + " eq '" + Constants.GEOAUTOSYN + "' &$top=1", Constants.TypeValue);
        } catch (OfflineODataStoreException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void startAutoSyncLocation(Context mContext, boolean isForceReset) {
        try {
            if (ConstantsUtils.isAutomaticTimeZone(mContext)) {
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
                Constants.isSync = false;
                String autoSyncTime = ConstantsUtils.getLocationAutoSyncTimeInMin();  /*"15"*/
                // autoSyncTime = "2";
                ;
                if (TextUtils.isEmpty(autoSyncTime)) {
                    autoSyncTime = "15";
                }
                if (isForceReset || !sharedPreferences.getString("LocationServiceAutoSync", "").equalsIgnoreCase(autoSyncTime)) {
                    if (!TextUtils.isEmpty(autoSyncTime)) {
                        // UpdatePendingLatLongRequest.getInstance(null).callSchedule(autoSyncTime);
                        Intent intent = new Intent(mContext.getApplicationContext(), AutoSyncDataLocationAlarmReceiver.class);
                        // Create a PendingIntent to be triggered when the alarm goes off
                        alarmPendingIntent = PendingIntent.getBroadcast(mContext, AutoSyncDataLocationAlarmReceiver.REQUEST_CODE,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        // Setup periodic alarm every 5 seconds
                        long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
                        int intervalMillis = 1000 * 60 * Integer.parseInt(autoSyncTime); // as of API 19, alarm manager will be forced up to 60000 to save battery
                        AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        // See https://developer.android.com/training/scheduling/alarms.html
                        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, alarmPendingIntent);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("LocationServiceAutoSync", autoSyncTime);
                        editor.apply();
                    }
                }
            }else{
                LogManager.writeLogError("Auto Location Sync not started because date is not valid ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.writeLogError(Constants.error_txt + e.getMessage());
        }
    }


    public static String convertALBussinessMsgToString(ArrayList<String> arrayList) {
        String mErrorMsg = "";
        if (arrayList != null && arrayList.size() > 0) {
            for (String errMsg : arrayList) {
                if (mErrorMsg.length() == 0) {
                    mErrorMsg = mErrorMsg + errMsg;
                } else {
                    mErrorMsg = mErrorMsg + "\n" + errMsg;
                }
            }
        }
        return mErrorMsg;
    }
    public static void setScheduleAlaram(Context context, int hours,
                                         int minuts, int seconds, int date) {
        Calendar calNow = new GregorianCalendar();
        calNow.setTimeInMillis(System.currentTimeMillis());  // Set current time

        Calendar calSet = new GregorianCalendar();
//        cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        calSet.set(Calendar.HOUR_OF_DAY, hours);
        calSet.set(Calendar.MINUTE, minuts);
//        cal.set(Calendar.AM_PM,Calendar.PM);
//        cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
//        cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
//        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
//        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));
        if (calSet.compareTo(calNow) <= 0) {
            //Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, date);
        }
        Intent intent = new Intent(context, AlaramRecevier.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
        }

        Log.d("LocationServiceCapture", "Alaram enabled");
        writeLogsToInternalStorage(context,"Alaram Scheduled to start service");
    }

    public static ODataDuration getTimeAsODataDurationConvertionLocation(String timeString) {
        List<String> timeDuration = Arrays.asList(timeString.split("-"));
        int hour = Integer.parseInt((String) timeDuration.get(0));
        int minute = Integer.parseInt((String) timeDuration.get(1));
        int seconds = Integer.parseInt((String) timeDuration.get(2));
        ODataDurationDefaultImpl oDataDuration = null;

        try {
            oDataDuration = new ODataDurationDefaultImpl();
            oDataDuration.setHours(hour);
            oDataDuration.setMinutes(minute);
            oDataDuration.setSeconds(BigDecimal.valueOf((long) seconds));
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return oDataDuration;
    }

    public static void getLocation(Activity mActivity, final LocationInterface locationInterface) {
        UtilConstants.latitude = 0.0;
        UtilConstants.longitude = 0.0;
        getCustomLocation(mActivity, new LocationInterface() {
            @Override
            public void location(boolean status, LocationModel locationModel, String errorMsg, int errorCode) {
                if (status) {
                    android.location.Location location = locationModel.getLocation();
                    UtilConstants.latitude = location.getLatitude();
                    UtilConstants.longitude = location.getLongitude();
                    Log.d("LocationUtils", "location: " + locationModel.getLocationFrom());
                }
                if (locationInterface != null) {
                    locationInterface.location(status, locationModel, errorMsg, errorCode);
                }
            }
        });
    }

    public static Calendar convertDateFormat1(String dateVal) {
        Date date = null;
        Calendar curCal = new GregorianCalendar();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        try {
            date = format.parse(dateVal);
            curCal.setTime(date);
            System.out.println("Date" + curCal.getTime());
        } catch (ParseException var5) {
            var5.printStackTrace();
        }

        return curCal;
    }
    public static String getErrorEntityName() {
        String mEntityName = "";

        try {
            if (Constants.Entity_Set != null && Constants.Entity_Set.size() > 0) {

                if (Constants.Entity_Set != null && !Constants.Entity_Set.isEmpty()) {
                    Iterator itr = Constants.Entity_Set.iterator();
                    while (itr.hasNext()) {
                        if (mEntityName.length() == 0) {
                            mEntityName = mEntityName + itr.next().toString();
                        } else {
                            mEntityName = mEntityName + "," + itr.next().toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            mEntityName = "";
        }

        return mEntityName;
    }

    public static void customAlertDialogWithScroll(final Context context, final String mErrTxt) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_dialog_scroll, null);

        String mStrErrorEntity = getErrorEntityName();

        TextView textview = (TextView) view.findViewById(R.id.tv_err_msg);
        final TextView tvdetailmsg = (TextView) view.findViewById(R.id.tv_detail_msg);

        String temp_errMsg = mErrTxt;
        temp_errMsg = Constants.makecustomHttpErrormessage(temp_errMsg);
        if (!TextUtils.isEmpty(temp_errMsg) && temp_errMsg.equalsIgnoreCase(mErrTxt))
            if (mErrTxt.contains("invalid authentication")) {
                textview.setText(Constants.PasswordExpiredMsg);
                tvdetailmsg.setText(mErrTxt);
            } else if (mErrTxt.contains("HTTP Status 401 ? Unauthorized")) {
                textview.setText(Constants.PasswordExpiredMsg);
                tvdetailmsg.setText(mErrTxt);
            } else {
                textview.setText(context.getString(R.string.msg_error_occured_during_sync_except) + " " + mStrErrorEntity + " \n" + mErrTxt);
            }

        else {
            textview.setText("\n" + temp_errMsg);
        }

        if (mErrTxt.contains("invalid authentication") || mErrTxt.contains("HTTP Status 401 ? Unauthorized")) {
            final AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .setNeutralButton("Details", null)
                    .setNegativeButton("Settings", null)
                    .create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // TODO Do something
                            dialog.dismiss();
                        }
                    });

                    Button mesg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    mesg.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // TODO Do something

                            tvdetailmsg.setVisibility(View.VISIBLE);
                            // dialog.dismiss();
                        }
                    });

                    Button change = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    change.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // TODO Do something
                            RegistrationModel<Serializable> registrationModel = new RegistrationModel<>();
                            Intent intent = new Intent(context, com.arteriatech.mutils.support.SecuritySettingActivity.class);
                            registrationModel.setExtenndPwdReq(true);
                            registrationModel.setUpdateAsPortalPwdReq(true);
                            registrationModel.setIDPURL(Configuration.IDPURL);
                            registrationModel.setExternalTUserName(Configuration.IDPTUSRNAME);
                            registrationModel.setExternalTPWD(Configuration.IDPTUSRPWD);
                            intent.putExtra(UtilConstants.RegIntentKey, registrationModel);
                            //context.startActivityForResult(intent, 350);
                            context.startActivity(intent);
                            // dialog.dismiss();
                        }
                    });

                }
            });
            dialog.show();
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.MyTheme);
            alertDialog.setCancelable(false)
                    .setPositiveButton(context.getString(R.string.msg_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            copyMessageToClipBoard(context, mErrTxt);
                        }
                    });
            alertDialog.setView(view);
            AlertDialog alert = alertDialog.create();
            alert.show();
        }


    }

    public static void copyMessageToClipBoard(Context context, String message) {
        ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Error Message", message);
        clipboard.setPrimaryClip(clip);
        if (!message.contains("invalid authentication")) {
            UtilConstants.showAlert(context.getString(R.string.issue_copied_to_clipboard_send_to_chnnel_team), context);
        }
    }
    public static void updateSyncTime(final List<String> alAssignColl, final Context context, final String syncType, final SyncHistoryCallBack syncHistoryCallBack) {
        String strSPGUID = Constants.getSPGUID(Constants.SPGUID);
//        String StrSPGUID32 = "";
//        String parternTypeID = "";

        if (!TextUtils.isEmpty(strSPGUID)) {
            StrSPGUID32 = strSPGUID.replaceAll("-", "");
            try {
                parternTypeID = OfflineManager.getPartnerTypeID(Constants.UserPartners + "?$filter= PartnerID eq'" + StrSPGUID32 + "'");
            } catch (OfflineODataStoreException e) {
                e.printStackTrace();
            }
        }

        final boolean checkSyncHistoryColl = getSyncHistoryColl(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        final String loginId = sharedPreferences.getString("username", "");
        final String syncTime = Constants.getSyncHistoryddmmyyyyTime();
        final ODataRequestParamBatch requestParamBatch = new ODataRequestParamBatchDefaultImpl();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (reentrantLock == null) {
                    reentrantLock = new ReentrantLock();
                }
                reentrantLock.lock();
                Log.e("Sync Histroy REENTRANT:", "LOCKED");
                try {
                    for (int incReq = 0; incReq < alAssignColl.size(); incReq++) {
                        colName = alAssignColl.get(incReq);
                        if (colName.contains("?$")) {
                            String splitCollName[] = colName.split("\\?");
                            colName = splitCollName[0];
                        }


                        Constants.events.updateStatus(Constants.SYNC_TABLE,
                                colName, Constants.TimeStamp, syncTime
                        );

                        if (checkSyncHistoryColl) {
                            try {
//                                Constants.createSyncHistory(colName, syncTime, syncType, StrSPGUID32, parternTypeID, loginId);
                                Hashtable hashtable = Constants.createSyncHistoryBatch(colName, syncTime, syncType, StrSPGUID32, parternTypeID, loginId);
                                ODataEntity channelPartnerEntity = null;
                                try {
                                    channelPartnerEntity = OfflineManager.createSyncHistroyEntity(hashtable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                int id = incReq + 1;
                                String contentId = String.valueOf(id);
                                ODataRequestParamSingle batchItem = new ODataRequestParamSingleDefaultImpl();
                                // Create change set
                                batchItem.setPayload(channelPartnerEntity);

                                batchItem.setMode(ODataRequestParamSingle.Mode.Create);
                                batchItem.setResourcePath(Constants.SyncHistroy);

                                batchItem.setContentID(contentId);
                               /* HashMap<String, String> map = new HashMap<>();
                                map.put("OfflineOData.RemoveAfterUpload", "true");*/
                                // batchItem.setOptions(map);



                                Map<String, String> createHeaders = new HashMap<String, String>();
                                createHeaders.put("OfflineOData.RemoveAfterUpload", "true");
                                batchItem.getCustomHeaders().putAll(createHeaders);


                                ODataRequestChangeSet changeSetItem = new ODataRequestChangeSetDefaultImpl();
                                changeSetItem.add(batchItem);

                                try {
                                    requestParamBatch.add(changeSetItem);
                                    //requestParamBatch.getCustomHeaders().putAll(map);
                                } catch (ODataException e) {
                                    LogManager.writeLogError("LN 8631"+e.getMessage());
                                    e.printStackTrace();
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                LogManager.writeLogError("LN 8637"+e.getMessage());
                            }
                        }
                    }
                    try {
                        offlineStore.executeRequest(requestParamBatch);
                    } catch (Exception e) {
                        LogManager.writeLogError("LN 8644"+e.getMessage());
                        try {
                            throw new OfflineODataStoreException(e);
                        } catch (OfflineODataStoreException e1) {
                            LogManager.writeLogError("LN 8648"+e.getMessage());
                            e1.printStackTrace();
                        }
                    }
                    try {
                        updateStartSyncTime(context, syncType, Constants.EndSync);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
                        reentrantLock.unlock();
                    }
                    Log.e("Sync Histroy EXCEPTION", "ANR EXCEPTION OCCURRED");
                } finally {
                    if (reentrantLock != null && reentrantLock.isHeldByCurrentThread())
                        reentrantLock.unlock();
                    Log.e("Sync Histroy REENTRANT:", "UNLOCKED FINALLY");
                }
                if (syncHistoryCallBack != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            syncHistoryCallBack.displaySuccessMessage();
                        }
                    });
                }
            }
        }).start();

    }
    public static ArrayList<String> getDefinigReqList(Context mContext) {
        ArrayList<String> alAssignColl = new ArrayList<>();
        String[] DEFINGREQARRAY = getDefinigReq(mContext);
        for (String collectionName : DEFINGREQARRAY) {
            if (collectionName.contains("?")) {
                String splitCollName[] = collectionName.split("\\?");
                collectionName = splitCollName[0];
            }
            alAssignColl.add(collectionName);
        }
        return alAssignColl;
    }

    public static final String getLastSyncTimeStamp(String tableName, String columnName, String columnValue) {
        return "select *  from  " + tableName + " Where " + columnName + "='" + columnValue + "'  ;";

    }

    public static boolean isNetWorkNotAval(Context context){
        boolean isConnected = false;
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        isConnected = info != null && info.isConnectedOrConnecting();
        return isConnected;
    }

    public static void requestConfigTypesetValues(Context mContext,String userName,String psw) {
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
                        SharedPreferences mPrefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
                        try {
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString(mContext.getString(R.string.geo_start_time), startTime);
                            editor.putString(mContext.getString(R.string.geo_end_time), endTime);
                            editor.putInt(mContext.getString(R.string.geo_location_interval_time), timeInterval);
                            editor.putString(mContext.getString(R.string.geo_smallest_displacement), displacement);
                            editor.apply();
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

    public static void writeLogsToInternalStorage(Context mContext,String logData){
        try {
            String  dateAsFileName = UtilConstants.getCurrentDate();
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
               File path = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                        dateAsFileName+".txt");
                if (!path.exists()) {
                    try {
                        path.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (path.exists()) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(path.getAbsolutePath(), true));
                        writer.write(logData);
                        writer.newLine();
                        writer.close();
                    }
                } catch (Exception e) {
                    Log.e("Error", "Log file error", e);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void createLogDirectory() {
        String folder_main = "TrackerLogs";
        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
        String path = f.getAbsolutePath();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = dateFormat.parse(UtilConstants.getCurrentDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -6);
        String yesterdayAsString = dateFormat.format(calendar.getTime());

        String dtStart = yesterdayAsString;
        Date dateCOMPARE = null;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
            dateCOMPARE  = format.parse(dtStart);
            System.out.println(dateCOMPARE);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());
                String dateFIle = files[i].getName();
                dateFIle = dateFIle.replace(".txt","");
                SimpleDateFormat fileformat = new SimpleDateFormat("dd-MM-yyyy");
                Date pastdate = null;
                try {
                    pastdate = format.parse(dateFIle);
                    System.out.println(pastdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(pastdate.compareTo(dateCOMPARE)<0){
                    files[i].delete();
                }
            }

       String  dateAsFileName = UtilConstants.getCurrentDate();
        File dateFile = new File(Environment.getExternalStoragePublicDirectory("")+"/TrackerLogs",
                dateAsFileName+".txt");
        if (!dateFile.exists()) {
            try {
                dateFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<ExternalLogViewBean> getLogFilesFromStorage() {
        ArrayList<ExternalLogViewBean> dataList = new ArrayList<>();
        ExternalLogViewBean beanData = new ExternalLogViewBean();
        String folder_main = "TrackerLogs";
        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
        String path = f.getAbsolutePath();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        beanData = new ExternalLogViewBean();
        for (int i = 0; i < files.length; i++) {
            beanData = new ExternalLogViewBean();
            Log.d("Files", "FileName:" + files[i].getName());
            beanData.setFileName(files[i].getName());
            beanData.setFilePath(files[i].getAbsolutePath());
            dataList.add(beanData);
        }
        return dataList;
    }

    public static void getCustomLocation(final Context context, final LocationInterface locationInterface) {
        GoogleApiClient mGoogleApiClient = (new GoogleApiClient.Builder(context)).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(100);
        locationRequest.setInterval(30000L);
        locationRequest.setFastestInterval(5000L);
        com.google.android.gms.location.LocationSettingsRequest.Builder builder = (new com.google.android.gms.location.LocationSettingsRequest.Builder())
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build());
        result.addOnSuccessListener( new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                new LocationUsingGoogleAPI((Activity) context, new LocationServiceInterface() {
                    public void location(boolean status, android.location.Location location, String errorMsg, int errorCode, int currentAttempt) {
                        boolean isNetworkAvailable = UtilConstants.isNetworkAvailable(context);
                        if (status) {
                            Log.d("LocationUtils", "latitude: " + location.getLatitude() + " longitude: " + location.getLongitude() + " Accuracy :" + location.getAccuracy());
                            LocationModel locationModel = new LocationModel();
                            locationModel.setLocation(location);
                            locationModel.setInternetAvailable(isNetworkAvailable);
                            locationModel.setLocationFrom("G");
                            if (locationInterface != null) {
                                locationInterface.location(status, locationModel, errorMsg, errorCode);
                            }
                        } else {
                            if (errorCode == 502) {
                                LogManager.writeLogError("Location :Unable to connect google play service");
                            } else if (errorCode == 504) {
                                LogManager.writeLogError("Location :Connection with google play service is suspended");
                            } else if (errorCode == 508) {
                                String networkMsg = isNetworkAvailable ? "with mobile data" : "without mobile data ";
                                LogManager.writeLogError("Location :Unable to get location from google play service " + networkMsg);
                            } else {
                                LogManager.writeLogError("Location :other google play service error " + errorMsg);
                            }

                            if (LocationUtils.isGPSEnabled(context) && LocationUtils.isHighAccuracy(context)) {
                                Location locations = UtilConstants.getLocationNoDialog(context);
                                if (locations != null) {
                                    LocationModel locationModelx = new LocationModel();
                                    locationModelx.setLocation(locations);
                                    locationModelx.setInternetAvailable(isNetworkAvailable);
                                    locationModelx.setLocationFrom("L");
                                    if (locationInterface != null) {
                                        locationInterface.location(true, locationModelx, "", 0);
                                        return;
                                    }
                                } else {
                                    LogManager.writeLogError("Location :Unable to get location from Location Manager");
                                }
                            }

                            if (!isNetworkAvailable) {
                                errorMsg ="Unable to get Location";
                            } else {
                                errorMsg = "Unable to get Location";
                            }

                            if (locationInterface != null) {
                                locationInterface.location(false, null, errorMsg, errorCode);
                            }

                            UtilConstants.dialogBoxWithButton(context, "", errorMsg, "Ok", "", null);
                        }

                    }
                }, 1);
            }
        });

        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                try {
                    if (e instanceof ResolvableApiException) {
                        locationInterface.location(false, null, e.getMessage(), ((ResolvableApiException) e).getStatusCode());
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.

                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        });
        /*result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            public void onResult(LocationSettingsResult result) {
                Status status = result.getStatus();
                LocationSettingsStates state = result.getLocationSettingsStates();
                switch(status.getStatusCode()) {
                    case 0:
//                        Log.d(LocationUtils.TAG, "onResult: success");
                        new LocationUsingGoogleAPI(context, new LocationServiceInterface() {
                            public void location(boolean status, android.location.Location location, String errorMsg, int errorCode, int currentAttempt) {
                                boolean isNetworkAvailable = UtilConstants.isNetworkAvailable(context);
                                if (status) {
                                    Log.d("LocationUtils", "latitude: " + location.getLatitude() + " longitude: " + location.getLongitude() + " Accuracy :" + location.getAccuracy());
                                    LocationModel locationModel = new LocationModel();
                                    locationModel.setLocation(location);
                                    locationModel.setInternetAvailable(isNetworkAvailable);
                                    locationModel.setLocationFrom("G");
                                    if (locationInterface != null) {
                                        locationInterface.location(status, locationModel, errorMsg, errorCode);
                                    }
                                } else {
                                    if (errorCode == 502) {
                                        LogManager.writeLogError("Location :Unable to connect google play service");
                                    } else if (errorCode == 504) {
                                        LogManager.writeLogError("Location :Connection with google play service is suspended");
                                    } else if (errorCode == 508) {
                                        String networkMsg = isNetworkAvailable ? "with mobile data" : "without mobile data ";
                                        LogManager.writeLogError("Location :Unable to get location from google play service " + networkMsg);
                                    } else {
                                        LogManager.writeLogError("Location :other google play service error " + errorMsg);
                                    }

                                    if (LocationUtils.isGPSEnabled(context) && LocationUtils.isHighAccuracy(context)) {
                                        Location locations = UtilConstants.getLocationNoDialog(context);
                                        if (locations != null) {
                                            LocationModel locationModelx = new LocationModel();
                                            locationModelx.setLocation(locations);
                                            locationModelx.setInternetAvailable(isNetworkAvailable);
                                            locationModelx.setLocationFrom("L");
                                            if (locationInterface != null) {
                                                locationInterface.location(true, locationModelx, "", 0);
                                                return;
                                            }
                                        } else {
                                            LogManager.writeLogError("Location :Unable to get location from Location Manager");
                                        }
                                    }

                                    if (!isNetworkAvailable) {
                                        errorMsg ="Unable to get Location";
                                    } else {
                                        errorMsg = "Unable to get Location";
                                    }

                                    if (locationInterface != null) {
                                        locationInterface.location(false, null, errorMsg, errorCode);
                                    }

                                    UtilConstants.dialogBoxWithButton(context, "", errorMsg, "Ok", "", null);
                                }

                            }
                        }, 1);
                        break;
                    case 6:
                        *//*try {
                            status.startResolutionForResult(activity, 9871);
                        } catch (IntentSender.SendIntentException var5) {
                            var5.printStackTrace();
                        }*//*

                        if (locationInterface != null) {
                            locationInterface.location(false, null, "GPS Not enabled", 500);
                        }

//                        Log.d(LocationUtils.TAG, "onResult: result send");
                        break;
                    case 8502:
//                        Log.d(LocationUtils.TAG, "onResult: settings not satisfied");
                        if (locationInterface != null) {
                            locationInterface.location(false, null, "Not able to change your settings", 509);
                        }

//                        UtilConstants.alertLocationPopup(501, "Not able to change your settings", activity, activity);
                }

            }
        });*/

    }
}

