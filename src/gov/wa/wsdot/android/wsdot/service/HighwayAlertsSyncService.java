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

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;
import gov.wa.wsdot.android.wsdot.ui.TrafficMapActivity.HighwayAlertsSyncReceiver;

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
import android.util.Log;

public class HighwayAlertsSyncService extends IntentService {
	
	private static final String DEBUG_TAG = "HighwayAlertsSyncService";
    public static final String REQUEST_STRING = "alertsRequest";
    public static final String RESPONSE_STRING = "alertsResponse";
	
    public HighwayAlertsSyncService() {
		super("HighwayAlertsSyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		String requestString = intent.getStringExtra(REQUEST_STRING);
		String responseString = "";
		
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
			JSONObject result = obj.getJSONObject("alerts");
			JSONArray items = result.getJSONArray("items");
			ContentResolver resolver = getContentResolver();
			List<ContentValues> alerts = new ArrayList<ContentValues>();
			
			for (int j=0; j < items.length(); j++) {
				JSONObject item = items.getJSONObject(j);
				JSONObject startRoadwayLocation = item.getJSONObject("StartRoadwayLocation");
				ContentValues alertData = new ContentValues();

				alertData.put(HighwayAlerts.HIGHWAY_ALERT_ID, item.getString("AlertID"));
				alertData.put(HighwayAlerts.HIGHWAY_ALERT_HEADLINE, item.getString("HeadlineDescription"));
				alertData.put(HighwayAlerts.HIGHWAY_ALERT_CATEGORY, item.getString("EventCategory"));
				alertData.put(HighwayAlerts.HIGHWAY_ALERT_LATITUDE, startRoadwayLocation.getString("Latitude"));
				alertData.put(HighwayAlerts.HIGHWAY_ALERT_LONGITUDE, startRoadwayLocation.getString("Longitude"));
				alertData.put(HighwayAlerts.HIGHWAY_ALERT_ROAD_NAME, startRoadwayLocation.getString("RoadName"));
				alerts.add(alertData);
			}
			
			resolver.bulkInsert(HighwayAlerts.CONTENT_URI, alerts.toArray(new ContentValues[alerts.size()]));
			responseString = "OK";
    	} catch (Exception e) {
    		Log.e(DEBUG_TAG, "Error: " + e.getMessage());
    		responseString = "ERROR";
    	}
    	
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(HighwayAlertsSyncReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESPONSE_STRING, responseString);
        sendBroadcast(broadcastIntent);   	
	}	

}
