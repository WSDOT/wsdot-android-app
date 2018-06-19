package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.I405TollRatesRepository;
import gov.wa.wsdot.android.wsdot.shared.I405TollRateSignItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class I405TollRatesViewModel extends ViewModel {

    private static String TAG = I405TollRatesViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private I405TollRatesRepository tollRepo;

    @Inject
    I405TollRatesViewModel(I405TollRatesRepository tollRepo) {
        this.mStatus = new MutableLiveData<>();
        this.tollRepo = tollRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<I405TollRateSignItem>> getTollRateItems(){
        return tollRepo.getTollRates(mStatus);
    }

    public void refresh() {
        tollRepo.refreshData(this.mStatus);
    }
}
