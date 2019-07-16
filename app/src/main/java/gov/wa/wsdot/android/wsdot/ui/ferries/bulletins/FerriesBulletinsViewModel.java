package gov.wa.wsdot.android.wsdot.ui.ferries.bulletins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteAlertItem;
import gov.wa.wsdot.android.wsdot.util.AbsentLiveData;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

import static gov.wa.wsdot.android.wsdot.util.Utils.getDateFromString;

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

                if (schedule != null) {
                    ArrayList<FerriesRouteAlertItem> alertItems = processAlerts(schedule.getAlert());
                    for (FerriesRouteAlertItem alertValue: alertItems){
                        if (alertValue.getBulletinID().equals(alertId)){
                            return alertValue;
                        }
                    }
                }
                return null;
            });
        } else {
            alerts = Transformations.map(this.scheduleRepo.loadFerryScheduleFor(routeId, mStatus), schedule -> {
                if (schedule != null) {
                    return processAlerts(schedule.getAlert());
                }
                return null;
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

    public LiveData<FerriesRouteAlertItem> getAlert() {
        if (this.alert == null){
            return AbsentLiveData.create();
        }
        return this.alert;
    }

    private ArrayList<FerriesRouteAlertItem> processAlerts(String alertsJson){

        DateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

        ArrayList<FerriesRouteAlertItem> routeAlertItems = new ArrayList<>();

        try {
            JSONArray alerts = new JSONArray(alertsJson);
            int numAlerts = alerts.length();
            for (int j=0; j < numAlerts; j++)	{
                JSONObject alert = alerts.getJSONObject(j);
                FerriesRouteAlertItem i = new FerriesRouteAlertItem();
                i.setAlertFullTitle(alert.getString("AlertFullTitle"));
                i.setPublishDate(getDateFromString(alert.getString("PublishDate"), shortDateFormat));
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