/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.service.CamerasSyncService;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.util.map.CamerasOverlay;
import gov.wa.wsdot.android.wsdot.util.map.VesselsOverlay;

public class VesselWatchMapActivity extends BaseActivity implements
        OnMarkerClickListener, OnMyLocationButtonClickListener,
        OnConnectionFailedListener, ConnectionCallbacks,
        OnCameraChangeListener, LocationListener {

	private static final String TAG = VesselWatchMapActivity.class.getSimpleName();
	private GoogleMap map = null;
	private Handler handler = new Handler();
	private Timer timer;
	private VesselsOverlay vesselsOverlay = null;
	private CamerasOverlay camerasOverlay = null;
	private List<CameraItem> cameras = new ArrayList<CameraItem>();
	private List<VesselWatchItem> vessels = new ArrayList<VesselWatchItem>();
	private HashMap<Marker, String> markers = new HashMap<Marker, String>();
	boolean showCameras;
	
	private CamerasSyncReceiver mCamerasReceiver;
	private Intent camerasIntent;
	private static AsyncTask<Void, Void, Void> camerasOverlayTask = null;
	private static AsyncTask<Void, Void, Void> vesselsOverlayTask = null;
	private LatLngBounds bounds;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_ACCESS_FINE_LOCATION = 100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.map);
        
        enableAds();
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize AsyncTasks
        camerasOverlayTask = new CamerasOverlayTask();
        vesselsOverlayTask = new VesselsOverlayTask();

        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
        
        // Setup Service Intent.
        camerasIntent = new Intent(this, CamerasSyncService.class);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationRequest = LocationRequest.create().setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        
        prepareMap();
        mGoogleApiClient.connect();
        
        timer = new Timer();
        timer.schedule(new VesselsTimerTask(), 0, 30000); // Schedule vessels to update every 30 seconds
        
        IntentFilter camerasFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasReceiver, camerasFilter); 
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        
        timer.cancel();
        this.unregisterReceiver(mCamerasReceiver);
    }
	
	public void prepareMap() {
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapview)).getMap();

            if (map != null) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.setTrafficEnabled(true);
                map.setMyLocationEnabled(true);
                map.setOnMyLocationButtonClickListener(this);
                
                LatLng latLng = new LatLng(47.565125, -122.480508);
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                map.animateCamera(CameraUpdateFactory.zoomTo(11));
                map.setOnMarkerClickListener(this);
                map.setOnCameraChangeListener(this);
            }
        }
	}

    public void onCameraChange(CameraPosition cameraPosition) {
        setSupportProgressBarIndeterminateVisibility(true);
        startService(camerasIntent);        
    }

    public boolean onMarkerClick(Marker marker) {
        Bundle b = new Bundle();
        Intent intent = new Intent();        
        
        if (markers.get(marker).equalsIgnoreCase("vessel")) {
            intent.setClass(this, VesselWatchDetailsActivity.class);
            b.putString("title", marker.getTitle());
            b.putString("description", marker.getSnippet());
            intent.putExtras(b);
            this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("camera")) {
            intent.setClass(this, CameraActivity.class);
            b.putInt("id", Integer.parseInt(marker.getSnippet()));
            intent.putExtras(b);
            this.startActivity(intent);            
        }
        
        return true;
    }
	
	public class CamerasSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			
			if (responseString != null) {
				if (responseString.equals("OK") || responseString.equals("NOP")) {
					// We've got cameras, now add them.
					if (camerasOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
						camerasOverlayTask = new CamerasOverlayTask().execute();
					} else if (camerasOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
						camerasOverlayTask.execute();
					}
				} else {
					Log.e("CameraSyncReceiver", "Received an error. Not executing OverlayTask.");
				}
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
	    getMenuInflater().inflate(R.menu.vessel_watch, menu);

	    if (showCameras) {
	    	menu.getItem(0).setTitle("Hide Cameras");
	    } else {
	    	menu.getItem(0).setTitle("Show Cameras");
	    }	    
	    
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {

	    case android.R.id.home:
	    	finish();
	    	return true;	    
	    case R.id.goto_anacortes:
	    	goToLocation(48.535868, -123.013808, 10);
	    	return true;
	    case R.id.goto_edmonds:
	    	goToLocation(47.803096, -122.438718, 12);
	    	return true;
	    case R.id.goto_fauntleroy:
	    	goToLocation(47.513625, -122.450820, 13);
	    	return true;
	    case R.id.goto_mukilteo:
	    	goToLocation(47.963857, -122.327721, 13);
	    	return true;
	    case R.id.goto_pointdefiance:
	    	goToLocation(47.319040, -122.510890, 13);
	    	return true;
	    case R.id.goto_porttownsend:
	    	goToLocation(48.135562, -122.714449, 12);
	    	return true;
	    case R.id.goto_sanjuanislands:
	    	goToLocation(48.557233, -122.897078, 12);
	    	return true;
	    case R.id.goto_seattle:
	    	goToLocation(47.565125, -122.480508, 11);
	    	return true;
	    case R.id.goto_seattlebainbridge:
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
		if (showCameras) {
			for(Entry<Marker, String> entry : markers.entrySet()) {
			    Marker key = entry.getKey();
			    String value = entry.getValue();
			    
			    if (value.equalsIgnoreCase("camera")) {
			        key.setVisible(false);
			    }
			}
			
			item.setTitle("Show Cameras");
			showCameras = false;
		} else {
            for(Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();
                
                if (value.equalsIgnoreCase("camera")) {
                    key.setVisible(true);
                }
            }
            
			item.setTitle("Hide Cameras");
			showCameras = true;
		}		

		// Save camera display preference
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("KEY_SHOW_CAMERAS", showCameras);
		editor.commit();
	}	
	
	public void goToLocation(double latitude, double longitude, int zoomLevel) {	
        LatLng latLng = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
	}	
	
    public class VesselsTimerTask extends TimerTask {
        private Runnable runnable = new Runnable() {
            public void run() {
                if (vesselsOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
                    vesselsOverlayTask = new VesselsOverlayTask().execute();
                } else if (vesselsOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
                    vesselsOverlayTask.execute();
                }
            }
        };

        public void run() {
            handler.post(runnable);
        }
    }
	
	class CamerasOverlayTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		public void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
            
			camerasOverlay = null;
			bounds = map.getProjection().getVisibleRegion().latLngBounds;

		 }
		
		 @Override
		 public Void doInBackground(Void... unused) {
			 camerasOverlay = new CamerasOverlay(
					 VesselWatchMapActivity.this,
					 bounds,
					 "ferries");		 
			 
			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
            Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Marker, String> entry = iter.next();
                if (entry.getValue().equalsIgnoreCase("camera")) {
                    entry.getKey().remove();
                    iter.remove();
                }
            }
            cameras.clear();
            cameras = camerasOverlay.getCameraMarkers();
             
             if (cameras != null) {
                 if (cameras.size() != 0) {
                     for (int i = 0; i < cameras.size(); i++) {
                         LatLng latLng = new LatLng(cameras.get(i).getLatitude(), cameras.get(i).getLongitude());
                         Marker marker = map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(cameras.get(i).getTitle())
                            .snippet(cameras.get(i).getCameraId().toString())
                            .icon(BitmapDescriptorFactory.fromResource(cameras.get(i).getCameraIcon()))
                            .visible(showCameras));
                         
                         markers.put(marker, "camera");
                     }
                 }
             }
            
            setSupportProgressBarIndeterminateVisibility(false);
		 }
	}
	
	class VesselsOverlayTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		public void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);

			vesselsOverlay = null;
		 }
		
		 @Override
		 public Void doInBackground(Void... unused) {
		     vesselsOverlay = new VesselsOverlay(getString(R.string.wsdot_api_access_code));
			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
            Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Marker, String> entry = iter.next();
                if (entry.getValue().equalsIgnoreCase("vessel")) {
                    entry.getKey().remove();
                    iter.remove();
                }
            }
		     vessels.clear();
		     vessels = vesselsOverlay.getVesselWatchItems();
		     
		     if (vessels != null) {
				if (vessels.size() != 0) {
				    for (int i = 0; i < vessels.size(); i++) {
				        LatLng latLng = new LatLng(vessels.get(i).getLat(), vessels.get(i).getLon());
				        Marker marker = map.addMarker(new MarkerOptions()
				            .position(latLng)
				            .title(vessels.get(i).getName())
				            .snippet(vessels.get(i).getDescription())
				            .icon(BitmapDescriptorFactory.fromResource(vessels.get(i).getIcon()))
				            .visible(true));
				        
				        markers.put(marker, "vessel");
				    }
				}
			}
			
			setSupportProgressBarIndeterminateVisibility(false);
		 }
	}

    public boolean onMyLocationButtonClick() {
        Location location = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        
        if (location == null) {
            requestLocationUpdates();
        }
        else {
            handleNewLocation(location);
        };

        return true;
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
        
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        
        if (location == null) {
            requestLocationUpdates();
        }
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
        map.animateCamera(cameraUpdate);   
    }
    
    /**
     * Request location updates after checking permissions first.
     */
    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(VesselWatchMapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    VesselWatchMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show explanation to user explaining why we need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("To receive relevant location based notifications you must allow us access to your location.");
                builder.setTitle("Location Services");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(
                                        VesselWatchMapActivity.this,
                                        new String[] {
                                                Manifest.permission.ACCESS_FINE_LOCATION },
                                        REQUEST_ACCESS_FINE_LOCATION);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();

            } else {
                // No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(VesselWatchMapActivity.this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION },
                        REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Log.i(TAG, "Request permissions granted!!!");
                } else {
                    // Permission was denied or request was cancelled
                    Log.i(TAG, "Request permissions denied...");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
