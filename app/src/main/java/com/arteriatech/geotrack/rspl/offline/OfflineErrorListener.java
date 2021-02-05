package com.arteriatech.geotrack.rspl.offline;

import com.arteriatech.mutils.common.OfflineODataStoreException;
import com.arteriatech.mutils.log.TraceLog;
import com.arteriatech.geotrack.rspl.Constants;
import com.sap.smp.client.odata.ODataError;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.offline.ODataOfflineStore;
import com.sap.smp.client.odata.offline.ODataOfflineStoreRequestErrorListener;
import com.sap.smp.client.odata.store.ODataRequestExecution;
import com.sap.smp.client.odata.store.ODataResponse;
import com.sap.smp.client.odata.store.ODataResponseSingle;

import java.util.Map;

/**
 * Created by e10526 on 22-06-2016.
 *
 */
public class OfflineErrorListener implements ODataOfflineStoreRequestErrorListener {

    public OfflineErrorListener() {
        super();
    }

    @Override
    public void offlineStoreRequestFailed(ODataOfflineStore store,
                                          ODataRequestExecution request, ODataException exception) {

   ODataResponseSingle response = (ODataResponseSingle) request.getResponse();
        Map<ODataResponse.Headers, String> headerMap = response.getHeaders();
        String code  = headerMap.get(ODataResponse.Headers.Code);
        TraceLog.d(Constants.RequestFlushResponse+ code);
        String eTag = headerMap.get(ODataResponse.Headers.ETag);

        if (request!=null && request.getResponse() !=null) {
            ODataPayload payload = ((ODataResponseSingle)
                    request.getResponse()).getPayload();
            //Verify if the request contains payload
            if (payload!=null && payload instanceof ODataError) {
                ODataError oError = (ODataError) payload;
                //Get the error message
                OfflineODataStoreException exc =
                        new OfflineODataStoreException(oError.getMessage());
                //Log the error message
                TraceLog.e(Constants.OfflineStoreRequestFailed, exc);
            } else {
                //Log the exception
                TraceLog.e(Constants.OfflineStoreRequestFailed, exception);
            }
        } else {
            //Log the exception
            TraceLog.e(Constants.offlineStoreRequestFailed, exception);
        }

    }



}
