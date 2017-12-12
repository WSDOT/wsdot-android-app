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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.expresslanes;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.ExpressLaneItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.DividerNoBottom;

public class SeattleExpressLanesFragment extends BaseFragment implements
		SwipeRefreshLayout.OnRefreshListener, Injectable {

	private static final String TAG = SeattleExpressLanesFragment.class.getSimpleName();
	private static ItemAdapter mAdapter;

	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

	protected RecyclerView mRecyclerView;
	protected LinearLayoutManager mLayoutManager;

	ExpressLanesViewModel viewModel;

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
		mAdapter = new ItemAdapter(null);

		mRecyclerView.setAdapter(mAdapter);

		mRecyclerView.addItemDecoration(new DividerNoBottom(getActivity()));

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

		routeImage.put(5, R.drawable.ic_list_i5);
		routeImage.put(90, R.drawable.ic_list_i90);

		viewModel = ViewModelProviders.of(this, viewModelFactory).get(ExpressLanesViewModel.class);

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

		viewModel.getExpressLanesStatus().observe(this, expressLaneItems -> {
			if (expressLaneItems != null) {
				mEmptyView.setVisibility(View.GONE);
				mAdapter.setData(expressLaneItems);
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
	private class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private List<ExpressLaneItem> itemList;
		private LaneViewHolder laneItem;
		private LinkViewHolder linkItem;
		private InfoViewHolder infoItem;

		public ItemAdapter(List<ExpressLaneItem> items){
			this.itemList = items;
			notifyDataSetChanged();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

			View itemView;

			switch (viewType){
				case 0:
					itemView = LayoutInflater.
							from(parent.getContext()).
							inflate(R.layout.simple_list_item_with_icon, parent, false);
					return new LaneViewHolder(itemView);
				case 1:
					itemView = LayoutInflater.
							from(parent.getContext()).
							inflate(R.layout.list_item, parent, false);
					return new LinkViewHolder(itemView);
				case 2:
					itemView = LayoutInflater.
							from(parent.getContext()).
							inflate(R.layout.simple_list_item, parent, false);
					return new InfoViewHolder(itemView);
				default:
					return null;
			}
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

			int viewType = getItemViewType(position);
			switch (viewType){
				case 0:
					laneItem = (LaneViewHolder) holder;
					ExpressLaneItem item = itemList.get(position);
					laneItem.title.setText(item.getTitle() + " " + item.getStatus());
					laneItem.text.setText(item.getUpdated());
					laneItem.icon.setImageResource(routeImage.get(item.getRoute()));
					break;
				case 1:
					linkItem = (LinkViewHolder) holder;
					linkItem.title.setText(R.string.expresslanes_link_title);
					linkItem.itemView.setOnClickListener(
							v -> {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(APIEndPoints.EXPRESS_LANES_WEBSITE));
                                startActivity(intent);
                            }
					);
					break;
				case 2:
					infoItem = (InfoViewHolder) holder;
					infoItem.info.setText(R.string.expresslanes_info);
					break;
			}
		}

		//Add +2 for Schedule link & info item
		@Override
		public int getItemCount() {
			if (itemList == null) {
				return 0;
			}else {
				return itemList.size() + 2;
			}
		}

		@Override
		public int getItemViewType(int position){
			if (position < 2){
				return 0;
			}else if (position == 2){
				return 1;
			}else{
				return 2;
			}
		}

		public void clear(){
			if (itemList != null) {
				this.itemList.clear();
				notifyDataSetChanged();
			}
		}

		public void setData(List<ExpressLaneItem> posts){
			this.itemList = posts;
			notifyDataSetChanged();
		}

	}

	public static class LaneViewHolder extends RecyclerView.ViewHolder {
		public TextView title;
		public TextView text;
		public ImageView icon;

		public LaneViewHolder(View itemView){
			super(itemView);
			title = itemView.findViewById(R.id.title);
			text = itemView.findViewById(R.id.text);
			icon = itemView.findViewById(R.id.icon);

		}
	}

	public static class LinkViewHolder extends RecyclerView.ViewHolder{
		public TextView title;

		public LinkViewHolder(View itemView){
			super(itemView);
			title = itemView.findViewById(R.id.title);
		}
	}

	public static class InfoViewHolder extends RecyclerView.ViewHolder{
		public TextView info;

		public InfoViewHolder(View itemView){
			super(itemView);
			info = itemView.findViewById(R.id.description);
		}
	}

	public void onRefresh() {
		viewModel.refresh();
	}
}