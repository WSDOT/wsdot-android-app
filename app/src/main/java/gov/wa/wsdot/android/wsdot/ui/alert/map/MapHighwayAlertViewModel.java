package gov.wa.wsdot.android.wsdot.ui.alert.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
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
