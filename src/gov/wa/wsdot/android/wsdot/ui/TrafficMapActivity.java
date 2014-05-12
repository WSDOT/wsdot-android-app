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
import gov.wa.wsdot.android.wsdot.service.HighwayAlertsSyncService;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.LatLonItem;
import gov.wa.wsdot.android.wsdot.ui.map.CamerasOverlay;
import gov.wa.wsdot.android.wsdot.ui.map.HighwayAlertsOverlay;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.*;

public class TrafficMapActivity extends ActionBarActivity {
	
    private static final String TAG = TrafficMapActivity.class.getSimpleName();
    private GoogleMap map = null;
	//private HighwayAlertsOverlay alerts = null;
	private CamerasOverlay camerasOverlay = null;
	private List<CameraItem> cameras = new ArrayList<CameraItem>();
	private List<Marker> markers = new ArrayList<Marker>();
	boolean showCameras;
	boolean showShadows;
	private ArrayList<LatLonItem> seattleArea = new ArrayList<LatLonItem>();
	
	static final private int MENU_ITEM_SEATTLE_ALERTS = Menu.FIRST;
	static final private int MENU_ITEM_EXPRESS_LANES = Menu.FIRST + 1;
	
	private CamerasSyncReceiver mCamerasReceiver;
	//private HighwayAlertsSyncReceiver mHighwayAlertsSyncReceiver;
	private Intent camerasIntent;
	private static AsyncTask<Void, Void, Void> mCamerasOverlayTask = null;
	private LatLngBounds bounds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        prepareBoundingBox();
        
        // Initialize AsyncTask
        mCamerasOverlayTask = new CamerasOverlayTask();
		
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
    
		camerasIntent = new Intent(this.getApplicationContext(), CamerasSyncService.class);
		//setSupportProgressBarIndeterminateVisibility(true);
		startService(camerasIntent);

		//Intent alertsIntent = new Intent(this.getApplicationContext(), HighwayAlertsSyncService.class);
		//setSupportProgressBarIndeterminateVisibility(true);
		//startService(alertsIntent);
    }
	
	public void prepareBoundingBox() {
		seattleArea.add(new LatLonItem(48.01749, -122.46185));
		seattleArea.add(new LatLonItem(48.01565, -121.86584));
		seattleArea.add(new LatLonItem(47.27737, -121.86310));
		seattleArea.add(new LatLonItem(47.28109, -122.45911));
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
        
        map.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
               Bundle b = new Bundle();
               Intent intent = new Intent(TrafficMapActivity.this, CameraActivity.class);
               b.putInt("id", Integer.parseInt(marker.getSnippet()));
               intent.putExtras(b);
               TrafficMapActivity.this.startActivity(intent);

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
	protected void onResume() {
		super.onResume();
        
        IntentFilter camerasFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasReceiver, camerasFilter); 
        
        //IntentFilter alertsFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.HIGHWAY_ALERTS_RESPONSE");
        //alertsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        //mHighwayAlertsSyncReceiver = new HighwayAlertsSyncReceiver();
        //registerReceiver(mHighwayAlertsSyncReceiver, alertsFilter); 
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		this.unregisterReceiver(mCamerasReceiver);
		//this.unregisterReceiver(mHighwayAlertsSyncReceiver);
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
				Log.e("CameraDownloadReceiver", responseString);
			}
		}
	}
	
	/*
	public class HighwayAlertsSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			if (responseString.equals("OK") || responseString.equals("NOP")) {
				new HighwayAlertsOverlayTask().execute(); // We've got alerts, now add them.
			} else {
				Log.e("HighwayAlertsSyncReceiver", responseString);
				
				if (!UIUtils.isNetworkAvailable(context)) {
					responseString = getString(R.string.no_connection);
				}
				
				Toast.makeText(context, responseString, Toast.LENGTH_LONG).show();
			}
		}
	}
	*/
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		LatLng center = map.getCameraPosition().target;
	    getMenuInflater().inflate(R.menu.traffic, menu);
	    
	    if (showCameras) {
	    	menu.getItem(0).setTitle("Hide Cameras");
	    } else {
	    	menu.getItem(0).setTitle("Show Cameras");
	    }

	    /**
	     * Check if current location is within a lat/lon bounding box surrounding
	     * the greater Seattle area.
	     */
		if (inPolygon(seattleArea, center.latitude, center.longitude)) {
			MenuItem menuItem_Alerts = menu.add(0, MENU_ITEM_SEATTLE_ALERTS, menu.size(), "Seattle Alerts")
				.setIcon(R.drawable.ic_menu_alerts);
			
            MenuItemCompat.setShowAsAction(menuItem_Alerts,
                    MenuItemCompat.SHOW_AS_ACTION_IF_ROOM | MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);

		    MenuItem menuItem_Lanes = menu.add(0, MENU_ITEM_EXPRESS_LANES, menu.size(), "Express Lanes");
		    MenuItemCompat.setShowAsAction(menuItem_Lanes, MenuItemCompat.SHOW_AS_ACTION_NEVER);
		}
	    
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {

	    case android.R.id.home:
	    	finish();
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
        case R.id.goto_snoqualmiepass:
            AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Snoqualmie Pass");
            goToLocation("Snoqualmie Pass Traffic", 47.4216734, -121.4232569, 13);
            UIUtils.refreshActionBarMenu(this);
            return true;
        case R.id.goto_tricities:
            AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Tri-Cities");
            goToLocation("Tri-Cities Traffic", 46.2577199, -119.1813155, 13);
            UIUtils.refreshActionBarMenu(this);
            return true;
        case R.id.goto_yakima:
            AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Yakima");
            goToLocation("Yakima Traffic", 46.6063273, -120.4886952, 13);
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

			for (Marker marker: markers) {
			    marker.setVisible(false);
			}
			
			item.setTitle("Show Cameras");
			showCameras = false;
		} else {
			AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/Show Cameras");

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

	public void goToLocation(String title, double latitude, double longitude, int zoomLevel) {	
        LatLng latLng = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
	}

	/**
	 * Iterate through collection of LatLon objects in arrayList and see
	 * if passed latitude and longitude point is within the collection.
	 */
	public boolean inPolygon(ArrayList<LatLonItem> points, double latitude, double longitude) {	
		int j = points.size() - 1;
		double lat = latitude;
		double lon = longitude;		
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
					 TrafficMapActivity.this,
					 bounds,
					 null);

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
			if (alerts != null) {
				if (alerts.size() != 0) {
					map.getOverlays().add(alerts);
				}
			}
			
			setSupportProgressBarIndeterminateVisibility(false);
			map.invalidate();
		 }
	}
	*/
	
}
