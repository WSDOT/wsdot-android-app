package gov.wa.wsdot.android.wsdot.ui.ferries;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerrySchedulesViewModel extends ViewModel {

    private String TAG = FerrySchedulesViewModel.class.getSimpleName();

    private LiveData<List<FerryScheduleEntity>> schedules;
    private LiveData<FerryScheduleEntity> schedule;

    private MutableLiveData<List<FerriesScheduleDateItem>> datesWithSailings;

    private MutableLiveData<ResourceStatus> mStatus;

    private AppExecutors appExecutors;
    private FerryScheduleRepository ferryScheduleRepo;

    @Inject
    FerrySchedulesViewModel(FerryScheduleRepository ferryScheduleRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.datesWithSailings = new MutableLiveData<>();
        this.ferryScheduleRepo = ferryScheduleRepo;
        this.appExecutors = appExecutors;
    }

    public LiveData<List<FerryScheduleEntity>> getFerrySchedules(){
        if (schedules == null){
            this.schedules = ferryScheduleRepo.getFerrySchedules(mStatus);
            return this.schedules;
        } else {
            return this.schedules;
        }
    }

    public LiveData<FerryScheduleEntity> getFerryScheduleFor(Integer id){
        if (schedule == null){
            this.schedule = ferryScheduleRepo.getFerryScheduleFor(id, mStatus);
            return this.schedule;
        } else {
            return this.schedule;
        }
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public void setIsStarredFor(Integer passId, Integer isStarred){
        ferryScheduleRepo.setIsStarred(passId, isStarred);
    }

    public void forceRefreshFerrySchedules() {
        ferryScheduleRepo.refreshData(mStatus, true);
    }

    public LiveData<List<FerriesScheduleDateItem>> getDatesWithSailings() {
        return this.datesWithSailings;
    }

    public void loadDatesWithSailingsFromJson(String datesWithSailingsJson) {
        appExecutors.taskIO().execute(() -> {
            processDates(datesWithSailingsJson);
        });
    }

    private void processDates(String datesJson){

        ArrayList<FerriesScheduleDateItem> dateItems = new ArrayList<>();
        FerriesScheduleDateItem scheduleDate;
        FerriesTerminalItem terminal;
        FerriesAnnotationsItem notes;
        FerriesScheduleTimesItem timesItem;
        FerriesAnnotationIndexesItem indexesItem;
        Date now = new Date();

        try {
            JSONArray dates = new JSONArray(datesJson);
            int numDates = dates.length();
            for (int j = 0; j < numDates; j++) {
                JSONObject date = dates.getJSONObject(j);
                scheduleDate = new FerriesScheduleDateItem();
                scheduleDate.setDate(date.getString("Date").substring(6, 19));

                JSONArray sailings = date.getJSONArray("Sailings");
                int numSailings = sailings.length();
                for (int k = 0; k < numSailings; k++) {
                    JSONObject sailing = sailings.getJSONObject(k);
                    terminal = new FerriesTerminalItem();
                    terminal.setArrivingTerminalID(sailing.getInt("ArrivingTerminalID"));
                    terminal.setArrivingTerminalName(sailing.getString("ArrivingTerminalName"));
                    terminal.setDepartingTerminalID(sailing.getInt("DepartingTerminalID"));
                    terminal.setDepartingTerminalName(sailing.getString("DepartingTerminalName"));

                    JSONArray annotations = sailing.getJSONArray("Annotations");
                    int numAnnotations = annotations.length();
                    for (int l = 0; l < numAnnotations; l++) {
                        notes = new FerriesAnnotationsItem();
                        notes.setAnnotation(annotations.getString(l));
                        terminal.setAnnotations(notes);
                    }

                    JSONArray times = sailing.getJSONArray("Times");
                    int numTimes = times.length();
                    for (int m = 0; m < numTimes; m++) {
                        JSONObject time = times.getJSONObject(m);

                        // Don't display past sailing times. Doesn't make sense.
                        if (now.after(new Date(Long.parseLong(time
                                .getString("DepartingTime")
                                .substring(6, 19))))) {
                            continue;
                        }

                        timesItem = new FerriesScheduleTimesItem();
                        timesItem.setDepartingTime(time.getString("DepartingTime").substring(6, 19));

                        try {
                            timesItem.setArrivingTime(time.getString("ArrivingTime").substring(6, 19));
                        } catch (StringIndexOutOfBoundsException e) {
                            timesItem.setArrivingTime("N/A");
                        }

                        JSONArray annotationIndexes = time.getJSONArray("AnnotationIndexes");
                        int numIndexes = annotationIndexes.length();
                        for (int n = 0; n < numIndexes; n++) {
                            indexesItem = new FerriesAnnotationIndexesItem();
                            indexesItem.setIndex(annotationIndexes.getInt(n));
                            timesItem.setAnnotationIndexes(indexesItem);
                        }
                        terminal.setScheduleTimes(timesItem);
                    }
                    scheduleDate.setFerriesTerminalItem(terminal);
                }

                dateItems.add(scheduleDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding schedule date items", e);
        }
        datesWithSailings.postValue(dateItems);
    }
}
