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

package gov.wa.wsdot.android.wsdot.ui.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;
import gov.wa.wsdot.android.wsdot.service.HighwayAlertsSyncService;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.alert.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

public class HighImpactAlertsFragment extends BaseFragment implements
        LoaderCallbacks<Cursor> {

	private ViewGroup mRootView;
    private ViewPagerAdapter mAdapter;
    private static ViewPager mPager;
    private static LinePageIndicator mIndicator;
	private static View mLoadingSpinner;
	private HighwayAlertsSyncReceiver mHighwayAlertsSyncReceiver;
	private ArrayList<HighwayAlertsItem> alertItems = new ArrayList<HighwayAlertsItem>();
	private Handler mHandler = new Handler();
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }    
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
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
	public void onResume() {
		super.onResume();
		
        IntentFilter alertsFilter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.HIGHWAY_ALERTS_RESPONSE");
        alertsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mHighwayAlertsSyncReceiver = new HighwayAlertsSyncReceiver();
        getActivity().registerReceiver(mHighwayAlertsSyncReceiver, alertsFilter);
		
        mHandler.post(runnable);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		mHandler.removeCallbacks(runnable);
		getActivity().unregisterReceiver(mHighwayAlertsSyncReceiver);
	}
	
    private Runnable runnable = new Runnable() {
        public void run() {
        	Intent intent = new Intent(getActivity(), HighwayAlertsSyncService.class);
        	getActivity().startService(intent);
        	mHandler.postDelayed(runnable, (1 * DateUtils.MINUTE_IN_MILLIS)); // Check every minute.
        }
    };
    
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
		String[] projection = {
	            HighwayAlerts.HIGHWAY_ALERT_ID,
				HighwayAlerts.HIGHWAY_ALERT_LATITUDE,
				HighwayAlerts.HIGHWAY_ALERT_LONGITUDE,
				HighwayAlerts.HIGHWAY_ALERT_CATEGORY,
				HighwayAlerts.HIGHWAY_ALERT_HEADLINE,
				HighwayAlerts.HIGHWAY_ALERT_PRIORITY,
				HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED
				};

		// We are only displaying the highest impact alerts on the dashboard.
		CursorLoader cursorLoader = new HighImpactAlertsLoader(getActivity(),
				HighwayAlerts.CONTENT_URI,
				projection,
				HighwayAlerts.HIGHWAY_ALERT_PRIORITY + " LIKE ?",
				new String[] {"Highest"},
				null
				);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		alertItems.clear();
		
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				HighwayAlertsItem item = new HighwayAlertsItem();
                item.setAlertId(cursor.getString(0));
                item.setStartLatitude(cursor.getDouble(1));
                item.setStartLongitude(cursor.getDouble(2));
                item.setEventCategory(cursor.getString(3));
				item.setExtendedDescription(cursor.getString(4));
				item.setLastUpdatedTime(cursor.getString(6));
				alertItems.add(item);

				cursor.moveToNext();
			}

		} else {
			HighwayAlertsItem item = new HighwayAlertsItem();
			item.setEventCategory("empty");
			alertItems.add(item);
		}
			
		mLoadingSpinner.setVisibility(View.GONE);
		mPager.setVisibility(View.VISIBLE);
		mIndicator.setVisibility(View.VISIBLE);
		mAdapter = new ViewPagerAdapter(getActivity(), alertItems);
		mPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mPager);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
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

	    	String category = items.get(position).getEventCategory();
	    	
	    	if (category.equalsIgnoreCase("empty")) {
	    		view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_inactive, null);
	    		mIndicator.setVisibility(View.GONE);
	    	} else if (category.equalsIgnoreCase("error")) {
	    		view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_inactive, null);
	    		mIndicator.setVisibility(View.GONE);
    			TextView title = (TextView)view.findViewById(R.id.title_alert);
		    	title.setTypeface(tf);
		    	title.setText(items.get(position).getExtendedDescription());
	    	} else {
		    	view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_active, null);
		    	view.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Bundle b = new Bundle();
						Intent intent = new Intent(getActivity(), HighwayAlertDetailsActivity.class);
						b.putString("id", items.get(position).getAlertId());
						intent.putExtras(b);
						startActivity(intent);				
					}
		    	});

		    	TextView title = (TextView)view.findViewById(R.id.title_alert);
		    	title.setTypeface(tf);
		    	title.setText(items.get(position).getExtendedDescription());

		    	if (getCount() < 2) mIndicator.setVisibility(View.GONE);
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
	
	public static class HighImpactAlertsLoader extends CursorLoader {
		public HighImpactAlertsLoader(Context context, Uri uri,
				String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
			super(context, uri, projection, selection, selectionArgs, sortOrder);
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();

			mLoadingSpinner.setVisibility(View.VISIBLE);
			mPager.setVisibility(View.GONE);
			mIndicator.setVisibility(View.GONE);
			forceLoad();
		}
	}
	
	public class HighwayAlertsSyncReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			
			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(0, null, HighImpactAlertsFragment.this); // We've got alerts, now add them.
				} else if (responseString.equals("NOP")) {
					// Nothing to do.
				} else {
					Log.e("HighwayAlertsSyncReceiver", responseString);
					mLoadingSpinner.setVisibility(View.GONE);
					
					alertItems.clear();
					HighwayAlertsItem item = new HighwayAlertsItem();
					item.setEventCategory("error");
					
					if (!UIUtils.isNetworkAvailable(context)) {
						responseString = getString(R.string.no_connection);
					}
					
					item.setExtendedDescription(responseString);
					alertItems.add(item);
	
					mPager.setVisibility(View.VISIBLE);
					mIndicator.setVisibility(View.VISIBLE);
					mAdapter = new ViewPagerAdapter(getActivity(), alertItems);
					mPager.setAdapter(mAdapter);
					mIndicator.setViewPager(mPager);		
				}
			}
		}
	}

}
