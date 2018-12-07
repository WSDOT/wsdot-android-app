package gov.wa.wsdot.android.wsdot.ui.myroute.newroute;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.repository.MyRoutesRepository;

public class NewRouteViewModel extends ViewModel {

    private MyRoutesRepository myRoutesRepo;
    private MutableLiveData<Boolean> foundFavorites;

    @Inject
    NewRouteViewModel(MyRoutesRepository myRoutesRepo){
        this.myRoutesRepo = myRoutesRepo;
        foundFavorites = new MutableLiveData<>();
    }

    public void addMyRoute(MyRouteEntity myRoute){
        myRoutesRepo.addMyRoute(myRoute);
    }

    public LiveData<Boolean> getFoundFavorites(){
        return this.foundFavorites;
    }

    public void findFavoritesOnRoute(Long myRouteId) {
        myRoutesRepo.findFavoritesOnRoute(foundFavorites, myRouteId);
    }
}
