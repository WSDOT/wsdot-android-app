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

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class HighwayAlertsOverlay {
	private static final String TAG = HighwayAlertsOverlay.class.getSimpleName();
	private List<HighwayAlertsItem> mAlertItems = new ArrayList<HighwayAlertsItem>();
	private final Activity mActivity;

	private String[] projection = {
	        HighwayAlerts.HIGHWAY_ALERT_ID,
			HighwayAlerts.HIGHWAY_ALERT_LATITUDE,
			HighwayAlerts.HIGHWAY_ALERT_LONGITUDE,
			HighwayAlerts.HIGHWAY_ALERT_CATEGORY,
			HighwayAlerts.HIGHWAY_ALERT_HEADLINE,
			HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED,
			HighwayAlerts.HIGHWAY_ALERT_PRIORITY
			};
	
	public HighwayAlertsOverlay(Activity activity, LatLngBounds bounds) {
	    this.mActivity = activity;
		
		Cursor alertCursor = null;

		// Types of categories which result in one icon or another being displayed. 
		String[] event_closure = {"closed", "closure"};
		String[] event_construction = {"construction", "maintenance", "lane closure"};
		
		HashMap<String, String[]> eventCategories = new HashMap<String, String[]>();
		eventCategories.put("closure", event_closure);
        eventCategories.put("construction", event_construction);
		
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
				    LatLng alertLocation = new LatLng(alertCursor.getDouble(1), alertCursor.getDouble(2));
					
				    if (bounds.contains(alertLocation)) {
    				    mAlertItems.add(new HighwayAlertsItem(
    				            alertCursor.getString(0),
    							alertCursor.getDouble(1),
    							alertCursor.getDouble(2),
    							alertCursor.getString(3),
    							alertCursor.getString(4),
    							alertCursor.getString(5),
    							alertCursor.getString(6),
    							getCategoryIcon(
                                        eventCategories,
                                        alertCursor.getString(3), // Category
                                        alertCursor.getString(6)) // Priority
    							));
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
	 
    private static int getCategoryIcon(
            HashMap<String, String[]> eventCategories, String category, String priority) {
        
        int alertClosed = R.drawable.closed;
        int alertHighest = R.drawable.alert_highest;
		int alertHigh = R.drawable.alert_high;
		int alertMedium = R.drawable.alert_moderate;
		int alertLow = R.drawable.alert_low;
		int constructionHighest = R.drawable.construction_highest;
		int constructionHigh = R.drawable.construction_high;
		int constructionMedium = R.drawable.construction_moderate;
		int constructionLow = R.drawable.construction_low;
		int defaultAlertImage = alertHighest;
		 
		Set<Entry<String, String[]>> set = eventCategories.entrySet();
		Iterator<Entry<String, String[]>> i = set.iterator();
			
		if (category.equals("")) return defaultAlertImage;

		 while(i.hasNext()) {
			 Entry<String, String[]> me = i.next();
			 for (String phrase: (String[])me.getValue()) {
				 String patternStr = phrase;
				 Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
				 Matcher matcher = pattern.matcher(category);
				 boolean matchFound = matcher.find();
				 if (matchFound) {
					 String keyWord = me.getKey();
					 
					 if (keyWord.equalsIgnoreCase("closure")) {
					     return alertClosed;
					 } else if (keyWord.equalsIgnoreCase("construction")) {
					     if (priority.equalsIgnoreCase("highest")) {
					         return constructionHighest;
					     } else if (priority.equalsIgnoreCase("high")) {
					         return constructionHigh;
					     } else if (priority.equalsIgnoreCase("medium")) {
					         return constructionMedium;
                        } else if (priority.equalsIgnoreCase("low")
                                || priority.equalsIgnoreCase("lowest")) {
					         return constructionLow;
					     }
					 }
				 } else {
				     if (priority.equalsIgnoreCase("highest")) {
				         return alertHighest;
				     } else if (priority.equalsIgnoreCase("high")) {
				         return alertHigh;
				     } else if (priority.equalsIgnoreCase("medium")) {
				         return alertMedium;
				     } else if (priority.equalsIgnoreCase("low")
				             || priority.equalsIgnoreCase("lowest")) {
				         return alertLow;
				     }
				 }
			 }
		 }
		 
		 return defaultAlertImage;
	 }
	 
}
