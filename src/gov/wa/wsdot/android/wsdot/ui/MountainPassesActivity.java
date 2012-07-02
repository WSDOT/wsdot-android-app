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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.ForecastItem;
import gov.wa.wsdot.android.wsdot.shared.MountainPassItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MountainPassesActivity extends SherlockListActivity {
	private static final String DEBUG_TAG = "MountainPassConditions";
	private ArrayList<MountainPassItem> mountainPassItems = null;
	private MountainPassItemAdapter adapter;

	private HashMap<Integer, String[]> weatherPhrases = new HashMap<Integer, String[]>();
	private HashMap<Integer, String[]> weatherPhrasesNight = new HashMap<Integer, String[]>();
	private View mLoadingSpinner;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Mountain Passes");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);        
        
        mountainPassItems = new ArrayList<MountainPassItem>();
        this.adapter = new MountainPassItemAdapter(this, R.layout.list_item_with_icon, mountainPassItems);
        setListAdapter(this.adapter);     
        buildWeatherPhrases();
        new GetMountainPassItems().execute();       
    }
	
	private void buildWeatherPhrases() {
		String[] weather_clear = {"fair", "sunny", "mostly sunny", "clear", "mostly clear"};
		String[] weather_few_clouds = {"few clouds", "scattered clouds"};
		String[] weather_partly_cloudy = {"partly cloudy"};
		String[] weather_cloudy = {"cloudy"};
		String[] weather_mostly_cloudy = {"broken", "mostly cloudy"};
		String[] weather_overcast = {"overcast"};
		String[] weather_light_rain = {"light rain", "showers"};
		String[] weather_rain = {"rain", "heavy rain", "raining"};
		String[] weather_snow = {"snow", "snowing", "light snow", "heavy snow"};
		String[] weather_fog = {"fog"};
		String[] weather_sleet = {"rain snow", "light rain snow", "heavy rain snow", "rain and snow"};
		String[] weather_hail = {"ice pellets", "light ice pellets", "heavy ice pellets", "hail"};
		
		weatherPhrases.put(R.drawable.ic_list_sunny, weather_clear);
		weatherPhrases.put(R.drawable.ic_list_cloudy_1, weather_few_clouds);
		weatherPhrases.put(R.drawable.ic_list_cloudy_2, weather_partly_cloudy);
		weatherPhrases.put(R.drawable.ic_list_cloudy_3, weather_cloudy);
		weatherPhrases.put(R.drawable.ic_list_cloudy_4, weather_mostly_cloudy);
		weatherPhrases.put(R.drawable.ic_list_overcast, weather_overcast);
		weatherPhrases.put(R.drawable.ic_list_light_rain, weather_light_rain);
		weatherPhrases.put(R.drawable.ic_list_shower_3, weather_rain);
		weatherPhrases.put(R.drawable.ic_list_snow_4, weather_snow);
		weatherPhrases.put(R.drawable.ic_list_fog, weather_fog);
		weatherPhrases.put(R.drawable.ic_list_sleet, weather_sleet);
		weatherPhrases.put(R.drawable.ic_list_hail, weather_hail);
		
		weatherPhrasesNight.put(R.drawable.ic_list_sunny_night, weather_clear);
		weatherPhrasesNight.put(R.drawable.ic_list_cloudy_1_night, weather_few_clouds);
		weatherPhrasesNight.put(R.drawable.ic_list_cloudy_2_night, weather_partly_cloudy);
		weatherPhrasesNight.put(R.drawable.ic_list_cloudy_3_night, weather_cloudy);
		weatherPhrasesNight.put(R.drawable.ic_list_cloudy_4_night, weather_mostly_cloudy);
		weatherPhrasesNight.put(R.drawable.ic_list_overcast, weather_overcast);
		weatherPhrasesNight.put(R.drawable.ic_list_light_rain, weather_light_rain);
		weatherPhrasesNight.put(R.drawable.ic_list_shower_3, weather_rain);
		weatherPhrasesNight.put(R.drawable.ic_list_snow_4, weather_snow);
		weatherPhrasesNight.put(R.drawable.ic_list_fog_night, weather_fog);
		weatherPhrasesNight.put(R.drawable.ic_list_sleet, weather_sleet);
		weatherPhrasesNight.put(R.drawable.ic_list_hail, weather_hail);		
		
		return;
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.refresh, menu);
    	
    	return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
	    	finish();
	    	return true;		
		case R.id.menu_refresh:
			this.adapter.clear();
			mountainPassItems.clear();
			new GetMountainPassItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, MountainPassItemActivity.class);
		b.putString("MountainPassName", mountainPassItems.get(position).getMountainPassName());
		b.putString("DateUpdated", mountainPassItems.get(position).getDateUpdated());
		b.putString("TemperatureInFahrenheit", mountainPassItems.get(position).getTemperatureInFahrenheit());
		b.putString("ElevationInFeet", mountainPassItems.get(position).getElevationInFeet());
		b.putString("RoadCondition", mountainPassItems.get(position).getRoadCondition());
		b.putString("WeatherCondition", mountainPassItems.get(position).getWeatherCondition());
		b.putString("RestrictionOneText", mountainPassItems.get(position).getRestrictionOneText());
		b.putString("RestrictionOneTravelDirection", mountainPassItems.get(position).getRestrictionOneTravelDirection());
		b.putString("RestrictionTwoText", mountainPassItems.get(position).getRestrictionTwoText());
		b.putString("RestrictionTwoTravelDirection", mountainPassItems.get(position).getRestrictionTwoTravelDirection());
		b.putString("Latitude", mountainPassItems.get(position).getLatitude());
		b.putString("Longitude", mountainPassItems.get(position).getLongitude());
		b.putInt("WeatherIcon", mountainPassItems.get(position).getWeatherIcon());
		b.putSerializable("Cameras", mountainPassItems.get(position).getCameraItem());
		b.putSerializable("Forecasts", mountainPassItems.get(position).getForecastItem());
		intent.putExtras(b);
		startActivity(intent);
	}
   
	private class GetMountainPassItems extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}
		
	    protected void onCancelled() {
	        Toast.makeText(MountainPassesActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/MountainPassConditions.js.gz");
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
				JSONObject result = obj.getJSONObject("GetMountainPassConditionsResult");
				JSONArray passConditions = result.getJSONArray("PassCondition");
				String weatherCondition;
				mountainPassItems = new ArrayList<MountainPassItem>();
				MountainPassItem i = null;
				CameraItem c = null;
				ForecastItem f = null;
				Integer weather_image;
				
				for (int j=0; j < passConditions.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject pass = passConditions.getJSONObject(j);
						i = new MountainPassItem();
						weatherCondition = pass.getString("WeatherCondition");
						weather_image = getWeatherImage(weatherPhrases, weatherCondition);
						i.setWeatherIcon(weather_image);
						
						JSONArray cameras = pass.getJSONArray("Cameras");
						for (int k=0; k < cameras.length(); k++) {
							JSONObject camera = cameras.getJSONObject(k);
							c = new CameraItem();
							c.setTitle(camera.getString("title"));
							c.setImageUrl(camera.getString("url"));
							c.setLatitude(camera.getDouble("lat"));
							c.setLongitude(camera.getDouble("lon"));
							i.setCameraItem(c);
						}
						
						JSONArray forecasts = pass.getJSONArray("Forecast");
						for (int l=0; l < forecasts.length(); l++) {
							JSONObject forecast = forecasts.getJSONObject(l);
							f = new ForecastItem();
							f.setDay(forecast.getString("Day"));
							f.setForecastText(forecast.getString("ForecastText"));
							
							if (isNight(f.getDay())) {
								weather_image = getWeatherImage(weatherPhrasesNight, f.getForecastText());
							} else {
								weather_image = getWeatherImage(weatherPhrases, f.getForecastText());
							}
							
							f.setWeatherIcon(weather_image);
							
							if (l == 0) {
								i.setWeatherIcon(weather_image);
							}
							
							i.setForecastItem(f);
						}
						
						i.setWeatherCondition(weatherCondition);
						i.setElevationInFeet(pass.getString("ElevationInFeet"));
						i.setTravelAdvisoryActive(pass.getString("TravelAdvisoryActive"));
						i.setLongitude(pass.getString("Longitude"));
						i.setMountainPassId(pass.getString("MountainPassId"));
						i.setRoadCondition(pass.getString("RoadCondition"));
						i.setTemperatureInFahrenheit(pass.getString("TemperatureInFahrenheit"));
						i.setLatitude(pass.getString("Latitude"));
						i.setDateUpdated(pass.getString("DateUpdated"));
						i.setMountainPassName(pass.getString("MountainPassName"));
						i.setLongitude(pass.getString("Longitude"));
						i.setLatitude(pass.getString("Latitude"));
						JSONObject restrictionOne = pass.getJSONObject("RestrictionOne");
						i.setRestrictionOneText(restrictionOne.getString("RestrictionText"));
						i.setRestrictionOneTravelDirection(restrictionOne.getString("TravelDirection"));
						JSONObject restrictionTwo = pass.getJSONObject("RestrictionTwo");
						i.setRestrictionTwoText(restrictionTwo.getString("RestrictionText"));
						i.setRestrictionTwoTravelDirection(restrictionTwo.getString("TravelDirection"));		
						mountainPassItems.add(i);
					} else {
						break;
					}
				}
				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}		

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
            if(mountainPassItems != null && mountainPassItems.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<mountainPassItems.size();i++)
                adapter.add(mountainPassItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}
	}      
	
	private static Integer getWeatherImage(HashMap<Integer, String[]> weatherPhrases, String weather) {
		Integer image = R.drawable.weather_na;
		Set<Entry<Integer, String[]>> set = weatherPhrases.entrySet();
		Iterator<Entry<Integer, String[]>> i = set.iterator();
		
		if (weather.equals("")) return image;

		String s0 = weather.split("\\.")[0]; // Pattern match on first sentence only.
		
		while(i.hasNext()) {
			Entry<Integer, String[]> me = i.next();
			for (String phrase: (String[])me.getValue()) {
				if (s0.toLowerCase().startsWith(phrase)) {
					image = (Integer)me.getKey();
					return image;
				}
			}
		}
		
		return image;
	}
    
	private static boolean isNight(String text) {
		String patternStr = "night|tonight";
		Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);
		boolean matchFound = matcher.find();
		
		return matchFound;
	}
	
	private class MountainPassItemAdapter extends ArrayAdapter<MountainPassItem> {
        private Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        private ArrayList<MountainPassItem> items;
        
        public MountainPassItemAdapter(Context context, int textViewResourceId, ArrayList<MountainPassItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = getLayoutInflater().inflate(R.layout.list_item_with_icon, null);
	        }
	        MountainPassItem o = items.get(position);
	        if (o != null) {
	        	TextView tt = (TextView) convertView.findViewById(R.id.title);
	        	tt.setTypeface(tf);
	            ImageView iv = (ImageView) convertView.findViewById(R.id.icon);
	            
	            if (tt != null) {
	            	tt.setText(o.getMountainPassName());
	            }
	            
	       		iv.setImageResource(o.getWeatherIcon());
	        }
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public ImageView iv;
		public TextView tt;
	}
}
