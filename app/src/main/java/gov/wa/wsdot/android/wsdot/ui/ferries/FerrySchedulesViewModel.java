package gov.wa.wsdot.android.wsdot.ui.ferries;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.util.AbsentLiveData;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerrySchedulesViewModel extends ViewModel {

    private String TAG = FerrySchedulesViewModel.class.getSimpleName();

    private LiveData<List<FerryScheduleEntity>> schedules;
    private LiveData<FerryScheduleEntity> schedule;

    private LiveData<List<FerriesScheduleDateItem>> datesWithSailings;
    private MutableLiveData<ResourceStatus> mStatus;

    private AppExecutors appExecutors;
    private FerryScheduleRepository ferryScheduleRepo;

    DateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");

    @Inject
    FerrySchedulesViewModel(FerryScheduleRepository ferryScheduleRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.datesWithSailings = new MutableLiveData<>();
        this.ferryScheduleRepo = ferryScheduleRepo;
        this.appExecutors = appExecutors;
    }

    // if an id was passed, just load details for that schedule, other wise load all schedules TODO: should this be two view models?
    public void init(@Nullable Integer routeId){
        if (routeId != null){
            this.schedule = ferryScheduleRepo.loadFerryScheduleFor(routeId, mStatus);
            if (schedule != null){
                this.datesWithSailings = Transformations.map(this.schedule, scheduleValue -> {
                    if ( scheduleValue != null ) {
                        return processDates(scheduleValue.getDate());
                    }
                    return null;
                });
            } else {
                this.datesWithSailings = null;
            }
        } else {
            this.schedules = ferryScheduleRepo.loadFerrySchedules(mStatus);
        }
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<List<FerryScheduleEntity>> getFerrySchedules(){
        if (this.schedules == null){
            return AbsentLiveData.create();
        }
        return this.schedules;
    }

    public LiveData<FerryScheduleEntity> getFerrySchedule(){
        if (this.schedule == null){
            return AbsentLiveData.create();
        }
        return this.schedule;
    }

    public LiveData<List<FerriesScheduleDateItem>> getDatesWithSailings() {
        if (this.datesWithSailings == null){
            return AbsentLiveData.create();
        }
        return this.datesWithSailings;
    }

    public void setIsStarredFor(Integer routeId, Integer isStarred){
        ferryScheduleRepo.setIsStarred(routeId, isStarred);
    }

    public void forceRefreshFerrySchedules() {
        ferryScheduleRepo.refreshData(mStatus, true);
    }

    // post values in datesJson to satesWithSailings LiveData.
    private List<FerriesScheduleDateItem> processDates(String datesJson){

        ArrayList<FerriesScheduleDateItem> dateItems = new ArrayList<>();
        FerriesScheduleDateItem scheduleDate;
        FerriesTerminalItem terminal;
        FerriesAnnotationsItem notes;
        FerriesScheduleTimesItem timesItem;
        FerriesAnnotationIndexesItem indexesItem;

        try {

            JSONArray dates = new JSONArray(datesJson);
            int numDates = dates.length();
            for (int j = 0; j < numDates; j++) {
                JSONObject date = dates.getJSONObject(j);

                scheduleDate = new FerriesScheduleDateItem();
                scheduleDate.setDate(getDateFromString(date.getString("Date"), shortDateFormat));

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

                        timesItem = new FerriesScheduleTimesItem();

                        timesItem.setDepartingTime(getDateFromString(time.getString("DepartingTime"), dateFormat));
                        timesItem.setArrivingTime(getDateFromString(time.getString("ArrivingTime"), dateFormat));

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
        return dateItems;
    }

    /**
     *
     * @param date
     * @param mDateFormat
     * @return
     */
    private Date getDateFromString(String date, DateFormat mDateFormat) {

        Date day = null;

        try {
            day = new Date(Long.parseLong(date.substring(6, 19)));
        } catch (NumberFormatException en) {
            // Log.e(TAG, "Failed to read date as timestamp");
        } catch (StringIndexOutOfBoundsException se) {
            // Log.e(TAG, "Failed to read date as timestamp");
        }

        try {
            day = mDateFormat.parse(date);
        } catch (ParseException e) {
            //Log.e(TAG, "Failed to read date as yyyy-MM-dd");
        }

        return day;
    }
}
