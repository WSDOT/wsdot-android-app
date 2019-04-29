package gov.wa.wsdot.android.wsdot.ui.tollrates;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.tollrates.constant.TollRateTable;
import gov.wa.wsdot.android.wsdot.repository.TollRatesRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class TollRatesViewModel extends ViewModel {

    private static String TAG = TollRatesViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;
    private TollRatesRepository tollRepo;

    @Inject
    TollRatesViewModel(TollRatesRepository tollRepo) {
        this.mStatus = new MutableLiveData<>();
        this.tollRepo = tollRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<TollRateTable> getTollRatesFor(int route) {
        return this.tollRepo.loadTollRatesFor(route, mStatus);
    }

    public void refresh(Boolean force) {
        tollRepo.refreshData(this.mStatus, force);
    }

}