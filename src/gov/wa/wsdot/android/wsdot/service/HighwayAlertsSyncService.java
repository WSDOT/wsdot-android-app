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

package gov.wa.wsdot.android.wsdot.service;

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Caches;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;

public class HighwayAlertsSyncService extends IntentService {
	
	private static final String DEBUG_TAG = "HighwayAlertsSyncService";
	private static final String HIGHWAY_ALERTS_URL = "http://data.wsdot.wa.gov/mobile/HighwayAlerts.js.gz";

	private String[] projection = {
    		Caches.CACHE_LAST_UPDATED
    		};
    
    public HighwayAlertsSyncService() {
		super("HighwayAlertsSyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		long now = System.currentTimeMillis();
		boolean shouldUpdate = true;
		String responseString = "";

		/** 
		 * Check the cache table for the last time data was downloaded. If we are within
		 * the allowed time period, don't sync, otherwise get fresh data from the server.
		 */
		try {
			cursor = resolver.query(
					Caches.CONTENT_URI,
					projection,
					Caches.CACHE_TABLE_NAME + " LIKE ?",
					new String[] {"highway_alerts"},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				long lastUpdated = cursor.getLong(0);
				//long deltaMinutes = (now - lastUpdated) / DateUtils.MINUTE_IN_MILLIS;
				//Log.d(DEBUG_TAG, "Delta since last update is " + deltaMinutes + " min");
				shouldUpdate = (Math.abs(now - lastUpdated) > (5 * DateUtils.MINUTE_IN_MILLIS));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		// Tapping the refresh button will force a data refresh.
		boolean forceUpdate = intent.getBooleanExtra("forceUpdate", false);
		
		if (shouldUpdate || forceUpdate) {
			
	    	try {
				URL url = new URL(HIGHWAY_ALERTS_URL);
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
				List<ContentValues> alerts = new ArrayList<ContentValues>();
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					JSONObject startRoadwayLocation = item.getJSONObject("StartRoadwayLocation");
					ContentValues alertData = new ContentValues();
	
					alertData.put(HighwayAlerts.HIGHWAY_ALERT_ID, item.getString("AlertID"));
					alertData.put(HighwayAlerts.HIGHWAY_ALERT_HEADLINE, item.getString("HeadlineDescription"));
					alertData.put(HighwayAlerts.HIGHWAY_ALERT_CATEGORY, item.getString("EventCategory"));
					alertData.put(HighwayAlerts.HIGHWAY_ALERT_PRIORITY, item.getString("Priority"));
					alertData.put(HighwayAlerts.HIGHWAY_ALERT_LATITUDE, startRoadwayLocation.getString("Latitude"));
					alertData.put(HighwayAlerts.HIGHWAY_ALERT_LONGITUDE, startRoadwayLocation.getString("Longitude"));
					alertData.put(HighwayAlerts.HIGHWAY_ALERT_ROAD_NAME, startRoadwayLocation.getString("RoadName"));
					alerts.add(alertData);
				}
				
				// Purge existing highway alerts covered by incoming data
				resolver.delete(HighwayAlerts.CONTENT_URI, null, null);
				// Bulk insert all the new highway alerts
				resolver.bulkInsert(HighwayAlerts.CONTENT_URI, alerts.toArray(new ContentValues[alerts.size()]));
				// Update the cache table with the time we did the update
				ContentValues values = new ContentValues();
				values.put(Caches.CACHE_LAST_UPDATED, System.currentTimeMillis());
				resolver.update(
						Caches.CONTENT_URI,
						values, Caches.CACHE_TABLE_NAME + " LIKE ?",
						new String[] {"highway_alerts"}
						);
				
				responseString = "OK";
	    	} catch (Exception e) {
	    		Log.e(DEBUG_TAG, "Error: " + e.getMessage());
	    		responseString = e.getMessage();
	    	}
		} else {
			responseString = "NOOP";
		}
		
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.HIGHWAY_ALERTS_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);   	
	}	

}
