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

import gov.wa.wsdot.android.wsdot.HighwayAlertItemDetails;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

public class HighImpactAlertsFragment extends SherlockFragment {

	private ArrayList<HighwayAlertsItem> alertItems = null;
	private ViewGroup mRootView;
    private ViewPagerAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
	private View mLoadingSpinner;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }    
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_high_impact_alerts, container);
        mLoadingSpinner = mRootView.findViewById(R.id.loading_spinner);
        mPager = (ViewPager)mRootView.findViewById(R.id.pager);
        mIndicator = (UnderlinePageIndicator)mRootView.findViewById(R.id.indicator);
        
        return mRootView;
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		new GetWhatsHappeningItems().execute();
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
			alertItems.clear();
			new GetWhatsHappeningItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	private class GetWhatsHappeningItems extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}		

	    protected void onCancelled() {
	        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
	    }		
		
		@Override
		protected String doInBackground(String... arg0) {
			alertItems = new ArrayList<HighwayAlertsItem>();
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
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);

						if (item.getString("Priority").equalsIgnoreCase("highest")) {
							i = new HighwayAlertsItem();
							i.setEventCategory(item.getString("EventCategory"));
							i.setExtendedDescription(item.getString("HeadlineDescription"));
							
							alertItems.add(i);
						}
					} else {
						break;
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
			
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			mLoadingSpinner.setVisibility(View.GONE);
			mAdapter = new ViewPagerAdapter(getActivity(), alertItems);
			mPager.setAdapter(mAdapter);
			mIndicator.setViewPager(mPager);			
		}
	}
	
	public class ViewPagerAdapter extends PagerAdapter {
        private ArrayList<HighwayAlertsItem> items;

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
						Intent intent = new Intent(getActivity(), HighwayAlertItemDetails.class);
						b.putString("title", items.get(position).getEventCategory());
						b.putString("description", items.get(position).getExtendedDescription());
						intent.putExtras(b);
						startActivity(intent);				
					}
		    	});
		    	TextView title = (TextView)view.findViewById(R.id.title_alert);
		    	title.setText(items.get(position).getExtendedDescription());
	    	} else {
	    		view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_inactive, null);
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
	
}
