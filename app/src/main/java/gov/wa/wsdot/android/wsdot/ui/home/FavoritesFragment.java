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
package gov.wa.wsdot.android.wsdot.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.tollratesign.tolltrips.TollTripEntity;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraViewPagerActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerriesRouteSchedulesDayDeparturesActivity;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.report.MyRouteReportActivity;
import gov.wa.wsdot.android.wsdot.ui.tollrates.I405TollRatesFragment;
import gov.wa.wsdot.android.wsdot.ui.tollrates.SR167TollRatesFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;
import gov.wa.wsdot.android.wsdot.ui.traveltimes.TravelTimesFragment;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByDirection;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByLocation;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByStateRoute;

public class FavoritesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        Injectable {

    private final String TAG = getClass().getSimpleName();

	private View mEmptyView;

    private FavItemAdapter mFavoritesAdapter;

	private SwipeRefreshLayout swipeRefreshLayout;

    public static final int MY_ROUTE_VIEWTYPE = 0;
    public static final int CAMERAS_VIEWTYPE = 1;
    public static final int MOUNTAIN_PASSES_VIEWTYPE = 2;
    public static final int TRAVEL_TIMES_VIEWTYPE = 3;
    public static final int FERRIES_SCHEDULES_VIEWTYPE = 4;
    public static final int LOCATION_VIEWTYPE = 5;
    public static final int TOLL_RATE_VIEWTYPE = 6;
    public static final int BORDER_WAIT_VIEWTYPE = 7;

    private static final int HEADER_VIEWTYPE = 999;

    public static LinkedHashMap headers = new LinkedHashMap<Integer, String>(){
        {
            put(MY_ROUTE_VIEWTYPE, "My Routes");
            put(CAMERAS_VIEWTYPE, "Cameras");
            put(MOUNTAIN_PASSES_VIEWTYPE, "Mountain Passes");
            put(TRAVEL_TIMES_VIEWTYPE, "Travel Times");
            put(FERRIES_SCHEDULES_VIEWTYPE, "Ferries Schedules");
            put(LOCATION_VIEWTYPE, "Locations");
            put(TOLL_RATE_VIEWTYPE, "Toll Rates");
            put(BORDER_WAIT_VIEWTYPE, "Border Waits");
        }
    };

    // used for border waits
    @SuppressLint("UseSparseArrays")
    private static HashMap<Integer, Integer> northboundRouteImage = new HashMap<>();
    private static HashMap<Integer, Integer> southboundRouteImage = new HashMap<>();

    private int orderedViewTypes[] = new int[8];

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    FavoritesViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

        northboundRouteImage.put(5, R.drawable.ic_list_i5);
        northboundRouteImage.put(9, R.drawable.ic_list_sr9);
        northboundRouteImage.put(539, R.drawable.ic_list_sr539);
        northboundRouteImage.put(543, R.drawable.ic_list_sr543);
        northboundRouteImage.put(97, R.drawable.ic_list_us97);

