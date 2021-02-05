package com.arteriatech.geotrack.rspl.SPGeo.database;

public class LocationBean {
    public LocationBean() {
    }

    public LocationBean(String spNo, String SPNAME, String LAT,
                        String LONG, String STARTDATE,
                        String STARTTIME, String Status, String TEMPNO, String TimeStamp, String AppVisible, String batteryLevel, String distance) {
        this.COLUMN_SPNO = spNo;
        this.COLUMN_SPNAME = SPNAME;
        this.COLUMN_LAT = LAT;
        this.COLUMN_LONG = LONG;
        this.COLUMN_STARTDATE = STARTDATE;
        this.COLUMN_STARTTIME = STARTTIME;
        this.COLUMN_Status = Status;
        this.COLUMN_TEMPNO = TEMPNO;
        this.COLUMN_TIMESTAMP = TimeStamp;
        this.COLUMN_AppVisibility = AppVisible;
        this.COLUMN_BATTERYLEVEL = batteryLevel;
        this.COLUMN_DISTANCE = distance;
    }
    public static final String TABLE_NAME = "Locations";

    public String getColumnSpno() {
        return COLUMN_SPNO;
    }

    public  void setColumnSpno(String columnSpno) {
        COLUMN_SPNO = columnSpno;
    }

    public String getColumnSpname() {
        return COLUMN_SPNAME;
    }

    public  void setColumnSpname(String columnSpname) {
        COLUMN_SPNAME = columnSpname;
    }

    public String getColumnLat() {
        return COLUMN_LAT;
    }

    public  void setColumnLat(String columnLat) {
        COLUMN_LAT = columnLat;
    }

    public String getColumnLong() {
        return COLUMN_LONG;
    }

    public  void setColumnLong(String columnLong) {
        COLUMN_LONG = columnLong;
    }

    public String getColumnStartdate() {
        return COLUMN_STARTDATE;
    }

    public  void setColumnStartdate(String columnStartdate) {
        COLUMN_STARTDATE = columnStartdate;
    }

    public String getColumnStarttime() {
        return COLUMN_STARTTIME;
    }

    public  void setColumnStarttime(String columnStarttime) {
        COLUMN_STARTTIME = columnStarttime;
    }

    public String getCOLUMN_Status() {
        return COLUMN_Status;
    }

    public  void setCOLUMN_Status(String COLUMN_Status) {
        COLUMN_Status = COLUMN_Status;
    }

    public String getColumnTempno() {
        return COLUMN_TEMPNO;
    }

    public  void setColumnTempno(String columnTempno) {
        COLUMN_TEMPNO = columnTempno;
    }

    public String getColumnTimestamp() {
        return COLUMN_TIMESTAMP;
    }

    public  void setColumnTimestamp(String columnTimestamp) {
        COLUMN_TIMESTAMP = columnTimestamp;
    }

    public String COLUMN_SPNO = "";
    public String COLUMN_SPNAME = "";
    public String COLUMN_LAT = "";
    public String COLUMN_LONG = "";
    public String COLUMN_STARTDATE = "";
    public String COLUMN_STARTTIME = "";
    public String COLUMN_Status = "";
    public String COLUMN_TEMPNO = "";
    public String COLUMN_TIMESTAMP = "";

    public  String COLUMN_Loc = "Loc";
    public  String COLUMN_PhoneStatePermsn = "PhoneState";
    public  String COLUMN_ExtStoragePermsn = "Storage";
    public  String COLUMN_GPSEnabled = "Gps";
    public  String COLUMN_AutoTimeZone = "Adt";
    public  String COLUM_NDeviceAdmnPermsn = "Dmp";
    public  String COLUMN_MockLocation = "MockLocation";
    public  String COLUMN_PhoneRestartInd = "PhoneRestart";
    public  String COLUMN_AppInstallStatus = "AppInstStatus";
    public  String COLUMN_AccuracyLevel = "AccuracyLevel";
    public  String COLUMN_GPSStatus = "GPSStatus";

