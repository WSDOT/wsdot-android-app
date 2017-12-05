package gov.wa.wsdot.android.wsdot.ui.alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import gov.wa.wsdot.android.wsdot.ui.trafficmap.incidents.TrafficAlertsListFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

/**
 * Fragment for displaying a list of alerts.
 *
 *  getAlerts() left Abstract for custom implementations
 */

public abstract class AlertsListFragment extends BaseFragment
        implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = TrafficAlertsListFragment.class.getSimpleName();
    private static ArrayList<HighwayAlertsItem> trafficAlertItems = new ArrayList<>();
    private static AlertsListFragment.Adapter mAdapter;
    private static SwipeRefreshLayout swipeRefreshLayout;

    private GetAlertsTask alertsTask = null;

    private AlertsListFragment.HighwayAlertsSyncReceiver mHighwayAlertsSyncReceiver;
    private Intent alertsIntent;

    private Typeface tf;
    private Typeface tfb;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private final int INCIDENT = 0;
    private final int CONSTRUCTION = 1;
    private final int CLOSURE = 2;
    private final int SPECIAL_EVENTS = 3;
    private final int AMBER = 24;

    /**
     *
     * @param alerts
     * @return
     */
    protected abstract ArrayList<HighwayAlertsItem> getAlerts(ArrayList<HighwayAlertsItem> alerts);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
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
        mAdapter = new AlertsListFragment.Adapter();

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

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        alertsIntent = new Intent(getActivity(), HighwayAlertsSyncService.class);
        alertsIntent.putExtra("forceUpdate", true);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter alertsFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.HIGHWAY_ALERTS_RESPONSE");
        alertsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mHighwayAlertsSyncReceiver = new AlertsListFragment.HighwayAlertsSyncReceiver();
        getActivity().registerReceiver(mHighwayAlertsSyncReceiver, alertsFilter);
        getActivity().startService(alertsIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mHighwayAlertsSyncReceiver);
        if (alertsTask != null) {
            alertsTask.cancel(true);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_ID,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_HEADLINE,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LATITUDE,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LONGITUDE,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_END_LATITUDE,
                WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_END_LONGITUDE,
        };

        Uri baseUri = Uri.withAppendedPath(WSDOTContract.HighwayAlerts.CONTENT_URI, Uri.encode(""));

        CursorLoader cursorLoader = new TrafficAlertsListFragment.HighwayLoader(getActivity(),
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
            swipeRefreshLayout.setRefreshing(true);
            forceLoad();
        }

        @Override
        public Cursor loadInBackground() {
            return super.loadInBackground();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        alertsTask = new GetAlertsTask();
        // We can ignore this warning because we know the varargs param
        // will for sure be an ArrayList of HighwayAlertItems.
        alertsTask.execute(getAllAlerts(cursor));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        alertsTask.cancel(true);
    }

    private class GetAlertsTask extends AsyncTask<ArrayList<HighwayAlertsItem>, Void, ArrayList<HighwayAlertsItem>> {

        @Override
        protected ArrayList<HighwayAlertsItem> doInBackground(ArrayList<HighwayAlertsItem>[] params) {
            return getAlerts(params[0]);
        }

        protected void onPostExecute(ArrayList<HighwayAlertsItem> result) {
            trafficAlertItems = result;
            mAdapter.clear();
            mAdapter.setData(trafficAlertItems);
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private ArrayList<HighwayAlertsItem> getAllAlerts(Cursor cursor){
        ArrayList<HighwayAlertsItem> allAlerts = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                HighwayAlertsItem item = new HighwayAlertsItem();
                item.setHeadlineDescription(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_HEADLINE)));
                item.setEventCategory(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY)).toLowerCase());
                item.setLastUpdatedTime(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED)));
                item.setAlertId(Integer.toString(cursor.getInt(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_ID))));
                item.setStartLatitude(cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LATITUDE)));
                item.setStartLongitude(cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LONGITUDE)));
                item.setEndLatitude(cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_END_LATITUDE)));
                item.setEndLongitude(cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_END_LONGITUDE)));

                allAlerts.add(item);
                cursor.moveToNext();
            }
        }
        return allAlerts;
    }

    public class HighwayAlertsSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {
                    // We've got cameras, now add them.
                    getLoaderManager().destroyLoader(0);
                    getLoaderManager().initLoader(0, null, AlertsListFragment.this);
                } else {
                    Toast.makeText(AlertsListFragment.this.getContext(), "Failed to load. Check your connection.", Toast.LENGTH_SHORT).show();
                    Log.e("HighwaySyncReceiver", responseString);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    /**
     * Custom adapter for items in recycler view.
     * <p>
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
        private Stack<HighwayAlertsItem> special = new Stack<>();
        private Stack<HighwayAlertsItem> amberalert = new Stack<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.incident_item, parent, false);
                    return new AlertsListFragment.ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.list_header, parent, false);
                    return new AlertsListFragment.TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            TrafficAlertsListFragment.ItemViewHolder itemholder;
            TrafficAlertsListFragment.TitleViewHolder titleholder;

            if (getItemViewType(position) == TYPE_ITEM) {
                itemholder = (TrafficAlertsListFragment.ItemViewHolder) viewholder;
                itemholder.textView.setText(mData.get(position).getHeadlineDescription());
                itemholder.updated.setText(ParserUtils.relativeTime(
                        mData.get(position).getLastUpdatedTime(),
                        "MMMM d, yyyy h:mm a", false));
                itemholder.id = mData.get(position).getAlertId();
            } else {
                titleholder = (TrafficAlertsListFragment.TitleViewHolder) viewholder;
                titleholder.textView.setText(mData.get(position).getHeadlineDescription());
                if (position == 0) {
                    titleholder.divider.setVisibility(View.GONE);
                } else {
                    titleholder.divider.setVisibility(View.VISIBLE);
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
                for (int i = 0; i < size; i++) {
                    // Check if Traffic Management Center is closed

                    Integer category_id = getCategoryID(data.get(i).getEventCategory());

                    // Check if there is an active amber alert
                    if (category_id.equals(AMBER)) {
                        amberalert.push(data.get(i));
                    } else if (category_id.equals(CLOSURE)) {
                        closure.push(data.get(i));
                    } else if (category_id.equals(CONSTRUCTION)) {
                        construction.push(data.get(i));
                    } else if (category_id.equals(INCIDENT)) {
                        incident.push(data.get(i));
                    } else if (category_id.equals(SPECIAL_EVENTS)) {
                        special.push(data.get(i));
                    }
                }

                if (amberalert != null && amberalert.size() != 0) {
                    mAdapter.addSeparatorItem(new HighwayAlertsItem("Amber Alerts"));
                    while (!amberalert.empty()) {
                        mAdapter.addItem(amberalert.pop());
                    }
                }

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

                mAdapter.addSeparatorItem(new HighwayAlertsItem("Special Events"));
                if (special.empty()) {
                    mAdapter.addItem(new HighwayAlertsItem("None reported"));
                } else {
                    while (!special.empty()) {
                        mAdapter.addItem(special.pop());
                    }
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

        public void clear() {
            mData.clear();
            mSeparatorsSet.clear();
            closure.clear();
            construction.clear();
            incident.clear();
            amberalert.clear();
            special.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    /**
     * ViewHolder for a traffic alert
     * <p>
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
        private TextView textView;
        private LinearLayout divider;

        public TitleViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.list_header_title);
            textView.setTypeface(tfb);
            divider = (LinearLayout) itemView.findViewById(R.id.divider);
        }
    }

    public void onRefresh() {

        getActivity().startService(alertsIntent);
    }

    private int getCategoryID(String category) {

        // Types of categories
        String[] event_closure = {"closed", "closure"};
        String[] event_construction = {"construction", "maintenance", "lane closure"};
        String[] event_special = {"special event"};
        String[] event_amber = {"amber"};

        HashMap<String, String[]> eventCategories = new HashMap<>();
        eventCategories.put("closure", event_closure);
        eventCategories.put("construction", event_construction);
        eventCategories.put("amber", event_amber);
        eventCategories.put("special", event_special);

        Set<Map.Entry<String, String[]>> set = eventCategories.entrySet();
        Iterator<Map.Entry<String, String[]>> i = set.iterator();

        if (category.equals("")) return INCIDENT;

        while (i.hasNext()) {
            Map.Entry<String, String[]> me = i.next();
            for (String phrase : me.getValue()) {
                Pattern pattern = Pattern.compile(phrase, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(category);
                boolean matchFound = matcher.find();
                if (matchFound) {
                    String keyWord = me.getKey();
                    if (keyWord.equalsIgnoreCase("closure")) {
                        return CLOSURE;
                    } else if (keyWord.equalsIgnoreCase("construction")) {
                        return CONSTRUCTION;
                    } else if (keyWord.equalsIgnoreCase("amber")) {
                        return AMBER;
                    } else if (keyWord.equalsIgnoreCase("special")) {
                        return SPECIAL_EVENTS;
                    }
                }
            }
        }
        return INCIDENT;
    }
}