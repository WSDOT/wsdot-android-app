package gov.wa.wsdot.android.wsdot.shared.livedata;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;

import static gov.wa.wsdot.android.wsdot.util.Utils.getDateFromString;

/**
 *  LiveData class that transforms db entity Live data into a displayable
 *  object on a thread.
 *
 *  Used in favor of transformations.map since that method runs on main Thread.
 */
public class FerriesScheduleDateItemLiveData extends LiveData<List<FerriesScheduleDateItem>>
        implements Observer<FerryScheduleEntity> {

    private String TAG = FerriesScheduleDateItemLiveData.class.getSimpleName();

    @NonNull
    private LiveData<FerryScheduleEntity> sourceLiveData;

    public FerriesScheduleDateItemLiveData(@NonNull LiveData<FerryScheduleEntity> sourceLiveData) {
        this.sourceLiveData = sourceLiveData;
    }


    @Override protected void onActive()   { sourceLiveData.observeForever(this); }
    @Override protected void onInactive() { sourceLiveData.removeObserver(this); }

    @Override public void onChanged(@Nullable FerryScheduleEntity ferryScheduleEntity) {

        AsyncTask.execute(() -> {
            if (ferryScheduleEntity != null) {
                postValue(processDates(ferryScheduleEntity.getDate()));
            } else {
                postValue(null);
            }
        });
    }

    private List<FerriesScheduleDateItem> processDates(String datesJson){

        DateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");

        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        shortDateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

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


}
