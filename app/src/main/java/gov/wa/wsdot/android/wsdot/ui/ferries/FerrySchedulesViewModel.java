package gov.wa.wsdot.android.wsdot.ui.ferries;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerrySchedulesViewModel extends ViewModel {

    private LiveData<List<FerryScheduleEntity>> schedules;
    private LiveData<FerryScheduleEntity> schedule;

    private MutableLiveData<ResourceStatus> mStatus;

    private FerryScheduleRepository ferryScheduleRepo;

    @Inject
    FerrySchedulesViewModel(FerryScheduleRepository ferryScheduleRepo) {
        this.mStatus = new MutableLiveData<>();
        this.ferryScheduleRepo = ferryScheduleRepo;
    }

    public LiveData<List<FerryScheduleEntity>> getFerrySchedules(){
        if (schedules == null){
            this.schedules = ferryScheduleRepo.getFerrySchedules(mStatus);
            return this.schedules;
        } else {
            return this.schedules;
        }
    }

    public LiveData<FerryScheduleEntity> getPassFor(Integer id){
        if (schedule == null){
            this.schedule = ferryScheduleRepo.getFerryScheduleFor(id, mStatus);
            return this.schedule;
        } else {
            return this.schedule;
        }
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public void setIsStarredFor(Integer passId, Integer isStarred){
        ferryScheduleRepo.setIsStarred(passId, isStarred);
    }

    public void forceRefreshFerrySchedules() {
        ferryScheduleRepo.refreshData(mStatus, true);
    }

}
