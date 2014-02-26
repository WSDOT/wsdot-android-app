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
import gov.wa.wsdot.android.wsdot.shared.FacebookItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FacebookFragment extends ListFragment
	implements LoaderCallbacks<ArrayList<FacebookItem>> {
	
	private static final String TAG = FacebookFragment.class.getName();
	private static ArrayList<FacebookItem> mFacebookItems = null;
	private static FacebookItemAdapter mAdapter;
	private static View mLoadingSpinner;
	ActionMode mActionMode;
	private View mEmptyView;

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
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/News & Social Media/Facebook");
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
        mEmptyView = root.findViewById( R.id.empty_list_view );

        return root;
    }    
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new FacebookItemAdapter(getActivity());
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

	public Loader<ArrayList<FacebookItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new FacebookItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<FacebookItem>> loader, ArrayList<FacebookItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		
		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
			getListView().setEmptyView(mEmptyView);
		}
	}

	public void onLoaderReset(Loader<ArrayList<FacebookItem>> loader) {
		mAdapter.setData(null);
	}	
	
	/**
	 * A custom Loader that loads Facebook posts from the data server.
	 */	
	public static class FacebookItemsLoader extends AsyncTaskLoader<ArrayList<FacebookItem>> {

		public FacebookItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FacebookItem> loadInBackground() {
			String urlPattern = "(https?:\\/\\/[-a-zA-Z0-9._~:\\/?#@!$&\'()*+,;=%]+)";
			String text;
			String htmlText;

	    	mFacebookItems = new ArrayList<FacebookItem>();
			FacebookItem i = null;
			URL url;
			
			try {
				url = new URL("http://www.wsdot.wa.gov/news/socialroom/posts/facebook");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONArray items = new JSONArray(jsonFile);
				
				int numItems = items.length();
				for (int j=0; j < numItems; j++) {
						JSONObject item = items.getJSONObject(j);
						i = new FacebookItem();
						htmlText = "";
						text = item.getString("message");
						htmlText = text.replaceAll(urlPattern, "<a href=\"$1\">$1</a>");

						i.setMessage(text);
						i.setmHtmlFormattedMessage(htmlText);
						
		            	try {
		            		i.setCreatedAt(ParserUtils.relativeTime(item.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true));
		            	} catch (Exception e) {
		            		i.setCreatedAt("");
		            		Log.e(TAG, "Error parsing date", e);
		            	}
		            	
						mFacebookItems.add(i);
				}				
			} catch (Exception e) {
				Log.e(TAG, "Error in network call", e);
			}
			
			return mFacebookItems;
		}

		@Override
		public void deliverResult(ArrayList<FacebookItem> data) {
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
		public void onCanceled(ArrayList<FacebookItem> data) {
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
		Intent intent = new Intent(getActivity(), FacebookDetailsActivity.class);
		b.putString("createdAt", mFacebookItems.get(position).getCreatedAt());
		b.putString("text", mFacebookItems.get(position).getMessage());
		b.putString("htmlText", mFacebookItems.get(position).getmHtmlFormattedMessage());
		intent.putExtras(b);

		startActivity(intent);
	}

	private class FacebookItemAdapter extends ArrayAdapter<FacebookItem> {
		private final LayoutInflater mInflater;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

        public FacebookItemAdapter(Context context) {
        	super(context, R.layout.simple_list_item);
        	mInflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<FacebookItem> data) {
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
	            holder.title.setMovementMethod(LinkMovementMethod.getInstance());
	            holder.title.setTypeface(tf);
	            holder.description = (TextView) convertView.findViewById(R.id.description);
	            holder.description.setTypeface(tf);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        FacebookItem item = getItem(position);
	        
           	holder.title.setText(Html.fromHtml(item.getmHtmlFormattedMessage()));
           	holder.description.setText(item.getCreatedAt());
	        
	        return convertView;
        }

	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView description;
	}
}