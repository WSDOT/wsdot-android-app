package gov.wa.wsdot.android.wsdot.ui.borderwait;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.repository.BorderWaitRepository;
import gov.wa.wsdot.android.wsdot.util.AbsentLiveData;
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

    private MutableLiveData<BorderDirection> direction;

    private BorderWaitRepository borderWaitRepo;

    enum BorderDirection {
        NORTHBOUND,
        SOUTHBOUND
    }

    @Inject // BorderWaitRepository parameter is provided by Dagger 2
    BorderWaitViewModel(BorderWaitRepository borderWaitRepo) {
        this.mStatus = new MutableLiveData<>();
        this.direction = new MutableLiveData<>();
        this.borderWaitRepo = borderWaitRepo;

        this.borderWaits = Transformations.switchMap(direction, directionValue -> {
            if (directionValue != null) {
                switch (directionValue){
                    case NORTHBOUND:
                        return borderWaitRepo.getBorderWaitsFor("northbound", mStatus);
                    case SOUTHBOUND:
                        return borderWaitRepo.getBorderWaitsFor("southbound", mStatus);
                }
            }
            return AbsentLiveData.create();
        });

    }

    public void init(BorderDirection direction){
        this.direction.setValue(direction);
    }

    public LiveData<BorderDirection> getDirection() {
        return direction;
    }

    public LiveData<List<BorderWaitEntity>> getBorderWaits() {
        return borderWaits;
    }

    public MutableLiveData<ResourceStatus> getResourceStatus() {
        return this.mStatus;
    }

    public void forceRefreshBorderWaits() {
        if (direction != null) {
            borderWaitRepo.refreshData(mStatus, true);
        }
    }

    public void setIsStarredFor(Integer waitId, Integer isStarred) {
        borderWaitRepo.setIsStarred(waitId, isStarred);
    }
}
