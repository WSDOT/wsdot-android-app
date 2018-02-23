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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerrySchedulesViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.bulletins.FerriesRouteAlertsBulletinsActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.sailings.FerriesRouteSchedulesDaySailingsActivity;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = FerriesRouteSchedulesFragment.class.getSimpleName();
	private static RouteSchedulesAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private List<FerryScheduleEntity> mSchedule = new ArrayList<>();

	private Tracker mTracker;

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
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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
        viewModel.init(null);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
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
                        if (mAdapter.getItemCount() > 0){
                            mEmptyView.setVisibility(View.GONE);
                        }
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getFerrySchedules().observe(this, schedules -> {
            mSchedule = schedules;
            mAdapter.notifyDataSetChanged();
        });

        return root;
	}

	/**
	 * Custom adapter for items in recycler view.
	 *
	 * Binds the custom ViewHolder class to it's data.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter
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
                        Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDaySailingsActivity.class);
                        b.putInt("id", schedule.getFerryScheduleId());
                        b.putString("title", schedule.getTitle());
                        b.putString("date", schedule.getDate());
                        b.putInt("isStarred", schedule.getIsStarred());
                        intent.putExtras(b);

                        // GA tracker
                        mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Ferries")
                                .setAction("Schedules")
                                .setLabel(schedule.getTitle())
                                .build());

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
            holder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
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

            String alerts = schedule.getAlert();

            if (alerts.equals("[]")) {
                holder.alert_button.setVisibility(View.GONE);
            } else {
                holder.alert_button.setVisibility(View.VISIBLE);
                holder.alert_button.setTag(position);
                holder.alert_button.setImageResource(R.drawable.btn_alert_on);
                holder.alert_button.setContentDescription("Route has active alerts");
                holder.alert_button.setOnClickListener(v -> {
                    Bundle b = new Bundle();
                    Intent intent = new Intent(getActivity(), FerriesRouteAlertsBulletinsActivity.class);
                    b.putInt("routeId", schedule.getFerryScheduleId());
                    b.putString("title", schedule.getTitle());
                    intent.putExtras(b);
                    startActivity(intent);
                });
            }
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
