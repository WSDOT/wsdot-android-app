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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.ForecastItem;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MountainPassItemForecastFragment extends ListFragment {
	
    private ArrayList<ForecastItem> forecastItems;
	private MountainPassItemForecastAdapter adapter;
	private static String forecastsArray;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Bundle args = activity.getIntent().getExtras();
		forecastsArray = args.getString("Forecasts");
        
        JSONArray forecasts;
        ForecastItem f = null;
        forecastItems = new ArrayList<ForecastItem>();
        
        try {
			forecasts = new JSONArray(forecastsArray);
			int numForecasts = forecasts.length();
			for (int i=0; i < numForecasts; i++) {
				JSONObject forecast = forecasts.getJSONObject(i);
				f = new ForecastItem();
				f.setDay(forecast.getString("Day"));
				f.setForecastText(forecast.getString("ForecastText"));
				f.setWeatherIcon(forecast.getInt("weather_icon"));
				forecastItems.add(f);
			}
        } catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    	
    	return root;
	}	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        this.adapter = new MountainPassItemForecastAdapter(getActivity(), R.layout.simple_list_item_with_icon, forecastItems);
        setListAdapter(this.adapter);
	}	

	private class MountainPassItemForecastAdapter extends ArrayAdapter<ForecastItem> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private ArrayList<ForecastItem> items;

        public MountainPassItemForecastAdapter(Context context, int textViewResourceId, ArrayList<ForecastItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }        
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
        	
        	if (convertView == null) {
	            convertView = getActivity().getLayoutInflater().inflate(R.layout.simple_list_item_with_icon, null);
	            holder = new ViewHolder();
	            holder.title = (TextView) convertView.findViewById(R.id.title);
	            holder.title.setTypeface(tfb);
	            holder.text = (TextView) convertView.findViewById(R.id.text);
	            holder.text.setTypeface(tf);
	            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
        	
	        ForecastItem o = items.get(position);

           	holder.title.setText(o.getDay());
       		holder.text.setText(o.getForecastText());
            holder.icon.setImageResource(o.getWeatherIcon());

	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView text;
		public ImageView icon;
	}
}
