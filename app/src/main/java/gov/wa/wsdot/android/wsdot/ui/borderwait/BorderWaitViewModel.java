package gov.wa.wsdot.android.wsdot.ui.borderwait;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.repository.BorderWaitRepository;

public class BorderWaitViewModel extends ViewModel {

    private LiveData<List<BorderWaitEntity>> borderWaits;

    private BorderWaitRepository borderWaitRepo;

    enum BorderDirection {
        NORTHBOUND,
        SOUTHBOUND
    }

    @Inject // BorderWaitRepository parameter is provided by Dagger 2
    BorderWaitViewModel(BorderWaitRepository borderWaitRepo) {
        this.borderWaits = new MutableLiveData<>();
        this.borderWaitRepo = borderWaitRepo;
    }

    public void init(BorderDirection direction){
        switch(direction){
            case NORTHBOUND:
                this.borderWaits = borderWaitRepo.getBorderWaitsFor("northbound");
                break;
            case SOUTHBOUND:
                this.borderWaits = borderWaitRepo.getBorderWaitsFor("southbound");
        }
    }

    public LiveData<List<BorderWaitEntity>> getBorderWaits(){
        return this.borderWaits;
    }

    public void forceRefreshBorderWaits() {
        borderWaitRepo.refreshBorderWaits();
    }
}
