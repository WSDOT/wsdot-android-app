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
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
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
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FerriesRouteSchedulesFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<FerriesRouteItem>> {

	private static final String DEBUG_TAG = "RouteSchedules";
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
        
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Ferries/Route Schedules");
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
	
	public Loader<ArrayList<FerriesRouteItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new RouteSchedulesLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<FerriesRouteItem>> loader, ArrayList<FerriesRouteItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);
		
	}

	public void onLoaderReset(Loader<ArrayList<FerriesRouteItem>> loader) {
		adapter.setData(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDaysActivity.class);
		b.putString("description", routeItems.get(position).getDescription());
		b.putSerializable("routeItems", routeItems.get(position));
		intent.putExtras(b);
		startActivity(intent);
	}

	/**
	 * A custom Loader that loads all of the WSF route schedules from the data server.
	 */		
	public static class RouteSchedulesLoader extends AsyncTaskLoader<ArrayList<FerriesRouteItem>> {

		public RouteSchedulesLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesRouteItem> loadInBackground() {
			routeItems = new ArrayList<FerriesRouteItem>();
			FerriesRouteItem route = null;
			FerriesScheduleDateItem scheduleDate = null;
			FerriesTerminalItem terminal = null;
			FerriesAnnotationsItem notes = null;
			FerriesScheduleTimesItem timesItem = null;
			FerriesAnnotationIndexesItem indexesItem = null;
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/WSFRouteSchedules.js.gz");
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
				
				for (int i=0; i < items.length(); i++) {
					JSONObject item = items.getJSONObject(i);
					route = new FerriesRouteItem();
					route.setRouteID(item.getInt("RouteID"));
					route.setDescription(item.getString("Description"));
					
					JSONArray dates = item.getJSONArray("Date");
					for (int j=0; j < dates.length(); j++) {
						JSONObject date = dates.getJSONObject(j);
						scheduleDate = new FerriesScheduleDateItem();
						scheduleDate.setDate(date.getString("Date").substring(6, 19));
						
						JSONArray sailings = date.getJSONArray("Sailings");
						for (int k=0; k < sailings.length(); k++) {
							JSONObject sailing = sailings.getJSONObject(k);
							terminal = new FerriesTerminalItem();
							terminal.setArrivingTerminalID(sailing.getInt("ArrivingTerminalID"));
							terminal.setArrivingTerminalName(sailing.getString("ArrivingTerminalName"));
							terminal.setDepartingTerminalID(sailing.getInt("DepartingTerminalID"));
							terminal.setDepartingTerminalName(sailing.getString("DepartingTerminalName"));
							
							JSONArray annotations = sailing.getJSONArray("Annotations");
							for (int l=0; l < annotations.length(); l++) {
								notes = new FerriesAnnotationsItem();
								notes.setAnnotation(annotations.getString(l));
								terminal.setAnnotations(notes);	
							}
							
							JSONArray times = sailing.getJSONArray("Times");
							for (int m=0; m < times.length(); m++) {
								JSONObject time = times.getJSONObject(m);
								timesItem = new FerriesScheduleTimesItem();
								timesItem.setDepartingTime(time.getString("DepartingTime").substring(6, 19));
								
								
								JSONArray annotationIndexes = time.getJSONArray("AnnotationIndexes");
								for (int n=0; n < annotationIndexes.length(); n++) {
									indexesItem = new FerriesAnnotationIndexesItem();
									indexesItem.setIndex(annotationIndexes.getInt(n));
									timesItem.setAnnotationIndexes(indexesItem);									
								}
								terminal.setScheduleTimes(timesItem);
							}
							scheduleDate.setFerriesTerminalItem(terminal);
						}
						route.setFerriesScheduleDateItem(scheduleDate);
					}
					routeItems.add(route);
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
	        super(context, R.layout.list_item);
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
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item, null);
	        }
	        
	        FerriesRouteItem item = getItem(position);
	        
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tf);
            	tt.setText(item.getDescription());
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
	}	
	
}
