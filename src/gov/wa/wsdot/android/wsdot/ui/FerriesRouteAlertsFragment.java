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
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteAlertItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteItem;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FerriesRouteAlertsFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<FerriesRouteItem>> {

	private static final String DEBUG_TAG = "RouteAlerts";
	private static ArrayList<FerriesRouteItem> routeItems = null;
	private static RouteItemAdapter adapter;
	private static View mLoadingSpinner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
        setHasOptionsMenu(true);
        
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Ferries/Route Alerts");
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

        adapter = new RouteItemAdapter(getActivity());
        setListAdapter(adapter);
        
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
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), FerriesRouteAlertsBulletinsActivity.class);
		b.putString("description", routeItems.get(position).getDescription());
		b.putSerializable("routeItems", routeItems.get(position));
		intent.putExtras(b);
		startActivity(intent);
	}	
	
	public Loader<ArrayList<FerriesRouteItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new RouteAlertsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<FerriesRouteItem>> loader, ArrayList<FerriesRouteItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);
		
	}

	public void onLoaderReset(Loader<ArrayList<FerriesRouteItem>> loader) {
		adapter.setData(null);
	}

	/**
	 * A custom Loader that loads all of the WSF route alerts from the data server.
	 */		
	public static class RouteAlertsLoader extends AsyncTaskLoader<ArrayList<FerriesRouteItem>> {

		public RouteAlertsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesRouteItem> loadInBackground() {
			routeItems = new ArrayList<FerriesRouteItem>();
			FerriesRouteItem i = null;
			FerriesRouteAlertItem a = null;
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/WSFRouteAlerts.js.gz");
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
				
				JSONArray items = new JSONArray(jsonFile);
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new FerriesRouteItem();
					i.setRouteID(item.getInt("RouteID"));
					i.setDescription(item.getString("Description"));
					
					JSONArray alerts = item.getJSONArray("RouteAlert");
					
					for (int k=0; k < alerts.length(); k++) {
						JSONObject alert = alerts.getJSONObject(k);
						a = new FerriesRouteAlertItem();
						a.setBulletinID(alert.getInt("BulletinID"));
						a.setPublishDate(alert.getString("PublishDate").substring(6, 19));
						a.setAlertDescription(alert.getString("AlertDescription"));
						a.setAlertFullTitle(alert.getString("AlertFullTitle"));
						
						if (alert.getString("AlertFullText").equals("null")) {
							a.setAlertFullText("");
						}
						else {
							a.setAlertFullText(alert.getString("AlertFullText"));
						}
						
						i.setFerriesRouteAlertItem(a);
					}
					
					routeItems.add(i);
				}			

			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			
			return routeItems;
		}

		@Override
		public void deliverResult(ArrayList<FerriesRouteItem> data) {
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
		public void onCanceled(ArrayList<FerriesRouteItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
			// Ensure the loader is stopped
			onStopLoading();
		}

	}
	
	public class RouteItemAdapter extends ArrayAdapter<FerriesRouteItem> {
		private final LayoutInflater mInflater;
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        
        public RouteItemAdapter(Context context) {
	        super(context, R.layout.list_item_with_star);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<FerriesRouteItem> data) {
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
	        ViewHolder holder;
        	
        	if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item_with_star, null);
	            holder = new ViewHolder();
	            holder.title = (TextView) convertView.findViewById(R.id.title);
	            holder.title.setTypeface(tf);
	            holder.star_button = (CheckBox) convertView.findViewById(R.id.star_button);
            	holder.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
						int getPosition = (Integer) buttonView.getTag();
						routeItems.get(getPosition).setSelected(buttonView.isChecked());
					}
				});
            	convertView.setTag(holder);	            
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        FerriesRouteItem item = getItem(position);
        	holder.title.setText(item.getDescription());
        	holder.star_button.setTag(position);
        	holder.star_button.setChecked(routeItems.get(position).isSelected());
	        
        	return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView title;
		public CheckBox star_button;
	}
	
}
