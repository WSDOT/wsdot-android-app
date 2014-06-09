/*
 * Copyright (c) 2014 Washington State Department of Transportation
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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import gov.wa.wsdot.android.wsdot.service.FerriesSchedulesSyncService;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FerriesRouteSchedulesFragment extends ListFragment implements
        LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

	@SuppressWarnings("unused")
    private static final String TAG = FerriesRouteSchedulesFragment.class.getSimpleName();
	private static RouteSchedulesAdapter adapter;
	private FerriesSchedulesSyncReceiver mFerriesSchedulesSyncReceiver;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Ferries/Route Schedules");
        
		Intent intent = new Intent(getActivity(), FerriesSchedulesSyncService.class);
		getActivity().startService(intent);
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

        adapter = new RouteSchedulesAdapter(getActivity(), null, false);
        setListAdapter(adapter);
        
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
	}	
	
    @Override
	public void onPause() {
		super.onPause();
		
		getActivity().unregisterReceiver(mFerriesSchedulesSyncReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		
        IntentFilter filter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.FERRIES_SCHEDULES_RESPONSE");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mFerriesSchedulesSyncReceiver = new FerriesSchedulesSyncReceiver();
		getActivity().registerReceiver(mFerriesSchedulesSyncReceiver, filter);
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {
				FerriesSchedules._ID,
				FerriesSchedules.FERRIES_SCHEDULE_ID,
				FerriesSchedules.FERRIES_SCHEDULE_TITLE,
				FerriesSchedules.FERRIES_SCHEDULE_DATE,
				FerriesSchedules.FERRIES_SCHEDULE_ALERT,
				FerriesSchedules.FERRIES_SCHEDULE_UPDATED,
				FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED };
		
		CursorLoader cursorLoader = new RouteSchedulesLoader(getActivity(),
				FerriesSchedules.CONTENT_URI,
				projection,
				null,
				null,
				null
				);
		
		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
	    cursor.moveToFirst();
	    swipeRefreshLayout.setRefreshing(false);
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	    swipeRefreshLayout.setRefreshing(false);
		adapter.swapCursor(null);
	}

	public static class RouteSchedulesLoader extends CursorLoader {
		public RouteSchedulesLoader(Context context, Uri uri,
				String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
			super(context, uri, projection, selection, selectionArgs, sortOrder);
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();

			swipeRefreshLayout.setRefreshing(true);
			forceLoad();
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Cursor c = (Cursor) adapter.getItem(position);
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDaySailingsActivity.class);
		b.putInt("id", c.getInt(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ID)));
		b.putString("title", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
		b.putString("date", c.getString(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_DATE)));
		b.putInt("isStarred", c.getInt(c.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED)));
		intent.putExtras(b);
		startActivity(intent);
	}
	
	public class RouteSchedulesAdapter extends CursorAdapter {
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        
        public RouteSchedulesAdapter(Context context, Cursor c, boolean autoRequery) {
        	super(context, c, autoRequery);
        }

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			ViewHolder viewholder = (ViewHolder) view.getTag();
			
			viewholder.title.setText(cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
			viewholder.title.setTypeface(tf);
			
            String created_at = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_UPDATED));
            viewholder.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
            viewholder.created_at.setTypeface(tf);
			
            viewholder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            viewholder.star_button.setOnCheckedChangeListener(null);
            viewholder.star_button
					.setChecked(cursor.getInt(cursor
							.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED)) != 0);
            viewholder.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					int rowId = (Integer) buttonView.getTag();
					ContentValues values = new ContentValues();
					values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, isChecked ? 1 : 0);
					
					int toastMessage = isChecked ? R.string.add_favorite : R.string.remove_favorite;
					Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
					
					getActivity().getContentResolver().update(
							FerriesSchedules.CONTENT_URI,
							values,
							FerriesSchedules._ID + "=?",
							new String[] {Integer.toString(rowId)}
							);
				}
			});
            
			String alerts = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_ALERT));
			
			if (alerts.equals("[]")) {
				viewholder.alert_button.setVisibility(View.GONE);
			} else {
				viewholder.alert_button.setVisibility(View.VISIBLE);
				viewholder.alert_button.setTag(cursor.getPosition());
				viewholder.alert_button.setImageResource(R.drawable.btn_alert_on_holo_light);
	            viewholder.alert_button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
	                	int position = (Integer) v.getTag();
	                	Cursor c = (Cursor) adapter.getItem(position);
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
            view.setTag(viewholder);
            
            return view;
		}

    	public class ViewHolder {
    		TextView title;
    		TextView created_at;
    		CheckBox star_button;
    		ImageButton alert_button;
    		
    		public ViewHolder(View view) {
    			title = (TextView) view.findViewById(R.id.title);
    			created_at = (TextView) view.findViewById(R.id.created_at);   			
    			star_button = (CheckBox) view.findViewById(R.id.star_button);
    			alert_button = (ImageButton) view.findViewById(R.id.alert_button);
    		}
    	}
	}
	
	public class FerriesSchedulesSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			
			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(0, null, FerriesRouteSchedulesFragment.this);
				} else if (responseString.equals("NOP")) {
				    swipeRefreshLayout.setRefreshing(false);
				} else {
	                swipeRefreshLayout.setRefreshing(false);
				    Log.e("FerriesSchedulesSyncReceiver", responseString);
	
					if (!UIUtils.isNetworkAvailable(context)) {
						responseString = getString(R.string.no_connection);
					}
					
					if (getListView().getCount() > 0) {
						Toast.makeText(context, responseString, Toast.LENGTH_LONG).show();
					} else {
					    TextView t = (TextView) mEmptyView;
						t.setText(responseString);
						getListView().setEmptyView(mEmptyView);
					}
				}
			} else {
				swipeRefreshLayout.setRefreshing(false);
			}
		}
	}

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        Intent intent = new Intent(getActivity(), FerriesSchedulesSyncService.class);
        intent.putExtra("forceUpdate", true);
        getActivity().startService(intent);        
    }
	
}
