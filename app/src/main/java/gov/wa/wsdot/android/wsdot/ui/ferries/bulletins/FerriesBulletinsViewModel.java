package gov.wa.wsdot.android.wsdot.ui.ferries.bulletins;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteAlertItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerriesBulletinsViewModel extends ViewModel {

    private String TAG = FerriesBulletinsViewModel.class.getSimpleName();

    private LiveData<List<FerriesRouteAlertItem>> alerts;
    private LiveData<FerriesRouteAlertItem> alert;

    private MutableLiveData<ResourceStatus> mStatus;

    private FerryScheduleRepository scheduleRepo;

    @Inject
    FerriesBulletinsViewModel(FerryScheduleRepository scheduleRepo) {
        this.mStatus = new MutableLiveData<>();
        this.scheduleRepo = scheduleRepo;
    }

    public void init(Integer routeId, @Nullable Integer alertId) {

        if (alertId != null){
            alert = Transformations.map(this.scheduleRepo.loadFerryScheduleFor(routeId, mStatus), schedule -> {

                FerriesRouteAlertItem alertItem = new FerriesRouteAlertItem();

                if (schedule != null) {
                    ArrayList<FerriesRouteAlertItem> alertItems = processAlerts(schedule.getAlert());
                    for (FerriesRouteAlertItem alertValue: alertItems){
                        if (alertValue.getBulletinID().equals(alertId)){
                            alertItem = alertValue;
                        }
                    }
                }
                return alertItem;
            });
        } else {
            alerts = Transformations.map(this.scheduleRepo.loadFerryScheduleFor(routeId, mStatus), schedule -> {
                ArrayList<FerriesRouteAlertItem> alertItems = new ArrayList<>();
                if (schedule != null) {
                    alertItems = processAlerts(schedule.getAlert());
                }
                return alertItems;
            });
        }

    }

    public void refresh(){
        scheduleRepo.refreshData(mStatus, false);
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }
    public LiveData<List<FerriesRouteAlertItem>> getAlerts(){
        return this.alerts;
    }
    public LiveData<FerriesRouteAlertItem> getAlert() { return this.alert; }

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