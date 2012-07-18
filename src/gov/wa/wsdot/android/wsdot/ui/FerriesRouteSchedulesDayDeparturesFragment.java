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
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
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
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class FerriesRouteSchedulesDayDeparturesFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<FerriesScheduleTimesItem>> {

	private static final String DEBUG_TAG = "RouteSchedulesDayDepartures";
	private static FerriesTerminalItem terminalItem;
	private static ArrayList<FerriesAnnotationsItem> annotations;
	private static ArrayList<FerriesScheduleTimesItem> times;
	private static DepartureTimesAdapter adapter;
	private static View mLoadingSpinner;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		terminalItem = (FerriesTerminalItem)activity.getIntent().getSerializableExtra("terminalItems");
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
		
        adapter = new DepartureTimesAdapter(getActivity());
        setListAdapter(adapter);
        
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}

	public Loader<ArrayList<FerriesScheduleTimesItem>> onCreateLoader(int id,
			Bundle args) {
		
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new DepartureTimesLoader(getActivity());
	}

	public void onLoadFinished(
			Loader<ArrayList<FerriesScheduleTimesItem>> loader,
			ArrayList<FerriesScheduleTimesItem> data) {
		
		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);
	}

	public void onLoaderReset(Loader<ArrayList<FerriesScheduleTimesItem>> loader) {
		adapter.setData(null);
	}

	public static class DepartureTimesLoader extends AsyncTaskLoader<ArrayList<FerriesScheduleTimesItem>> {

		public DepartureTimesLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesScheduleTimesItem> loadInBackground() {
			int numAnnotations = terminalItem.getAnnotations().size();
	    	int numTimes = terminalItem.getScheduleTimes().size();
	    	annotations = new ArrayList<FerriesAnnotationsItem>();
	    	times = new ArrayList<FerriesScheduleTimesItem>();
			
	    	try {
	    		for (int i=0; i<numAnnotations; i++) {
	    			FerriesAnnotationsItem annotationItem = new FerriesAnnotationsItem();
	    			annotationItem.setAnnotation(terminalItem.getAnnotations().get(i).getAnnotation());
	    			annotations.add(annotationItem);
	    		}
	    		
				for (int i=0; i<numTimes; i++) {
					FerriesScheduleTimesItem timesItem = new FerriesScheduleTimesItem();
					timesItem.setDepartingTime(terminalItem.getScheduleTimes().get(i).getDepartingTime());
					
					for (int j=0; j<terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().size(); j++) {
						FerriesAnnotationIndexesItem index = new FerriesAnnotationIndexesItem();
						index.setIndex(terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().get(j).getIndex());
						timesItem.setAnnotationIndexes(index);
					}
					
					times.add(timesItem);
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error adding terminal departure times", e);
			}
	    	
			return times;
		}

		@Override
		public void deliverResult(ArrayList<FerriesScheduleTimesItem> data) {
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
		public void onCanceled(ArrayList<FerriesScheduleTimesItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
			// Ensure the loader is stopped
			onStopLoading();
		}
		
	}
	
	private class DepartureTimesAdapter extends ArrayAdapter<FerriesScheduleTimesItem> {
		private final LayoutInflater mInflater;
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

        public DepartureTimesAdapter(Context context) {
	        super(context, R.layout.simple_list_item);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }        
        
        public void setData(ArrayList<FerriesScheduleTimesItem> data) {
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
	        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
	        
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.simple_list_item, null);
	        }
	        
	        FerriesScheduleTimesItem item = getItem(position);
	        String annotation = "";

	        for (int i=0; i < item.getAnnotationIndexes().size(); i++) {
	        	FerriesAnnotationsItem p = annotations.get(item.getAnnotationIndexes().get(i).getIndex());
	        	annotation += p.getAnnotation();
	        }
	        
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tf);
            	tt.setText(dateFormat.format(new Date(Long.parseLong(item.getDepartingTime()))));
	            
	            TextView bt = (TextView) convertView.findViewById(R.id.description);
	            bt.setTypeface(tf);
           		bt.setText(annotation);
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
		public TextView bt;
	}	
	
}
