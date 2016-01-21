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

package gov.wa.wsdot.android.wsdot.ui.socialmedia.blogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import android.widget.ImageView;
import android.widget.TextView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.BlogItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ImageManager;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class BlogFragment extends BaseFragment implements
        LoaderCallbacks<ArrayList<BlogItem>>,
        SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = BlogFragment.class.getSimpleName();
	private static BlogItemAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
        mAdapter = new BlogItemAdapter(null);

        mRecyclerView.setAdapter(mAdapter);


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
	
	public Loader<ArrayList<BlogItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new BlogItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<BlogItem>> loader, ArrayList<BlogItem> data) {

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

	public void onLoaderReset(Loader<ArrayList<BlogItem>> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	    mAdapter.setData(null);
	}
	
	/**
	 * A custom Loader that loads all of the posts from the WSDOT blog.
	 */	
	public static class BlogItemsLoader extends AsyncTaskLoader<ArrayList<BlogItem>> {

		private ArrayList<BlogItem> mItems = null;
		
		public BlogItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<BlogItem> loadInBackground() {
			mItems = new ArrayList<BlogItem>();
			BlogItem i = null;
			
			try {
				URL url = new URL("http://wsdotblog.blogspot.com/feeds/posts/default?alt=json&max-results=10");
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
				
				int numEntries = entries.length();
				for (int j=0; j < numEntries; j++) {
					JSONObject entry = entries.getJSONObject(j);
					i = new BlogItem();
					i.setTitle(entry.getJSONObject("title").getString("$t"));

	            	try {
	            		i.setPublished(ParserUtils.relativeTime(entry.getJSONObject("published").getString("$t"), "yyyy-MM-dd'T'HH:mm:ss.SSSz", true));
	            	} catch (Exception e) {
	            		i.setPublished("Unavailable");
	            		Log.e(TAG, "Error parsing date", e);
	            	}
					
	            	String content = entry.getJSONObject("content").getString("$t");
					i.setContent(content);
					
					Document doc = Jsoup.parse(content);
					Element imgTable = doc.select("table img").first();
					Element imgDiv = doc.select("div:not(.blogger-post-footer) img").first();
					Element table = doc.select("table").first();
					if (imgTable != null) {
						String imgSrc = imgTable.attr("src");
						i.setImageUrl(imgSrc);
						if (table != null) {
							try {
								String caption = table.text();
								i.setImageCaption(caption);
							} catch (NullPointerException e) {
								// TODO Auto-generated catch block
							}
						}
					} else if (imgDiv != null) {
						String imgSrc = imgDiv.attr("src");
						i.setImageUrl(imgSrc);
					}
					
					String temp = content.replaceFirst("<i>(.*)</i><br /><br />", "");
					temp = temp.replaceFirst("<table(.*?)>.*?</table>", "");
					String tempDoc = Jsoup.parse(temp).text();
					try {
						String description = tempDoc.split("\\.", 2)[0] + ".";
						i.setDescription(description);
					} catch (ArrayIndexOutOfBoundsException e) {
						i.setDescription("");
					}

					i.setLink(entry.getJSONArray("link").getJSONObject(4).getString("href"));
					
					mItems.add(i);
				}

			} catch (Exception e) {
				Log.e(TAG, "Error in network call", e);
			}

			return mItems;
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
		public void onCanceled(ArrayList<BlogItem> data) {
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

	/**
	 * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
	 */
    private class BlogItemAdapter extends RecyclerView.Adapter<BlogViewHolder> {

        private ImageManager imageManager;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private List<BlogItem> postList;

        public BlogItemAdapter(List<BlogItem> posts){
            this.postList = posts;
            imageManager = new ImageManager(getActivity(), 0);
            notifyDataSetChanged();
        }

        @Override
        public BlogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_item_with_image_blog, parent, false);
            return new BlogViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(BlogViewHolder holder, int position) {

            BlogItem post = postList.get(position);

            if (post.getImageUrl() == null) {
                holder.image.setVisibility(View.GONE);
                holder.caption.setVisibility(View.GONE);
            } else {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setTag(post.getImageUrl());
                imageManager.displayImage(post.getImageUrl(), getActivity(), holder.image);
                if (post.getImageCaption() == null) {
                    holder.caption.setVisibility(View.GONE);
                } else {
                    holder.caption.setVisibility(View.VISIBLE);
                    holder.caption.setText(post.getImageCaption().toUpperCase());
                }
            }

            holder.title.setText(post.getTitle());
            holder.description.setText(post.getDescription());
            holder.createdAt.setText(post.getPublished());

            holder.title.setTypeface(tfb);
            holder.description.setTypeface(tf);
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

        public void setData(List<BlogItem> posts){
            this.postList = posts;
            notifyDataSetChanged();
        }
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView caption;
        protected TextView title;
        protected TextView description;
        protected TextView createdAt;

        public BlogViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            caption = (TextView) itemView.findViewById(R.id.caption);
            title = (TextView) itemView.findViewById(R.id.title);
            description =	(TextView) itemView.findViewById(R.id.description);
            createdAt =	(TextView) itemView.findViewById(R.id.created_at);
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