        southboundRouteImage.put(5, R.drawable.ic_list_bc99);
        southboundRouteImage.put(9, R.drawable.ic_list_bc11);
        southboundRouteImage.put(539, R.drawable.ic_list_bc13);
        southboundRouteImage.put(543, R.drawable.ic_list_bc15);

	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_favorites, null);

        // Check preferences and set defaults if none set
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        orderedViewTypes[0] = settings.getInt(getResources().getString(R.string.favorites_one), MY_ROUTE_VIEWTYPE);
        orderedViewTypes[1] = settings.getInt(getResources().getString(R.string.favorites_two), CAMERAS_VIEWTYPE);
        orderedViewTypes[2] = settings.getInt(getResources().getString(R.string.favorites_three), FERRIES_SCHEDULES_VIEWTYPE);
        orderedViewTypes[3] = settings.getInt(getResources().getString(R.string.favorites_four), MOUNTAIN_PASSES_VIEWTYPE);
        orderedViewTypes[4] = settings.getInt(getResources().getString(R.string.favorites_five), TRAVEL_TIMES_VIEWTYPE);
        orderedViewTypes[5] = settings.getInt(getResources().getString(R.string.favorites_six), LOCATION_VIEWTYPE);
        orderedViewTypes[6] = settings.getInt(getResources().getString(R.string.favorites_seven), TOLL_RATE_VIEWTYPE);
        orderedViewTypes[7] = settings.getInt(getResources().getString(R.string.favorites_eight), BORDER_WAIT_VIEWTYPE);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
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

                final String item_id;
                final int viewType = mFavoritesAdapter.getItemViewType(holder.getAdapterPosition());

                //Keeps holders data for undo
                holder.setIsRecyclable(false);

                //get the camera id or tag for the item being removed.
                switch (viewType){
                    case CAMERAS_VIEWTYPE:
                        CameraEntity camera = (CameraEntity) mFavoritesAdapter.getItem(holder.getAdapterPosition());
                        item_id = String.valueOf(camera.getCameraId());
                        break;
                    case FERRIES_SCHEDULES_VIEWTYPE:
                        FerryScheduleEntity schedule = (FerryScheduleEntity) mFavoritesAdapter.getItem(holder.getAdapterPosition());
                        item_id = String.valueOf(schedule.getFerryScheduleId());
                        break;
                    case TRAVEL_TIMES_VIEWTYPE:
                        TravelTimeGroup group = (TravelTimeGroup) mFavoritesAdapter.getItem(holder.getAdapterPosition());
                        item_id = String.valueOf(group.trip.getTitle());
                        break;
                    case MOUNTAIN_PASSES_VIEWTYPE:
                        MountainPassEntity pass = (MountainPassEntity) mFavoritesAdapter.getItem(holder.getAdapterPosition());
                        item_id = String.valueOf(pass.getPassId());
                        break;
                    case LOCATION_VIEWTYPE:
                        MapLocationEntity location = (MapLocationEntity) mFavoritesAdapter.getItem(holder.getAdapterPosition());
                        item_id = String.valueOf(location.getLocationId());
                        break;
                    case MY_ROUTE_VIEWTYPE:
                        MyRouteEntity myRoute = (MyRouteEntity) mFavoritesAdapter.getItem(holder.getAdapterPosition());
                        item_id = String.valueOf(myRoute.getMyRouteId());
                        break;
                    case TOLL_RATE_VIEWTYPE:
                        TollRateGroup tollRateGroup = (TollRateGroup) mFavoritesAdapter.getItem((holder.getAdapterPosition()));
                        item_id = String.valueOf(tollRateGroup.tollRateSign.getId());
                        break;
                    case BORDER_WAIT_VIEWTYPE:
                        BorderWaitEntity borderWait = (BorderWaitEntity) mFavoritesAdapter.getItem((holder.getAdapterPosition()));
                        item_id = String.valueOf(borderWait.getBorderWaitId());
                        break;
                    default:
                        item_id = null;
                }

                mFavoritesAdapter.remove(item_id, viewType);

                // Display snack bar with undo button
                final Snackbar snackbar = Snackbar
                        .make(mRecyclerView, R.string.remove_favorite, Snackbar.LENGTH_LONG);

                snackbar.setAction("UNDO", view -> mFavoritesAdapter.undo(item_id, viewType, holder));
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FavoritesViewModel.class);

        viewModel.getFavoriteCameras().observe(this, cameras -> {
            if (cameras != null) {
                if (cameras.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setCameras(cameras);
            }
        });

        viewModel.getFavoriteFerrySchedules().observe(this, ferrySchedules -> {
            if (ferrySchedules != null){
                if (ferrySchedules.size() > 0){
                    mEmptyView.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setFerrySchedules(ferrySchedules);
            }
        });

        viewModel.getFavoriteTravelTimes().observe(this, travelTimeGroups -> {
            if (travelTimeGroups != null){
                if (travelTimeGroups.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setTravelTimeGroups(travelTimeGroups);
            }
        });

        viewModel.getFavoritePasses().observe(this, passes -> {
            if (passes != null){
                if (passes.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setPasses(passes);
            }
        });

        viewModel.getFavoriteMyRoutes().observe(this, myRoutes -> {
            if (myRoutes != null) {
                if (myRoutes.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setMyRoutes(myRoutes);
            }
        });

        viewModel.getMapLocations().observe(this, mapLocations -> {
            if (mapLocations != null){
                if (mapLocations.size() > 0){
                    mEmptyView.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setMapLocations(mapLocations);
            }
        });

        viewModel.getFavoriteTollRates().observe(this, tollRateGroups -> {
            if (tollRateGroups != null) {

                if (tollRateGroups.size() > 0){
                    mEmptyView.setVisibility(View.GONE);
                }

                Collections.sort(tollRateGroups, new SortTollGroupByLocation());
                Collections.sort(tollRateGroups, new SortTollGroupByDirection());
                Collections.sort(tollRateGroups, new SortTollGroupByStateRoute());

                mFavoritesAdapter.setTollRates(tollRateGroups);
            }
        });

        viewModel.getFavoriteBorderWaits().observe(this, borderWaits -> {
            if (borderWaits != null){
                if (borderWaits.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setBorderWaits(borderWaits);
            }
        });

        viewModel.getFavoritesLoadingTasksCount().observe(this, numTasks -> {
            if (numTasks != null) {

                if (numTasks == 0){
                    swipeRefreshLayout.setRefreshing(false);
                } else if (numTasks < 0) {
                    Log.e(TAG, "numTasks in invalid state");
                } else {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        return root;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	    TextView t = (TextView) mEmptyView;
        t.setText(R.string.no_favorites);
	}

	@Override
	public void onResume() {
		super.onResume();
        mFavoritesAdapter.notifyDataSetChanged();

        // Check preferences and set defaults if none set
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        orderedViewTypes[0] = settings.getInt(getResources().getString(R.string.favorites_one), MY_ROUTE_VIEWTYPE);
        orderedViewTypes[1] = settings.getInt(getResources().getString(R.string.favorites_two), CAMERAS_VIEWTYPE);
        orderedViewTypes[2] = settings.getInt(getResources().getString(R.string.favorites_three), FERRIES_SCHEDULES_VIEWTYPE);
        orderedViewTypes[3] = settings.getInt(getResources().getString(R.string.favorites_four), MOUNTAIN_PASSES_VIEWTYPE);
        orderedViewTypes[4] = settings.getInt(getResources().getString(R.string.favorites_five), TRAVEL_TIMES_VIEWTYPE);
        orderedViewTypes[5] = settings.getInt(getResources().getString(R.string.favorites_six), LOCATION_VIEWTYPE);
        orderedViewTypes[6] = settings.getInt(getResources().getString(R.string.favorites_seven), TOLL_RATE_VIEWTYPE);
        orderedViewTypes[7] = settings.getInt(getResources().getString(R.string.favorites_eight), BORDER_WAIT_VIEWTYPE);


	}

    /**
     * Custom adapter for favorites items in recycler view.
     *
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolders
     * class to it's data.
     *
     * @see RecyclerView.Adapter
     */
    private class FavItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        private List<CameraEntity> mCameras;
        private List<FerryScheduleEntity> mFerrySchedules;
        private List<TravelTimeGroup> mTravelTimeGroups;
        private List<MountainPassEntity> mPasses;
        private List<MyRouteEntity> mMyRoutes;
        private List<MapLocationEntity> mMapLocations;
        private List<TollRateGroup> mTollRates;
        private List<BorderWaitEntity> mBorderWaits;

        FavItemAdapter() {
            this.mCameras = new ArrayList<>();
            this.mFerrySchedules = new ArrayList<>();
            this.mTravelTimeGroups = new ArrayList<>();
            this.mPasses = new ArrayList<>();
            this.mMyRoutes = new ArrayList<>();
            this.mMapLocations = new ArrayList<>();
            this.mTollRates = new ArrayList<>();
            this.mBorderWaits = new ArrayList<>();
        }

        void setCameras(List<CameraEntity> cameras){
            this.mCameras = cameras;
            this.notifyDataSetChanged();
        }

        void setFerrySchedules(List<FerryScheduleEntity> ferrySchedules){
            this.mFerrySchedules = ferrySchedules;
            this.notifyDataSetChanged();
        }

        void setTravelTimeGroups(List<TravelTimeGroup> travelTimeGroups){
            this.mTravelTimeGroups = travelTimeGroups;
            this.notifyDataSetChanged();
        }

        void setPasses(List<MountainPassEntity> passes){
            this.mPasses = passes;
            this.notifyDataSetChanged();
        }

        void setBorderWaits(List<BorderWaitEntity> borderWaits) {
            this.mBorderWaits = borderWaits;
            this.notifyDataSetChanged();
        }

        void setMyRoutes(List<MyRouteEntity> myRoutes){
            this.mMyRoutes = myRoutes;
            this.notifyDataSetChanged();
        }

        void setMapLocations(List<MapLocationEntity> mapLocations){
            this.mMapLocations = mapLocations;
            this.notifyDataSetChanged();
        }

        void setTollRates(List<TollRateGroup> tollRates){
            this.mTollRates = tollRates;
            this.notifyDataSetChanged();
        }

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
                            from(parent.getContext()).inflate(R.layout.list_item_travel_time_group, null);
                    return new TravelTimeViewHolder(itemView);
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
                case TOLL_RATE_VIEWTYPE:
                    itemView = LayoutInflater
                            .from(parent.getContext()).inflate(R.layout.list_item_travel_time_group, null);
                    return new TollRateViewHolder(itemView);
                case BORDER_WAIT_VIEWTYPE:
                    itemView = LayoutInflater
                            .from(parent.getContext()).inflate(R.layout.borderwait_row, null);
                    return new BorderWaitViewHolder(itemView);
                case HEADER_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_header, parent, false);
                    return new HeaderViewHolder(itemView);
                default:
                    MyLogger.crashlyticsLog("Home", "Error", "FavoritesFragment: No matching view type for type: " + viewType, 1);
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
                CameraEntity camera = (CameraEntity) mFavoritesAdapter.getItem(position);
                String title = camera.getTitle();
                viewholder.title.setText(title);
                viewholder.title.setTypeface(tf);

                // Set onClickListener for holder's view
                viewholder.itemView.setOnClickListener(
                        v -> {
                            MyLogger.crashlyticsLog("Home", "Tap", "Favorite Camera " + String.valueOf(camera.getCameraId()), 1);
                            Bundle b = new Bundle();
                            Intent intent = new Intent(getActivity(), CameraViewPagerActivity.class);
                            b.putInt("id", camera.getCameraId());
                            b.putString("advertisingTarget", "other");
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                );

            } else if (holder instanceof PassViewHolder){

                PassViewHolder viewHolder = (PassViewHolder) holder;

                MountainPassEntity pass = (MountainPassEntity) mFavoritesAdapter.getItem(position);

                String title = pass.getName();
                viewHolder.title.setText(title);
                viewHolder.title.setTypeface(tfb);

                String created_at = pass.getDateUpdated();
                viewHolder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
                viewHolder.created_at.setTypeface(tf);

                String text = pass.getWeatherCondition();

                if (text.equals("")) {
                    viewHolder.text.setVisibility(View.GONE);
                } else {
                    viewHolder.text.setVisibility(View.VISIBLE);
                    viewHolder.text.setText(text);
                    viewHolder.text.setTypeface(tf);
                }

                int icon = getResources().getIdentifier(pass.getWeatherIcon(),
                        "drawable", getActivity().getPackageName());
                viewHolder.icon.setImageResource(icon);

                viewHolder.star_button.setVisibility(View.GONE);
                viewHolder.star_button.setTag(pass.getPassId());


                // Set onClickListener for holder's view
                viewHolder.itemView.setOnClickListener(
                        v -> {
                            MyLogger.crashlyticsLog("Home", "Tap", "Favorite Pass " + String.valueOf(pass.getPassId()), 1);
                            Bundle b = new Bundle();
                            Intent intent = new Intent(getActivity(), MountainPassItemActivity.class);
                            b.putInt("id", pass.getPassId());
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                );
            } else if (holder instanceof TravelTimeViewHolder){

                TravelTimeViewHolder viewholder = (TravelTimeViewHolder) holder;

                TravelTimeGroup travelTimeGroup = (TravelTimeGroup) mFavoritesAdapter.getItem(position);

                final String title = travelTimeGroup.trip.getTitle();
                viewholder.title.setText(title);
                viewholder.title.setTypeface(tfb);

                viewholder.travel_times_layout.removeAllViews();

                for (TravelTimeEntity time: travelTimeGroup.travelTimes) {

                    View travelTimeView = TravelTimesFragment.makeTravelTimeView(time, getContext());

                    if (travelTimeGroup.travelTimes.indexOf(time) == travelTimeGroup.travelTimes.size() - 1){
                        travelTimeView.findViewById(R.id.line).setVisibility(View.GONE);
                    }

                    viewholder.travel_times_layout.addView(travelTimeView);
                }

                viewholder.star_button.setVisibility(View.GONE);

            } else if (holder instanceof FerryViewHolder){

                FerryScheduleEntity schedule = (FerryScheduleEntity) mFavoritesAdapter.getItem(position);

                FerryViewHolder viewHolder = (FerryViewHolder) holder;

                viewHolder.title.setText(schedule.getTitle());
                viewHolder.title.setTypeface(tfb);

                String text = schedule.getCrossingTime();

                // Set onClickListener for holder's view
                viewHolder.itemView.setOnClickListener(
                        v -> {

                            Bundle b = new Bundle();
                            Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDayDeparturesActivity.class);
                            b.putInt("scheduleId", schedule.getFerryScheduleId());
                            b.putString("title", schedule.getTitle());
                            b.putInt("isStarred", schedule.getIsStarred());
                            intent.putExtras(b);

                            startActivity(intent);
                        }
                );

                try {
                    if (text.equalsIgnoreCase("null")) {
                        viewHolder.text.setText("");
                    } else {
                        viewHolder.text.setText("Crossing Time: ~ " + text + " min");
                        viewHolder.text.setTypeface(tf);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                String created_at = schedule.getUpdated();

                // Try to read the created at field in the old format,
                // it that fails, assume we are using the new format.
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
                    viewHolder.created_at.setText(ParserUtils.relativeTime(
                            dateFormat.format(new Date(Long.parseLong(created_at.substring(6, 19)))),
                            "MMMM d, yyyy h:mm a",
                            false));

                } catch (Exception e) {
                    viewHolder.created_at.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", false));
                }

                viewHolder.star_button.setTag(schedule.getFerryScheduleId());
                viewHolder.star_button.setVisibility(View.GONE);

                String alerts = schedule.getAlert();

                viewHolder.alert_button.setVisibility(View.GONE);


            } else if (holder instanceof LocationViewHolder) {
                final LocationViewHolder viewholder = (LocationViewHolder) holder;
                MapLocationEntity location = (MapLocationEntity) mFavoritesAdapter.getItem(position);

                String title = location.getTitle();
                viewholder.title.setText(title);
                viewholder.title.setTypeface(tf);

                viewholder.lng = location.getLatitude();
                viewholder.lat = location.getLongitude();
                viewholder.zoom = location.getZoom();

                String latlong = String.format("%.2f, %.2f", viewholder.lat, viewholder.lng);

                viewholder.latlong.setText(latlong);
                viewholder.latlong.setTypeface(tf);

                viewholder.title.setTag(location.getLocationId());

                // Set onClickListener for holder's view
                viewholder.itemView.setOnClickListener(
                        v -> {

                            MyLogger.crashlyticsLog("Home", "Tap", "Favorite Location", 1);

                            Bundle b = new Bundle();
                            Intent intent = new Intent(getActivity(), TrafficMapActivity.class);

                            b.putDouble("lat", location.getLatitude());
                            b.putDouble("long", location.getLongitude());
                            b.putInt("zoom", location.getZoom());

                            intent.putExtras(b);
                            startActivity(intent);
                        }
                );

                viewholder.itemView.setOnLongClickListener(
                        v -> {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.WSDOT_popup);

                            final EditText textEntryView = new EditText(getContext());
                            textEntryView.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder.setView(textEntryView);
                            builder.setMessage(R.string.rename_location_dialog);
                            builder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.dismiss());
                            builder.setPositiveButton(R.string.submit, (dialog, whichButton) -> {
                                String value = textEntryView.getText().toString();
                                dialog.dismiss();
                                editTitle(String.valueOf(location.getLocationId()), LOCATION_VIEWTYPE, value);
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            return true;
                        }
                );

            } else if (holder instanceof TollRateViewHolder) {

                TollRateViewHolder viewholder = (TollRateViewHolder) holder;

                TollRateGroup tollRateGroup = (TollRateGroup) mFavoritesAdapter.getItem(position);

                String routeType;
                switch (tollRateGroup.tollRateSign.getStateRoute()) {
                    case 405:
                        routeType = "I-";
                        break;
                    case 167:
                        routeType = "SR ";
                        break;
                    default:
                        routeType = "";
                        break;
                }
                String title = routeType
                        .concat(String.valueOf(tollRateGroup.tollRateSign.getStateRoute()))
                        .concat(" - ")
                        .concat(tollRateGroup.tollRateSign.getLocationName());

                viewholder.title.setText(title);
                viewholder.title.setTypeface(tfb);


                viewholder.travel_times_layout.removeAllViews();

                // make a trip view with toll rate for each trip in the group
                for (TollTripEntity trip : tollRateGroup.trips) {

                    View tripView;

                    switch (tollRateGroup.tollRateSign.getStateRoute()) {
                        case 405:
                            tripView = I405TollRatesFragment.makeTripView(trip, tollRateGroup.tollRateSign, getContext());
                            // remove the line from the last trip
                            if (tollRateGroup.trips.indexOf(trip) == tollRateGroup.trips.size() - 1) {
                                tripView.findViewById(R.id.line).setVisibility(View.GONE);
                            }
                            break;
                        case 167:
                            tripView = SR167TollRatesFragment.makeTripView(trip, tollRateGroup.tollRateSign, getContext());
                            // remove the line from the last trip
                            if (tollRateGroup.trips.indexOf(trip) == tollRateGroup.trips.size() - 1) {
                                tripView.findViewById(R.id.line).setVisibility(View.GONE);
                            }
                            break;
                        default:
                            tripView = null;
                            break;
                    }

                    viewholder.travel_times_layout.addView(tripView);
                }

                viewholder.star_button.setVisibility(View.GONE);

            } else if (holder instanceof BorderWaitViewHolder) {

                BorderWaitViewHolder viewHolder = (BorderWaitViewHolder) holder;

                BorderWaitEntity waitItem = (BorderWaitEntity) mFavoritesAdapter.getItem(position);

                String title = waitItem.getTitle();
                String lane = waitItem.getLane();

                viewHolder.tt.setText(title + " (" + lane + ")");
                viewHolder.tt.setTypeface(tfb);

                String created_at = waitItem.getUpdated();
                viewHolder.bt.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", true));
                viewHolder.bt.setTypeface(tf);

                int wait = waitItem.getWait();
                if (wait == -1) {
                    viewHolder.rt.setText("N/A");
                } else if (wait < 5) {
                    viewHolder.rt.setText("< 5 min");
                } else {
                    viewHolder.rt.setText(wait + " min");
                }

                viewHolder.rt.setTypeface(tfb);

                if (waitItem.getDirection().equals("northbound")) {
                    viewHolder.iv.setImageResource(northboundRouteImage.get(waitItem.getRoute()));
                } else if (waitItem.getDirection().equals("southbound")) {
                    viewHolder.iv.setImageResource(southboundRouteImage.get(waitItem.getRoute()));
                }

                viewHolder.star.setVisibility(View.GONE);

            } else if (holder instanceof MyRouteViewHolder) {

                MyRouteViewHolder viewholder = (MyRouteViewHolder) holder;
                MyRouteEntity myRoute = (MyRouteEntity) mFavoritesAdapter.getItem(position);

                String title = myRoute.getTitle();

                viewholder.title.setText(title);
                viewholder.title.setTypeface(tf);

                viewholder.title.setTag(myRoute.getMyRouteId());

                // Set onClickListener for holder's view
                viewholder.view.setOnClickListener(
                        v -> {
                            Bundle b = new Bundle();
                            b.putLong("route_id", myRoute.getMyRouteId());
                            b.putString("route_name", myRoute.getTitle());
                            b.putString("route", myRoute.getRouteLocations());
                            Intent intent = new Intent(getActivity(), MyRouteReportActivity.class);
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                );


            } else {
                MyLogger.crashlyticsLog("Home", "Error", "FavoritesFragment: No view holder for type: " + holder.getClass().getName(), 1);
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

                int size = 0;
                switch (viewType) {
                    case CAMERAS_VIEWTYPE:
                        size = mCameras.size() + (mCameras.size() > 0 ? 1 : 0);
                        break;
                    case FERRIES_SCHEDULES_VIEWTYPE:
                        size = mFerrySchedules.size() + (mFerrySchedules.size() > 0 ? 1 : 0);
                        break;
                    case TRAVEL_TIMES_VIEWTYPE:
                        size = mTravelTimeGroups.size() + (mTravelTimeGroups.size() > 0 ? 1 : 0);
                        break;
                    case MOUNTAIN_PASSES_VIEWTYPE:
                        size = mPasses.size() + (mPasses.size() > 0 ? 1 : 0);
                        break;
                    case LOCATION_VIEWTYPE:
                        size = mMapLocations.size() + (mMapLocations.size() > 0 ? 1 : 0);
                        break;
                    case MY_ROUTE_VIEWTYPE:
                        size = mMyRoutes.size() + (mMyRoutes.size() > 0 ? 1 : 0);
                        break;
                    case TOLL_RATE_VIEWTYPE:
                        size = mTollRates.size() + (mTollRates.size() > 0 ? 1 : 0);
                        break;
                    case BORDER_WAIT_VIEWTYPE:
                        size = mBorderWaits.size() + (mBorderWaits.size() > 0 ? 1 : 0);
                        break;
                    default:
                        break;
                }

                // check if position inside this section
                if (position == 0 && size > 0) return HEADER_VIEWTYPE;
                if (position < size) return viewType;

                position -= size;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("-");
            for (int viewType : orderedViewTypes) {
                sb.append(String.valueOf(viewType));
                sb.append("-");
            }
            MyLogger.crashlyticsLog("Home", "error", "FavoritesFragment: orderViewTypes = " + sb.toString(), 1);

            return -1;
        }

        /**
         * Returns the item at a given position. This could either be an Entity
         * or a section header (a String).
         * @param position
         * @return
         */
        public Object getItem(int position){

            for (int viewType : orderedViewTypes) {

                int size = 0;

                switch (viewType) {
                    case CAMERAS_VIEWTYPE:
                        size = mCameras.size() + (mCameras.size() > 0 ? 1 : 0);
                        break;
                    case FERRIES_SCHEDULES_VIEWTYPE:
                        size = mFerrySchedules.size() + (mFerrySchedules.size() > 0 ? 1 : 0);
                        break;
                    case TRAVEL_TIMES_VIEWTYPE:
                        size = mTravelTimeGroups.size() + (mTravelTimeGroups.size() > 0 ? 1 : 0);
                        break;
                    case MOUNTAIN_PASSES_VIEWTYPE:
                        size = mPasses.size() + (mPasses.size() > 0 ? 1 : 0);
                        break;
                    case LOCATION_VIEWTYPE:
                        size = mMapLocations.size() + (mMapLocations.size() > 0 ? 1 : 0);
                        break;
                    case MY_ROUTE_VIEWTYPE:
                        size = mMyRoutes.size() + (mMyRoutes.size() > 0 ? 1 : 0);
                        break;
                    case TOLL_RATE_VIEWTYPE:
                        size = mTollRates.size() + (mTollRates.size() > 0 ? 1 : 0);
                        break;
                    case BORDER_WAIT_VIEWTYPE:
                        size = mBorderWaits.size() + (mBorderWaits.size() > 0 ? 1 : 0);
                        break;
                    default:
                        break;
                }

                // check if position inside this section
                if(position == 0 && size > 0) return headers.get(viewType);

                if(position < size) {
                    switch (viewType) {
                        case CAMERAS_VIEWTYPE:
                            return mCameras.get(position - 1); // - 1 for header
                        case FERRIES_SCHEDULES_VIEWTYPE:
                            return mFerrySchedules.get(position - 1);
                        case TRAVEL_TIMES_VIEWTYPE:
                            return mTravelTimeGroups.get(position - 1);
                        case MOUNTAIN_PASSES_VIEWTYPE:
                            return mPasses.get(position - 1);
                        case LOCATION_VIEWTYPE:
                            return mMapLocations.get(position - 1);
                        case MY_ROUTE_VIEWTYPE:
                            return mMyRoutes.get(position - 1);
                        case TOLL_RATE_VIEWTYPE:
                            return mTollRates.get(position - 1);
                        case BORDER_WAIT_VIEWTYPE:
                            return mBorderWaits.get(position - 1);
                        default:
                            break;
                    }
                }

                // otherwise jump into next section
                position -= size;

            }
            return null;
        }

        @Override
        public int getItemCount() {
            int count = 0;

            count += mCameras.size() + (mCameras.size() > 0 ? 1 : 0); // + 1 for header
            count += mFerrySchedules.size() + (mFerrySchedules.size() > 0 ? 1 : 0);
            count += mTravelTimeGroups.size() + (mTravelTimeGroups.size() > 0 ? 1 : 0);
            count += mPasses.size() + (mPasses.size() > 0 ? 1 : 0);
            count += mMapLocations.size() + (mMapLocations.size() > 0 ? 1 : 0);
            count += mMyRoutes.size() + (mMyRoutes.size() > 0 ? 1 : 0);
            count += mTollRates.size() + (mTollRates.size() > 0 ? 1 : 0);
            count += mBorderWaits.size() + (mBorderWaits.size() > 0 ? 1 : 0);

            return count;
        }

        /**
         * Edits an item form the favorites list.
         */
        public void editTitle(final String item_id, final int viewtype, final String newTitle){

            switch (viewtype){
                case CAMERAS_VIEWTYPE:
                    break;
                case MOUNTAIN_PASSES_VIEWTYPE:
                    break;
                case TRAVEL_TIMES_VIEWTYPE:
                    break;
                case FERRIES_SCHEDULES_VIEWTYPE:
                    break;
                case LOCATION_VIEWTYPE:
                    viewModel.editMapLocationName(Integer.valueOf(item_id), newTitle);
                    break;
                case MY_ROUTE_VIEWTYPE:
                    break;
                case TOLL_RATE_VIEWTYPE:
                    break;
                case BORDER_WAIT_VIEWTYPE:
                    break;
            }
            notifyDataSetChanged();
        }

        /**
         * Removes an item from the favorites list.
         *
         * @param item_id
         * @param viewtype
         */
        public void remove(final String item_id, final int viewtype){

            switch (viewtype){
                case CAMERAS_VIEWTYPE:
                    viewModel.setCameraIsStarred(Integer.valueOf(item_id), 0);
                    break;
                case MOUNTAIN_PASSES_VIEWTYPE:
                    viewModel.setPassIsStarred(Integer.valueOf(item_id), 0);
                    break;
                case TRAVEL_TIMES_VIEWTYPE:
                    viewModel.setTravelTimeIsStarred(item_id, 0);
                    break;
                case FERRIES_SCHEDULES_VIEWTYPE:
                    viewModel.setFerryScheduleIsStarred(Integer.valueOf(item_id), 0);
                    break;
                case LOCATION_VIEWTYPE:
                    viewModel.deleteMapLocation(Integer.valueOf(item_id));
                    break;
                case MY_ROUTE_VIEWTYPE:
                    viewModel.setMyRouteIsStarred(Integer.valueOf(item_id), 0);
                    break;
                case TOLL_RATE_VIEWTYPE:
                    viewModel.setTollRateIsStarred(item_id, 0);
                case BORDER_WAIT_VIEWTYPE:
                    viewModel.setBorderWaitIsStarred(Integer.valueOf(item_id), 0);
            }
            notifyDataSetChanged();
        }

        /**
         * Adds a formerly deleted item to the favorites list.
         *
         * @param item_id
         * @param viewtype
         */
        public void undo(final String item_id, final int viewtype, final RecyclerView.ViewHolder holder){

            switch (viewtype){
                case CAMERAS_VIEWTYPE:
                    viewModel.setCameraIsStarred(Integer.valueOf(item_id), 1);
                    break;
                case MOUNTAIN_PASSES_VIEWTYPE:
                    viewModel.setPassIsStarred(Integer.valueOf(item_id), 1);
                    break;
                case TRAVEL_TIMES_VIEWTYPE:
                    viewModel.setTravelTimeIsStarred(item_id, 1);
                    break;
                case BORDER_WAIT_VIEWTYPE:
                    viewModel.setBorderWaitIsStarred(Integer.valueOf(item_id), 1);
                    break;
                case FERRIES_SCHEDULES_VIEWTYPE:
                    viewModel.setFerryScheduleIsStarred(Integer.valueOf(item_id), 1);
                    break;
                case LOCATION_VIEWTYPE:

                    LocationViewHolder locHolder = (LocationViewHolder) holder;

                    String title = locHolder.title.getText().toString();
                    MapLocationEntity recoveredLocation = new MapLocationEntity();

                    recoveredLocation.setTitle(title);
                    recoveredLocation.setLatitude(locHolder.lat);
                    recoveredLocation.setLongitude(locHolder.lng);
                    recoveredLocation.setZoom(locHolder.zoom);

                    viewModel.addMapLocation(recoveredLocation);
                    break;
                case MY_ROUTE_VIEWTYPE:
                    viewModel.setMyRouteIsStarred(Integer.valueOf(item_id), 1);
                    break;
                case TOLL_RATE_VIEWTYPE:
                    viewModel.setTollRateIsStarred(item_id, 1);
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
            title = view.findViewById(R.id.title);
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
            title = view.findViewById(R.id.title);
            created_at = view.findViewById(R.id.created_at);
            text = view.findViewById(R.id.text);
            icon = view.findViewById(R.id.icon);
            star_button = view.findViewById(R.id.star_button);
        }
    }

    private class TravelTimeViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout travel_times_layout;
        public TextView title;
        public CheckBox star_button;

        public TravelTimeViewHolder(View view) {
            super(view);
            travel_times_layout = view.findViewById(R.id.travel_times_linear_layout);
            title = view.findViewById(R.id.title);
            star_button = view.findViewById(R.id.star_button);
        }
    }

    private class TollRateViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout travel_times_layout;
        public TextView title;
        public CheckBox star_button;

        public TollRateViewHolder(View view) {
            super(view);
            travel_times_layout = view.findViewById(R.id.travel_times_linear_layout);
            title = view.findViewById(R.id.title);
            star_button = view.findViewById(R.id.star_button);
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
            title = view.findViewById(R.id.title);
            text = view.findViewById(R.id.text);
            created_at = view.findViewById(R.id.created_at);
            star_button = view.findViewById(R.id.star_button);
            alert_button = view.findViewById(R.id.alert_button);
        }
    }

    private class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView latlong;
        int zoom;
        double lat;
        double lng;

        public LocationViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            latlong = view.findViewById(R.id.content);
        }
    }

    // View Holder for list items.
    private class BorderWaitViewHolder extends RecyclerView.ViewHolder {
        TextView tt;
        TextView bt;
        TextView rt;
        ImageView iv;
        CheckBox star;

        public BorderWaitViewHolder(View view) {
            super(view);
            tt = view.findViewById(R.id.toptext);
            bt = view.findViewById(R.id.bottomtext);
            rt = view.findViewById(R.id.righttext);
            iv = view.findViewById(R.id.icon);
            star = view.findViewById(R.id.star_button);
        }
    }

    private class MyRouteViewHolder extends  RecyclerView.ViewHolder {
        TextView title;
        public View view;

        public MyRouteViewHolder(View itemView){
            super(itemView);
            view = itemView;
            title = itemView.findViewById(R.id.title);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LinearLayout divider;

        public HeaderViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.list_header_title);
            divider = view.findViewById(R.id.divider);
        }
    }


    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.forceRefresh();
    }
}