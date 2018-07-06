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

package gov.wa.wsdot.android.wsdot.util;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ParserUtils {


	public static String relativeTimeFromUTC(String createdAt, String datePattern) {

		DateFormat parseDateFormat = new SimpleDateFormat(datePattern);
		parseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date parseDate;

		try {
			parseDate = parseDateFormat.parse(createdAt);
		} catch (ParseException e) {
			return "Unavailable";
		} catch (NullPointerException e) {
			return "";
		}

		return getRelative(parseDate);

	}
	
	@SuppressLint("SimpleDateFormat")
	public static String relativeTime(String createdAt, String datePattern, boolean shouldConvertTimeZone) {
		DateFormat parseDateFormat = new SimpleDateFormat(datePattern);
		Date parseDate;

		// createdAt is in America/Los_Angeles time zone
		if (shouldConvertTimeZone){

			DateFormat dateFormat = new SimpleDateFormat(datePattern);
			dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

			try {
				Date localDate = dateFormat.parse(createdAt);
				return getRelative(new Date(localDate.getTime()));
			} catch(ParseException e){
				return "Unavailable";
			}
		}		

		try {
			parseDate = parseDateFormat.parse(createdAt);
		} catch (ParseException e) {
			return "Unavailable";
		} catch (NullPointerException e) {
			return "";
		}
		
		return getRelative(parseDate);

	}
	
	public static String relativeTime(Date createdAt) { 
		return(getRelative(createdAt));
	}
	
	private static String getRelative(Date date) {
		DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");	
		int delta = 0;
		  
		try {
			Date relativeDate = new Date();
			delta = (int)((relativeDate.getTime() - date.getTime()) / 1000); // convert to seconds
			if (delta < 60) {
				return "Just now";
			} else if (delta < 120) {
                return "1 minute ago";
			} else if (delta < (60*60)) {
                return Integer.toString(delta / 60) + " minutes ago";
			} else if (delta < (120*60)) {
                return "1 hour ago";
			} else if (delta < (24*60*60)) {
                return Integer.toString(delta / 3600) + " hours ago";
			} else {
                return displayDateFormat.format(date);
			}
		} catch (Exception e) {
			return "Unavailable";
		}
	}
	
    /**
     * Returns a singlular or pluralized word.
     * 
     * @param count count to base if the word should be treated as singular or plural
     * @param singular single version of the word
     * @param plural plural version of the word
     * @return pluralized String
     */
    public static String pluralize(int count, String singular, String plural) {
        return (count == 1 ? singular : plural);
    }

	/**
	 * @param locations - array of lat/longs that make up a route.
	 * @return json array of lat/long JSONObjects
	 */
    public static JSONArray convertLocationsToJson(List<LatLng> locations){
		JSONArray locationsJson = new JSONArray();
		try {
			for (LatLng location : locations) {
				JSONObject locationJson = new JSONObject();
				locationJson.put("latitude", location.latitude);
				locationJson.put("longitude", location.longitude);
				locationsJson.put(locationJson);
			}
		} catch (JSONException e){
			e.printStackTrace();
		}
		return locationsJson;
	}

	/**
	 * converts a JSONArray of location data (ex. [{"latitude":0.0, "longitude":0.0}, ...]) into an ArrayList<LatLng>
	 * @param jsonLocations
	 */
	public static ArrayList<LatLng> getRouteArrayList(JSONArray jsonLocations){
		ArrayList<LatLng> myRouteLocations = new ArrayList<>();
		try {
			for (int i = 0; i < jsonLocations.length(); i++){
				myRouteLocations.add(new LatLng(jsonLocations.getJSONObject(i).getDouble("latitude"), jsonLocations.getJSONObject(i).getDouble("longitude")));
			}
		} catch (JSONException e){
			e.printStackTrace();
		}
		return myRouteLocations;
	}


}
