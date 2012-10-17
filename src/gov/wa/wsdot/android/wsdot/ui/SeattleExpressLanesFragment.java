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

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SeattleExpressLanesFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<ExpressLaneItem>> {
	
	private static ExpressLaneItemAdapter adapter;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	private View mEmptyView;
	private static View mLoadingSpinner;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
		setHasOptionsMenu(true);
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Traffic Map/Seattle/Express Lanes");
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
        mEmptyView = root.findViewById( R.id.empty_list_view );

        return root;
    } 	
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ExpressLaneItemAdapter(getActivity());
        setListAdapter(adapter);
        
        routeImage.put(5, R.drawable.ic_list_i5);
        routeImage.put(90, R.drawable.ic_list_i90);        
        
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
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
	

	public Loader<ArrayList<ExpressLaneItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new ExpressLaneStatusLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<ExpressLaneItem>> loader, ArrayList<ExpressLaneItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);

		if (!data.isEmpty()) {
			adapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
			getListView().setEmptyView(mEmptyView);
		}
	}

	public void onLoaderReset(Loader<ArrayList<ExpressLaneItem>> loader) {
		adapter.setData(null);
	}
	
	/**
	 * A custom Loader that loads the express lane status from the data server.
	 */
	public static class ExpressLaneStatusLoader extends AsyncTaskLoader<ArrayList<ExpressLaneItem>> {

		public ExpressLaneStatusLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<ExpressLaneItem> loadInBackground() {
			ArrayList<ExpressLaneItem> expressLaneItems = new ArrayList<ExpressLaneItem>();
			ExpressLaneItem i = null;
			
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
							
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new ExpressLaneItem();
					i.setTitle(item.getString("title"));
					i.setRoute(item.getInt("route"));
					i.setStatus(item.getString("status"));
					i.setUpdated(ParserUtils.relativeTime(item.getString("updated"), "yyyy-MM-dd h:mm a", false));
					expressLaneItems.add(i);
				}
				
				Collections.sort(expressLaneItems, new RouteComparator());

			} catch (Exception e) {
				Log.e("SeattleExpressLanes", "Error in network call", e);
			}
			
			return expressLaneItems;

		}

		@Override
		public void deliverResult(ArrayList<ExpressLaneItem> data) {
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
		public void onCanceled(ArrayList<ExpressLaneItem> data) {
			super.onCanceled(data);
		}		
		
		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();
		}
		
	}
	
    private static class RouteComparator implements Comparator<ExpressLaneItem> {

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
    
	private class ExpressLaneItemAdapter extends ArrayAdapter<ExpressLaneItem> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private final LayoutInflater mInflater;
        
        public ExpressLaneItemAdapter(Context context) {
	        super(context, R.layout.simple_list_item_with_icon);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }        
        
        public void setData(ArrayList<ExpressLaneItem> data) {
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
        	ViewHolder holder = null;
        	
        	if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.simple_list_item_with_icon, null);
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
	        
	        ExpressLaneItem item = getItem(position);
           	
	        holder.title.setText(item.getTitle() + " " + item.getStatus());
        	holder.text.setText(item.getUpdated());
            holder.icon.setImageResource(routeImage.get(item.getRoute()));
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView text;
		public ImageView icon;
	}

}
