package com.arteriatech.geotrack.rspl.synchistoryInfo;

import java.util.ArrayList;

public class PendingCountBean {
    private int count = 0;
    private String collection = "";
    private String syncTime="";
    private boolean showProgress=false;

    public boolean isShowProgress() {
        return showProgress;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    public ArrayList<String> getAlCollectionList() {
        return alCollectionList;
    }

    public void setAlCollectionList(ArrayList<String> alCollectionList) {
        this.alCollectionList = alCollectionList;
    }

    private ArrayList<String> alCollectionList = new ArrayList<>();
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
