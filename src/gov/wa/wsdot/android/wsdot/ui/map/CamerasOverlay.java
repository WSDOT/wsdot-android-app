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

package gov.wa.wsdot.android.wsdot.ui.map;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class CamerasOverlay {
	private static final String TAG = CamerasOverlay.class.getSimpleName();
	private List<CameraItem> cameraItems = new ArrayList<CameraItem>();
	private final Activity mActivity;
	private String mRoadName;
	boolean showCameras;
	boolean showShadows;

	private String[] projection = {
			Cameras.CAMERA_LATITUDE,
			Cameras.CAMERA_LONGITUDE,
			Cameras.CAMERA_TITLE,
			Cameras.CAMERA_URL,
			Cameras.CAMERA_HAS_VIDEO,
			Cameras.CAMERA_ID,
			Cameras.CAMERA_ROAD_NAME
			};
	
	public CamerasOverlay(Activity activity, LatLngBounds bounds, String roadName) {
		
		this.mActivity = activity;
		this.mRoadName = roadName;
		
		Cursor cameraCursor = null;
		
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);
        
        Uri baseUri;
        
        if (mRoadName != null) {
            baseUri = Uri.withAppendedPath(Cameras.CONTENT_ROAD_NAME_URI, Uri.encode(mRoadName));
        } else {
            baseUri = Cameras.CONTENT_URI;
        }
        
        try {
			cameraCursor = mActivity.getContentResolver().query(
					baseUri,
					projection,
					null,
					null,
					null
					);
			
			if (cameraCursor.moveToFirst()) {
				while (!cameraCursor.isAfterLast()) {
				    LatLng cameraLocation = new LatLng(cameraCursor.getDouble(0), cameraCursor.getDouble(1));

				    if (bounds.contains(cameraLocation)) {
						int video = cameraCursor.getInt(4);
						int cameraIcon = (video == 0) ? R.drawable.camera : R.drawable.camera_video;
						
						cameraItems.add(new CameraItem(
						        cameraCursor.getDouble(0),
						        cameraCursor.getDouble(1),
						        cameraCursor.getString(2),
								cameraCursor.getInt(5),
								cameraIcon
								));
					}
					cameraCursor.moveToNext();
				}
			}

        } catch (Exception e) {
			 Log.e(TAG, "Error in network call", e);
		 } finally {
			 if (cameraCursor != null) {
				 cameraCursor.close();
			 }
		 }
	}
	
	public List<CameraItem> getCameraMarkers() {
	    return cameraItems;
	}

	public int size() {
	    return cameraItems.size();
	}
}
