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
package gov.wa.wsdot.android.wsdot.ui.home;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.LinkedHashMap;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MapLocation;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MyRoute;
import gov.wa.wsdot.android.wsdot.service.FerriesSchedulesSyncService;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.FerriesRouteAlertsBulletinsActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.FerriesRouteSchedulesDaySailingsActivity;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassItemActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class FavoritesFragment extends BaseFragment implements
        LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = getClass().getSimpleName();

	private View mEmptyView;

    private FavItemAdapter mFavoritesAdapter;
	
	private Intent mFerriesSchedulesIntent;
	private Intent mMountainPassesIntent;
	private Intent mTravelTimesIntent;

    private Tracker mTracker;

	private SwipeRefreshLayout swipeRefreshLayout;
	
	private MountainPassesSyncReceiver mMountainPassesSyncReceiver;
	private FerriesSchedulesSyncReceiver mFerriesSchedulesSyncReceiver;
	private TravelTimesSyncReceiver mTravelTimesSyncReceiver;

    private static final String[] cameras_projection = {
            Cameras._ID,
            Cameras.CAMERA_ID,
            Cameras.CAMERA_TITLE,
            Cameras.CAMERA_IS_STARRED
    };

	private static final String[] mountain_passes_projection = {
            MountainPasses._ID,
            MountainPasses.MOUNTAIN_PASS_ID,
            MountainPasses.MOUNTAIN_PASS_DATE_UPDATED,
            MountainPasses.MOUNTAIN_PASS_IS_STARRED,
            MountainPasses.MOUNTAIN_PASS_NAME,
            MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION,
            MountainPasses.MOUNTAIN_PASS_WEATHER_ICON,
            MountainPasses.MOUNTAIN_PASS_CAMERA,
            MountainPasses.MOUNTAIN_PASS_ELEVATION,
            MountainPasses.MOUNTAIN_PASS_FORECAST,
            MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE,
            MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION,
            MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO,
            MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION,
            MountainPasses.MOUNTAIN_PASS_ROAD_CONDITION,
            MountainPasses.MOUNTAIN_PASS_TEMPERATURE
    };

	private static final String[] travel_times_projection = {
            TravelTimes._ID,
            TravelTimes.TRAVEL_TIMES_ID,
            TravelTimes.TRAVEL_TIMES_TITLE,
            TravelTimes.TRAVEL_TIMES_UPDATED,
            TravelTimes.TRAVEL_TIMES_DISTANCE,
            TravelTimes.TRAVEL_TIMES_AVERAGE,
            TravelTimes.TRAVEL_TIMES_CURRENT,
            TravelTimes.TRAVEL_TIMES_IS_STARRED
    };

	private static final String[] ferries_schedules_projection = {
            FerriesSchedules._ID,
			FerriesSchedules.FERRIES_SCHEDULE_ID,
			FerriesSchedules.FERRIES_SCHEDULE_TITLE,
			FerriesSchedules.FERRIES_SCHEDULE_CROSSING_TIME,
			FerriesSchedules.FERRIES_SCHEDULE_DATE,
			FerriesSchedules.FERRIES_SCHEDULE_ALERT,
			FerriesSchedules.FERRIES_SCHEDULE_UPDATED,
			FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED
    };

    private static final String[] location_projection = {
            MapLocation._ID,
            MapLocation.LOCATION_TITLE,
            MapLocation.LOCATION_LAT,
            MapLocation.LOCATION_LONG,
            MapLocation.LOCATION_ZOOM
    };

    private static final String[] my_route_projection = {
            MyRoute._ID,
            MyRoute.MY_ROUTE_TITLE,
            MyRoute.MY_ROUTE_DISPLAY_LAT,
            MyRoute.MY_ROUTE_DISPLAY_LONG,
            MyRoute.MY_ROUTE_DISPLAY_ZOOM,
            MyRoute.MY_ROUTE_LOCATIONS
    };

    private static final int MY_ROUTE_LOADER_ID = 10;
	private static final int CAMERAS_LOADER_ID = 11;
	private static final int MOUNTAIN_PASSES_LOADER_ID = 12;
	private static final int TRAVEL_TIMES_LOADER_ID = 13;
	private static final int FERRIES_SCHEDULES_LOADER_ID = 14;
    private static final int LOCATION_LOADER_ID = 15;

    public static final int MY_ROUTE_VIEWTYPE = 0;
    public static final int CAMERAS_VIEWTYPE = 1;
    public static final int MOUNTAIN_PASSES_VIEWTYPE = 2;
    public static final int TRAVEL_TIMES_VIEWTYPE = 3;
    public static final int FERRIES_SCHEDULES_VIEWTYPE = 4;
    public static final int LOCATION_VIEWTYPE = 5;

    private static final int HEADER_VIEWTYPE = 6;

    public static LinkedHashMap headers = new LinkedHashMap<Integer, String>(){
        {
            put(MY_ROUTE_VIEWTYPE, "My Routes");
            put(CAMERAS_VIEWTYPE, "Cameras");
            put(MOUNTAIN_PASSES_VIEWTYPE, "Mountain Passes");
            put(TRAVEL_TIMES_VIEWTYPE, "Travel Times");
            put(FERRIES_SCHEDULES_VIEWTYPE, "Ferries Schedules");
            put(LOCATION_VIEWTYPE, "Locations");
        }
    };

    private int orderedViewTypes[] = new int[6];

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);


		mFerriesSchedulesIntent = new Intent(getActivity(), FerriesSchedulesSyncService.class);
        getActivity().startService(mFerriesSchedulesIntent);

		mMountainPassesIntent = new Intent(getActivity(), MountainPassesSyncService.class);
        getActivity().startService(mMountainPassesIntent);
		
		mTravelTimesIntent = new Intent(getActivity(), TravelTimesSyncService.class);
		getActivity().startService(mTravelTimesIntent);

	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_favorites, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFavoritesAdapter = new FavItemAdapter();

        mRecyclerView.setAdapter(mFavoritesAdapter);

        // Add swipe dismiss to favorites list items.
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof HeaderViewHolder) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder holder, int swipeDir) {

                final String[] item_id;
                final int viewType = mFavoritesAdapter.getItemViewType(holder.getAdapterPosition());

                //Keeps holders data for undo
                holder.setIsRecyclable(false);

                //get the camera id or tag for the item being removed.
                switch (viewType){
                    case CAMERAS_VIEWTYPE:
                        Cursor c = (Cursor) mFavoritesAdapter.getItem(holder.getAdapterPosition());
                        item_id = new String[]{Integer.toString(c.getInt(c.getColumnIndex(Cameras.CAMERA_ID)))};
                        break;
                    case FERRIES_SCHEDULES_VIEWTYPE:
                        FerryViewHolder ferryholder = (FerryViewHolder) holder;
                        item_id = new String[] {Integer.toString((Integer) ferryholder.star_button.getTag())};
                        break;
                    case TRAVEL_TIMES_VIEWTYPE:
                        TimesViewHolder timesholder = (TimesViewHolder) holder;
                        item_id = new String[] {Integer.toString((Integer) timesholder.star_button.getTag())};
                        break;
                    case MOUNTAIN_PASSES_VIEWTYPE:
                        PassViewHolder passholder = (PassViewHolder) holder;
                        item_id = new String[] {Integer.toString((Integer) passholder.star_button.getTag())};
                        break;
                    case LOCATION_VIEWTYPE:
                        LocationViewHolder locholder = (LocationViewHolder) holder;
                        item_id = new String[] {Integer.toString((Integer) locholder.title.getTag())};
                        break;
                    case MY_ROUTE_VIEWTYPE:
                        MyRouteViewHolder routeholder = (MyRouteViewHolder) holder;
                        item_id = new String[] {Integer.toString((Integer) routeholder.title.getTag())};
                        break;
                    default:
                        item_id = null;
                }

                mFavoritesAdapter.remove(item_id, viewType);

                // Display snack bar with undo button
                final Snackbar snackbar = Snackbar
                        .make(mRecyclerView, R.string.remove_favorite, Snackbar.LENGTH_LONG);

                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFavoritesAdapter.undo(item_id, viewType, holder);
                    }
                });
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

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

        mEmptyView = root.findViewById( R.id.empty_list_view );

        return root;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(CAMERAS_LOADER_ID, null, this);
		getLoaderManager().initLoader(FERRIES_SCHEDULES_LOADER_ID, null, this);
        getLoaderManager().initLoader(MOUNTAIN_PASSES_LOADER_ID, null, this);
        getLoaderManager().initLoader(TRAVEL_TIMES_LOADER_ID, null, this);
        getLoaderManager().initLoader(LOCATION_LOADER_ID, null, this);
        getLoaderManager().initLoader(MY_ROUTE_LOADER_ID, null, this);

	    TextView t = (TextView) mEmptyView;
        t.setText(R.string.no_favorites);
	}

	@Override
	public void onPause() {
        super.onPause();
		getActivity().unregisterReceiver(mFerriesSchedulesSyncReceiver);
		getActivity().unregisterReceiver(mMountainPassesSyncReceiver);
		getActivity().unregisterReceiver(mTravelTimesSyncReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();

        // Check preferences and set defaults if none set
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        orderedViewTypes[0] = settings.getInt("KEY_FIRST_FAVORITES_SECTION", MY_ROUTE_VIEWTYPE);
        orderedViewTypes[1] = settings.getInt("KEY_SECOND_FAVORITES_SECTION", CAMERAS_VIEWTYPE);
        orderedViewTypes[2] = settings.getInt("KEY_THIRD_FAVORITES_SECTION", FERRIES_SCHEDULES_VIEWTYPE);
        orderedViewTypes[3] = settings.getInt("KEY_FOURTH_FAVORITES_SECTION", MOUNTAIN_PASSES_VIEWTYPE);
        orderedViewTypes[4] = settings.getInt("KEY_FIFTH_FAVORITES_SECTION", TRAVEL_TIMES_VIEWTYPE);
        orderedViewTypes[5] = settings.getInt("KEY_SIXTH_FAVORITES_SECTION", LOCATION_VIEWTYPE);
        mFavoritesAdapter.notifyDataSetChanged();

        // Ferries Route Schedules
        IntentFilter ferriesSchedulesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.FERRIES_SCHEDULES_RESPONSE");
		ferriesSchedulesFilter.addCategory(Intent.CATEGORY_DEFAULT);
		mFerriesSchedulesSyncReceiver = new FerriesSchedulesSyncReceiver();
		getActivity().registerReceiver(mFerriesSchedulesSyncReceiver, ferriesSchedulesFilter);

		// Mountain Passes
        IntentFilter mountainPassesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
        mountainPassesFilter.addCategory(Intent.CATEGORY_DEFAULT);
		mMountainPassesSyncReceiver = new MountainPassesSyncReceiver();
        getActivity().registerReceiver(mMountainPassesSyncReceiver, mountainPassesFilter);

		// Travel Times
        IntentFilter travelTimesfilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.TRAVEL_TIMES_RESPONSE");
		travelTimesfilter.addCategory(Intent.CATEGORY_DEFAULT);
		mTravelTimesSyncReceiver = new TravelTimesSyncReceiver();
        getActivity().registerReceiver(mTravelTimesSyncReceiver, travelTimesfilter);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    CursorLoader cursorLoader = null;
        swipeRefreshLayout.post(new Runnable() {
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

		switch(id) {
	    case CAMERAS_LOADER_ID:
			cursorLoader = new CursorLoader(
					getActivity(),
					Cameras.CONTENT_URI,
					cameras_projection,
					Cameras.CAMERA_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
	    case MOUNTAIN_PASSES_LOADER_ID:
			cursorLoader = new CursorLoader(
					getActivity(),
					MountainPasses.CONTENT_URI,
					mountain_passes_projection,
                    MountainPasses.MOUNTAIN_PASS_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
	    case TRAVEL_TIMES_LOADER_ID:
			cursorLoader = new CursorLoader(
					getActivity(),
					TravelTimes.CONTENT_URI,
					travel_times_projection,
					TravelTimes.TRAVEL_TIMES_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
	    case FERRIES_SCHEDULES_LOADER_ID:
			cursorLoader = new CursorLoader(
					getActivity(),
					FerriesSchedules.CONTENT_URI,
					ferries_schedules_projection,
					FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
        case LOCATION_LOADER_ID:
            cursorLoader = new CursorLoader(
                    getActivity(),
                    MapLocation.CONTENT_URI,
                    location_projection,
                    null,
                    null,
                    MapLocation.LOCATION_TITLE + " ASC"
            );
            break;
        case MY_ROUTE_LOADER_ID:
            cursorLoader = new CursorLoader(
                    getActivity(),
                    MyRoute.CONTENT_URI,
                    my_route_projection,
                    MyRoute.MY_ROUTE_IS_STARRED + "=?",
                    new String[] {Integer.toString(1)},
                    MyRoute.MY_ROUTE_TITLE + " ASC"
            );
            break;
		}

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mFavoritesAdapter.swapCursor(cursor, loader.getId());

        if (mFavoritesAdapter.getItemCount() > 0){
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }

		swipeRefreshLayout.setRefreshing(false);

	}

	public void onLoaderReset(Loader<Cursor> loader) {
	    swipeRefreshLayout.setRefreshing(false);
        mFavoritesAdapter.swapCursor(null, loader.getId());
	}

    /**
     * Custom adapter for favorites items in recycler view.
     *
     * This class stores a LinkedHashMap of cursors for each type of
     * favorite-able item.
     *
     * It should be possible to add items that do not have cursors to this adapter
     * as long as they have their own ViewHolders. There would need to be a new
     * viewType and a way to identify it.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolders
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class FavItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        public LinkedHashMap loaderIdToViewType = new LinkedHashMap<Integer, Integer>(){
            {
                put(MY_ROUTE_LOADER_ID, MY_ROUTE_VIEWTYPE);
                put(CAMERAS_LOADER_ID, CAMERAS_VIEWTYPE);
                put(MOUNTAIN_PASSES_LOADER_ID, MOUNTAIN_PASSES_VIEWTYPE);
                put(TRAVEL_TIMES_LOADER_ID, TRAVEL_TIMES_VIEWTYPE);
                put(FERRIES_SCHEDULES_LOADER_ID, FERRIES_SCHEDULES_VIEWTYPE);
                put(LOCATION_LOADER_ID, LOCATION_VIEWTYPE);
            }
        };

        public final LinkedHashMap sections = new LinkedHashMap<Integer, Cursor>(){
            {
                put(MY_ROUTE_VIEWTYPE, null);
                put(CAMERAS_VIEWTYPE, null);
                put(MOUNTAIN_PASSES_VIEWTYPE, null);
                put(TRAVEL_TIMES_VIEWTYPE, null);
                put(FERRIES_SCHEDULES_VIEWTYPE, null);
                put(LOCATION_VIEWTYPE, null);
            }
        };

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView;

            switch (viewType){
                case CAMERAS_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_item, null);
                    return new CamViewHolder(itemView);
                case MOUNTAIN_PASSES_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_item_details_with_icon, null);
                    return new PassViewHolder(itemView);
                case TRAVEL_TIMES_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_item_travel_times, null);
                    return new TimesViewHolder(itemView);
                case FERRIES_SCHEDULES_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_item_with_star, null);
                    return new FerryViewHolder(itemView);
                case LOCATION_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_item_content, null);
                    return new LocationViewHolder(itemView);
                case MY_ROUTE_VIEWTYPE:
                    itemView = LayoutInflater
                            .from(parent.getContext()).inflate(R.layout.list_item_my_route_favorite, null);
                    return new MyRouteViewHolder(itemView);
                case HEADER_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_header, parent, false);
                    return new HeaderViewHolder(itemView);
                default:
                    Log.e(TAG, "No matching view type for type: " + viewType);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            holder.itemView.setVisibility(View.VISIBLE);

            if (holder instanceof HeaderViewHolder){
                HeaderViewHolder viewholder = (HeaderViewHolder) holder;

                String title = (String) mFavoritesAdapter.getItem(position);
                viewholder.title.setText(title);
                viewholder.title.setTypeface(tfb);

                //Remove divider if first element in favorites list
                if (position == 0) {
                    viewholder.divider.setVisibility(View.GONE);
                }else{
                    viewholder.divider.setVisibility(View.VISIBLE);
                }

            }else if (holder instanceof CamViewHolder){
                CamViewHolder viewholder = (CamViewHolder) holder;
                Cursor cursor = (Cursor) mFavoritesAdapter.getItem(position);
                String title = cursor.getString(cursor.getColumnIndex(Cameras.CAMERA_TITLE));
                viewholder.title.setText(title);
                viewholder.title.setTypeface(tf);

                final int pos = position;

                // Set onClickListener for holder's view
                viewholder.itemView.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                Cursor c = (Cursor) mFavoritesAdapter.getItem(pos);
                                Bundle b = new Bundle();
                                Intent intent = new Intent(getActivity(), CameraActivity.class);
                                b.putInt("id", c.getInt(c.getColumnIndex(Cameras.CAMERA_ID)));
                                b.putString("advertisingTarget", "other");
                                intent.putExtras(b);
                                startActivity(intent);
                            }
                        }
                );

            } else if (holder instanceof PassViewHolder){
                PassViewHolder viewHolder = (PassViewHolder) holder;
                Cursor cursor = (Cursor) mFavoritesAdapter.getItem(position);

                String title = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_NAME));
                viewHolder.title.setText(title);
                viewHolder.title.setTypeface(tfb);

                String created_at = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_DATE_UPDATED));
                viewHolder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
                viewHolder.created_at.setTypeface(tf);

                String text = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION));

                if (text.equals("")) {
                    viewHolder.text.setVisibility(View.GONE);
                } else {
                    viewHolder.text.setVisibility(View.VISIBLE);
                    viewHolder.text.setText(text);
                    viewHolder.text.setTypeface(tf);
                }

                int icon = getResources().getIdentifier(cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_ICON)),
                        "drawable", getActivity().getPackageName());
                viewHolder.icon.setImageResource(icon);

                viewHolder.star_button.setVisibility(View.GONE);
                viewHolder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));

                final int pos = position;

                // Set onClickListener for holder's view
                viewHolder.itemView.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                Cursor c = (Cursor) mFavoritesAdapter.getItem(pos);
                                Bundle b = new Bundle();
                                Intent intent = new Intent(getActivity(), MountainPassItemActivity.class);
                                b.putInt("id", c.getInt(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_ID)));
                                b.putString("MountainPassName", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_NAME)));
                                b.putString("DateUpdated", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_DATE_UPDATED)));
                                b.putString("TemperatureInFahrenheit", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_TEMPERATURE)));
                                b.putString("ElevationInFeet", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_ELEVATION)));
                                b.putString("RoadCondition", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_ROAD_CONDITION)));
                                b.putString("WeatherCondition", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION)));
                                b.putString("RestrictionOneText", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE)));
                                b.putString("RestrictionOneTravelDirection", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION)));
                                b.putString("RestrictionTwoText", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO)));
                                b.putString("RestrictionTwoTravelDirection", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION)));
                                b.putString("Cameras", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_CAMERA)));
                                b.putString("Forecasts", c.getString(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_FORECAST)));
                                b.putInt("isStarred", c.getInt(c.getColumnIndex(MountainPasses.MOUNTAIN_PASS_IS_STARRED)));
                                intent.putExtras(b);
                                startActivity(intent);
                            }
                        }
                );

            } else if (holder instanceof TimesViewHolder){

                Cursor cursor = (Cursor) mFavoritesAdapter.getItem(position);
                TimesViewHolder viewholder = (TimesViewHolder) holder;

                String average_time;

                String title = cursor.getString(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_TITLE));
                viewholder.title.setText(title);
                viewholder.title.setTypeface(tfb);

                String distance = cursor.getString(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_DISTANCE));
                int average = cursor.getInt(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_AVERAGE));

                if (average == 0) {
                    average_time = "Not Available";
                } else {
                    average_time = average + " min";
                }

                viewholder.distance_average_time.setText(distance + " / " + average_time);
                viewholder.distance_average_time.setTypeface(tf);

                int current = cursor.getInt(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_CURRENT));

                if (current < average) {
                    viewholder.current_time.setTextColor(0xFF008060);
                } else if ((current > average) && (average != 0)) {
                    viewholder.current_time.setTextColor(Color.RED);
                } else {
                    viewholder.current_time.setTextColor(Color.BLACK);
                }

                viewholder.current_time.setText(current + " min");
                viewholder.current_time.setTypeface(tfb);

                String created_at = cursor.getString(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_UPDATED));
                viewholder.updated.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", false));
                viewholder.updated.setTypeface(tf);

                viewholder.star_button.setVisibility(View.GONE);
                viewholder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));

            } else if (holder instanceof FerryViewHolder){

                Cursor cursor = (Cursor) mFavoritesAdapter.getItem(position);

                FerryViewHolder viewholder = (FerryViewHolder) holder;

                viewholder.title.setText(cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
                viewholder.title.setTypeface(tfb);

                String text = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_CROSSING_TIME));

                final int pos = position;

                // Set onClickListener for holder's view
                viewholder.itemView.setOnClickListener(
                        new OnClickListener() {
                            public void onClick(View v) {
                                Cursor c = (Cursor) mFavoritesAdapter.getItem(pos);
                                Bundle b = new Bundle();
                                Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDaySailingsActivity.class);
                                b.putInt("id", c.getInt(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ID)));
                                b.putString("title", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
                                b.putString("date", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_DATE)));
                                b.putInt("isStarred", c.getInt(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED)));
                                intent.putExtras(b);
                                startActivity(intent);
                            }
                        }
                );

                try {
                    if (text.equalsIgnoreCase("null")) {
                        viewholder.text.setText("");
                    } else {
                        viewholder.text.setText("Crossing Time: ~ " + text + " min");
                        viewholder.text.setTypeface(tf);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                String created_at = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_UPDATED));
                viewholder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
                viewholder.created_at.setTypeface(tf);

                viewholder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
                viewholder.star_button.setVisibility(View.GONE);

                String alerts = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ALERT));

                if (alerts.equals("[]")) {
                    viewholder.alert_button.setVisibility(View.GONE);
                } else {
                    viewholder.alert_button.setVisibility(View.VISIBLE);
                    viewholder.alert_button.setTag(cursor.getPosition());
                    viewholder.alert_button.setImageResource(R.drawable.btn_alert_on);
                    viewholder.alert_button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Cursor c = (Cursor) mFavoritesAdapter.getItem(pos);
                            Bundle b = new Bundle();
                            Intent intent = new Intent(getActivity(), FerriesRouteAlertsBulletinsActivity.class);
                            b.putString("title", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
                            b.putString("alert", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ALERT)));
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                    });
                }

            }else if (holder instanceof LocationViewHolder) {
                LocationViewHolder viewholder = (LocationViewHolder) holder;
                Cursor cursor = (Cursor) mFavoritesAdapter.getItem(position);

                String title = cursor.getString(cursor.getColumnIndex(MapLocation.LOCATION_TITLE));
                viewholder.title.setText(title);
                viewholder.title.setTypeface(tf);

                viewholder.lng = cursor.getFloat(cursor.getColumnIndex(MapLocation.LOCATION_LONG));
                viewholder.lat = cursor.getFloat(cursor.getColumnIndex(MapLocation.LOCATION_LAT));
                viewholder.zoom = cursor.getInt(cursor.getColumnIndex(MapLocation.LOCATION_ZOOM));

                String latlong = String.format("%.2f, %.2f", viewholder.lat, viewholder.lng);

                viewholder.latlong.setText(latlong);
                viewholder.latlong.setTypeface(tf);

                viewholder.title.setTag(cursor.getInt(cursor.getColumnIndex("_id")));

                final int pos = position;

                // Set onClickListener for holder's view
                viewholder.itemView.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                Cursor c = (Cursor) mFavoritesAdapter.getItem(pos);
                                Bundle b = new Bundle();
                                Intent intent = new Intent(getActivity(), TrafficMapActivity.class);

                                b.putFloat("lat", c.getFloat(c.getColumnIndex(MapLocation.LOCATION_LAT)));
                                b.putFloat("long", c.getFloat(c.getColumnIndex(MapLocation.LOCATION_LONG)));
                                b.putInt("zoom", c.getInt(c.getColumnIndex(MapLocation.LOCATION_ZOOM)));

                                intent.putExtras(b);
                                startActivity(intent);
                            }
                        }
                );

            }else if (holder instanceof  MyRouteViewHolder) {

                MyRouteViewHolder viewholder = (MyRouteViewHolder) holder;
                Cursor cursor = (Cursor) mFavoritesAdapter.getItem(position);

                String title = cursor.getString(cursor.getColumnIndex(MyRoute.MY_ROUTE_TITLE));

                viewholder.title.setText(title);
                viewholder.title.setTypeface(tf);

                viewholder.title.setTag(cursor.getInt(cursor.getColumnIndex("_id")));

                viewholder.lng = cursor.getFloat(cursor.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_LAT));
                viewholder.lat = cursor.getFloat(cursor.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_LONG));
                viewholder.zoom = cursor.getInt(cursor.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_ZOOM));

                final int pos = position;

                viewholder.alerts_button.setTag(cursor.getPosition());
                viewholder.alerts_button.setContentDescription("Check alerts on route");
                viewholder.alerts_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Button Tap")
                                .setAction("Check Alerts")
                                .setLabel("My Routes")
                                .build());

                        Cursor c = (Cursor) mFavoritesAdapter.getItem(pos);
                        Bundle b = new Bundle();

                        Intent intent = new Intent(getActivity(), MyRouteAlertsActivity.class);

                        b.putString("title", "Alerts on Route: " + c.getString(c.getColumnIndex(MyRoute.MY_ROUTE_TITLE)));
                        b.putString("route", c.getString(c.getColumnIndexOrThrow(MyRoute.MY_ROUTE_LOCATIONS)));

                        intent.putExtras(b);
                        startActivity(intent);
                    }
                });

                viewholder.map_button.setTag(cursor.getPosition());
                viewholder.map_button.setContentDescription("Check map for route");
                viewholder.map_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Button Tap")
                                .setAction("Check Map for Route")
                                .setLabel("My Routes")
                                .build());

                        Cursor c = (Cursor) mFavoritesAdapter.getItem(pos);
                        Bundle b = new Bundle();

                        Intent intent = new Intent(getActivity(), TrafficMapActivity.class);

                        b.putFloat("lat", c.getFloat(c.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_LAT)));
                        b.putFloat("long", c.getFloat(c.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_LONG)));
                        b.putInt("zoom", c.getInt(c.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_ZOOM)));

                        intent.putExtras(b);
                        startActivity(intent);
                    }
                });


            }else{
                Log.i(TAG, "No view holder for type: " + holder.getClass().getName()); //TODO
            }
        }

        /**
         * Returns the viewType at a given position.
         *
         * If new items become favorite-able they will need to
         * be able to be identified by using this function.
         *
         * @param position
         * @return The viewType of the item at position
         */
        @Override
        public int getItemViewType(int position) {

            for (int viewType : orderedViewTypes) {
                Cursor c = (Cursor) sections.get(viewType);
                int size = 0;
                if (c != null) {
                    if (c.getCount() > 0) {
                        size = c.getCount() + 1;
                    }
                }
                // check if position inside this section
                if (position == 0 && size > 0) return HEADER_VIEWTYPE;
                if (position < size) return viewType;

                position -= size;
            }
            return -1;
        }

        /**
         * Swaps the cursor with new based on id. Acts like CursorAdapter.swapCursor but
         * for multiple cursors. This does not close the old cursor.
         *
         * @param newCursor
         * @param id
         * @return oldCursor
         *
         * @see android.support.v4.widget.CursorAdapter
         */
        public Cursor swapCursor(Cursor newCursor, int id) {

            Cursor cursor = (Cursor) sections.get(id);

            if (newCursor == cursor) {
                return null;
            }

            sections.put(loaderIdToViewType.get(id), newCursor);

            if (newCursor != null) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeRemoved(0, getItemCount());
            }
            return cursor;

        }

        /**
         * Returns the item at a given position. This could either be a Cursor
         * or a section header (a String).
         * @param position
         * @return
         */
        public Object getItem(int position){

            for (int viewType : orderedViewTypes) {
                Cursor c = (Cursor) sections.get(viewType);

                int size = 0;

                if (c != null) {
                    if (c.getCount() != 0) {
                        size = c.getCount() + 1;
                    }
                }

                // check if position inside this section
                if(position == 0 && size > 0) return headers.get(viewType);

                if(position < size && c != null) {
                    c.moveToPosition(position - 1);
                    return c;
                }

                // otherwise jump into next section
                position -= size;

            }
            return null;
        }

        @Override
        public int getItemCount(){
            int count = 0;
            for(Object section : this.sections.keySet()) {
                Cursor c = (Cursor) sections.get(section);
                if (c != null) {
                    if (c.getCount() > 0) {
                        count += c.getCount();
                        count += 1; //for header
                    }
                }
            }
            return count;
        }

        /**
         * Removes an item from the favorites list.
         *
         * @param item_id
         * @param viewtype
         */
        public void remove(final String[] item_id, final int viewtype){

            final ContentValues values = new ContentValues();

            switch (viewtype){
                case CAMERAS_VIEWTYPE:
                    ContentResolver resolver = getActivity().getContentResolver();

                    try {
                        values.put(Cameras.CAMERA_IS_STARRED, 0);
                        resolver.update(
                                Cameras.CONTENT_URI,
                                values,
                                Cameras.CAMERA_ID + "=?",
                                item_id
                        );

                    } catch (Exception e) {
                        Log.e("CameraImageFragment", "Error: " + e.getMessage());
                    }
                    break;
                case MOUNTAIN_PASSES_VIEWTYPE:

                    values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, 0);

                    getActivity().getContentResolver().update(
                            MountainPasses.CONTENT_URI,
                            values,
                            MountainPasses._ID + "=?",
                            item_id
                    );
                    break;
                case TRAVEL_TIMES_VIEWTYPE:

                    values.put(TravelTimes.TRAVEL_TIMES_IS_STARRED, 0);

                    getActivity().getContentResolver().update(
                            TravelTimes.CONTENT_URI,
                            values,
                            TravelTimes._ID + "=?",
                            item_id
                    );

                    break;
                case FERRIES_SCHEDULES_VIEWTYPE:
                    values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 0);

                    getActivity().getContentResolver().update(
                            FerriesSchedules.CONTENT_URI,
                            values,
                            FerriesSchedules._ID + "=?",
                            item_id
                    );
                    break;
                case LOCATION_VIEWTYPE:
                    getActivity().getContentResolver().delete(
                            MapLocation.CONTENT_URI,
                            MapLocation._ID + "=?",
                            item_id
                    );
                    break;
                case MY_ROUTE_VIEWTYPE:
                    values.put(MyRoute.MY_ROUTE_IS_STARRED, 0);

                    getActivity().getContentResolver().update(
                            MyRoute.CONTENT_URI,
                            values,
                            MyRoute._ID + "=?",
                            item_id
                    );
                    break;
            }

            notifyDataSetChanged();
        }


        /**
         * Adds a formerly deleted item to the favorites list.
         *
         * @param item_id
         * @param viewtype
         */
        public void undo(final String[] item_id, final int viewtype, final RecyclerView.ViewHolder holder){

            final ContentValues values = new ContentValues();

            switch (viewtype){
                case CAMERAS_VIEWTYPE:
                    ContentResolver resolver = getActivity().getContentResolver();

                    try {
                        values.put(Cameras.CAMERA_IS_STARRED, 1);
                        resolver.update(
                                Cameras.CONTENT_URI,
                                values,
                                Cameras.CAMERA_ID + "=?",
                                item_id
                        );

                    } catch (Exception e) {
                        Log.e("CameraImageFragment", "Error: " + e.getMessage());
                    }
                    break;
                case MOUNTAIN_PASSES_VIEWTYPE:

                    values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, 1);

                    getActivity().getContentResolver().update(
                            MountainPasses.CONTENT_URI,
                            values,
                            MountainPasses._ID + "=?",
                            item_id
                    );
                    break;
                case TRAVEL_TIMES_VIEWTYPE:

                    values.put(TravelTimes.TRAVEL_TIMES_IS_STARRED, 1);

                    getActivity().getContentResolver().update(
                            TravelTimes.CONTENT_URI,
                            values,
                            TravelTimes._ID + "=?",
                            item_id
                    );

                    break;
                case FERRIES_SCHEDULES_VIEWTYPE:
                    values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 1);

                    getActivity().getContentResolver().update(
                            FerriesSchedules.CONTENT_URI,
                            values,
                            FerriesSchedules._ID + "=?",
                            item_id
                    );
                    break;
                case LOCATION_VIEWTYPE:

                    LocationViewHolder locholder = (LocationViewHolder) holder;

                    String title = locholder.title.getText().toString();

                    values.put(WSDOTContract.MapLocation.LOCATION_TITLE, title);
                    values.put(WSDOTContract.MapLocation.LOCATION_LAT, locholder.lat);
                    values.put(WSDOTContract.MapLocation.LOCATION_LONG, locholder.lng);
                    values.put(WSDOTContract.MapLocation.LOCATION_ZOOM, locholder.zoom);

                    getActivity().getContentResolver().insert(
                            WSDOTContract.MapLocation.CONTENT_URI, values);
                    break;
                case MY_ROUTE_VIEWTYPE:
                    values.put(MyRoute.MY_ROUTE_IS_STARRED, 1);

                    getActivity().getContentResolver().update(
                            MyRoute.CONTENT_URI,
                            values,
                            MyRoute._ID + "=?",
                            item_id
                    );
                    break;
                }
            notifyDataSetChanged();
        }

    }

    /**
     * View holders for favorite items
     */
    private class CamViewHolder extends RecyclerView.ViewHolder{
        TextView title;

        public CamViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
        }
    }

    private class PassViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView created_at;
        TextView text;
        CheckBox star_button;

        public PassViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            created_at = (TextView) view.findViewById(R.id.created_at);
            text = (TextView) view.findViewById(R.id.text);
            icon = (ImageView) view.findViewById(R.id.icon);
            star_button = (CheckBox) view.findViewById(R.id.star_button);
        }
    }

    private class TimesViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView current_time;
        TextView distance_average_time;
        TextView updated;
        CheckBox star_button;

        public TimesViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            current_time = (TextView) view.findViewById(R.id.current_time);
            distance_average_time = (TextView) view.findViewById(R.id.distance_average_time);
            updated = (TextView) view.findViewById(R.id.updated);
            star_button = (CheckBox) view.findViewById(R.id.star_button);
        }
    }

    private class FerryViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView text;
        TextView created_at;
        CheckBox star_button;
        ImageButton alert_button;

        public FerryViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            text = (TextView) view.findViewById(R.id.text);
            created_at = (TextView) view.findViewById(R.id.created_at);
            star_button = (CheckBox) view.findViewById(R.id.star_button);
            alert_button = (ImageButton) view.findViewById(R.id.alert_button);
        }
    }

    private class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView latlong;
        int zoom;
        float lat;
        float lng;

        public LocationViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            latlong = (TextView) view.findViewById(R.id.content);
        }
    }

    private class MyRouteViewHolder extends  RecyclerView.ViewHolder {
        TextView title;
        ImageButton alerts_button;
        ImageButton map_button;
        int zoom;
        float lat;
        float lng;

        public MyRouteViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            alerts_button = (ImageButton) view.findViewById(R.id.alert_button);
            map_button = (ImageButton) view.findViewById(R.id.map_button);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LinearLayout divider;

        public HeaderViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.list_header_title);
            divider = (LinearLayout) view.findViewById(R.id.divider);
        }
    }

    /**
     * Sync Receivers
     */
	
	public class MountainPassesSyncReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			
			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(MOUNTAIN_PASSES_LOADER_ID, null, FavoritesFragment.this);
				} else if (responseString.equals("NOP")) {
					// Nothing to do.
				} else {
					Log.e("PassesSyncReceiver", responseString);
				}
			}
		}
	}
	
	public class FerriesSchedulesSyncReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			
			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(FERRIES_SCHEDULES_LOADER_ID, null, FavoritesFragment.this);
				} else if (responseString.equals("NOP")) {
					// Nothing to do.
				} else {
					Log.e("FerriesSyncReceiver", responseString);
				}
			}
		}
	}
	
	public class TravelTimesSyncReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(TRAVEL_TIMES_LOADER_ID, null, FavoritesFragment.this);
				} else if (responseString.equals("NOP")) {
					// Nothing to do.
				} else {
					Log.e("TravelTimesSyncReceiver", responseString);
				}
			}
			swipeRefreshLayout.setRefreshing(false);
		}
	}

    public void onRefresh() {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
        getActivity().startService(mFerriesSchedulesIntent);
        getActivity().startService(mMountainPassesIntent);
        getActivity().startService(mTravelTimesIntent);        
    }
}