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

package gov.wa.wsdot.android.wsdot.ui.map;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.shared.LatLonItem;
import gov.wa.wsdot.android.wsdot.ui.CameraActivity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CamerasOverlay extends ItemizedOverlay<OverlayItem> {
	private static final String DEBUG_TAG = "CamerasOverlay";
	private ArrayList<CameraItem> mCameraItems = new ArrayList<CameraItem>();
	private final Activity mActivity;
	private double mTopLatitude;
	private double mLeftLongitude;
	private double mBottomLatitude;
	private double mRightLongitude;
	private String mRoadName;
	boolean showCameras;
	boolean showShadows;
	private ArrayList<LatLonItem> mViewableMapArea = new ArrayList<LatLonItem>();

	private String[] projection = {
			Cameras.CAMERA_LATITUDE,
			Cameras.CAMERA_LONGITUDE,
			Cameras.CAMERA_TITLE,
			Cameras.CAMERA_URL,
			Cameras.CAMERA_HAS_VIDEO,
			Cameras.CAMERA_ID,
			Cameras.CAMERA_ROAD_NAME
			};
	
	public CamerasOverlay(Activity activity, double topLatitude, double leftLongitude,
			double bottomLatitude, double rightLongitude, String roadName) {
		
		super(null);
		
		this.mActivity = activity;
		this.mTopLatitude = topLatitude;
		this.mLeftLongitude = leftLongitude;
		this.mBottomLatitude = bottomLatitude;
		this.mRightLongitude = rightLongitude;
		this.mRoadName = roadName;
		
		Cursor cameraCursor = null;
		
		mViewableMapArea.add(new LatLonItem(mTopLatitude, mLeftLongitude));
		mViewableMapArea.add(new LatLonItem(mTopLatitude, mRightLongitude));
		mViewableMapArea.add(new LatLonItem(mBottomLatitude, mRightLongitude));
		mViewableMapArea.add(new LatLonItem(mBottomLatitude, mLeftLongitude));
		
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
					if (inPolygon(mViewableMapArea, cameraCursor.getDouble(0), cameraCursor.getDouble(1))) {
						//Log.d(DEBUG_TAG, "Camera: " + cameraCursor.getString(2));
						int video = cameraCursor.getInt(4);
						int cameraIcon = (video == 0) ? R.drawable.camera : R.drawable.camera_video;
						
						mCameraItems.add(new CameraItem(
								getPoint(cameraCursor.getDouble(0), cameraCursor.getDouble(1)),
								null,
								null,
								getMarker(cameraIcon),
								cameraCursor.getInt(5)
								));
					}
					cameraCursor.moveToNext();
				}
				//Log.d(DEBUG_TAG, "Done adding cameras");
			}
		 } catch (Exception e) {
			 Log.e(DEBUG_TAG, "Error in network call", e);
		 } finally {
			 if (cameraCursor != null) {
				 cameraCursor.close();
			 }
		 }
		 
		 populate();
	}
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1E6), (int)(lon*1E6)));
	}
	
	class CameraItem extends OverlayItem {
		 Drawable marker = null;
		 int id;
	
		 CameraItem(GeoPoint pt, String title, String description, Drawable marker, int id) {
			 super(pt, title, description);
			 this.marker = marker;
			 this.id = id;
		 }

		 @Override
		 public Drawable getMarker(int stateBitset) {
			 Drawable result = marker;
			 setState(result, stateBitset);

			 return result;
		 }
	}	
	
	@Override
	protected CameraItem createItem(int i) {
		return(mCameraItems.get(i));
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (!showShadows) {
			shadow = false;
		}
		super.draw(canvas, mapView, shadow);
	}

	@Override
	protected boolean onTap(int i) {
		Bundle b = new Bundle();
		Intent intent = new Intent(mActivity, CameraActivity.class);
		b.putInt("id", this.mCameraItems.get(i).id);
		intent.putExtras(b);
		mActivity.startActivity(intent);

		return true;
	} 
	 
	 @Override
	 public int size() {
		 return(mCameraItems.size());
	 }
	 
	 public void clear() {
		 mCameraItems.clear();
		 populate();
	 }
	 
	 private Drawable getMarker(int resource) {
		 Drawable marker = mActivity.getResources().getDrawable(resource);
		 marker.setBounds(0, 0, marker.getIntrinsicWidth(),
		 marker.getIntrinsicHeight());
		 boundCenterBottom(marker);

		 return(marker);
	 }
	 
	 /**
	  * Iterate through collection of LatLon objects in arrayList and see
	  * if passed latitude and longitude point is within the collection.
	  */	
	 private boolean inPolygon(ArrayList<LatLonItem> points, double latitude, double longitude) {	
		 int j = points.size() - 1;
		 double lat = latitude;
		 double lon = longitude;		
		 boolean inPoly = false;

		 for (int i = 0; i < points.size(); i++) {
			 if ((points.get(i).getLongitude() < lon && points.get(j).getLongitude() >= lon) || 
					 (points.get(j).getLongitude() < lon && points.get(i).getLongitude() >= lon)) {
				 if (points.get(i).getLatitude() + (lon - points.get(i).getLongitude()) / 
						 (points.get(j).getLongitude() - points.get(i).getLongitude()) * 
						 (points.get(j).getLatitude() - points.get(i).getLatitude()) < lat) {
					 inPoly = !inPoly;
				 }
			 }
			 j = i;
		 }
		 return inPoly;
	 }
	 
}
