/*
 * Copyright (c) 2010 Washington State Department of Transportation
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class TrafficMap extends MapActivity {
	
	private static final String DEBUG_TAG = "TrafficMap";
	private ArrayList<HighwayAlertsItem> highwayAlertsItems = null;
	private ArrayList<CameraItem> cameraItems = null;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	AlertsItemizedOverlay alertsItemizedOverlay;
	CamerasItemizedOverlay camerasItemizedOverlay;
	private HashMap<Integer, String[]> eventCategories = new HashMap<Integer, String[]>();
	MapView map = null;
	boolean showCameras;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true);
 
        buildEventCategories();
        new GetTrafficAlerts().execute();       
    }
	
	abstract void prepareMap();

	private void buildEventCategories() {
		String[] event_construction = {"construction"};
		String[] event_closure = {"closure"};
		
		eventCategories.put(R.drawable.closed, event_closure);
		eventCategories.put(R.drawable.construction_high, event_construction);
	}

	private class GetTrafficAlerts extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(TrafficMap.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Retrieving latest traffic alerts ...");
			this.dialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/HighwayAlerts.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
		        mapOverlays = map.getOverlays();
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("alerts");
				JSONArray items = result.getJSONArray("items");
				highwayAlertsItems = new ArrayList<HighwayAlertsItem>();
				HighwayAlertsItem i = null;
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new HighwayAlertsItem();
					i.setEventCategory(item.getString("EventCategory"));
					Integer catetgoryIcon = getCategoryIcon(eventCategories, item.getString("EventCategory"));
					i.setCategoryIcon(catetgoryIcon);
					i.setHeadlineDescription(item.getString("HeadlineDescription"));
					JSONObject startRoadwayLocation = item.getJSONObject("StartRoadwayLocation");
					i.setStartLatitude(startRoadwayLocation.getDouble("Latitude"));
					i.setStartLongitude(startRoadwayLocation.getDouble("Longitude"));
					highwayAlertsItems.add(i);
			        drawable = getResources().getDrawable(i.getCategoryIcon());
			        alertsItemizedOverlay = new AlertsItemizedOverlay(drawable, TrafficMap.this);				
					GeoPoint point = new GeoPoint((int)(i.getStartLatitude() * 1E6), (int)(i.getStartLongitude() * 1E6));
					OverlayItem overlayitem = new OverlayItem(point, "", i.getHeadlineDescription());
					alertsItemizedOverlay.addOverlay(overlayitem);
					mapOverlays.add(alertsItemizedOverlay);
				}
				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			map.invalidate();
			if (showCameras) {
				new GetCameras().execute();	
			}
		}
	}

	private class GetCameras extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(TrafficMap.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Setting up camera locations ...");
			this.dialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			cameraItems = new ArrayList<CameraItem>();
			CameraItem i = null;
			
			try {
				InputStream is = getResources().openRawResource(R.raw.cameras);
				byte [] buffer = new byte[is.available()];
				while (is.read(buffer) != -1);
				String jsonFile = new String(buffer);
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("cameras");
				JSONArray items = result.getJSONArray("items");
		        mapOverlays = map.getOverlays();
				
				for (int j=0; j < items.length(); j++) {
					i = new CameraItem();
					JSONObject item = items.getJSONObject(j);
			        drawable = getResources().getDrawable(R.drawable.camera);
			        camerasItemizedOverlay = new CamerasItemizedOverlay(drawable, TrafficMap.this);      
			        i.setTitle(item.getString("title"));
			        i.setImageUrl(item.getString("url"));
			        i.setLatitude(item.getDouble("lat"));
			        i.setLongitude(item.getDouble("lon"));
					GeoPoint point = new GeoPoint((int)(i.getLatitude() * 1E6), (int)(i.getLongitude() * 1E6));
					OverlayItem overlayitem = new OverlayItem(point, i.getTitle(), i.getImageUrl());
					camerasItemizedOverlay.addOverlay(overlayitem);
					mapOverlays.add(camerasItemizedOverlay);
					cameraItems.add(i);
					Log.i(DEBUG_TAG, item.getString("title"));
				}

			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			map.invalidate();
		}
	}

	@SuppressWarnings("unchecked")
	private static Integer getCategoryIcon(HashMap<Integer, String[]> eventCategories, String category) {
		Integer image = R.drawable.alert_highest;
		Set set = eventCategories.entrySet();
		Iterator i = set.iterator();
		
		if (category.equals("")) return image;
		
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
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
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
