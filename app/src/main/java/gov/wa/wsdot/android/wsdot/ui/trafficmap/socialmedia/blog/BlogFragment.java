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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.blog;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.BlogItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ImageManager;

public class BlogFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        Injectable {

	private static final String TAG = BlogFragment.class.getSimpleName();
	private static BlogItemAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    BlogViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new BlogItemAdapter(null);

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


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(BlogViewModel.class);

        viewModel.getResourceStatus().observe(getViewLifecycleOwner(), resourceStatus -> {
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

        viewModel.getBlogPosts().observe(getViewLifecycleOwner(), blogItems -> {
            if (blogItems != null) {
                mEmptyView.setVisibility(View.GONE);
                if (!blogItems.isEmpty()) {
                    mAdapter.setData(blogItems);
                } else {
                    TextView t = (TextView) mEmptyView;
                    t.setText("blog unavailable.");
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
     * @see RecyclerView.Adapter
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
                    v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(postLink));
                        startActivity(intent);
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
            image = itemView.findViewById(R.id.image);
            caption = itemView.findViewById(R.id.caption);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            createdAt =	itemView.findViewById(R.id.created_at);
        }
    }

    public void onRefresh() {
		viewModel.refresh();
    }
	
}
