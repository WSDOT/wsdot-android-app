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
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class TwitterFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<TwitterItem>> {
	
	private static final String DEBUG_TAG = "Twitter";
	private static ArrayList<TwitterItem> twitterItems = null;
	private static TwitterItemAdapter mAdapter;
	private static View mLoadingSpinner;
	private static String mScreenName;
	private HashMap<String, Integer> mTwitterProfileImages = new HashMap<String, Integer>();
	ActionMode mActionMode;
	private View mEmptyView;

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
        mEmptyView = root.findViewById( R.id.empty_list_view );

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
				
				mActionMode = getSherlockActivity().startActionMode(new ActionModeCallback(twitterItems.get(position).getText()));
				
				return true;
			}
			
		});
		
		mAdapter = new TwitterItemAdapter(getActivity());
		setListAdapter(mAdapter);
		
		mTwitterProfileImages.put("wsferries", R.drawable.ic_list_wsdot_ferries);
		mTwitterProfileImages.put("GoodToGoWSDOT", R.drawable.ic_list_wsdot_goodtogo);
		mTwitterProfileImages.put("SnoqualmiePass", R.drawable.ic_list_wsdot_snoqualmie_pass);
		mTwitterProfileImages.put("wsdot", R.drawable.ic_list_wsdot);
		mTwitterProfileImages.put("wsdot_sw", R.drawable.ic_list_wsdot_sw);
		mTwitterProfileImages.put("wsdot_tacoma", R.drawable.ic_list_wsdot_tacoma);
		mTwitterProfileImages.put("wsdot_traffic", R.drawable.ic_list_wsdot_traffic);		
		
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

	public Loader<ArrayList<TwitterItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new TwitterItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<TwitterItem>> loader, ArrayList<TwitterItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		
		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
			getListView().setEmptyView(mEmptyView);
		}
	}

	public void onLoaderReset(Loader<ArrayList<TwitterItem>> loader) {
		mAdapter.setData(null);
	}	
	
	/**
	 * A custom Loader that loads all of the Twitter feeds from the data server.
	 */	
	public static class TwitterItemsLoader extends AsyncTaskLoader<ArrayList<TwitterItem>> {

		public TwitterItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<TwitterItem> loadInBackground() {
			String urlPattern = "(https?:\\/\\/[-a-zA-Z0-9._~:\\/?#@!$&\'()*+,;=%]+)";
			String atPattern = "@+([_a-zA-Z0-9-]+)";
			String hashPattern = "#+([_a-zA-Z0-9-]+)";
			String text;
			String htmlText;

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
				
				for (int j=0; j < items.length(); j++) {
						JSONObject item = items.getJSONObject(j);
						i = new TwitterItem();
						htmlText = "";
						text = item.getString("text");
						htmlText = text.replaceAll(urlPattern, "<a href=\"$1\">$1</a>");
						htmlText = htmlText.replaceAll(atPattern, "<a href=\"http://twitter.com/#!/$1\">@$1</a>");
						htmlText = htmlText.replaceAll(hashPattern, "<a href=\"http://twitter.com/#!/search?q=%23$1\">#$1</a>");

						i.setText(text);
						i.setFormatedHtmlText(htmlText);

		            	JSONObject user = item.getJSONObject("user");
		            	i.setUserName(user.getString("name"));
		            	i.setScreenName(user.getString("screen_name"));
						
		            	try {
		            		i.setCreatedAt(ParserUtils.relativeTime(item.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true));
		            	} catch (Exception e) {
		            		i.setCreatedAt("");
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
		b.putString("userName", twitterItems.get(position).getUserName());
		b.putString("createdAt", twitterItems.get(position).getCreatedAt());
		b.putString("text", twitterItems.get(position).getText());
		b.putString("htmlText", twitterItems.get(position).getFormatedHtmlText());
		intent.putExtras(b);

		startActivity(intent);
	}

	private class TwitterItemAdapter extends ArrayAdapter<TwitterItem> {
		private final LayoutInflater mInflater;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");		

        public TwitterItemAdapter(Context context) {
        	super(context, R.layout.twitter_list_item_with_icon);
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
	        ViewHolder holder = null;
			
			if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.twitter_list_item_with_icon, null);
	            holder = new ViewHolder();
	            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
	            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
	            holder.userName.setTypeface(tfb);
	            holder.createdAt = (TextView) convertView.findViewById(R.id.created_at);
	            holder.createdAt.setTypeface(tf);
	            holder.text = (TextView) convertView.findViewById(R.id.text);
	            holder.text.setMovementMethod(LinkMovementMethod.getInstance());
	            holder.text.setTypeface(tf);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        TwitterItem item = getItem(position);
	        
        	holder.icon.setImageResource(mTwitterProfileImages.get(item.getScreenName()));
        	holder.userName.setText(item.getUserName());
        	holder.createdAt.setText(item.getCreatedAt());
        	holder.text.setText(Html.fromHtml(item.getFormatedHtmlText()));
	        
	        return convertView;
        }

	}
	
	public static class ViewHolder {
		public ImageView icon;
		public TextView userName;
		public TextView createdAt;
		public TextView text;
	}
}