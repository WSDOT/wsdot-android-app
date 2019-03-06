package gov.wa.wsdot.android.wsdot.ui.camera;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
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

        displayedCameraItems.addSource(displayableCameraItems, cameraItems -> {
            if (mapBounds.getValue() != null) {
                displayedCameraItems.postValue(filterDisplayedCamerasFor(mapBounds.getValue(), cameraItems));
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
