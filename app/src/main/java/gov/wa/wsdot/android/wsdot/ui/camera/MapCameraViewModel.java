package gov.wa.wsdot.android.wsdot.ui.camera;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MapCameraViewModel extends ViewModel {

    private static String TAG = MapCameraViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private MutableLiveData<LatLngBounds> mapBounds;
    private LiveData<List<CameraItem>> displayableCameraItems;
    private MediatorLiveData<List<CameraItem>> displayedCameraItems;

    private CameraRepository cameraRepo;

    @Inject
    MapCameraViewModel(CameraRepository cameraRepo) {
        this.mStatus = new MutableLiveData<>();
        this.cameraRepo = cameraRepo;
    }

    public void init(@Nullable String roadName){

        if (roadName != null){
            this.displayableCameraItems = Transformations.map(cameraRepo.getCamerasForRoad(roadName, mStatus), cameras -> transformCameras(cameras));
        } else {
            this.displayableCameraItems = Transformations.map(cameraRepo.loadCameras(mStatus), cameras -> transformCameras(cameras));
        }

        mapBounds = new MutableLiveData<>();

        displayedCameraItems = new MediatorLiveData<>();

        displayedCameraItems.addSource(mapBounds, bounds -> {
            if (displayableCameraItems.getValue() != null) {
                displayedCameraItems.postValue(filterDisplayedCamerasFor(bounds, displayableCameraItems.getValue()));
            }
        });

        displayedCameraItems.addSource(displayableCameraItems, alertsItems -> {
            if (mapBounds.getValue() != null) {
                displayedCameraItems.postValue(filterDisplayedCamerasFor(mapBounds.getValue(), alertsItems));
            }
        });
    }

    private ArrayList<CameraItem> transformCameras(List<CameraEntity> cameras){
        ArrayList<CameraItem> displayableAlertItemValues = new ArrayList<>();

        if (cameras != null) {
            for (CameraEntity camera : cameras) {

                int video = camera.getHasVideo();
                int cameraIcon = (video == 0) ? R.drawable.camera : R.drawable.camera_video;

                displayableAlertItemValues.add(new CameraItem(
                        camera.getLatitude(),
                        camera.getLongitude(),
                        camera.getTitle(),
                        camera.getUrl(),
                        camera.getCameraId(),
                        cameraIcon
                ));
            }
        }
        return displayableAlertItemValues;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public void refreshCameras(){
        cameraRepo.refreshData(mStatus, true);
    }

    public LiveData<List<CameraItem>> getDisplayCameras() {
        return displayedCameraItems;
    }

    public void setMapBounds(LatLngBounds bounds){
        this.mapBounds.setValue(bounds);
    }

    public List<CameraItem> filterDisplayedCamerasFor(LatLngBounds bounds, List<CameraItem> cameraItems) {

        ArrayList<CameraItem> displayedCameraValues = new ArrayList<>();
        for (CameraItem camera : cameraItems) {
            LatLng cameraLocation = new LatLng(camera.getLatitude(), camera.getLongitude());
            if (bounds.contains(cameraLocation)) {
                displayedCameraValues.add(camera);
            }
        }
       return displayedCameraValues;
    }

}
