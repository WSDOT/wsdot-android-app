package gov.wa.wsdot.android.wsdot.viewmodal.borderwait;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitRepository;

public class BorderWaitViewModel extends ViewModel {

    private LiveData<List<BorderWaitEntity>> borderWaits;
    private BorderWaitRepository borderWaitRepo;
    private String direction;

    @Inject // BorderWaitRepository parameter is provided by Dagger 2
    BorderWaitViewModel(BorderWaitRepository borderWaitRepo) {
        this.borderWaits = new MutableLiveData<>();
        this.borderWaitRepo = borderWaitRepo;
    }

    public void init(String direction) {
        this.direction = direction;
        this.borderWaits = borderWaitRepo.getBorderWaitsFor(direction);
    }

    public LiveData<List<BorderWaitEntity>> getBorderWaits(Boolean refresh){
        if (refresh){
            this.borderWaits = borderWaitRepo.getBorderWaitsFor(this.direction);
        }
        return this.borderWaits;
    }
}
