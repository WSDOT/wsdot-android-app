package gov.wa.wsdot.android.wsdot.viewmodal.borderwait;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitRepository;

public class BorderWaitViewModel extends ViewModel {

    private LiveData<List<BorderWaitEntity>> borderWaits;
    private BorderWaitRepository borderWaitRepo;

    @Inject // BorderWaitRepository parameter is provided by Dagger 2
    public BorderWaitViewModel(BorderWaitRepository borderWaitRepo) {
        this.borderWaitRepo = borderWaitRepo;
    }

    public void init(String direction) {
        this.borderWaits = borderWaitRepo.getBorderWaitsFor(direction);
    }

    public LiveData<List<BorderWaitEntity>> getBorderWaits(){
        return borderWaits;
    }

}
