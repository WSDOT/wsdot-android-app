package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroup;
import gov.wa.wsdot.android.wsdot.repository.TollRatesRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class I405TollRatesViewModel extends ViewModel {

    private static String TAG = I405TollRatesViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private TollRatesRepository tollRepo;

    @Inject
    I405TollRatesViewModel(TollRatesRepository tollRepo) {
        this.mStatus = new MutableLiveData<>();
        this.tollRepo = tollRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<List<TollRateGroup>> getTollRateItems(){
        return tollRepo.loadI405TollRateGroups(mStatus);
    }

    public void setIsStarredFor(String title, Integer isStarred){
        tollRepo.setIsStarred(title, isStarred);
    }

    public void refresh() {
        tollRepo.refreshData(this.mStatus, true);
    }
}
