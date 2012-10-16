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
import gov.wa.wsdot.android.wsdot.ui.VesselWatchDetailsActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class VesselsOverlay extends ItemizedOverlay<OverlayItem> {
	private static final String DEBUG_TAG = "VesselsOverlay";
	private List<VesselItem> mVesselItems = new ArrayList<VesselItem>();
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Integer> ferryIcons = new HashMap<Integer, Integer>();
	private final Activity mActivity;
	private boolean showShadows;
	
	public VesselsOverlay(Activity activity) {
		super(null);
		
		mActivity = activity;
		
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);

		ferryIcons.put(0, R.drawable.ferry_0);
		ferryIcons.put(30, R.drawable.ferry_30);
		ferryIcons.put(60, R.drawable.ferry_60);
		ferryIcons.put(90, R.drawable.ferry_90);
		ferryIcons.put(120, R.drawable.ferry_120);
		ferryIcons.put(150, R.drawable.ferry_150);
		ferryIcons.put(180, R.drawable.ferry_180);
		ferryIcons.put(210, R.drawable.ferry_210);
		ferryIcons.put(240, R.drawable.ferry_240);
		ferryIcons.put(270, R.drawable.ferry_270);
		ferryIcons.put(300, R.drawable.ferry_300);
		ferryIcons.put(330, R.drawable.ferry_330);
		ferryIcons.put(360, R.drawable.ferry_360);
					
		try {
			URL url = new URL("http://www.wsdot.wa.gov/ferries/vesselwatch/Vessels.ashx");
			URLConnection urlConn = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String jsonFile = "";
			String line;
			while ((line = in.readLine()) != null)
				jsonFile += line;
			in.close();
			
			JSONObject obj = new JSONObject(jsonFile);
			JSONArray items = obj.getJSONArray("vessellist");
			int ferryIcon;
			
			for (int j=0; j < items.length(); j++) {
				JSONObject item = items.getJSONObject(j);
				if (item.getString("inservice").equalsIgnoreCase("false")) {
					continue;
				}
				
				int nearest = (item.getInt("head") + 30 / 2) / 30 * 30; // round heading to nearest 30 degrees
				ferryIcon = ferryIcons.get(nearest);
				String route = item.getString("route");
				String lastDock = item.getString("lastdock");
				String arrivingTerminal = item.getString("aterm");
				String leftDock = item.getString("leftdock");
				String actualDeparture = "";
				
				if (route.length() == 0) route = "Not available";
				if (lastDock.length() == 0) lastDock = "Not available";
				if (arrivingTerminal.length() == 0) arrivingTerminal = "Not available";
				if (leftDock.length() == 0) {
					actualDeparture = "--:--";
				} else {
					actualDeparture = leftDock + " " + item.getString("leftdockAMPM");
				}
				
				mVesselItems.add(new VesselItem(getPoint(item.getDouble("lat"), item.getDouble("lon")),
						item.getString("name"),
						"<b>Route:</b> " + route
							+ "<br><b>Departing:</b> " + lastDock
							+ "<br><b>Arriving:</b> " + arrivingTerminal
							+ "<br><b>Scheduled Departure:</b> " + item.getString("nextdep") + " " + item.getString("nextdepAMPM")
							+ "<br><b>Actual Departure:</b> " + actualDeparture
							+ "<br><b>Estimated Arrival:</b> " + item.getString("eta") + " " + item.getString("etaAMPM")
							+ "<br><b>Heading:</b> "	+ Integer.toString(item.getInt("head")) + "\u00b0 " + item.getString("headtxt")
							+ "<br><b>Speed:</b> " + Double.toString(item.getDouble("speed")) + " knots"
							+ "<br><br><a href=\"http://www.wsdot.com/ferries/vesselwatch/VesselDetail.aspx?vessel_id="
							+ item.getInt("vesselID") + "\">" + item.getString("name") + " Web page</a>",
							getMarker(ferryIcon)));
			}
			
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Error in network call", e);
		}

		populate();
	}

	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1E6), (int)(lon*1E6)));
	}
	
	class VesselItem extends OverlayItem {
		Drawable marker = null;

		VesselItem(GeoPoint pt, String title, String description, Drawable marker) {
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
	protected VesselItem createItem(int i) {
		return(mVesselItems.get(i));
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
		Intent intent = new Intent(mActivity, VesselWatchDetailsActivity.class);
		b.putString("title", item.getTitle());
		b.putString("description", item.getSnippet());
		intent.putExtras(b);
		mActivity.startActivity(intent);
		
		return true;
	}
	
	@Override
	public int size() {
		return(mVesselItems.size());
	}

	private Drawable getMarker(int resource) {
		Drawable marker = mActivity.getResources().getDrawable(resource);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());
		boundCenterBottom(marker);

		return(marker);
	}

}
