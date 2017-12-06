package gov.wa.wsdot.android.wsdot.ui.camera;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MapCameraViewModel extends ViewModel {

    private static String TAG = MapCameraViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private List<CameraEntity> displayableCameras;
    private MediatorLiveData<List<CameraItem>> displayedCameras;

    private AppExecutors appExecutors;

    private CameraRepository cameraRepo;

    @Inject
    MapCameraViewModel(CameraRepository cameraRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.displayableCameras = new ArrayList<>();
        this.displayedCameras = new MediatorLiveData<>();
        this.cameraRepo = cameraRepo;
        this.appExecutors = appExecutors;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<CameraItem>> getDisplayCameras() {
        return this.displayedCameras;
    }

    public void loadDisplayCameras(LatLngBounds bounds, String roadName){
        displayedCameras.addSource(cameraRepo.getCamerasForRoad(roadName, mStatus), cameras -> {
            this.displayableCameras = cameras;
            refreshDisplayedCameras(bounds);
        });
    }

    public void refreshDisplayedCameras(LatLngBounds bounds) {
        appExecutors.taskIO().execute(() -> {
            ArrayList<CameraItem> displayedCameraValues = new ArrayList<>();

            for (CameraEntity camera : this.displayableCameras) {

                LatLng cameraLocation = new LatLng(camera.getLatitude(), camera.getLongitude());

                if (bounds.contains(cameraLocation)) {
                    int video = camera.getHasVideo();
                    int cameraIcon = (video == 0) ? R.drawable.camera : R.drawable.camera_video;

                    displayedCameraValues.add(new CameraItem(
                            camera.getLatitude(),
                            camera.getLongitude(),
                            camera.getTitle(),
                            camera.getUrl(),
                            camera.getCameraId(),
                            cameraIcon
                    ));

                }
            }
            displayedCameras.postValue(displayedCameraValues);
        });
    }

}
