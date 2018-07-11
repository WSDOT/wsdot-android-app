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

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateSignEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollTripEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByDirection;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByLocation;

public class SR167TollRatesFragment extends BaseFragment
		implements SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemSelectedListener,
        Injectable {
	
    private static final String TAG = SR167TollRatesFragment.class.getSimpleName();
	private static SR167TollRatesItemAdapter mAdapter;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

	protected RecyclerView mRecyclerView;
	protected LinearLayoutManager mLayoutManager;

    private Handler handler = new Handler();
    private Timer timer;

    private Spinner directionSpinner;
    public static ArrayList<CharSequence> spinnerOptions = new ArrayList<>();
    private int spinnerIndex = 0;

	private Tracker mTracker;

    private ArrayList<TollRateGroup> tollGroups = new ArrayList<>();

	@Inject
	ViewModelProvider.Factory viewModelFactory;
	TollRatesViewModel viewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
        spinnerOptions.clear();
        spinnerOptions.add(0, "Northbound");
        spinnerOptions.add(1, "Southbound");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_dynamic_toll_rates, null);

		mRecyclerView = root.findViewById(R.id.my_recycler_view);
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mAdapter = new SR167TollRatesItemAdapter(getActivity());
		mRecyclerView.setAdapter(mAdapter);

		mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

		mRecyclerView.setPadding(0,0,0,120);

		addDisclaimerView(root);

        directionSpinner = root.findViewById(R.id.fragment_spinner);

        ArrayAdapter<CharSequence> routeArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, spinnerOptions);
        routeArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        directionSpinner.setAdapter(routeArrayAdapter);
        directionSpinner.setOnItemSelectedListener(this);
        directionSpinner.setSelection(0, false);
        directionSpinner.setVisibility(View.VISIBLE);

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
		header_link.setText("Learn about tolling on SR 167");
		header_link.setTextColor(getResources().getColor(R.color.primary_default));
		header_link.setOnClickListener(v -> {
			Intent intent = new Intent();
			// GA tracker
			mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
			mTracker.setScreenName("/Toll Rates/Learn about SR-167");
			mTracker.send(new HitBuilders.ScreenViewBuilder().build());
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("https://www.wsdot.wa.gov/Tolling/SR167HotLanes/HOTtollrates.htm"));
			startActivity(intent);

		});

		viewModel = ViewModelProviders.of(this, viewModelFactory).get(TollRatesViewModel.class);

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
						TextView t = (TextView) mEmptyView;
						t.setText("toll rates unavailable.");
						mEmptyView.setVisibility(View.VISIBLE);
				}
			}
		});

		viewModel.getSR167TollRateItems().observe(this, tollRateGroups -> {
			if (tollRateGroups != null) {
				mEmptyView.setVisibility(View.GONE);
				Collections.sort(tollRateGroups, new SortTollGroupByLocation());
				Collections.sort(tollRateGroups, new SortTollGroupByDirection());
                tollGroups = new ArrayList<>(tollRateGroups);

                mAdapter.setData(filterTollsForDirection(String.valueOf(spinnerOptions.get(spinnerIndex).charAt(0))));
			}
		});

		viewModel.refresh();

		return root;
	}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerIndex = position;
        mAdapter.setData(filterTollsForDirection(String.valueOf(spinnerOptions.get(position).charAt(0))));
        mLayoutManager.scrollToPositionWithOffset(0, 0);
    }

    private ArrayList<TollRateGroup> filterTollsForDirection(String direction){

        ArrayList<TollRateGroup> filteredTolls = new ArrayList<>();

        for (TollRateGroup group: tollGroups) {
            if (group.tollRateSign.getTravelDirection().equals(direction)){
                filteredTolls.add(group);
            }
        }
        return filteredTolls;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

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
	 * Adds a toll rate accuracy disclaimer to the bottom of the view
	 * @param root
	 */
	private void addDisclaimerView(ViewGroup root) {
		FrameLayout frame = root.findViewById(R.id.list_container);
		TextView textView = new TextView(getContext());
		textView.setBackgroundColor(getResources().getColor(R.color.alerts));
		textView.setText("Estimated toll rates provided as a courtesy. You’ll always pay the toll you see on actual road signs when you enter.");
		textView.setPadding(15, 20, 15, 15);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		textView.setLayoutParams(params);
		frame.addView(textView);
	}

	/**
	 * Custom adapter for items in recycler view.
	 *
	 * Binds the custom ViewHolder class to it's data.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter
	 */
	private class SR167TollRatesItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private Context context;

		private ArrayList<TollRateGroup> mData = new ArrayList<>();

		private List<RecyclerView.ViewHolder> mItems = new ArrayList<>();

		public SR167TollRatesItemAdapter(Context context) {
			this.context = context;
		}

		public void setData(ArrayList<TollRateGroup> data){
			mData = data;
			this.notifyDataSetChanged();
		}

		@Override
		public SR167TollRatesItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_time_group, null);
			SR167TollRatesItemAdapter.ViewHolder viewholder = new SR167TollRatesItemAdapter.ViewHolder(view);
			view.setTag(viewholder);
			mItems.add(viewholder);
			return viewholder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

			SR167TollRatesItemAdapter.ViewHolder viewholder = (SR167TollRatesItemAdapter.ViewHolder) viewHolder;

			TollRateGroup tollRateGroup = mData.get(position);

			final String id = tollRateGroup.tollRateSign.getId();

            String title = "Lane entrance near ".concat(tollRateGroup.tollRateSign.getLocationName());

			viewholder.title.setText(title);
			viewholder.title.setTypeface(tfb);

			viewholder.travel_times_layout.removeAllViews();

			// make a trip view with toll rate for each trip in the group
			for (TollTripEntity trip: tollRateGroup.trips) {

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

				if (isChecked){
					added_snackbar.show();
				}else{
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
	public static View makeTripView(TollTripEntity tripItem, TollRateSignEntity sign, Context context) {

		Typeface tfb = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
		LayoutInflater li = LayoutInflater.from(context);
		View cv = li.inflate(R.layout.trip_view, null);

        cv.findViewById(R.id.title).setVisibility(View.GONE);

		((TextView) cv.findViewById(R.id.subtitle)).setText("Show on map");
		((TextView) cv.findViewById(R.id.subtitle)).setTextColor(context.getResources().getColor(R.color.primary_default));
		cv.findViewById(R.id.subtitle).setOnClickListener(v -> {
			Bundle b = new Bundle();

			b.putDouble("startLat", sign.getStartLatitude());
			b.putDouble("startLong", sign.getStartLongitude());

			b.putDouble("endLat", tripItem.getEndLatitude());
			b.putDouble("endLong", tripItem.getEndLongitude());

			b.putString("title", "Lane entrance near ".concat(sign.getLocationName()));
			b.putString("text", String.format("Exits near %s", tripItem.getEndLocationName()));

			Intent intent = new Intent(context, TollRatesRouteActivity.class);
			intent.putExtras(b);
			context.startActivity(intent);
		});

        ((TextView) cv.findViewById(R.id.content)).setText("Carpools and motorcycles free");

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
