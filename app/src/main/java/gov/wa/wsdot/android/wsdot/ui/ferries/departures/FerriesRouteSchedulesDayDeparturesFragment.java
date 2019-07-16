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

package gov.wa.wsdot.android.wsdot.ui.ferries.departures;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesDayDeparturesFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = FerriesRouteSchedulesDayDeparturesFragment.class.getSimpleName();

    private Handler mHandler = new Handler();
    private static DepartureTimesAdapter mAdapter;

	private static Typeface tf;
	private static Typeface tfb;

	private SwipeRefreshLayout swipeRefreshLayout;
    private View mEmptyView;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private static FerryTerminalViewModel terminalViewModel;

    // get the schedules view model so we can refresh the whole schedule
    private static FerryScheduleViewModel scheduleViewModel;

    // private static FerriesTerminalItem mTerminalItem;
    private static ArrayList<FerriesAnnotationsItem> annotations;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_route_schedules_day_departures, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DepartureTimesAdapter(getActivity(), null);

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
        
        mEmptyView = root.findViewById(R.id.empty_list_view);

        scheduleViewModel = ViewModelProviders.of(getActivity()).get(FerryScheduleViewModel.class);
        terminalViewModel = ViewModelProviders.of(getActivity()).get(FerryTerminalViewModel.class);

        terminalViewModel.getResourceStatus().observe(getViewLifecycleOwner(), resourceStatus -> {
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Toast.makeText(this.getContext(), "connection error, can't update spaces", Toast.LENGTH_SHORT).show();
                        }
                }
            }
        });

        terminalViewModel.getDepartureTimes().observe(getViewLifecycleOwner(), sailingTimes -> {

            if (sailingTimes != null ) {
                if (sailingTimes.size() != 0) {
                    mEmptyView.setVisibility(View.GONE);
                } else {
                    TextView t = (TextView) mEmptyView;
                    t.setText(R.string.no_day_departures);
                    mEmptyView.setVisibility(View.VISIBLE);
                }

                mAdapter.setData(new ArrayList<>(sailingTimes));

                // Scroll to the first sailing time that hasn't already passed.
                if (terminalViewModel.getShouldScrollToCurrent()) {
                    terminalViewModel.setScrollToCurrent(false);
                    try {
                        Date now = new Date();
                        for (int i = 0; i < sailingTimes.size(); i++) {
                            if (now.before(sailingTimes.get(i).getDepartingTime())) {
                                mRecyclerView.stopScroll();
                                ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                                i = sailingTimes.size();
                            }
                        }
                    } catch (Exception e) {
                        MyLogger.crashlyticsLog("Ferries", "Error", "Auto scroll failed", 1);
                    }
                }
            }
        });

        terminalViewModel.getDepartureTimesAnnotations().observe(getViewLifecycleOwner(), sailingAnnotations -> {
            if (sailingAnnotations != null) {
                annotations = new ArrayList<>(sailingAnnotations);
                mAdapter.notifyDataSetChanged();
            } else {
                annotations.clear();
            }
        });

        terminalViewModel.forceRefreshVesselStatus();

        return root;
	}

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(runnable, (DateUtils.MINUTE_IN_MILLIS)); // Check every minute.
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(runnable);
    }

    // Runnable for updating sailing spaces
    private Runnable runnable = new Runnable() {
        public void run() {
            terminalViewModel.forceRefreshTerminalSpacesAndVessel();
            mHandler.postDelayed(runnable, (DateUtils.MINUTE_IN_MILLIS)); // Check every minute.
        }
    };

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see RecyclerView.Adapter
     */
    private class DepartureTimesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ITEM = 1;
        private List<FerriesScheduleTimesItem> items;

        public DepartureTimesAdapter(Context context, List<FerriesScheduleTimesItem> data) {
            this.items = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == TYPE_ITEM){
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.list_item_departure_times, parent, false);
                return new TimesViewHolder(itemView);
            } else {
                throw new RuntimeException("There is no view type that matches the type: " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            DateFormat dateFormat = new SimpleDateFormat("h:mm a");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

            TimesViewHolder itemHolder;

            FerriesScheduleTimesItem item = getItem(position);

            itemHolder = (TimesViewHolder) holder;

            Date now = new Date();

            if (item.getDepartingTime() != null) {
                if (now.after(item.getDepartingTime())) {
                    itemHolder.departing.setTextColor(getResources().getColor(R.color.semi_white));
                    itemHolder.arriving.setTextColor(getResources().getColor(R.color.semi_white));
                    itemHolder.annotation.setTextColor(getResources().getColor(R.color.semi_white));

                } else {
                    itemHolder.departing.setTextColor(getResources().getColor(R.color.body_text_1));
                    itemHolder.arriving.setTextColor(getResources().getColor(R.color.body_text_1));
                    itemHolder.annotation.setTextColor(getResources().getColor(R.color.body_text_1));
                }
            }

            StringBuilder contentDescriptionBuilder = new StringBuilder();

            String annotation = "";

            if (item.getDepartingTime() != null) {
                itemHolder.departing.setText(dateFormat.format(item.getDepartingTime()));
                contentDescriptionBuilder.append("departing at ");
                contentDescriptionBuilder.append(itemHolder.departing.getText());
                contentDescriptionBuilder.append(". ");
            } else {
                itemHolder.departing.setText("Unavailable");
            }

            if (item.getArrivingTime() != null) {
                itemHolder.arriving.setText(dateFormat.format(item.getArrivingTime()));
                contentDescriptionBuilder.append("arriving at");
                contentDescriptionBuilder.append(itemHolder.departing.getText());
                contentDescriptionBuilder.append(". ");
            }

            int numIndexes = item.getAnnotationIndexes().size();

            for (int i = 0; i < numIndexes; i++) {
                if (annotations.size() > item.getAnnotationIndexes().get(i).getIndex()) {
                    FerriesAnnotationsItem p = annotations.get(item.getAnnotationIndexes().get(i).getIndex());
                    annotation += p.getAnnotation();
                }
            }

            // only show actualDeapture if viewing today's sailings
            if (item.getActualDeparture() != null && DateUtils.isToday(item.getDepartingTime().getTime())) {
                itemHolder.actualDeparture.setText(String.format("Actual departure\n%s", item.getActualDeparture()));
            } else {
                itemHolder.actualDeparture.setText("");
            }

            if (item.getEta() != null && DateUtils.isToday(item.getDepartingTime().getTime())) {
                itemHolder.eta.setText(String.format("ETA %s", item.getEta()));
            } else {
                itemHolder.eta.setText("");
            }

            if (annotation.equals("")) {
                itemHolder.annotation.setVisibility(View.GONE);
            } else {
                itemHolder.annotation.setVisibility(View.VISIBLE);
                contentDescriptionBuilder.append(annotation);
                contentDescriptionBuilder.append(". ");
            }

            itemHolder.annotation.setText(android.text.Html.fromHtml(annotation));

            if (item.getDriveUpSpaceCount() != -1) {
                itemHolder.vehicleSpaceGroup.setVisibility(View.VISIBLE);
                itemHolder.driveUpProgressBar.setMax(item.getMaxSpaceCount());
                itemHolder.driveUpProgressBar.setProgress(item.getMaxSpaceCount() - item.getDriveUpSpaceCount());
                itemHolder.driveUpProgressBar.setSecondaryProgress(item.getMaxSpaceCount());
                itemHolder.driveUpSpaceCount.setVisibility(View.VISIBLE);
                itemHolder.driveUpSpaceCount.setText(Integer.toString(item.getDriveUpSpaceCount()));
                itemHolder.driveUpSpaces.setVisibility(View.VISIBLE);
                itemHolder.driveUpSpacesDisclaimer.setVisibility(View.VISIBLE);
                itemHolder.updated.setVisibility(View.VISIBLE);
                itemHolder.updated.setText(ParserUtils.relativeTime(item.getLastUpdated(), "MMMM d, yyyy h:mm a", false));
                contentDescriptionBuilder.append(itemHolder.driveUpSpaceCount.getText());
                contentDescriptionBuilder.append(" drive-up spaces. ");
                contentDescriptionBuilder.append(itemHolder.driveUpSpacesDisclaimer.getText());
                contentDescriptionBuilder.append(". Drive-up spaces updated ");
                contentDescriptionBuilder.append(itemHolder.updated.getText());
            } else {
                itemHolder.vehicleSpaceGroup.setVisibility(View.GONE);
                itemHolder.driveUpSpaceCount.setVisibility(View.GONE);
                itemHolder.driveUpSpaces.setVisibility(View.GONE);
                itemHolder.driveUpSpacesDisclaimer.setVisibility(View.GONE);
                itemHolder.updated.setVisibility(View.GONE);
            }
            itemHolder.itemView.setContentDescription(contentDescriptionBuilder.toString());
        }

        @Override
        public int getItemCount() {
            if (items != null) {
                return items.size();
            }
            return 0;
        }

        public void setData(ArrayList<FerriesScheduleTimesItem> data) {
            if(data != null) {
                items = data;
            }else{
                items = null;
            }
            notifyDataSetChanged();
        }

        public void clear() {
            if (items != null) {
                this.items.clear();
            }
            notifyDataSetChanged();
        }

        private FerriesScheduleTimesItem getItem(int position){
            return items.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_ITEM;
        }

    }

    public static class TimesViewHolder extends RecyclerView.ViewHolder {
        protected TextView departing;
        protected TextView arriving;
        protected TextView actualDeparture;
        protected TextView eta;
        protected TextView annotation;
        protected RelativeLayout vehicleSpaceGroup;
        protected ProgressBar driveUpProgressBar;
        protected TextView driveUpSpaceCount;
        protected TextView driveUpSpaces;
        protected TextView driveUpSpacesDisclaimer;
        protected TextView updated;

        public TimesViewHolder(View itemView) {
            super(itemView);
            departing = itemView.findViewById(R.id.departing);
            departing.setTypeface(tfb);
            arriving = itemView.findViewById(R.id.arriving);
            arriving.setTypeface(tfb);
            actualDeparture = itemView.findViewById(R.id.actualDeparture);
            actualDeparture.setTypeface(tf);
            eta = itemView.findViewById(R.id.eta);
            eta.setTypeface(tf);
            annotation = itemView.findViewById(R.id.annotation);
            annotation.setTypeface(tf);
            vehicleSpaceGroup = itemView.findViewById(R.id.driveUpProgressBarGroup);
            driveUpProgressBar = itemView.findViewById(R.id.driveUpProgressBar);
            driveUpSpaceCount = itemView.findViewById(R.id.driveUpSpaceCount);
            driveUpSpaceCount.setTypeface(tf);
            driveUpSpaces = itemView.findViewById(R.id.driveUpSpaces);
            driveUpSpaces.setTypeface(tf);
            driveUpSpacesDisclaimer = itemView.findViewById(R.id.driveUpSpacesDisclaimer);
            driveUpSpacesDisclaimer.setTypeface(tf);
            updated = itemView.findViewById(R.id.updated);
            updated.setTypeface(tf);
        }
    }

    public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
		scheduleViewModel.forceRefreshFerrySchedules();
        terminalViewModel.forceRefreshTerminalSpacesAndVessel();
    }

}
