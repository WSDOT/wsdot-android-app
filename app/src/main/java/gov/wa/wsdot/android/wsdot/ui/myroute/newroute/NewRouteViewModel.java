package gov.wa.wsdot.android.wsdot.ui.myroute.newroute;

import javax.inject.Inject;

import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.repository.MyRoutesRepository;

public class NewRouteViewModel extends ViewModel {

    private MyRoutesRepository myRoutesRepo;

    @Inject
    NewRouteViewModel(MyRoutesRepository myRoutesRepo){
        this.myRoutesRepo = myRoutesRepo;
    }

    public void addMyRoute(MyRouteEntity myRoute){
        myRoutesRepo.addMyRoute(myRoute);
    }

}
