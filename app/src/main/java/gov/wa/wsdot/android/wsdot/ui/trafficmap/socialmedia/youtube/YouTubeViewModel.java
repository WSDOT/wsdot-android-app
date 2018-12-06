package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.youtube;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.YouTubeRepository;
import gov.wa.wsdot.android.wsdot.shared.YouTubeItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class YouTubeViewModel extends ViewModel {

    private static String TAG = YouTubeViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private YouTubeRepository youTubeRepository;

    @Inject
    YouTubeViewModel(YouTubeRepository youTubeRepository) {
        this.mStatus = new MutableLiveData<>();
        this.youTubeRepository = youTubeRepository;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<YouTubeItem>> getYouTubePosts(){
        return youTubeRepository.getYoutubePosts(mStatus);
    }

    public void refresh() {
        youTubeRepository.refreshData(this.mStatus);
    }

}