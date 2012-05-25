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

package gov.wa.wsdot.android.wsdot;

import gov.wa.wsdot.android.wsdot.shared.TravelTimesItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SeattleTrafficTravelTimes extends SherlockListActivity {
	private static final String DEBUG_TAG = "TravelTimes";
	private ArrayList<TravelTimesItem> travelTimesItems = null;
	private TravelTimesItemAdapter adapter;
	private View mLoadingSpinner;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/Seattle/Travel Times");
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);
        
        travelTimesItems = new ArrayList<TravelTimesItem>();
        this.adapter = new TravelTimesItemAdapter(this, R.layout.traveltimes_item, travelTimesItems);
        setListAdapter(this.adapter);
        
        new GetTravelTimesItems().execute();
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
			travelTimesItems.clear();
			new GetTravelTimesItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}    
    
    private class GetTravelTimesItems extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(SeattleTrafficTravelTimes.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }

		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/TravelTimes.js.gz");
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
				JSONObject result = obj.getJSONObject("traveltimes");
				JSONArray items = result.getJSONArray("items");
				travelTimesItems = new ArrayList<TravelTimesItem>();
				TravelTimesItem i = null;
							
				for (int j=0; j < items.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new TravelTimesItem();
						i.setTitle(item.getString("title"));
						i.setCurrentTime(Integer.toString(item.getInt("current")));
						i.setAverageTime(Integer.toString(item.getInt("average")));
						i.setDistance(item.getString("distance") + " miles");
						i.setRouteID(item.getString("routeid"));
						travelTimesItems.add(i);
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

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
            if (travelTimesItems != null && travelTimesItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for (int i=0; i < travelTimesItems.size(); i++)
                adapter.add(travelTimesItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }

	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
	}
	
	private class TravelTimesItemAdapter extends ArrayAdapter<TravelTimesItem> {
        private ArrayList<TravelTimesItem> items;

        public TravelTimesItemAdapter(Context context, int textViewResourceId, ArrayList<TravelTimesItem> items) {
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
	        if (convertView == null) {
	            convertView = getLayoutInflater().inflate(R.layout.traveltimes_item, null);
	        }
	        
	        TravelTimesItem o = items.get(position);
	        if (o != null) {
	            TextView description = (TextView) convertView.findViewById(R.id.route_description);
	            TextView distance = (TextView) convertView.findViewById(R.id.distance);
	            TextView average = (TextView) convertView.findViewById(R.id.average);
	            TextView current = (TextView) convertView.findViewById(R.id.current);
	            
	            if (description != null) {
	            	description.setText(o.getTitle());
	            }
	            if (distance != null) {
            		distance.setText(o.getDistance());
	            }
	            if (average != null) {
	            	if (Integer.parseInt(o.getAverageTime()) == 0) {
	            		average.setText("Not Available");
	            	} else {
	            		average.setText(o.getAverageTime() + " min");
	            	}
	            }
	            if (current != null) {
	            	if (Integer.parseInt(o.getCurrentTime()) < Integer.parseInt(o.getAverageTime())) {
	            		current.setTextColor(0xFF017359);
	            	} else if (Integer.parseInt(o.getCurrentTime()) > Integer.parseInt(o.getAverageTime()) && (Integer.parseInt(o.getAverageTime()) != 0)) {
	            		current.setTextColor(Color.RED);
	            	} else {
	            		current.setTextColor(Color.BLUE);
	            	}
	            	current.setText(o.getCurrentTime() + " min");
	            }
	        }
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView description;
		public TextView distance;
		public TextView average;
		public TextView current;
	}
	
}