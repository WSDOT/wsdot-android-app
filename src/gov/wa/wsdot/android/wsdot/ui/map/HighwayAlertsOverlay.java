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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;
import gov.wa.wsdot.android.wsdot.ui.HighwayAlertDetailsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class HighwayAlertsOverlay extends ItemizedOverlay<OverlayItem> {
	private static final String DEBUG_TAG = "HighwayAlertsOverlay";
	private List<AlertItem> mAlertItems = new ArrayList<AlertItem>();
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, String[]> eventCategories = new HashMap<Integer, String[]>();
	private final Activity mActivity;
	private boolean showShadows;

	private String[] projection = {
			HighwayAlerts.HIGHWAY_ALERT_LATITUDE,
			HighwayAlerts.HIGHWAY_ALERT_LONGITUDE,
			HighwayAlerts.HIGHWAY_ALERT_CATEGORY,
			HighwayAlerts.HIGHWAY_ALERT_HEADLINE
			};
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1E6), (int)(lon*1E6)));
	}
	
	public HighwayAlertsOverlay(Activity activity) {
		super(null);
		
		mActivity = activity;
		Cursor alertCursor = null;
		String[] event_construction = {"construction", "maintenance"};
		String[] event_closure = {
				"bridge closed",
				"closure",
				"emergency closure",
				"hcb closed maint",
				"hcb closed marine",
				"hcb closed police",
				"hcb closed winds",
				"rocks - closure"
				};
		
		eventCategories.put(R.drawable.closed, event_closure);
		eventCategories.put(R.drawable.construction_high, event_construction);
		
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);
		
        try {
			alertCursor = mActivity.getContentResolver().query(
					HighwayAlerts.CONTENT_URI,
					projection,
					null,
					null,
					null
					);
			
			if (alertCursor.moveToFirst()) {
				while (!alertCursor.isAfterLast()) {
					mAlertItems.add(new AlertItem(
							getPoint(alertCursor.getDouble(0), alertCursor.getDouble(1)),
							alertCursor.getString(2),
							alertCursor.getString(3),
							getMarker(getCategoryIcon(eventCategories, alertCursor.getString(2)))));
					
					alertCursor.moveToNext();
				}
			}
			
        } catch (Exception e) {
        	Log.e(DEBUG_TAG, "Error in network call", e);
        } finally {
			 if (alertCursor != null) {
				 alertCursor.close();
			 }
        }
        
        populate();

	}
	
	public class AlertItem extends OverlayItem {
		 Drawable marker = null;

		 AlertItem(GeoPoint pt, String title, String description, Drawable marker) {
			 super(pt, title, description);
			 this.marker = marker;
		 }

		 @Override
		 public Drawable getMarker(int stateBitset) {
			 Drawable result = marker;
			 setState(result, stateBitset);

			 return result;
		 }
	}	
	
	@Override
	protected AlertItem createItem(int i) {
		return(mAlertItems.get(i));
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
		OverlayItem item = getItem(i);
		Bundle b = new Bundle();
		Intent intent = new Intent(mActivity, HighwayAlertDetailsActivity.class);
		b.putString("title", item.getTitle());
		b.putString("description", item.getSnippet());
		intent.putExtras(b);
		mActivity.startActivity(intent);

		return true;
	} 
	 
	 @Override
	 public int size() {
		 return(mAlertItems.size());
	 }
	 
	 public void clear() {
		 mAlertItems.clear();
		 populate();
	 }
	 
	 private Drawable getMarker(int resource) {
		 Drawable marker = mActivity.getResources().getDrawable(resource);
		 marker.setBounds(0, 0, marker.getIntrinsicWidth(),
		 marker.getIntrinsicHeight());
		 boundCenterBottom(marker);

		 return(marker);
	 }
	 
	 private static Integer getCategoryIcon(HashMap<Integer, String[]> eventCategories, String category) {
		 Integer image = R.drawable.alert_highest;
		 Set<Entry<Integer, String[]>> set = eventCategories.entrySet();
		 Iterator<Entry<Integer, String[]>> i = set.iterator();
			
		 if (category.equals("")) return image;

		 while(i.hasNext()) {
			 Entry<Integer, String[]> me = i.next();
			 for (String phrase: (String[])me.getValue()) {
				 String patternStr = phrase;
				 Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
				 Matcher matcher = pattern.matcher(category);
				 boolean matchFound = matcher.find();
				 if (matchFound) {
					 image = (Integer)me.getKey();
				 }
			 }
		 }
		 
		 return image;
	 }
	 
}
