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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.youtube;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.YouTubeItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.ImageManager;

public class YouTubeFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        Injectable {

    private static final String TAG = YouTubeFragment.class.getSimpleName();
    private static VideoItemAdapter mAdapter;

    @SuppressWarnings("unused")
    private ActionMode mActionMode;
    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    YouTubeViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new VideoItemAdapter(null);

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

        mEmptyView = root.findViewById(R.id.empty_list_view);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(YouTubeViewModel.class);

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

        viewModel.getYouTubePosts().observe(this, youTubeItems -> {
            if (youTubeItems != null) {
                mEmptyView.setVisibility(View.GONE);
                if (!youTubeItems.isEmpty()) {
                    mAdapter.setData(youTubeItems);
                } else {
                    TextView t = (TextView) mEmptyView;
                    t.setText("posts unavailable.");
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.refresh();

        return root;
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

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class VideoItemAdapter extends RecyclerView.Adapter<YouTubeViewHolder> {

        private ImageManager imageManager;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private List<YouTubeItem> videoList;

        public VideoItemAdapter(List<YouTubeItem> posts) {
            this.videoList = posts;
            imageManager = new ImageManager(getActivity(), 0);
            notifyDataSetChanged();
        }

        @Override
        public YouTubeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_item_youtube, parent, false);
            return new YouTubeViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(YouTubeViewHolder holder, int position) {

            YouTubeItem post = videoList.get(position);

            holder.title.setText(post.getTitle());
            holder.title.setTypeface(tf);

            holder.uploaded.setText(post.getUploaded());
            holder.uploaded.setTypeface(tf);

            holder.image.setTag(post.getThumbNailUrl());
            imageManager.displayImage(post.getThumbNailUrl(), getActivity(), holder.image);

            final String videoId = post.getId();

            // Set onClickListener for holder's view
            holder.itemView.setOnClickListener(
                    v -> {
                        String url = APIEndPoints.YOUTUBE_WATCH + "?v=" + videoId;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
            );
        }

        @Override
        public int getItemCount() {
            if (videoList == null) {
                return 0;
            } else {
                return videoList.size();
            }
        }

        public void clear() {
            if (videoList != null) {
                this.videoList.clear();
                notifyDataSetChanged();
            }
        }

        public void setData(List<YouTubeItem> posts) {
            this.videoList = posts;
            notifyDataSetChanged();
        }
    }

    public static class YouTubeViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView description;
        protected ImageView image;
        protected TextView uploaded;

        public YouTubeViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            uploaded = itemView.findViewById(R.id.uploaded);
        }
    }

    public void onRefresh() {
        viewModel.refresh();
    }
}
