package gov.wa.wsdot.android.wsdot.ui.mountainpasses;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.repository.MountainPassRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MountainPassViewModel extends ViewModel {

    private LiveData<List<MountainPassEntity>> passes;
    private LiveData<MountainPassEntity> pass;

    private MutableLiveData<ResourceStatus> mStatus;

    private MountainPassRepository passRepo;

    @Inject
    MountainPassViewModel(MountainPassRepository passRepo) {
        this.mStatus = new MutableLiveData<>();
        this.passRepo = passRepo;
    }

    public LiveData<List<MountainPassEntity>> getPasses(){
        if (passes == null){
            this.passes = passRepo.loadMountainPasses(mStatus);
        }
        return this.passes;
    }

    public LiveData<MountainPassEntity> getPassFor(Integer id){
        if (pass == null){
            this.pass = passRepo.loadMountainPassFor(id, mStatus);
        }
        return this.pass;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public void setIsStarredFor(Integer passId, Integer isStarred){
        passRepo.setIsStarred(passId, isStarred);
    }

    public void forceRefreshPasses() {
        passRepo.refreshData(mStatus, true);
    }
}
