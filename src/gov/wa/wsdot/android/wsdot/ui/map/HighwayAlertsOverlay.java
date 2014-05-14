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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;

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
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class HighwayAlertsOverlay {
	private static final String TAG = HighwayAlertsOverlay.class.getSimpleName();
	private List<HighwayAlertsItem> mAlertItems = new ArrayList<HighwayAlertsItem>();

	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, String[]> eventCategories = new HashMap<Integer, String[]>();
	private final Activity mActivity;

	private String[] projection = {
			HighwayAlerts.HIGHWAY_ALERT_LATITUDE,
			HighwayAlerts.HIGHWAY_ALERT_LONGITUDE,
			HighwayAlerts.HIGHWAY_ALERT_CATEGORY,
			HighwayAlerts.HIGHWAY_ALERT_HEADLINE
			};
	
	public HighwayAlertsOverlay(Activity activity, LatLngBounds bounds) {
		this.mActivity = activity;
		
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
				    LatLng alertLocation = new LatLng(alertCursor.getDouble(0), alertCursor.getDouble(1));
					
				    if (bounds.contains(alertLocation)) {
				    mAlertItems.add(new HighwayAlertsItem(
							alertCursor.getDouble(0),
							alertCursor.getDouble(1),
							alertCursor.getString(2),
							alertCursor.getString(3),
							getCategoryIcon(eventCategories, alertCursor.getString(2))));
				    }
					alertCursor.moveToNext();
				}
			}
			
        } catch (Exception e) {
        	Log.e(TAG, "Error in network call", e);
        } finally {
			 if (alertCursor != null) {
				 alertCursor.close();
			 }
        }

	}
	
	 public int size() {
		 return mAlertItems.size();
	 }
	 
	 public List<HighwayAlertsItem> getAlertMarkers() {
	     return mAlertItems;
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
