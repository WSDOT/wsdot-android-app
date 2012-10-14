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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
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
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MountainPassesFragment extends SherlockListFragment
	implements LoaderCallbacks<Cursor> {
	
	@SuppressWarnings("unused")
	private static final String DEBUG_TAG = "MountainPassConditions";
	private static MountainPassAdapter adapter;
	private static View mLoadingSpinner;
	private MountainPassesSyncReceiver mMountainPassesSyncReceiver;
	private View mEmptyView;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setHasOptionsMenu(true);         
		
		Intent intent = new Intent(getActivity(), MountainPassesSyncService.class);
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		getActivity().startService(intent);
		
		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Mountain Passes");
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
        mEmptyView = root.findViewById( R.id.empty_list_view );

        return root;
    } 
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
        adapter = new MountainPassAdapter(getActivity(), null, false);
        setListAdapter(adapter);
	
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(0, null, this);        
	}

    @Override
	public void onPause() {
		super.onPause();
		
		getActivity().unregisterReceiver(mMountainPassesSyncReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mMountainPassesSyncReceiver = new MountainPassesSyncReceiver();
		getActivity().registerReceiver(mMountainPassesSyncReceiver, filter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.refresh, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			Intent intent = new Intent(getActivity(), MountainPassesSyncService.class);
		    intent.putExtra("forceUpdate", true);
			getActivity().startService(intent);			
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Cursor c = (Cursor) adapter.getItem(position);
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

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {
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

		// We are only displaying the highest impact alerts on the dashboard.
		CursorLoader cursorLoader = new MountainPassItemsLoader(getActivity(),
				MountainPasses.CONTENT_URI,
				projection,
				null,
				null,
				null
				);

		return cursorLoader;
		
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor.moveToFirst()) {
			mLoadingSpinner.setVisibility(View.GONE);
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
		}
		
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
    
	public static class MountainPassItemsLoader extends CursorLoader {
		public MountainPassItemsLoader(Context context, Uri uri,
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
	
	private class MountainPassAdapter extends CursorAdapter {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        
        public MountainPassAdapter(Context context, Cursor c, boolean autoRequery) {
        	super(context, c, autoRequery);
        }

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
            ViewHolder viewholder = (ViewHolder) view.getTag();
            
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
            
            viewholder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
			
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            viewholder.star_button.setOnCheckedChangeListener(null);
            viewholder.star_button
					.setChecked(cursor.getInt(cursor
							.getColumnIndex(MountainPasses.MOUNTAIN_PASS_IS_STARRED)) != 0);
            viewholder.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					int rowId = (Integer) buttonView.getTag();
					ContentValues values = new ContentValues();
					values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, isChecked ? 1 : 0);

					int toastMessage = isChecked ? R.string.add_favorite : R.string.remove_favorite;
					Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
					
					getActivity().getContentResolver().update(
							MountainPasses.CONTENT_URI,
							values,
							MountainPasses._ID + "=?",
							new String[] {Integer.toString(rowId)}
							);
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_details_with_icon, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(viewholder);
            
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
	
	public class MountainPassesSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			if (responseString.equals("OK")) {
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
				getLoaderManager().restartLoader(0, null, MountainPassesFragment.this);
			} else if (responseString.equals("NOP")) {
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
				mLoadingSpinner.setVisibility(View.GONE);
			} else {
				Log.e("MountainPassesSyncReceiver", responseString);
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
				mLoadingSpinner.setVisibility(View.GONE);

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
		}
	}

}
