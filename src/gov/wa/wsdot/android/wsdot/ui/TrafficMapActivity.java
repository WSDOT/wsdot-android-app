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
import gov.wa.wsdot.android.wsdot.service.HighwayAlertsSyncService;
import gov.wa.wsdot.android.wsdot.shared.LatLonItem;
import gov.wa.wsdot.android.wsdot.ui.map.CamerasOverlay;
import gov.wa.wsdot.android.wsdot.ui.map.HighwayAlertsOverlay;
import gov.wa.wsdot.android.wsdot.ui.widget.MyMapView;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.FixedMyLocationOverlay;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class TrafficMapActivity extends SherlockMapActivity {
	private MyMapView map = null;
	protected MapController mapController = null;
	private HighwayAlertsOverlay alerts = null;
	private CamerasOverlay cameras = null;
	boolean showCameras;
	boolean showShadows;
	private FixedMyLocationOverlay myLocationOverlay;
	private ArrayList<LatLonItem> seattleArea = new ArrayList<LatLonItem>();
	
	static final private int MENU_ITEM_SEATTLE_ALERTS = Menu.FIRST;
	static final private int MENU_ITEM_EXPRESS_LANES = Menu.FIRST + 1;
	
	private CamerasSyncReceiver mCamerasReceiver;
	private HighwayAlertsSyncReceiver mHighwayAlertsSyncReceiver;
	private Intent camerasIntent;
	private static AsyncTask<Void, Void, Void> mCamerasOverlayTask = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        prepareBoundingBox();
        
        // Initialize AsyncTask
        mCamerasOverlayTask = new CamerasOverlayTask();
        
        IntentFilter camerasFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasReceiver, camerasFilter); 
        
        IntentFilter alertsFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.HIGHWAY_ALERTS_RESPONSE");
        alertsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mHighwayAlertsSyncReceiver = new HighwayAlertsSyncReceiver();
        registerReceiver(mHighwayAlertsSyncReceiver, alertsFilter);         
        
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
		
		// Will be executed as soon as we have a location fix
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
            	map.getController().animateTo(myLocationOverlay.getMyLocation());
            }
        });
        
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
    
		camerasIntent = new Intent(TrafficMapActivity.this, CamerasSyncService.class);
		startService(camerasIntent);        

		Intent alertsIntent = new Intent(TrafficMapActivity.this, HighwayAlertsSyncService.class);
		startService(alertsIntent);
    }
	
	public void prepareBoundingBox() {
		seattleArea.add(new LatLonItem(48.01749, -122.46185));
		seattleArea.add(new LatLonItem(48.01565, -121.86584));
		seattleArea.add(new LatLonItem(47.27737, -121.86310));
		seattleArea.add(new LatLonItem(47.28109, -122.45911));
	}
	
	public void prepareMap() {
		setContentView(R.layout.map);
		setSupportProgressBarIndeterminateVisibility(false);
	
        map = (MyMapView) findViewById(R.id.mapview);
        map.setSatellite(false);
        map.setBuiltInZoomControls(true);
        map.setTraffic(true);
        map.getController().setZoom(13);
        
        map.setOnChangeListener(new MapViewChangeListener());
	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mCamerasReceiver);
		this.unregisterReceiver(mHighwayAlertsSyncReceiver);
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
			if (responseString.equals("OK") || responseString.equals("NOOP")) {
				// We've got cameras, now add them.
				if (mCamerasOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
					mCamerasOverlayTask = new CamerasOverlayTask().execute();
				} else if (mCamerasOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
					mCamerasOverlayTask.execute();
				}
			} else {
				Log.e("CameraDownloadReceiver", "Received an error. Not executing OverlayTask.");
				Toast.makeText(TrafficMapActivity.this, responseString, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public class HighwayAlertsSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			if (responseString.equals("OK") || responseString.equals("NOOP")) {
				new HighwayAlertsOverlayTask().execute(); // We've got alerts, now add them.
			} else {
				Log.e("HighwayAlertsSyncReceiver", "Received an error. Not executing OverlayTask.");
				Toast.makeText(TrafficMapActivity.this, responseString, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		GeoPoint p = map.getMapCenter();
	    getSupportMenuInflater().inflate(R.menu.traffic, menu);
	    
	    if (showCameras) {
	    	menu.getItem(1).setTitle("Hide Cameras");
	    } else {
	    	menu.getItem(1).setTitle("Show Cameras");
	    }

	    /**
	     * Check if current location is within a lat/lon bounding box surrounding
	     * the greater Seattle area.
	     */
		if (inPolygon(seattleArea, p.getLatitudeE6(), p.getLongitudeE6())) {
			menu.add(0, MENU_ITEM_SEATTLE_ALERTS, menu.size(), "Seattle Alerts")
				.setIcon(R.drawable.ic_menu_alerts)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		    menu.add(0, MENU_ITEM_EXPRESS_LANES, menu.size(), "Express Lanes")
		    	.setIcon(R.drawable.ic_menu_express_lanes)
		    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/My Location");
	    	myLocationOverlay.runOnFirstFix(new Runnable() {
	            public void run() {	    	
	            	map.getController().animateTo(myLocationOverlay.getMyLocation());
	            }
	        });
	        UIUtils.refreshActionBarMenu(this);
	        return true;
	    case R.id.toggle_cameras:
	    	toggleCameras(item);
	    	return true;	        
	    case R.id.travel_times:
	    	Intent timesIntent = new Intent(this, TravelTimesActivity.class);
	    	startActivity(timesIntent);
	    	return true;
	    case R.id.goto_bellingham:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Bellingham");
	    	goToLocation("Bellingham Traffic", 48.756302,-122.46151, 12);
	    	UIUtils.refreshActionBarMenu(this);
	    	return true;	        
	    case R.id.goto_chehalis:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Chehalis");
	    	goToLocation("Chelalis Traffic", 46.635529, -122.937698, 13);
	    	UIUtils.refreshActionBarMenu(this);
	    	return true;
	    case R.id.goto_hoodcanal:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Hood Canal");
	    	goToLocation("Hood Canal Traffic", 47.85268,-122.628365, 13);
	    	UIUtils.refreshActionBarMenu(this);
	    	return true;
	    case R.id.goto_mtvernon:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Mt Vernon");
	    	goToLocation("Mt Vernon Traffic", 48.420657,-122.334824, 13);
	    	UIUtils.refreshActionBarMenu(this);
	    	return true;
	    case R.id.goto_stanwood:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Stanwood");
	    	goToLocation("Stanwood Traffic", 48.22959, -122.34581, 13);
	    	UIUtils.refreshActionBarMenu(this);
	    	return true;
	    case R.id.goto_monroe:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Monroe");
	    	goToLocation("Monroe Traffic", 47.859476, -121.972446, 14);
	    	UIUtils.refreshActionBarMenu(this);
	    	return true;
	    case R.id.goto_sultan:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Sultan");
	    	goToLocation("Sultan Traffic", 47.86034, -121.812286, 14);
	    	UIUtils.refreshActionBarMenu(this);
	    	return true;
	    case R.id.goto_olympia:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Olympia");
	    	goToLocation("Olympia Traffic", 47.021461, -122.899933, 13);
	    	UIUtils.refreshActionBarMenu(this);
	        return true;	    	    	
	    case R.id.goto_seattle:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Seattle");
	    	goToLocation("Seattle Area Traffic", 47.5990, -122.3350, 12);
	    	UIUtils.refreshActionBarMenu(this);
	        return true;
	    case R.id.goto_spokane:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Spokane");
	    	goToLocation("Spokane Area Traffic", 47.658566, -117.425995, 12);
	    	UIUtils.refreshActionBarMenu(this);
	        return true;	        
	    case R.id.goto_tacoma:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Tacoma");
	    	goToLocation("Tacoma Traffic", 47.206275, -122.46254, 12);
	    	UIUtils.refreshActionBarMenu(this);
	        return true;	        
	    case R.id.goto_vancouver:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Vancouver");
	    	goToLocation("Vancouver Area Traffic", 45.639968, -122.610512, 12);
	    	UIUtils.refreshActionBarMenu(this);
	        return true;
	    case R.id.goto_wenatchee:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Wenatchee");
	    	goToLocation("Wenatchee Traffic", 47.435867, -120.309563, 13);
	    	UIUtils.refreshActionBarMenu(this);
	        return true;
	    case MENU_ITEM_SEATTLE_ALERTS:
	    	Intent alertsIntent = new Intent(this, SeattleTrafficAlertsActivity.class);
	    	startActivity(alertsIntent);
	    	return true;
	    case MENU_ITEM_EXPRESS_LANES:
	    	Intent expressIntent = new Intent(this, SeattleExpressLanesActivity.class);
	    	startActivity(expressIntent);
	    	return true;
	    }
	    
	    return super.onOptionsItemSelected(item);
	}

	private void toggleCameras(MenuItem item) {
		if (showCameras) {
			AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/Hide Cameras");
			map.getOverlays().remove(cameras);
			map.invalidate();
			item.setTitle("Show Cameras");
			showCameras = false;
		} else {
			AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/Show Cameras");
			map.getOverlays().add(cameras);
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

	public void goToLocation(String title, double latitude, double longitude, int zoomLevel) {	
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        map.getController().setZoom(zoomLevel);
        map.getController().setCenter(newPoint);
	}

	/**
	 * Iterate through collection of LatLon objects in arrayList and see
	 * if passed latitude and longitude point is within the collection.
	 */	
	public boolean inPolygon(ArrayList<LatLonItem> points, int latitude, int longitude) {	
		int j = points.size() - 1;
		double lat = (double)(latitude / 1E6);
		double lon = (double)(longitude / 1E6);		
		boolean inPoly = false;
		
		for (int i = 0; i < points.size(); i++) {
			if ( (points.get(i).getLongitude() < lon && points.get(j).getLongitude() >= lon) || 
					(points.get(j).getLongitude() < lon && points.get(i).getLongitude() >= lon) ) {
						if ( points.get(i).getLatitude() + (lon - points.get(i).getLongitude()) / 
								(points.get(j).getLongitude() - points.get(i).getLongitude()) * 
									(points.get(j).getLatitude() - points.get(i).getLatitude()) < lat ) {
										inPoly = !inPoly;
						}
			}
			j = i;
		}
		return inPoly;
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
					 TrafficMapActivity.this,
					 topLatitude, leftLongitude,
					 bottomLatitude, rightLongitude,
					 null);		 

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

	class HighwayAlertsOverlayTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		public void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
			
			if (alerts != null) {
				map.getOverlays().remove(alerts);
				alerts = null;
			}

		 }
		
		 @Override
		 public Void doInBackground(Void... unused) {
			 alerts = new HighwayAlertsOverlay(TrafficMapActivity.this);
			 
			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
			if (alerts.size() != 0) {
				 map.getOverlays().add(alerts);				
			}
			
			setSupportProgressBarIndeterminateVisibility(false);
			map.invalidate();
		 }
	}
	
}
