package com.arteriatech.geotrack.rspl.online;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.arteriatech.geotrack.rspl.Constants;
import com.sap.maf.tools.logon.core.LogonCoreContext;
import com.sap.maf.tools.logon.core.LogonCoreException;
import com.sap.smp.client.httpc.HttpMethod;
import com.sap.smp.client.httpc.events.ISendEvent;
import com.sap.smp.client.httpc.filters.IRequestFilter;
import com.sap.smp.client.httpc.filters.IRequestFilterChain;

public class XCSRFTokenRequestFilter implements IRequestFilter {


	private static XCSRFTokenRequestFilter instance;

	private String lastXCSRFToken = null;
	private LogonCoreContext lgCtx;
    private Context context;
    private SharedPreferences sharedPerf;



    private XCSRFTokenRequestFilter(Context mcontext, LogonCoreContext logonContext) {
        this.context = mcontext;
		lgCtx = logonContext;
	}

	/**
	 * @return XCSRFTokenRequestFilter
	 */
	public static XCSRFTokenRequestFilter getInstance(Context context, LogonCoreContext logonContext) {
		if (instance == null) {
			instance = new XCSRFTokenRequestFilter(context,logonContext);
		}
		return instance;
	}


	@Override
	public Object filter(ISendEvent event, IRequestFilterChain chain) {
		HttpMethod method = event.getMethod();
		Log.i("XCSRFTokenRequestFilter", "method: " + method + ", lastXCSRFToken: " + lastXCSRFToken);
		if(lastXCSRFToken==null){
            try {
                sharedPerf = context.getSharedPreferences(Constants.PREFS_NAME, 0);
                lastXCSRFToken = sharedPerf.getString("XCSRFTokenHeader", "");
            }catch (Exception e){
                e.printStackTrace();
            }

        }
		if (method == HttpMethod.GET /* && lastXCSRFToken == null */) {
//			lastXCSRFToken =null;
			event.getRequestHeaders().put("X-CSRF-Token", "Fetch");
		} else if (lastXCSRFToken != null) {
			event.getRequestHeaders().put("X-CSRF-Token", lastXCSRFToken);
		} else {
			event.getRequestHeaders().put("X-Requested-With", "XMLHttpRequest");
		}

		String appConnID = null;
		try {
			appConnID = lgCtx.getConnId();
		} catch (LogonCoreException e) {
			Log.e("XCSRFTokenRequestFilter", "error getting connection id", e);
		}

		//for backward compatibility. not needed for SMP 3.0 SP05
		if (appConnID != null) {
			event.getRequestHeaders().put(Constants.HTTP_HEADER_SUP_APPCID, appConnID);
			event.getRequestHeaders().put(Constants.HTTP_HEADER_SMP_APPCID, appConnID);
		}
		event.getRequestHeaders().put("Connection", "Keep-Alive");

		return chain.filter();
	}

	@Override
	public Object getDescriptor() {
		return "XCSRFTokenRequestFilter";
	}

	public void setLastXCSRFToken(String lastXCSRFToken) {
		Log.i("XCSRFTokenRequestFilter", "set: , lastXCSRFToken: " + lastXCSRFToken);
		this.lastXCSRFToken = lastXCSRFToken;
	}

}