    public String getCOLUMNAddresslineTemp() {
        return COLUMNAddresslineTemp;
    }

    public void setCOLUMNAddresslineTemp(String COLUMNAddresslineTemp) {
        this.COLUMNAddresslineTemp = COLUMNAddresslineTemp;
    }

    public static String COLUMNAddresslineTemp = "";

    public String getCOLUMNSubThoroughfareTemp() {
        return COLUMNSubThoroughfareTemp;
    }

    public void setCOLUMNSubThoroughfareTemp(String COLUMNSubThoroughfareTemp) {
        this.COLUMNSubThoroughfareTemp = COLUMNSubThoroughfareTemp;
    }

    public String getCOLUMNThroughfareTemp() {
        return COLUMNThroughfareTemp;
    }

    public void setCOLUMNThroughfareTemp(String COLUMNThroughfareTemp) {
        this.COLUMNThroughfareTemp = COLUMNThroughfareTemp;
    }

    public String getCOLUMNSubLocalityTemp() {
        return COLUMNSubLocalityTemp;
    }

    public void setCOLUMNSubLocalityTemp(String COLUMNSubLocalityTemp) {
        this.COLUMNSubLocalityTemp = COLUMNSubLocalityTemp;
    }

    public String getCOLUMNLocalityTemp() {
        return COLUMNLocalityTemp;
    }

    public void setCOLUMNLocalityTemp(String COLUMNLocalityTemp) {
        this.COLUMNLocalityTemp = COLUMNLocalityTemp;
    }

    public String getCOLUMNSubAdminAreaTemp() {
        return COLUMNSubAdminAreaTemp;
    }

    public void setCOLUMNSubAdminAreaTemp(String COLUMNSubAdminAreaTemp) {
        this.COLUMNSubAdminAreaTemp = COLUMNSubAdminAreaTemp;
    }

    public String getCOLUMNAdminAreaTemp() {
        return COLUMNAdminAreaTemp;
    }

    public void setCOLUMNAdminAreaTemp(String COLUMNAdminAreaTemp) {
        this. COLUMNAdminAreaTemp = COLUMNAdminAreaTemp;
    }

    public String getCOLUMNCoutryTemp() {
        return COLUMNCoutryTemp;
    }

    public void setCOLUMNCoutryTemp(String COLUMNCoutryTemp) {
        this.COLUMNCoutryTemp = COLUMNCoutryTemp;
    }

    public String getCOLUMNPostalCodeTemp() {
        return COLUMNPostalCodeTemp;
    }

    public void setCOLUMNPostalCodeTemp(String COLUMNPostalCodeTemp) {
        this.COLUMNPostalCodeTemp = COLUMNPostalCodeTemp;
    }

    public String getCOLUMNPremisesTemp() {
        return COLUMNPremisesTemp;
    }

    public void setCOLUMNPremisesTemp(String COLUMNPremisesTemp) {
        this.COLUMNPremisesTemp = COLUMNPremisesTemp;
    }

    public static String COLUMNSubThoroughfareTemp = "";
    public static String COLUMNThroughfareTemp= "";
    public static String COLUMNSubLocalityTemp= "";
    public static String COLUMNLocalityTemp = "";
    public static String COLUMNSubAdminAreaTemp = "";
    public static String COLUMNAdminAreaTemp = "";
    public static String COLUMNCoutryTemp = "";
    public static String COLUMNPostalCodeTemp= "";
    public static String COLUMNPremisesTemp= "";

    public static String getCOLUMNXMLTemp() {
        return COLUMNXMLTemp;
    }

    public static void setCOLUMNXMLTemp(String COLUMNXMLTemp) {
        LocationBean.COLUMNXMLTemp = COLUMNXMLTemp;
    }

    public static String COLUMNXMLTemp= "";




    public String getCOLUMN_Loc() {
        return COLUMN_Loc;
    }

