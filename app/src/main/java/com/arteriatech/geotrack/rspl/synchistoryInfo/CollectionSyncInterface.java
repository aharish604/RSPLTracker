package com.arteriatech.geotrack.rspl.synchistoryInfo;

public interface CollectionSyncInterface {
    void onUploadDownload(boolean isUpload, PendingCountBean countBean, String syncType);
}
