package gov.wa.wsdot.android.wsdot.ui.borderwait;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.repository.BorderWaitRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

/**
 *  ViewModel for Border Waits Data.
 *
 *  Has two states, set by passing the BorderDirection enum
 *  to the init function.
 *
 *  always call the {@link #init init()} method to insure there is data
 */
public class BorderWaitViewModel extends ViewModel {

    private LiveData<List<BorderWaitEntity>> borderWaits;
    private MutableLiveData<ResourceStatus> mStatus;

    private BorderWaitRepository borderWaitRepo;

    enum BorderDirection {
        NORTHBOUND,
        SOUTHBOUND
    }

    @Inject // BorderWaitRepository parameter is provided by Dagger 2
    BorderWaitViewModel(BorderWaitRepository borderWaitRepo) {
        this.borderWaits = new MutableLiveData<>();
        this.mStatus = new MutableLiveData<>();
        this.borderWaitRepo = borderWaitRepo;
    }

    public void init(BorderDirection direction){
        switch(direction){
            case NORTHBOUND:
                this.borderWaits = borderWaitRepo.getBorderWaitsFor("northbound", mStatus);
                break;
            case SOUTHBOUND:
                this.borderWaits = borderWaitRepo.getBorderWaitsFor("southbound", mStatus);
        }
    }

    public LiveData<List<BorderWaitEntity>> getBorderWaits(){
        return this.borderWaits;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public void forceRefreshBorderWaits() {
        borderWaitRepo.refreshData(mStatus, true);
    }
}
