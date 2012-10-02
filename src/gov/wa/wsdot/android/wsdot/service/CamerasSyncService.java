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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;

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

public class CamerasSyncService extends IntentService {
	
	private static final String DEBUG_TAG = "CamerasSyncService";
	
    private String[] projection = {
    		Caches.CACHE_LAST_UPDATED
    		};
    
    public CamerasSyncService() {
		super("CamerasSyncService");
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
					new String[] {"cameras"},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				long lastUpdated = cursor.getLong(0);
				//long deltaDays = (now - lastUpdated) / DateUtils.DAY_IN_MILLIS;
				//Log.d(DEBUG_TAG, "Delta since last update is " + deltaDays + " day(s)");
				shouldUpdate = (Math.abs(now - lastUpdated) > (7 * DateUtils.DAY_IN_MILLIS));
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
			List<Integer> starred = new ArrayList<Integer>();
			
			starred = getStarred();
			
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
				JSONObject result = obj.getJSONObject("cameras");
				JSONArray items = result.getJSONArray("items");
				List<ContentValues> cams = new ArrayList<ContentValues>();
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					ContentValues cameraData = new ContentValues();
					
					cameraData.put(Cameras.CAMERA_ID, item.getString("id"));
					cameraData.put(Cameras.CAMERA_TITLE, item.getString("title"));
					cameraData.put(Cameras.CAMERA_URL, item.getString("url"));
					cameraData.put(Cameras.CAMERA_LATITUDE, item.getString("lat"));
					cameraData.put(Cameras.CAMERA_LONGITUDE, item.getString("lon"));
					cameraData.put(Cameras.CAMERA_HAS_VIDEO, item.getString("video"));
					cameraData.put(Cameras.CAMERA_ROAD_NAME, item.getString("roadName"));
					
					if (starred.contains(Integer.parseInt(item.getString("id")))) {
						cameraData.put(Cameras.CAMERA_IS_STARRED, 1);
					}
					
					cams.add(cameraData);
				}

				// Purge existing cameras covered by incoming data
				resolver.delete(Cameras.CONTENT_URI, null, null);
				// Bulk insert all the new cameras
				resolver.bulkInsert(Cameras.CONTENT_URI, cams.toArray(new ContentValues[cams.size()]));
				// Update the cache table with the time we did the update
				ContentValues values = new ContentValues();
				values.put(Caches.CACHE_LAST_UPDATED, System.currentTimeMillis());
				resolver.update(
						Caches.CONTENT_URI,
						values, Caches.CACHE_TABLE_NAME + " LIKE ?",
						new String[] {"cameras"}
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
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);   	
	}

	/** 
	 * Check the camera table for any starred entries. If we find some, save them
	 * to a list so we can re-star those cameras after we flush the database.
	 */	
	private List<Integer> getStarred() {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		List<Integer> starred = new ArrayList<Integer>();

		try {
			cursor = resolver.query(
					Cameras.CONTENT_URI,
					new String[] {Cameras.CAMERA_ID},
					Cameras.CAMERA_IS_STARRED + "=?",
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
