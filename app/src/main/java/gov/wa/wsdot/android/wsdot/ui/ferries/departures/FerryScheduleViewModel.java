package gov.wa.wsdot.android.wsdot.ui.ferries.departures;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.livedata.FerriesScheduleDateItemLiveData;
import gov.wa.wsdot.android.wsdot.util.AbsentLiveData;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerryScheduleViewModel extends ViewModel {

    private LiveData<FerryScheduleEntity> schedule; // Object from database
    private FerriesScheduleDateItemLiveData datesWithSailings; // Object UI expects when displaying one sailing

    private MutableLiveData<ResourceStatus> mStatus;

    private AppExecutors appExecutors;
    private FerryScheduleRepository ferryScheduleRepo;


    @Inject
    FerryScheduleViewModel(FerryScheduleRepository ferryScheduleRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.ferryScheduleRepo = ferryScheduleRepo;
        this.appExecutors = appExecutors;
    }

    // if an id was passed, just load details for that schedule, other wise load all schedules TODO: should this be two view models?
    public void init(@Nullable Integer routeId){
            this.schedule = ferryScheduleRepo.loadFerryScheduleFor(routeId, mStatus);
            if (schedule != null){
                this.datesWithSailings = new FerriesScheduleDateItemLiveData(schedule);
            } else {
                this.datesWithSailings = null;
            }
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<FerryScheduleEntity> getFerrySchedule(){
        if (this.schedule == null){
            return AbsentLiveData.create();
        }
        return this.schedule;
    }

    public LiveData<List<FerriesScheduleDateItem>> getDatesWithSailings() {
        if (this.datesWithSailings == null){
            return AbsentLiveData.create();
        }
        return this.datesWithSailings;
    }

    public void setIsStarredFor(Integer routeId, Integer isStarred){
        ferryScheduleRepo.setIsStarred(routeId, isStarred);
    }

    public void forceRefreshFerrySchedules() {
        ferryScheduleRepo.refreshData(mStatus, true);
    }

}
