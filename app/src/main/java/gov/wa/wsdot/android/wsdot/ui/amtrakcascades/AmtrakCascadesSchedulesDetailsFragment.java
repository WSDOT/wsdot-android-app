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

package gov.wa.wsdot.android.wsdot.ui.amtrakcascades;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesServiceItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class AmtrakCascadesSchedulesDetailsFragment extends BaseFragment
        implements Injectable,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = AmtrakCascadesSchedulesDetailsFragment.class.getSimpleName();

    private static Typeface tf;
    private static Typeface tfb;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private View mEmptyView;

    private static Map<String, String> amtrakStations = new HashMap<String, String>();
    private static String statusDate;
    private static String fromLocation;
    private static String toLocation;
    private static ScheduleAdapter mAdapter;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    AmtrakCascadesSchedulesDetailsViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getActivity().getIntent().getExtras();
        statusDate = args.getString("dayId");
        fromLocation = args.getString("originId");
        toLocation = args.getString("destinationId");

        getAmtrakStations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_textview_swipe_refresh, null);
        TextView schedule_title = root.findViewById(R.id.title);
        schedule_title.setTypeface(tfb);

        if ((fromLocation.equalsIgnoreCase(toLocation))) {
            schedule_title.setText(amtrakStations.get(fromLocation));
        } else {
            schedule_title.setText("Departing: "
                    + amtrakStations.get(fromLocation)
                    + " and Arriving: "
                    + amtrakStations.get(toLocation));
        }

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ScheduleAdapter(null);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

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

        mEmptyView = root.findViewById(R.id.empty_list_view);


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AmtrakCascadesSchedulesDetailsViewModel.class);

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
                        TextView t = (TextView) mEmptyView;
                        t.setText(R.string.no_connection);
                        mEmptyView.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "connection error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getSchedule(statusDate, fromLocation, toLocation).observe(this, serviceItems -> {
            if (serviceItems != null) {
                mEmptyView.setVisibility(View.GONE);
                if (!serviceItems.isEmpty()) {
                    mAdapter.setData(new ArrayList<>(serviceItems));
                } else {
                    TextView t = (TextView) mEmptyView;
                    t.setText("schedule unavailable.");
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.refresh();

        return root;
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

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private List<AmtrakCascadesServiceItem> items;

        public ScheduleAdapter(List<AmtrakCascadesServiceItem> data) {
            this.items = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView;

            if (viewType == TYPE_HEADER) {
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.list_item_amtrakschedules_times_header, parent, false);
                return new TitleViewHolder(itemView);
            }else if (viewType == TYPE_ITEM){
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.list_item_amtrakschedules_times, parent, false);
                return new AmtrakViewHolder(itemView);
            }else{
                throw new RuntimeException("There is no view type that matches the type: " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            DateFormat updateDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
            updateDateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

            TitleViewHolder titleHolder;
            AmtrakViewHolder itemHolder;

            if (holder instanceof TitleViewHolder){

                titleHolder = (TitleViewHolder) holder;

                titleHolder.Arriving.setTypeface(tfb);
                titleHolder.Departing.setTypeface(tfb);
                titleHolder.itemView.setContentDescription("departure and arrival times heading");
            }else {
                AmtrakCascadesServiceItem item = this.getItem(position);

                itemHolder = (AmtrakViewHolder) holder;
                StringBuilder contentDescriptionBuilder = new StringBuilder();

                // Departing Time
                String schedDepartureTime = null;
                try {
                    schedDepartureTime = item.getLocation().get(0).get(fromLocation).getScheduledDepartureTime();
                } catch (NullPointerException e) {
                }

                if (schedDepartureTime != null) {
                    itemHolder.scheduledDeparture.setText(dateFormat.format(new Date(Long.parseLong(schedDepartureTime))));
                    contentDescriptionBuilder.append("Departing ");
                    contentDescriptionBuilder.append(itemHolder.scheduledDeparture.getText());
                    contentDescriptionBuilder.append(". ");
                } else {
                    itemHolder.scheduledDeparture.setText("");
                    contentDescriptionBuilder.append("Departing time not available. ");
                }

                // Arriving Time
                String schedArrivalTime = null;
                try {
                    schedArrivalTime = item.getLocation().get(0).get(toLocation).getScheduledArrivalTime();
                } catch (NullPointerException e) {
                }

                if ((fromLocation.equalsIgnoreCase(toLocation))
                        && (schedDepartureTime != null && schedArrivalTime != null)) {
                    itemHolder.scheduledArrival.setText("");
                    contentDescriptionBuilder.append("Arriving time not available. ");
                } else {
                    if (schedArrivalTime != null) {
                        itemHolder.scheduledArrival.setText(dateFormat.format(new Date(Long.parseLong(schedArrivalTime))));
                    } else if (schedDepartureTime != null && !toLocation.equalsIgnoreCase(fromLocation)) {
                    /* When a station only has a scheduled departure time and not arrival,
                     * it is assumed that the train only stops for a short time at that station.
                     * In these cases, the Amtrak site appears to show this scheduled departure
                     * time as the scheduled arrival time so we'll do the same thing for the app.
                     */
                        schedArrivalTime = item.getLocation().get(0).get(toLocation).getScheduledDepartureTime();
                        itemHolder.scheduledArrival.setText(dateFormat.format(new Date(Long.parseLong(schedArrivalTime))));
                        contentDescriptionBuilder.append("Arriving ");
                        contentDescriptionBuilder.append(itemHolder.scheduledArrival.getText());
                        contentDescriptionBuilder.append(". ");
                    } else {
                        itemHolder.scheduledArrival.setText("");
                        contentDescriptionBuilder.append("Arriving time not available. ");
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
                    String timelyType = "on time ";
                    if (minutesDiff < 0) {
                        timelyType = "early ";
                    } else if (minutesDiff > 0) {
                        timelyType = "late ";
                    }

                    if (scheduleType.equalsIgnoreCase("Estimated")) {
                        if (minutesDiff == 0) {
                            itemHolder.departureComment.setText("Estimated " + timelyType);
                        } else {
                            itemHolder.departureComment.setText("Estimated "
                                    + getHoursMinutes(Math.abs(minutesDiff))
                                    + timelyType
                                    + "at "
                                    + dateFormat.format(departureTime));
                        }
                    } else {
                        if (minutesDiff == 0) {
                            itemHolder.departureComment.setText("Departed " + timelyType);
                        } else {
                            itemHolder.departureComment.setText("Departed "
                                    + getHoursMinutes(Math.abs(minutesDiff))
                                    + timelyType + "at "
                                    + dateFormat.format(departureTime));
                        }
                    }
                    contentDescriptionBuilder.append(itemHolder.departureComment.getText());
                    contentDescriptionBuilder.append(". ");
                } else {
                    itemHolder.departureComment.setText("");
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

                    itemHolder.arrivalComment.setText("");
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
                                itemHolder.arrivalComment.setText("");
                            }
                        }

                        if (scheduledArrivalTime != null) {
                            int minutesDiff = (int) (((arrivalTime.getTime() - scheduledArrivalTime.getTime()) / 1000) / 60);
                            String scheduleType = item.getLocation().get(0).get(toLocation).getArrivalScheduleType();
                            String timelyType = "on time ";
                            if (minutesDiff < 0) {
                                timelyType = "early ";
                            } else if (minutesDiff > 0) {
                                timelyType = "late ";
                            }

                            if (scheduleType.equalsIgnoreCase("Estimated")) {
                                if (minutesDiff == 0) {
                                    itemHolder.arrivalComment.setText("Estimated " + timelyType);
                                } else {
                                    itemHolder.arrivalComment.setText("Estimated "
                                            + getHoursMinutes(Math.abs(minutesDiff))
                                            + timelyType
                                            + "at "
                                            + dateFormat.format(arrivalTime));
                                }
                            } else {
                                if (minutesDiff == 0) {
                                    itemHolder.arrivalComment.setText("Arrived " + timelyType);
                                } else {
                                    itemHolder.arrivalComment.setText("Arrived "
                                            + getHoursMinutes(Math.abs(minutesDiff))
                                            + timelyType
                                            + "at "
                                            + dateFormat.format(arrivalTime));
                                }
                            }
                        }
                        contentDescriptionBuilder.append(itemHolder.arrivalComment.getText());
                        contentDescriptionBuilder.append(". ");
                    } else {
                        itemHolder.arrivalComment.setText("");
                    }
                }

                // Train name and message
                String trainMessage = null;
                try {
                    trainMessage = item.getLocation().get(0).get(fromLocation).getTrainMessage();
                } catch (NullPointerException e) {
                }

                if (trainMessage == null) {
                    itemHolder.trainName.setText(item.getLocation().get(0).get(fromLocation).getTrainName());
                } else {
                    itemHolder.trainName.setText(item.getLocation()
                            .get(0).get(fromLocation).getTrainName()
                            + " - "
                            + item.getLocation().get(0)
                            .get(fromLocation)
                            .getTrainMessage());
                }

                contentDescriptionBuilder.append("Via the ");
                contentDescriptionBuilder.append(itemHolder.trainName.getText());
                contentDescriptionBuilder.append(". ");

                // Updated Time
                String updatedTime = null;
                try {

                    updatedTime = updateDateFormat
                            .format(new Date(Long.parseLong(item.getLocation().get(0)
                                    .get(fromLocation).getUpdateTime())));

                } catch (NullPointerException e) {

                }

                if (updatedTime != null) {

                    itemHolder.lastUpdated.setText(ParserUtils.relativeTime(updatedTime,
                            "MMMM d, yyyy h:mm a", false));
                    contentDescriptionBuilder.append("updated ");
                    contentDescriptionBuilder.append(itemHolder.lastUpdated.getText());
                } else {
                }
                itemHolder.itemView.setContentDescription(contentDescriptionBuilder.toString());
            }
        }

        // Add plus one for the header cell.
        @Override
        public int getItemCount() {
            if (items != null) {
                return items.size() + 1;
            }
            return 0;
        }

        public void setData(ArrayList<AmtrakCascadesServiceItem> data) {
            if(data != null) {
                items = data;
            }else{
                items = null;
            }
            notifyDataSetChanged();
        }

        public void clear() {
            if (items != null) {
                this.items.clear();
            }
            notifyDataSetChanged();
        }

        // Because of the header cell the we do position - 1 to index into the data
        private AmtrakCascadesServiceItem getItem(int position){
            return items.get(position - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

    }

    public static class AmtrakViewHolder extends RecyclerView.ViewHolder {
        TextView scheduledDeparture;
        TextView scheduledArrival;
        TextView departureComment;
        TextView arrivalComment;
        TextView trainName;
        TextView lastUpdated;

        AmtrakViewHolder(View itemView) {
            super(itemView);
            scheduledDeparture = itemView.findViewById(R.id.scheduledDeparture);
            scheduledDeparture.setTypeface(tfb);
            scheduledArrival = itemView.findViewById(R.id.scheduledArrival);
            scheduledArrival.setTypeface(tfb);
            departureComment = itemView.findViewById(R.id.departureComment);
            departureComment.setTypeface(tfb);
            arrivalComment = itemView.findViewById(R.id.arrivalComment);
            arrivalComment.setTypeface(tfb);
            trainName = itemView.findViewById(R.id.trainName);
            trainName.setTypeface(tf);
            lastUpdated = itemView.findViewById(R.id.lastUpdated);
            lastUpdated.setTypeface(tf);
        }
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView Departing;
        TextView Arriving;

        TitleViewHolder(View itemView) {
            super(itemView);
            Departing = itemView.findViewById(R.id.departing_title);
            Arriving = itemView.findViewById(R.id.arriving_title);
        }
    }

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

   public void onRefresh() {
       viewModel.refresh();
   }
}
