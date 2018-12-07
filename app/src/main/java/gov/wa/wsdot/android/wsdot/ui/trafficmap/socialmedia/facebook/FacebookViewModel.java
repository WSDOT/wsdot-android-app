package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.facebook;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.FacebookRepository;
import gov.wa.wsdot.android.wsdot.shared.FacebookItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FacebookViewModel extends ViewModel {

    private static String TAG = FacebookViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private FacebookRepository facebookRepo;

    @Inject
    FacebookViewModel(FacebookRepository facebookRepo) {
        this.mStatus = new MutableLiveData<>();
        this.facebookRepo = facebookRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<FacebookItem>> getFacebookPosts(){
        return facebookRepo.getFacebookPosts(mStatus);
    }

    public void refresh() {
        facebookRepo.refreshData(this.mStatus);
    }

}