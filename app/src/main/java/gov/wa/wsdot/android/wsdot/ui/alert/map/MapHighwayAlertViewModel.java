package gov.wa.wsdot.android.wsdot.ui.alert.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.shared.livedata.HighwayAlertsItemLiveData;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MapHighwayAlertViewModel extends ViewModel {

    private static String TAG = MapHighwayAlertViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private MutableLiveData<LatLngBounds> mapBounds;
    private HighwayAlertsItemLiveData displayableAlertItems;
    private MediatorLiveData<List<HighwayAlertsItem>> displayedAlertItems;

    private HighwayAlertRepository highwayAlertRepo;

    @Inject
    MapHighwayAlertViewModel(HighwayAlertRepository alertRepo) {

        this.mStatus = new MutableLiveData<>();
        this.highwayAlertRepo = alertRepo;

        this.displayableAlertItems = new HighwayAlertsItemLiveData(alertRepo.getHighwayAlerts(mStatus));

        mapBounds = new MutableLiveData<>();

        displayedAlertItems = new MediatorLiveData<>();
        displayedAlertItems.addSource(mapBounds, bounds -> {
            if (displayableAlertItems.getValue() != null) {
                displayedAlertItems.postValue(filterDisplayedAlertsFor(bounds, displayableAlertItems.getValue()));
            }
        });

        displayedAlertItems.addSource(displayableAlertItems, alertsItems -> {
            if (mapBounds.getValue() != null) {
                displayedAlertItems.postValue(filterDisplayedAlertsFor(mapBounds.getValue(), alertsItems));
            }
        });
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public void refreshAlerts(){
        highwayAlertRepo.refreshData(mStatus, true);
    }

    public LiveData<List<HighwayAlertsItem>> getDisplayAlerts() {
        return displayedAlertItems;
    }

    public void setMapBounds(LatLngBounds bounds){
        this.mapBounds.setValue(bounds);
    }

    private List<HighwayAlertsItem> filterDisplayedAlertsFor(LatLngBounds bounds, List<HighwayAlertsItem> alertItems) {

        ArrayList<HighwayAlertsItem> displayedAlertItems = new ArrayList<>();

        for (HighwayAlertsItem alert : alertItems) {
            LatLng alertLocation = new LatLng(alert.getStartLatitude(), alert.getStartLongitude());
            if (bounds.contains(alertLocation)) {
                displayedAlertItems.add(alert);
            }
        }
        return displayedAlertItems;
    }
}