    public void setCOLUMN_Loc(String COLUMN_Loc) {
        this.COLUMN_Loc = COLUMN_Loc;
    }

    public String getCOLUMN_PhoneStatePermsn() {
        return COLUMN_PhoneStatePermsn;
    }

    public void setCOLUMN_PhoneStatePermsn(String COLUMN_PhoneStatePermsn) {
        this.COLUMN_PhoneStatePermsn = COLUMN_PhoneStatePermsn;
    }

    public String getCOLUMN_ExtStoragePermsn() {
        return COLUMN_ExtStoragePermsn;
    }

    public void setCOLUMN_ExtStoragePermsn(String COLUMN_ExtStoragePermsn) {
        this.COLUMN_ExtStoragePermsn = COLUMN_ExtStoragePermsn;
    }

    public String getCOLUMN_GPSEnabled() {
        return COLUMN_GPSEnabled;
    }

    public void setCOLUMN_GPSEnabled(String COLUMN_GPSEnabled) {
        this.COLUMN_GPSEnabled = COLUMN_GPSEnabled;
    }

    public String getCOLUMN_AutoTimeZone() {
        return COLUMN_AutoTimeZone;
    }

    public void setCOLUMN_AutoTimeZone(String COLUMN_AutoTimeZone) {
        this.COLUMN_AutoTimeZone = COLUMN_AutoTimeZone;
    }

    public String getCOLUM_NDeviceAdmnPermsn() {
        return COLUM_NDeviceAdmnPermsn;
    }

    public void setCOLUM_NDeviceAdmnPermsn(String COLUM_NDeviceAdmnPermsn) {
        this.COLUM_NDeviceAdmnPermsn = COLUM_NDeviceAdmnPermsn;
    }

    public String getCOLUMN_MockLocation() {
        return COLUMN_MockLocation;
    }

    public void setCOLUMN_MockLocation(String COLUMN_MockLocation) {
        this.COLUMN_MockLocation = COLUMN_MockLocation;
    }

    public String getCOLUMN_PhoneRestartInd() {
        return COLUMN_PhoneRestartInd;
    }

    public void setCOLUMN_PhoneRestartInd(String COLUMN_PhoneRestartInd) {
        this.COLUMN_PhoneRestartInd = COLUMN_PhoneRestartInd;
    }

    public String getCOLUMN_AppInstallStatus() {
        return COLUMN_AppInstallStatus;
    }

    public void setCOLUMN_AppInstallStatus(String COLUMN_AppInstallStatus) {
        this.COLUMN_AppInstallStatus = COLUMN_AppInstallStatus;
    }

    public String getCOLUMN_AccuracyLevel() {
        return COLUMN_AccuracyLevel;
    }

    public void setCOLUMN_AccuracyLevel(String COLUMN_AccuracyLevel) {
        this.COLUMN_AccuracyLevel = COLUMN_AccuracyLevel;
    }

    public String getCOLUMN_GPSStatus() {
        return COLUMN_GPSStatus;
    }

    public void setCOLUMN_GPSStatus(String COLUMN_GPSStatus) {
        this.COLUMN_GPSStatus = COLUMN_GPSStatus;
    }

    public String getCOLUMN_DISTANCE() {
        return COLUMN_DISTANCE;
    }

    public void setCOLUMN_DISTANCE(String COLUMN_DISTANCE) {
        this.COLUMN_DISTANCE = COLUMN_DISTANCE;
    }

    public String COLUMN_DISTANCE = "";

    public String getCOLUMN_BATTERYLEVEL() {
        return COLUMN_BATTERYLEVEL;
    }

    public void setCOLUMN_BATTERYLEVEL(String COLUMN_BATTERYLEVEL) {
        this.COLUMN_BATTERYLEVEL = COLUMN_BATTERYLEVEL;
    }

    public String COLUMN_BATTERYLEVEL = "";

    public String getCOLUMN_AppVisibility() {
        return COLUMN_AppVisibility;
    }

