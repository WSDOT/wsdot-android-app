package gov.wa.wsdot.android.wsdot.ui.home;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.repository.BorderWaitRepository;
import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.repository.MapLocationRepository;
import gov.wa.wsdot.android.wsdot.repository.MountainPassRepository;
import gov.wa.wsdot.android.wsdot.repository.MyRoutesRepository;
import gov.wa.wsdot.android.wsdot.repository.TollRateSignRepository;
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

    private BorderWaitRepository borderWaitRepo;
    private MutableLiveData<ResourceStatus> mBorderWaitStatus;

    private MyRoutesRepository myRoutesRepo;

    private MapLocationRepository mapLocationRepo;

    private TollRateSignRepository tollRatesRepo;
    private MutableLiveData<ResourceStatus> mTollRatesStatus;

    private MediatorLiveData<Integer> mFavoritesLoadingTasks;

    @Inject
    FavoritesViewModel(CameraRepository cameraRepo,
                       FerryScheduleRepository ferryScheduleRepo,
                       TravelTimeRepository travelTimeRepo,
                       MountainPassRepository mountainPassRepo,
                       MyRoutesRepository myRoutesRepo,
                       MapLocationRepository mapLocationRepo,
                       TollRateSignRepository tollRatesRepo,
                       BorderWaitRepository borderWaitRepo) {

        this.mCameraStatus = new MutableLiveData<>();
        this.mFerryStatus = new MutableLiveData<>();
        this.mTravelTimeStatus = new MutableLiveData<>();
        this.mPassStatus = new MutableLiveData<>();
        this.mTollRatesStatus = new MutableLiveData<>();
        this.mBorderWaitStatus = new MutableLiveData<>();

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

        this.mFavoritesLoadingTasks.addSource(mBorderWaitStatus, status -> {
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

        this.mFavoritesLoadingTasks.addSource(mTollRatesStatus, status -> {
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
        this.tollRatesRepo = tollRatesRepo;
        this.borderWaitRepo = borderWaitRepo;

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

    public LiveData<List<TravelTimeGroup>> getFavoriteTravelTimes(){
        return this.travelTimeRepo.loadFavoriteTravelTimes(mTravelTimeStatus);
    }
    public void setTravelTimeIsStarred(String title, int isStarred){
        this.travelTimeRepo.setIsStarred(title, isStarred);
    }

    public LiveData<List<MountainPassEntity>> getFavoritePasses(){
        return this.mountainPassRepo.loadFavoriteMountainPasses(mPassStatus);
    }
    public void setPassIsStarred(int passId, int isStarred){
        this.mountainPassRepo.setIsStarred(passId, isStarred);
    }

    public LiveData<List<TollRateGroup>> getFavoriteTollRates(){
        return this.tollRatesRepo.loadFavoriteTolls(mTollRatesStatus);
    }
    public void setTollRateIsStarred(String signId, int isStarred){
        this.tollRatesRepo.setIsStarred(signId, isStarred);
    }

    public LiveData<List<BorderWaitEntity>> getFavoriteBorderWaits() {
        return this.borderWaitRepo.loadFavoriteBorderWaits(mBorderWaitStatus);
    }
    public void setBorderWaitIsStarred(int waitId, int isStarred){
        this.borderWaitRepo.setIsStarred(waitId, isStarred);
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
        tollRatesRepo.refreshData(mTollRatesStatus, true);
        borderWaitRepo.refreshData(mBorderWaitStatus, true);
    }

}
