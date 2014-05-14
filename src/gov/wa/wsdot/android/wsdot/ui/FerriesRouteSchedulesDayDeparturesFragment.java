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
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FerriesRouteSchedulesDayDeparturesFragment extends ListFragment
	implements LoaderCallbacks<ArrayList<FerriesScheduleTimesItem>> {

	private static final String TAG = FerriesRouteSchedulesDayDeparturesFragment.class.getName();
	private static FerriesTerminalItem terminalItem;
	private static ArrayList<FerriesAnnotationsItem> annotations;
	private static ArrayList<FerriesScheduleTimesItem> times;
	private static DepartureTimesAdapter adapter;
	private static View mLoadingSpinner;
	private View mHeaderView;
	private Typeface tf;
	private Typeface tfb;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		terminalItem = (FerriesTerminalItem) getArguments().getSerializable("terminalItems");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        
        mHeaderView = inflater.inflate(R.layout.list_item_departure_times_header, null);
        TextView departing_title = (TextView) mHeaderView.findViewById(R.id.departing_title);
        departing_title.setTypeface(tfb);
        TextView arriving_title = (TextView) mHeaderView.findViewById(R.id.arriving_title);
        arriving_title.setTypeface(tfb);

        return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		setListAdapter(null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		
		if (adapter == null) {
			adapter = new DepartureTimesAdapter(getActivity());
		}
		this.getListView().addHeaderView(mHeaderView);
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
	    		for (int i=0; i < numAnnotations; i++) {
	    			FerriesAnnotationsItem annotationItem = new FerriesAnnotationsItem();
	    			annotationItem.setAnnotation(terminalItem.getAnnotations().get(i).getAnnotation());
	    			annotations.add(annotationItem);
	    		}
	    		
				for (int i=0; i < numTimes; i++) {
					FerriesScheduleTimesItem timesItem = new FerriesScheduleTimesItem();
					timesItem.setDepartingTime(terminalItem.getScheduleTimes().get(i).getDepartingTime());
					timesItem.setArrivingTime(terminalItem.getScheduleTimes().get(i).getArrivingTime());
					
					int numIndexes = terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().size();
					for (int j=0; j < numIndexes; j++) {
						FerriesAnnotationIndexesItem index = new FerriesAnnotationIndexesItem();
						index.setIndex(terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().get(j).getIndex());
						timesItem.setAnnotationIndexes(index);
					}
					
					times.add(timesItem);
				}
			} catch (Exception e) {
				Log.e(TAG, "Error adding terminal departure times", e);
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

        public DepartureTimesAdapter(Context context) {
	        super(context, R.layout.list_item_departure_times);
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
        	if (data != null) {
                //addAll(data); // Only in API level 11
                notifyDataSetChanged();
                int size = data.size();
                for (int i=0; i < size; i++) {
                	add(data.get(i));
                }
                notifyDataSetChanged();                
            }
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
	        ViewHolder holder;
	        
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item_departure_times, null);
	            holder = new ViewHolder();
	            holder.departing = (TextView) convertView.findViewById(R.id.departing);
	            holder.departing.setTypeface(tfb);
	            holder.arriving = (TextView) convertView.findViewById(R.id.arriving);
	            holder.arriving.setTypeface(tfb);
	            holder.annotation = (TextView) convertView.findViewById(R.id.annotation);
	            holder.annotation.setTypeface(tf);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        FerriesScheduleTimesItem item = getItem(position);
	        String annotation = "";

	        int numIndexes = item.getAnnotationIndexes().size();
	        for (int i=0; i < numIndexes; i++) {
	        	FerriesAnnotationsItem p = annotations.get(item.getAnnotationIndexes().get(i).getIndex());
	        	annotation += p.getAnnotation();
	        }
	        
        	holder.departing.setText(dateFormat.format(new Date(Long.parseLong(item.getDepartingTime()))));
        	
        	if (!item.getArrivingTime().equals("N/A")) {
        		holder.arriving.setText(dateFormat.format(new Date(Long.parseLong(item.getArrivingTime()))));
        	}
        	
       		holder.annotation.setText(android.text.Html.fromHtml(annotation));
	        
	        return convertView;
        }
        
    	private class ViewHolder {
    		public TextView departing;
    		public TextView arriving;
    		public TextView annotation;
    	}
	}
	
}
