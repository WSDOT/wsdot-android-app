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

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Caches;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;

public class MountainPassesSyncService extends IntentService {

	private static final String DEBUG_TAG = "MountainPassesSyncService";
	@SuppressLint("UseSparseArrays")
	private static HashMap<String, String[]> weatherPhrases = new HashMap<>();
	@SuppressLint("UseSparseArrays")
	private static HashMap<String, String[]> weatherPhrasesNight = new HashMap<>();
	private static DateFormat parseDateFormat = new SimpleDateFormat("yyyy,M,d,H,m"); //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]
	private static DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	private static final String MOUNTAIN_PASS_URL = "http://data.wsdot.wa.gov/mobile/MountainPassConditions.js.gz";
	
	public MountainPassesSyncService() {
		super("MountainPassesSyncService");
	}

	@SuppressLint("LongLogTag")
	@Override
	protected void onHandleIntent(Intent intent) {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		long now = System.currentTimeMillis();
		boolean shouldUpdate = true;
		String responseString = "";

		/** 
		 * Check the cache table for the last time data was downloaded. If we are within
		 * the allowed time period, don't sync, otherwise get fresh data from the server.
		 */
		try {
			cursor = resolver.query(
					Caches.CONTENT_URI,
					new String[] {Caches.CACHE_LAST_UPDATED},
					Caches.CACHE_TABLE_NAME + " LIKE ?",
					new String[] {"mountain_passes"},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				long lastUpdated = cursor.getLong(0);
				//long deltaMinutes = (now - lastUpdated) / DateUtils.MINUTE_IN_MILLIS;
				//Log.d(DEBUG_TAG, "Delta since last update is " + deltaMinutes + " min");
				shouldUpdate = (Math.abs(now - lastUpdated) > (15 * DateUtils.MINUTE_IN_MILLIS));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		// Ability to force a refresh of camera data.
		boolean forceUpdate = intent.getBooleanExtra("forceUpdate", false);
		
		if (shouldUpdate || forceUpdate) {
			List<Integer> starred = new ArrayList<Integer>();

			starred = getStarred();
	        buildWeatherPhrases();
			
			try {
				URL url = new URL(MOUNTAIN_PASS_URL);
				URLConnection urlConn = url.openConnection();
				
				BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);
				
				String mDateUpdated = "";
                String jsonFile = "";
				String line;
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("GetMountainPassConditionsResult");
				JSONArray passConditions = result.getJSONArray("PassCondition");
				String weatherCondition;
				String weather_image_name;
				String forecast_weather_image_name;
				List<ContentValues> passes = new ArrayList<ContentValues>();
				
				int numConditions = passConditions.length();
				for (int j=0; j < numConditions; j++) {
					JSONObject pass = passConditions.getJSONObject(j);
					ContentValues passData = new ContentValues();
					weatherCondition = pass.getString("WeatherCondition");
					weather_image_name = getWeatherImage(weatherPhrases, weatherCondition);

				    String tempDate = pass.getString("DateUpdated");
				    
					try {
						tempDate = tempDate.replace("[", "");
						tempDate = tempDate.replace("]", "");
						
						String[] a = tempDate.split(",");
						StringBuilder sb = new StringBuilder();
						for (int m=0; m < 5; m++) {
							sb.append(a[m]);
							sb.append(",");
						}
						tempDate = sb.toString().trim();
						tempDate = tempDate.substring(0, tempDate.length()-1);
						Date date = parseDateFormat.parse(tempDate);
						mDateUpdated = displayDateFormat.format(date);
					} catch (Exception e) {
						Log.e(DEBUG_TAG, "Error parsing date: " + tempDate, e);
						mDateUpdated = "N/A";
					}					
					
					JSONArray forecasts = pass.getJSONArray("Forecast");
					JSONArray forecastItems = new JSONArray();
					
					int numForecasts = forecasts.length();
					for (int l=0; l < numForecasts; l++) {
						JSONObject forecast = forecasts.getJSONObject(l);
						
						if (isNight(forecast.getString("Day"))) {
							forecast_weather_image_name = getWeatherImage(weatherPhrasesNight, forecast.getString("ForecastText"));
						} else {
							forecast_weather_image_name = getWeatherImage(weatherPhrases, forecast.getString("ForecastText"));
						}
						
						forecast.put("weather_icon", forecast_weather_image_name);
						
						if (l == 0) {
							if (weatherCondition.equals("")) {
								weatherCondition = forecast.getString("ForecastText").split("\\.")[0] + ".";
								weather_image_name = forecast_weather_image_name;
							}
						}
						
						forecastItems.put(forecast);
					}
					
					passData.put(MountainPasses.MOUNTAIN_PASS_ID, pass.getString("MountainPassId"));
					passData.put(MountainPasses.MOUNTAIN_PASS_NAME, pass.getString("MountainPassName"));
					passData.put(MountainPasses.MOUNTAIN_PASS_WEATHER_ICON, weather_image_name);
					passData.put(MountainPasses.MOUNTAIN_PASS_FORECAST, forecastItems.toString());
					passData.put(MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION, weatherCondition);
					passData.put(MountainPasses.MOUNTAIN_PASS_DATE_UPDATED, mDateUpdated);
					passData.put(MountainPasses.MOUNTAIN_PASS_CAMERA, pass.getString("Cameras"));
					passData.put(MountainPasses.MOUNTAIN_PASS_ELEVATION, pass.getString("ElevationInFeet"));
					passData.put(MountainPasses.MOUNTAIN_PASS_TRAVEL_ADVISORY_ACTIVE, pass.getString("TravelAdvisoryActive"));
					passData.put(MountainPasses.MOUNTAIN_PASS_ROAD_CONDITION, pass.getString("RoadCondition"));
					passData.put(MountainPasses.MOUNTAIN_PASS_TEMPERATURE, pass.getString("TemperatureInFahrenheit"));
					JSONObject restrictionOne = pass.getJSONObject("RestrictionOne");
					passData.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE, restrictionOne.getString("RestrictionText"));
					passData.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION, restrictionOne.getString("TravelDirection"));
					JSONObject restrictionTwo = pass.getJSONObject("RestrictionTwo");
					passData.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO, restrictionTwo.getString("RestrictionText"));
					passData.put(MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION, restrictionTwo.getString("TravelDirection"));
					
					if (starred.contains(Integer.parseInt(pass.getString("MountainPassId")))) {
						passData.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, 1);
					}
					
					passes.add(passData);

				}
				
