package gov.wa.wsdot.android.wsdot.shared.livedata;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

/**
 *  LiveData class that transforms db entity Live data into a displayable
 *  object on a thread.
 *
 *  Used in favor of transformations.map since that method runs on main Thread.
 */
public class HighwayAlertsItemLiveData extends LiveData<List<HighwayAlertsItem>>
        implements Observer<List<HighwayAlertEntity>> {

    final private String TAG = HighwayAlertsItemLiveData.class.getSimpleName();

    @NonNull
    private LiveData<List<HighwayAlertEntity>> sourceLiveData;

    public HighwayAlertsItemLiveData(@NonNull LiveData<List<HighwayAlertEntity>> sourceLiveData) {
        this.sourceLiveData = sourceLiveData;
    }

    @Override protected void onActive()   { sourceLiveData.observeForever(this); }
    @Override protected void onInactive() { sourceLiveData.removeObserver(this); }

    @Override public void onChanged(@Nullable List<HighwayAlertEntity> alerts) {
        AsyncTask.execute(() -> postValue(processAlerts(alerts)));
    }

    private List<HighwayAlertsItem> processAlerts(List<HighwayAlertEntity> alerts){

        ArrayList<HighwayAlertsItem> displayableAlertItemValues = new ArrayList<>();

        if (alerts != null) {
            for (HighwayAlertEntity alert : alerts) {
                displayableAlertItemValues.add(new HighwayAlertsItem(
                        String.valueOf(alert.getAlertId()),
                        alert.getStartLatitude(),
                        alert.getStartLongitude(),
                        alert.getEndLatitude(),
                        alert.getEndLongitude(),
                        alert.getCategory(),
                        alert.getHeadline(),
                        alert.getLastUpdated(),
                        alert.getPriority(),
                        UIUtils.getCategoryIcon(
                                alert.getCategory(),
                                alert.getPriority()
                        )
                ));
            }
        }
        return displayableAlertItemValues;
    }
}
