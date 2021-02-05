package com.arteriatech.geotrack.rspl.asyncTask;

import com.sap.smp.client.odata.ODataEntity;

import java.util.List;

/**
 * Created by e10526 on 12/28/2017.
 */

public interface AsyncTaskItemInterface {
    void soItemLoaded(List<ODataEntity> entity, Boolean isStoreOpened, String mReqId);
}
