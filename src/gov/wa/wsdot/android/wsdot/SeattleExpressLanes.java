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

import gov.wa.wsdot.android.wsdot.shared.ExpressLaneItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SeattleExpressLanes extends SherlockListActivity {
	private ArrayList<ExpressLaneItem> expressLaneItems;
	private ExpressLaneItemAdapter adapter;
	
	private HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	private View mLoadingSpinner;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/Seattle/Express Lanes");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);        

        routeImage.put(5, R.drawable.ic_list_i5);
        routeImage.put(90, R.drawable.ic_list_i90);
        
        expressLaneItems = new ArrayList<ExpressLaneItem>();
        this.adapter = new ExpressLaneItemAdapter(this, R.layout.simple_list_item_with_icon, expressLaneItems);
        setListAdapter(this.adapter);
        
        new GetExpressLaneStatus().execute();
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
	    	expressLaneItems.clear();
	    	new GetExpressLaneStatus().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
    private class RouteComparator implements Comparator<ExpressLaneItem> {

    	public int compare(ExpressLaneItem object1, ExpressLaneItem object2) {
			int route1 = object1.getRoute();
			int route2 = object2.getRoute();
			
			if (route1 > route2) {
				return 1;
			} else if (route1 < route2) {
				return -1;
			} else {
				return 0;
			}			
		}    	
    }

    private class GetExpressLaneStatus extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(SeattleExpressLanes.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/ExpressLanes.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("express_lanes");
				JSONArray items = result.getJSONArray("routes");
				expressLaneItems = new ArrayList<ExpressLaneItem>();
				ExpressLaneItem i = null;
							
				for (int j=0; j < items.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new ExpressLaneItem();
						i.setTitle(item.getString("title"));
						i.setRoute(item.getInt("route"));
						i.setStatus(item.getString("status"));
						i.setUpdated(ParserUtils.relativeTime(item.getString("updated"), "yyyy-MM-dd h:mm a", false));
						expressLaneItems.add(i);
					} else {
						break;
					}
				}
				
				Collections.sort(expressLaneItems, new RouteComparator());

			} catch (Exception e) {
				Log.e("SeattleExpressLanes", "Error in network call", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
			if (expressLaneItems != null & expressLaneItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<expressLaneItems.size();i++)
                adapter.add(expressLaneItems.get(i));				
			}
			adapter.notifyDataSetChanged();
		}   
    }
    
    
	private class ExpressLaneItemAdapter extends ArrayAdapter<ExpressLaneItem> {
        private Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
		
		private ArrayList<ExpressLaneItem> items;

        public ExpressLaneItemAdapter(Context context, int textViewResourceId, ArrayList<ExpressLaneItem> items) {
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
	            convertView = getLayoutInflater().inflate(R.layout.simple_list_item_with_icon, null);
	        }
	        ExpressLaneItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tfb);
	            TextView bt = (TextView) convertView.findViewById(R.id.text);
	            bt.setTypeface(tf);
	            ImageView iv = (ImageView) convertView.findViewById(R.id.icon);
	            
	            if (tt != null) {
	            	tt.setText(o.getTitle() + " " + o.getStatus());
	            }
	            
	            if(bt != null) {
            		bt.setText(o.getUpdated());
	            }
	            
	       		iv.setImageResource(routeImage.get(o.getRoute()));
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
		public TextView bt;
		public ImageView iv;
	}
}
