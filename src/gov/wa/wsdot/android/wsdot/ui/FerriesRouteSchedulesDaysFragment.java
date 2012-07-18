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
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
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

public class FerriesRouteSchedulesDaysFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<FerriesScheduleDateItem>> {
	
	private static final String DEBUG_TAG = "RouteSchedulesDays";
	private static FerriesRouteItem routeItems;
	private static ArrayList<FerriesScheduleDateItem> scheduleDateItems;
	private static DaysOfWeekAdapter adapter;
	private static View mLoadingSpinner;	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		routeItems = (FerriesRouteItem)activity.getIntent().getSerializableExtra("routeItems");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
		setRetainInstance(true);
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
	
        adapter = new DaysOfWeekAdapter(getActivity());
        setListAdapter(adapter);
        
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		DateFormat dateFormat = new SimpleDateFormat("EEEE");
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDaySailingsActivity.class);
		b.putString("dayOfWeek", dateFormat.format(new Date(Long.parseLong(scheduleDateItems.get(position).getDate()))));
		b.putSerializable("scheduleDateItems", scheduleDateItems.get(position));
		intent.putExtras(b);
		startActivity(intent);		
	}
	
	public Loader<ArrayList<FerriesScheduleDateItem>> onCreateLoader(int id,
			Bundle args) {
		
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new ScheduleDatesLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<FerriesScheduleDateItem>> loader,
			ArrayList<FerriesScheduleDateItem> data) {
		
		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);
	}

	public void onLoaderReset(Loader<ArrayList<FerriesScheduleDateItem>> loader) {
		adapter.setData(null);
	}

	public static class ScheduleDatesLoader extends AsyncTaskLoader<ArrayList<FerriesScheduleDateItem>> {

		public ScheduleDatesLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesScheduleDateItem> loadInBackground() {
	    	int numDates = routeItems.getFerriesScheduleDateItem().size();
	    	scheduleDateItems = new ArrayList<FerriesScheduleDateItem>();
			
	    	try {   		
				for (int i=0; i<numDates; i++) {
					FerriesScheduleDateItem scheduleDateItem = new FerriesScheduleDateItem();
					scheduleDateItem.setDate(routeItems.getFerriesScheduleDateItem().get(i).getDate());
					for (int j=0; j<routeItems.getFerriesScheduleDateItem().get(i).getFerriesTerminalItem().size(); j++) {
						scheduleDateItem.setFerriesTerminalItem(routeItems.getFerriesScheduleDateItem().get(i).getFerriesTerminalItem().get(j));
					}
					scheduleDateItems.add(scheduleDateItem);
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error adding dates", e);
			}
	    	
			return scheduleDateItems;

		}

		@Override
		public void deliverResult(ArrayList<FerriesScheduleDateItem> data) {
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
		public void onCanceled(ArrayList<FerriesScheduleDateItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
			// Ensure the loader is stopped
			onStopLoading();
		}
		
	}
	
	private class DaysOfWeekAdapter extends ArrayAdapter<FerriesScheduleDateItem> {
		private final LayoutInflater mInflater;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
    	DateFormat dateFormat = new SimpleDateFormat("EEEE");
    	DateFormat subTitleDateFormat = new SimpleDateFormat("MMMM d");

        public DaysOfWeekAdapter(Context context) {
	        super(context, R.layout.simple_list_item);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<FerriesScheduleDateItem> data) {
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
	            convertView = mInflater.inflate(R.layout.simple_list_item, null);
	        }
	        
	        FerriesScheduleDateItem item = getItem(position);
	        
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tf);
	            TextView bt = (TextView) convertView.findViewById(R.id.description);
	            bt.setTypeface(tf);
	            
	            tt.setText(dateFormat.format(new Date(Long.parseLong(item.getDate()))));
	            
            	try {
            		Date date = new Date(Long.parseLong(item.getDate()));
            		bt.setText(subTitleDateFormat.format(date));
            	} catch (Exception e) {
            		Log.e(DEBUG_TAG, "Error parsing date", e);
            	}
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
		public TextView bt;
	}
	
}
