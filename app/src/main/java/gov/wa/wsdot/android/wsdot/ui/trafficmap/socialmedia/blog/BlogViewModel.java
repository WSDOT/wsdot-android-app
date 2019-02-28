package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.blog;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.repository.BlogRepository;
import gov.wa.wsdot.android.wsdot.shared.BlogItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class BlogViewModel extends ViewModel {

    private static String TAG = BlogViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private BlogRepository blogRepository;

    @Inject
    BlogViewModel(BlogRepository blogRepository) {
        this.mStatus = new MutableLiveData<>();
        this.blogRepository = blogRepository;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<BlogItem>> getBlogPosts(){
        return blogRepository.getBlogPosts(mStatus);
    }

    public void refresh() {
        blogRepository.refreshData(this.mStatus);
    }

}