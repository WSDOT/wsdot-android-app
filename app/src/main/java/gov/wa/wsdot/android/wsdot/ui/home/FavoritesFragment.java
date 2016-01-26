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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.service.FerriesSchedulesSyncService;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.FerriesRouteAlertsBulletinsActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.FerriesRouteSchedulesDaySailingsActivity;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassItemActivity;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FavoritesFragment extends BaseFragment implements
        LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

	private View mEmptyView;
	
	private Intent mFerriesSchedulesIntent;
	private Intent mMountainPassesIntent;
	private Intent mTravelTimesIntent;
	
	private static SwipeRefreshLayout swipeRefreshLayout;
	
	private MountainPassesSyncReceiver mMountainPassesSyncReceiver;
	private FerriesSchedulesSyncReceiver mFerriesSchedulesSyncReceiver;
	private TravelTimesSyncReceiver mTravelTimesSyncReceiver;
	
	private Tracker mTracker;

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
	
	private static final int CAMERAS_LOADER_ID = 0;
	private static final int MOUNTAIN_PASSES_LOADER_ID = 1;
	private static final int TRAVEL_TIMES_LOADER_ID = 2;
	private static final int FERRIES_SCHEDULES_LOADER_ID = 3;

    private CameraAdapter mCameraAdapter;
    private MountainPassAdapter mMountainPassAdapter;
    private TravelTimesAdapter mTravelTimesAdapter;
    private RouteSchedulesAdapter mFerriesSchedulesAdapter;

    //RecyclerView for each favorites category
    protected RecyclerView passRecyclerView;
    protected RecyclerView camRecyclerView;
    protected RecyclerView ferriesRecyclerView;
    protected RecyclerView timesRecyclerView;

    protected LinearLayoutManager passLayoutManager;
    protected LinearLayoutManager camLayoutManager;
    protected LinearLayoutManager ferriesLayoutManager;
    protected LinearLayoutManager timesLayoutManager;

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


        passRecyclerView = (RecyclerView) root.findViewById(R.id.pass_recycler_view);
        passRecyclerView.setHasFixedSize(true);
        passLayoutManager = new LinearLayoutManager(getActivity());
        passLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        passRecyclerView.setLayoutManager(passLayoutManager);
        mMountainPassAdapter = new MountainPassAdapter(getContext(), null);

        passRecyclerView.setAdapter(mMountainPassAdapter);

        passRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        /////
        camRecyclerView = (RecyclerView) root.findViewById(R.id.cameras_recycler_view);
        camRecyclerView.setHasFixedSize(true);
        camLayoutManager = new LinearLayoutManager(getActivity());
        camLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        camRecyclerView.setLayoutManager(camLayoutManager);
        mCameraAdapter = new CameraAdapter(getContext(), null);

        camRecyclerView.setAdapter(mCameraAdapter);

        camRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        /////
        ferriesRecyclerView = (RecyclerView) root.findViewById(R.id.ferry_recycler_view);
        ferriesRecyclerView.setHasFixedSize(true);
        ferriesLayoutManager = new LinearLayoutManager(getActivity());
        ferriesLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ferriesRecyclerView.setLayoutManager(ferriesLayoutManager);
        mFerriesSchedulesAdapter = new RouteSchedulesAdapter(getContext(), null);

        ferriesRecyclerView.setAdapter(mFerriesSchedulesAdapter);

        ferriesRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        /////
        timesRecyclerView = (RecyclerView) root.findViewById(R.id.times_recycler_view);
        timesRecyclerView.setHasFixedSize(true);
        timesLayoutManager = new LinearLayoutManager(getActivity());
        timesLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        timesRecyclerView.setLayoutManager(timesLayoutManager);
        mTravelTimesAdapter = new TravelTimesAdapter(getContext(), null);

        timesRecyclerView.setAdapter(mTravelTimesAdapter);

        timesRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        /////




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

	    TextView t = (TextView) mEmptyView;
		t.setText(R.string.no_favorites);

        mEmptyView.setVisibility(View.GONE);
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
	    case 0:
			cursorLoader = new CursorLoader(
					getActivity(),
					Cameras.CONTENT_URI,
					cameras_projection,
					Cameras.CAMERA_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
	    case 1:
			cursorLoader = new CursorLoader(
					getActivity(),
					MountainPasses.CONTENT_URI,
					mountain_passes_projection,
					MountainPasses.MOUNTAIN_PASS_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
	    case 2:
			cursorLoader = new CursorLoader(
					getActivity(),
					TravelTimes.CONTENT_URI,
					travel_times_projection,
					TravelTimes.TRAVEL_TIMES_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
	    case 3:
			cursorLoader = new CursorLoader(
					getActivity(),
					FerriesSchedules.CONTENT_URI,
					ferries_schedules_projection,
					FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED + "=?",
					new String[] {Integer.toString(1)},
					null
					);
			break;
		}

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        cursor.moveToFirst();

		switch(loader.getId()) {
		case 0:
			mCameraAdapter.swapCursor(cursor);
            if (mCameraAdapter.getItemCount() == 0) {
                camRecyclerView.setVisibility(View.GONE);
            }else{
                camRecyclerView.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = camRecyclerView.getLayoutParams();

                params.height = mCameraAdapter.getItemCount() * 275;
                camRecyclerView.setLayoutParams(params);

            }
            break;
		case 1:
			mMountainPassAdapter.swapCursor(cursor);
            if (mMountainPassAdapter.getItemCount() == 0) {
                passRecyclerView.setVisibility(View.GONE);
            }else{
                passRecyclerView.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams params = passRecyclerView.getLayoutParams();

                params.height = mMountainPassAdapter.getItemCount() * 275;
                passRecyclerView.setLayoutParams(params);

            }
			break;
		case 2:
			mTravelTimesAdapter.swapCursor(cursor);
            if (mTravelTimesAdapter.getItemCount() == 0) {
                timesRecyclerView.setVisibility(View.GONE);
            }else{
                timesRecyclerView.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = timesRecyclerView.getLayoutParams();

                params.height = mTravelTimesAdapter.getItemCount() * 275;
                timesRecyclerView.setLayoutParams(params);
            }
			break;
		case 3:
			mFerriesSchedulesAdapter.swapCursor(cursor);
            if (mFerriesSchedulesAdapter.getItemCount() == 0) {
                ferriesRecyclerView.setVisibility(View.GONE);
            }else{
                ferriesRecyclerView.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams params = ferriesRecyclerView.getLayoutParams();

                params.height = mFerriesSchedulesAdapter.getItemCount() * 275;
                ferriesRecyclerView.setLayoutParams(params);

            }
			break;
		}

		swipeRefreshLayout.setRefreshing(false);
		
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	    
	    switch(loader.getId()) {
		case 0:
			mCameraAdapter.swapCursor(null);
			break;
		case 1:
			mMountainPassAdapter.swapCursor(null);
			break;
		case 2:
			mTravelTimesAdapter.swapCursor(null);
			break;
		case 3:
			mFerriesSchedulesAdapter.swapCursor(null);
			break;
		}
	}

    /**
     * Custom adapter for items in recycler view that need a cursor adapter.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see CursorRecyclerAdapter
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
	public class CameraAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
	    private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Context mContext;

		public CameraAdapter(Context context, Cursor c) {
			super(c);
            mContext = context;
        }

        @Override
        public void onBindViewHolderCursor(RecyclerView.ViewHolder viewholder, Cursor cursor) {
            FavCamsVH holder = (FavCamsVH) viewholder;


            String title = cursor.getString(cursor.getColumnIndex(Cameras.CAMERA_TITLE));
            holder.title.setText(title);
            holder.title.setTypeface(tf);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
            FavCamsVH viewholder = new FavCamsVH(view);
            view.setTag(new Object[]{viewholder, "camera"});
            return viewholder;
        }

        private class FavCamsVH extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            int itemId;

            public FavCamsVH(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                view.setOnClickListener(this);
            }

            public void setCursorPos(int position){
                this.itemId = position;
            }

            public void onClick(View v) {
                Cursor c = mCameraAdapter.getCursor();
                c.moveToPosition(itemId);
                Bundle b = new Bundle();
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                b.putInt("id", c.getInt(c.getColumnIndex(Cameras.CAMERA_ID)));
                intent.putExtras(b);
                startActivity(intent);
            }

        }
	}

    /**
     * Custom adapter for items in recycler view that need a cursor adapter.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see CursorRecyclerAdapter
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class MountainPassAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;
        private List<FavPassVH> mItems = new ArrayList<>();

        public MountainPassAdapter(Context context, Cursor c) {
            super(c);
            this.context = context;
        }

        @Override
        public void onBindViewHolderCursor(RecyclerView.ViewHolder viewHolder, Cursor cursor) {

            FavPassVH mtpassVH = (FavPassVH) viewHolder;

            String title = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_NAME));
            mtpassVH.title.setText(title);
            mtpassVH.title.setTypeface(tfb);

            mtpassVH.setCursorPos(cursor.getPosition());

            String created_at = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_DATE_UPDATED));
            mtpassVH.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
            mtpassVH.created_at.setTypeface(tf);

            String text = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION));

            if (text.equals("")) {
                mtpassVH.text.setVisibility(View.GONE);
            } else {
                mtpassVH.text.setVisibility(View.VISIBLE);
                mtpassVH.text.setText(text);
                mtpassVH.text.setTypeface(tf);
            }

            int icon = cursor.getInt(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_ICON));
            mtpassVH.icon.setImageResource(icon);

            mtpassVH.star_button.setVisibility(View.GONE);
        }

        @Override
        public FavPassVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_details_with_icon, null);
            FavPassVH viewholder = new FavPassVH(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        // View Holder for Mt pass list items.
        private class FavPassVH extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView icon;
            TextView title;
            TextView created_at;
            TextView text;
            CheckBox star_button;
            int itemId;

            public FavPassVH(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                created_at = (TextView) view.findViewById(R.id.created_at);
                text = (TextView) view.findViewById(R.id.text);
                icon = (ImageView) view.findViewById(R.id.icon);
                star_button = (CheckBox) view.findViewById(R.id.star_button);
                view.setOnClickListener(this);
            }

            public void setCursorPos(int position){
                this.itemId = position;
            }

            public void onClick(View v) {
                Cursor c = mMountainPassAdapter.getCursor();
                c.moveToPosition(itemId);
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
    }



    /**
     * Custom adapter for items in recycler view that need a cursor adapter.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see CursorRecyclerAdapter
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class TravelTimesAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;
        private List<RecyclerView.ViewHolder> mItems = new ArrayList<>();

        public TravelTimesAdapter(Context context, Cursor c) {
            super(c);
            this.context = context;
        }

        @Override
        public void onBindViewHolderCursor(RecyclerView.ViewHolder viewholder, Cursor cursor) {

            FavTimesVH holder = (FavTimesVH) viewholder;

            String average_time;

            String title = cursor.getString(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_TITLE));
            holder.title.setText(title);
            holder.title.setTypeface(tfb);

            String distance = cursor.getString(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_DISTANCE));
            int average = cursor.getInt(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_AVERAGE));

            if (average == 0) {
                average_time = "Not Available";
            } else {
                average_time = average + " min";
            }

            holder.distance_average_time.setText(distance + " / " + average_time);
            holder.distance_average_time.setTypeface(tf);

            int current = cursor.getInt(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_CURRENT));

            if (current < average) {
                holder.current_time.setTextColor(0xFF008060);
            } else if ((current > average) && (average != 0)) {
                holder.current_time.setTextColor(Color.RED);
            } else {
                holder.current_time.setTextColor(Color.BLACK);
            }

            holder.current_time.setText(current + " min");
            holder.current_time.setTypeface(tfb);

            String created_at = cursor.getString(cursor.getColumnIndex(TravelTimes.TRAVEL_TIMES_UPDATED));
            holder.updated.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", false));
            holder.updated.setTypeface(tf);

            holder.star_button.setVisibility(View.GONE);
        }

        @Override
        public FavTimesVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_times, null);
            FavTimesVH viewholder = new FavTimesVH(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        private class FavTimesVH extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView current_time;
            public TextView distance_average_time;
            public TextView updated;
            public CheckBox star_button;

            public FavTimesVH(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                current_time = (TextView) view.findViewById(R.id.current_time);
                distance_average_time = (TextView) view.findViewById(R.id.distance_average_time);
                updated = (TextView) view.findViewById(R.id.updated);
                star_button = (CheckBox) view.findViewById(R.id.star_button);
            }
        }
    }



	/**
	 * Custom adapter for items in recycler view that need a cursor adapter.
	 *
	 * Binds the custom ViewHolder class to it's data.
	 *
	 * @see CursorRecyclerAdapter
	 * @see android.support.v7.widget.RecyclerView.Adapter
	 */
	private class RouteSchedulesAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private Context context;
		private List<FavFerryVH> mItems = new ArrayList<>();

		public RouteSchedulesAdapter(Context context, Cursor c) {
			super(c);
			this.context = context;
		}

		@Override
		public void onBindViewHolderCursor(RecyclerView.ViewHolder viewholder, Cursor cursor) {

			final int position = cursor.getPosition();

			FavFerryVH holder = (FavFerryVH) viewholder;

			holder.title.setText(cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
			holder.title.setTypeface(tfb);

			String text = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_CROSSING_TIME));

			// Set onClickListener for holder's view
			holder.view.setOnClickListener(
					new OnClickListener() {
						public void onClick(View v) {
							Cursor c = mFerriesSchedulesAdapter.getCursor();
							c.moveToPosition(position);
							Bundle b = new Bundle();
							Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDaySailingsActivity.class);
							b.putInt("id", c.getInt(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ID)));
							b.putString("title", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
							b.putString("date", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_DATE)));
							b.putInt("isStarred", c.getInt(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED)));
							intent.putExtras(b);

							// GA tracker
							mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
							mTracker.send(new HitBuilders.EventBuilder()
									.setCategory("Ferries")
									.setAction("Schedules")
									.setLabel(c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)))
									.build());

							startActivity(intent);
						}
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

			String created_at = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_UPDATED));
			holder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
			holder.created_at.setTypeface(tf);

			holder.star_button.setVisibility(View.GONE);

			String alerts = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ALERT));

			if (alerts.equals("[]")) {
				holder.alert_button.setVisibility(View.GONE);
			} else {
				holder.alert_button.setVisibility(View.VISIBLE);
				holder.alert_button.setTag(cursor.getPosition());
				holder.alert_button.setImageResource(R.drawable.btn_alert_on_holo_light);
				holder.alert_button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Cursor c = mFerriesSchedulesAdapter.getCursor();
						c.moveToPosition(position);
						Bundle b = new Bundle();
						Intent intent = new Intent(getActivity(), FerriesRouteAlertsBulletinsActivity.class);
						b.putString("title", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
						b.putString("alert", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ALERT)));
						intent.putExtras(b);
						startActivity(intent);
					}
				});
			}
		}

		@Override
		public FavFerryVH onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context).inflate(R.layout.list_item_with_star, null);
			FavFerryVH viewholder = new FavFerryVH(view);
			view.setTag(viewholder);
			mItems.add(viewholder);

			return viewholder;
		}

        // View Holder for ferry Schedule list items.
        private class FavFerryVH extends RecyclerView.ViewHolder{
            TextView title;
            TextView text;
            TextView created_at;
            CheckBox star_button;
            ImageButton alert_button;
            public View view;

            public FavFerryVH(View v) {
                super(v);
                view = v;
                title = (TextView) v.findViewById(R.id.title);
                text = (TextView) v.findViewById(R.id.text);
                created_at = (TextView) v.findViewById(R.id.created_at);
                star_button = (CheckBox) v.findViewById(R.id.star_button);
                alert_button = (ImageButton) v.findViewById(R.id.alert_button);

            }
        }

	}


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
					Log.e("MountainPassesSyncReceiver", responseString);
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
					Log.e("FerriesSchedulesSyncReceiver", responseString);
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
