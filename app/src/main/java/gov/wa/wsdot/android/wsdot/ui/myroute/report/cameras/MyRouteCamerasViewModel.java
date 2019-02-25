package gov.wa.wsdot.android.wsdot.ui.myroute.report.cameras;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.livedata.CameraItemLiveData;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MyRouteCamerasViewModel extends ViewModel {

        private static String TAG = MyRouteCamerasViewModel.class.getSimpleName();

        private MutableLiveData<ResourceStatus> mStatus;
        private CameraRepository cameraRepo;

        @Inject
        MyRouteCamerasViewModel(CameraRepository cameraRepo) {
            this.mStatus = new MutableLiveData<>();
            this.cameraRepo = cameraRepo;
        }

        public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

        public void refreshCameras(){
            cameraRepo.refreshData(mStatus, true);
        }

        public LiveData<List<CameraItem>> loadCamerasForIds(int[] ids) {
            return new CameraItemLiveData(cameraRepo.loadCamerasForIds(ids, mStatus));
        }

    }
