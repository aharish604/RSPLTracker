package com.arteriatech.geotrack.rspl.online;

import android.content.Context;
import android.content.SharedPreferences;

import com.arteriatech.geotrack.rspl.Constants;
import com.sap.smp.client.httpc.events.IReceiveEvent;
import com.sap.smp.client.httpc.filters.IResponseFilter;
import com.sap.smp.client.httpc.filters.IResponseFilterChain;
/*import com.sap.smp.client.supportability.ClientLogLevel;
import com.sap.smp.client.supportability.ClientLogger;
import com.sap.smp.client.supportability.Supportability;*/

import java.io.IOException;
import java.util.List;

public class XCSRFTokenResponseFilter implements IResponseFilter {
	private static XCSRFTokenResponseFilter instance;

	private Context context;
	private XCSRFTokenRequestFilter requestFilter;
	private SharedPreferences sharedPerf;



	private XCSRFTokenResponseFilter(Context context, XCSRFTokenRequestFilter requestFilter) {
		this.context = context;
		this.requestFilter = requestFilter;
	}

	/**
	 * @return XCSRFTokenResponseFilter
	 */
	public static XCSRFTokenResponseFilter getInstance(Context context, XCSRFTokenRequestFilter requestFilter) {
		if (instance == null) {
			instance = new XCSRFTokenResponseFilter(context, requestFilter);
		}
		return instance;
	}


	@Override
	public Object filter(IReceiveEvent event, IResponseFilterChain chain)
			throws IOException {
		try {
			sharedPerf = context.getSharedPreferences(Constants.PREFS_NAME, 0);
		}catch (Exception e){
			e.printStackTrace();
		}

	//	ClientLogger logger = Supportability.getInstance().getClientLogger(this.context, OnlineODataStore.class.getCanonicalName());

		//SAPLoggerUtils.logResponseDetails(event, logger, ClientLogLevel.INFO, true, true);
		List<String> xcsrfTokens = event.getResponseHeaders().get("X-CSRF-Token");

		if (xcsrfTokens != null) {
			String xcsrfToken = xcsrfTokens.get(0);
			if (xcsrfToken != null)

				requestFilter.setLastXCSRFToken(xcsrfToken);
			try {
				SharedPreferences.Editor editor = sharedPerf.edit();
				editor.putString("XCSRFTokenHeader", xcsrfToken);
				editor.apply();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return chain.filter();
	}

	@Override
	public Object getDescriptor() {
		return "XCSRFTokenResponseFilter";
	}

}
