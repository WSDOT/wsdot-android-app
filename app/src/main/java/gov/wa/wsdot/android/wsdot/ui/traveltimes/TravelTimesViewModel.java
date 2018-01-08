package gov.wa.wsdot.android.wsdot.ui.traveltimes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.repository.TravelTimeRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class TravelTimesViewModel extends ViewModel{

    final static String TAG = TravelTimesViewModel.class.getSimpleName();

    private LiveData<List<TravelTimeEntity>> filteredTravelTimes;

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

    public LiveData<List<TravelTimeEntity>> getQueryTravelTimes() {
        if (filteredTravelTimes == null){
            // Observe changes to queryTerm, request new travelTimes with each new query term.
            // filteredTravelTimes becomes backed by the LiveData result of of queryTravelTimes()
            this.filteredTravelTimes = Transformations.switchMap(queryTerm, queryString -> travelTimeRepo.queryTravelTimes(queryString, mStatus));
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

    public void setIsStarredFor(Integer passId, Integer isStarred){
        travelTimeRepo.setIsStarred(passId, isStarred);
    }

    public void forceRefreshTravelTimes() {
        travelTimeRepo.refreshData(mStatus, true);
    }

}
