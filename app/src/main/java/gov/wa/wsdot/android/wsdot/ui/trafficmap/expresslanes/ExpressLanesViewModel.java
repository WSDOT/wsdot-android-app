package gov.wa.wsdot.android.wsdot.ui.trafficmap.expresslanes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.ExpressLanesRepository;
import gov.wa.wsdot.android.wsdot.shared.ExpressLaneItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class ExpressLanesViewModel extends ViewModel {

    private static String TAG = ExpressLanesViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private ExpressLanesRepository expressLanesRepo;

    @Inject
    ExpressLanesViewModel(ExpressLanesRepository expressLanesRepo) {
        this.mStatus = new MutableLiveData<>();
        this.expressLanesRepo = expressLanesRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<ExpressLaneItem>> getExpressLanesStatus(){
        return expressLanesRepo.getExpressLanes();
    }

    public void refresh() {
        expressLanesRepo.refreshData(this.mStatus);
    }

}
