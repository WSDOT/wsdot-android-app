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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.traveltimes;

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
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class TravelTimesFragment extends BaseFragment implements
        LoaderCallbacks<Cursor>,
        OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = TravelTimesFragment.class.getSimpleName();
	private static TravelTimesAdapter mAdapter;
	private TravelTimesSyncReceiver mTravelTimesSyncReceiver;
	private String mFilter;
	private View mEmptyView;
	private boolean mIsQuery = false;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		
		Intent intent = new Intent(getActivity(), TravelTimesSyncService.class);
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
        mAdapter = new TravelTimesAdapter(getActivity(), null);
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
        
        mEmptyView = root.findViewById(R.id.empty_list_view);
        
        return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
	}

    @Override
	public void onPause() {
		super.onPause();
		
		getActivity().unregisterReceiver(mTravelTimesSyncReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		
        IntentFilter filter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.TRAVEL_TIMES_RESPONSE");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mTravelTimesSyncReceiver = new TravelTimesSyncReceiver();
		getActivity().registerReceiver(mTravelTimesSyncReceiver, filter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
		
        //Create the search view
        SearchView searchView = new SearchView(
                ((BaseActivity) getActivity()).getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Search Travel Times");
        searchView.setOnQueryTextListener(this);
		
        MenuItem menuItem_Search = menu.add(R.string.search_title).setIcon(R.drawable.ic_menu_search);
        MenuItemCompat.setActionView(menuItem_Search, searchView);
        MenuItemCompat.setShowAsAction(menuItem_Search,
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
                        | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        
        MenuItemCompat.setOnActionExpandListener(menuItem_Search, new MenuItemCompat.OnActionExpandListener() {
			public boolean onMenuItemActionCollapse(MenuItem item) {
				mFilter = null;
				getLoaderManager().restartLoader(0, null, TravelTimesFragment.this);
				
				return true;
			}

			public boolean onMenuItemActionExpand(MenuItem item) {

				return true;
			}
		});
	}

	public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.
		// Update the search filter and restart the loader.
        mFilter = !TextUtils.isEmpty(newText) ? newText : null;
        mIsQuery = true;
        getLoaderManager().restartLoader(0, null, this);
        
        return true;
	}

	public boolean onQueryTextSubmit(String query) {
		getLoaderManager().restartLoader(0, null, this);
		mIsQuery = true;
		
		return false;
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
		
        Uri baseUri;
        
        if (mFilter != null) {
            baseUri = Uri.withAppendedPath(TravelTimes.CONTENT_FILTER_URI, Uri.encode(mFilter));
        } else {
            baseUri = TravelTimes.CONTENT_URI;
        }
		
		CursorLoader cursorLoader = new TravelTimesItemsLoader(getActivity(),
				baseUri,
				projection,
				null,
				null,
				null
				);
		
		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor.moveToFirst()) {
		    // Nothing.
		} else {
			if (mIsQuery) {
				mIsQuery = false;
			    TextView t = (TextView) mEmptyView;
				t.setText(R.string.no_matching_travel_times);
                mEmptyView.setVisibility(View.VISIBLE);
			}
		}

		swipeRefreshLayout.setRefreshing(false);
		mAdapter.swapCursor(cursor);
        //When getItemCount is check in onReceive the
        //size appears to be 0. So we check here.
        if (mAdapter.getItemCount() > 0){
            mEmptyView.setVisibility(View.GONE);
        }
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	    mAdapter.swapCursor(null);
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

			swipeRefreshLayout.post(new Runnable() {
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
				}
			});
			forceLoad();
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

            ViewHolder holder = (ViewHolder) viewholder;

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
            holder.updated.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", true));
            holder.updated.setTypeface(tf);

            holder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            holder.star_button.setOnCheckedChangeListener(null);
            holder.star_button
                    .setChecked(cursor.getInt(cursor
                            .getColumnIndex(TravelTimes.TRAVEL_TIMES_IS_STARRED)) != 0);
            holder.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
                    int rowId = (Integer) buttonView.getTag();
                    ContentValues values = new ContentValues();
                    values.put(TravelTimes.TRAVEL_TIMES_IS_STARRED, isChecked ? 1 : 0);

					Snackbar added_snackbar = Snackbar
							.make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

					Snackbar removed_snackbar = Snackbar
							.make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

					if (isChecked){
						added_snackbar.show();
					}else{
						removed_snackbar.show();
					}

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
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_times, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView current_time;
            public TextView distance_average_time;
            public TextView updated;
            public CheckBox star_button;

            public ViewHolder(View view) {
                super(view);
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

            mEmptyView.setVisibility(View.GONE);

			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(0, null, TravelTimesFragment.this);
				} else if (responseString.equals("NOP")) {
				    swipeRefreshLayout.setRefreshing(false);
				} else {
				    swipeRefreshLayout.setRefreshing(false);
					Log.e("TravelTimesSyncReceiver", responseString);
	
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

    public void onRefresh() {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
        Intent intent = new Intent(getActivity(), TravelTimesSyncService.class);
        intent.putExtra("forceUpdate", true);
        getActivity().startService(intent);        
    }

}
