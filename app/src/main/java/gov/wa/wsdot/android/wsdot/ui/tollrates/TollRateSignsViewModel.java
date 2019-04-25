package gov.wa.wsdot.android.wsdot.ui.tollrates;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.repository.TollRateSignRepository;
import gov.wa.wsdot.android.wsdot.repository.TravelTimeRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class TollRateSignsViewModel extends ViewModel {

    private static String TAG = TollRateSignsViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;
    private MutableLiveData<ResourceStatus> mTravelTimeStatus;

    private TollRateSignRepository tollRepo;
    private TravelTimeRepository travelTimeRepo;

    @Inject
    TollRateSignsViewModel(TollRateSignRepository tollRepo, TravelTimeRepository travelTimeRepo) {
        this.mStatus = new MutableLiveData<>();
        this.mTravelTimeStatus = new MutableLiveData<>();
        this.tollRepo = tollRepo;
        this.travelTimeRepo = travelTimeRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<ResourceStatus> getTravelTimesStatus() { return this.mTravelTimeStatus; }

    public LiveData<List<TollRateGroup>> getI405TollRateItems() {
        return tollRepo.loadI405TollRateGroups(mStatus);
    }

    public LiveData<List<TollRateGroup>> getSR167TollRateItems() {
        return tollRepo.loadSR167TollRateGroups(mStatus);
    }

    public LiveData<List<TravelTimeEntity>> getTravelTimesForETLFor(String route){
        List<Integer> ids = new ArrayList<>();
        if (route.equals("405")) {

            // Northbound
            ids.add(35); // GP
            ids.add(36); // ETL

            // Southbound
            ids.add(38); // GP
            ids.add(37); // ETL

        } else if (route.equals("167")) {

            // Northbound
            ids.add(67); // GP
            ids.add(68); // HOV

            // Southbound
            ids.add(70); // GP
            ids.add(69); // HOV

        }

        return this.travelTimeRepo.getTravelTimesWithIds(ids, mTravelTimeStatus);
    }

    public void setIsStarredFor(String title, Integer isStarred){
        tollRepo.setIsStarred(title, isStarred);
    }

    public void refresh() {
        tollRepo.refreshData(this.mStatus, true);
        travelTimeRepo.refreshData(this.mTravelTimeStatus, true);
    }

}
