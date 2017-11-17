package gov.wa.wsdot.android.wsdot.ui.alert;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class HighwayAlertViewModel extends ViewModel {

    private LiveData<List<HighwayAlertEntity>> highwayAlerts;
    private MutableLiveData<ResourceStatus> mStatus;

    private HighwayAlertRepository highwayAlertRepo;

    public enum AlertPriority {
        HIGHEST,
        ALL
    }

    @Inject
    HighwayAlertViewModel(HighwayAlertRepository highwayAlertRepo) {
        this.highwayAlertRepo = highwayAlertRepo;
        this.highwayAlerts = new MutableLiveData<>();
        this.mStatus = new MutableLiveData<>();
    }

    public void init(AlertPriority priority){
        switch(priority){
            case HIGHEST:
                this.highwayAlerts = highwayAlertRepo.getHighwayAlertsFor("highest", mStatus);
                break;
            case ALL:
                this.highwayAlerts = highwayAlertRepo.getHighwayAlerts(mStatus);
        }
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<List<HighwayAlertEntity>> getHighwayAlerts(){
        return this.highwayAlerts;
    }

    public void forceRefreshHighwayAlerts(){
        highwayAlertRepo.refreshData(mStatus, true);
    }
}
