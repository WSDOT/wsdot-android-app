package gov.wa.wsdot.android.wsdot.ui.camera;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class CameraViewModel extends ViewModel {

    private static String TAG = MapCameraViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private CameraRepository cameraRepo;

    @Inject
    CameraViewModel(CameraRepository cameraRepo) {
        this.mStatus = new MutableLiveData<>();
        this.cameraRepo = cameraRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<CameraEntity> getCamera(Integer id) {
        return this.cameraRepo.getCamera(id, mStatus);
    }

    public LiveData<List<CameraEntity>> loadCamerasForIds(int[] ids) {
        return this.cameraRepo.loadCamerasForIds(ids, mStatus);
    }

    public void setIsStarredFor(Integer cameraId, Integer isStarred){
        cameraRepo.setIsStarred(cameraId, isStarred);
    }

    public void forceRefreshCameras() {
        cameraRepo.refreshData(mStatus, true);
    }
}
