/*
 * Copyright (c) 2014 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.service.CamerasSyncService;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.ui.map.CamerasOverlay;
import gov.wa.wsdot.android.wsdot.ui.map.VesselsOverlay;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class VesselWatchMapActivity extends ActionBarActivity {

	private static final String TAG = VesselWatchMapActivity.class.getSimpleName();
	private GoogleMap map = null;
	private Handler handler = new Handler();
	private Timer timer;
	private VesselsOverlay vessels = null;
	private CamerasOverlay camerasOverlay = null;
	private List<CameraItem> cameras = new ArrayList<CameraItem>();
	private List<Marker> markers = new ArrayList<Marker>();	
	boolean showCameras;
	boolean showShadows;
	
	private CamerasSyncReceiver mCamerasReceiver;
	private Intent camerasIntent;
	private static AsyncTask<Void, Void, Void> mCamerasOverlayTask = null;
	private LatLngBounds bounds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        
        // Initialize AsyncTask
        mCamerasOverlayTask = new CamerasOverlayTask();

        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
        
		camerasIntent = new Intent(this.getApplicationContext(), CamerasSyncService.class);
		startService(camerasIntent);
    }
	
	public void prepareMap() {
		setContentView(R.layout.map);
		//setSupportProgressBarIndeterminateVisibility(false);
		
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment =  (SupportMapFragment)
            fragmentManager.findFragmentById(R.id.mapview);

        map = mapFragment.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setTrafficEnabled(true);
        map.setMyLocationEnabled(true);
        
        LatLng latLng = new LatLng(47.565125, -122.480508);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(11));
        
        map.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
               Bundle b = new Bundle();
               Intent intent = new Intent(VesselWatchMapActivity.this, CameraActivity.class);
               b.putInt("id", Integer.parseInt(marker.getSnippet()));
               intent.putExtras(b);
               VesselWatchMapActivity.this.startActivity(intent);

               return true;
            }
        });
        
        map.setOnCameraChangeListener(new OnCameraChangeListener() {
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(TAG, "onCameraChange");
                startService(camerasIntent);
            }
        });
	}

	@Override
	protected void onPause() {
		super.onPause();
		timer.cancel();
		this.unregisterReceiver(mCamerasReceiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		timer = new Timer();
		timer.schedule(new VesselsTimerTask(), 0, 30000); // Schedule vessels to update every 30 seconds
		
        IntentFilter camerasFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasReceiver, camerasFilter); 
	}
	
	public class CamerasSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			if (responseString.equals("OK") || responseString.equals("NOP")) {
				// We've got cameras, now add them.
				if (mCamerasOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
					mCamerasOverlayTask = new CamerasOverlayTask().execute();
				} else if (mCamerasOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
					mCamerasOverlayTask.execute();
				}
			} else {
				Log.e("CameraSyncReceiver", "Received an error. Not executing OverlayTask.");
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
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Anacortes");
	    	goToLocation(48.535868, -123.013808, 10);
	    	return true;
	    case R.id.goto_edmonds:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Edmonds");
	    	goToLocation(47.803096, -122.438718, 12);
	    	return true;
	    case R.id.goto_fauntleroy:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Fauntleroy");
	    	goToLocation(47.513625, -122.450820, 13);
	    	return true;
	    case R.id.goto_mukilteo:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Mukilteo");
	    	goToLocation(47.963857, -122.327721, 13);
	    	return true;
	    case R.id.goto_pointdefiance:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Pt Defiance");
	    	goToLocation(47.319040, -122.510890, 13);
	    	return true;
	    case R.id.goto_porttownsend:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Port Townsend");
	    	goToLocation(48.135562, -122.714449, 12);
	    	return true;
	    case R.id.goto_sanjuanislands:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/San Juan Islands");
	    	goToLocation(48.557233, -122.897078, 12);
	    	return true;
	    case R.id.goto_seattle:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Seattle");
	    	goToLocation(47.565125, -122.480508, 11);
	    	return true;
	    case R.id.goto_seattlebainbridge:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Seattle-Bainbridge");
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
			AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/Hide Cameras");
            
			for (Marker marker: markers) {
                marker.setVisible(false);
            }
			
			item.setTitle("Show Cameras");
			showCameras = false;
		} else {
			AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/Show Cameras");
            
			for (Marker marker: markers) {
                marker.setVisible(true);
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
                //new VesselsOverlayTask().execute();
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

			if (camerasOverlay != null) {
                for (Marker marker: markers) {
                    marker.remove();
                }
				camerasOverlay = null;
			}
			
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
             markers.clear();
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
                         
                         markers.add(marker);
                     }
                 }
             }
            
            setSupportProgressBarIndeterminateVisibility(false);
		 }
	}
	
	/*
	class VesselsOverlayTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		public void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
			
			if (vessels != null) {
				map.getOverlays().remove(vessels);
				vessels = null;
			}

		 }
		
		 @Override
		 public Void doInBackground(Void... unused) {
			 vessels = new VesselsOverlay(VesselWatchMapActivity.this);
			 
			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
			if (vessels != null) {
				if (vessels.size() != 0) {
					map.getOverlays().add(vessels);
					map.invalidate();
				}
			}
			
			setSupportProgressBarIndeterminateVisibility(false);
		 }
	}
	*/

}
