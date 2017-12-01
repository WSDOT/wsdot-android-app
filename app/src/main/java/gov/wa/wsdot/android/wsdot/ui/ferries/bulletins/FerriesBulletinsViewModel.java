package gov.wa.wsdot.android.wsdot.ui.ferries.bulletins;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteAlertItem;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerriesBulletinsViewModel extends ViewModel {

    private String TAG = FerriesBulletinsViewModel.class.getSimpleName();

    private MediatorLiveData<List<FerriesRouteAlertItem>> alerts;

    private MutableLiveData<ResourceStatus> mStatus;

    private AppExecutors appExecutors;

    private FerryScheduleRepository scheduleRepo;

    @Inject
    FerriesBulletinsViewModel(FerryScheduleRepository scheduleRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.scheduleRepo = scheduleRepo;
        this.appExecutors = appExecutors;
        this.alerts = new MediatorLiveData<>();
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<FerriesRouteAlertItem>> getAlerts(){
        return this.alerts;
    }

    public void loadAlertsForRoute(Integer routeId){
        appExecutors.taskIO().execute(() -> {
            alerts.addSource(scheduleRepo.getFerryScheduleFor(routeId, mStatus), schedule -> {
                if (schedule != null) {
                    processAlerts(schedule.getAlert());
                }
                scheduleRepo.refreshData(mStatus, false);
            });
        });
    }

    private void processAlerts(String alertsJson){
        ArrayList<FerriesRouteAlertItem> routeAlertItems = new ArrayList<>();

        try {
            JSONArray alerts = new JSONArray(alertsJson);
            int numAlerts = alerts.length();
            for (int j=0; j < numAlerts; j++)	{
                JSONObject alert = alerts.getJSONObject(j);
                FerriesRouteAlertItem i = new FerriesRouteAlertItem();
                i.setAlertFullTitle(alert.getString("AlertFullTitle"));
                i.setPublishDate(alert.getString("PublishDate").substring(6, 19));
                i.setAlertDescription(alert.getString("AlertDescription"));
                i.setAlertFullText(alert.getString("AlertFullText"));
                routeAlertItems.add(i);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.alerts.postValue(routeAlertItems);
    }
}