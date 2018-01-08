package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationDao;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationEntity;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;

@Singleton
public class MapLocationRepository {

    private final MapLocationDao mapLocationDao;
    private final AppExecutors appExecutors;

    @Inject
    public MapLocationRepository(MapLocationDao mapLocationDao, AppExecutors appExecutors){
        this.mapLocationDao = mapLocationDao;
        this.appExecutors = appExecutors;
    }

    public LiveData<List<MapLocationEntity>> loadMapLocations(){
        return mapLocationDao.loadMapLocations();
    }

    public void addMapLocation(MapLocationEntity mapLocation){
        appExecutors.diskIO().execute(() -> {
            this.mapLocationDao.insertMapLocation(mapLocation);
        });
    }

    public void editMapLocationTitle(Integer id, String newTitle){
        appExecutors.diskIO().execute(() -> {
            this.mapLocationDao.updateTitle(id, newTitle);
        });
    }

    public void deleteMapLocation(Integer id){
        appExecutors.diskIO().execute(() -> {
            this.mapLocationDao.deleteMapLocation(id);
        });
    }

}
