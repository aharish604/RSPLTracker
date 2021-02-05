package com.arteriatech.geotrack.rspl;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatDelegate;

import com.arteriatech.mutils.upgrade.ApplicationLifecycleHandler;

import com.sap.mobile.lib.configuration.Preferences;
import com.sap.mobile.lib.parser.IODataSchema;
import com.sap.mobile.lib.parser.Parser;
import com.sap.mobile.lib.parser.ParserException;
import com.sap.mobile.lib.request.ConnectivityParameters;
import com.sap.mobile.lib.request.RequestManager;
import com.sap.mobile.lib.supportability.Logger;

public class MSFAGEOApplication extends Application {
	
	private static final String TAG = MSFAGEOApplication.class.getName();
	public static volatile int MINUTES=2;
	public  static RequestManager mRequestManager = null;
	public Preferences mPreferences = null;
	private Logger mLogger = null;
	private ConnectivityParameters mConnectivityParameters = null;
	private Parser mParser = null;
	private IODataSchema mSchema = null;

	String username="",password="";

	private final static int NUMBER_OF_THREADS = 3;
	private BroadcastReceiver openStoreReceiver=null;
	private boolean isFinished=false;
	private String serviceErrorMsg="";

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		try {
			getParameters(username ,password);
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
			ApplicationLifecycleHandler handler = new ApplicationLifecycleHandler();
			registerActivityLifecycleCallbacks(handler);
			registerComponentCallbacks(handler);
		/*	new ANRWatchDog().setANRListener(new ANRWatchDog.ANRListener() {
				@Override
				public void onAppNotResponding(final ANRError error) {
					// Handle the error. For example, log it to HockeyApp:
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable() {
						@Override
						public void run() {
							//Your UI code here
//							Toast.makeText(MSFAGEOApplication.this, error.getMessage(), Toast.LENGTH_LONG).show();
							LogManager.writeLogInfo(error.getMessage());
						}
					});
				}
			}).start();*/
//			new ANRWatchDog().start();
			/*final Handler handlertemp = new Handler();
			handlertemp.postDelayed(new Runnable() {
				@Override
				public void run() {
					//Write whatever to want to do after delay specified (1 sec)
					Log.d("Handler", "Running Handler");
					InitialRegistrationIntentService.startInitialRegService(MSFAGEOApplication.this, false);

				}
			}, 1000);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
//		IntentFilter intentFilter= new IntentFilter();
//		intentFilter.addAction(Intent.ACTION_TIME_TICK);
//		registerReceiver(new TrackerService(getApplicationContext()).getInstance(),intentFilter);
//		setTime();
	}
	final Handler handler = new Handler();

	public void getParameters(String username, String password) {
	mLogger = new Logger();
		
		//CreateOperation Connectivity Parameters
		
		mConnectivityParameters = new ConnectivityParameters();
		mConnectivityParameters.setLanguage(this.getResources()
				.getConfiguration().locale.getLanguage());
		mConnectivityParameters.enableXsrf(true);
		
		mConnectivityParameters.setUserName(username);
		mConnectivityParameters.setUserPassword(password);

		//CreateOperation Preferences

		mPreferences = new Preferences(this, mLogger);

		
		try {
			mParser = new Parser(mPreferences, getLogger());
		} catch (ParserException e) {
			mLogger.e(TAG, Constants.ErrorInParser, e);
		}
				
	}

	public Logger getLogger() {
		return mLogger;
	}

	/**
	 * It returns the only  instance of Preferences for the lifetime of the application 
	 * @return Preferences
	 */


	/**
	 * @param username
	 */
	public void setUsername(String username) {
		mConnectivityParameters.setUserName(username);
	}

	/**
	 * @return String username
	 */
	public String getUsername() {
		return mConnectivityParameters.getUserName();
	}
	



	/**
	 * It creates only one instance of RequestManager for the lifetime of the application 
	 * @return RequestManager
	 */
	public RequestManager getRequestManager() {
		if (mRequestManager == null) {
			mRequestManager = new RequestManager(mLogger, mPreferences,
					mConnectivityParameters, NUMBER_OF_THREADS);
		}
		return mRequestManager;
	}
	

	
	/**
	 * @return
	 */
	public Parser getParser() {
		return this.mParser;
	}


	public IODataSchema getODataSchema() {
		return mSchema;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	public void startService(Context mContext) {
		isFinished = false;
		serviceErrorMsg = "";
		OpenStoreIntentService.startServices(mContext);
	}

	public boolean isServiceFinished() {
		return isFinished;
	}

	public String getServiceError() {
		return serviceErrorMsg;
	}

	public void setBroadCastReceiver(BroadcastReceiver broadCastReceiver) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(OpenStoreIntentService.ACTION_SERVICE_KEY);
		this.openStoreReceiver = broadCastReceiver;
		registerReceiver(openStoreReceiver, intentFilter);
	}

	public void unRegisterReceiver() {
		try {
			if (openStoreReceiver != null) {
				unregisterReceiver(openStoreReceiver);
				openStoreReceiver = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setServiceFinished(boolean isFinished, String serviceErrorMsg) {
		this.isFinished = isFinished;
		this.serviceErrorMsg = serviceErrorMsg;
		if (openStoreReceiver != null) {
			Intent intent = new Intent(OpenStoreIntentService.ACTION_SERVICE_KEY);
			intent.putExtra(OpenStoreIntentService.EXTRA_ERROR_KEY, serviceErrorMsg);
			intent.putExtra(OpenStoreIntentService.EXTRA_FINISHED_KEY, isFinished);
			sendBroadcast(intent);
		}
	}

}
