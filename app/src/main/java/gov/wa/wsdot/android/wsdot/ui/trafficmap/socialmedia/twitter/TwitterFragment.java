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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.twitter;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.TwitterItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ImageManager;

public class TwitterFragment extends BaseFragment implements
        AdapterView.OnItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener,
		Injectable {
	
	private static final String TAG = TwitterFragment.class.getSimpleName();
	private static TwitterItemAdapter mAdapter;

    private String[] accounts = {"all", "wsferries", "SnoqualmiePass", "wsdot", "WSDOT_East", "wsdot_north", "wsdot_sw", "wsdot_tacoma", "wsdot_traffic"};

	private HashMap<String, Integer> mTwitterProfileImages = new HashMap<String, Integer>();
	
	@SuppressWarnings("unused")
    private ActionMode mActionMode;
	private View mEmptyView;

	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	TwitterViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_spinner_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TwitterItemAdapter(null);

        mRecyclerView.setAdapter(mAdapter);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);
        
        mEmptyView = root.findViewById( R.id.empty_list_view );

        Spinner spinner = root.findViewById(R.id.fragment_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.twitter_accounts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

		viewModel = ViewModelProviders.of(this, viewModelFactory).get(TwitterViewModel.class);

		viewModel.getResourceStatus().observe(this, resourceStatus -> {
			if (resourceStatus != null) {
				switch (resourceStatus.status) {
					case LOADING:
						swipeRefreshLayout.setRefreshing(true);
						break;
					case SUCCESS:
						swipeRefreshLayout.setRefreshing(false);
						break;
					case ERROR:
						swipeRefreshLayout.setRefreshing(false);
						TextView t = (TextView) mEmptyView;
						t.setText(R.string.no_connection);
						mEmptyView.setVisibility(View.VISIBLE);
						Toast.makeText(getContext(), "connection error", Toast.LENGTH_SHORT).show();
				}
			}
		});

		viewModel.getTwitterPosts().observe(this, twitterItems -> {
			if (twitterItems != null) {
				mEmptyView.setVisibility(View.GONE);
				if (!twitterItems.isEmpty()) {
					mAdapter.setData(twitterItems);
				} else {
					TextView t = (TextView) mEmptyView;
					t.setText("tweets unavailable.");
					mEmptyView.setVisibility(View.VISIBLE);
				}
			}
		});

		viewModel.refresh();

        return root;
    }    
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mTwitterProfileImages.put("wsferries", R.drawable.ic_list_wsdot_ferries);
		mTwitterProfileImages.put("SnoqualmiePass", R.drawable.ic_list_wsdot_snoqualmie_pass);
		mTwitterProfileImages.put("wsdot", R.drawable.ic_list_wsdot);
		mTwitterProfileImages.put("WSDOT_East", R.drawable.ic_list_wsdot_east);
		mTwitterProfileImages.put("wsdot_sw", R.drawable.ic_list_wsdot_sw);
		mTwitterProfileImages.put("wsdot_tacoma", R.drawable.ic_list_wsdot_tacoma);
		mTwitterProfileImages.put("wsdot_traffic", R.drawable.ic_list_wsdot_traffic);
		mTwitterProfileImages.put("wsdot_north", R.drawable.ic_list_wsdot_north);

	}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        viewModel.setAccount(accounts[position]);
        this.onRefresh();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

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

	/**
	 * Custom adapter for items in recycler view.
	 *
	 * Extending RecyclerView adapter this adapter binds the custom ViewHolder
	 * class to it's data.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter
	 */
	private class TwitterItemAdapter extends RecyclerView.Adapter<TwitterViewHolder> {

        private ImageManager imageManager;
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private List<TwitterItem> tweetList;

		public TwitterItemAdapter(List<TwitterItem> posts){
			this.tweetList = posts;
            imageManager = new ImageManager(getActivity(), 0);
			notifyDataSetChanged();
		}

		@Override
		public TwitterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = LayoutInflater.
					from(parent.getContext()).
					inflate(R.layout.card_item_with_icon_twitter, parent, false);
			return new TwitterViewHolder(itemView);
		}

		@Override
		public void onBindViewHolder(TwitterViewHolder holder, int position) {

            TwitterItem post = tweetList.get(position);

            if (post.getMediaUrl() == null) {
                holder.image.setVisibility(View.GONE);
            } else {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setTag(post.getMediaUrl());
                imageManager.displayImage(post.getMediaUrl(), getActivity(), holder.image);
            }

            try {
                holder.icon.setImageResource(mTwitterProfileImages.get(post.getScreenName()));
            } catch (Exception e) {
                // Use regular WSDOT icon if we add an additional Twitter feed
                // and have not updated the app to include the new icon.
                holder.icon.setImageResource(mTwitterProfileImages.get("wsdot"));
            }

            holder.text.setText(post.getText());
            holder.text.setTypeface(tf);

			holder.createdAt.setText(post.getCreatedAt());
            holder.createdAt.setTypeface(tf);

            holder.userName.setText(post.getUserName());
            holder.userName.setTypeface(tfb);

			final String postID = post.getId();
            final String screenName = post.getScreenName();

			// Set onClickListener for holder's view
			holder.itemView.setOnClickListener(
                    v -> {
                        String url = "https://twitter.com/" + screenName + "/status/" + postID;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
            );
		}

		@Override
		public int getItemCount() {
			if (tweetList == null) {
				return 0;
			}else {
				return tweetList.size();
			}
		}

		public void clear(){
			if (tweetList != null) {
				this.tweetList.clear();
				notifyDataSetChanged();
			}
		}

		public void setData(List<TwitterItem> posts){
			this.tweetList = posts;
			notifyDataSetChanged();
		}

	}

	public static class TwitterViewHolder extends RecyclerView.ViewHolder {
		protected ImageView image;
		protected ImageView icon;
		protected TextView userName;
		protected TextView createdAt;
		protected TextView text;

		public TwitterViewHolder(View itemView) {
			super(itemView);
			image = itemView.findViewById(R.id.image);
			icon = itemView.findViewById(R.id.icon);
			userName = itemView.findViewById(R.id.user_name);
			createdAt =	itemView.findViewById(R.id.created_at);
			text = itemView.findViewById(R.id.text);
		}
	}

    public void onRefresh() {
		viewModel.refresh();
    }
}