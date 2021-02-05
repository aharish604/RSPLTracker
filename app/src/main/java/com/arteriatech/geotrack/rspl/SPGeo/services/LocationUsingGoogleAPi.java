package com.arteriatech.geotrack.rspl.SPGeo.services;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.arteriatech.mutils.R.string;
import com.arteriatech.mutils.location.LocationServiceInterface;
import com.arteriatech.mutils.location.LocationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class LocationUsingGoogleAPi implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
    private static final long INTERVAL = 10000L;
    private static final long FASTEST_INTERVAL = 5000L;
    private static final float SMALLEST_DISPLACEMENT = 50;//10 Meters
    private static String TAG = LocationUsingGoogleAPi.class.getSimpleName();
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient = null;
    private Context mContext;
    private LocationServiceInterface locationInterface = null;
    private Handler handler1 = new Handler();
    private Runnable runnable1 = null;
    private Handler handler = new Handler();
    private Runnable runnable = null;
    private boolean locationChanged = false;
    private boolean gotExactLocation = false;
    private Location altLocation = null;
    private float oldAccuracy = 1000.0F;
    private int totalAttempt = 1;
    private int currentAttempt = 1;
    private LinkedList<Map<String, Object>> mTransportStatuses = new LinkedList<>();




    public LocationUsingGoogleAPi(Context mContext, LocationServiceInterface locationInterface, int totalAttempt) {
        this.mContext = mContext;
        this.locationInterface = locationInterface;
        this.totalAttempt = totalAttempt;
        this.initiLocationService(mContext);
    }

    private void initiLocationService(Context mContext) {
        if (!LocationUtils.isGPSEnabled(mContext)) {
            this.setError(500, "location disabled");
        } else {
            boolean gApiOk = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext) == 0;
            if (gApiOk) {
                this.createLocationRequest();
                this.mGoogleApiClient = (new Builder(mContext)).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
                Log.d(TAG, "onStart fired ..............");
                this.mGoogleApiClient.connect();
            } else {
                this.setError(501, "Please install google play service");
            }
        }



    }

    private void setError(int errorCode, String msg) {
        Log.d(TAG, "err=>" + msg);
        //   this.disConnect();
        if (this.totalAttempt > this.currentAttempt) {
            if (errorCode == 508) {
                this.setError(errorCode, msg, this.currentAttempt);
                ++this.currentAttempt;
                this.initiLocationService(this.mContext);
            } else {
                this.currentAttempt = this.totalAttempt;
                this.setError(errorCode, msg, this.currentAttempt);
                this.locationInterface = null;
            }
        } else {
            this.setError(errorCode, msg, this.currentAttempt);
            this.locationInterface = null;
        }

    }

    private void setError(int errorCode, String msg, int currentAttempt) {
        if (this.locationInterface != null) {
            this.locationInterface.location(false, (Location)null, msg, errorCode, currentAttempt);
        }

    }

    private void setSuccess(Location location) {
        if (this.locationInterface != null && location != null) {// && !isAlarmScheduled
            this.locationInterface.location(true, location, "", 200, this.currentAttempt);
            //  this.locationInterface = null;
        } else if (this.locationInterface != null) {// && !isAlarmScheduled
            this.locationInterface.location(false, (Location)null, "Not able to get location ", 400, this.currentAttempt);
            this.locationInterface = null;
        }else{
            this.disConnect();
        }


    }

    public void onConnected(@Nullable Bundle bundle) {
        if (this.mGoogleApiClient != null) {
            Log.d(TAG, "onConnected - isConnected ...............: " + this.mGoogleApiClient.isConnected());
            //  prodThread.start();
            //  consThread.start();
            //  this.startLocationUpdates();
        }

    }

    public void onConnectionSuspended(int i) {
        this.setError(504, "connection suspended");
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        this.setError(502, "connection failed");
    }

    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged..............");
        this.locationChanged = true;
        this.gotExactLocation = true;
      /*  try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
//        Toast.makeText(mContext,"Location Captured"+location.getLongitude()+location.getLongitude(),Toast.LENGTH_LONG).show();
        //  LogManager.writeLogError(Constants.LOCATION_LOG + "onLocationChanged Called"+location.getLongitude()+location.getLongitude());

        Map<String, Object> transportStatus = new HashMap<>();
        transportStatus.put("lat", location.getLatitude());
        transportStatus.put("lng", location.getLongitude());
        transportStatus.put("time", new Date().getTime());
        // transportStatus.put("power", getBatteryLevel());
        if (locationIsAtStatus(location, 1) && locationIsAtStatus(location, 0)) {
            // If the most recent two statuses are approximately at the same
            // location as the new current location, rather than adding the new
            // location, we update the latest status with the current. Two statuses
            // are kept when the locations are the same, the earlier representing
            // the time the location was arrived at, and the latest representing the
            // current time.
            mTransportStatuses.set(0, transportStatus);
            // Only need to update 0th status, so we can save bandwidth.
            //  mFirebaseTransportRef.child("0").setValue(transportStatus);
        } else {
            // Maintain a fixed number of previous statuses.
            while (mTransportStatuses.size() >= 2) {
                mTransportStatuses.removeLast();
            }
            mTransportStatuses.addFirst(transportStatus);
            this.setSuccess(location);
            // We push the entire list at once since each key/index changes, to
            // minimize network requests.
            //   mFirebaseTransportRef.setValue(mTransportStatuses);
        }





    }

    private boolean locationIsAtStatus(Location location, int statusIndex) {
        if (mTransportStatuses.size() <= statusIndex) {
            return false;
        }
        Map<String, Object> status = mTransportStatuses.get(statusIndex);
        Location locationForStatus = new Location("");
        locationForStatus.setLatitude((double) status.get("lat"));
        locationForStatus.setLongitude((double) status.get("lng"));
        float distance = location.distanceTo(locationForStatus);
        Log.d(TAG, String.format("Distance from status %s is %sm", statusIndex, distance+"---"+locationForStatus.getLatitude()+"--"+locationForStatus.getLongitude()));
        return distance < 50;
    }

    private void disConnect() {
        if (this.mGoogleApiClient != null) {
            Log.d(TAG, "onStop fired ..............");
            LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
            this.mGoogleApiClient.disconnect();
            Log.d(TAG, "isConnected ...............: " + this.mGoogleApiClient.isConnected());
            this.mGoogleApiClient = null;
        }

        if (this.runnable1 != null) {
            this.handler1.removeCallbacks(this.runnable1);
            this.runnable1 = null;
        }

        if (this.runnable != null) {
            this.handler.removeCallbacks(this.runnable);
            this.runnable = null;
        }

    }

    protected void createLocationRequest() {
        new LocationRequest();
        this.mLocationRequest = LocationRequest.create();
        this.mLocationRequest.setInterval(5000);
        this.mLocationRequest.setFastestInterval(1000);
        this.mLocationRequest.setPriority(100);
        // this.mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(mContext, "android.permission.ACCESS_FINE_LOCATION") != 0 && ActivityCompat.checkSelfPermission(mContext, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            setError(503, "Please grant permission for Location in app settings");
        } else {
            if (mGoogleApiClient != null) {
                PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, this.mLocationRequest, this);
              /*  LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                // do work here
                              *//*  Log.d(TAG, "getFusedLocationProviderClient ..............: ");
                                Map<String, Object> transportStatus = new HashMap<>();
                                transportStatus.put("lat", locationResult.getLastLocation().getLatitude());
                                transportStatus.put("lng", locationResult.getLastLocation().getLongitude());
                                transportStatus.put("time", new Date().getTime());
                                mTransportStatuses.set(0, transportStatus);*//*
                                onLocationChanged(locationResult.getLastLocation());
                             *//*   try {
                                    sharedQueue.put(locationResult.getLastLocation());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
*//*
                            }
                        },
                        Looper.myLooper());*/

                Log.d(TAG, "Location update started ..............: ");
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                    runnable = null;
                }

                Log.d(TAG, "runnable started");
                runnable = new Runnable() {
                    public void run() {
                        if (!LocationUsingGoogleAPi.this.locationChanged) {
                            LocationUsingGoogleAPi.this.setError(508, LocationUsingGoogleAPi.this.mContext.getString(string.unable_to_get_location));
                        }

                    }
                };
                handler.postDelayed(runnable, 30000L);
            }

        }

    }
}
