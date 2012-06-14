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
import gov.wa.wsdot.android.wsdot.shared.BorderWaitItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
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

public class BorderWaitSouthboundFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<BorderWaitItem>> {
	
	private static final String DEBUG_TAG = "BorderWaitSouthbound";
	private static BorderWaitItemAdapter adapter;	
	private static HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	private static View mLoadingSpinner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
		setHasOptionsMenu(true);
		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Canadian Border/Southbound");
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

        adapter = new BorderWaitItemAdapter(getActivity());
        setListAdapter(adapter);
        
        routeImage.put(5, R.drawable.i5);
        routeImage.put(9, R.drawable.sr9);
        routeImage.put(539, R.drawable.sr539);
        routeImage.put(543, R.drawable.sr543);
        routeImage.put(97, R.drawable.us97);        
        
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

	public Loader<ArrayList<BorderWaitItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new BorderWaitItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<BorderWaitItem>> loader, ArrayList<BorderWaitItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);
	}

	public void onLoaderReset(Loader<ArrayList<BorderWaitItem>> loader) {
		adapter.setData(null);
	}    
    
	/**
	 * A custom Loader that loads all of the border wait times from the data server.
	 */		
	public static class BorderWaitItemsLoader extends AsyncTaskLoader<ArrayList<BorderWaitItem>> {

		public BorderWaitItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<BorderWaitItem> loadInBackground() {
			ArrayList<BorderWaitItem> borderWaitItems = new ArrayList<BorderWaitItem>();
			BorderWaitItem i = null;
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/BorderCrossings.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("waittimes");
				JSONArray items = result.getJSONArray("items");
							
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new BorderWaitItem();
					i.setLane(item.getString("lane"));
					i.setUpdated(item.getString("updated"));
					i.setName(item.getString("name"));
					i.setRoute(item.getInt("route"));
					i.setDirection(item.getString("direction"));
					i.setWait(item.getInt("wait"));
					
					if (item.getString("direction").equals("southbound")) {
						borderWaitItems.add(i);
					}
				}			

			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}

			return borderWaitItems;
		}

		@Override
		public void deliverResult(ArrayList<BorderWaitItem> data) {
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
		public void onCanceled(ArrayList<BorderWaitItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();			
		}
	}
	
	public static class BorderWaitItemAdapter extends ArrayAdapter<BorderWaitItem> {
		private final LayoutInflater mInflater;
		
        public BorderWaitItemAdapter(Context context) {
        	super(context, R.layout.borderwait_row);
        	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }
        
        public void setData(ArrayList<BorderWaitItem> data) {
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
	            convertView = mInflater.inflate(R.layout.borderwait_row, null);
	        }
	        
	        BorderWaitItem item = getItem(position);
	        
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.toptext);
	            TextView bt = (TextView) convertView.findViewById(R.id.bottomtext);
	            TextView rt = (TextView) convertView.findViewById(R.id.righttext);
	            ImageView iv = (ImageView) convertView.findViewById(R.id.icon);

	            if (tt != null) {
	            	tt.setText(item.getName() + " (" + item.getLane() + ")");
	            }

	            if (bt != null) {
            		bt.setText(item.getUpdated());
	            }

	            if (rt != null) {
	            	if (item.getWait() == -1) {
	            		rt.setText("N/A");
	            	} else if (item.getWait() < 5) {
	            		rt.setText("< 5 min");
	            	} else {
	            		rt.setText(item.getWait() + " min");
	            	}
	            }

	            iv.setImageResource(routeImage.get(item.getRoute()));
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
		public TextView bt;
		public TextView rt;
		public ImageView iv;
	}
}
