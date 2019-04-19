/*
 * Copyright (c) 2018 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroupDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateSignEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByDirection;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByLocation;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByMilepost;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollTripsByMilepost;

public class I405TollRatesFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener,
            Injectable {
	
    private static final String TAG = I405TollRatesFragment.class.getSimpleName();

    private static I405TollRatesItemAdapter mAdapter;
    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private Handler handler = new Handler();
    private Timer timer;

    private RadioGroup directionRadioGroup;
    private int radioGroupDirectionIndex = 0;

    private ArrayList<TollRateGroup> tollGroups = new ArrayList<>();
    private ArrayList<TravelTimeEntity> travelTimes = new ArrayList<>();

	@Inject
	ViewModelProvider.Factory viewModelFactory;
	TollRatesViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_dynamic_toll_rates, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new I405TollRatesItemAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        directionRadioGroup = root.findViewById(R.id.segment_control);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        radioGroupDirectionIndex = sharedPref.getInt(getString(R.string.toll_rates_405_travel_direction_key), 0);

        if (radioGroupDirectionIndex == 0) {
            RadioButton leftSegment = root.findViewById(R.id.radio_left);
            leftSegment.setChecked(true);
        } else {
            RadioButton rightSegment = root.findViewById(R.id.radio_right);
            rightSegment.setChecked(true);
        }

        directionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            RadioButton selectedDirection = directionRadioGroup.findViewById(checkedId);

            mAdapter.setData(filterTollsForDirection(String.valueOf(selectedDirection.getText().charAt(0))));

            mLayoutManager.scrollToPositionWithOffset(0, 0);
            SharedPreferences sharedPref1 = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPref1.edit();

            radioGroupDirectionIndex = directionRadioGroup.indexOfChild(selectedDirection);

            TextView travelTimeView = root.findViewById(R.id.travel_time_text);
            travelTimeView.setText(getTravelTimeStringForDirection(radioGroupDirectionIndex == 0 ? "N" : "S"));

            editor.putInt(getString(R.string.toll_rates_405_travel_direction_key), radioGroupDirectionIndex);
            editor.apply();

        });

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

        TextView header_link = root.findViewById(R.id.header_text);

        // create spannable string for underline
        SpannableString content = new SpannableString(getActivity().getResources().getString(R.string.i405_info_link));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        header_link.setText(content);

        header_link.setTextColor(getResources().getColor(R.color.primary_default));
        header_link.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.wsdot.wa.gov/Tolling/405/rates.htm"));
            startActivity(intent);
        });

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TollRatesViewModel.class);

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
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getTravelTimesStatus().observe(getViewLifecycleOwner(), resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        root.findViewById(R.id.travel_time_text).setVisibility(View.VISIBLE);
                        break;
                    case ERROR:
                        root.findViewById(R.id.travel_time_text).setVisibility(View.GONE);
                }
            }
        });

        viewModel.getI405TollRateItems().observe(getViewLifecycleOwner(), tollRateGroups -> {
            if (tollRateGroups != null) {

                mEmptyView.setVisibility(View.GONE);

                Collections.sort(tollRateGroups, new SortTollGroupByLocation());
                Collections.sort(tollRateGroups, new SortTollGroupByDirection());

                tollGroups = new ArrayList<>(tollRateGroups);

                directionRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedDirection = directionRadioGroup.findViewById(directionRadioGroup.getCheckedRadioButtonId());

                mAdapter.setData(filterTollsForDirection(String.valueOf(selectedDirection.getText().charAt(0))));
            }
        });

        viewModel.getTravelTimesForETLFor("405").observe(getViewLifecycleOwner(), travelTimes -> {

            TextView travelTimeView = root.findViewById(R.id.travel_time_text);

            if (travelTimes.size() > 0) {
                travelTimeView.setVisibility(View.VISIBLE);

                this.travelTimes = new ArrayList<>(travelTimes);

                travelTimeView.setText(getTravelTimeStringForDirection(radioGroupDirectionIndex == 0 ? "N" : "S"));
            } else {
                travelTimeView.setVisibility(View.GONE);
            }
        });

        timer = new Timer();
        timer.schedule(new RatesTimerTask(), 0, 60000); // Schedule rates to update every 60 seconds

        return root;
    }

    private ArrayList<TollRateGroup> filterTollsForDirection(String direction){

        ArrayList<TollRateGroup> filteredTolls = new ArrayList<>();

        for (TollRateGroup group: tollGroups) {
            if (group.tollRateSign.getTravelDirection().equals(direction)){

                if (direction.equals("S")) {
                    Collections.sort(group.trips, new SortTollTripsByMilepost(SortTollTripsByMilepost.SortOrder.DESCENDING));
                } else if (direction.equals("N")) {
                    Collections.sort(group.trips, new SortTollTripsByMilepost(SortTollTripsByMilepost.SortOrder.ASCENDING));
                }

                filteredTolls.add(group);
            }
        }

        if (direction.equals("S")) {
            Collections.sort(filteredTolls, new SortTollGroupByMilepost(SortTollGroupByMilepost.SortOrder.DESCENDING));
        } else if (direction.equals("N")) {
            Collections.sort(filteredTolls, new SortTollGroupByMilepost(SortTollGroupByMilepost.SortOrder.ASCENDING));
        }

        return filteredTolls;
    }

    private String getTravelTimeStringForDirection(String direction){

        int[] timeIDs = new int[2];

        if (direction.equals("N")) {
            timeIDs[0] = 35; // GP
            timeIDs[1] = 36; // ETL
        } else if (direction.equals("S")) {
            timeIDs[0] = 38; // GP
            timeIDs[1] = 37; // ETL
        }

        // array holds indexes for travelTimes in north or southbound direction to build string with
        int[] timeIndexes = new int[2];

        for (TravelTimeEntity time: travelTimes) {
            if (time.getTravelTimeId() == timeIDs[0]) {
                timeIndexes[0] = travelTimes.indexOf(time);
            } else if (time.getTravelTimeId() == timeIDs[1]) {
                timeIndexes[1] = travelTimes.indexOf(time);
            }
        }

        return String.format("%s: %s min%s or %s min%s via ETL",
                travelTimes.get(timeIndexes[0]).getTripTitle(),
                travelTimes.get(timeIndexes[0]).getCurrent(),
                (travelTimes.get(timeIndexes[0]).getCurrent() > 1 ? "s" : ""),
                travelTimes.get(timeIndexes[1]).getCurrent(),
                (travelTimes.get(timeIndexes[1]).getCurrent() > 1 ? "s" : ""));

    }

    public class RatesTimerTask extends TimerTask {
        private Runnable runnable = new Runnable() {
            public void run() {
                viewModel.refresh();
            }
        };

        public void run() {
            handler.post(runnable);
        }
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see RecyclerView.Adapter
     */
    private class I405TollRatesItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;

        private ArrayList<TollRateGroup> mData = new ArrayList<>();

        private List<RecyclerView.ViewHolder> mItems = new ArrayList<>();

        public I405TollRatesItemAdapter(Context context) {
            this.context = context;
        }

        public void setData(ArrayList<TollRateGroup> data){
            mData = data;
            this.notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_time_group, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            ViewHolder viewholder = (ViewHolder) viewHolder;

            TollRateGroup tollRateGroup = mData.get(position);

            final String id = tollRateGroup.tollRateSign.getId();

            String title = tollRateGroup.tollRateSign.getLocationName();
            viewholder.title.setText(title);
            viewholder.title.setTypeface(tfb);

            viewholder.travel_times_layout.removeAllViews();

            // make a trip view with toll rate for each trip in the group
            for (TollRateGroupDao.TollTripEntity trip: tollRateGroup.trips) {

                View tripView = makeTripView(trip, tollRateGroup.tollRateSign, getContext());

                // remove the line from the last trip
                if (tollRateGroup.trips.indexOf(trip) == tollRateGroup.trips.size() - 1){
                    tripView.findViewById(R.id.line).setVisibility(View.GONE);
                }

                viewholder.travel_times_layout.addView(tripView);
            }

            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            viewholder.star_button.setOnCheckedChangeListener(null);
            viewholder.star_button
                    .setChecked(tollRateGroup.tollRateSign.getIsStarred() != 0);

            viewholder.star_button.setOnCheckedChangeListener((buttonView, isChecked) -> {

                Snackbar added_snackbar = Snackbar
                        .make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                Snackbar removed_snackbar = Snackbar
                        .make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

                if (isChecked) {
                    added_snackbar.show();
                }else {
                    removed_snackbar.show();
                }

                viewModel.setIsStarredFor(id, isChecked ? 1 : 0);
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout travel_times_layout;
            public TextView title;
            public CheckBox star_button;

            public ViewHolder(View view) {
                super(view);
                travel_times_layout = view.findViewById(R.id.travel_times_linear_layout);
                title = view.findViewById(R.id.title);
                star_button = view.findViewById(R.id.star_button);
            }
        }
    }

    /**
     * Returns a view for the toll trip provided by the tripItem.
     * A tripItem holds information about the trip destination and toll rate
     *
     * @param tripItem
     * @param context
     * @return
     */
    public static View makeTripView(TollRateGroupDao.TollTripEntity tripItem, TollRateSignEntity sign, Context context) {

        Typeface tfb = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        LayoutInflater li = LayoutInflater.from(context);
        View cv = li.inflate(R.layout.trip_view, null);

        // set end location label
        ((TextView) cv.findViewById(R.id.title)).setText("to ".concat(tripItem.getEndLocationName()));

        ((TextView) cv.findViewById(R.id.subtitle)).setText("Show on map");
        ((TextView) cv.findViewById(R.id.subtitle)).setTextColor(context.getResources().getColor(R.color.primary_default));
        cv.findViewById(R.id.subtitle).setOnClickListener(v -> {
            Bundle b = new Bundle();

            b.putDouble("startLat", sign.getStartLatitude());
            b.putDouble("startLong", sign.getStartLongitude());

            b.putDouble("endLat", tripItem.getEndLatitude());
            b.putDouble("endLong", tripItem.getEndLongitude());

            b.putString("title", sign.getLocationName());
            b.putString("text", String.format("Travel as far as %s.", tripItem.getEndLocationName()));

            Intent intent = new Intent(context, TollRatesRouteActivity.class);
            intent.putExtras(b);
            context.startActivity(intent);
        });

        cv.findViewById(R.id.content).setVisibility(View.GONE);

        // set updated label
        ((TextView) cv.findViewById(R.id.updated)).setText(ParserUtils.relativeTime(
                tripItem.getUpdated(),
                "MMMM d, yyyy h:mm a",
                false));

        // set toll
        TextView currentTimeTextView = cv.findViewById(R.id.current_value);
        currentTimeTextView.setTypeface(tfb);
        currentTimeTextView.setText(String.format(Locale.US, "$%.2f", tripItem.getTollRate()/100));

        // set message if there is one
        if (!tripItem.getMessage().equals("null")){
            currentTimeTextView.setText(tripItem.getMessage());
        }

        return cv;
    }

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.refresh();
    }
}