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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.ui.widget.SeparatedListAdapter;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class FavoritesFragment extends SherlockListFragment
	implements LoaderCallbacks<Cursor>{

	private View mLoadingSpinner;
	private View mEmptyView;
	private SeparatedListAdapter mAdapter;
	private CameraAdapter mCameraAdapter;
	private MountainPassAdapter mMountainPassAdapter;

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
	
	private static final int CAMERAS_LOADER_ID = 0;
	private static final int MOUNTAIN_PASSES_LOADER_ID = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        //setRetainInstance(true);
            
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Favorites");
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

		mCameraAdapter = new CameraAdapter(getActivity(), null, false);
		mMountainPassAdapter = new MountainPassAdapter(getActivity(), null, false);
		
		getLoaderManager().initLoader(CAMERAS_LOADER_ID, null, this);
		getLoaderManager().initLoader(MOUNTAIN_PASSES_LOADER_ID, null, this);
		
	    TextView t = (TextView) mEmptyView;
		t.setText(R.string.no_favorites);
		getListView().setEmptyView(mEmptyView);	
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
		}
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    CursorLoader cursorLoader = null;
	    
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
		}
		
		if (mCameraAdapter.getCount() > 0) {
			mAdapter.addSection("Cameras", mCameraAdapter);
		} 
		if (mMountainPassAdapter.getCount() > 0) {
			mAdapter.addSection("Mountain Passes", mMountainPassAdapter);
		}
		
		setListAdapter(mAdapter);
		
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()) {
		case 0:
			mCameraAdapter.swapCursor(null);
			break;
		case 1:
			mMountainPassAdapter.swapCursor(null);
			break;
		}
	}
	
	public class CameraAdapter extends CursorAdapter {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        
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
            viewholder.title.setTypeface(tf);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(new Object[] { viewholder, "mountain_pass" });
            
            return view;
		}
		
        private class ViewHolder {
            TextView title;

            public ViewHolder(View view) {
                    title = (TextView) view.findViewById(R.id.title);
            }
        }		
		
	}
    
}
