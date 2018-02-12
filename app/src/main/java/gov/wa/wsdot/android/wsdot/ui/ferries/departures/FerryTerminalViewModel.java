package gov.wa.wsdot.android.wsdot.ui.ferries.departures;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.ferries.FerryTerminalSailingSpacesEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryTerminalSpaceRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

/**
 * Handles setting up display of departure times and sailing spaces.
 *
 * Buts departure times in a MediatorLiveData object so it can observe
 * changes to the sailing spaces LiveData.
 */
public class FerryTerminalViewModel extends ViewModel {

    private String TAG = FerryTerminalViewModel.class.getSimpleName();

    private MediatorLiveData<List<FerriesScheduleTimesItem>> departureTimes;
    private MutableLiveData<List<FerriesAnnotationsItem>> departureTimesAnnotations;

    private int selectedDay = 0;

    private MutableLiveData<ResourceStatus> mStatus;

    // var used to ensure we only jump to the next sailing on the first load
    private boolean firstLoad = true;

    private FerryTerminalSpaceRepository terminalSpaceRepo;

    @Inject
    FerryTerminalViewModel(FerryTerminalSpaceRepository terminalSpaceRepo) {
        this.mStatus = new MutableLiveData<>();
        this.terminalSpaceRepo = terminalSpaceRepo;
        this.departureTimes = new  MediatorLiveData<>();
        this.departureTimesAnnotations = new MutableLiveData<>();
    }

    Boolean isFirstLoad() {
        return this.firstLoad;
    }

    void firstLoadComplete() {
        this.firstLoad = false;
    }

    void setSelectedDay(int selection){
        selectedDay = selection;
    }

