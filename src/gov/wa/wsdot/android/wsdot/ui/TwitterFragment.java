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
import gov.wa.wsdot.android.wsdot.shared.TwitterItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.ImageManager;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aje.android.sdk.AdError;
import aje.android.sdk.AdJugglerAdView;
import aje.android.sdk.AdListener;
import aje.android.sdk.AdRequest;
import aje.android.sdk.IncorrectAdRequestException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TwitterFragment extends ListFragment implements
        LoaderCallbacks<ArrayList<TwitterItem>>,
        SwipeRefreshLayout.OnRefreshListener {
	
	private static final String TAG = TwitterFragment.class.getSimpleName();
	private static ArrayList<TwitterItem> twitterItems = null;
	private static TwitterItemAdapter mAdapter;
	private static String mScreenName;
	private HashMap<String, Integer> mTwitterProfileImages = new HashMap<String, Integer>();
	
	@SuppressWarnings("unused")
    private ActionMode mActionMode;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

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
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/News & Social Media/Twitter");
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_adjuggler_with_swipe_refresh, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorScheme(
                17170451,  // android.R.color.holo_blue_bright 
                17170452,  // android.R.color.holo_green_light 
                17170456,  // android.R.color.holo_orange_light 
                17170454); // android.R.color.holo_red_light)
        
        mEmptyView = root.findViewById( R.id.empty_list_view );

        final AdJugglerAdView mAdJugglerAdView = (AdJugglerAdView) root.findViewById(R.id.ajAdView);
        mAdJugglerAdView.setListener(new AdListener() {

            public boolean onClickAd(String arg0) {
                return false;
            }

            public void onExpand() {
            }

            public void onExpandClose() {
            }

            public void onFailedToClickAd(String arg0, String arg1) {
            }

            public void onFailedToFetchAd(AdError arg0, String arg1) {
            }

            public void onFetchAdFinished() {
            }

            public void onFetchAdStarted() {
            }

            public void onResize() {
            }

            public void onResizeClose() {
            }
        });
        
        try {
            AdRequest adRequest = new AdRequest();
            adRequest.setServer(getString(R.string.adRequest_server));
            adRequest.setZone(getString(R.string.adRequest_zone));
            adRequest.setAdSpot(getString(R.string.adRequest_adspot));
            mAdJugglerAdView.showAd(adRequest);
        } catch (IncorrectAdRequestException e) {
            Log.e(TAG, "Error showing banner ad", e);
        }
        
        return root;
    }    
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Remove the separator between items in the ListView
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
                mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(
                        new ActionModeCallback(twitterItems.get(position).getText()));
				
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
	        MenuItem menuItem_Share = menu.findItem(R.id.action_share);
	        ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem_Share);
	        shareAction.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
	        // Note that you can set/change the intent any time,
	        // say when the user has selected an image.
	        shareAction.setShareIntent(createShareIntent(mText));
			
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

	public Loader<ArrayList<TwitterItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new TwitterItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<TwitterItem>> loader, ArrayList<TwitterItem> data) {
		
		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
			getListView().setEmptyView(mEmptyView);
		}
		
		swipeRefreshLayout.setRefreshing(false);
	}

	public void onLoaderReset(Loader<ArrayList<TwitterItem>> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	    mAdapter.setData(null);
	}	
	
	/**
	 * A custom Loader that loads all of the Twitter feeds from the data server.
	 */	
	public static class TwitterItemsLoader extends AsyncTaskLoader<ArrayList<TwitterItem>> {

		private ArrayList<TwitterItem> mItems = null;
		
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

	    	mItems = new ArrayList<TwitterItem>();
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
				
				int numItems = items.length();
				for (int j=0; j < numItems; j++) {
					JSONObject item = items.getJSONObject(j);
					i = new TwitterItem();
					htmlText = "";
					text = item.getString("text");
					htmlText = text.replaceAll(urlPattern, "<a href=\"$1\">$1</a>");
					htmlText = htmlText.replaceAll(atPattern, "<a href=\"http://twitter.com/#!/$1\">@$1</a>");
					htmlText = htmlText.replaceAll(hashPattern, "<a href=\"http://twitter.com/#!/search?q=%23$1\">#$1</a>");
					
					JSONObject entities = item.getJSONObject("entities");
					
					try {
						JSONArray media = entities.getJSONArray("media");
						JSONObject mediaItem = media.getJSONObject(0);
						i.setMediaUrl(mediaItem.getString("media_url"));
					} catch (JSONException e) {
						// TODO Nothing.
					}
					
					if (i.getMediaUrl() == null) {
						try {
							JSONArray urls = entities.getJSONArray("urls");
							JSONObject urlItem = urls.getJSONObject(0);
							String expanded_url = urlItem.getString("expanded_url");
							if (expanded_url.matches("(.*)twitpic.com(.*)")) {
								i.setMediaUrl(urlItem.getString("expanded_url"));
							}
						} catch (Exception e1) {
							// TODO Nothing.
						}
					}
					
					i.setText(text);
					i.setFormatedHtmlText(htmlText);

	            	JSONObject user = item.getJSONObject("user");
	            	i.setUserName(user.getString("name"));
	            	i.setScreenName(user.getString("screen_name"));
					
	            	try {
	            		i.setCreatedAt(ParserUtils.relativeTime(item.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true));
	            	} catch (Exception e) {
	            		i.setCreatedAt("");
	            		Log.e(TAG, "Error parsing date", e);
	            	}
	            	
					mItems.add(i);
				}				
			} catch (Exception e) {
				Log.e(TAG, "Error in network call", e);
			}
			
			return mItems;
		}

		@Override
		public void deliverResult(ArrayList<TwitterItem> data) {
		    /**
		     * Called when there is new data to deliver to the client. The
		     * super class will take care of delivering it; the implementation
		     * here just adds a little more logic.
		     */	
			twitterItems = data;
			
			super.deliverResult(data);
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			
			mAdapter.clear();
			swipeRefreshLayout.setRefreshing(true);
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
	        
	        if (mItems != null) {
	        	mItems = null;
	        }
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
        private ImageManager imageManager;

        public TwitterItemAdapter(Context context) {
        	super(context, R.layout.twitter_list_item_with_icon);
        	mInflater = LayoutInflater.from(context);
        	imageManager = new ImageManager(getActivity(), 0);
        }

        public void setData(ArrayList<TwitterItem> data) {
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
	            convertView = mInflater.inflate(R.layout.twitter_list_item_with_icon, null);
	            holder = new ViewHolder();
	            holder.image = (ImageView) convertView.findViewById(R.id.image);
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
	        
	        if (item.getMediaUrl() == null) {
	        	holder.image.setVisibility(View.GONE);
	        } else {
	        	holder.image.setVisibility(View.VISIBLE);
	        	holder.image.setTag(item.getMediaUrl());
	        	imageManager.displayImage(item.getMediaUrl(), getActivity(), holder.image);
	        }
	        
        	try {
				holder.icon.setImageResource(mTwitterProfileImages.get(item.getScreenName()));
			} catch (Exception e) {
				// Use regular WSDOT icon if we add an additional Twitter feed
				// and have not updated the app to include the new icon.
				holder.icon.setImageResource(mTwitterProfileImages.get("wsdot"));
			}
        	holder.userName.setText(item.getUserName());
        	holder.createdAt.setText(item.getCreatedAt());
        	holder.text.setText(Html.fromHtml(item.getFormatedHtmlText()));
	        
	        return convertView;
        }

	}
	
	public static class ViewHolder {
		public ImageView image;
		public ImageView icon;
		public TextView userName;
		public TextView createdAt;
		public TextView text;
	}

    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);        
    }
}