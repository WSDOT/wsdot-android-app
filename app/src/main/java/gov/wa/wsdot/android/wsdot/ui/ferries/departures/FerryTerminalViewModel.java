package gov.wa.wsdot.android.wsdot.ui.ferries.departures;

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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryTerminalSailingSpacesEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryTerminalSpaceRepository;
import gov.wa.wsdot.android.wsdot.repository.VesselWatchRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
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
    private boolean scrollToCurrent = true;

    private FerryTerminalSpaceRepository terminalSpaceRepo;
    private VesselWatchRepository vesselWatchRepo;

    @Inject
    FerryTerminalViewModel(FerryTerminalSpaceRepository terminalSpaceRepo, VesselWatchRepository vesselWatchRepo) {
        this.mStatus = new MutableLiveData<>();
        this.terminalSpaceRepo = terminalSpaceRepo;
        this.vesselWatchRepo = vesselWatchRepo;
        this.departureTimes = new MediatorLiveData<>();
        this.departureTimesAnnotations = new MutableLiveData<>();
    }

    Boolean getShouldScrollToCurrent() {
        return this.scrollToCurrent;
    }

    void setScrollToCurrent(Boolean shouldScroll) {
        this.scrollToCurrent = shouldScroll;
    }

    public LiveData<ResourceStatus> getResourceStatus() {
        return this.mStatus;
    }

    public MediatorLiveData<List<FerriesScheduleTimesItem>> getDepartureTimes() {
        return this.departureTimes;
    }

    LiveData<List<FerriesAnnotationsItem>> getDepartureTimesAnnotations() {
        return this.departureTimesAnnotations;
    }

    void forceRefreshTerminalSpacesAndVessel() {
        terminalSpaceRepo.refreshData(mStatus, true);
        vesselWatchRepo.refreshData(mStatus);
    }

    void forceRefreshVesselStatus() {
        vesselWatchRepo.refreshData(mStatus);
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

        // since getVessels() returns the same LiveData each time
        // remove it as a source to prevent doubling up each time loadDepartures is called.
        // loadDepartures should only be called again when switching sailings
        departureTimes.removeSource(vesselWatchRepo.getVessels());

        departureTimes.addSource(
                vesselWatchRepo.getVessels(),
                vessels -> {
                    if (vessels != null) {
                        addActualDepartureAndETA(terminalItem, vessels);
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
     * @param terminalItem        Needed for the terminal IDs
     * @param terminalSpacesValue Sailing spaces count data
     */
    private void addSailingSpaces(FerriesTerminalItem terminalItem, FerryTerminalSailingSpacesEntity terminalSpacesValue) {

        List<FerriesScheduleTimesItem> times = departureTimes.getValue();

        String departingSpacesString = terminalSpacesValue.getDepartingSpaces();
        String lastUpdated = terminalSpacesValue.getLastUpdated();

        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        try {
            JSONArray departingSpaces = new JSONArray(departingSpacesString);
            for (int i = 0; i < departingSpaces.length(); i++) {
                JSONObject spaces = departingSpaces.getJSONObject(i);
                String departure = dateFormat.format(new Date(Long.parseLong(spaces.getString("Departure").substring(6, 19))));
                JSONArray spaceForArrivalTerminals = spaces.getJSONArray("SpaceForArrivalTerminals");
                for (int j = 0; j < spaceForArrivalTerminals.length(); j++) {

                    JSONObject terminals = spaceForArrivalTerminals.getJSONObject(j);

                    JSONArray arrivalTerminalIDs = terminals.getJSONArray("ArrivalTerminalIDs");

                    // Check terminalID field
                    if (terminals.getInt("TerminalID") == terminalItem.getDepartingTerminalID()) {

                        int driveUpSpaceCount = terminals.getInt("DriveUpSpaceCount");
                        int maxSpaceCount = terminals.getInt("MaxSpaceCount");

                        for (FerriesScheduleTimesItem time : times) {
                            if (dateFormat.format(time.getDepartingTime()).equals(departure)
                                    && (new Date().before(time.getDepartingTime()))) {
                                time.setDriveUpSpaceCount(driveUpSpaceCount);
                                time.setMaxSpaceCount(maxSpaceCount);
                                time.setLastUpdated(lastUpdated);
                            }
                        }
                    }

                    // Check terminals in ArrivalTerminalIDs array
                    for (int k = 0; k < arrivalTerminalIDs.length(); k++) {
                        if (arrivalTerminalIDs.getInt(k) != terminalItem.getArrivingTerminalID()) {
                            continue;
                        } else {
                            int driveUpSpaceCount = terminals.getInt("DriveUpSpaceCount");
                            int maxSpaceCount = terminals.getInt("MaxSpaceCount");

                            for (FerriesScheduleTimesItem time : times) {

                                if (time.getDepartingTime() != null) {
                                    if (dateFormat.format(time.getDepartingTime()).equals(departure)
                                            && new Date().before(time.getDepartingTime())) {
                                        time.setDriveUpSpaceCount(driveUpSpaceCount);
                                        time.setMaxSpaceCount(maxSpaceCount);
                                        time.setLastUpdated(lastUpdated);
                                    }
                                }
                            }
                            k = arrivalTerminalIDs.length();
                        }
                    }
                }
            }
            departureTimes.postValue(times);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * inserts sailing spaces data into departureTimes. Posts update to departureTimes
     *
     * @param terminalItem Needed for the terminal IDs
     * @param vessels      list of vessel data
     */
    private void addActualDepartureAndETA(FerriesTerminalItem terminalItem, List<VesselWatchItem> vessels) {

        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        List<FerriesScheduleTimesItem> times = departureTimes.getValue();

        // Search for a vessel on this sailing
        for (VesselWatchItem vessel: vessels) {

            if (vessel.getDepartedTerminalId().equals(terminalItem.getDepartingTerminalID())
                && vessel.getArrivingTerminalId().equals(terminalItem.getArrivingTerminalID())){

                // add the vessels actual departure time and ETA to the schedule if available.
                for (FerriesScheduleTimesItem time: times ) {

                    if (time.getDepartingTime() != null) {
                        if (dateFormat.format(time.getDepartingTime()).equals(vessel.getScheduledDeparture())) {
                            time.setActualDeparture(vessel.getLeftDock());
                            time.setEta(vessel.getEta());
                        }
                    }
                }
            }
        }

        departureTimes.postValue(times);

    }
}