    int getSelectedDay() {
        return this.selectedDay;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<FerriesScheduleTimesItem>> getDepartureTimes() {
        return this.departureTimes;
    }

    LiveData<List<FerriesAnnotationsItem>> getDepartureTimesAnnotations() {
        return this.departureTimesAnnotations;
    }

    void forceRefreshTerminalSpaces() {
        terminalSpaceRepo.refreshData(mStatus, true);
    }

    /**
     * Given a terminalItem, compiles departure times into departureTimes. Adds the sailingSpaces
     * LiveData as a data source to departureTimes. When sailingSpaces are updated, addSailingSpaces
     * will be called to insert the spaces counts into departureTimes.
     *
     * @param terminalItem holds sailing times data. Extracted from a FerriesScheduleDateItem
     */
    void loadDepartureTimesForTerminal(FerriesTerminalItem terminalItem) {
        processDepartureTimes(terminalItem);

        departureTimes.addSource(
                terminalSpaceRepo.getTerminalSpacesFor(terminalItem.getDepartingTerminalID(), mStatus),
                spaces -> {
                    if (spaces != null) {
                        addSailingSpaces(terminalItem, spaces);
                    }
                }
        );
    }

    /**
     * Gets sailing times and sailing annotation messages from
     * a terminal item. Posts values to departureTimes and departureTimesAnnotations
     *
     * @param terminalItem holds sailing times data. Extracted from a FerriesScheduleDateItem
     */
    private void processDepartureTimes(FerriesTerminalItem terminalItem) {

        int numAnnotations = terminalItem.getAnnotations().size();
        int numTimes = terminalItem.getScheduleTimes().size();

        ArrayList<FerriesAnnotationsItem> annotations = new ArrayList<>();
        ArrayList<FerriesScheduleTimesItem> times = new ArrayList<>();

        try {
            for (int i = 0; i < numAnnotations; i++) {
                FerriesAnnotationsItem annotationItem = new FerriesAnnotationsItem();
                annotationItem.setAnnotation(terminalItem.getAnnotations().get(i).getAnnotation());
                annotations.add(annotationItem);
            }

            for (int i = 0; i < numTimes; i++) {
                FerriesScheduleTimesItem timesItem = new FerriesScheduleTimesItem();
                timesItem.setDepartingTime(terminalItem.getScheduleTimes().get(i).getDepartingTime());
                timesItem.setArrivingTime(terminalItem.getScheduleTimes().get(i).getArrivingTime());

                int numIndexes = terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().size();
                for (int j = 0; j < numIndexes; j++) {
                    FerriesAnnotationIndexesItem index = new FerriesAnnotationIndexesItem();
                    index.setIndex(terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().get(j).getIndex());
                    timesItem.setAnnotationIndexes(index);
                }

                times.add(timesItem);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding terminal departure times", e);
        }

        departureTimesAnnotations.setValue(annotations);
        departureTimes.setValue(times);

    }

    /**
     * inserts sailing spaces data into departureTimes. Posts update to departureTimes
     *
     * @param terminalItem Needed for the terminal IDs
     * @param terminalSpacesValue Sailing spaces count data
     */
    private void addSailingSpaces(FerriesTerminalItem terminalItem, FerryTerminalSailingSpacesEntity terminalSpacesValue){

        List<FerriesScheduleTimesItem> times = departureTimes.getValue();

        String departingSpacesString = terminalSpacesValue.getDepartingSpaces();
        String lastUpdated = terminalSpacesValue.getLastUpdated();

        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        try {
            JSONArray departingSpaces = new JSONArray(departingSpacesString);
            for (int i=0; i < departingSpaces.length(); i++) {
                JSONObject spaces = departingSpaces.getJSONObject(i);
                String departure = dateFormat.format(new Date(Long.parseLong(spaces.getString("Departure").substring(6, 19))));
                JSONArray spaceForArrivalTerminals = spaces.getJSONArray("SpaceForArrivalTerminals");
                for (int j=0; j < spaceForArrivalTerminals.length(); j++) {

                    JSONObject terminals = spaceForArrivalTerminals.getJSONObject(j);

                    JSONArray arrivalTerminalIDs = terminals.getJSONArray("ArrivalTerminalIDs");

                    // Check terminalID field
                    if (terminals.getInt("TerminalID") == terminalItem.getDepartingTerminalID()){

                        int driveUpSpaceCount = terminals.getInt("DriveUpSpaceCount");
                        int maxSpaceCount = terminals.getInt("MaxSpaceCount");

                        for (FerriesScheduleTimesItem time : times) {
                            Date departingTime = new Date(Long.parseLong(time.getDepartingTime()));
                            if (dateFormat.format(departingTime).equals(departure)
                                    && (new Date().before(departingTime))) {
                                Log.e(TAG, "found a time");
                                time.setDriveUpSpaceCount(driveUpSpaceCount);
                                time.setMaxSpaceCount(maxSpaceCount);
                                time.setLastUpdated(lastUpdated);
                            }
                        }
                    }

                    // Check terminals in ArrivalTerminalIDs array
                    for (int k=0; k < arrivalTerminalIDs.length(); k++) {
                        if (arrivalTerminalIDs.getInt(k)!= terminalItem.getArrivingTerminalID()) {
                            continue;
                        } else {
                            int driveUpSpaceCount = terminals.getInt("DriveUpSpaceCount");
                            int maxSpaceCount = terminals.getInt("MaxSpaceCount");

                            for (FerriesScheduleTimesItem time : times) {
                                Date departingTime = new Date(Long.parseLong(time.getDepartingTime()));
                                if (dateFormat.format(departingTime).equals(departure)
                                        && new Date().before(departingTime)) {
                                    time.setDriveUpSpaceCount(driveUpSpaceCount);
                                    time.setMaxSpaceCount(maxSpaceCount);
                                    time.setLastUpdated(lastUpdated);
                                }
                            }
                            k = arrivalTerminalIDs.length();
                        }
                    }
                }
            }
            Log.e(TAG, "posting new departure times with spaces");
            departureTimes.postValue(times);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
