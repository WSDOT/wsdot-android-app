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

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.BorderWait;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Caches;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;

public class BorderWaitSyncService extends IntentService {

	private static final String DEBUG_TAG = "BorderWaitSyncService";
	private static final String BORDER_WAIT_URL = "http://data.wsdot.wa.gov/mobile/BorderCrossings.js";
	
	public BorderWaitSyncService() {
		super("BorderWaitSyncService");
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
					new String[] {Caches.CACHE_LAST_UPDATED},
					Caches.CACHE_TABLE_NAME + " LIKE ?",
					new String[] {"border_wait"},
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
			List<Integer> starred = new ArrayList<Integer>();
			
			starred = getStarred();
			
			try {
				URL url = new URL(BORDER_WAIT_URL);
				URLConnection urlConn = url.openConnection();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("waittimes");
				JSONArray items = result.getJSONArray("items");
				List<ContentValues> times = new ArrayList<ContentValues>();
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					ContentValues timesValues = new ContentValues();
					timesValues.put(BorderWait.BORDER_WAIT_ID, item.getInt("id"));
					timesValues.put(BorderWait.BORDER_WAIT_TITLE, item.getString("name"));
					timesValues.put(BorderWait.BORDER_WAIT_UPDATED, item.getString("updated"));
					timesValues.put(BorderWait.BORDER_WAIT_LANE, item.getString("lane"));
					timesValues.put(BorderWait.BORDER_WAIT_ROUTE, item.getInt("route"));
					timesValues.put(BorderWait.BORDER_WAIT_DIRECTION, item.getString("direction"));
					timesValues.put(BorderWait.BORDER_WAIT_TIME, item.getInt("wait"));
					
					if (starred.contains(item.getInt("id"))) {
						timesValues.put(BorderWait.BORDER_WAIT_IS_STARRED, 1);
					}
					
					times.add(timesValues);

				}
				
				// Purge existing border wait times covered by incoming data
				resolver.delete(BorderWait.CONTENT_URI, null, null);
				// Bulk insert all the new travel times
				resolver.bulkInsert(BorderWait.CONTENT_URI, times.toArray(new ContentValues[times.size()]));		
				// Update the cache table with the time we did the update
				ContentValues values = new ContentValues();
				values.put(Caches.CACHE_LAST_UPDATED, System.currentTimeMillis());
				resolver.update(
						Caches.CONTENT_URI,
						values, Caches.CACHE_TABLE_NAME + "=?",
						new String[] {"border_wait"}
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
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.BORDER_WAIT_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);
		
	}

	/** 
	 * Check the travel border wait table for any starred entries. If we find some, save them
	 * to a list so we can re-star those after we flush the database.
	 */	
	private List<Integer> getStarred() {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		List<Integer> starred = new ArrayList<Integer>();

		try {
			cursor = resolver.query(
					BorderWait.CONTENT_URI,
					new String[] {BorderWait.BORDER_WAIT_ID},
					BorderWait.BORDER_WAIT_IS_STARRED + "=?",
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
