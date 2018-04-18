/*
 * Copyright (c) 2017 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.ferries.vesselwatch;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class VesselWatchMapActivity extends BaseActivity implements
        OnMarkerClickListener, OnMyLocationButtonClickListener,
        OnConnectionFailedListener, ConnectionCallbacks, LocationListener,
		ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback, Injectable {

	private static final String TAG = VesselWatchMapActivity.class.getSimpleName();
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private GoogleMap mMap;
	private Handler handler = new Handler();
	private Timer timer;

	private List<CameraItem> cameras = new ArrayList<CameraItem>();
	private List<VesselWatchItem> vessels = new ArrayList<VesselWatchItem>();

	private HashMap<Marker, String> markers = new HashMap<Marker, String>();
	boolean showCameras;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_ACCESS_FINE_LOCATION = 100;
	private Toolbar mToolbar;
	private ProgressBar mProgressBar;

    private boolean mPermissionDenied = false;
    private LocationCallback mLocationCallback;

    FloatingActionButton fabLayers;

    private static VesselWatchViewModel vesselViewModel;
    private static MapCameraViewModel mapCameraViewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private Tracker mTracker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.map);
        
        enableAds(getString(R.string.ferries_ad_target));

        mProgressBar = findViewById(R.id.progress_bar);

		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Hide map fab
        fabLayers = findViewById(R.id.fab);
        fabLayers.setVisibility(View.INVISIBLE);

        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e(TAG, "mLocationCallback!");
                if (locationResult == null){
                    return;
                }
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        mLocationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mapCameraViewModel = ViewModelProviders.of(this, viewModelFactory).get(MapCameraViewModel.class);
        mapCameraViewModel.init("ferries");

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);

        MyLogger.crashlyticsLog("Ferries", "Screen View", "VesselWatchMapActivity", 1);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMarkerClickListener(this);

        LatLng defaultLatLng = new LatLng(47.565125, -122.480508);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 11));

        mMap.setOnCameraMoveListener(() -> {
            if (mMap != null) {
                mapCameraViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);
            }
        });

        vesselViewModel = ViewModelProviders.of(this, viewModelFactory).get(VesselWatchViewModel.class);

        vesselViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mProgressBar.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "connection error, failed to load vessels", Toast.LENGTH_SHORT).show();
                }
            }
        });

        vesselViewModel.getVessels().observe(this, vesselItems -> {
            if (vesselItems != null){

                Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Marker, String> entry = iter.next();
                    if (entry.getValue().equalsIgnoreCase("vessel")) {
                        entry.getKey().remove();
                        iter.remove();
                    }
                }
                vessels.clear();
                vessels = vesselItems;

                if (vessels.size() != 0) {
                    for (int i = 0; i < vessels.size(); i++) {
                        LatLng latLng = new LatLng(vessels.get(i).getLat(), vessels.get(i).getLon());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(vessels.get(i).getName())
                                .snippet(vessels.get(i).getDescription())
                                .icon(BitmapDescriptorFactory.fromResource(vessels.get(i).getIcon()))
                                .visible(true));

                        markers.put(marker, "vessel");
                    }
                }
            }
        });

        vesselViewModel.refreshVessels();

        mapCameraViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mProgressBar.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "connection error, failed to load cameras", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mapCameraViewModel.getDisplayCameras().observe(this, cameraItems -> {
            if (cameraItems != null) {
                Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Marker, String> entry = iter.next();
                    if (entry.getValue().equalsIgnoreCase("camera")) {
                        entry.getKey().remove();
                        iter.remove();
                    }
                }
                cameras.clear();
                cameras = cameraItems;

                if (cameras.size() != 0) {
                    for (int i = 0; i < cameras.size(); i++) {
                        LatLng latLng = new LatLng(cameras.get(i).getLatitude(), cameras.get(i).getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(cameras.get(i).getTitle())
                                .snippet(cameras.get(i).getCameraId().toString())
                                .icon(BitmapDescriptorFactory.fromResource(cameras.get(i).getCameraIcon()))
                                .visible(showCameras));

                        markers.put(marker, "camera");
                    }
                }
            }
        });

        timer = new Timer();
        timer.schedule(new VesselsTimerTask(), 0, 30000); // Schedule vessels to update every 30 seconds

        enableMyLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (mGoogleApiClient.isConnected()) {
            getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
            mGoogleApiClient.disconnect();
        }
        if (timer != null) {
            timer.cancel();
        }
    }


    public boolean onMarkerClick(Marker marker) {
        Bundle b = new Bundle();
        Intent intent = new Intent();        
        
        if (markers.get(marker).equalsIgnoreCase("vessel")) {
            MyLogger.crashlyticsLog("Ferries", "Tap", "Vessel " + marker.getTitle(), 1);
            intent.setClass(this, VesselWatchDetailsActivity.class);
            b.putString("title", marker.getTitle());
            b.putString("description", marker.getSnippet());
            intent.putExtras(b);
            this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("camera")) {
            MyLogger.crashlyticsLog("Ferries", "Tap", "Camera " + marker.getSnippet(), 1);
        	// GA tracker
        	mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
            mTracker.setScreenName("/Ferries/Vessel Watch/Cameras");
    		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        	
            intent.setClass(this, CameraActivity.class);
            b.putInt("id", Integer.parseInt(marker.getSnippet()));
            intent.putExtras(b);
            this.startActivity(intent);            
        }
        
        return true;
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
	    getMenuInflater().inflate(R.menu.vessel_watch, menu);

	    if (showCameras) {
	    	menu.getItem(0).setTitle("Hide Cameras");
            menu.getItem(0).setIcon(R.drawable.ic_menu_traffic_cam);
	    } else {
	    	menu.getItem(0).setTitle("Show Cameras");
            menu.getItem(0).setIcon(R.drawable.ic_menu_traffic_cam_off);
	    }	    
	    
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
		
		switch (item.getItemId()) {

	    case android.R.id.home:
	    	finish();
	    	return true;	    
	    case R.id.goto_anacortes:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Anacortes");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(48.535868, -123.013808, 10);
	    	return true;
	    case R.id.goto_edmonds:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Edmonds");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(47.803096, -122.438718, 12);
	    	return true;
	    case R.id.goto_fauntleroy:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Fauntleroy");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(47.513625, -122.450820, 13);
	    	return true;
	    case R.id.goto_mukilteo:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Mukilteo");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(47.963857, -122.327721, 13);
	    	return true;
	    case R.id.goto_pointdefiance:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Pt. Defiance");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(47.319040, -122.510890, 13);
	    	return true;
	    case R.id.goto_porttownsend:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Port Townsend");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(48.135562, -122.714449, 12);
	    	return true;
	    case R.id.goto_sanjuanislands:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/San Juan Islands");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(48.557233, -122.897078, 12);
	    	return true;
	    case R.id.goto_seattle:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Seattle");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(47.565125, -122.480508, 11);
	    	return true;
	    case R.id.goto_seattlebainbridge:
	    	mTracker.setScreenName("/Ferries/Vessel Watch/Go To Location/Seattle-Bainbridge");
	    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	    	goToLocation(47.600325, -122.437249, 12);
	    	return true;
	    case R.id.toggle_cameras:
	    	toggleCameras(item);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void toggleCameras(MenuItem item) {
		
	    // GA tracker
		mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
		mTracker.setScreenName("/Ferries/Vessel Watch/");

		if (showCameras) {
			for(Entry<Marker, String> entry : markers.entrySet()) {
			    Marker key = entry.getKey();
			    String value = entry.getValue();
			    
			    if (value.equalsIgnoreCase("camera")) {
			        key.setVisible(false);
			    }	
			}
			
			item.setTitle("Show Cameras");
            item.setIcon(R.drawable.ic_menu_traffic_cam_off);
			showCameras = false;
		    
			mTracker.send(new HitBuilders.EventBuilder()
				    .setCategory("Ferries")
				    .setAction("Cameras")
				    .setLabel("Hide Cameras")
				    .build());
			
		} else {
            for(Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();
                
                if (value.equalsIgnoreCase("camera")) {
                    key.setVisible(true);
                }
            }
            
			item.setTitle("Hide Cameras");
            item.setIcon(R.drawable.ic_menu_traffic_cam);
			showCameras = true;
			
			mTracker.send(new HitBuilders.EventBuilder()
				    .setCategory("Ferries")
				    .setAction("Cameras")
				    .setLabel("Show Cameras")
				    .build());
			
		}		

		// Save camera display preference
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("KEY_SHOW_CAMERAS", showCameras);
		editor.commit();
	}	
	
	public void goToLocation(double latitude, double longitude, int zoomLevel) {	
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
	}	
	
    public class VesselsTimerTask extends TimerTask {
        private Runnable runnable = new Runnable() {
            public void run() {
                vesselViewModel.refreshVessels();
            }
        };

        public void run() {
            handler.post(runnable);
        }
    }


    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
    }

    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
    }

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");        
    }
    
    /**
     * 
     * @param location
     */
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mMap.animateCamera(cameraUpdate);
    }


    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(8000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void enableMyLocation() {
        if (checkPermission(true)) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    /**
     * Request location updates after checking permissions first.
     */
    private void requestLocationUpdates() {
        Log.e(TAG, "requesting location updates");
        if (checkPermission(true)) {
            if (mGoogleApiClient.isConnected()) {
                getFusedLocationProviderClient(this).requestLocationUpdates(createLocationRequest(),
                        mLocationCallback,
                        Looper.myLooper());
            }
        }
    }


    public boolean onMyLocationButtonClick() {
        if (checkPermission(true)){
            // Get last known recent location using new Google Play Services SDK (v11+)
            FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

            locationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        requestLocationUpdates();
                        if (location != null) {
                            handleNewLocation(location);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    });
        }
        return true;
    }

    private boolean checkPermission(Boolean requestIfNeeded){
        if (ContextCompat.checkSelfPermission(VesselWatchMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (requestIfNeeded){
                requestPermissions();
            }
        } else {
            return true;
        }
        return false;
    }

    private void requestPermissions() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            ActivityCompat.requestPermissions(VesselWatchMapActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(VesselWatchMapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    requestLocationUpdates();

                }
            }
        }
    }
}
