package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.VisibleForTesting;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteDao;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;

@Singleton
public class MyRoutesRepository {

    private static String TAG = MyRoutesRepository.class.getSimpleName();

    private final MyRouteDao myRouteDao;
    private final AppExecutors appExecutors;

    @Inject
    @VisibleForTesting
    public MyRoutesRepository(MyRouteDao myRouteDao, AppExecutors appExecutors) {
        this.myRouteDao = myRouteDao;
        this.appExecutors = appExecutors;
    }

    public LiveData<List<MyRouteEntity>> getMyRoutes() {
        return myRouteDao.loadMyRoutes();
    }

    public LiveData<List<MyRouteEntity>> getFavoriteMyRoutes() {
        return myRouteDao.loadFavoriteMyRoutes();
    }

    public void deleteMyRoute(Integer id){
        appExecutors.diskIO().execute(() -> myRouteDao.deleteCacheTimeFor(id));
    }

    public void addMyRoute(MyRouteEntity myRoute){
        appExecutors.diskIO().execute(() -> myRouteDao.insertMyRoute(myRoute));
    }

    public void setIsStarred(Integer id, Integer isStarred) {
        appExecutors.diskIO().execute(() -> myRouteDao.updateIsStarred(id, isStarred));
    }
}
