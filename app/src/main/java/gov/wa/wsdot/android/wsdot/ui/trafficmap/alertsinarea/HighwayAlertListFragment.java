package gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.alert.detail.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

/**
 * Fragment for displaying a list of alerts.
 */

public class HighwayAlertListFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = HighwayAlertListFragment.class.getSimpleName();
    private static ArrayList<HighwayAlertsItem> trafficAlertItems = new ArrayList<>();
    private static HighwayAlertListFragment.Adapter mAdapter;
    private static SwipeRefreshLayout swipeRefreshLayout;

    private Typeface tf;
    private Typeface tfb;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private final int INCIDENT = 0;
    private final int CONSTRUCTION = 1;
    private final int CLOSURE = 2;
    private final int SPECIAL_EVENTS = 3;
    private final int AMBER = 24;

    private static HighwayAlertListViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new HighwayAlertListFragment.Adapter();

        mRecyclerView.setAdapter(mAdapter);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HighwayAlertListViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Retrieve the bounds from the intent. Defaults to 0
        Intent intent = getActivity().getIntent();

        String routeString = intent.getStringExtra("route");

        if (routeString == null) {

            Double nelat = intent.getDoubleExtra("nelat", 0.0);
            Double nelong = intent.getDoubleExtra("nelong", 0.0);
            Double swlat = intent.getDoubleExtra("swlat", 0.0);
            Double swlong = intent.getDoubleExtra("swlong", 0.0);

            LatLng northEast = new LatLng(nelat, nelong);
            LatLng southWest = new LatLng(swlat, swlong);

            LatLngBounds mBounds = new LatLngBounds(southWest, northEast);

            viewModel.getHighwayAlertsInBounds(mBounds).observe(this, alerts -> {
                if (alerts != null) {
                    trafficAlertItems = new ArrayList<>(alerts);
                    mAdapter.clear();
                    mAdapter.setData(trafficAlertItems);
                }
            });

        } else {



        }

        return root;
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

            View itemView;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.incident_item, parent, false);
                    return new HighwayAlertListFragment.ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.list_header, parent, false);
                    return new HighwayAlertListFragment.TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            HighwayAlertListFragment.ItemViewHolder itemholder;
            HighwayAlertListFragment.TitleViewHolder titleholder;

            if (getItemViewType(position) == TYPE_ITEM) {
                itemholder = (HighwayAlertListFragment.ItemViewHolder) viewholder;
                itemholder.textView.setText(mData.get(position).getHeadlineDescription());
                itemholder.updated.setText(ParserUtils.relativeTime(
                        mData.get(position).getLastUpdatedTime(),
                        "MMMM d, yyyy h:mm a", false));
                itemholder.id = mData.get(position).getAlertId();
            } else {
                titleholder = (HighwayAlertListFragment.TitleViewHolder) viewholder;
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
            textView = itemView.findViewById(R.id.description);
            updated = itemView.findViewById(R.id.last_updated);
            textView.setTypeface(tf);
            updated.setTypeface(tf);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (id != null) {
                Bundle b = new Bundle();
                Intent intent = new Intent(getActivity(), HighwayAlertDetailsActivity.class);
                b.putInt("id", Integer.valueOf(id));
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
        viewModel.forceRefreshHighwayAlerts();
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