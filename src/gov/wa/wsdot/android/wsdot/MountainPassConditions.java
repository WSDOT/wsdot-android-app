/*
 * Copyright (c) 2010 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MountainPassConditions extends ListActivity {
	private static final String DEBUG_TAG = "MountainPassConditions";
	private ArrayList<MountainPassItem> mountainPassItems = null;
	private MountainPassItemAdapter adapter;

	private HashMap<Integer, String[]> weatherPhrases = new HashMap<Integer, String[]>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((TextView)findViewById(R.id.sub_section)).setText("Mountain Passes");
        mountainPassItems = new ArrayList<MountainPassItem>();
        this.adapter = new MountainPassItemAdapter(this, R.layout.row, mountainPassItems);
        setListAdapter(this.adapter);     
        buildWeatherPhrases();
        new GetMountainPassItems().execute();       
    }
	
	private void buildWeatherPhrases() {
		String[] weather_clear = {"clear"};
		String[] weather_few_clouds = {"scattered clouds"};
		String[] weather_partly_cloudy = {"partly cloudy"};
		String[] weather_mostly_cloudy = {"broken"};
		String[] weather_overcast = {"overcast"};
		String[] weather_light_rain = {"light rain"};
		String[] weather_rain = {"raining"};
		String[] weather_snow = {"snow", "snowing"};
		
		weatherPhrases.put(R.drawable.weather_clear, weather_clear);
		weatherPhrases.put(R.drawable.weather_few_clouds, weather_few_clouds);
		weatherPhrases.put(R.drawable.weather_partly_cloudy, weather_partly_cloudy);
		weatherPhrases.put(R.drawable.weather_mostly_cloudy, weather_mostly_cloudy);
		weatherPhrases.put(R.drawable.weather_overcast, weather_overcast);
		weatherPhrases.put(R.drawable.weather_light_rain, weather_light_rain);
		weatherPhrases.put(R.drawable.weather_rain, weather_rain);
		weatherPhrases.put(R.drawable.weather_snow, weather_snow);
		
		return;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, MountainPassItemTabs.class);
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
		private final ProgressDialog dialog = new ProgressDialog(MountainPassConditions.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving mountain pass conditions ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setMax(15);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }				
			});
	        this.dialog.show();
		}
		
	    protected void onCancelled() {
	        Toast.makeText(MountainPassConditions.this, "Cancelled", Toast.LENGTH_SHORT).show();
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
				
				for (int j=0; j < passConditions.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject pass = passConditions.getJSONObject(j);
						i = new MountainPassItem();
						weatherCondition = pass.getString("WeatherCondition");
						Integer weather_image = getWeatherImage(weatherPhrases, weatherCondition);
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
						publishProgress(1);
					} else {
						break;
					}
				}
				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}		
		
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
            if(mountainPassItems != null && mountainPassItems.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<mountainPassItems.size();i++)
                adapter.add(mountainPassItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}
	}      
	
	@SuppressWarnings("unchecked")
	private static Integer getWeatherImage(HashMap<Integer, String[]> weatherPhrases, String weather) {
		Integer image = R.drawable.weather_na;
		Set set = weatherPhrases.entrySet();
		Iterator i = set.iterator();
		
		if (weather.equals("")) return image;
		
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			for (String phrase: (String[])me.getValue()) {
				String patternStr = phrase;
				Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(weather);
				boolean matchFound = matcher.find();
				if (matchFound) {
					image = (Integer)me.getKey();
				}
			}
		}	
		return image;
	}
    
	private class MountainPassItemAdapter extends ArrayAdapter<MountainPassItem> {
        private ArrayList<MountainPassItem> items;

        public MountainPassItemAdapter(Context context, int textViewResourceId, ArrayList<MountainPassItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.row, null);
	        }
	        MountainPassItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(R.id.toptext);
	            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
	            ImageView iv = (ImageView) v.findViewById(R.id.icon);
	            if (tt != null) {
	            	tt.setText(o.getMountainPassName());
	            }
	            if(bt != null) {
            		bt.setText(o.getWeatherCondition());
	            }
	       		iv.setImageResource(o.getWeatherIcon());
	        }
	        return v;
        }
	}
}
