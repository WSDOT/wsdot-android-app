package gov.wa.wsdot.android.wsdot.ui.camera;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.livedata.CameraItemLiveData;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MapCameraViewModel extends ViewModel {

    private static String TAG = MapCameraViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private MutableLiveData<LatLngBounds> mapBounds;
    private CameraItemLiveData displayableCameraItems;
    private MediatorLiveData<List<CameraItem>> displayedCameraItems;

    private CameraRepository cameraRepo;

    @Inject
    MapCameraViewModel(CameraRepository cameraRepo) {
        this.mStatus = new MutableLiveData<>();
        this.cameraRepo = cameraRepo;
    }

    public void init(@Nullable String roadName) {

        if (roadName != null){
            this.displayableCameraItems = new CameraItemLiveData(cameraRepo.getCamerasForRoad(roadName, mStatus));
        } else {
            this.displayableCameraItems = new CameraItemLiveData(cameraRepo.loadCameras(mStatus));
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
