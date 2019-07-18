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

package gov.wa.wsdot.android.wsdot.ui.ferries;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerriesRouteSchedulesDayDeparturesActivity;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = FerriesRouteSchedulesFragment.class.getSimpleName();
	private static RouteSchedulesAdapter mAdapter;
	private TextView mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private List<FerryScheduleEntity> mSchedule = new ArrayList<>();

	private static FerrySchedulesViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RouteSchedulesAdapter(getActivity());
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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FerrySchedulesViewModel.class);

        viewModel.getResourceStatus().observe(getViewLifecycleOwner(), resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        if (mAdapter.getItemCount() > 0) {
                            mEmptyView.setVisibility(View.GONE);
                        }
                        break;
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getFerrySchedules().observe(getViewLifecycleOwner(), schedules -> {
            mSchedule = schedules;
            mAdapter.notifyDataSetChanged();
            if (mSchedule.size() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        return root;
	}

	/**
	 * Custom adapter for items in recycler view.
	 *
	 * Binds the custom ViewHolder class to it's data.
	 *
	 * @see RecyclerView.Adapter
	 */
	private class RouteSchedulesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;

        public RouteSchedulesAdapter(Context context) {
            this.context = context;
        }

		@Override
		public FerryScheduleVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_with_star, null);
            FerryScheduleVH viewholder = new FerryScheduleVH(view);
			view.setTag(viewholder);
            return viewholder;
		}

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            FerryScheduleVH holder = (FerryScheduleVH) viewHolder;

            FerryScheduleEntity schedule = mSchedule.get(position);

            holder.title.setText(schedule.getTitle());
            holder.title.setTypeface(tfb);

            String text = schedule.getCrossingTime();

            // Set onClickListener for holder's view
            holder.view.setOnClickListener(
                    v -> {

                        Bundle b = new Bundle();
                        Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDayDeparturesActivity.class);
                        b.putInt("scheduleId", schedule.getFerryScheduleId());
                        b.putString("title", schedule.getTitle());
                        intent.putExtras(b);

                        startActivity(intent);
                    }
            );

            try {
                if (text.equalsIgnoreCase("null")) {
                    holder.text.setText("");
                } else {
                    holder.text.setText("Crossing Time: ~ " + text + " min");
                    holder.text.setTypeface(tf);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            String created_at = schedule.getUpdated();

            // Try to read the created at field in the old format,
            // it that fails, assume we are using the new format.
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
                holder.created_at.setText(ParserUtils.relativeTime(
                    dateFormat.format(new Date(Long.parseLong(created_at.substring(6, 19)))),
                    "MMMM d, yyyy h:mm a",
                    false));

            } catch (Exception e) {
                holder.created_at.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", false));
            }

            holder.created_at.setTypeface(tf);

            holder.star_button.setTag(schedule.getFerryScheduleId());
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            holder.star_button.setOnCheckedChangeListener(null);
            holder.star_button.setContentDescription("favorite");
            holder.star_button
                    .setChecked(schedule.getIsStarred() != 0);
            holder.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
                    int scheduleId = (Integer) buttonView.getTag();

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

                    viewModel.setIsStarredFor(scheduleId, isChecked ? 1 : 0);
                }
            });

            holder.alert_button.setVisibility(View.GONE);

        }

        @Override
        public int getItemCount() {
            return mSchedule.size();
        }

        // View Holder for ferry Schedule list items.
        private class FerryScheduleVH extends RecyclerView.ViewHolder{
            TextView title;
            TextView text;
            TextView created_at;
            CheckBox star_button;
            ImageButton alert_button;
            public View view;

            public FerryScheduleVH(View v) {
                super(v);
                view = v;
                title = v.findViewById(R.id.title);
                text = v.findViewById(R.id.text);
                created_at = v.findViewById(R.id.created_at);
                star_button = v.findViewById(R.id.star_button);
                alert_button =v.findViewById(R.id.alert_button);

            }
        }
    }

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.forceRefreshFerrySchedules();
    }
}
