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
import gov.wa.wsdot.android.wsdot.shared.TwitterItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TwitterFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<TwitterItem>> {
	
	private static final String DEBUG_TAG = "Twitter";
	private static ArrayList<TwitterItem> twitterItems = null;
	private static TwitterItemAdapter mAdapter;
	private static View mLoadingSpinner;
	private static String mScreenName;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mScreenName = getArguments().getString("account");
		} catch (Exception e) {
			
		}
	}
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
		setHasOptionsMenu(true);        
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/News & Social Media/Twitter");
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
		
		mAdapter = new TwitterItemAdapter(getActivity());
		setListAdapter(mAdapter);
		
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

	public Loader<ArrayList<TwitterItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new TwitterItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<TwitterItem>> loader, ArrayList<TwitterItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		mAdapter.setData(data);
	}

	public void onLoaderReset(Loader<ArrayList<TwitterItem>> loader) {
		mAdapter.setData(null);
	}	
	
	/**
	 * A custom Loader that loads all of the border wait times from the data server.
	 */	
	public static class TwitterItemsLoader extends AsyncTaskLoader<ArrayList<TwitterItem>> {

		public TwitterItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<TwitterItem> loadInBackground() {
	    	String patternStr = "(http://[A-Za-z0-9./]+)"; // Find bit.ly addresses
	    	Pattern pattern = Pattern.compile(patternStr);
	    	DateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	    	parseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	    	twitterItems = new ArrayList<TwitterItem>();
			TwitterItem i = null;
			URL url;
			
			try {
				if (mScreenName == null || mScreenName == "all") {
					url = new URL("http://www.wsdot.wa.gov/news/socialroom/posts/twitter");
				} else {
					url = new URL("http://www.wsdot.wa.gov/news/socialroom/posts/twitter/" + mScreenName);
				}
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONArray items = new JSONArray(jsonFile);
				String d;			
				
				for (int j=0; j < items.length(); j++) {
						JSONObject item = items.getJSONObject(j);
						i = new TwitterItem();
						d = item.getString("text");
	                	Matcher matcher = pattern.matcher(d);
	                	boolean matchFound = matcher.find();
	                	if (matchFound) {
	                		String textLink = matcher.group();
	                		String hyperLink = "<a href=\"" + textLink + "\">" + textLink + "</a>";
	                		d = matcher.replaceFirst(hyperLink);
	                	}
						i.setTitle(item.getString("text"));
						i.setDescription(d);
						
		            	try {
		            		Date date = parseDateFormat.parse(item.getString("created_at"));
		            		i.setPubDate(displayDateFormat.format(date));
		            	} catch (Exception e) {
		            		i.setPubDate("");
		            		Log.e(DEBUG_TAG, "Error parsing date", e);
		            	}
						
						twitterItems.add(i);
				}				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			
			return twitterItems;
		}

		@Override
		public void deliverResult(ArrayList<TwitterItem> data) {
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
			
			mAdapter.clear();
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
		public void onCanceled(ArrayList<TwitterItem> data) {
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
		
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), TwitterDetailsActivity.class);
		b.putString("description", twitterItems.get(position).getDescription());
		b.putString("link", twitterItems.get(position).getLink());
		b.putString("publishDate", twitterItems.get(position).getPubDate());
		intent.putExtras(b);

		startActivity(intent);
	}

	private class TwitterItemAdapter extends ArrayAdapter<TwitterItem> {
		private final LayoutInflater mInflater;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");		

        public TwitterItemAdapter(Context context) {
        	super(context, R.layout.simple_list_item);
        	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<TwitterItem> data) {
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
	        
	        TwitterItem item = getItem(position);
	        
	        if (item != null) {
                TextView tt = (TextView) convertView.findViewById(R.id.title);
                tt.setTypeface(tfb);
                TextView bt = (TextView) convertView.findViewById(R.id.description);
                bt.setTypeface(tf);
                
                if (tt != null) {
                	tt.setText(item.getTitle());
                }
                
                if(bt != null) {
	        		bt.setText(item.getPubDate());
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