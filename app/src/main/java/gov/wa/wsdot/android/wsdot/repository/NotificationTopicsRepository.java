package gov.wa.wsdot.android.wsdot.repository;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.notifications.NotificationTopicDao;
import gov.wa.wsdot.android.wsdot.database.notifications.NotificationTopicEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class NotificationTopicsRepository extends NetworkResourceSyncRepository {

    private final static String TAG = NotificationTopicsRepository.class.getSimpleName();

    private final NotificationTopicDao notificationTopicDao;

    @Inject
    public NotificationTopicsRepository(AppExecutors appExecutors, NotificationTopicDao notificationTopicDao, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (5 * DateUtils.MINUTE_IN_MILLIS), "notification_topic"); //(5 * DateUtils.MINUTE_IN_MILLIS), "notification_topic");
        this.notificationTopicDao = notificationTopicDao;
    }

    public LiveData<List<NotificationTopicEntity>> loadTopics(MutableLiveData<ResourceStatus> status) {
        this.refreshData(status, false);
        return notificationTopicDao.loadNotificationTopics();
    }

    public void updateSubscription(String topic, Boolean subscription){
        getExecutor().diskIO().execute(() -> {
            this.notificationTopicDao.updateSubscription(topic, subscription);
        });
    }

    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        List<NotificationTopicEntity> topicEntities = new ArrayList<>();

        URL url = new URL(APIEndPoints.FIREBASE_TOPICS);

        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONArray items = obj.getJSONArray("topics");

        int numItems = items.length();

        for (int j = 0; j < numItems; j++) {

            JSONObject topicJSON = items.getJSONObject(j);

            NotificationTopicEntity topic = new NotificationTopicEntity(
                    topicJSON.getString("topic"),
                    topicJSON.getString("title"),
                    topicJSON.getString("category"),
                    false
            );

            topicEntities.add(topic);
        }

        NotificationTopicEntity[] topicsArray = new NotificationTopicEntity[topicEntities.size()];
        topicsArray = topicEntities.toArray(topicsArray);

        notificationTopicDao.updateAndCleanTransaction(topicsArray);

        CacheEntity cache = new CacheEntity("notification_topic", System.currentTimeMillis());
        getCacheRepository().setCacheTime(cache);

    }
}
