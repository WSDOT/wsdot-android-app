package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesScheduleFeed;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesScheduleItem;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesServiceItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class AmtrakCascadesRepository extends NetworkResourceRepository {

    private final static String TAG = AmtrakCascadesRepository.class.getSimpleName();

    private MutableLiveData<List<AmtrakCascadesServiceItem>> serviceItems;

    private static Map<Integer, String> trainNumberMap = new HashMap<>();

    private String statusDate;
    private String fromLocation;
    private String toLocation;

    @Inject
    public AmtrakCascadesRepository(AppExecutors appExecutors) {
        super(appExecutors);
        serviceItems = new MutableLiveData<>();
    }

    public MutableLiveData<List<AmtrakCascadesServiceItem>> getServiceItems(
            String statusDate, String fromLocation, String toLocation) {
        serviceItems.setValue(null);
        getTrainNumbers();
        this.statusDate = statusDate;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        return this.serviceItems;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {
        if (toLocation.equals("N/A") || toLocation.equalsIgnoreCase(fromLocation)){
            fetchDepartingToAll();
        } else {
            fetchDepartingToArriving();
        }
    }

    /**
     * Get train schedules for those with a departing and arriving station.
     */
    private void fetchDepartingToArriving() throws Exception {
        List<AmtrakCascadesServiceItem> mServiceItems = new ArrayList<>();
        AmtrakCascadesScheduleItem scheduleItem;
        URL url;

        url = new URL(
                APIEndPoints.AMTRAK_SCHEDULE
                        + "?AccessCode=" + APIEndPoints.WSDOT_API_KEY
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
            Map<String, AmtrakCascadesScheduleItem> stationItems = new HashMap<>();
            List<Map<String, AmtrakCascadesScheduleItem>> locationItems = new ArrayList<>();

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
                    scheduleItem.setArrivalTime(scheduleFeed[i].getArrivalTime().substring(6, 19));
                }

                if (scheduleFeed[i].getDepartureComment() != null) {
                    scheduleItem.setDepartureComment(scheduleFeed[i].getDepartureComment());
                }

                if (scheduleFeed[i].getDepartureScheduleType() != null) {
                    scheduleItem.setDepartureScheduleType(scheduleFeed[i].getDepartureScheduleType());
                }

                if (scheduleFeed[i].getDepartureTime() != null) {
                    scheduleItem.setDepartureTime(scheduleFeed[i].getDepartureTime().substring(6, 19));
                }

                if (scheduleFeed[i].getScheduledArrivalTime() != null) {
                    scheduleItem.setScheduledArrivalTime(scheduleFeed[i].getScheduledArrivalTime().substring(6, 19));
                }

                scheduleItem.setStationName(scheduleFeed[i].getStationName());

                if (scheduleFeed[i].getTrainMessage() != "") {
                    scheduleItem.setTrainMessage(scheduleFeed[i].getTrainMessage());
                }

                if (scheduleFeed[i].getScheduledDepartureTime() != null) {
                    scheduleItem.setScheduledDepartureTime(
                            scheduleFeed[i].getScheduledDepartureTime().substring(6, 19));

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

                scheduleItem.setUpdateTime(scheduleFeed[i].getUpdateTime().substring(6, 19));

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

        serviceItems.postValue(mServiceItems);

    }

    private void fetchDepartingToAll() throws Exception {

        List<AmtrakCascadesServiceItem> mServiceItems = new ArrayList<>();
        AmtrakCascadesScheduleItem scheduleItem;
        URL url;

        url = new URL(
                APIEndPoints.AMTRAK_SCHEDULE
                        + "?AccessCode=" + APIEndPoints.WSDOT_API_KEY
                        + "&StatusDate=" + statusDate
                        + "&TrainNumber=-1"
                        + "&FromLocation=" + fromLocation
                        + "&ToLocation=N/A"
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
            Map<String, AmtrakCascadesScheduleItem> stationItems = new HashMap<>();
            List<Map<String, AmtrakCascadesScheduleItem>> locationItems = new ArrayList<>();

            scheduleItem = new AmtrakCascadesScheduleItem();

            if (scheduleFeed[i].getArrivalComment() != null) {
                scheduleItem.setArrivalComment(scheduleFeed[i].getArrivalComment());
            }

            if (scheduleFeed[i].getArrivalScheduleType() != null) {
                scheduleItem.setArrivalScheduleType(scheduleFeed[i].getArrivalScheduleType());
            }

            if (scheduleFeed[i].getArrivalTime() != null) {
                scheduleItem.setArrivalTime(scheduleFeed[i].getArrivalTime().substring(6, 19));
            }

            if (scheduleFeed[i].getDepartureComment() != null) {
                scheduleItem.setDepartureComment(scheduleFeed[i].getDepartureComment());
            }

            if (scheduleFeed[i].getDepartureScheduleType() != null) {
                scheduleItem.setDepartureScheduleType(scheduleFeed[i].getDepartureScheduleType());
            }

            if (scheduleFeed[i].getDepartureTime() != null) {
                scheduleItem.setDepartureTime(scheduleFeed[i].getDepartureTime().substring(6, 19));
            }

            scheduleItem.setStationName(scheduleFeed[i].getStationName());

            if (scheduleFeed[i].getTrainMessage() != "") {
                scheduleItem.setTrainMessage(scheduleFeed[i].getTrainMessage());
            }

            if (scheduleFeed[i].getScheduledArrivalTime() != null) {
                scheduleItem.setScheduledArrivalTime(
                        scheduleFeed[i].getScheduledArrivalTime().substring(6, 19));

                if (fromLocation.equalsIgnoreCase(scheduleItem.getStationName())) {
                    scheduledTime = new Date(
                            Long.parseLong((scheduleItem
                                    .getScheduledArrivalTime())));
                }
            }

            if (scheduleFeed[i].getScheduledDepartureTime() != null) {
                scheduleItem.setScheduledDepartureTime(
                        scheduleFeed[i].getScheduledDepartureTime().substring(6, 19));

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


            scheduleItem.setUpdateTime(scheduleFeed[i].getUpdateTime().substring(6, 19));

            stationItems.put(scheduleItem.getStationName(), scheduleItem);
            locationItems.add(stationItems);

            mServiceItems.add(new AmtrakCascadesServiceItem(scheduledTime, locationItems));
        }

        Collections.sort(mServiceItems, AmtrakCascadesServiceItem.scheduledDepartureTimeComparator);
        toLocation = fromLocation;

        serviceItems.postValue(mServiceItems);
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
        trainNumberMap.put(502, "Amtrak Cascades Train");
        trainNumberMap.put(503, "Amtrak Cascades Train");
        trainNumberMap.put(504, "Amtrak Cascades Train");
        trainNumberMap.put(505, "Amtrak Cascades Train");
        trainNumberMap.put(506, "Amtrak Cascades Train");
        trainNumberMap.put(507, "Amtrak Cascades Train");
        trainNumberMap.put(508, "Amtrak Cascades Train");
        trainNumberMap.put(509, "Amtrak Cascades Train");
        trainNumberMap.put(510, "Amtrak Cascades Train");
        trainNumberMap.put(511, "Amtrak Cascades Train");
        trainNumberMap.put(513, "Amtrak Cascades Train");
        trainNumberMap.put(514, "Amtrak Cascades Train");
        trainNumberMap.put(515, "Amtrak Cascades Train");
        trainNumberMap.put(516, "Amtrak Cascades Train");
        trainNumberMap.put(517, "Amtrak Cascades Train");
        trainNumberMap.put(518, "Amtrak Cascades Train");
        trainNumberMap.put(519, "Amtrak Cascades Train");
    }
}