package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.twitter;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.TwitterRepository;
import gov.wa.wsdot.android.wsdot.shared.TwitterItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class TwitterViewModel extends ViewModel {

    private static String TAG = TwitterViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private TwitterRepository twitterRepo;

    @Inject
    TwitterViewModel(TwitterRepository twitterRepo) {
        this.mStatus = new MutableLiveData<>();
        this.twitterRepo = twitterRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<TwitterItem>> getTwitterPosts(){
        return twitterRepo.getTwitterPosts(mStatus);
    }

    public void setAccount(String screenName){
        twitterRepo.setScreenName(screenName);
    }

    public void refresh() {
        twitterRepo.refreshData(this.mStatus);
    }

}