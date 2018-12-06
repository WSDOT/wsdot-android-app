package gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class HighwayAlertListViewModel extends ViewModel {

    private LiveData<List<HighwayAlertsItem>> highwayAlerts;
    private MutableLiveData<ResourceStatus> mStatus;
    private HighwayAlertRepository highwayAlertRepo;

    @Inject
    HighwayAlertListViewModel(HighwayAlertRepository highwayAlertRepo) {
        this.highwayAlertRepo = highwayAlertRepo;
        this.mStatus = new MutableLiveData<>();
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<List<HighwayAlertsItem>> getHighwayAlertsInBounds(LatLngBounds bounds){
        if (highwayAlerts == null) {
            this.highwayAlerts = Transformations.map(highwayAlertRepo.getHighwayAlerts(mStatus), alerts -> {

                ArrayList<HighwayAlertsItem> alertsInArea = new ArrayList<>();

                for (HighwayAlertEntity alert : alerts) {
                    LatLng alertStartLocation = new LatLng(alert.getStartLatitude(), alert.getStartLongitude());

                    // If alert is within bounds of shown on screen show it on list
                    if (bounds.contains(alertStartLocation) ||
                            alert.getCategory().toLowerCase().equals("amber")) {

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

                        alertsInArea.add(alertItem);
                    }
                }
                return alertsInArea;
            });
        }
        return this.highwayAlerts;
    }

    public void forceRefreshHighwayAlerts(){
        highwayAlertRepo.refreshData(mStatus, true);
    }
}

