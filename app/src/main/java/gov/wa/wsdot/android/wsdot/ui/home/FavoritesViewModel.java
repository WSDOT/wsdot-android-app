package gov.wa.wsdot.android.wsdot.ui.home;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.repository.MapLocationRepository;
import gov.wa.wsdot.android.wsdot.repository.MountainPassRepository;
import gov.wa.wsdot.android.wsdot.repository.MyRoutesRepository;
import gov.wa.wsdot.android.wsdot.repository.TravelTimeRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.network.Status;

public class FavoritesViewModel extends ViewModel {

    private final String TAG = FavoritesViewModel.class.getSimpleName();

    private CameraRepository cameraRepo;
    private MutableLiveData<ResourceStatus> mCameraStatus;

    private FerryScheduleRepository ferryScheduleRepo;
    private MutableLiveData<ResourceStatus> mFerryStatus;

    private TravelTimeRepository travelTimeRepo;
    private MutableLiveData<ResourceStatus> mTravelTimeStatus;

    private MountainPassRepository mountainPassRepo;
    private MutableLiveData<ResourceStatus> mPassStatus;

    private MyRoutesRepository myRoutesRepo;

    private MapLocationRepository mapLocationRepo;

    private MediatorLiveData<Integer> mFavoritesLoadingTasks;

    @Inject
    FavoritesViewModel(CameraRepository cameraRepo,
                       FerryScheduleRepository ferryScheduleRepo,
                       TravelTimeRepository travelTimeRepo,
                       MountainPassRepository mountainPassRepo,
                       MyRoutesRepository myRoutesRepo,
                       MapLocationRepository mapLocationRepo) {

        this.mCameraStatus = new MutableLiveData<>();
        this.mFerryStatus = new MutableLiveData<>();
        this.mTravelTimeStatus = new MutableLiveData<>();
        this.mPassStatus = new MutableLiveData<>();

        this.mFavoritesLoadingTasks = new MediatorLiveData<>();
        this.mFavoritesLoadingTasks.setValue(0);

        this.mFavoritesLoadingTasks.addSource(mFerryStatus, status -> {
            if (status != null && mFavoritesLoadingTasks.getValue() != null) {
                if (status.status.equals(Status.LOADING)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() + 1);
                } else if (status.status.equals(Status.ERROR)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() - 1);
                } else if (status.status.equals(Status.SUCCESS)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() - 1);
                }
            }
        });

        this.mFavoritesLoadingTasks.addSource(mTravelTimeStatus, status -> {
            if (status != null && mFavoritesLoadingTasks.getValue() != null) {
                if (status.status.equals(Status.LOADING)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() + 1);
                } else if (status.status.equals(Status.ERROR)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() - 1);
                } else if (status.status.equals(Status.SUCCESS)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() - 1);
                }
            }
        });

        this.mFavoritesLoadingTasks.addSource(mPassStatus, status -> {
            if (status != null && mFavoritesLoadingTasks.getValue() != null) {
                if (status.status.equals(Status.LOADING)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() + 1);
                } else if (status.status.equals(Status.ERROR)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() - 1);
                } else if (status.status.equals(Status.SUCCESS)) {
                    mFavoritesLoadingTasks.setValue(mFavoritesLoadingTasks.getValue() - 1);
                }
            }
        });

        this.cameraRepo = cameraRepo;
        this.ferryScheduleRepo = ferryScheduleRepo;
        this.travelTimeRepo = travelTimeRepo;
        this.mountainPassRepo = mountainPassRepo;
        this.myRoutesRepo = myRoutesRepo;
        this.mapLocationRepo = mapLocationRepo;
    }

    LiveData<Integer> getFavoritesLoadingTasksCount() {
        return this.mFavoritesLoadingTasks;
    }

    public LiveData<List<CameraEntity>> getFavoriteCameras() {
        return this.cameraRepo.loadFavoriteCameras(mCameraStatus);
    }
    public void setCameraIsStarred(int cameraId, int isStarred){
        this.cameraRepo.setIsStarred(cameraId, isStarred);
    }

    public LiveData<List<FerryScheduleEntity>> getFavoriteFerrySchedules() {
        return this.ferryScheduleRepo.loadFavoriteSchedules(mFerryStatus);
    }
    public void setFerryScheduleIsStarred(int routeId, int isStarred){
        this.ferryScheduleRepo.setIsStarred(routeId, isStarred);
    }

    public LiveData<List<TravelTimeEntity>> getFavoriteTravelTimes(){
        return this.travelTimeRepo.loadFavoriteTravelTimes(mTravelTimeStatus);
    }
    public void setTravelTimeIsStarred(int timeId, int isStarred){
        this.travelTimeRepo.setIsStarred(timeId, isStarred);
    }

    public LiveData<List<MountainPassEntity>> getFavoritePasses(){
        return this.mountainPassRepo.loadFavoriteMountainPasses(mPassStatus);
    }
    public void setPassIsStarred(int passId, int isStarred){
        this.mountainPassRepo.setIsStarred(passId, isStarred);
    }

    public LiveData<List<MyRouteEntity>> getFavoriteMyRoutes(){
        return this.myRoutesRepo.loadFavoriteMyRoutes();
    }
    public void setMyRouteIsStarred(int routeId, int isStarred){
        this.myRoutesRepo.setIsStarred(routeId, isStarred);
    }

    public LiveData<List<MapLocationEntity>> getMapLocations(){
        return this.mapLocationRepo.loadMapLocations();
    }
    public void addMapLocation(MapLocationEntity mapLocation){
        this.mapLocationRepo.addMapLocation(mapLocation);
    }
    public void editMapLocationName(int locationId, String newName){
        this.mapLocationRepo.editMapLocationTitle(locationId, newName);
    }
    public void deleteMapLocation(int locationId){
        this.mapLocationRepo.deleteMapLocation(locationId);
    }

    public void forceRefresh() {
        ferryScheduleRepo.refreshData(mFerryStatus, true);
        mountainPassRepo.refreshData(mPassStatus, true);
        travelTimeRepo.refreshData(mTravelTimeStatus, true);
    }

}
