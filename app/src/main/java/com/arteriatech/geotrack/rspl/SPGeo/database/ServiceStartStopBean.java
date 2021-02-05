package com.arteriatech.geotrack.rspl.SPGeo.database;

public class ServiceStartStopBean {
    private String time = "";
    private String serviceDescription = "";
    public static String COLUMNID = "ID";
    public static String COLUMNTIME = "TIME";
    public static String COLUMNDESCRIPTION = "DESCRIPTION";
    public static final String TABLE_NAME = "Service";


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public ServiceStartStopBean(String time, String serviceDescription) {

        this.time = time;
        this.serviceDescription = serviceDescription;
    }

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("+COLUMNID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"
                    + COLUMNTIME + " TEXT,"
                    + COLUMNDESCRIPTION + " TEXT" + ")";

}
