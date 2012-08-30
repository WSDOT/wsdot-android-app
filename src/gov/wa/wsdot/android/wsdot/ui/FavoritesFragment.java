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

	private static final String[] cameras_projection = {
		Cameras._ID,
		Cameras.CAMERA_ID,
		Cameras.CAMERA_TITLE,
		Cameras.CAMERA_IS_STARRED
		};		

	private static final int CAMERAS_LOADER_ID = 0;
	
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
		
		getLoaderManager().initLoader(CAMERAS_LOADER_ID, null, this);
		
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
			b.putInt("id", c.getInt(1));
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
		}

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter = new SeparatedListAdapter(getActivity());
		
		switch(loader.getId()) {
		case 0:
			mCameraAdapter.swapCursor(cursor);
			if (cursor.moveToFirst()) {
				mAdapter.addSection("Cameras", mCameraAdapter);
			}
			break;
		}
		
		setListAdapter(mAdapter);
		
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()) {
		case 0:
			mCameraAdapter.swapCursor(null);
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

            String title = cursor.getString(2); // CAMERA_TITLE
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
    
}
