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
    private MediatorLiveData<FerriesRouteAlertItem> alert;

    private MutableLiveData<ResourceStatus> mStatus;

    private AppExecutors appExecutors;

    private FerryScheduleRepository scheduleRepo;

    @Inject
    FerriesBulletinsViewModel(FerryScheduleRepository scheduleRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.scheduleRepo = scheduleRepo;
        this.appExecutors = appExecutors;
        this.alerts = new MediatorLiveData<>();
        this.alert = new MediatorLiveData<>();
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<FerriesRouteAlertItem>> getAlerts(){
        return this.alerts;
    }

    public MediatorLiveData<FerriesRouteAlertItem> getAlert() { return this.alert; }

    public void loadAlertsForRoute(Integer routeId){
        appExecutors.taskIO().execute(() -> {
            alerts.addSource(scheduleRepo.getFerryScheduleFor(routeId, mStatus), schedule -> {
                if (schedule != null) {
                    alerts.postValue(processAlerts(schedule.getAlert()));
                }
                scheduleRepo.refreshData(mStatus, false);
            });
        });
    }

    public void loadAlert(Integer routeId, Integer alertId){
        appExecutors.taskIO().execute(() -> {
            alert.addSource(scheduleRepo.getFerryScheduleFor(routeId, mStatus), schedule -> {
                if (schedule != null) {
                    ArrayList<FerriesRouteAlertItem> alertValues = processAlerts(schedule.getAlert());

                    for (FerriesRouteAlertItem alertValue: alertValues){
                        if (alertValue.getBulletinID().equals(alertId)){
                            alert.postValue(alertValue);
                        }
                    }
                }
                scheduleRepo.refreshData(mStatus, false);
            });
        });

    }

    private ArrayList<FerriesRouteAlertItem> processAlerts(String alertsJson){
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
                i.setBulletinID(alert.getInt("BulletinID"));
                routeAlertItems.add(i);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return routeAlertItems;
    }
}