package gov.wa.wsdot.android.wsdot.ui.myroute;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.repository.MyRoutesRepository;

public class MyRouteViewModel extends ViewModel {

    private MyRoutesRepository myRoutesRepo;

    private MutableLiveData<Boolean> foundFavorites;
    private LiveData<List<MyRouteEntity>> myRoutes;

    @Inject
    MyRouteViewModel(MyRoutesRepository myRoutesRepo) {
        this.myRoutesRepo = myRoutesRepo;
        foundFavorites = new MutableLiveData<>();
    }

    LiveData<List<MyRouteEntity>> loadMyRoutes() {
        if (myRoutes == null){
            this.myRoutes = myRoutesRepo.loadMyRoutes();
        }
        return this.myRoutes;
    }

    LiveData<MyRouteEntity> loadMyRoute(long routeId){
        return myRoutesRepo.loadMyRoute(routeId);
    }

    MyRouteEntity getMyRoute(long routeId) {
        return myRoutesRepo.getMyRoute(routeId);
    }

    LiveData<Boolean> getFoundFavorites() {
        return this.foundFavorites;
    }

    void updateRouteTitle(long routeId, String newTitle){
        this.myRoutesRepo.updateTitle(routeId, newTitle);
    }

    void setIsStarred(long routeId, Integer isStarred){
        this.myRoutesRepo.setIsStarred(routeId, isStarred);
    }

    void findFavoritesOnRoute(Long myRouteId) {
        myRoutesRepo.findFavoritesOnRoute(foundFavorites, myRouteId);
    }

    void resetFindFavorites(){
        foundFavorites.setValue(false);
    }

    void deleteRoute(long routeId){
        this.myRoutesRepo.deleteMyRoute(routeId);
    }
}

