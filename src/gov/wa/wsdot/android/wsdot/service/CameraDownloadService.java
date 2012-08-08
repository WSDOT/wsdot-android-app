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

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class CameraDownloadService extends Service {

    private static final String DEBUG_TAG = "CameraDownloadService";
    private DownloadTask mDownloadTask;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        URL cameraUrl;
        
        try {
            String url = intent.getDataString();
            if (url != null && (url.length() > 0)) {
                cameraUrl = new URL(url);
                mDownloadTask = new DownloadTask();
                mDownloadTask.execute(cameraUrl);
            }
        } catch (MalformedURLException e) {
            Log.e(DEBUG_TAG, "Bad URL", e);
        }
        
		return Service.START_FLAG_REDELIVERY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class DownloadTask extends AsyncTask<URL, Void, Boolean> {

		@Override
		protected Boolean doInBackground(URL... params) {
			URL downloadPath = params[0];

            if (downloadPath != null) {
            	try {
					URL url = downloadPath;
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
						
	                    // save the data, and then continue with the loop
	                    getContentResolver().insert(Cameras.CONTENT_URI, cameraData);
					}
            	} catch (Exception e) {
            		Log.e(DEBUG_TAG, "Error parsing JSON file."); 
            	}
            }
            
            return true;		
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
 
			stopSelf();
		}
	}

}
