package gov.wa.wsdot.android.wsdot.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 *  Service for tracking users route.
 *  Uses the fusedLocation api to collect location data while the user is tracking their route.
 */

public class MyRouteTrackingService extends Service
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = MyRouteTrackingService.class.getSimpleName();

    public static final String API_CONNECTION_ERROR_KEY = "KEY_TRACKING_ROUTE_GOOGLE_CONNECTION_ERROR";
    public static final String PERMISSION_ERROR_KEY = "KEY_TRACKING_ROUTE_LOCATION_PERMISSION_ERROR";

    private static final int LOCATION_INTERVAL = 3000; // in milliseconds
    private static final float LOCATION_DISTANCE = 200f; // in meters
    private static final float LOCATION_ACCURACY = 100f; // in meters

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private final IBinder mBinder = new LocalBinder();

    Callbacks activity;

    ArrayList<LatLng> mRouteLocations = new ArrayList<>();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public MyRouteTrackingService getService() {
            return MyRouteTrackingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                // Disconnecting the client invalidates it.
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Log.e(TAG, "Google API connection suspended - service disconnected");
        } else if (i == CAUSE_NETWORK_LOST) {
            Log.e(TAG, "Google API connection suspended - network issues");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google API");
        if (activity != null) {
            activity.trackingError("Failed to connect to Google Service");
        } else {
            setConnectionError();
        }
        stopSelf();
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_INTERVAL)
                .setFastestInterval(0)
                .setSmallestDisplacement(LOCATION_DISTANCE);
        // Request location updates
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        } catch (SecurityException e){
            Log.e(TAG, "Missing location permission");

            if (activity != null) {
                activity.trackingError("Missing location permission");
            } else {
                setLocationPermissionError();
            }
            stopSelf();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getAccuracy() < LOCATION_ACCURACY) {
            mRouteLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    // Service Methods used by Activity service is bound to

    // Returns all the collected points while service was running
    public ArrayList<LatLng> getRouteLocations() {
        return mRouteLocations;
    }

    // Callbacks & Interface for Activity

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    //callbacks interface for communication with service clients!
    public interface Callbacks {
        void trackingError(String message);
    }

    // error logging
    private void setConnectionError(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(API_CONNECTION_ERROR_KEY, true);
        editor.apply();
    }

    private void setLocationPermissionError(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(PERMISSION_ERROR_KEY, true);
        editor.apply();
    }
}