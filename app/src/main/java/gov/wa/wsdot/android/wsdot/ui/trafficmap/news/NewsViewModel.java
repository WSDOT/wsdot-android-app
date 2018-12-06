package gov.wa.wsdot.android.wsdot.ui.trafficmap.news;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.NewsReleaseRepository;
import gov.wa.wsdot.android.wsdot.shared.NewsItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class NewsViewModel extends ViewModel {

    private static String TAG = NewsViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private NewsReleaseRepository newsReleaseRepo;

    @Inject
    NewsViewModel(NewsReleaseRepository newsReleaseRepo) {
        this.mStatus = new MutableLiveData<>();
        this.newsReleaseRepo = newsReleaseRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<NewsItem>> getNewsItems(){
        return newsReleaseRepo.getNewsItems();
    }

    public void refresh() {
        newsReleaseRepo.refreshData(this.mStatus);
    }
}
