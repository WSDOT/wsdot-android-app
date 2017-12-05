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

package gov.wa.wsdot.android.wsdot.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;

public class TravelTimesSyncService extends IntentService {
	
	private static final String DEBUG_TAG = "TravelTimesSyncService";

	public TravelTimesSyncService() {
		super("TravelTimesSyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		long now = System.currentTimeMillis();
		boolean shouldUpdate = true;
		String responseString = "";

		
		// Ability to force a refresh of camera data.
		boolean forceUpdate = intent.getBooleanExtra("forceUpdate", false);
		
		if (shouldUpdate || forceUpdate) {
			List<Integer> starred = new ArrayList<Integer>();

			starred = getStarred();
			
			try {
				URL url = new URL(APIEndPoints.TRAVEL_TIMES);
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
				JSONObject result = obj.getJSONObject("traveltimes");
				JSONArray items = result.getJSONArray("items");
				List<ContentValues> times = new ArrayList<ContentValues>();
							
				int numItems = items.length();
				for (int j=0; j < numItems; j++) {
					JSONObject item = items.getJSONObject(j);
					ContentValues timesValues = new ContentValues();
					timesValues.put(TravelTimes.TRAVEL_TIMES_TITLE, item.getString("title"));
					timesValues.put(TravelTimes.TRAVEL_TIMES_CURRENT, item.getInt("current"));
					timesValues.put(TravelTimes.TRAVEL_TIMES_AVERAGE, item.getInt("average"));
					timesValues.put(TravelTimes.TRAVEL_TIMES_DISTANCE, item.getString("distance") + " miles");
					timesValues.put(TravelTimes.TRAVEL_TIMES_ID, Integer.parseInt(item.getString("routeid")));
					timesValues.put(TravelTimes.TRAVEL_TIMES_UPDATED, item.getString("updated"));
					timesValues.put(TravelTimes.TRAVEL_TIMES_START_LATITUDE, item.getDouble("startLatitude"));
					timesValues.put(TravelTimes.TRAVEL_TIMES_START_LONGITUDE, item.getDouble("startLongitude"));
					timesValues.put(TravelTimes.TRAVEL_TIMES_END_LATITUDE, item.getDouble("endLatitude"));
					timesValues.put(TravelTimes.TRAVEL_TIMES_END_LONGITUDE, item.getDouble("endLongitude"));
					
					if (starred.contains(Integer.parseInt(item.getString("routeid")))) {
						timesValues.put(TravelTimes.TRAVEL_TIMES_IS_STARRED, 1);
					}
					
					times.add(timesValues);
				}

				// Purge existing travel times covered by incoming data
				resolver.delete(TravelTimes.CONTENT_URI, null, null);
				// Bulk insert all the new travel times
				resolver.bulkInsert(TravelTimes.CONTENT_URI, times.toArray(new ContentValues[times.size()]));		
				//todo  Update the cache table with the time we did the update

				
				responseString = "OK";				
	    	} catch (Exception e) {
	    		Log.e(DEBUG_TAG, "Error: " + e.getMessage());
	    		responseString = e.getMessage();
			}
			
		} else {
			responseString = "NOP";
		}
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.TRAVEL_TIMES_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);
	}

	/** 
	 * Check the travel times table for any starred entries. If we find some, save them
	 * to a list so we can re-star those after we flush the database.
	 */	
	private List<Integer> getStarred() {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		List<Integer> starred = new ArrayList<Integer>();

		try {
			cursor = resolver.query(
					TravelTimes.CONTENT_URI,
					new String[] {TravelTimes.TRAVEL_TIMES_ID},
					TravelTimes.TRAVEL_TIMES_IS_STARRED + "=?",
					new String[] {"1"},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					starred.add(cursor.getInt(0));
					cursor.moveToNext();
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return starred;
	}
	
}
