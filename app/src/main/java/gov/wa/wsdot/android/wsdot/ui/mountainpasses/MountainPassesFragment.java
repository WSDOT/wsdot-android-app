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

package gov.wa.wsdot.android.wsdot.ui.mountainpasses;

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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

public class MountainPassesFragment extends BaseFragment implements
        LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = MountainPassesFragment.class.getSimpleName();
    private MountainPassesSyncReceiver mMountainPassesSyncReceiver;
    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    private static MountainPassAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(getActivity(), MountainPassesSyncService.class);
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

        mAdapter = new MountainPassAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAdapter);

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

        getActivity().unregisterReceiver(mMountainPassesSyncReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mMountainPassesSyncReceiver = new MountainPassesSyncReceiver();
        getActivity().registerReceiver(mMountainPassesSyncReceiver, filter);
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
        cursor.moveToFirst();
        swipeRefreshLayout.setRefreshing(false);
        mAdapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        mAdapter.swapCursor(null);
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

            swipeRefreshLayout.post(new Runnable() {
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            forceLoad();
        }
    }

    public class MountainPassesSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            mEmptyView.setVisibility(View.GONE);

            if (responseString != null) {
                if (responseString.equals("OK")) {
                    getLoaderManager().restartLoader(0, null, MountainPassesFragment.this);
                } else if (responseString.equals("NOP")) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Log.e("MountPassSyncReceiver", responseString);
                    swipeRefreshLayout.setRefreshing(false);

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
        Intent intent = new Intent(getActivity(), MountainPassesSyncService.class);
        intent.putExtra("forceUpdate", true);
        getActivity().startService(intent);
    }

    /**
     * Custom adapter for items in recycler view that need a cursor adapter.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see CursorRecyclerAdapter
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class MountainPassAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;
        private List<MtPassVH> mItems = new ArrayList<>();

        public MountainPassAdapter(Context context, Cursor c) {
            super(c);
            this.context = context;
        }

        @Override
        public void onBindViewHolderCursor(RecyclerView.ViewHolder viewHolder, Cursor cursor) {

            MtPassVH mtpassVH = (MtPassVH) viewHolder;

            String title = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_NAME));
            mtpassVH.title.setText(title);
            mtpassVH.title.setTypeface(tfb);

            mtpassVH.setCursorPos(cursor.getPosition());

            String created_at = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_DATE_UPDATED));
            mtpassVH.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
            mtpassVH.created_at.setTypeface(tf);

            String text = cursor.getString(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_CONDITION));

            if (text.equals("")) {
                mtpassVH.text.setVisibility(View.GONE);
            } else {
                mtpassVH.text.setVisibility(View.VISIBLE);
                mtpassVH.text.setText(text);
                mtpassVH.text.setTypeface(tf);
            }

            int icon = cursor.getInt(cursor.getColumnIndex(MountainPasses.MOUNTAIN_PASS_WEATHER_ICON));
            mtpassVH.icon.setImageResource(icon);

            mtpassVH.star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));

            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            mtpassVH.star_button.setOnCheckedChangeListener(null);

            mtpassVH.star_button
                    .setChecked(cursor.getInt(cursor
                            .getColumnIndex(MountainPasses.MOUNTAIN_PASS_IS_STARRED)) != 0);
            mtpassVH.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
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
        public MtPassVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_details_with_icon, null);
            MtPassVH viewholder = new MtPassVH(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        @Override
        public int getItemViewType(int position) {
            return 2;
        }

        // View Holder for Mt pass list items.
        private class MtPassVH extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView icon;
            TextView title;
            TextView created_at;
            TextView text;
            CheckBox star_button;
            int itemId;

            public MtPassVH(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                created_at = (TextView) view.findViewById(R.id.created_at);
                text = (TextView) view.findViewById(R.id.text);
                icon = (ImageView) view.findViewById(R.id.icon);
                star_button = (CheckBox) view.findViewById(R.id.star_button);
                view.setOnClickListener(this);
            }
            public void setCursorPos(int position){
                this.itemId = position;
            }

            public void onClick(View v) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(itemId);
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
    }
}
