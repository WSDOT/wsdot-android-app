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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.BorderWait;
import gov.wa.wsdot.android.wsdot.service.BorderWaitSyncService;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;

public class BorderWaitNorthboundFragment extends SherlockListFragment
	implements LoaderCallbacks<Cursor> {
	
	@SuppressWarnings("unused")
	private static final String DEBUG_TAG = "BorderWaitNorthbound";
	private static BorderWaitAdapter adapter;	
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	private static View mLoadingSpinner;
	private BorderWaitSyncReceiver mBorderWaitSyncReceiver;
	private View mEmptyView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		Intent intent = new Intent(getActivity(), BorderWaitSyncService.class);
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		getActivity().startService(intent);
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

        adapter = new BorderWaitAdapter(getActivity(), null, false);
        setListAdapter(adapter);
        
        routeImage.put(5, R.drawable.ic_list_i5);
        routeImage.put(9, R.drawable.ic_list_sr9);
        routeImage.put(539, R.drawable.ic_list_sr539);
        routeImage.put(543, R.drawable.ic_list_sr543);
        routeImage.put(97, R.drawable.ic_list_us97);        
        
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
    }    
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().unregisterReceiver(mBorderWaitSyncReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter("gov.wa.wsdot.android.wsdot.intent.action.BORDER_WAIT_RESPONSE");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mBorderWaitSyncReceiver = new BorderWaitSyncReceiver();
		getActivity().registerReceiver(mBorderWaitSyncReceiver, filter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			Intent intent = new Intent(getActivity(), BorderWaitSyncService.class);
		    intent.putExtra("forceUpdate", true);
			getActivity().startService(intent);
		}
		
		return super.onOptionsItemSelected(item);
	}	

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {
			BorderWait._ID,
			BorderWait.BORDER_WAIT_DIRECTION,
			BorderWait.BORDER_WAIT_ID,
			BorderWait.BORDER_WAIT_IS_STARRED,
			BorderWait.BORDER_WAIT_LANE,
			BorderWait.BORDER_WAIT_ROUTE,
			BorderWait.BORDER_WAIT_TIME,
			BorderWait.BORDER_WAIT_TITLE,
			BorderWait.BORDER_WAIT_UPDATED
		};
		
		CursorLoader cursorLoader = new BorderWaitItemsLoader(getActivity(),
				BorderWait.CONTENT_URI,
				projection,
				BorderWait.BORDER_WAIT_DIRECTION + " LIKE ?",
				new String[] {"northbound"},
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
    
	public static class BorderWaitItemsLoader extends CursorLoader {
		public BorderWaitItemsLoader(Context context, Uri uri,
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
	
	public class BorderWaitAdapter extends CursorAdapter {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		
        public BorderWaitAdapter(Context context, Cursor c, boolean autoRequery) {
        	super(context, c, autoRequery);
        }
        
        public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			ViewHolder viewholder = (ViewHolder) view.getTag();
			
			String title = cursor.getString(cursor.getColumnIndex(BorderWait.BORDER_WAIT_TITLE));
			String lane = cursor.getString(cursor.getColumnIndex(BorderWait.BORDER_WAIT_LANE));
			
			viewholder.tt.setText(title + " (" + lane + ")");
			viewholder.tt.setTypeface(tfb);
			
			String created_at = cursor.getString(cursor.getColumnIndex(BorderWait.BORDER_WAIT_UPDATED));
			viewholder.bt.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", false));
			viewholder.bt.setTypeface(tf);
			
			int wait = cursor.getInt(cursor.getColumnIndex(BorderWait.BORDER_WAIT_TIME));
        	if (wait == -1) {
        		viewholder.rt.setText("N/A");
        	} else if (wait < 5) {
        		viewholder.rt.setText("< 5 min");
        	} else {
        		viewholder.rt.setText(wait + " min");
        	}
        	viewholder.rt.setTypeface(tfb);
        	
			viewholder.iv.setImageResource(routeImage.get(cursor.getInt(cursor
					.getColumnIndex(BorderWait.BORDER_WAIT_ROUTE))));
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.borderwait_row, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(viewholder);
            
            return view;
		}
		
		private class ViewHolder {
			TextView tt;
			TextView bt;
			TextView rt;
			ImageView iv;
			
			public ViewHolder(View view) {
				tt = (TextView) view.findViewById(R.id.toptext);
				bt = (TextView) view.findViewById(R.id.bottomtext);		
				rt = (TextView) view.findViewById(R.id.righttext);
				iv = (ImageView) view.findViewById(R.id.icon);
			}
		}
	}
	
	public class BorderWaitSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");
			
			if (responseString.equals("OK")) {
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
				getLoaderManager().restartLoader(0, null, BorderWaitNorthboundFragment.this);
			} else if (responseString.equals("NOP")) {
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
				mLoadingSpinner.setVisibility(View.GONE);
			} else {
				Log.e("BorderWaitSyncReceiver", responseString);
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
