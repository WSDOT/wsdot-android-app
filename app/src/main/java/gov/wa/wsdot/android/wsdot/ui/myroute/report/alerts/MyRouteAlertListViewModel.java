package gov.wa.wsdot.android.wsdot.ui.myroute.report.alerts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;
import gov.wa.wsdot.android.wsdot.repository.MyRoutesRepository;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.Utils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MyRouteAlertListViewModel extends ViewModel {

    final String TAG = "test";

    private MediatorLiveData<List<HighwayAlertsItem>> alertsOnMap;
    private LiveData<List<HighwayAlertsItem>> highwayAlerts;

    private MutableLiveData<ResourceStatus> mStatus;
    private HighwayAlertRepository highwayAlertRepo;

    private MyRoutesRepository myRoutesRepo;

    private final Double MAX_ALERT_DISTANCE = 0.248548;

    @Inject
    MyRouteAlertListViewModel(HighwayAlertRepository highwayAlertRepo, MyRoutesRepository myRoutesRepo) {
        this.highwayAlertRepo = highwayAlertRepo;
        this.myRoutesRepo = myRoutesRepo;
        this.mStatus = new MutableLiveData<>();
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<HighwayAlertsItem>> getAlertsOnMap(long routeId) {
        if (alertsOnMap == null){
            this.alertsOnMap = new MediatorLiveData<>();
            alertsOnMap.addSource(
                    myRoutesRepo.loadMyRoute(routeId),
                    myRoute -> {
                        if (myRoute != null){
                            alertsOnMap.removeSource(highwayAlerts);
                            alertsOnMap.addSource(getHighwayAlertsInBounds(myRoute.getRouteLocations()), alerts -> {
                                if (alerts != null) {
                                    alertsOnMap.postValue(alerts);
                                }
                            });
                        }
                    }
            );
        }
        return this.alertsOnMap;
    }


    LiveData<List<HighwayAlertsItem>> getHighwayAlertsInBounds(String routeString){

            this.highwayAlerts = Transformations.map(highwayAlertRepo.getHighwayAlerts(mStatus), alerts -> {

                JSONArray routeJSON = new JSONArray();

                try {
                    routeJSON = new JSONArray(routeString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayList<HighwayAlertsItem> alertsOnRoute = new ArrayList<>();

                for (HighwayAlertEntity alert : alerts) {
                    try {
                        for (int i = 0; i < routeJSON.length(); i++) {
                            JSONObject locationJSON = routeJSON.getJSONObject(i);

                            if (Utils.getDistanceFromPoints(alert.getStartLatitude(), alert.getStartLongitude(), locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE ||
                                    Utils.getDistanceFromPoints(alert.getEndLatitude(), alert.getEndLongitude(), locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE) {

                                HighwayAlertsItem alertItem = new HighwayAlertsItem();
                                alertItem.setAlertId(String.valueOf(alert.getAlertId()));
                                alertItem.setEventCategory(alert.getCategory());
                                alertItem.setHeadlineDescription(alert.getHeadline());
                                alertItem.setStartLatitude(alert.getStartLatitude());
                                alertItem.setStartLongitude(alert.getStartLongitude());
                                alertItem.setEndLatitude(alert.getEndLatitude());
                                alertItem.setEndLongitude(alert.getStartLongitude());
                                alertItem.setPriority(alert.getPriority());
                                alertItem.setLastUpdatedTime(alert.getLastUpdated());
                                alertItem.setCategoryIcon(
                                        UIUtils.getCategoryIcon(
                                                alert.getCategory(),
                                                alert.getPriority()
                                        ));

                                alertsOnRoute.add(alertItem);
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return alertsOnRoute;
            });

        return this.highwayAlerts;
    }

    public void forceRefreshHighwayAlerts(){
        highwayAlertRepo.refreshData(mStatus, true);
    }

}
