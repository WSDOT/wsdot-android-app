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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import gov.wa.wsdot.android.wsdot.service.FerriesSchedulesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesFragment extends BaseFragment implements
        LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FerriesRouteSchedulesFragment.class.getSimpleName();
	private static RouteSchedulesAdapter mAdapter;
	private FerriesSchedulesSyncReceiver mFerriesSchedulesSyncReceiver;
	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	private Tracker mTracker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		Intent intent = new Intent(getActivity(), FerriesSchedulesSyncService.class);
		getActivity().startService(intent);
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RouteSchedulesAdapter(getActivity(), null);
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
				FerriesSchedules.FERRIES_SCHEDULE_CROSSING_TIME,
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

	public static class RouteSchedulesLoader extends CursorLoader {

		public RouteSchedulesLoader(Context context, Uri uri,
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
	private class RouteSchedulesAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;
        private List<FerryScheduleVH> mItems = new ArrayList<>();

        public RouteSchedulesAdapter(Context context, Cursor c) {
        	super(c);
            this.context = context;
        }

		@Override
        public void onBindViewHolderCursor(RecyclerView.ViewHolder viewholder, Cursor cursor) {

            final int position = cursor.getPosition();

            FerryScheduleVH holder = (FerryScheduleVH) viewholder;

			holder.title.setText(cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
			holder.title.setTypeface(tfb);

            String text = cursor.getString(cursor.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_CROSSING_TIME));

            // Set onClickListener for holder's view
            holder.view.setOnClickListener(
                    new OnClickListener() {
                        public void onClick(View v) {
                            Cursor c = mAdapter.getCursor();
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

            holder.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            holder.star_button.setOnCheckedChangeListener(null);
			holder.star_button.setContentDescription("favorite");
            holder.star_button
					.setChecked(cursor.getInt(cursor
							.getColumnIndex(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED)) != 0);
            holder.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					int rowId = (Integer) buttonView.getTag();
					ContentValues values = new ContentValues();
					values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, isChecked ? 1 : 0);

					Snackbar added_snackbar = Snackbar
							.make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

					Snackbar removed_snackbar = Snackbar
							.make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

					added_snackbar.addCallback(new Snackbar.Callback() {
						@Override
						public void onShown(Snackbar snackbar) {
							super.onShown(snackbar);
							snackbar.getView().setContentDescription("added to favorites");
							snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
						}
					});

					removed_snackbar.addCallback(new Snackbar.Callback() {
						@Override
						public void onShown(Snackbar snackbar) {
							super.onShown(snackbar);
							snackbar.getView().setContentDescription("removed from favorites");
							snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
						}
					});

                    if (isChecked){
                        added_snackbar.show();
                    }else{
                        removed_snackbar.show();
                    }

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
				holder.alert_button.setVisibility(View.GONE);
			} else {
				holder.alert_button.setVisibility(View.VISIBLE);
				holder.alert_button.setTag(cursor.getPosition());
				holder.alert_button.setImageResource(R.drawable.btn_alert_on);
				holder.alert_button.setContentDescription("Route has active alerts");
	            holder.alert_button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
                        Cursor c = mAdapter.getCursor();
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
		public FerryScheduleVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_with_star, null);
            FerryScheduleVH viewholder = new FerryScheduleVH(view);
			view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
		}

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        // View Holder for ferry Schedule list items.
        private class FerryScheduleVH extends RecyclerView.ViewHolder{
            TextView title;
            TextView text;
            TextView created_at;
            CheckBox star_button;
            ImageButton alert_button;
            public View view;

            public FerryScheduleVH(View v) {
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

	public class FerriesSchedulesSyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent.getStringExtra("responseString");

            mEmptyView.setVisibility(View.GONE);

			if (responseString != null) {
				if (responseString.equals("OK")) {
					getLoaderManager().restartLoader(0, null, FerriesRouteSchedulesFragment.this);
				} else if (responseString.equals("NOP")) {
				    swipeRefreshLayout.setRefreshing(false);
				} else {
	                swipeRefreshLayout.setRefreshing(false);
				    Log.e("FerriesScheSyncReceiver", responseString);
	
					if (!UIUtils.isNetworkAvailable(context)) {
						responseString = getString(R.string.no_connection);
					}

                    if( mAdapter.getItemCount() > 0) {
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
        Intent intent = new Intent(getActivity(), FerriesSchedulesSyncService.class);
        intent.putExtra("forceUpdate", true);
        getActivity().startService(intent);        
    }
}
