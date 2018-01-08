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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.facebook;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.FacebookItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FacebookFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener,
		Injectable {
	
	private static final String TAG = FacebookFragment.class.getSimpleName();
	private static FacebookItemAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    FacebookViewModel viewModel;

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
        mAdapter = new FacebookItemAdapter(null);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FacebookViewModel.class);

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

        viewModel.getFacebookPosts().observe(this, facebookItems -> {
            if (facebookItems != null) {
                mEmptyView.setVisibility(View.GONE);
                if (!facebookItems.isEmpty()) {
                    mAdapter.setData(facebookItems);
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
            text = itemView.findViewById(R.id.title);
            createdAt = itemView.findViewById(R.id.description);
        }
    }

    public void onRefresh() {
		viewModel.refresh();
    }
}