package gov.wa.wsdot.android.wsdot.ui.myroute;


import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.repository.MyRoutesRepository;

public class MyRouteViewModel extends ViewModel {

    private MyRoutesRepository myRoutesRepo;

    private MutableLiveData<Boolean> foundCameras;
    private MutableLiveData<Boolean> foundTravelTimes;

    private LiveData<List<MyRouteEntity>> myRoutes;

    @Inject
    MyRouteViewModel(MyRoutesRepository myRoutesRepo) {
        this.myRoutesRepo = myRoutesRepo;
        foundCameras = new MutableLiveData<>();
        foundTravelTimes = new MutableLiveData<>();
    }

    public LiveData<List<MyRouteEntity>> loadMyRoutes() {
        if (myRoutes == null){
            this.myRoutes = myRoutesRepo.loadMyRoutes();
        }
        return this.myRoutes;
    }

    public LiveData<MyRouteEntity> loadMyRoute(long routeId){
        return myRoutesRepo.loadMyRoute(routeId);
    }

    void updateRouteTitle(long routeId, String newTitle){
        this.myRoutesRepo.updateTitle(routeId, newTitle);
    }

    void setIsStarred(long routeId, Integer isStarred){
        this.myRoutesRepo.setIsStarred(routeId, isStarred);
    }

    public void findCamerasOnRoute(Long myRouteId) {
        myRoutesRepo.findCamerasOnRoute(foundCameras, myRouteId);
    }

    public void findTravelTimesOnRoute(Long myRouteId) {
        myRoutesRepo.findTravelTimesOnRoute(foundTravelTimes, myRouteId);
    }

    void deleteRoute(long routeId){
        this.myRoutesRepo.deleteMyRoute(routeId);
    }

}

