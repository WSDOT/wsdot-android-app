package gov.wa.wsdot.android.wsdot.ui.myroute.report.traveltimes;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.repository.TravelTimeRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MyRouteTravelTimesViewModel extends ViewModel {

    final static String TAG = MyRouteTravelTimesViewModel.class.getSimpleName();

    private LiveData<List<TravelTimeGroup>> travelTimes;

    private MutableLiveData<ResourceStatus> mStatus;

    private TravelTimeRepository travelTimeRepo;

    @Inject
    MyRouteTravelTimesViewModel(TravelTimeRepository travelTimeRepo) {
        this.mStatus = new MutableLiveData<>();
        this.travelTimeRepo = travelTimeRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<List<TravelTimeGroup>> loadTravelTimesForTitles(String[] titles) {
        return travelTimeRepo.getTravelTimesWithTitles(titles, mStatus);
    }

    public void setIsStarredFor(String title, Integer isStarred){
        travelTimeRepo.setIsStarred(title, isStarred);
    }

    public void forceRefreshTravelTimes() {
        travelTimeRepo.refreshData(mStatus, true);
    }


}
