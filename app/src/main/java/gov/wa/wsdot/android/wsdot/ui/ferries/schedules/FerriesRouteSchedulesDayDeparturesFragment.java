/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesTerminalSailingSpace;
import gov.wa.wsdot.android.wsdot.service.FerriesTerminalSailingSpaceSyncService;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.tollrates.SR520TollRatesFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesDayDeparturesFragment extends BaseFragment
        implements LoaderCallbacks<ArrayList<FerriesScheduleTimesItem>>,
        AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FerriesRouteSchedulesDayDeparturesFragment.class.getSimpleName();
	private static FerriesTerminalItem terminalItem;
	private static ArrayList<FerriesAnnotationsItem> annotations;
	private static ArrayList<FerriesScheduleTimesItem> times;
	private static DepartureTimesAdapter mAdapter;
	private Typeface tf;
	private Typeface tfb;
	private static SwipeRefreshLayout swipeRefreshLayout;
	private FerriesTerminalSyncReceiver ferriesTerminalSyncReceiver;
    private View mEmptyView;
	private static LoaderCallbacks<Cursor> ferriesTerminalSyncCallbacks;
	private Spinner daySpinner;
    private static ArrayList<FerriesScheduleDateItem> mScheduleDateItems;
    private static ArrayList<CharSequence> mDaysOfWeek;
	private static int mPosition;
	
	private static final int FERRIES_DEPARTURES_LOADER_ID = 0;
	private static final int FERRIES_VEHICLE_SPACE_LOADER_ID = 1;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
        DateFormat dateFormat = new SimpleDateFormat("EEEE");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		
		Bundle args = activity.getIntent().getExtras();
		
        mPosition = args.getInt("position");
        mScheduleDateItems = (ArrayList<FerriesScheduleDateItem>) args.getSerializable("scheduleDateItems");
        terminalItem = mScheduleDateItems.get(0).getFerriesTerminalItem().get(mPosition);
        mDaysOfWeek = new ArrayList<CharSequence>();
        
        int numDates = mScheduleDateItems.size();
        for (int i = 0; i < numDates; i++) {
            mDaysOfWeek.add(dateFormat.format(new Date(
                    Long.parseLong(mScheduleDateItems.get(i).getDate()))));
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
		setRetainInstance(true);
		
		ferriesTerminalSyncCallbacks = new LoaderCallbacks<Cursor>() {

            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String[] projection = {
                        FerriesTerminalSailingSpace._ID,
                        FerriesTerminalSailingSpace.TERMINAL_ID,
                        FerriesTerminalSailingSpace.TERMINAL_NAME,
                        FerriesTerminalSailingSpace.TERMINAL_ABBREV,
                        FerriesTerminalSailingSpace.TERMINAL_DEPARTING_SPACES,
                        FerriesTerminalSailingSpace.TERMINAL_LAST_UPDATED,
                        FerriesTerminalSailingSpace.TERMINAL_IS_STARRED
                };
                
                CursorLoader cursorLoader = new FerriesTerminalLoader(getActivity(),
                        FerriesTerminalSailingSpace.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null
                        );
                
                return cursorLoader;
            }

            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                
                if (cursor != null && cursor.moveToFirst()) {
                    // Update existing FerriesScheduleTimesItem (times)
                    do {
                        int departingTerminalID = cursor.getInt(cursor.getColumnIndex(FerriesTerminalSailingSpace.TERMINAL_ID));
                        if (departingTerminalID != terminalItem.getDepartingTerminalID()) {
                            continue;
                        }
                        try {
                            JSONArray departingSpaces = new JSONArray(cursor.getString(cursor.getColumnIndex(FerriesTerminalSailingSpace.TERMINAL_DEPARTING_SPACES)));
                            for (int i=0; i < departingSpaces.length(); i++) {
                                JSONObject spaces = departingSpaces.getJSONObject(i);
                                String departure = dateFormat.format(new Date(Long.parseLong(spaces.getString("Departure").substring(6, 19))));
                                JSONArray spaceForArrivalTerminals = spaces.getJSONArray("SpaceForArrivalTerminals");
                                for (int j=0; j < spaceForArrivalTerminals.length(); j++) {
                                    JSONObject terminals = spaceForArrivalTerminals.getJSONObject(j);
                                    if (terminals.getInt("TerminalID") != terminalItem.getArrivingTerminalID()) {
                                        continue;
                                    } else {
                                        int driveUpSpaceCount = terminals.getInt("DriveUpSpaceCount");
                                        int maxSpaceCount = terminals.getInt("MaxSpaceCount");

                                        for (FerriesScheduleTimesItem time: times) {
                                            if (dateFormat.format(new Date(Long.parseLong(time.getDepartingTime()))).equals(departure)) {
                                                time.setDriveUpSpaceCount(driveUpSpaceCount);
                                                time.setMaxSpaceCount(maxSpaceCount);
                                                time.setLastUpdated(cursor.getString(cursor.getColumnIndex(FerriesTerminalSailingSpace.TERMINAL_LAST_UPDATED)));
                                            }

                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }

                swipeRefreshLayout.setRefreshing(false);
                mAdapter.setData(times);
            }

            public void onLoaderReset(Loader<Cursor> loader) {
                swipeRefreshLayout.setRefreshing(false);                 
            }
		};
	}

    public static class FerriesTerminalLoader extends CursorLoader {

        public FerriesTerminalLoader(Context context, Uri uri,
                String[] projection, String selection, String[] selectionArgs,
                String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
        }
        
    }
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        this.mAdapter = new DepartureTimesAdapter(getActivity(), null);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));


        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);
        
        mEmptyView = root.findViewById(R.id.empty_list_view);

        // Set up custom spinner
        Spinner daySpinner = (Spinner) getActivity().findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> dayOfWeekArrayAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.simple_spinner_item_white, mDaysOfWeek);;
        dayOfWeekArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_white);
        daySpinner.setAdapter(dayOfWeekArrayAdapter);
        daySpinner.setOnItemSelectedListener(this);

        return root;
	}

    @Override
    public void onPause() {
        super.onPause();
        
        getActivity().unregisterReceiver(ferriesTerminalSyncReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        IntentFilter filter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.FERRIES_TERMINAL_SAILING_SPACE_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        ferriesTerminalSyncReceiver = new FerriesTerminalSyncReceiver();
        getActivity().registerReceiver(ferriesTerminalSyncReceiver, filter);
    }	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        
		// Prepare the loaders. Either re-connect with an existing one, or start new ones.
        getLoaderManager().initLoader(FERRIES_DEPARTURES_LOADER_ID, null, this);
        getLoaderManager().initLoader(FERRIES_VEHICLE_SPACE_LOADER_ID, null, ferriesTerminalSyncCallbacks);
        
        TextView t = (TextView) mEmptyView;
        t.setText(R.string.no_day_departures);
	}

	public Loader<ArrayList<FerriesScheduleTimesItem>> onCreateLoader(int id,
			Bundle args) {
		
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new DepartureTimesLoader(getActivity());
	}

	public void onLoadFinished(
			Loader<ArrayList<FerriesScheduleTimesItem>> loader,
			ArrayList<FerriesScheduleTimesItem> data) {
		
        Intent intent = new Intent(getActivity(), FerriesTerminalSailingSpaceSyncService.class);
        getActivity().startService(intent);

	    swipeRefreshLayout.setRefreshing(false);
		mAdapter.setData(data);

        if (data != null){
            mEmptyView.setVisibility(View.GONE);
        }

	}

	public void onLoaderReset(Loader<ArrayList<FerriesScheduleTimesItem>> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	}

	public static class DepartureTimesLoader extends AsyncTaskLoader<ArrayList<FerriesScheduleTimesItem>> {

        public DepartureTimesLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<FerriesScheduleTimesItem> loadInBackground() {
            int numAnnotations = terminalItem.getAnnotations().size();
            int numTimes = terminalItem.getScheduleTimes().size();
            annotations = new ArrayList<FerriesAnnotationsItem>();
            times = new ArrayList<FerriesScheduleTimesItem>();

            try {
                for (int i = 0; i < numAnnotations; i++) {
                    FerriesAnnotationsItem annotationItem = new FerriesAnnotationsItem();
                    annotationItem.setAnnotation(terminalItem.getAnnotations().get(i).getAnnotation());
                    annotations.add(annotationItem);
                }

                for (int i = 0; i < numTimes; i++) {
                    FerriesScheduleTimesItem timesItem = new FerriesScheduleTimesItem();
                    timesItem.setDepartingTime(terminalItem.getScheduleTimes().get(i).getDepartingTime());
                    timesItem.setArrivingTime(terminalItem.getScheduleTimes().get(i).getArrivingTime());

                    int numIndexes = terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().size();
                    for (int j = 0; j < numIndexes; j++) {
                        FerriesAnnotationIndexesItem index = new FerriesAnnotationIndexesItem();
                        index.setIndex(terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().get(j).getIndex());
                        timesItem.setAnnotationIndexes(index);
                    }

                    times.add(timesItem);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding terminal departure times", e);
            }

            return times;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            swipeRefreshLayout.post(new Runnable() {
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();

            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ArrayList<FerriesScheduleTimesItem> data) {
            super.onCanceled(data);
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();
        }
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

            }else {

                FerriesScheduleTimesItem item = getItem(position);

                itemHolder = (TimesViewHolder) holder;

                String annotation = "";

                int numIndexes = item.getAnnotationIndexes().size();
                for (int i = 0; i < numIndexes; i++) {
                    FerriesAnnotationsItem p = annotations.get(item.getAnnotationIndexes().get(i).getIndex());
                    annotation += p.getAnnotation();
                }

                if (annotation.equals("")) {
                    itemHolder.annotation.setVisibility(View.GONE);
                } else {
                    itemHolder.annotation.setVisibility(View.VISIBLE);
                }

                itemHolder.departing.setText(dateFormat.format(new Date(Long.parseLong(item.getDepartingTime()))));

                if (!item.getArrivingTime().equals("N/A")) {
                    itemHolder.arriving.setText(dateFormat.format(new Date(Long.parseLong(item.getArrivingTime()))));
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
                } else {
                    itemHolder.vehicleSpaceGroup.setVisibility(View.GONE);
                    itemHolder.driveUpSpaceCount.setVisibility(View.GONE);
                    itemHolder.driveUpSpaces.setVisibility(View.GONE);
                    itemHolder.driveUpSpacesDisclaimer.setVisibility(View.GONE);
                    itemHolder.updated.setVisibility(View.GONE);
                }
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
            departing = (TextView) itemView.findViewById(R.id.departing);
            arriving = (TextView) itemView.findViewById(R.id.arriving);
            annotation = (TextView) itemView.findViewById(R.id.annotation);
            vehicleSpaceGroup = (RelativeLayout) itemView.findViewById(R.id.driveUpProgressBarGroup);
            driveUpProgressBar = (ProgressBar) itemView.findViewById(R.id.driveUpProgressBar);
            driveUpSpaceCount = (TextView) itemView.findViewById(R.id.driveUpSpaceCount);
            driveUpSpaces = (TextView) itemView.findViewById(R.id.driveUpSpaces);
            driveUpSpacesDisclaimer = (TextView) itemView.findViewById(R.id.driveUpSpacesDisclaimer);
            updated = (TextView) itemView.findViewById(R.id.updated);
        }
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView Departing;
        protected TextView Arriving;

        public TitleViewHolder(View itemView) {
            super(itemView);
            Departing = (TextView) itemView.findViewById(R.id.departing_title);
            Arriving = (TextView) itemView.findViewById(R.id.arriving_title);
        }
    }

    public void onRefresh() {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
        Intent intent = new Intent(getActivity(), FerriesTerminalSailingSpaceSyncService.class);
        getActivity().startService(intent); 
    }
    
    public class FerriesTerminalSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK")) {
                    getLoaderManager().restartLoader(
                            FERRIES_VEHICLE_SPACE_LOADER_ID, null,
                            ferriesTerminalSyncCallbacks);
                } else {
                    Log.e(TAG, responseString);
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        terminalItem = mScheduleDateItems.get(parent.getSelectedItemPosition()).getFerriesTerminalItem().get(mPosition);
        getLoaderManager().restartLoader(FERRIES_DEPARTURES_LOADER_ID, null, this);
        getLoaderManager().restartLoader(FERRIES_VEHICLE_SPACE_LOADER_ID, null, ferriesTerminalSyncCallbacks);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

}
