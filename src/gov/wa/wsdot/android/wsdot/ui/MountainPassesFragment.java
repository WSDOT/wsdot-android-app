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
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

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
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MountainPassesFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<MountainPassItem>> {
	
	private static final String DEBUG_TAG = "MountainPassConditions";
	private static DateFormat parseDateFormat = new SimpleDateFormat("yyyy,M,d,H,m"); //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]
	private static DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	private static MountainPassItemAdapter adapter;
	private static ArrayList<MountainPassItem> mountainPassItems = null;
	private static HashMap<Integer, String[]> weatherPhrases = new HashMap<Integer, String[]>();
	private static HashMap<Integer, String[]> weatherPhrasesNight = new HashMap<Integer, String[]>();
	private static View mLoadingSpinner;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
		setHasOptionsMenu(true);         
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Mountain Passes");
    }

    @SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);

        return root;
    } 
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
        adapter = new MountainPassItemAdapter(getActivity());
        setListAdapter(adapter);     
        buildWeatherPhrases();	
	
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);        
	}
    
	private void buildWeatherPhrases() {
		String[] weather_clear = {"fair", "sunny", "clear"};
		String[] weather_few_clouds = {"few clouds", "scattered clouds", "mostly sunny", "mostly clear"};
		String[] weather_partly_cloudy = {"partly cloudy", "partly sunny"};
		String[] weather_cloudy = {"cloudy"};
		String[] weather_mostly_cloudy = {"broken", "mostly cloudy"};
		String[] weather_overcast = {"overcast"};
		String[] weather_light_rain = {"light rain", "showers"};
		String[] weather_rain = {"rain", "heavy rain", "raining"};
		String[] weather_snow = {"snow", "snowing", "light snow", "heavy snow"};
		String[] weather_fog = {"fog"};
		String[] weather_sleet = {"rain snow", "light rain snow", "heavy rain snow", "rain and snow"};
		String[] weather_hail = {"ice pellets", "light ice pellets", "heavy ice pellets", "hail"};
		String[] weather_thunderstorm = {"thunderstorm", "thunderstorms"};
		
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
		weatherPhrases.put(R.drawable.ic_list_tstorm_3, weather_thunderstorm);
		
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
		weatherPhrasesNight.put(R.drawable.ic_list_tstorm_3, weather_thunderstorm);
		
		return;
	}

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.refresh, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			getLoaderManager().restartLoader(0, null, this);
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), MountainPassItemActivity.class);
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

	public Loader<ArrayList<MountainPassItem>> onCreateLoader(int id, Bundle args) {
		return new MountainPassItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<MountainPassItem>> loader, ArrayList<MountainPassItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);		
	}

	public void onLoaderReset(Loader<ArrayList<MountainPassItem>> loader) {
		adapter.setData(null);
	}
    
	/**
	 * A custom Loader that loads all of the mountain pass info from the data server.
	 */
	public static class MountainPassItemsLoader extends AsyncTaskLoader<ArrayList<MountainPassItem>> {

		public MountainPassItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<MountainPassItem> loadInBackground() {
			mountainPassItems = new ArrayList<MountainPassItem>();
			MountainPassItem i = null;
			CameraItem c = null;
			ForecastItem f = null;
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/MountainPassConditions.js.gz");
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
				Integer weather_image;
				
				for (int j=0; j < passConditions.length(); j++) {
					JSONObject pass = passConditions.getJSONObject(j);
					i = new MountainPassItem();
					weatherCondition = pass.getString("WeatherCondition");
					weather_image = getWeatherImage(weatherPhrases, weatherCondition);
					i.setWeatherIcon(weather_image);
					
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
							if (weatherCondition.equals("")) {
								weatherCondition = f.getForecastText().split("\\.")[0] + ".";
							}
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
					i.setDateUpdated(mDateUpdated);
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
				}
				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}

			return mountainPassItems;
		}

		@Override
		public void deliverResult(ArrayList<MountainPassItem> data) {
		    /**
		     * Called when there is new data to deliver to the client. The
		     * super class will take care of delivering it; the implementation
		     * here just adds a little more logic.
		     */	
			super.deliverResult(data);
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			
			adapter.clear();
			mLoadingSpinner.setVisibility(View.VISIBLE);
			forceLoad();
		}

		@Override
		protected void onStopLoading() {
			super.onStopLoading();
			
	        // Attempt to cancel the current load task if possible.
	        cancelLoad();		
		}
		
		@Override
		public void onCanceled(ArrayList<MountainPassItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();

	        // Ensure the loader is stopped
	        onStopLoading();
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
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private final LayoutInflater mInflater;
        
        public MountainPassItemAdapter(Context context) {
                super(context, R.layout.list_item_details_with_icon);
                mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<MountainPassItem> data) {
            clear();
            if (data != null) {
                //addAll(data); // Only in API level 11
                notifyDataSetChanged();
                for (int i=0; i < data.size(); i++) {
                	add(data.get(i));
                }
                notifyDataSetChanged();                
            }
        }        
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item_details_with_icon, null);
	        }
	        MountainPassItem item = getItem(position);
	        
	        if (item != null) {
	        	TextView title = (TextView) convertView.findViewById(R.id.title);
	        	title.setTypeface(tfb);
	        	TextView created_at = (TextView) convertView.findViewById(R.id.created_at);
	        	created_at.setTypeface(tf);
	        	TextView text = (TextView) convertView.findViewById(R.id.text);
	            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
	            
	       		icon.setImageResource(item.getWeatherIcon());
	            title.setText(item.getMountainPassName());
	            created_at.setText(ParserUtils.relativeTime(item.getDateUpdated(), "MMMM d, yyyy h:mm a", false));
	            
	            if (item.getWeatherCondition().equals("")) {
	            	text.setVisibility(View.GONE);
	            } else {
	            	text.setVisibility(View.VISIBLE);
	            	text.setText(item.getWeatherCondition());
	            }
	        }
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public ImageView iv;
		public TextView tt;
	}

}
