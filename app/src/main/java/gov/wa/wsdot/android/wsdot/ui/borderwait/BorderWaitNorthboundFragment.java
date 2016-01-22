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

package gov.wa.wsdot.android.wsdot.ui.borderwait;

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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.BorderWait;
import gov.wa.wsdot.android.wsdot.service.BorderWaitSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class BorderWaitNorthboundFragment extends BaseFragment implements
        LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {
	
    private static final String TAG = BorderWaitNorthboundFragment.class.getSimpleName();
	
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	private BorderWaitSyncReceiver mBorderWaitSyncReceiver;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    private static BorderWaitAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(getActivity(), BorderWaitSyncService.class);
		getActivity().startService(intent);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new BorderWaitAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

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
		
        IntentFilter filter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.BORDER_WAIT_RESPONSE");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mBorderWaitSyncReceiver = new BorderWaitSyncReceiver();
		getActivity().registerReceiver(mBorderWaitSyncReceiver, filter);
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
		cursor.moveToFirst();
		swipeRefreshLayout.setRefreshing(false);		
		mAdapter.swapCursor(cursor);
        //When getItemCount is checked in onReceive the
        //size appears to be 0. So we check here.
        if (mAdapter.getItemCount() > 0){
            mEmptyView.setVisibility(View.GONE);
        }
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	    swipeRefreshLayout.setRefreshing(false);
		mAdapter.swapCursor(null);
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

			swipeRefreshLayout.post(new Runnable() {
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
				}
			});
			forceLoad();
		}
	}
	
	public class BorderWaitSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");

            mEmptyView.setVisibility(View.GONE);

			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(0, null, BorderWaitNorthboundFragment.this);
				} else if (responseString.equals("NOP")) {
				    swipeRefreshLayout.setRefreshing(false);
				} else {
				    swipeRefreshLayout.setRefreshing(false);
				    Log.e("BorderWaitSyncReceiver", responseString);
	
					if (!UIUtils.isNetworkAvailable(context)) {
						responseString = getString(R.string.no_connection);
					}
					
					if (mAdapter.getItemCount() > 0) {
						Toast.makeText(context, responseString, Toast.LENGTH_LONG).show();
					} else {
					    TextView t = (TextView) mEmptyView;
						t.setText(responseString);
                        mEmptyView.setVisibility(View.VISIBLE);
					}
				}
			} else {
				swipeRefreshLayout.setRefreshing(false);
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
	private class BorderWaitAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private Context context;
		private List<BorderWaitVH> mItems = new ArrayList<>();

		public BorderWaitAdapter(Context context, Cursor c) {
			super(c);
			this.context = context;
		}

		@Override
		public void onBindViewHolderCursor(RecyclerView.ViewHolder viewholder, Cursor cursor) {

            BorderWaitVH borderVH = (BorderWaitVH) viewholder;

            String title = cursor.getString(cursor.getColumnIndex(BorderWait.BORDER_WAIT_TITLE));
            String lane = cursor.getString(cursor.getColumnIndex(BorderWait.BORDER_WAIT_LANE));

            borderVH.tt.setText(title + " (" + lane + ")");
            borderVH.tt.setTypeface(tfb);

            String created_at = cursor.getString(cursor.getColumnIndex(BorderWait.BORDER_WAIT_UPDATED));
            borderVH.bt.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", false));
            borderVH.bt.setTypeface(tf);

            int wait = cursor.getInt(cursor.getColumnIndex(BorderWait.BORDER_WAIT_TIME));
            if (wait == -1) {
                borderVH.rt.setText("N/A");
            } else if (wait < 5) {
                borderVH.rt.setText("< 5 min");
            } else {
                borderVH.rt.setText(wait + " min");
            }
            borderVH.rt.setTypeface(tfb);

            borderVH.iv.setImageResource(routeImage.get(cursor.getInt(cursor
					.getColumnIndex(BorderWait.BORDER_WAIT_ROUTE))));


		}
		@Override
		public BorderWaitVH onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context).inflate(R.layout.borderwait_row, null);
			BorderWaitVH viewholder = new BorderWaitVH(view);
			view.setTag(viewholder);
			mItems.add(viewholder);
			return viewholder;
		}

		// View Holder for list items.
		private class BorderWaitVH extends RecyclerView.ViewHolder {
            TextView tt;
            TextView bt;
            TextView rt;
            ImageView iv;

			public BorderWaitVH(View view) {
				super(view);
                tt = (TextView) view.findViewById(R.id.toptext);
                bt = (TextView) view.findViewById(R.id.bottomtext);
                rt = (TextView) view.findViewById(R.id.righttext);
                iv = (ImageView) view.findViewById(R.id.icon);
			}
		}
	}

    public void onRefresh() {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
        Intent intent = new Intent(getActivity(), BorderWaitSyncService.class);
        intent.putExtra("forceUpdate", true);
        getActivity().startService(intent);        
    }
}
