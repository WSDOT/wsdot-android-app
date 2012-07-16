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
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.LinePageIndicator;

public class HighImpactAlertsFragment extends SherlockFragment
	implements LoaderCallbacks<ArrayList<HighwayAlertsItem>> {

	private ViewGroup mRootView;
    private ViewPagerAdapter mAdapter;
    private static ViewPager mPager;
    private static LinePageIndicator mIndicator;
	private static View mLoadingSpinner;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_high_impact_alerts, container);
        mLoadingSpinner = mRootView.findViewById(R.id.loading_spinner);
        mPager = (ViewPager)mRootView.findViewById(R.id.pager);
        mIndicator = (LinePageIndicator)mRootView.findViewById(R.id.indicator);
        
        return mRootView;
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

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
	
	public Loader<ArrayList<HighwayAlertsItem>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple.
		return new HighImpactAlertsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<HighwayAlertsItem>> loader, ArrayList<HighwayAlertsItem> alertItems) {
		mLoadingSpinner.setVisibility(View.GONE);
		mPager.setVisibility(View.VISIBLE);
		mIndicator.setVisibility(View.VISIBLE);
		mAdapter = new ViewPagerAdapter(getActivity(), alertItems);
		mPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mPager);				
	}

	public void onLoaderReset(Loader<ArrayList<HighwayAlertsItem>> loader) {
		mAdapter.items = null;
	}	
	
	public class ViewPagerAdapter extends PagerAdapter {
        private ArrayList<HighwayAlertsItem> items;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        @SuppressWarnings("unused")
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");        

        @SuppressWarnings("unused")
		private final Context context;
	 
	    public ViewPagerAdapter(Context context, ArrayList<HighwayAlertsItem> items) {
	        this.context = context;
	        this.items = items;
	    }
	  
	    @Override
	    public int getCount() {
	        return items.size();
	    }
	 
	    @Override
	    public Object instantiateItem(View pager, final int position) {
	    	View view;
	    	
	    	if (!items.get(position).getExtendedDescription().equalsIgnoreCase("error")) {
		    	view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_active, null);
		    	view.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Bundle b = new Bundle();
						Intent intent = new Intent(getActivity(), HighwayAlertDetailsActivity.class);
						b.putString("title", items.get(position).getEventCategory());
						b.putString("description", items.get(position).getExtendedDescription());
						intent.putExtras(b);
						startActivity(intent);				
					}
		    	});
		    	
		    	TextView title = (TextView)view.findViewById(R.id.title_alert);
		    	title.setTypeface(tf);
		    	title.setText(items.get(position).getExtendedDescription());
		    	
		    	if (getCount() < 2) mIndicator.setVisibility(View.GONE);
		    	
	    	} else {
	    		view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_inactive, null);
	    		mIndicator.setVisibility(View.GONE);
	    	}
	    	
	        ((ViewPager)pager).addView(view, 0);
	        
	        return view;
	    }
	 
	    @Override
	    public void destroyItem(View pager, int position, Object view) {
	        ((ViewPager)pager).removeView( (TextView)view );
	    }
	 
	    @Override
	    public boolean isViewFromObject(View view, Object object) {
	        return view.equals( object );
	    }
	 
	    @Override
	    public void finishUpdate(View view) {
	    }
	 
	    @Override
	    public void restoreState(Parcelable p, ClassLoader c) {
	    }
	 
	    @Override
	    public Parcelable saveState() {
	        return null;
	    }
	 
	    @Override
	    public void startUpdate(View view) {
	    }
	}

	/**
	 * A custom Loader that loads all of the highest impact highway alerts from the data server.
	 */	
	public static class HighImpactAlertsLoader extends AsyncTaskLoader<ArrayList<HighwayAlertsItem>> {
		
		public HighImpactAlertsLoader(Context context) {
				super(context);
		}

		@Override
		public ArrayList<HighwayAlertsItem> loadInBackground() {
			ArrayList<HighwayAlertsItem> alertItems = new ArrayList<HighwayAlertsItem>();
			HighwayAlertsItem i = null;
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/HighwayAlerts.js.gz");
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
				JSONObject result = obj.getJSONObject("alerts");
				JSONArray items = result.getJSONArray("items");
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);

					if (item.getString("Priority").equalsIgnoreCase("highest")) {
						i = new HighwayAlertsItem();
						i.setEventCategory(item.getString("EventCategory"));
						i.setExtendedDescription(item.getString("HeadlineDescription"));
						
						alertItems.add(i);
					}
				}
				
			} catch (Exception e) {
				// Likely a network error accessing data file.
			} finally {
				if (alertItems.isEmpty()) {
					i = new HighwayAlertsItem();
					i.setExtendedDescription("error");
					alertItems.add(i);					
				}
			}
			
			return alertItems;
		}
		
	    /**
	     * Called when there is new data to deliver to the client. The
	     * super class will take care of delivering it; the implementation
	     * here just adds a little more logic.
	     */		
		@Override
		public void deliverResult(ArrayList<HighwayAlertsItem> alertItems) {
			super.deliverResult(alertItems);				
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			
			mLoadingSpinner.setVisibility(View.VISIBLE);
			mPager.setVisibility(View.GONE);
			mIndicator.setVisibility(View.GONE);
			forceLoad();
		}		
		
		@Override
		protected void onStopLoading() {
	        // Attempt to cancel the current load task if possible.
	        cancelLoad();
		}
		
		@Override
		public void onCanceled(ArrayList<HighwayAlertsItem> alertItems) {
			super.onCanceled(alertItems);
		}

	    @Override
	    protected void onReset() {
	        super.onReset();

	        // Ensure the loader is stopped
	        onStopLoading();
	    }
		
	}
}
