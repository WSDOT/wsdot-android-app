package gov.wa.wsdot.android.wsdot.ui.alert.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class HighwayAlertDetailsViewModel extends ViewModel {

    private HighwayAlertRepository highwayAlertRepo;
    private MutableLiveData<ResourceStatus> mStatus;

    @Inject
    HighwayAlertDetailsViewModel(HighwayAlertRepository highwayAlertRepo){
        this.highwayAlertRepo = highwayAlertRepo;
        this.mStatus = new MutableLiveData<>();
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<HighwayAlertEntity> getHighwayAlertFor(Integer alertId, Boolean forceRefresh){
        return highwayAlertRepo.getHighwayAlert(alertId, mStatus, forceRefresh);
    }

}
