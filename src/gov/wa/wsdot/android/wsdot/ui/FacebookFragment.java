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
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class FacebookFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<FacebookItem>> {
	
	private static final String DEBUG_TAG = "Facebook";
	private static ArrayList<FacebookItem> mFacebookItems = null;
	private static FacebookItemAdapter mAdapter;
	private static View mLoadingSpinner;
	ActionMode mActionMode;

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
		
		// Remove the separator between items in the ListView
		//getListView().setDivider(null);
		//getListView().setDividerHeight(0);
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				mActionMode = getSherlockActivity().startActionMode(new ActionModeCallback(mFacebookItems.get(position).getMessage()));
				
				return true;
			}
			
		});
		
		mAdapter = new FacebookItemAdapter(getActivity());
		setListAdapter(mAdapter);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);		
	}    
	
	private final class ActionModeCallback implements ActionMode.Callback {
		private String mText;
		
		public ActionModeCallback(String text) {
			this.mText = text;
		}

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.share_action_provider, menu);
	        // Set file with share history to the provider and set the share intent.
	        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
	        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
	        //actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
	        actionProvider.setShareHistoryFileName(null);
	        // Note that you can set/change the intent any time,
	        // say when the user has selected an image.
	        actionProvider.setShareIntent(createShareIntent(mText));
			
	        return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			mode.finish();
			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
		
	}

	private Intent createShareIntent(String mText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mText);
        
        return shareIntent;
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
		mAdapter.setData(data);
	}

	public void onLoaderReset(Loader<ArrayList<FacebookItem>> loader) {
		mAdapter.setData(null);
	}	
	
	/**
	 * A custom Loader that loads all of the border wait times from the data server.
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
				
				for (int j=0; j < items.length(); j++) {
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
		            		Log.e(DEBUG_TAG, "Error parsing date", e);
		            	}
		            	
						mFacebookItems.add(i);
				}				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
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
        //private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");		

        public FacebookItemAdapter(Context context) {
        	super(context, R.layout.simple_list_item);
        	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<FacebookItem> data) {
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
	        
	        FacebookItem item = getItem(position);
	        
	        if (item != null) {
	        	TextView message = (TextView) convertView.findViewById(R.id.title);
                message.setMovementMethod(LinkMovementMethod.getInstance());
                message.setTypeface(tf);
	        	TextView createdAt = (TextView) convertView.findViewById(R.id.description);
                createdAt.setTypeface(tf);
                
                if (message != null) {
                	message.setText(Html.fromHtml(item.getmHtmlFormattedMessage()));
                }

                if (createdAt != null) {
                	createdAt.setText(item.getCreatedAt());
                }
                
	        }
	        
	        return convertView;
        }

	}
	
	public static class ViewHolder {
		public TextView text;
		public TextView createdAt;
	}
}