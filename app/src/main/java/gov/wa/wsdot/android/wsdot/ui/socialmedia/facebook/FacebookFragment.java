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

package gov.wa.wsdot.android.wsdot.ui.socialmedia.facebook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.FacebookItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FacebookFragment extends BaseFragment implements
        LoaderCallbacks<ArrayList<FacebookItem>>,
        SwipeRefreshLayout.OnRefreshListener {
	
	private static final String TAG = FacebookFragment.class.getSimpleName();
	private static FacebookItemAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

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
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FacebookItemAdapter(null);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

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
        
        return root;
    }    
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);		
	}    

	public Loader<ArrayList<FacebookItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new FacebookItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<FacebookItem>> loader, ArrayList<FacebookItem> data) {

        mEmptyView.setVisibility(View.GONE);

		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
            mEmptyView.setVisibility(View.VISIBLE);
		}
		
		swipeRefreshLayout.setRefreshing(false);
	}

	public void onLoaderReset(Loader<ArrayList<FacebookItem>> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	    mAdapter.setData(null);
	}	
	
	/**
	 * A custom Loader that loads Facebook posts from the data server.
	 */	
	public static class FacebookItemsLoader extends AsyncTaskLoader<ArrayList<FacebookItem>> {

		public FacebookItemsLoader(Context context) {
			super(context);
		}
        private ArrayList<FacebookItem> mFacebookItems;

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
						i.setId(item.getString("id"));
						
		            	try {
                        i.setCreatedAt(ParserUtils.relativeTime(
                                item.getString("created_at"),
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true));
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
		protected void onStartLoading() {
			super.onStartLoading();
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

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
	private class FacebookItemAdapter extends RecyclerView.Adapter<FacebookViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private List<FacebookItem> postList;

        public FacebookItemAdapter(List<FacebookItem> posts){
            this.postList = posts;
            notifyDataSetChanged();
        }

        @Override
        public FacebookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.simple_list_item, parent, false);
            return new FacebookViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(FacebookViewHolder holder, int position) {

            FacebookItem post = postList.get(position);
            holder.text.setText(post.getMessage());
            holder.createdAt.setText(post.getCreatedAt());

            holder.text.setTypeface(tf);
            holder.createdAt.setTypeface(tf);

            final String postID = post.getId();

            // Set onClickListener for holder's view
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            String url = "https://facebook.com/" + postID;
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            if (postList == null) {
                return 0;
            }else {
                return postList.size();
            }
        }

        public void clear(){
            if (postList != null) {
                this.postList.clear();
                notifyDataSetChanged();
            }
        }

        public void setData(List<FacebookItem> posts){
            this.postList = posts;
            notifyDataSetChanged();
        }

    }

    public static class FacebookViewHolder extends RecyclerView.ViewHolder {
        protected TextView text;
        protected TextView createdAt;

        public FacebookViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.title);
            createdAt = (TextView) itemView.findViewById(R.id.description);
        }
    }

    public void onRefresh() {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
        getLoaderManager().restartLoader(0, null, this);        
    }
}