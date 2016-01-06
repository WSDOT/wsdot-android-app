/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.socialmedia.news;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.NewsItem;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;

public class NewsFragment extends BaseListFragment implements
        LoaderCallbacks<ArrayList<NewsItem>>,
        SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = NewsFragment.class.getSimpleName();
	private static ArrayList<NewsItem> newsItems = null;	
	private static NewsItemAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

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

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_swipe_refresh, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
				R.color.holo_blue_bright,
				R.color.holo_green_light,
				R.color.holo_orange_light,
				R.color.holo_red_light);
        
        mEmptyView = root.findViewById( R.id.empty_list_view );
        
        disableAds(root);
        
        return root;
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new NewsItemAdapter(getActivity());
		setListAdapter(mAdapter);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
	}
	
	/*
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
	*/
	
	public Loader<ArrayList<NewsItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new NewsItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<NewsItem>> loader, ArrayList<NewsItem> data) {

		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
			getListView().setEmptyView(mEmptyView);
		}
		
		swipeRefreshLayout.setRefreshing(false);
	}

	public void onLoaderReset(Loader<ArrayList<NewsItem>> loader) {
	    mAdapter.setData(null);
	    swipeRefreshLayout.setRefreshing(false);
	}	

	/**
	 * A custom Loader that loads all of the news items from the data server.
	 */	
	public static class NewsItemsLoader extends AsyncTaskLoader<ArrayList<NewsItem>> {

		public NewsItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<NewsItem> loadInBackground() {
			DateFormat parseDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
			DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);
	    	newsItems = new ArrayList<NewsItem>();
			NewsItem i = null;
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/News.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("news");
				JSONArray items = result.getJSONArray("items");
				newsItems = new ArrayList<NewsItem>();
				
				int numItems = items.length();
				for (int j=0; j < numItems; j++) {
					JSONObject item = items.getJSONObject(j);
					i = new NewsItem();
					i.setTitle(item.getString("title"));
					i.setDescription(item.getString("description"));
					i.setLink(item.getString("link"));
					
	            	try {
	            		Date date = parseDateFormat.parse(item.getString("pubdate"));
	            		i.setPubDate(displayDateFormat.format(date));
	            	} catch (Exception e) {
	            		i.setPubDate("");
	            		Log.e(TAG, "Error parsing date", e);
	            	}				
					
					newsItems.add(i);
				}			

			} catch (Exception e) {
				Log.e(TAG, "Error in network call", e);
			}
			return newsItems;
		}

		@Override
		public void deliverResult(ArrayList<NewsItem> data) {
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
			swipeRefreshLayout.post(new Runnable() {
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
				}
			});
			forceLoad();
		}

		@Override
		protected void onStopLoading() {
			super.onStopLoading();
			
	        // Attempt to cancel the current load task if possible.
	        cancelLoad();
		}
		
		@Override
		public void onCanceled(ArrayList<NewsItem> data) {
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
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItems.get(position).getLink()));
		startActivity(intent);
	}

	private class NewsItemAdapter extends ArrayAdapter<NewsItem> {
		private final LayoutInflater mInflater;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");		

        public NewsItemAdapter(Context context) {
        	super(context, R.layout.simple_list_item);
        	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<NewsItem> data) {
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
	        
	        NewsItem item = getItem(position);
	        
        	holder.title.setText(item.getTitle());
        	holder.description.setText(item.getPubDate());
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView description;
	}

    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }
	
}
