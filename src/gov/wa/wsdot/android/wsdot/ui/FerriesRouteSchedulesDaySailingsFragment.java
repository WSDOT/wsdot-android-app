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
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;

import java.util.ArrayList;

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

public class FerriesRouteSchedulesDaySailingsFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<FerriesTerminalItem>> {
	
	private static final String DEBUG_TAG = "RouteSchedulesDaySailings";
	private static FerriesScheduleDateItem scheduleDateItems;
	private static ArrayList<FerriesTerminalItem> terminalItems;
	private static SailingsAdapter adapter;
	private static View mLoadingSpinner;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		scheduleDateItems = (FerriesScheduleDateItem)activity.getIntent().getSerializableExtra("scheduleDateItems");
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
		
        adapter = new SailingsAdapter(getActivity());
        setListAdapter(adapter);
        
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}


	public Loader<ArrayList<FerriesTerminalItem>> onCreateLoader(int id,
			Bundle args) {
		
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new TerminalLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<FerriesTerminalItem>> loader,
			ArrayList<FerriesTerminalItem> data) {
		
		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);		
	}

	public void onLoaderReset(Loader<ArrayList<FerriesTerminalItem>> loader) {
		adapter.setData(null);
	}
	
	public static class TerminalLoader extends AsyncTaskLoader<ArrayList<FerriesTerminalItem>> {

		public TerminalLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesTerminalItem> loadInBackground() {
	    	int numTerminals = scheduleDateItems.getFerriesTerminalItem().size();
	    	terminalItems = new ArrayList<FerriesTerminalItem>();
			
	    	try {   		
				for (int i=0; i<numTerminals; i++) {
					FerriesTerminalItem terminalItem = new FerriesTerminalItem();
					terminalItem.setArrivingTerminalID(scheduleDateItems.getFerriesTerminalItem().get(i).getArrivingTerminalID());
					terminalItem.setArrivingTerminalName(scheduleDateItems.getFerriesTerminalItem().get(i).getArrivingTerminalName());
					terminalItem.setDepartingTerminalID(scheduleDateItems.getFerriesTerminalItem().get(i).getDepartingTerminalID());
					terminalItem.setDepartingTerminalName(scheduleDateItems.getFerriesTerminalItem().get(i).getDepartingTerminalName());

					for (int j=0; j<scheduleDateItems.getFerriesTerminalItem().get(i).getAnnotations().size(); j++) {
						terminalItem.setAnnotations(scheduleDateItems.getFerriesTerminalItem().get(i).getAnnotations().get(j));
					}					
					
					for (int k=0; k<scheduleDateItems.getFerriesTerminalItem().get(i).getScheduleTimes().size(); k++) {
						terminalItem.setScheduleTimes(scheduleDateItems.getFerriesTerminalItem().get(i).getScheduleTimes().get(k));
					}				
					
					terminalItems.add(terminalItem);
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error adding terminal info", e);
			}
		
	    	return terminalItems;		
		}

		@Override
		public void deliverResult(ArrayList<FerriesTerminalItem> data) {
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
		public void onCanceled(ArrayList<FerriesTerminalItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
			// Ensure the loader is stopped
			onStopLoading();
		}
		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String terminalNames = terminalItems.get(position).getDepartingTerminalName() + " to " +
				terminalItems.get(position).getArrivingTerminalName();
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDayDeparturesActivity.class);
		b.putString("terminalNames", terminalNames);
		b.putSerializable("terminalItems", terminalItems.get(position));
		intent.putExtras(b);
		startActivity(intent);		
	}
    
	private class SailingsAdapter extends ArrayAdapter<FerriesTerminalItem> {
		private final LayoutInflater mInflater;
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

        public SailingsAdapter(Context context) {
	        super(context, R.layout.list_item);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<FerriesTerminalItem> data) {
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
	        
	        FerriesTerminalItem item = getItem(position);
	        
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tf);
            	tt.setText(item.getDepartingTerminalName() + " to " + item.getArrivingTerminalName());
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
	}	
	
}
