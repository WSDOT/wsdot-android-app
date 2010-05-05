package gov.wa.wsdot.android.wsdot;

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

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MountainPassConditions extends ListActivity {
	private static final String DEBUG_TAG = "MountainPassConditions";
	private ArrayList<MountainPassItem> mountainPassItems = null;
	private MountainPassItemAdapter adapter;
	WebView webview;

	private HashMap<Integer, String[]> weatherPhrases = new HashMap<Integer, String[]>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
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
		b.putStringArrayList("Cameras", mountainPassItems.get(position).getCameraUrls());
		intent.putExtras(b);
		startActivity(intent);
	}
   
	private class GetMountainPassItems extends AsyncTask<String, Integer, String> {
		private final ProgressDialog dialog = new ProgressDialog(MountainPassConditions.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving mountain pass conditions ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setCancelable(true);
	        this.dialog.setMax(15);
	        this.dialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/MountainPassConditions.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
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
				
				for (int j=0; j < passConditions.length(); j++) {
					JSONObject pass = passConditions.getJSONObject(j);
					ArrayList<String> urls = new ArrayList<String>();
					i = new MountainPassItem();
					weatherCondition = pass.getString("WeatherCondition");
					Integer weather_image = getWeatherImage(weatherPhrases, weatherCondition);
					i.setWeatherIcon(weather_image);
					
					JSONArray cameras = pass.getJSONArray("Cameras");
					for (int k=0; k < cameras.length(); k++) {
						urls.add(cameras.getString(k));
					}
					i.setCameraUrls(urls);				
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
