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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerrySchedulesViewModel;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesDayDeparturesFragment extends BaseFragment
        implements AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = FerriesRouteSchedulesDayDeparturesFragment.class.getSimpleName();

    private Handler mHandler = new Handler();
    private static DepartureTimesAdapter mAdapter;

	private static Typeface tf;
	private static Typeface tfb;

	private static SwipeRefreshLayout swipeRefreshLayout;
    private View mEmptyView;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private static FerrySchedulesViewModel scheduleViewModel;
    private static FerryTerminalViewModel terminalViewModel;

    private static ArrayList<FerriesScheduleDateItem> mScheduleDateItems;
    private static FerriesTerminalItem terminalItem;
    private static ArrayList<FerriesAnnotationsItem> annotations;

    private static int mScheduleId;
    private static int mTerminalIndex;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        DateFormat dateFormat = new SimpleDateFormat("EEEE");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        Bundle args = getActivity().getIntent().getExtras();

        mScheduleId = args.getInt("scheduleId");
        mTerminalIndex = args.getInt("terminalIndex");

	}
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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

        scheduleViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerrySchedulesViewModel.class);
        terminalViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerryTerminalViewModel.class);

        scheduleViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        break;
                    case ERROR:
                        TextView t = (TextView) mEmptyView;
                        t.setText(R.string.no_day_departures);
                }
            }
        });

        terminalViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this.getContext(), "connection error, can't update spaces", Toast.LENGTH_SHORT).show();
                }
            }
        });

        scheduleViewModel.getFerryScheduleFor(mScheduleId).observe(this, schedule -> {
            if (schedule != null) {
                scheduleViewModel.loadDatesWithSailingsFromJson(schedule.getDate());
            }
        });

        scheduleViewModel.getDatesWithSailings().observe(this, dates -> {
            if (dates != null) {
                mScheduleDateItems = new ArrayList<>(dates);

                initDaySpinner();
                terminalItem = mScheduleDateItems.get(0).getFerriesTerminalItem().get(mTerminalIndex);

                terminalViewModel.loadDepartureTimesForTerminal(terminalItem);
            }
        });

        terminalViewModel.getDepartureTimes().observe(this, sailingTimes -> {
            if (sailingTimes != null ){
                if (sailingTimes.size() != 0) {
                    mEmptyView.setVisibility(View.GONE);
                } else {
                    TextView t = (TextView) mEmptyView;
                    t.setText(R.string.no_day_departures);
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                mAdapter.setData(new ArrayList<>(sailingTimes));
            }
        });

        terminalViewModel.getDepartureTimesAnnotations().observe(this, sailingAnnotations -> {
            if (sailingAnnotations != null) {
                annotations = new ArrayList<>(sailingAnnotations);
                mAdapter.notifyDataSetChanged();
            }
        });

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
            Log.e(TAG, "Runnable!");
            terminalViewModel.forceRefreshTerminalSpaces();
            mHandler.postDelayed(runnable, (DateUtils.MINUTE_IN_MILLIS)); // Check every minute.
        }
    };

	private void initDaySpinner(){

        ArrayList<CharSequence> mDaysOfWeek = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat("EEEE");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        int numDates = mScheduleDateItems.size();
        for (int i = 0; i < numDates; i++) {
            if (!mScheduleDateItems.get(i).getFerriesTerminalItem().isEmpty()) {
                mDaysOfWeek.add(dateFormat.format(new Date(
                        Long.parseLong(mScheduleDateItems.get(i).getDate()))));
            }
        }

        // Set up custom spinner
        Spinner daySpinner = getActivity().findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> dayOfWeekArrayAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.simple_spinner_item_white, mDaysOfWeek);;
        dayOfWeekArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_white);
        daySpinner.setAdapter(dayOfWeekArrayAdapter);
        daySpinner.setOnItemSelectedListener(this);
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class DepartureTimesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private List<FerriesScheduleTimesItem> items;

        public DepartureTimesAdapter(Context context, List<FerriesScheduleTimesItem> data) {
            this.items = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView;

            if (viewType == TYPE_HEADER) {
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.list_item_departure_times_header, parent, false);

                return new TitleViewHolder(itemView);

            }else if (viewType == TYPE_ITEM){
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.list_item_departure_times, parent, false);
                return new TimesViewHolder(itemView);
            }else{
                throw new RuntimeException("There is no view type that matches the type: " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

            TimesViewHolder itemHolder;
            TitleViewHolder titleHolder;

            if(holder instanceof TitleViewHolder){

                titleHolder = (TitleViewHolder) holder;

                titleHolder.Arriving.setTypeface(tfb);
                titleHolder.Departing.setTypeface(tfb);

                // Accessibility
                titleHolder.itemView.setContentDescription("departures and arrivals heading");
            }else {

                FerriesScheduleTimesItem item = getItem(position);

                itemHolder = (TimesViewHolder) holder;

                StringBuilder contentDescriptionBuilder = new StringBuilder();

                String annotation = "";

                int numIndexes = item.getAnnotationIndexes().size();

                itemHolder.departing.setText(dateFormat.format(new Date(Long.parseLong(item.getDepartingTime()))));
                contentDescriptionBuilder.append("departing at ");
                contentDescriptionBuilder.append(itemHolder.departing.getText());
                contentDescriptionBuilder.append(". ");

                if (!item.getArrivingTime().equals("N/A")) {
                    itemHolder.arriving.setText(dateFormat.format(new Date(Long.parseLong(item.getArrivingTime()))));
                    contentDescriptionBuilder.append("arriving at");
                    contentDescriptionBuilder.append(itemHolder.departing.getText());
                    contentDescriptionBuilder.append(". ");
                }

                for (int i = 0; i < numIndexes; i++) {
                    FerriesAnnotationsItem p = annotations.get(item.getAnnotationIndexes().get(i).getIndex());
                    annotation += p.getAnnotation();
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
        }

        @Override
        public int getItemCount() {
            if (items != null) {
                return items.size() + 1;
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
            return items.get(position - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }
    }

    public static class TimesViewHolder extends RecyclerView.ViewHolder {
        protected TextView departing;
        protected TextView arriving;
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

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView Departing;
        protected TextView Arriving;

        public TitleViewHolder(View itemView) {
            super(itemView);
            Departing = itemView.findViewById(R.id.departing_title);
            Departing.setTypeface(tfb);
            Arriving = itemView.findViewById(R.id.arriving_title);
            Arriving.setTypeface(tfb);
        }
    }

    public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
        terminalViewModel.forceRefreshTerminalSpaces();
    }

    /**
     * Callback for schedule day picker. Gets the terminal item for the selected day and
     * requests the departure times for that day from the view model.
     */
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        terminalItem = mScheduleDateItems.get(position).getFerriesTerminalItem().get(mTerminalIndex);
        terminalViewModel.loadDepartureTimesForTerminal(terminalItem);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }
}