				// Purge existing mountain passes covered by incoming data
				resolver.delete(MountainPasses.CONTENT_URI, null, null);
				// Bulk insert all the new mountain passes
				resolver.bulkInsert(MountainPasses.CONTENT_URI, passes.toArray(new ContentValues[passes.size()]));		
				// Update the cache table with the time we did the update
				ContentValues values = new ContentValues();
				values.put(Caches.CACHE_LAST_UPDATED, System.currentTimeMillis());
				resolver.update(
						Caches.CONTENT_URI,
						values, Caches.CACHE_TABLE_NAME + "=?",
						new String[] {"mountain_passes"}
						);
				
				responseString = "OK";
	    	} catch (Exception e) {
	    		Log.e(DEBUG_TAG, "Error: " + e.getMessage());
	    		responseString = e.getMessage();
			}
			
		} else {
			responseString = "NOP";
		}
		
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("responseString", responseString);
        sendBroadcast(broadcastIntent);		
	}
	
	private void buildWeatherPhrases() {
		String[] weather_clear = {"fair", "sunny", "clear"};
		String[] weather_few_clouds = {"few clouds", "scattered clouds", "scattered clouds", "mostly sunny", "mostly clear"};
		String[] weather_partly_cloudy = {"partly cloudy", "partly sunny"};
		String[] weather_cloudy = {"cloudy", "increasing clouds"};
		String[] weather_mostly_cloudy = {"broken", "mostly cloudy"};
		String[] weather_overcast = {"overcast"};
		String[] weather_light_rain = {"light rain", "showers"};
		String[] weather_rain = {"rain", "heavy rain", "raining"};
		String[] weather_snow = {"snow", "snowing", "light snow", "heavy snow"};
		String[] weather_fog = {"fog"};
		String[] weather_sleet = {"rain snow", "light rain snow", "heavy rain snow", "rain and snow"};
		String[] weather_hail = {"ice pellets", "light ice pellets", "heavy ice pellets", "hail"};
		String[] weather_thunderstorm = {"thunderstorm", "thunderstorms"};
		
		weatherPhrases.put("ic_list_sunny", weather_clear);
		weatherPhrases.put("ic_list_cloudy_1", weather_few_clouds);
		weatherPhrases.put("ic_list_cloudy_2", weather_partly_cloudy);
		weatherPhrases.put("ic_list_cloudy_3", weather_cloudy);
		weatherPhrases.put("ic_list_cloudy_4", weather_mostly_cloudy);
		weatherPhrases.put("ic_list_overcast", weather_overcast);
		weatherPhrases.put("ic_list_light_rain", weather_light_rain);
		weatherPhrases.put("ic_list_shower_3", weather_rain);
		weatherPhrases.put("ic_list_snow_4", weather_snow);
		weatherPhrases.put("ic_list_fog", weather_fog);
		weatherPhrases.put("ic_list_sleet", weather_sleet);
		weatherPhrases.put("ic_list_hail", weather_hail);
		weatherPhrases.put("ic_list_tstorm_3", weather_thunderstorm);
		
		weatherPhrasesNight.put("ic_list_sunny_night", weather_clear);
		weatherPhrasesNight.put("ic_list_cloudy_1_night", weather_few_clouds);
		weatherPhrasesNight.put("ic_list_cloudy_2_night", weather_partly_cloudy);
		weatherPhrasesNight.put("ic_list_cloudy_3_night", weather_cloudy);
		weatherPhrasesNight.put("ic_list_cloudy_4_night", weather_mostly_cloudy);
		weatherPhrasesNight.put("ic_list_overcast", weather_overcast);
		weatherPhrasesNight.put("ic_list_light_rain", weather_light_rain);
		weatherPhrasesNight.put("ic_list_shower_3", weather_rain);
		weatherPhrasesNight.put("ic_list_snow_4", weather_snow);
		weatherPhrasesNight.put("ic_list_fog_night", weather_fog);
		weatherPhrasesNight.put("ic_list_sleet", weather_sleet);
		weatherPhrasesNight.put("ic_list_hail", weather_hail);
		weatherPhrasesNight.put("ic_list_tstorm_3", weather_thunderstorm);
		
		return;
	}
	
	private static String getWeatherImage(HashMap<String, String[]> weatherPhrases, String weather) {
		String image_name = "weather_na";
		Set<Entry<String, String[]>> set = weatherPhrases.entrySet();
		Iterator<Entry<String, String[]>> i = set.iterator();
		
		if (weather.equals("")) return image_name;

		String s0 = weather.split("\\.")[0]; // Pattern match on first sentence only.
		
		while(i.hasNext()) {
			Entry<String, String[]> me = i.next();
			for (String phrase: (String[])me.getValue()) {
				if (s0.toLowerCase().startsWith(phrase)) {
					image_name = (String)me.getKey();
					return image_name;
				}
			}
		}
		
		return image_name;
	}
    
	private static boolean isNight(String text) {
		String patternStr = "night|tonight";
		Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);
		boolean matchFound = matcher.find();
		
		return matchFound;
	}
	
	/** 
	 * Check the mountain pass table for any starred entries. If we find some, save them
	 * to a list so we can re-star those passes after we flush the database.
	 */	
	private List<Integer> getStarred() {
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		List<Integer> starred = new ArrayList<Integer>();

		try {
			cursor = resolver.query(
					MountainPasses.CONTENT_URI,
					new String[] {MountainPasses.MOUNTAIN_PASS_ID},
					MountainPasses.MOUNTAIN_PASS_IS_STARRED + "=?",
					new String[] {"1"},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					starred.add(cursor.getInt(0));
					cursor.moveToNext();
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return starred;
	}

}
