package gov.wa.wsdot.android.wsdot.ui.alert.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;

public class HighwayAlertDetailsViewModel extends ViewModel {

    private HighwayAlertRepository highwayAlertRepo;

    @Inject
    HighwayAlertDetailsViewModel(HighwayAlertRepository highwayAlertRepo){
        this.highwayAlertRepo = highwayAlertRepo;
    }

    public LiveData<HighwayAlertEntity> getHighwayAlertfor(Integer alertId){
        return highwayAlertRepo.getHighwayAlert(alertId);
    }

}
