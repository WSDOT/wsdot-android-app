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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.incidents;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.service.HighwayAlertsSyncService;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.alert.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class TrafficAlertsFragment extends BaseFragment
        implements LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = TrafficAlertsFragment.class.getSimpleName();
	private static ArrayList<HighwayAlertsItem> trafficAlertItems = null;
    private static Adapter mAdapter;
	private static View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    private HighwayAlertsSyncReceiver mHighwayAlertsSyncReceiver;
    private Intent alertsIntent;

    private Typeface tf;
    private Typeface tfb;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private Double nelat;
    private Double nelong;
    private Double swlat;
    private Double swlong;

    private LatLngBounds mBounds;

    private final int INCIDENT = 0;
    private final int CONSTRUCTION = 1;
    private final int CLOSURE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);

        //Retrieve the bounds from the intent. Defaults to 0
        Intent intent = getActivity().getIntent();
        nelat = intent.getDoubleExtra("nelat", 0.0);
        nelong = intent.getDoubleExtra("nelong", 0.0);
        swlat = intent.getDoubleExtra("swlat", 0.0);
        swlong = intent.getDoubleExtra("swlong", 0.0);

        LatLng northEast = new LatLng(nelat, nelong);
        LatLng southWest = new LatLng(swlat, swlong);

        mBounds = new LatLngBounds(southWest, northEast);
	}    
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter();

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
        
        mEmptyView = root.findViewById(R.id.empty_list_view);

        return root;
    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        alertsIntent = new Intent(getActivity(), HighwayAlertsSyncService.class);
	}

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter alertsFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.HIGHWAY_ALERTS_RESPONSE");
        alertsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mHighwayAlertsSyncReceiver = new HighwayAlertsSyncReceiver();
        getActivity().registerReceiver(mHighwayAlertsSyncReceiver, alertsFilter);

        getActivity().startService(alertsIntent);
    }


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mHighwayAlertsSyncReceiver);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_ID,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_HEADLINE,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LATITUDE,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LONGITUDE,
        };

        Uri baseUri = Uri.withAppendedPath(WSDOTContract.HighwayAlerts.CONTENT_URI, Uri.encode(""));

        CursorLoader cursorLoader = new HighwayLoader(getActivity(),
                baseUri,
                projection,
                null,
                null,
                null
        );

        return cursorLoader;
    }

    public static class HighwayLoader extends CursorLoader {

        public HighwayLoader(Context context, Uri uri,
                                  String[] projection, String selection, String[] selectionArgs,
                                  String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            mAdapter.clear();
            swipeRefreshLayout.setRefreshing(true);
            forceLoad();
        }
        @Override
        public Cursor loadInBackground() {
            return super.loadInBackground();
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.clear();
        mEmptyView.setVisibility(View.GONE);
        HighwayAlertsItem i;
        trafficAlertItems = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                Double latitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LATITUDE));
                Double longitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LONGITUDE));

                LatLng alertLocation = new LatLng(latitude, longitude);

                // If alert is within bounds of shown screen show it on list
                if (mBounds.contains(alertLocation) ||
                        cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY)).toLowerCase().equals("amber")) {

                    i = new HighwayAlertsItem();

                    i.setHeadlineDescription(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_HEADLINE)));
                    i.setEventCategory(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY)).toLowerCase());
                    i.setLastUpdatedTime(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED)));
                    i.setAlertId(Integer.toString(cursor.getInt(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_ID))));

                    trafficAlertItems.add(i);
                }
                cursor.moveToNext();
            }

            mAdapter.setData(trafficAlertItems);
        } else {
            TextView t = (TextView) mEmptyView;
            t.setText(R.string.no_list_data);
            mEmptyView.setVisibility(View.VISIBLE);
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.setData(null);
    }


    public class HighwayAlertsSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {
                    // We've got cameras, now add them.
                    getLoaderManager().initLoader(0, null, TrafficAlertsFragment.this);
                } else {
                    Log.e("CameraDownloadReceiver", responseString);
                }
            }
        }
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private ArrayList<HighwayAlertsItem> mData = new ArrayList<>();
        private TreeSet<Integer> mSeparatorsSet = new TreeSet<>();
        private Stack<HighwayAlertsItem> closure = new Stack<>();
        private Stack<HighwayAlertsItem> construction = new Stack<>();
        private Stack<HighwayAlertsItem> incident = new Stack<>();
        private Stack<HighwayAlertsItem> closed = new Stack<>();
        private Stack<HighwayAlertsItem> amberalert = new Stack<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.incident_item, parent, false);
                    return new ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.list_header, parent, false);
                    return new TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            ItemViewHolder itemholder;
            TitleViewHolder titleholder;

            if (getItemViewType(position) == TYPE_ITEM){
                itemholder = (ItemViewHolder) viewholder;
                itemholder.textView.setText(mData.get(position).getHeadlineDescription());
                itemholder.updated.setText(ParserUtils.relativeTime(
                        mData.get(position).getLastUpdatedTime(),
                        "MMMM d, yyyy h:mm a", false));
                itemholder.id = mData.get(position).getAlertId();
            }else{
                titleholder = (TitleViewHolder) viewholder;
                titleholder.textView.setText(mData.get(position).getHeadlineDescription());
                if (position == 0){
                    titleholder.divider.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }

        public void setData(ArrayList<HighwayAlertsItem> data) {
            mData.clear();
            if (data != null) {
                int size = data.size();
                for (int i=0; i < size; i++) {
                    // Check if Traffic Management Center is closed

                    Integer category_id = getCategoryID(data.get(i).getEventCategory());

                    if (category_id.equals(27)) {
                        closed.push(data.get(i));
                        break; // TSMC is closed so stop here
                    }
                    // Check if there is an active amber alert
                    else if (category_id.equals(24)) {
                        amberalert.push(data.get(i));
                    }
                    else if (category_id == CLOSURE) {
                        closure.push(data.get(i));
                    }
                    else if (category_id == CONSTRUCTION) {
                        construction.push(data.get(i));
                    }
                    else if (category_id == INCIDENT) {
                        incident.push(data.get(i));
                    }
                }

                if (amberalert != null && amberalert.size() != 0) {
                    mAdapter.addSeparatorItem(new HighwayAlertsItem("Amber Alerts"));
                    while (!amberalert.empty()) {
                        mAdapter.addItem(amberalert.pop());
                    }
                }
                if (closed != null && closed.size() == 0) {

                    mAdapter.addSeparatorItem(new HighwayAlertsItem("Incidents"));
                    if (incident.empty()) {
                        mAdapter.addItem(new HighwayAlertsItem("None reported"));
                    } else {
                        while (!incident.empty()) {
                            mAdapter.addItem(incident.pop());
                        }
                    }

                    mAdapter.addSeparatorItem(new HighwayAlertsItem("Construction Closures"));
                    if (construction.empty()) {
                        mAdapter.addItem(new HighwayAlertsItem("None reported"));
                    } else {
                        while (!construction.empty()) {
                            mAdapter.addItem(construction.pop());
                        }
                    }

                    mAdapter.addSeparatorItem(new HighwayAlertsItem("Road Closures"));
                    if (closure.empty()) {
                        mAdapter.addItem(new HighwayAlertsItem("None reported"));
                    } else {
                        while (!closure.empty()) {
                            mAdapter.addItem(closure.pop());
                        }
                    }

                } else {
                    mAdapter.addItem(closed.pop());
                }
                mAdapter.notifyDataSetChanged();
            }
        }


        public void addItem(final HighwayAlertsItem item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        public void addSeparatorItem(final HighwayAlertsItem item) {
            mData.add(item);
            // save separator position
            mSeparatorsSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }

        public void clear(){
            mData.clear();
            mSeparatorsSet.clear();
            closure.clear();
            construction.clear();
            incident.clear();
            closed.clear();
            amberalert.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

    /**
     * ViewHolder for a traffic alert
     *
     * holds the alert id for onClick()
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView textView;
        protected TextView updated;
        protected String id;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.description);
            updated = (TextView) itemView.findViewById(R.id.last_updated);
            textView.setTypeface(tf);
            updated.setTypeface(tf);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (id != null) {
                Bundle b = new Bundle();
                Intent intent = new Intent(getActivity(), HighwayAlertDetailsActivity.class);
                b.putString("id", id);
                intent.putExtras(b);
                startActivity(intent);
            }
        }
    }
    public class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;
        protected LinearLayout divider;

        public TitleViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.list_header_title);
            textView.setTypeface(tfb);
            divider = (LinearLayout) itemView.findViewById(R.id.divider);
        }
    }

    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);        
    }

    private int getCategoryID(String category) {

        // Types of categories
        String[] event_closure = {"closed", "closure"};
        String[] event_construction = {"construction", "maintenance", "lane closure"};
        String[] event_amber = {"amber"};

        HashMap<String, String[]> eventCategories = new HashMap<>();
        eventCategories.put("closure", event_closure);
        eventCategories.put("construction", event_construction);
        eventCategories.put("amber", event_amber);

        Set<Map.Entry<String, String[]>> set = eventCategories.entrySet();
        Iterator<Map.Entry<String, String[]>> i = set.iterator();

        if (category.equals("")) return 0; //incident

        while(i.hasNext()) {
            Map.Entry<String, String[]> me = i.next();
            for (String phrase: me.getValue()) {
                String patternStr = phrase;
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(category);
                boolean matchFound = matcher.find();
                if (matchFound) {
                    String keyWord = me.getKey();
                    if (keyWord.equalsIgnoreCase("closure")) {
                        return 2;
                    } else if (keyWord.equalsIgnoreCase("construction")) {
                        return 1;
                    } else if (keyWord.equalsIgnoreCase("amber")){
                        return 24;
                    }
                }
            }
        }
        return 0;
    }
}
