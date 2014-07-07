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

package gov.wa.wsdot.android.wsdot.service;

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesTerminalSailingSpace;
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

public class FerriesTerminalSailingSpaceSyncService extends IntentService {

    private static final String TAG = FerriesTerminalSailingSpaceSyncService.class.getSimpleName();
    private static final String TERMINAL_SAILING_SPACE_URL = "http://www.wsdot.wa.gov/ferries/api/terminals/rest/terminalsailingspace?"
            + "apiaccesscode={API_ACCESS_CODE}";
    
    public FerriesTerminalSailingSpaceSyncService() {
        super("FerriesTerminalSailingSpaceSyncService");
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
                    new String[] {"ferries_terminal_sailing_space"},
                    null
                    );
            
            if (cursor != null && cursor.moveToFirst()) {
                long lastUpdated = cursor.getLong(0);
                shouldUpdate = (Math.abs(now - lastUpdated) > (15 * DateUtils.SECOND_IN_MILLIS));
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
                URL url = new URL(TERMINAL_SAILING_SPACE_URL);
                URLConnection urlConn = url.openConnection();
                
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String jsonFile = "";
                String line;
                
                while ((line = in.readLine()) != null)
                    jsonFile += line;
                in.close();
                
                JSONArray array = new JSONArray(jsonFile);
                List<ContentValues> terminal = new ArrayList<ContentValues>();
                
                int numItems = array.length();
                for (int j=0; j < numItems; j++) {
                    JSONObject item = array.getJSONObject(j);
                    ContentValues sailingSpaceValues = new ContentValues();
                    sailingSpaceValues.put(FerriesTerminalSailingSpace.TERMINAL_ID, item.getInt("TerminalID"));
                    sailingSpaceValues.put(FerriesTerminalSailingSpace.TERMINAL_NAME, item.getString("TerminalName"));
                    sailingSpaceValues.put(FerriesTerminalSailingSpace.TERMINAL_ABBREV, item.getString("TerminalAbbrev"));
                    sailingSpaceValues.put(FerriesTerminalSailingSpace.TERMINAL_DEPARTING_SPACES, item.getString("DepartingSpaces"));
                    
                    if (starred.contains(item.getInt("TerminalID"))) {
                        sailingSpaceValues.put(FerriesTerminalSailingSpace.TERMINAL_IS_STARRED, 1);
                    }
                    
                    terminal.add(sailingSpaceValues);
                }
                
                // Purge existing terminal sailing space items covered by incoming data
                resolver.delete(FerriesTerminalSailingSpace.CONTENT_URI, null, null);
                // Bulk insert all the new terminal sailing space items
                resolver.bulkInsert(FerriesTerminalSailingSpace.CONTENT_URI, terminal.toArray(new ContentValues[terminal.size()]));        
                // Update the cache table with the time we did the update
                ContentValues values = new ContentValues();
                values.put(Caches.CACHE_LAST_UPDATED, System.currentTimeMillis());
                resolver.update(
                        Caches.CONTENT_URI,
                        values, Caches.CACHE_TABLE_NAME + "=?",
                        new String[] {"ferries_terminal_sailing_space"}
                        );
                
                responseString = "OK";  

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                responseString = e.getMessage();
            }
        } else {
            responseString = "NOP";
        }
        
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.FERRIES_TERMINAL_SAILING_SPACE_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);
    }
    
    /** 
     * Check the ferries terminal space sailing table for any starred entries.
     * If we find some, save them to a list so we can re-star those after we
     * flush the database.
     */ 
    private List<Integer> getStarred() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        List<Integer> starred = new ArrayList<Integer>();

        try {
            cursor = resolver.query(
                    FerriesTerminalSailingSpace.CONTENT_URI,
                    new String[] {FerriesTerminalSailingSpace.TERMINAL_ID},
                    FerriesTerminalSailingSpace.TERMINAL_IS_STARRED + "=?",
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
