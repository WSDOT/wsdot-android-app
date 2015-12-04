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

package gov.wa.wsdot.android.wsdot.ui.amtrakcascades;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesScheduleFeed;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesScheduleItem;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesServiceItem;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class AmtrakCascadesSchedulesDetailsFragment extends BaseListFragment
        implements LoaderCallbacks<ArrayList<AmtrakCascadesServiceItem>>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = AmtrakCascadesSchedulesDetailsFragment.class.getSimpleName();
    
    private View mHeaderView;
    private Typeface tf;
    private Typeface tfb;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private View mEmptyView;
    
    private static Map<Integer, String> trainNumberMap = new HashMap<Integer, String>();
    private static Map<String, String> amtrakStations = new HashMap<String, String>();
    private static String statusDate;
    private static String fromLocation;
    private static String toLocation;
    private static ScheduleAdapter mAdapter;
    private static String WSDOT_API_ACCESS_CODE;
    private static TextView schedule_title;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        Bundle args = activity.getIntent().getExtras();
        statusDate = args.getString("dayId");
        fromLocation = args.getString("originId");
        toLocation = args.getString("destinationId");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WSDOT_API_ACCESS_CODE = getString(R.string.wsdot_api_access_code);

        getTrainNumbers();
        getAmtrakStations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_textview_swipe_refresh, null);
        schedule_title = (TextView) root.findViewById(R.id.title);
        schedule_title.setTypeface(tfb);

        if (toLocation.equalsIgnoreCase("N/A")) {
            schedule_title.setText(amtrakStations.get(fromLocation));
        } else if ((fromLocation.equalsIgnoreCase(toLocation))) {
            schedule_title.setText(amtrakStations.get(fromLocation));
        } else {
            schedule_title.setText("Departing: "
                    + amtrakStations.get(fromLocation)
                    + " and Arriving: "
                    + amtrakStations.get(toLocation));
        }

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                17170451,  // android.R.color.holo_blue_bright 
                17170452,  // android.R.color.holo_green_light 
                17170456,  // android.R.color.holo_orange_light 
                17170454); // android.R.color.holo_red_light)        
        
        mHeaderView = inflater.inflate(R.layout.list_item_amtrakschedules_times_header, null);
        TextView departing_title = (TextView) mHeaderView.findViewById(R.id.departing_title);
        departing_title.setTypeface(tfb);
        TextView arriving_title = (TextView) mHeaderView.findViewById(R.id.arriving_title);
        arriving_title.setTypeface(tfb);
        
        mEmptyView = root.findViewById(R.id.empty_list_view);
        
        disableAds(root);
        
        return root;
    }

    private void getTrainNumbers() {
        trainNumberMap.put(7, "Empire Builder Train");
        trainNumberMap.put(8, "Empire Builder Train");
        trainNumberMap.put(11, "Coast Starlight Train");
        trainNumberMap.put(14, "Coast Starlight Train");
        trainNumberMap.put(27, "Empire Builder Train");
        trainNumberMap.put(28, "Empire Builder Train");
        trainNumberMap.put(500, "Amtrak Cascades Train");
        trainNumberMap.put(501, "Amtrak Cascades Train");
        trainNumberMap.put(503, "Amtrak Cascades Train");
        trainNumberMap.put(505, "Amtrak Cascades Train");
        trainNumberMap.put(506, "Amtrak Cascades Train");
        trainNumberMap.put(507, "Amtrak Cascades Train");
        trainNumberMap.put(508, "Amtrak Cascades Train");
        trainNumberMap.put(509, "Amtrak Cascades Train");
        trainNumberMap.put(510, "Amtrak Cascades Train");
        trainNumberMap.put(511, "Amtrak Cascades Train");
        trainNumberMap.put(513, "Amtrak Cascades Train");
        trainNumberMap.put(516, "Amtrak Cascades Train");
        trainNumberMap.put(517, "Amtrak Cascades Train");
    }

    private void getAmtrakStations() {
        amtrakStations.put("VAC", "Vancouver, BC");
        amtrakStations.put("BEL", "Bellingham, WA");
        amtrakStations.put("MVW", "Mount Vernon, WA");
        amtrakStations.put("STW", "Stanwood, WA");
        amtrakStations.put("EVR", "Everett, WA");
        amtrakStations.put("EDM", "Edmonds, WA");
        amtrakStations.put("SEA", "Seattle, WA");
        amtrakStations.put("TUK", "Tukwila, WA");
        amtrakStations.put("TAC", "Tacoma, WA");
        amtrakStations.put("OLW", "Olympia/Lacey, WA");
        amtrakStations.put("CTL", "Centralia, WA");
        amtrakStations.put("KEL", "Kelso/Longview, WA");
        amtrakStations.put("VAN", "Vancouver, WA");
        amtrakStations.put("PDX", "Portland, OR");
        amtrakStations.put("ORC", "Oregon City, OR");
        amtrakStations.put("SLM", "Salem, OR");
        amtrakStations.put("ALY", "Albany, OR");
        amtrakStations.put("EUG", "Eugene, OR");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        mAdapter = new ScheduleAdapter(getActivity());
        this.getListView().addHeaderView(mHeaderView);
        setListAdapter(mAdapter);

        // Prepare the loaders. Either re-connect with an existing one, or start new ones.
        getLoaderManager().initLoader(0, null, this);
    }

    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);        
    }

    public Loader<ArrayList<AmtrakCascadesServiceItem>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
        if (toLocation.equalsIgnoreCase("N/A")) {
            return new DepartingTrainsLoader(getActivity());
        } else if ((fromLocation.equalsIgnoreCase(toLocation))) {
            toLocation = "N/A";
            return new DepartingTrainsLoader(getActivity());
        } else {
            return new DepartingArrivingTrainsLoader(getActivity());
        }
    }

    public void onLoadFinished(Loader<ArrayList<AmtrakCascadesServiceItem>> loader,
            ArrayList<AmtrakCascadesServiceItem> data) {
        
        if (!data.isEmpty()) {
            mAdapter.setData(data);
        } else {
            TextView t = (TextView) mEmptyView;
            t.setText(R.string.no_connection);
            getListView().setEmptyView(mEmptyView);
        }
        
        swipeRefreshLayout.setRefreshing(false);
    }

    public void onLoaderReset(Loader<ArrayList<AmtrakCascadesServiceItem>> loader) {
        swipeRefreshLayout.setRefreshing(false);
        mAdapter.setData(null);        
    }

    /**
     * Get train schedules for those with a departing and arriving station.
     * 
     */
    public static class DepartingArrivingTrainsLoader extends AsyncTaskLoader<ArrayList<AmtrakCascadesServiceItem>> {

        private ArrayList<AmtrakCascadesServiceItem> mServiceItems = null;

        public DepartingArrivingTrainsLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<AmtrakCascadesServiceItem> loadInBackground() {

            mServiceItems = new ArrayList<AmtrakCascadesServiceItem>();
            AmtrakCascadesScheduleItem scheduleItem = null;
            URL url;
            
            try {
                url = new URL(
                        "http://www.wsdot.wa.gov/traffic/api/amtrak/Schedulerest.svc/GetScheduleAsJson"
                                + "?AccessCode=" + WSDOT_API_ACCESS_CODE
                                + "&StatusDate=" + statusDate
                                + "&TrainNumber=-1"
                                + "&FromLocation=" + fromLocation
                                + "&ToLocation=" + toLocation
                        );

                URLConnection urlConn = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String jsonFile = "";
                String line;
                
                while ((line = in.readLine()) != null) {
                    jsonFile += line;
                }
                in.close();            
            
                Gson gson = new GsonBuilder().serializeNulls().create();
                AmtrakCascadesScheduleFeed[] scheduleFeed = gson.fromJson(jsonFile, AmtrakCascadesScheduleFeed[].class);
                int numItems = scheduleFeed.length;

                int i = 0;
                int startingTripNumber = 0;
                int currentTripNumber = 0;

                while (i < numItems) { // Loop through all trains
                    Date scheduledDepartureTime = null;
                    Map<String, AmtrakCascadesScheduleItem> stationItems;
                    List<Map<String, AmtrakCascadesScheduleItem>> locationItems;
                    locationItems = new ArrayList<Map<String, AmtrakCascadesScheduleItem>>();
                    stationItems = new HashMap<String, AmtrakCascadesScheduleItem>();
                    
                    startingTripNumber = scheduleFeed[i].getTripNumber();
                    currentTripNumber = startingTripNumber;
                    List<String> trainNameList = new ArrayList<String>();
                    int tripCounter = 0;
                    while (currentTripNumber == startingTripNumber && i < numItems) { // Trains are grouped by two or more 
                        scheduleItem = new AmtrakCascadesScheduleItem();

                        if (scheduleFeed[i].getArrivalComment() != null) {
                            scheduleItem.setArrivalComment(scheduleFeed[i].getArrivalComment());
                        }

                        if (scheduleFeed[i].getArrivalScheduleType() != null) {
                            scheduleItem.setArrivalScheduleType(scheduleFeed[i].getArrivalScheduleType());
                        }

                        if (scheduleFeed[i].getArrivalTime() != null) {
                            scheduleItem.setArrivalTime(scheduleFeed[i].getArrivalTime().toString().substring(6, 19));
                        }

                        if (scheduleFeed[i].getDepartureComment() != null) {
                            scheduleItem.setDepartureComment(scheduleFeed[i].getDepartureComment());
                        }

                        if (scheduleFeed[i].getDepartureScheduleType() != null) {
                            scheduleItem.setDepartureScheduleType(scheduleFeed[i].getDepartureScheduleType());
                        }

                        if (scheduleFeed[i].getDepartureTime() != null) {
                            scheduleItem.setDepartureTime(scheduleFeed[i].getDepartureTime().toString().substring(6, 19));
                        }

                        if (scheduleFeed[i].getScheduledArrivalTime() != null) {
                            scheduleItem.setScheduledArrivalTime(scheduleFeed[i].getScheduledArrivalTime().toString().substring(6, 19));
                        }

                        scheduleItem.setStationName(scheduleFeed[i].getStationName());
                        
                        if (scheduleFeed[i].getTrainMessage() != "") {
                            scheduleItem.setTrainMessage(scheduleFeed[i].getTrainMessage());
                        }

                        if (scheduleFeed[i].getScheduledDepartureTime() != null) {
                            scheduleItem.setScheduledDepartureTime(
                                    scheduleFeed[i].getScheduledDepartureTime().toString().substring(6, 19));
                            
                            // We sort by scheduled departure time of the From station.
                            if (fromLocation.equalsIgnoreCase(scheduleItem.getStationName())) {
                                scheduledDepartureTime = new Date(
                                        Long.parseLong((scheduleItem
                                                .getScheduledDepartureTime())));
                            }
                        }

                        int trainNumber = scheduleFeed[i].getTrainNumber();
                        scheduleItem.setTrainNumber(trainNumber);
                        String serviceName = trainNumberMap.get(trainNumber);

                        if (serviceName == null) {
                            serviceName = "Bus Service";
                        }

                        scheduleItem.setSortOrder(scheduleFeed[i].getSortOrder());
                        String trainName = trainNumber + " " + serviceName;
                        
                        // Add the train name for ever other record. When there is one origin and destination point
                        // the train name will be the same. If the tripNumber is the same over more than two records
                        // then we have multiple origin and destination points and likely different train names.
                        // e.g. 515 Amtrak Cascades Train, 8911 Bus Service
                        if (tripCounter % 2 == 0) {
                            trainNameList.add(trainName);
                        }
                        scheduleItem.setTrainName(trainName);
                        scheduleItem.setTripNumber(scheduleFeed[i].getTripNumber());
                        scheduleItem.setUpdateTime(scheduleFeed[i].getUpdateTime().toString().substring(6, 19));

                        stationItems.put(scheduleItem.getStationName(), scheduleItem);

                        i++;
                        if (i < numItems) {
                            currentTripNumber = scheduleFeed[i].getTripNumber();
                        }

                        tripCounter++;
                    }

                    if (trainNameList.size() > 1) {
                        StringBuilder sb = new StringBuilder();
                        for (String s: trainNameList) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(s);
                        }
                        stationItems.get(fromLocation).setTrainName(sb.toString());
                    }

                    locationItems.add(stationItems);
                    mServiceItems.add(new AmtrakCascadesServiceItem(scheduledDepartureTime, locationItems));
                }
                
                Collections.sort(mServiceItems, AmtrakCascadesServiceItem.scheduledDepartureTimeComparator);
                
            } catch (Exception e) {
                Log.e(TAG, "Error in network call", e);
            }
            
            return mServiceItems;
        }

        @Override
        public void deliverResult(ArrayList<AmtrakCascadesServiceItem> data) {
            /**
             * Called when there is new data to deliver to the client. The
             * super class will take care of delivering it; the implementation
             * here just adds a little more logic.
             */ 
            super.deliverResult(data);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            
            mAdapter.clear();
            swipeRefreshLayout.post(new Runnable() {
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ArrayList<AmtrakCascadesServiceItem> data) {
            super.onCanceled(data);
        }
        
        @Override
        protected void onReset() {
            super.onReset();
            
            // Ensure the loader is stopped
            onStopLoading();
            
            if (mServiceItems != null) {
                mServiceItems = null;
            }
        }
    }
    
    /**
     * Get train schedules for those with just a departing station.
     * 
     */
    public static class DepartingTrainsLoader extends AsyncTaskLoader<ArrayList<AmtrakCascadesServiceItem>> {

        private ArrayList<AmtrakCascadesServiceItem> mServiceItems = null;

        public DepartingTrainsLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<AmtrakCascadesServiceItem> loadInBackground() {
            
            mServiceItems = new ArrayList<AmtrakCascadesServiceItem>();
            AmtrakCascadesScheduleItem scheduleItem;
            URL url;
            
            try {
                url = new URL(
                        "http://www.wsdot.wa.gov/traffic/api/amtrak/Schedulerest.svc/GetScheduleAsJson"
                                + "?AccessCode=" + WSDOT_API_ACCESS_CODE
                                + "&StatusDate=" + statusDate
                                + "&TrainNumber=-1"
                                + "&FromLocation=" + fromLocation
                                + "&ToLocation=" + toLocation
                        );

                URLConnection urlConn = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String jsonFile = "";
                String line;
                
                while ((line = in.readLine()) != null) {
                    jsonFile += line;
                }
                in.close();            
            
                Gson gson = new GsonBuilder().serializeNulls().create();
                AmtrakCascadesScheduleFeed[] scheduleFeed = gson.fromJson(jsonFile, AmtrakCascadesScheduleFeed[].class);
                int numItems = scheduleFeed.length;

                for (int i = 0; i < numItems; i++) { // Loop through all trains
                    Date scheduledTime = null;
                    Map<String, AmtrakCascadesScheduleItem> stationItems;
                    List<Map<String, AmtrakCascadesScheduleItem>> locationItems;

                    locationItems = new ArrayList<Map<String, AmtrakCascadesScheduleItem>>();
                    stationItems = new HashMap<String, AmtrakCascadesScheduleItem>();
                    scheduleItem = new AmtrakCascadesScheduleItem();

                    if (scheduleFeed[i].getArrivalComment() != null) {
                        scheduleItem.setArrivalComment(scheduleFeed[i].getArrivalComment());
                    }

                    if (scheduleFeed[i].getArrivalScheduleType() != null) {
                        scheduleItem.setArrivalScheduleType(scheduleFeed[i].getArrivalScheduleType());
                    }

                    if (scheduleFeed[i].getArrivalTime() != null) {
                        scheduleItem.setArrivalTime(scheduleFeed[i].getArrivalTime().toString().substring(6, 19));
                    }

                    if (scheduleFeed[i].getDepartureComment() != null) {
                        scheduleItem.setDepartureComment(scheduleFeed[i].getDepartureComment());
                    }

                    if (scheduleFeed[i].getDepartureScheduleType() != null) {
                        scheduleItem.setDepartureScheduleType(scheduleFeed[i].getDepartureScheduleType());
                    }

                    if (scheduleFeed[i].getDepartureTime() != null) {
                        scheduleItem.setDepartureTime(scheduleFeed[i].getDepartureTime().toString().substring(6, 19));
                    }

                    scheduleItem.setStationName(scheduleFeed[i].getStationName());
                    
                    if (scheduleFeed[i].getTrainMessage() != "") {
                        scheduleItem.setTrainMessage(scheduleFeed[i].getTrainMessage());
                    }

                    if (scheduleFeed[i].getScheduledArrivalTime() != null) {
                        scheduleItem.setScheduledArrivalTime(
                                scheduleFeed[i].getScheduledArrivalTime().toString().substring(6, 19));
                        
                        if (fromLocation.equalsIgnoreCase(scheduleItem.getStationName())) {
                            scheduledTime = new Date(
                                    Long.parseLong((scheduleItem
                                            .getScheduledArrivalTime())));
                        }
                    }

                    if (scheduleFeed[i].getScheduledDepartureTime() != null) {
                        scheduleItem.setScheduledDepartureTime(
                                scheduleFeed[i].getScheduledDepartureTime().toString().substring(6, 19));
                        
                        // We sort by scheduled departure time of the From station.
                        if (fromLocation.equalsIgnoreCase(scheduleItem.getStationName())) {
                            scheduledTime = new Date(
                                    Long.parseLong((scheduleItem
                                            .getScheduledDepartureTime())));
                        }
                    }

                    int trainNumber = scheduleFeed[i].getTrainNumber();
                    scheduleItem.setTrainNumber(trainNumber);
                    String serviceName = trainNumberMap.get(trainNumber);
                    if (serviceName == null) {
                        serviceName = "Bus Service";
                    }
                    scheduleItem.setTrainName(trainNumber + " " + serviceName);
                    scheduleItem.setTripNumber(scheduleFeed[i].getTripNumber());
                    scheduleItem.setUpdateTime(scheduleFeed[i].getUpdateTime().toString().substring(6, 19));

                    stationItems.put(scheduleItem.getStationName(), scheduleItem);
                    locationItems.add(stationItems);
                    
                    mServiceItems.add(new AmtrakCascadesServiceItem(scheduledTime, locationItems));
                }

                Collections.sort(mServiceItems, AmtrakCascadesServiceItem.scheduledDepartureTimeComparator);
                toLocation = fromLocation;
                
            } catch (Exception e) {
                Log.e(TAG, "Error in network call", e);
            }
            
            return mServiceItems;
        }

        @Override
        public void deliverResult(ArrayList<AmtrakCascadesServiceItem> data) {
            /**
             * Called when there is new data to deliver to the client. The
             * super class will take care of delivering it; the implementation
             * here just adds a little more logic.
             */ 
            super.deliverResult(data);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            
            mAdapter.clear();
            swipeRefreshLayout.post(new Runnable() {
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ArrayList<AmtrakCascadesServiceItem> data) {
            super.onCanceled(data);
        }
        
        @Override
        protected void onReset() {
            super.onReset();
            
            // Ensure the loader is stopped
            onStopLoading();
            
            if (mServiceItems != null) {
                mServiceItems = null;
            }
        }
    }
    
    private class ScheduleAdapter extends ArrayAdapter<AmtrakCascadesServiceItem> {
        private final LayoutInflater mInflater;

        public ScheduleAdapter(Context context) {
            super(context, R.layout.list_item_departure_times);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @SuppressWarnings("unused")
        public boolean areAllItemsSelectable() {
            return false;
        }
        
        public boolean isEnabled(int position) {  
            return false;  
        }        
        
        public void setData(ArrayList<AmtrakCascadesServiceItem> data) {
            if (data != null) {
                //addAll(data); // Only in API level 11
                notifyDataSetChanged();
                int size = data.size();
                for (int i=0; i < size; i++) {
                    add(data.get(i));
                }
                notifyDataSetChanged();                
            }
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            DateFormat updateDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
            updateDateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_amtrakschedules_times, null);
                holder = new ViewHolder();
                holder.scheduledDeparture = (TextView) convertView.findViewById(R.id.scheduledDeparture);
                holder.scheduledDeparture.setTypeface(tfb);
                holder.scheduledArrival = (TextView) convertView.findViewById(R.id.scheduledArrival);
                holder.scheduledArrival.setTypeface(tfb);
                holder.departureComment = (TextView) convertView.findViewById(R.id.departureComment);
                holder.departureComment.setTypeface(tfb);
                holder.arrivalComment = (TextView) convertView.findViewById(R.id.arrivalComment);
                holder.arrivalComment.setTypeface(tfb);
                holder.trainName = (TextView) convertView.findViewById(R.id.trainName);
                holder.trainName.setTypeface(tf);
                holder.lastUpdated = (TextView) convertView.findViewById(R.id.lastUpdated);
                holder.lastUpdated.setTypeface(tf);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            AmtrakCascadesServiceItem item = getItem(position);
            
            // Departing Time
            String schedDepartureTime = null;
            try {
                schedDepartureTime = item.getLocation().get(0).get(fromLocation).getScheduledDepartureTime();
            } catch (NullPointerException e) {
            }
            
            if (schedDepartureTime != null) {
                holder.scheduledDeparture.setText(dateFormat.format(new Date(Long.parseLong(schedDepartureTime))));
            } else {
                holder.scheduledDeparture.setText("");
            }

            // Arriving Time
            String schedArrivalTime = null;
            try {
                schedArrivalTime = item.getLocation().get(0).get(toLocation).getScheduledArrivalTime();
            } catch (NullPointerException e) {
            }
            
            if ((fromLocation.equalsIgnoreCase(toLocation))
                    && (schedDepartureTime != null && schedArrivalTime != null)) {
                holder.scheduledArrival.setText("");
            } else {
                if (schedArrivalTime != null) {
                    holder.scheduledArrival.setText(dateFormat.format(new Date(Long.parseLong(schedArrivalTime))));
                } else if (schedDepartureTime != null && !toLocation.equalsIgnoreCase(fromLocation)) {
                    /* When a station only has a scheduled departure time and not arrival,
                     * it is assumed that the train only stops for a short time at that station.
                     * In these cases, the Amtrak site appears to show this scheduled departure
                     * time as the scheduled arrival time so we'll do the same thing for the app.
                     */
                    schedArrivalTime = item.getLocation().get(0).get(toLocation).getScheduledDepartureTime();
                    holder.scheduledArrival.setText(dateFormat.format(new Date(Long.parseLong(schedArrivalTime))));
                } else {
                    holder.scheduledArrival.setText("");
                }
            }
            
            // Departure comment
            String departureComment = null;
            try {
                departureComment = item.getLocation().get(0).get(fromLocation).getDepartureComment();
            } catch (NullPointerException e) {
            }
            
            if (departureComment != null) {
                Date departureTime = new Date(Long.parseLong(item.getLocation().get(0).get(fromLocation).getDepartureTime()));
                Date scheduledDepartureTime = new Date(Long.parseLong(item.getLocation().get(0).get(fromLocation).getScheduledDepartureTime()));
                int minutesDiff = (int) (((departureTime.getTime() - scheduledDepartureTime.getTime()) / 1000) / 60);
                String scheduleType = item.getLocation().get(0).get(fromLocation).getDepartureScheduleType();
                String timelyType = "on time";
                if (minutesDiff < 0) {
                    timelyType = " early ";
                } else if (minutesDiff > 0) {
                    timelyType = " late ";
                }
                
                if (scheduleType.equalsIgnoreCase("Estimated")) {
                    if (minutesDiff == 0) {
                        holder.departureComment.setText("Estimated " + timelyType);
                    } else {
                        holder.departureComment.setText("Estimated "
                                + getHoursMinutes(Math.abs(minutesDiff))
                                + timelyType
                                + " at "
                                + dateFormat.format(departureTime));
                    }
                } else {
                    if (minutesDiff == 0) {
                        holder.departureComment.setText("Departed " + timelyType);
                    } else {
                        holder.departureComment.setText("Departed "
                                + getHoursMinutes(Math.abs(minutesDiff))
                                + timelyType + " at "
                                + dateFormat.format(departureTime));
                    } 
                }
            } else {
                holder.departureComment.setText("");
            }
            
            // Arrival comment
            schedDepartureTime = null;
            schedArrivalTime = null;
            
            try {
                schedDepartureTime = item.getLocation().get(0).get(fromLocation).getScheduledDepartureTime();
            } catch (NullPointerException e) {
            }

            try {
                schedArrivalTime = item.getLocation().get(0).get(toLocation).getScheduledArrivalTime();
            } catch (NullPointerException e) {
            }
            
            if ((fromLocation.equalsIgnoreCase(toLocation))
                    && (schedDepartureTime != null && schedArrivalTime != null)) {
                
                holder.arrivalComment.setText("");
            } else {
                if (item.getLocation().get(0).get(toLocation).getArrivalTime() != null) {
                    Date arrivalTime = new Date(Long.parseLong(item.getLocation().get(0).get(toLocation).getArrivalTime()));
                    Date scheduledArrivalTime = null;

                    if (schedArrivalTime != null) {
                        scheduledArrivalTime = new Date(Long.parseLong(schedArrivalTime));
                    } else {
                        /* Stop looking for the scheduled departure time if origin and destination
                         * stations are the same
                         */ 
                        if (!toLocation.equalsIgnoreCase(fromLocation)) {
                            /* When a station only has a scheduled departure time and not arrival,
                             * it is assumed that the train only stops for a short time at that station.
                             * In these cases, the Amtrak site appears to show this scheduled departure
                             * time as the scheduled arrival time so we'll do the same thing for the app.
                             */ 
                            scheduledArrivalTime = new Date(
                                    Long.parseLong(item
                                            .getLocation()
                                            .get(0)
                                            .get(toLocation)
                                            .getScheduledDepartureTime()));
                        } else {
                            holder.arrivalComment.setText("");
                        }
                    }
                    
                    if (scheduledArrivalTime != null) {
                        int minutesDiff = (int) (((arrivalTime.getTime() - scheduledArrivalTime.getTime()) / 1000) / 60);
                        String scheduleType = item.getLocation().get(0).get(toLocation).getArrivalScheduleType();
                        String timelyType = "on time";
                        if (minutesDiff < 0) {
                            timelyType = " early ";
                        } else if (minutesDiff > 0) {
                            timelyType = " late ";
                        }

                        if (scheduleType.equalsIgnoreCase("Estimated")) {
                            if (minutesDiff == 0) {
                                holder.arrivalComment.setText("Estimated " + timelyType);
                            } else {
                                holder.arrivalComment.setText("Estimated "
                                        + getHoursMinutes(Math.abs(minutesDiff))
                                        + timelyType
                                        + " at "
                                        + dateFormat.format(arrivalTime));
                            }
                        } else {
                            if (minutesDiff == 0) {
                                holder.arrivalComment.setText("Arrived " + timelyType);
                            } else {
                                holder.arrivalComment.setText("Arrived "
                                        + getHoursMinutes(Math.abs(minutesDiff))
                                        + timelyType
                                        + " at "
                                        + dateFormat.format(arrivalTime));
                            }
                        }
                    }
                } else {
                    holder.arrivalComment.setText("");
                }
            }
            
            // Train name and message
            String trainMessage = null;
            try {
                trainMessage = item.getLocation().get(0).get(fromLocation).getTrainMessage();
            } catch (NullPointerException e) {
            }
            
            if (trainMessage == null) {
                holder.trainName.setText(item.getLocation().get(0).get(fromLocation).getTrainName());
            } else {
                holder.trainName.setText(item.getLocation()
                        .get(0).get(fromLocation).getTrainName()
                        + " - "
                        + item.getLocation().get(0)
                                .get(fromLocation)
                                .getTrainMessage());
            }
            
            // Updated Time
            String updatedTime = null;
            try {
                updatedTime = updateDateFormat
                        .format(new Date(Long.parseLong(item.getLocation().get(0)
                                .get(fromLocation).getUpdateTime())));
            } catch (NullPointerException e) {
            }

            if (updatedTime != null) {
                holder.lastUpdated.setText(ParserUtils.relativeTime(updatedTime,
                        "MMMM d, yyyy h:mm a", false));
            } else {
                holder.lastUpdated.setText("");
            }
            
            return convertView;
        }
        
        private class ViewHolder {
            TextView scheduledDeparture;
            TextView scheduledArrival;
            TextView departureComment;
            TextView arrivalComment;
            TextView trainName;
            TextView lastUpdated;
        }
    }
    
    /**
    *
    * @param minutesDiff
    * @return
    */
   private String getHoursMinutes(int minutesDiff) {
       int hours = (int) Math.floor(minutesDiff / 60);
       int minutes = (minutesDiff % 60);

       if (hours == 0) {
           return minutes
                   + ParserUtils.pluralize(minutes,
                           " minute ", " minutes ");
       } else {
           if (minutes == 0) {
               return hours
                       + ParserUtils.pluralize(minutes,
                               " hour ", " hours ");
           } else {
               return hours
                       + ParserUtils.pluralize(hours,
                               " hour ", " hours ")
                       + minutes
                       + ParserUtils.pluralize(minutes,
                               " minute ", " minutes ");
           }
       }
   }

}
