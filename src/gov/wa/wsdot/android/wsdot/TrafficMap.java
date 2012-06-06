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

package gov.wa.wsdot.android.wsdot;

import gov.wa.wsdot.android.wsdot.shared.LatLonItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.FixedMyLocationOverlay;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class TrafficMap extends SherlockMapActivity {
	
	private static final String DEBUG_TAG = "TrafficMap";
	private HashMap<Integer, String[]> eventCategories = new HashMap<Integer, String[]>();
	protected MapView map = null;
	protected MapController mapController = null;
	private AlertsOverlay alerts = null;
	private CamerasOverlay cameras = null;
	boolean showCameras;
	boolean showShadows;
	private FixedMyLocationOverlay myLocationOverlay;
	private ArrayList<LatLonItem> seattleArea = new ArrayList<LatLonItem>();
	
	static final private int MENU_ITEM_SEATTLE_ALERTS = Menu.FIRST;
	static final private int MENU_ITEM_TRAVEL_TIMES = Menu.FIRST + 1;
	static final private int MENU_ITEM_EXPRESS_LANES = Menu.FIRST + 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        prepareBoundingBox();
        
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
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);
        buildEventCategories();       

        new OverlayTask().execute();
    }
	
	public void prepareBoundingBox() {
		seattleArea.add(new LatLonItem(48.01749, -122.46185));
		seattleArea.add(new LatLonItem(48.01565, -121.86584));
		seattleArea.add(new LatLonItem(47.27737, -121.86310));
		seattleArea.add(new LatLonItem(47.28109, -122.45911));
	}
	
	public void prepareMap() {
		setContentView(R.layout.map);
	
        map = (MapView) findViewById(R.id.mapview);
        map.setSatellite(false);
        map.setBuiltInZoomControls(true);
        map.setTraffic(true);
        map.getController().setZoom(13);
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		GeoPoint p = map.getMapCenter();
	    getSupportMenuInflater().inflate(R.menu.traffic_menu, menu);
	    
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
			
		    menu.add(0, MENU_ITEM_TRAVEL_TIMES, menu.size(), "Travel Times")
		    	.setIcon(R.drawable.ic_menu_travel_times)
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
	        return true;
	    case R.id.goto_bellingham:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Bellingham");
	    	goToLocation("Bellingham Traffic", 48.756302,-122.46151, 12);
	    	return true;	        
	    case R.id.goto_chehalis:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Chehalis");
	    	goToLocation("Chelalis Traffic", 46.635529, -122.937698, 13);
	    	return true;
	    case R.id.goto_hoodcanal:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Hood Canal");
	    	goToLocation("Hood Canal Traffic", 47.85268,-122.628365, 13);
	    	return true;
	    case R.id.goto_mtvernon:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Mt Vernon");
	    	goToLocation("Mt Vernon Traffic", 48.420657,-122.334824, 13);
	    	return true;
	    case R.id.goto_stanwood:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Stanwood");
	    	goToLocation("Stanwood Traffic", 48.22959, -122.34581, 13);
	    	return true;
	    case R.id.goto_monroe:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Monroe");
	    	goToLocation("Monroe Traffic", 47.859476, -121.972446, 14);
	    	return true;
	    case R.id.goto_sultan:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Sultan");
	    	goToLocation("Sultan Traffic", 47.86034, -121.812286, 14);
	    	return true;
	    case R.id.goto_olympia:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Olympia");
	    	goToLocation("Olympia Traffic", 47.021461, -122.899933, 13);
	        return true;	    	    	
	    case R.id.goto_seattle:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Seattle");
	    	goToLocation("Seattle Area Traffic", 47.5990, -122.3350, 12);
	        return true;
	    case R.id.goto_spokane:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Spokane");
	    	goToLocation("Spokane Area Traffic", 47.658566, -117.425995, 12);
	        return true;	        
	    case R.id.goto_tacoma:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Tacoma");
	    	goToLocation("Tacoma Traffic", 47.206275, -122.46254, 12);
	        return true;	        
	    case R.id.goto_vancouver:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Vancouver");
	    	goToLocation("Vancouver Area Traffic", 45.639968, -122.610512, 12);
	        return true;
	    case R.id.goto_wenatchee:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/GoTo Location/Wenatchee");
	    	goToLocation("Wenatchee Traffic", 47.435867, -120.309563, 13);
	        return true;
	    case R.id.toggle_cameras:
	    	toggleCameras(item);
	    	return true;	        
	    case MENU_ITEM_SEATTLE_ALERTS:
	    	Intent alertsIntent = new Intent(this, SeattleTrafficAlerts.class);
	    	startActivity(alertsIntent);
	    	return true;
	    case MENU_ITEM_TRAVEL_TIMES:
	    	Intent timesIntent = new Intent(this, SeattleTrafficTravelTimes.class);
	    	startActivity(timesIntent);
	    	return true;
	    case MENU_ITEM_EXPRESS_LANES:
	    	Intent expressIntent = new Intent(this, SeattleExpressLanes.class);
	    	startActivity(expressIntent);
	    	return true;	    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
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
	
	private void buildEventCategories() {
		String[] event_construction = {"construction", "maintenance"};
		String[] event_closure = {"closure", "hcb closed marine"};
		
		eventCategories.put(R.drawable.closed, event_closure);
		eventCategories.put(R.drawable.construction_high, event_construction);
	}

	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1E6), (int)(lon*1E6)));
	 }	
	
	private class AlertsOverlay extends ItemizedOverlay<AlertItem> {
		private List<AlertItem> alertItems = new ArrayList<AlertItem>();

		public AlertsOverlay() {
			super(null);			
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/HighwayAlerts.js.gz");
				URLConnection urlConn = url.openConnection();
				
				BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);
				
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
			
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("alerts");
				JSONArray items = result.getJSONArray("items");

				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					JSONObject startRoadwayLocation = item.getJSONObject("StartRoadwayLocation");
					
					alertItems.add(new AlertItem(getPoint(startRoadwayLocation.getDouble("Latitude"), startRoadwayLocation.getDouble("Longitude")),
							item.getString("EventCategory"),
							item.getString("HeadlineDescription"),
							getMarker(getCategoryIcon(eventCategories, item.getString("EventCategory")))));
				}
				 
			 } catch (Exception e) {
				 Log.e(DEBUG_TAG, "Error in network call", e);
			 }			 
			 
			 populate();
		}
		
		@Override
		protected AlertItem createItem(int i) {
			return(alertItems.get(i));
		}
		
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if (!showShadows) {
				shadow = false;
			}
			super.draw(canvas, mapView, shadow);
		}

		@Override
		protected boolean onTap(int i) {
			OverlayItem item = getItem(i);
			Bundle b = new Bundle();
			Intent intent = new Intent(TrafficMap.this, HighwayAlertItemDetails.class);
			b.putString("title", item.getTitle());
			b.putString("description", item.getSnippet());
			intent.putExtras(b);
			startActivity(intent);

			return true;
		} 
		 
		 @Override
		 public int size() {
			 return(alertItems.size());
		 }
		 
		 private Drawable getMarker(int resource) {
			 Drawable marker = getResources().getDrawable(resource);
			 marker.setBounds(0, 0, marker.getIntrinsicWidth(),
			 marker.getIntrinsicHeight());
			 boundCenterBottom(marker);

			 return(marker);
		 }
	}
	
	private class CamerasOverlay extends ItemizedOverlay<CameraItem> {
		private List<CameraItem> cameraItems = new ArrayList<CameraItem>();

		public CamerasOverlay() {
			super(null);	
			
			try {				
				URL url = new URL("http://data.wsdot.wa.gov/mobile/Cameras.js.gz");
				URLConnection urlConn = url.openConnection();
				
				BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);
				
				String jsonFile = "";
				String line;
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("cameras");
				JSONArray items = result.getJSONArray("items");
				int video;

				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					try {
						video = item.getInt("video");
					} catch (Exception e) {
						video = 0;
					}
					int cameraIcon = (video == 0) ? R.drawable.camera : R.drawable.camera_video;

					cameraItems.add(new CameraItem(getPoint(item.getDouble("lat"), item.getDouble("lon")),
							item.getString("title"),
							item.getString("url") + "," + video,
							getMarker(cameraIcon)));
				}
				 
			 } catch (Exception e) {
				 Log.e(DEBUG_TAG, "Error in network call", e);
			 }			 
			 
			 populate();
		}
		
		@Override
		protected CameraItem createItem(int i) {
			return(cameraItems.get(i));
		}
		
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if (!showShadows) {
				shadow = false;
			}
			super.draw(canvas, mapView, shadow);
		}

		@Override
		protected boolean onTap(int i) {
			OverlayItem item = getItem(i);
			Bundle b = new Bundle();
			Intent intent = new Intent(TrafficMap.this, CameraTabs.class);
			b.putString("title", item.getTitle());
			b.putString("url", item.getSnippet());
			intent.putExtras(b);
			startActivity(intent);

			return true;
		} 
		 
		 @Override
		 public int size() {
			 return(cameraItems.size());
		 }
		 
		 private Drawable getMarker(int resource) {
			 Drawable marker = getResources().getDrawable(resource);
			 marker.setBounds(0, 0, marker.getIntrinsicWidth(),
			 marker.getIntrinsicHeight());
			 boundCenterBottom(marker);

			 return(marker);
		 }
	}	
	
	class AlertItem extends OverlayItem {
		 Drawable marker = null;
	
		 AlertItem(GeoPoint pt, String title, String description, Drawable marker) {
			 super(pt, title, description);
			 this.marker = marker;
		 }

		 @Override
		 public Drawable getMarker(int stateBitset) {
			 Drawable result = marker;
			 setState(result, stateBitset);

			 return result;
		 }
	}
	
	class CameraItem extends OverlayItem {
		 Drawable marker = null;
	
		 CameraItem(GeoPoint pt, String title, String description, Drawable marker) {
			 super(pt, title, description);
			 this.marker = marker;
		 }

		 @Override
		 public Drawable getMarker(int stateBitset) {
			 Drawable result = marker;
			 setState(result, stateBitset);

			 return result;
		 }
	}	
	
	class OverlayTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(TrafficMap.this);
		
		@Override
		public void onPreExecute() {
			if (alerts != null) {
				map.getOverlays().remove(alerts);
				map.invalidate();
				alerts = null;
			}
			if (cameras != null) {
				map.getOverlays().remove(cameras);
				map.invalidate();
				cameras = null;
			}
			
			this.dialog.setMessage("Retrieving latest traffic alerts and camera locations ...");	
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }
			});
			
			this.dialog.show();
		 }

	    protected void onCancelled() {
	        Toast.makeText(TrafficMap.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		 @Override
		 public Void doInBackground(Void... unused) {
			 if (!this.isCancelled()) alerts = new AlertsOverlay();
			 if (!this.isCancelled()) cameras = new CamerasOverlay();		 
			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			map.getOverlays().add(alerts);
			if (showCameras) {
				map.getOverlays().add(cameras);	
			}
			
			map.invalidate();
		 }
	}	
	
	private static Integer getCategoryIcon(HashMap<Integer, String[]> eventCategories, String category) {
		Integer image = R.drawable.alert_highest;
		Set<Entry<Integer, String[]>> set = eventCategories.entrySet();
		Iterator<Entry<Integer, String[]>> i = set.iterator();
		
		if (category.equals("")) return image;
		
		while(i.hasNext()) {
			Entry<Integer, String[]> me = i.next();
			for (String phrase: (String[])me.getValue()) {
				String patternStr = phrase;
				Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(category);
				boolean matchFound = matcher.find();
				if (matchFound) {
					image = (Integer)me.getKey();
				}
			}
		}	
		return image;
	}
}
