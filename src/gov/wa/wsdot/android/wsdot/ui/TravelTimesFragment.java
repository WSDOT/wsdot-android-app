/*
 * Copyright (c) 2012 Washington State Department of Transportation
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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class TravelTimesFragment extends SherlockListFragment
	implements LoaderCallbacks<Cursor> {

	@SuppressWarnings("unused")
	private static final String DEBUG_TAG = "TravelTimes";
	private static TravelTimesAdapter adapter;
	private static View mLoadingSpinner;
	private TravelTimesSyncReceiver mTravelTimesSyncReceiver;
	private static final String TRAVEL_TIMES_URL = "http://data.wsdot.wa.gov/mobile/TravelTimes.js.gz";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		IntentFilter filter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.TRAVEL_TIMES_RESPONSE");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mTravelTimesSyncReceiver = new TravelTimesSyncReceiver();
		getActivity().registerReceiver(mTravelTimesSyncReceiver, filter);
		
		Intent intent = new Intent(getActivity(), TravelTimesSyncService.class);
	    intent.putExtra("url", TRAVEL_TIMES_URL);
		getActivity().startService(intent);
		
		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Traffic Map/Travel Times");
	}	
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);

        return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		adapter = new TravelTimesAdapter(getActivity(), null, false);
		setListAdapter(adapter);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getActivity().unregisterReceiver(mTravelTimesSyncReceiver);
	}
	
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.refresh, menu);
		
        //Create the search view
        SearchView searchView = new SearchView(getSherlockActivity().getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Search Travel Times");
		
        menu.add(R.string.search_title)
        .setIcon(R.drawable.ic_menu_search)
        .setActionView(searchView)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			Intent intent = new Intent(getActivity(), TravelTimesSyncService.class);
		    intent.putExtra("url", TRAVEL_TIMES_URL);
		    intent.putExtra("forceUpdate", true);
			getActivity().startService(intent);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {
				TravelTimes._ID,
				TravelTimes.TRAVEL_TIMES_ID,
				TravelTimes.TRAVEL_TIMES_TITLE,
				TravelTimes.TRAVEL_TIMES_UPDATED,
				TravelTimes.TRAVEL_TIMES_DISTANCE,
				TravelTimes.TRAVEL_TIMES_AVERAGE,
				TravelTimes.TRAVEL_TIMES_CURRENT,
				TravelTimes.TRAVEL_TIMES_IS_STARRED
				};
		
		CursorLoader cursorLoader = new TravelTimesItemsLoader(getActivity(),
				TravelTimes.CONTENT_URI,
				projection,
				null,
				null,
				null
				);
		
		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
		
		if (cursor.moveToFirst()) {
			mLoadingSpinner.setVisibility(View.GONE);
		} else {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}
		
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);		
	}
	
	public static class TravelTimesItemsLoader extends CursorLoader {
		public TravelTimesItemsLoader(Context context, Uri uri,
				String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
			super(context, uri, projection, selection, selectionArgs, sortOrder);
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();

			mLoadingSpinner.setVisibility(View.VISIBLE);
			forceLoad();
		}
	}
	
	private class TravelTimesAdapter extends CursorAdapter {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        public TravelTimesAdapter(Context context, Cursor c, boolean autoRequery) {
	        super(context, c, autoRequery);
        }

        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }        
        
		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			ViewHolder viewholder = (ViewHolder) view.getTag();

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
        	
            viewholder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            viewholder.star_button.setOnCheckedChangeListener(null);
            viewholder.star_button
					.setChecked(cursor.getInt(cursor
							.getColumnIndex(TravelTimes.TRAVEL_TIMES_IS_STARRED)) != 0);
            viewholder.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					int rowId = (Integer) buttonView.getTag();
					ContentValues values = new ContentValues();
					values.put(TravelTimes.TRAVEL_TIMES_IS_STARRED, isChecked ? 1 : 0);

					getActivity().getContentResolver().update(
							TravelTimes.CONTENT_URI,
							values,
							TravelTimes._ID + "=?",
							new String[] {Integer.toString(rowId)}
							);
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_times, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(viewholder);
            
            return view;
		}
		
		private class ViewHolder {
			public TextView title;
			public TextView current_time;
			public TextView distance_average_time;
			public TextView updated;
			public CheckBox star_button;
			
			public ViewHolder(View view) {
				title = (TextView) view.findViewById(R.id.title);
				current_time = (TextView) view.findViewById(R.id.current_time);
				distance_average_time = (TextView) view.findViewById(R.id.distance_average_time);
				updated = (TextView) view.findViewById(R.id.updated);
				star_button = (CheckBox) view.findViewById(R.id.star_button);
			}
		}

	}

	public class TravelTimesSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			if (responseString.equals("OK")) {
				getLoaderManager().restartLoader(0, null, TravelTimesFragment.this);
			} else if (responseString.equals("NOOP")) {
				// Nothing to do.
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
			}
		}
	}

}
