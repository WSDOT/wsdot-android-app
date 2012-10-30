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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class FerriesSchedulesSyncService extends IntentService {

	private static final String DEBUG_TAG = "FerriesSchedulesSyncService";
	private static final String FERRIES_SCHEDULES_URL = "http://data.wsdot.wa.gov/mobile/WSFRouteSchedules.js.gz";
	
	public FerriesSchedulesSyncService() {
		super("FerriesSchedulesSyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		long now = System.currentTimeMillis();
		boolean shouldUpdate = true;
		String responseString = "";
		DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

		/** 
		 * Check the cache table for the last time data was downloaded. If we are within
		 * the allowed time period, don't sync, otherwise get fresh data from the server.
		 */
		try {
			cursor = resolver.query(
					Caches.CONTENT_URI,
					new String[] {Caches.CACHE_LAST_UPDATED},
					Caches.CACHE_TABLE_NAME + " LIKE ?",
					new String[] {"ferries_schedules"},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				long lastUpdated = cursor.getLong(0);
				//long deltaMinutes = (now - lastUpdated) / DateUtils.MINUTE_IN_MILLIS;
				//Log.d(DEBUG_TAG, "Delta since last update is " + deltaMinutes + " min");
				shouldUpdate = (Math.abs(now - lastUpdated) > (30 * DateUtils.MINUTE_IN_MILLIS));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		// Ability to force a refresh of camera data.
		boolean forceUpdate = intent.getBooleanExtra("forceUpdate", false);
		
		if (shouldUpdate || forceUpdate) {
			List<Integer> starred = new ArrayList<Integer>();

			starred = getStarred();
			
			try {
				URL url = new URL(FERRIES_SCHEDULES_URL);
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
				
				JSONArray items = new JSONArray(jsonFile);
				List<ContentValues> schedules = new ArrayList<ContentValues>();
				
				int numItems = items.length();
				for (int i=0; i < numItems; i++) {
					JSONObject item = items.getJSONObject(i);
					ContentValues schedule = new ContentValues();
					schedule.put(FerriesSchedules.FERRIES_SCHEDULE_ID, item.getInt("RouteID"));
					schedule.put(FerriesSchedules.FERRIES_SCHEDULE_TITLE, item.getString("Description"));
					schedule.put(FerriesSchedules.FERRIES_SCHEDULE_DATE, item.getString("Date"));
					schedule.put(FerriesSchedules.FERRIES_SCHEDULE_ALERT, item.getString("RouteAlert"));
					schedule.put(FerriesSchedules.FERRIES_SCHEDULE_UPDATED,
							dateFormat.format(new Date(Long.parseLong(item
									.getString("CacheDate").substring(6, 19)))));
					
					if (starred.contains(item.getInt("RouteID"))) {
						schedule.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 1);
					}
					
					schedules.add(schedule);
				}
				
				// Purge existing travel times covered by incoming data
				resolver.delete(FerriesSchedules.CONTENT_URI, null, null);
				// Bulk insert all the new travel times
				resolver.bulkInsert(FerriesSchedules.CONTENT_URI, schedules.toArray(new ContentValues[schedules.size()]));		
				// Update the cache table with the time we did the update
				ContentValues values = new ContentValues();
				values.put(Caches.CACHE_LAST_UPDATED, System.currentTimeMillis());
				resolver.update(
						Caches.CONTENT_URI,
						values, Caches.CACHE_TABLE_NAME + "=?",
						new String[] {"ferries_schedules"}
						);
				
				responseString = "OK";				
	    	} catch (Exception e) {
	    		Log.e(DEBUG_TAG, "Error: " + e.getMessage());
	    		responseString = e.getMessage();
			}
			
		} else {
			responseString = "NOP";
		}
		
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.FERRIES_SCHEDULES_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);
		
	}
	
	/** 
	 * Check the ferries schedules table for any starred entries. If we find some, save them
	 * to a list so we can re-star those after we flush the database.
	 */	
	private List<Integer> getStarred() {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		List<Integer> starred = new ArrayList<Integer>();

		try {
			cursor = resolver.query(
					FerriesSchedules.CONTENT_URI,
					new String[] {FerriesSchedules.FERRIES_SCHEDULE_ID},
					FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED + "=?",
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
