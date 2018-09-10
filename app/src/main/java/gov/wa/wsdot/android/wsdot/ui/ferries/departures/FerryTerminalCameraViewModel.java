package gov.wa.wsdot.android.wsdot.ui.ferries.departures;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerryTerminalCameraViewModel extends ViewModel {

    private static String TAG = MapCameraViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private MediatorLiveData<List<CameraItem>> terminalCameras;

    private static Map<Integer, FerriesTerminalItem> ferriesTerminalMap = new HashMap<>();

    private AppExecutors appExecutors;
    private CameraRepository cameraRepo;

    @Inject
    FerryTerminalCameraViewModel(CameraRepository cameraRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.terminalCameras = new MediatorLiveData<>();
        this.cameraRepo = cameraRepo;
        this.appExecutors = appExecutors;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<CameraItem>> getTerminalCameras() {
        return this.terminalCameras;
    }

    public void loadTerminalCameras(Integer terminalId, String roadName){
        terminalCameras.addSource(cameraRepo.getCamerasForRoad(roadName, mStatus), cameras -> {
            processTerminalCameras(terminalId, cameras);
        });
    }

    public void processTerminalCameras(Integer terminalId, List<CameraEntity> cameras){

        List<CameraItem> terminalCameraItems = new ArrayList<>();

        for (CameraEntity cameraEntity: cameras) {

            int distance = FerryHelper.getDistanceFromTerminal(terminalId, cameraEntity.getLatitude(), cameraEntity.getLongitude());

            // If less than 3 miles from terminal, and labeled as a ferries camera, show it
            if (distance < 15840 && cameraEntity.getRoadName().toLowerCase(Locale.US).equals("ferries")) { // in feet
                CameraItem camera = new CameraItem();
                camera.setCameraId(cameraEntity.getCameraId());
                camera.setTitle(cameraEntity.getTitle());
                camera.setImageUrl(cameraEntity.getUrl());
                camera.setLatitude(cameraEntity.getLatitude());
                camera.setLongitude(cameraEntity.getLongitude());
                camera.setDistance(distance);
                terminalCameraItems.add(camera);
            }
        }
        terminalCameras.postValue(terminalCameraItems);
    }

}
