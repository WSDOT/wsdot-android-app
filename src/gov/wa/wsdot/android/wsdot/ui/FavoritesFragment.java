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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.service.FerriesSchedulesSyncService;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.ui.widget.SeparatedListAdapter;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritesFragment extends ListFragment implements
        LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

	private View mEmptyView;
	private SeparatedListAdapter mAdapter;
	private CameraAdapter mCameraAdapter;
	private MountainPassAdapter mMountainPassAdapter;
	private TravelTimesAdapter mTravelTimesAdapter;
	private FerriesSchedulesAdapter mFerriesSchedulesAdapter;
	
	private Intent mFerriesSchedulesIntent;
	private Intent mMountainPassesIntent;
	private Intent mTravelTimesIntent;
	
	private static SwipeRefreshLayout swipeRefreshLayout;
	
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
	
	private static final int CAMERAS_LOADER_ID = 0;
	private static final int MOUNTAIN_PASSES_LOADER_ID = 1;
	private static final int TRAVEL_TIMES_LOADER_ID = 2;
	private static final int FERRIES_SCHEDULES_LOADER_ID = 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Favorites");
		
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

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_swipe_refresh, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorScheme(
                17170451,  // android.R.color.holo_blue_bright 
                17170452,  // android.R.color.holo_green_light 
                17170456,  // android.R.color.holo_orange_light 
                17170454); // android.R.color.holo_red_light)
        
        mEmptyView = root.findViewById( R.id.empty_list_view );

        return root;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mCameraAdapter = new CameraAdapter(getActivity(), null, false);
		mMountainPassAdapter = new MountainPassAdapter(getActivity(), null, false);
		mTravelTimesAdapter = new TravelTimesAdapter(getActivity(), null, false);
		mFerriesSchedulesAdapter = new FerriesSchedulesAdapter(getActivity(), null, false);
		
		getLoaderManager().initLoader(CAMERAS_LOADER_ID, null, this);
		getLoaderManager().initLoader(FERRIES_SCHEDULES_LOADER_ID, null, this);
		getLoaderManager().initLoader(MOUNTAIN_PASSES_LOADER_ID, null, this);
		getLoaderManager().initLoader(TRAVEL_TIMES_LOADER_ID, null, this);

	    TextView t = (TextView) mEmptyView;
		t.setText(R.string.no_favorites);
		getListView().setEmptyView(mEmptyView);	
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
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		String type = (String) ((Object[]) v.getTag())[1];
		Log.d("FavoritesFragment", type);
		
		if (type.equals("camera")) {
			Cursor c = (Cursor) mAdapter.getItem(position);
			Bundle b = new Bundle();
			Intent intent = new Intent(getActivity(), CameraActivity.class);
			b.putInt("id", c.getInt(c.getColumnIndex(Cameras.CAMERA_ID)));
			intent.putExtras(b);
			startActivity(intent);
		} else if (type.equals("mountain_pass")) {
			Cursor c = (Cursor) mAdapter.getItem(position);
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
		} else if (type.equals("ferries_schedules")) {
			Cursor c = (Cursor) mAdapter.getItem(position);
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

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    CursorLoader cursorLoader = null;
	    swipeRefreshLayout.setRefreshing(true);
	    
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
		mAdapter = new SeparatedListAdapter(getActivity());
		
		switch(loader.getId()) {
		case 0:
			mCameraAdapter.swapCursor(cursor);
			break;
		case 1:
			mMountainPassAdapter.swapCursor(cursor);
			break;
		case 2:
			mTravelTimesAdapter.swapCursor(cursor);
			break;
		case 3:
			mFerriesSchedulesAdapter.swapCursor(cursor);
			break;
		}
		
		if (mCameraAdapter.getCount() > 0) {
			mAdapter.addSection("Cameras", mCameraAdapter);
		} 
		if (mFerriesSchedulesAdapter.getCount() > 0) {
			mAdapter.addSection("Ferries Route Schedules", mFerriesSchedulesAdapter);
		}
		if (mMountainPassAdapter.getCount() > 0) {
			mAdapter.addSection("Mountain Passes", mMountainPassAdapter);
		}
		if (mTravelTimesAdapter.getCount() > 0) {
			mAdapter.addSection("Travel Times", mTravelTimesAdapter);
		}
		
		swipeRefreshLayout.setRefreshing(false);
		setListAdapter(mAdapter);
		
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
	
	public class CameraAdapter extends CursorAdapter {
	    private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

		public CameraAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewholder = (ViewHolder) ((Object[]) view.getTag())[0];

            String title = cursor.getString(cursor.getColumnIndex(Cameras.CAMERA_TITLE));
            viewholder.title.setText(title);
            viewholder.title.setTypeface(tf);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(new Object[] { viewholder, "camera" });
            
            return view;
		}
		
        private class ViewHolder {
            TextView title;

            public ViewHolder(View view) {
                    title = (TextView) view.findViewById(R.id.title);
            }
        }		
		
	}
	
	public class MountainPassAdapter extends CursorAdapter {
	    private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
	    private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

		public MountainPassAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewholder = (ViewHolder) ((Object[]) view.getTag())[0];

            String title = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_NAME));
            viewholder.title.setText(title);
            viewholder.title.setTypeface(tfb);
            
            String created_at = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_DATE_UPDATED));
            viewholder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
            viewholder.created_at.setTypeface(tf);
            
            String text = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION));
            
			if (text.equals("")) {
				viewholder.text.setVisibility(View.GONE);
			} else {
				viewholder.text.setVisibility(View.VISIBLE);
				viewholder.text.setText(text);
				viewholder.text.setTypeface(tf);
			}
            
            int icon = cursor.getInt(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_ICON));
            viewholder.icon.setImageResource(icon);
            
            viewholder.star_button.setVisibility(View.GONE);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_details_with_icon, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(new Object[] { viewholder, "mountain_pass" });
            
            return view;
		}
		
		private class ViewHolder {
			ImageView icon;
			TextView title;
			TextView created_at;
			TextView text;
			CheckBox star_button;
			
            public ViewHolder(View view) {
                title = (TextView) view.findViewById(R.id.title);
                created_at = (TextView) view.findViewById(R.id.created_at);
                text = (TextView) view.findViewById(R.id.text);
                icon = (ImageView) view.findViewById(R.id.icon);
                star_button = (CheckBox) view.findViewById(R.id.star_button);
            }
		}		
		
	}
	
	public class TravelTimesAdapter extends CursorAdapter {
	    private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
	    private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

		public TravelTimesAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewholder = (ViewHolder) ((Object[]) view.getTag())[0];

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
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_times, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(new Object[] { viewholder, "travel_times" });
            
            return view;
		}
		
		private class ViewHolder {
			TextView title;
			TextView current_time;
			TextView distance_average_time;
			TextView updated;
			CheckBox star_button;
			
			public ViewHolder(View view) {
				title = (TextView) view.findViewById(R.id.title);
				current_time = (TextView) view.findViewById(R.id.current_time);
				distance_average_time = (TextView) view.findViewById(R.id.distance_average_time);
				updated = (TextView) view.findViewById(R.id.updated);
				star_button = (CheckBox) view.findViewById(R.id.star_button);
			}
		}		
		
	}
	
	public class FerriesSchedulesAdapter extends CursorAdapter {
	    private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
	    private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

		public FerriesSchedulesAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewholder = (ViewHolder) ((Object[]) view.getTag())[0];

            String title = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE));
            viewholder.title.setText(title);
            viewholder.title.setTypeface(tfb);
            
            String text = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_CROSSING_TIME));
            
            if (text.equalsIgnoreCase("null")) {
                viewholder.text.setText("");
            } else {
                viewholder.text.setText("Crossing Time: ~ " + text + " min");
                viewholder.text.setTypeface(tf);
            }
            
            String created_at = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_UPDATED));
            viewholder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
            viewholder.created_at.setTypeface(tf);
            
            viewholder.star_button.setVisibility(View.GONE);
            
			String alerts = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ALERT));
			
			if (alerts.equals("[]")) {
				viewholder.alert_button.setVisibility(View.GONE);
			} else {
				viewholder.alert_button.setTag(cursor.getPosition());
				viewholder.alert_button.setImageResource(R.drawable.btn_alert_on_holo_light);
	            viewholder.alert_button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
	                	int position = (Integer) v.getTag();
	                	Cursor c = (Cursor) mFerriesSchedulesAdapter.getItem(position);
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
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_with_star, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(new Object[] { viewholder, "ferries_schedules" });
            
            return view;
		}
		
        private class ViewHolder {
            TextView title;
            TextView text;
            TextView created_at;
            CheckBox star_button;
            ImageButton alert_button;

            public ViewHolder(View view) {
                    title = (TextView) view.findViewById(R.id.title);
                    text = (TextView) view.findViewById(R.id.text);
                    created_at = (TextView) view.findViewById(R.id.created_at);
                    star_button = (CheckBox) view.findViewById(R.id.star_button);
                    alert_button = (ImageButton) view.findViewById(R.id.alert_button);
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
        swipeRefreshLayout.setRefreshing(true);
        getActivity().startService(mFerriesSchedulesIntent);
        getActivity().startService(mMountainPassesIntent);
        getActivity().startService(mTravelTimesIntent);        
    }
    
}
