/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.util.map;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;

public class VesselsOverlay {
	private static final String TAG = VesselsOverlay.class.getSimpleName();
	private List<VesselWatchItem> vesselWatchItems = new ArrayList<VesselWatchItem>();
	
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Integer> ferryIcons = new HashMap<Integer, Integer>();
	
	/**
	 * Constructor 
	 * 
	 * @param api_key WSDOT API key.
	 */
	public VesselsOverlay(String api_key) {

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

			URL url = new URL(APIEndPoints.VESSEL_LOCATIONS + "?apiaccesscode=" + api_key);
			Log.w(TAG, "URL: " + url.toString());
			URLConnection urlConn = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String jsonFile = "";
			String line;
			while ((line = in.readLine()) != null)
				jsonFile += line;
			in.close();
			
			JSONArray items = new JSONArray(jsonFile);
			int ferryIcon;
			
			for (int j=0; j < items.length(); j++) {
				JSONObject item = items.getJSONObject(j);
				if (item.getString("InService").equalsIgnoreCase("false")) {
					continue;
				}
				
				int nearest = (item.getInt("Heading") + 30 / 2) / 30 * 30; // round heading to nearest 30 degrees
				ferryIcon = ferryIcons.get(nearest);
				String route = item.getJSONArray("OpRouteAbbrev").getString(0).toUpperCase(Locale.ENGLISH);
				String lastDock = item.getString("DepartingTerminalName");
				String arrivingTerminal = item.getString("ArrivingTerminalName");
				String leftDock = formatTime(item, "LeftDock");
				String nextDepart = formatTime(item, "ScheduledDeparture");
				String eta = formatTime(item, "Eta");
				
				if (route.length() == 0) route = "Not available";
				if (lastDock.length() == 0) lastDock = "Not available";
				if (arrivingTerminal.length() == 0) arrivingTerminal = "Not available";

				vesselWatchItems.add(new VesselWatchItem(
				        item.getDouble("Latitude"),
				        item.getDouble("Longitude"),
						item.getString("VesselName"),
						"<b>Route:</b> " + route
							+ "<br><b>Departing:</b> " + lastDock
							+ "<br><b>Arriving:</b> " + arrivingTerminal
							+ "<br><b>Scheduled Departure:</b> " + nextDepart
							+ "<br><b>Actual Departure:</b> " + leftDock
							+ "<br><b>Estimated Arrival:</b> " + eta
							+ "<br><b>Heading:</b> "	+ Integer.toString(item.getInt("Heading")) + "\u00b0 " 
														+ headingToHeadtxt(item.getInt("Heading"))
							+ "<br><b>Speed:</b> " + Double.toString(item.getDouble("Speed")) + " knots"
							+ "<br><br><a href=\"http://www.wsdot.com/ferries/vesselwatch/VesselDetail.aspx?vessel_id="
							+ item.getInt("VesselID") + "\">" + item.getString("VesselName") + " Web page</a>",
						ferryIcon));
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Error in network call", e);
		}

	}
	
    public List<VesselWatchItem> getVesselWatchItems() {
        return vesselWatchItems;
    }
    
    public int size() {
        return vesselWatchItems.size();
    }
    
    
    
    /**
     * Giving vessel heading returns a string for the
     * cardinal direction.
     * 
     * @param heading
     * @return direction string
     */
    private static String headingToHeadtxt(int heading){
    	String directions[] = {"N", "NxE", "E", "SxE", "S", "SxW", "W", "NxW", "N"};
        return directions[ (int)Math.round((  ((double)heading % 360) / 45)) ];
    }
    /**
     * Formats the time field in JSON object
     * 
     * @param item JSONObject for ferry data
     * @param time field name for time in item
     * @return Formatted time string.
     * @throws JSONException 
     * @throws NumberFormatException 
     */
    private static String formatTime(JSONObject item, String time) throws NumberFormatException, JSONException{
		DateFormat dateFormat = new SimpleDateFormat("h:mm a");
		if (item.isNull(time)) {
			return "--:--";
		} else { 
			return dateFormat.format(new Date(Long.parseLong(item.getString(time).substring(6, 19))));
		}
    }

}
