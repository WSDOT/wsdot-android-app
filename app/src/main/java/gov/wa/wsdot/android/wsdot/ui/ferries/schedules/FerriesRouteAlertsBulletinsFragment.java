/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteAlertItem;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class FerriesRouteAlertsBulletinsFragment extends BaseListFragment implements
        LoaderCallbacks<ArrayList<FerriesRouteAlertItem>> {

	private static final String TAG = FerriesRouteAlertsBulletinsFragment.class.getName();
	private static ArrayList<FerriesRouteAlertItem> routeAlertItems;
	private static RouteAlertItemAdapter adapter;
	private static View mLoadingSpinner;
	private static String mAlerts;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

        if (context instanceof Activity) {
            Bundle args = ((Activity)context).getIntent().getExtras();
            mAlerts = args.getString("alert");
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
		setRetainInstance(true);
		setHasOptionsMenu(true);	
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);

        disableAds(root);
        
        return root;
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        adapter = new RouteAlertItemAdapter(getActivity());
        setListAdapter(adapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), FerriesRouteAlertsBulletinDetailsActivity.class);
		b.putString("AlertFullTitle", routeAlertItems.get(position).getAlertFullTitle());
		b.putString("AlertPublishDate", routeAlertItems.get(position).getPublishDate());
		b.putString("AlertDescription", routeAlertItems.get(position).getAlertDescription());
		b.putString("AlertFullText", routeAlertItems.get(position).getAlertFullText());
		intent.putExtras(b);
		startActivity(intent);		
	}
	
	public Loader<ArrayList<FerriesRouteAlertItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new RouteAlertsBulletinsLoader(getActivity());
	}
	public void onLoadFinished(Loader<ArrayList<FerriesRouteAlertItem>> loader,
			ArrayList<FerriesRouteAlertItem> data) {

		mLoadingSpinner.setVisibility(View.GONE);
		adapter.setData(data);		
	}
	
	public void onLoaderReset(Loader<ArrayList<FerriesRouteAlertItem>> loader) {
		adapter.setData(null);
	}

	/**
	 * A custom Loader that loads all of the WSF route alert bulletins for selected route.
	 */	
	public static class RouteAlertsBulletinsLoader extends AsyncTaskLoader<ArrayList<FerriesRouteAlertItem>> {

		public RouteAlertsBulletinsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesRouteAlertItem> loadInBackground() {
	        routeAlertItems = new ArrayList<FerriesRouteAlertItem>();
			
	        try {
				JSONArray alerts = new JSONArray(mAlerts);
				int numAlerts = alerts.length();
				for (int j=0; j < numAlerts; j++)	{
					JSONObject alert = alerts.getJSONObject(j);
					FerriesRouteAlertItem i = new FerriesRouteAlertItem();
					i.setAlertFullTitle(alert.getString("AlertFullTitle"));
					i.setPublishDate(alert.getString("PublishDate").substring(6, 19));
					i.setAlertDescription(alert.getString("AlertDescription"));
					i.setAlertFullText(alert.getString("AlertFullText"));
					routeAlertItems.add(i);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return routeAlertItems;
		}

		@Override
		public void deliverResult(ArrayList<FerriesRouteAlertItem> data) {
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
		public void onCanceled(ArrayList<FerriesRouteAlertItem> data) {
			super.onCanceled(data);
		}		
		
		@Override
		protected void onReset() {
			super.onReset();
			
			// Ensure the loader is stopped
			onStopLoading();
		}
		
	}
	
	private class RouteAlertItemAdapter extends ArrayAdapter<FerriesRouteAlertItem> {
		private final LayoutInflater mInflater;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        //DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        
        public RouteAlertItemAdapter(Context context) {
	        super(context, R.layout.simple_list_item);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<FerriesRouteAlertItem> data) {
            clear();
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
	        ViewHolder holder = null;
        	
        	if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.simple_list_item, null);
	            holder = new ViewHolder();
	            holder.title = (TextView) convertView.findViewById(R.id.title);
	            holder.title.setTypeface(tfb);
	            holder.description = (TextView) convertView.findViewById(R.id.description);
	            holder.description.setTypeface(tf);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        FerriesRouteAlertItem item = getItem(position);
	        
        	holder.title.setText(item.getAlertFullTitle());
            
        	try {
        		Date date = new Date(Long.parseLong(item.getPublishDate()));
        		holder.description.setText(ParserUtils.relativeTime(date));
        	} catch (Exception e) {
        		Log.e(TAG, "Error parsing date", e);
        	}	            	

	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView description;
	}
	
}
