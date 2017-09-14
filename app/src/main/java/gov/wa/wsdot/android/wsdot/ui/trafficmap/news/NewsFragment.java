/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.news;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.NewsItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class NewsFragment extends BaseFragment implements
        LoaderCallbacks<ArrayList<NewsItem>>,
        SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = NewsFragment.class.getSimpleName();
	private static ArrayList<NewsItem> newsItems = null;	
	private static NewsItemAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

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
        mAdapter = new NewsItemAdapter(null);

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
	
	public Loader<ArrayList<NewsItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new NewsItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<NewsItem>> loader, ArrayList<NewsItem> data) {

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
				URL url = new URL(APIEndPoints.WSDOT_NEWS);
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
			swipeRefreshLayout.post(
					new Runnable() {
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

	/**
	 * Custom adapter for items in recycler view.
	 *
	 * Extending RecyclerView adapter this adapter binds the custom ViewHolder
	 * class to it's data.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter
	 */
    private class NewsItemAdapter extends RecyclerView.Adapter<NewsViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        private List<NewsItem> newsList;

        public NewsItemAdapter(List<NewsItem> posts){
            this.newsList = posts;
            notifyDataSetChanged();
        }

        @Override
        public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.simple_list_item, parent, false);
            return new NewsViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(NewsViewHolder holder, int position) {

            NewsItem post = newsList.get(position);
            holder.title.setText(post.getTitle());
            holder.createdAt.setText(post.getPubDate());

            holder.title.setTypeface(tfb);
            holder.createdAt.setTypeface(tf);

            final String postLink = post.getLink();

            // Set onClickListener for holder's view
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(postLink));
                            startActivity(intent);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            if (newsList == null) {
                return 0;
            }else {
                return newsList.size();
            }
        }

        public void clear(){
            if (newsList != null) {
                this.newsList.clear();
                notifyDataSetChanged();
            }
        }

        public void setData(List<NewsItem> posts){
            this.newsList = posts;
            notifyDataSetChanged();
        }
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView createdAt;

        public NewsViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            createdAt = (TextView) itemView.findViewById(R.id.description);
        }
    }

    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }
	
}
