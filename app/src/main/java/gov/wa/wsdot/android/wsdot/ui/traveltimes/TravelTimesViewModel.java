package gov.wa.wsdot.android.wsdot.ui.traveltimes;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.repository.TravelTimeRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class TravelTimesViewModel extends ViewModel{

    final static String TAG = TravelTimesViewModel.class.getSimpleName();

    private LiveData<List<TravelTimeGroup>> filteredTravelTimes;

    private MutableLiveData<String> queryTerm;
    private MutableLiveData<ResourceStatus> mStatus;

    private TravelTimeRepository travelTimeRepo;

    @Inject
    TravelTimesViewModel(TravelTimeRepository travelTimeRepo) {
        this.mStatus = new MutableLiveData<>();
        this.travelTimeRepo = travelTimeRepo;
        this.queryTerm = new MutableLiveData<>();
        this.queryTerm.setValue("%");
    }

    public LiveData<List<TravelTimeGroup>> getQueryTravelTimes() {
        if (filteredTravelTimes == null){
            // Observe changes to queryTerm, request new travelTimes with each new query term.
            // filteredTravelTimes becomes backed by the LiveData result of of queryTravelTimes()
            this.filteredTravelTimes = Transformations.switchMap(queryTerm, queryString -> travelTimeRepo.queryTravelTimeGroups(queryString, mStatus));
        }
        return filteredTravelTimes;
    }

    public void setQueryTerm(String queryString){
        this.queryTerm.setValue(queryString.equals("") ? "%" : "%" + queryString + "%");
    }

    public String getQueryTermValue(){
        return queryTerm.getValue();
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public void setIsStarredFor(String title, Integer isStarred){
        travelTimeRepo.setIsStarred(title, isStarred);
    }

    public void forceRefreshTravelTimes() {
        travelTimeRepo.refreshData(mStatus, true);
    }

}
