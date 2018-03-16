package gov.wa.wsdot.android.wsdot.ui.notifications;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.repository.FirebaseTopicsRepository;
import gov.wa.wsdot.android.wsdot.shared.TopicItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class NotificationsViewModel extends ViewModel {

    private static String TAG = NotificationsViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private FirebaseTopicsRepository topicsRepo;

    @Inject
    NotificationsViewModel(FirebaseTopicsRepository topicsRepo) {
        this.mStatus = new MutableLiveData<>();
        this.topicsRepo = topicsRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<HashMap<String, List<TopicItem>>> getTopics(String iid) {
        return this.topicsRepo.getTopics(iid, mStatus);
    }

    public void updateSubscription(String topic, Boolean subscribe) {

        if (subscribe){
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        }

    }
}