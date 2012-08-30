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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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

public class MountainPassesSyncService extends IntentService {

	private static final String DEBUG_TAG = "MountainPassesSyncService";
	
    private String[] projection = {
    		Caches.CACHE_LAST_UPDATED
    		};	
	
	public MountainPassesSyncService(String name) {
		super("MountainPassesSyncService");
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
					new String[] {"mountain_passes"},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				long lastUpdated = cursor.getLong(0);
				//long deltaMinutes = (now - lastUpdated) / DateUtils.MINUTE_IN_MILLIS;
				//Log.d(DEBUG_TAG, "Delta since last update is " + deltaMinutes + " min");
				shouldUpdate = (Math.abs(now - lastUpdated) > (15 * DateUtils.MINUTE_IN_MILLIS));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		// Ability to force a refresh of camera data.
		boolean forceUpdate = intent.getBooleanExtra("forceUpdate", false);
		
		if (shouldUpdate || forceUpdate) {
			String requestString = intent.getStringExtra("url");
		
			try {
				URL url = new URL(requestString);
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
				JSONObject result = obj.getJSONObject("GetMountainPassConditionsResult");
				JSONArray passConditions = result.getJSONArray("PassCondition");
				
				for (int j=0; j < passConditions.length(); j++) {
					JSONObject pass = passConditions.getJSONObject(j);
					ContentValues values = new ContentValues();

					values.put(MountainPasses.MOUNTAIN_PASS_FORECAST, pass.getString("Forecast"));
					values.put(MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION, pass.getString("WeatherCondition"));
				    values.put(MountainPasses.MOUNTAIN_PASS_DATE_UPDATED, pass.getString("DateUpdated"));
					values.put(MountainPasses.MOUNTAIN_PASS_CAMERA, pass.getString("Cameras"));
					values.put(MountainPasses.MOUNTAIN_PASS_ELEVATION, pass.getString("ElevationInFeet"));
					values.put(MountainPasses.MOUNTAIN_PASS_TRAVEL_ADVISORY_ACTIVE, pass.getString("TravelAdvisoryActive"));
					values.put(MountainPasses.MOUNTAIN_PASS_ROAD_CONDITION, pass.getString("RoadCondition"));
					values.put(MountainPasses.MOUNTAIN_PASS_TEMPERATURE, pass.getString("TemperatureInFahrenheit"));
					JSONObject restrictionOne = pass.getJSONObject("RestrictionOne");
					values.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE, restrictionOne.getString("RestrictionText"));
					values.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION, restrictionOne.getString("TravelDirection"));
					JSONObject restrictionTwo = pass.getJSONObject("RestrictionTwo");
					values.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO, restrictionTwo.getString("RestrictionText"));
					values.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION, restrictionTwo.getString("TravelDirection"));
					
					resolver.update(
							MountainPasses.CONTENT_URI,
							values, MountainPasses.MOUNTAIN_PASS_ID + "=?",
							new String[] {pass.getString("MountainPassId")}
							);
				}
				
				responseString = "OK";
	    	} catch (Exception e) {
	    		Log.e(DEBUG_TAG, "Error: " + e.getMessage());
	    		responseString = e.getMessage();
			}
			
		} else {
			responseString = "NOOP";
		}
		
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);		
	}

}
