/*
 * Copyright (c) 2012 Washington State Department of Transportation
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
import gov.wa.wsdot.android.wsdot.ui.map.CamerasOverlay;
import gov.wa.wsdot.android.wsdot.ui.map.VesselsOverlay;
import gov.wa.wsdot.android.wsdot.ui.widget.MyMapView;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.FixedMyLocationOverlay;

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
import android.util.Log;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class VesselWatchMapActivity extends SherlockMapActivity {

	@SuppressWarnings("unused")
	private static final String DEBUG_TAG = "VesselWatchMap";
	private MyMapView map = null;
	private Handler handler = new Handler();
	private Timer timer;
	private FixedMyLocationOverlay myLocationOverlay;
	private VesselsOverlay vessels = null;
	private CamerasOverlay cameras = null;
	boolean showCameras;
	boolean showShadows;
	
	private CamerasSyncReceiver mCamerasReceiver;
	private Intent camerasIntent;
	private static AsyncTask<Void, Void, Void> mCamerasOverlayTask = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        
        // Initialize AsyncTask
        mCamerasOverlayTask = new CamerasOverlayTask();

        /**
         * Using an extended version of MyLocationOverlay class because it has been
         * reported the Motorola Droid X phones throw an exception when they try to
         * draw the dot showing the location of the device.
         * 
         * See this post titled, "Android applications that use the MyLocationOverlay
         * class crash on the new Droid X"
         * 
         * http://dimitar.me/applications-that-use-the-mylocationoverlay-class-crash-on-the-new-droid-x/
         */
		myLocationOverlay = new FixedMyLocationOverlay(this, map);
		map.getOverlays().add(myLocationOverlay);		

        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);
        
		camerasIntent = new Intent(VesselWatchMapActivity.this, CamerasSyncService.class);
		startService(camerasIntent);
    }
	
	public void prepareMap() {
		setContentView(R.layout.map);
		setSupportProgressBarIndeterminateVisibility(false);
		
		Double latitude = 47.565125;
        Double longitude = -122.480508;
        
        map = (MyMapView) findViewById(R.id.mapview);
        map.setSatellite(false);
        map.getController().setZoom(11);
        map.setBuiltInZoomControls(true);
        map.setTraffic(false);
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        map.getController().animateTo(newPoint);
        map.setOnChangeListener(new MapViewChangeListener());
	}

	@Override
	protected void onPause() {
		super.onPause();
		timer.cancel();
		myLocationOverlay.disableMyLocation();
		this.unregisterReceiver(mCamerasReceiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		timer = new Timer();
		timer.schedule(new VesselsTimerTask(), 0, 30000); // Schedule vessels to update every 30 seconds
		myLocationOverlay.enableMyLocation();
		
        IntentFilter camerasFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasReceiver, camerasFilter); 
	}
	
	private class MapViewChangeListener implements MyMapView.OnChangeListener {

		public void onChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom) {
			if ((!newCenter.equals(oldCenter)) && (newZoom != oldZoom)) {
				startService(camerasIntent);
			}
			else if (!newCenter.equals(oldCenter)) {
				startService(camerasIntent);
			}
			else if (newZoom != oldZoom) {
				startService(camerasIntent);
			}
		}	
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
	    getSupportMenuInflater().inflate(R.menu.vessel_watch, menu);

	    if (showCameras) {
	    	menu.getItem(1).setTitle("Hide Cameras");
	    } else {
	    	menu.getItem(1).setTitle("Show Cameras");
	    }	    
	    
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {

	    case android.R.id.home:
	    	finish();
	    	return true;	    
	    case R.id.my_location:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/My Location");
	        myLocationOverlay.runOnFirstFix(new Runnable() {
	            public void run() {	    	
	            	map.getController().animateTo(myLocationOverlay.getMyLocation());
	            }
	        });
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
			if (cameras != null) {
				map.getOverlays().remove(cameras);
			}
			map.invalidate();
			item.setTitle("Show Cameras");
			showCameras = false;
		} else {
			AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/Show Cameras");
			if (cameras != null) {
				map.getOverlays().add(cameras);
			}
			map.invalidate();
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
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        map.getController().setZoom(zoomLevel);
        map.getController().setCenter(newPoint);
	}	
	
    public class VesselsTimerTask extends TimerTask {
        private Runnable runnable = new Runnable() {
            public void run() {
                new VesselsOverlayTask().execute();
            }
        };

        public void run() {
            handler.post(runnable);
        }
    }
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	class CamerasOverlayTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		public void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);

			if (cameras != null) {
				map.getOverlays().remove(cameras);
				cameras = null;
			}

		 }
		
		 @Override
		 public Void doInBackground(Void... unused) {
			 GeoPoint mapTopLeft = map.getProjection().fromPixels(0, 0);
			 double topLatitude = (double)(mapTopLeft.getLatitudeE6())/1E6;
			 double leftLongitude = (double)(mapTopLeft.getLongitudeE6())/1E6;
   			 GeoPoint mapBottomRight = map.getProjection().fromPixels(map.getWidth(), map.getHeight());
			 double bottomLatitude = (double)(mapBottomRight.getLatitudeE6())/1E6;
			 double rightLongitude = (double)(mapBottomRight.getLongitudeE6())/1E6;			 
			 
			 cameras = new CamerasOverlay(
					 VesselWatchMapActivity.this,
					 topLatitude, leftLongitude,
					 bottomLatitude, rightLongitude,
					 "ferries");		 

			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
			 if (cameras.size() != 0) {
				 map.getOverlays().add(cameras);
			 }
			 
			 if (!showCameras) {
				 map.getOverlays().remove(cameras);
			 }
			
			setSupportProgressBarIndeterminateVisibility(false);
			map.invalidate();
		 }
	}
	
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
			if (vessels.size() != 0) {
				 map.getOverlays().add(vessels);
				 map.invalidate();
			}
			
			setSupportProgressBarIndeterminateVisibility(false);
		 }
	}

}
