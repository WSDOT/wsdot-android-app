package gov.wa.wsdot.android.wsdot.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.notifications.NotificationTopicEntity;
import gov.wa.wsdot.android.wsdot.repository.NotificationTopicsRepository;
import gov.wa.wsdot.android.wsdot.util.AbsentLiveData;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class NotificationsViewModel extends ViewModel {

    private static String TAG = NotificationsViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private NotificationTopicsRepository topicsRepo;

    private LiveData<List<NotificationTopicEntity>> topics;
    private LiveData<HashMap<String, List<NotificationTopicEntity>>> topicsMap;

    @Inject
    public NotificationsViewModel(NotificationTopicsRepository topicsRepo) {
        this.mStatus = new MutableLiveData<>();
        this.topicsRepo = topicsRepo;
    }

    public void init(String iid) {
        this.topics = topicsRepo.loadTopics(iid, mStatus);
        this.topicsMap = Transformations.map(this.topics, this::mapTopics);
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<HashMap<String, List<NotificationTopicEntity>>> getTopics() {

        if (this.topicsMap == null){
            return AbsentLiveData.create();
        }

        return this.topicsMap;
    }

    void updateSubscription(String topic, Boolean subscribe) {
        topicsRepo.updateSubscription(topic, subscribe);
    }

    private HashMap<String, List<NotificationTopicEntity>> mapTopics(List<NotificationTopicEntity> topics){

        HashMap<String, List<NotificationTopicEntity>> mTopicsMap = new HashMap<>();

        for (NotificationTopicEntity topic: topics) {

            String category = topic.getCategory();

            if (mTopicsMap.get(category) == null) {
                List<NotificationTopicEntity> mTopics = new ArrayList<>();
                mTopics.add(topic);
                mTopicsMap.put(category, mTopics);
            } else {
                mTopicsMap.get(category).add(topic);
            }

        }

        return  mTopicsMap;
    }

}