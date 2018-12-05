package gov.wa.wsdot.android.wsdot.shared.livedata;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.database.ferries.WeatherReportEntity;
import gov.wa.wsdot.android.wsdot.shared.WeatherItem;

/**
 *  LiveData class that transforms db entity Live data into a displayable
 *  object on a thread.
 *
 *  Used in favor of transformations.map since that method runs on main Thread.
 */
public class WeatherItemLiveData extends LiveData<List<WeatherItem>>
        implements Observer<List<WeatherReportEntity>> {

    @NonNull
    private LiveData<List<WeatherReportEntity>> sourceLiveData;

    public WeatherItemLiveData(@NonNull LiveData<List<WeatherReportEntity>> sourceLiveData) {
        this.sourceLiveData = sourceLiveData;
    }

    public void setNewSourceLiveData(LiveData<List<WeatherReportEntity>> sourceLiveData){
       this.sourceLiveData.removeObserver(this);
       this.sourceLiveData = sourceLiveData;
    }

    @Override protected void onActive()   { sourceLiveData.observeForever(this); }

    @Override protected void onInactive() { sourceLiveData.removeObserver(this); }

    @Override public void onChanged(@Nullable List<WeatherReportEntity> reports) {
        AsyncTask.execute(() -> postValue(processDates(reports)));
    }

    private List<WeatherItem> processDates(List<WeatherReportEntity> reports) {

        ArrayList<WeatherItem> weatherItems = new ArrayList<>();

        if (reports != null) {
            for (WeatherReportEntity report : reports) {

                weatherItems.add(new WeatherItem(
                        report.getSource(),
                        report.getWindSpeed(),
                        report.getWindDirection(),
                        report.getReport(),
                        report.getLatitude(),
                        report.getLongitude(),
                        report.getUpdated())
                );
            }
        }
        return weatherItems;
    }
}