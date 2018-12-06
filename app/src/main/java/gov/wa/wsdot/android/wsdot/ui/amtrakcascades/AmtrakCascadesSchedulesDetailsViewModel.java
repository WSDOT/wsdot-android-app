package gov.wa.wsdot.android.wsdot.ui.amtrakcascades;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.AmtrakCascadesRepository;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesServiceItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class AmtrakCascadesSchedulesDetailsViewModel extends ViewModel {

    private static String TAG = AmtrakCascadesSchedulesDetailsViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private AmtrakCascadesRepository amtrakRepo;

    @Inject
    AmtrakCascadesSchedulesDetailsViewModel(AmtrakCascadesRepository amtrakRepo) {
        this.mStatus = new MutableLiveData<>();
        this.amtrakRepo = amtrakRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<AmtrakCascadesServiceItem>> getSchedule(String statusDate, String fromLocation, String toLocation){
        return amtrakRepo.getServiceItems(statusDate, fromLocation, toLocation);
    }

    public void refresh() {
        amtrakRepo.refreshData(this.mStatus);
    }
}