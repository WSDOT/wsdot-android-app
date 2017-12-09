package gov.wa.wsdot.android.wsdot.ui.ferries.vesselwatch;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.VesselWatchRepository;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class VesselWatchViewModel extends ViewModel {

    private static String TAG = VesselWatchViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private VesselWatchRepository vesselWatchRepo;

    @Inject
    VesselWatchViewModel(VesselWatchRepository vesselWatchRepo) {
        this.mStatus = new MutableLiveData<>();
        this.vesselWatchRepo = vesselWatchRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<VesselWatchItem>> getVessels(){
        return vesselWatchRepo.getVessels();
    }

    public void refreshVessels() {
        vesselWatchRepo.refreshData(this.mStatus);
    }

}
