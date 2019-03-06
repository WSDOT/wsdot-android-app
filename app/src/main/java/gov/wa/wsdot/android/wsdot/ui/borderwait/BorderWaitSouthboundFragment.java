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

package gov.wa.wsdot.android.wsdot.ui.borderwait;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class BorderWaitSouthboundFragment extends BaseFragment implements
		SwipeRefreshLayout.OnRefreshListener, Injectable {

	private static final String TAG = BorderWaitSouthboundFragment.class.getSimpleName();

	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();

	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

	private static BorderWaitSouthboundFragment.BorderWaitAdapter mAdapter;
	protected RecyclerView mRecyclerView;
	protected LinearLayoutManager mLayoutManager;

	private static List<BorderWaitEntity> mBorderWaits = new ArrayList<>();

	private static BorderWaitViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		routeImage.put(5, R.drawable.ic_list_i5);
		routeImage.put(9, R.drawable.ic_list_sr9);
		routeImage.put(539, R.drawable.ic_list_sr539);
		routeImage.put(543, R.drawable.ic_list_sr543);
		routeImage.put(97, R.drawable.ic_list_us97);
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

		mAdapter = new BorderWaitSouthboundFragment.BorderWaitAdapter(getActivity());
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

		swipeRefreshLayout.setRefreshing(true);

		viewModel = ViewModelProviders.of(this, viewModelFactory).get(BorderWaitViewModel.class);

		viewModel.init(BorderWaitViewModel.BorderDirection.SOUTHBOUND);

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
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();
				}
			}
		});

		viewModel.getBorderWaits().observe(this, borderWaits -> {
			mBorderWaits.clear();
			mBorderWaits = borderWaits;
			mAdapter.notifyDataSetChanged();
		});

		mEmptyView = root.findViewById( R.id.empty_list_view );

		return root;
	}

	/**
	 * Binds the custom ViewHolder class to it's data.
	 *
	 * @see RecyclerView.Adapter
	 */
	private class BorderWaitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private Context context;

		private List<BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH> mItems = new ArrayList<>();


		public BorderWaitAdapter(Context context) {
			super();
			this.context = context;
		}


		@Override
		public BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context).inflate(R.layout.borderwait_row, null);
			BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH viewholder = new BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH(view);
			view.setTag(viewholder);
			mItems.add(viewholder);
			return viewholder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

			BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH borderVH = (BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH) viewHolder;

            BorderWaitEntity waitItem = mBorderWaits.get(i);

			String title = waitItem.getTitle();
			String lane = waitItem.getLane();

			borderVH.tt.setText(title + " (" + lane + ")");
			borderVH.tt.setTypeface(tfb);

			String created_at = waitItem.getUpdated();
			borderVH.bt.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", true));
			borderVH.bt.setTypeface(tf);

			int wait = waitItem.getWait();
			if (wait == -1) {
				borderVH.rt.setText("N/A");
			} else if (wait < 5) {
				borderVH.rt.setText("< 5 min");
			} else {
				borderVH.rt.setText(wait + " min");
			}
			borderVH.rt.setTypeface(tfb);

			borderVH.iv.setImageResource(routeImage.get(waitItem.getRoute()));

            borderVH.star.setTag(waitItem.getBorderWaitId());
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            borderVH.star.setOnCheckedChangeListener(null);
            borderVH.star.setContentDescription("favorite");
            borderVH.star.setChecked(waitItem.getIsStarred() != 0);

            borderVH.star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
                    int waitId = (Integer) buttonView.getTag();

                    Snackbar added_snackbar = Snackbar
                            .make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                    Snackbar removed_snackbar = Snackbar
                            .make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

                    added_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("added to favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    removed_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("removed from favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    if (isChecked){
                        added_snackbar.show();
                    }else{
                        removed_snackbar.show();
                    }

                    viewModel.setIsStarredFor(waitId, isChecked ? 1 : 0);
                }
            });

		}

		@Override
		public int getItemCount() {
			return mBorderWaits.size();
		}

		// View Holder for list items.
		private class BorderWaitVH extends RecyclerView.ViewHolder {
			TextView tt;
			TextView bt;
			TextView rt;
			ImageView iv;
			CheckBox star;

			public BorderWaitVH(View view) {
				super(view);
				tt = view.findViewById(R.id.toptext);
				bt = view.findViewById(R.id.bottomtext);
				rt = view.findViewById(R.id.righttext);
				iv = view.findViewById(R.id.icon);
				star = view.findViewById(R.id.star_button);
			}
		}
	}

	public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
		viewModel.forceRefreshBorderWaits();
	}
}