    public void setCOLUMN_AppVisibility(String COLUMN_AppVisibility) {
        this.COLUMN_AppVisibility = COLUMN_AppVisibility;
    }

    public String COLUMN_AppVisibility = "";

    public String getCOLUMN_ID() {
        return COLUMN_ID;
    }

    public void setCOLUMN_ID(String COLUMN_ID) {
        this.COLUMN_ID = COLUMN_ID;
    }

    public String COLUMN_ID = "";


    public static String COLUMNID = "ID";
    public static String COLUMNSPNO = "SPNO";
    public static String COLUMNSPNAME = "SPNAME";
    public static String COLUMNLAT = "Latitude";
    public static String COLUMNLONG = "Longitude";
    public static String COLUMNSTARTDATE = "StartDate";
    public static String COLUMNSTARTTIME = "StartTime";
    public static String COLUMNStatus = "StatusUpdate";
    public static String COLUMNTEMPNO = "LocationSeqNo";
    public static String COLUMNTIMESTAMP = "Timestamp";
    public static String COLUMNAPPVISBILITY = "AppVisibility";
    public static String COLUMNBATTERYLEVEL = "BatteryLevel";
    public static String COLUMNDISTANCE = "Distance";

    public static String COLUMNAppLocPermission = "Loc";
    public static String COLUMNPhoneStatePermsn = "PhoneState";
    public static String COLUMNExtStoragePermsn = "Storage";
    public static String COLUMNGPSEnabled = "Gps";
    public static String COLUMNAutoTimeZone = "Adt";
    public static String COLUMNDeviceAdmnPermsn = "Dmp";
    public static String COLUMNMockLocation = "MockLocation";
    public static String COLUMNPhoneRestartInd = "PhoneRestart";
    public static String COLUMNAppInstallStatus = "AppInstStatus";
    public static String COLUMNAccuracyLevel = "AccuracyLevel";
    public static String COLUMNGPSStatus = "GPSStatus";
    public static String COLUMNAddressline = "Addressline";
    public static String COLUMNSubThoroughfare = "SubThoroughfare";
    public static String COLUMNThroughfare= "Throughfare";
    public static String COLUMNSubLocality= "SubLocality";
    public static String COLUMNLocality = "Locality";
    public static String COLUMNSubAdminArea = "SubAdminArea";
    public static String COLUMNAdminArea = "AdminArea";
    public static String COLUMNCoutry = "Coutry";
    public static String COLUMNPostalCode= "PostalCode";
    public static String COLUMNPremises= "Premises";
    public static String COLUMNXML= "XML";
    private int id;
    private String note;
    private String timestamp;




    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("+COLUMNID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + COLUMNSPNO + " TEXT,"
                    + COLUMNSPNAME + " TEXT," +
                    ""+COLUMNLAT + " TEXT," +
                    ""+COLUMNLONG + " TEXT,"
                    +COLUMNSTARTDATE + " TEXT,"+COLUMNSTARTTIME + " TEXT," +
                    ""+COLUMNStatus + " TEXT,"+COLUMNTEMPNO + " TEXT,"+COLUMNTIMESTAMP + " TEXT,"+ COLUMNAPPVISBILITY + " TEXT,"
                    +COLUMNBATTERYLEVEL + " TEXT," +COLUMNDISTANCE + " TEXT," +COLUMNAppLocPermission + " TEXT,"
                    +COLUMNPhoneStatePermsn + " TEXT," +COLUMNExtStoragePermsn + " TEXT," +COLUMNGPSEnabled + " TEXT,"
                    +COLUMNAutoTimeZone + " TEXT," +COLUMNDeviceAdmnPermsn +" TEXT," +COLUMNMockLocation +" TEXT,"
                    +COLUMNPhoneRestartInd +" TEXT," +COLUMNAppInstallStatus +" TEXT," +COLUMNAccuracyLevel +" TEXT,"
                    +COLUMNGPSStatus +" TEXT,"+
                    COLUMNXML+" TEXT)";




}
