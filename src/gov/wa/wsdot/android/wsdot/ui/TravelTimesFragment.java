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
import gov.wa.wsdot.android.wsdot.shared.TravelTimesItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

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
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class TravelTimesFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<TravelTimesItem>> {

	private static final String DEBUG_TAG = "TravelTimes";
	private static TravelTimesItemAdapter adapter;
	private static View mLoadingSpinner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Traffic Map/Travel Times");
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
		
		adapter = new TravelTimesItemAdapter(getActivity());
		setListAdapter(adapter);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
	}

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.refresh, menu);
		
        //Create the search view
        SearchView searchView = new SearchView(getSherlockActivity().getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Search Travel Times…");
		
        menu.add(R.string.search_title)
        .setIcon(R.drawable.ic_menu_search)
        .setActionView(searchView)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			getLoaderManager().restartLoader(0, null, this);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public Loader<ArrayList<TravelTimesItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new TravelTimesLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<TravelTimesItem>> loader,
			ArrayList<TravelTimesItem> data) {

		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);		
	}

	public void onLoaderReset(Loader<ArrayList<TravelTimesItem>> loader) {
		adapter.setData(null);		
	}
	
	/**
	 * A custom Loader that loads all of the travel times from the data server.
	 */
	public static class TravelTimesLoader extends AsyncTaskLoader<ArrayList<TravelTimesItem>> {

		public TravelTimesLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<TravelTimesItem> loadInBackground() {
			ArrayList<TravelTimesItem> travelTimesItems = new ArrayList<TravelTimesItem>();
			TravelTimesItem i = null;
			
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
							
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new TravelTimesItem();
					i.setTitle(item.getString("title"));
					i.setCurrentTime(Integer.toString(item.getInt("current")));
					i.setAverageTime(Integer.toString(item.getInt("average")));
					i.setDistance(item.getString("distance") + " miles");
					i.setRouteID(item.getString("routeid"));
					i.setUpdated(ParserUtils.relativeTime(item.getString("updated"), "yyyy-MM-dd h:mm a", false));
					travelTimesItems.add(i);
				}

			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			
			return travelTimesItems;
		}

		@Override
		public void deliverResult(ArrayList<TravelTimesItem> data) {
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
		public void onCanceled(ArrayList<TravelTimesItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();
		}
		
	}
	
	private class TravelTimesItemAdapter extends ArrayAdapter<TravelTimesItem> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        private final LayoutInflater mInflater;
        
        public TravelTimesItemAdapter(Context context) {
	        super(context, R.layout.list_item_travel_times);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }        
        
        public void setData(ArrayList<TravelTimesItem> data) {
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
	            convertView = mInflater.inflate(R.layout.list_item_travel_times, null);
	        }
	        
	        TravelTimesItem item = getItem(position);
	        String distance;
	        String average_time;
	        
	        if (item != null) {
	            TextView description = (TextView) convertView.findViewById(R.id.description);
	            description.setTypeface(tfb);
	            TextView current_time = (TextView) convertView.findViewById(R.id.current_time);
	            current_time.setTypeface(tfb);
	            TextView distance_average_time = (TextView) convertView.findViewById(R.id.distance_average_time);
	            distance_average_time.setTypeface(tf);
	            TextView updated = (TextView) convertView.findViewById(R.id.updated);
	            updated.setTypeface(tf);
	            
            	description.setText(item.getTitle());
            	distance = item.getDistance();

            	if (Integer.parseInt(item.getAverageTime()) == 0) {
            		average_time = "Not Available";
            	} else {
            		average_time = item.getAverageTime() + " min";
            	}

            	distance_average_time.setText(distance + " / " + average_time);

            	if (Integer.parseInt(item.getCurrentTime()) < Integer.parseInt(item.getAverageTime())) {
            		current_time.setTextColor(0xFF008060);
            	} else if (Integer.parseInt(item.getCurrentTime()) > Integer.parseInt(item.getAverageTime()) && (Integer.parseInt(item.getAverageTime()) != 0)) {
            		current_time.setTextColor(Color.RED);
            	} else {
            		current_time.setTextColor(Color.BLACK);
            	}

            	current_time.setText(item.getCurrentTime() + " min");
            	updated.setText(item.getUpdated());

	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView description;
		public TextView current_time;
		public TextView distance_average_time;
		public TextView updated;
	}	

}
