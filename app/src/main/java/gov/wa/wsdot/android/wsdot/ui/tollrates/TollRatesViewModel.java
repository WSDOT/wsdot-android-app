package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.repository.TollRatesRepository;
import gov.wa.wsdot.android.wsdot.repository.TravelTimeRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class TollRatesViewModel extends ViewModel {

    private static String TAG = TollRatesViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;
    private MutableLiveData<ResourceStatus> mTravelTimeStatus;

    private TollRatesRepository tollRepo;
    private TravelTimeRepository travelTimeRepo;

    @Inject
    TollRatesViewModel(TollRatesRepository tollRepo, TravelTimeRepository travelTimeRepo) {
        this.mStatus = new MutableLiveData<>();
        this.mTravelTimeStatus = new MutableLiveData<>();
        this.tollRepo = tollRepo;
        this.travelTimeRepo = travelTimeRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<ResourceStatus> getTravelTimesStatus() { return this.mTravelTimeStatus; }

    public LiveData<List<TollRateGroup>> getI405TollRateItems(){
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
            ids.add(36); // HOV

            // Southbound
            ids.add(38); // GP
            ids.add(37); // HOV
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
