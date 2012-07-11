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
import gov.wa.wsdot.android.wsdot.shared.BlogItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class BlogFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<BlogItem>> {

	private static final String DEBUG_TAG = "Blog";
	private static ArrayList<BlogItem> blogItems = null;
	private static BlogItemAdapter mAdapter;
	private static View mLoadingSpinner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
        setHasOptionsMenu(true);
        
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/News & Social Media/Blog");
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
		
		mAdapter = new BlogItemAdapter(getActivity());
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
	
	public Loader<ArrayList<BlogItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new BlogItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<BlogItem>> loader, ArrayList<BlogItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		mAdapter.setData(data);
	}

	public void onLoaderReset(Loader<ArrayList<BlogItem>> loader) {
		mAdapter.setData(null);
	}
	
	/**
	 * A custom Loader that loads all of the posts from the WSDOT blog.
	 */	
	public static class BlogItemsLoader extends AsyncTaskLoader<ArrayList<BlogItem>> {

		public BlogItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<BlogItem> loadInBackground() {
			DateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz"); //e.g. 2010-08-20T10:11:00.000-07:00
			DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
			blogItems = new ArrayList<BlogItem>();
			BlogItem i = null;
			
			try {
				URL url = new URL("http://wsdotblog.blogspot.com/feeds/posts/default?alt=json");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject data = obj.getJSONObject("feed");			
				JSONArray entries = data.getJSONArray("entry");
				
				for (int j=0; j < entries.length(); j++) {
					JSONObject entry = entries.getJSONObject(j);
					i = new BlogItem();
					i.setTitle(entry.getJSONObject("title").getString("$t"));

	            	try {
	            		Date date = parseDateFormat.parse(entry.getJSONObject("published").getString("$t"));
	            		i.setPublished(displayDateFormat.format(date));
	            	} catch (Exception e) {
	            		i.setPublished("N/A");
	            		Log.e(DEBUG_TAG, "Error parsing date", e);
	            	}					
					
					i.setContent(entry.getJSONObject("content").getString("$t"));
					i.setLink(entry.getJSONArray("link").getJSONObject(4).getString("href"));
					
					blogItems.add(i);
				}

			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}

			return blogItems;
		}
		
		@Override
		public void deliverResult(ArrayList<BlogItem> data) {
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
		public void onCanceled(ArrayList<BlogItem> data) {
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
		Intent intent = new Intent(getActivity(), BlogDetailsActivity.class);
		b.putString("title", blogItems.get(position).getTitle());
		b.putString("content", blogItems.get(position).getContent());
		b.putString("link", blogItems.get(position).getLink());
		intent.putExtras(b);
		
		startActivity(intent);
	}
	
	private class BlogItemAdapter extends ArrayAdapter<BlogItem> {
		private final LayoutInflater mInflater;
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        public BlogItemAdapter(Context context) {
	        super(context, R.layout.simple_list_item);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        public void setData(ArrayList<BlogItem> data) {
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
	        
	        BlogItem item = getItem(position);
	        
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tfb);
	            TextView bt = (TextView) convertView.findViewById(R.id.description);
	            bt.setTypeface(tf);
	            if (tt != null) {
	            	tt.setText(item.getTitle());
	            }
	            if(bt != null){
            		bt.setText(item.getPublished());
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
