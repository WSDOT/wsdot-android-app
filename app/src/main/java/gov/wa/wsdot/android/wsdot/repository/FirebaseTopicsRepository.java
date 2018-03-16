package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.shared.TopicItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class FirebaseTopicsRepository extends NetworkResourceRepository {

    private final static String TAG = FirebaseTopicsRepository.class.getSimpleName();

    private MutableLiveData<HashMap<String, List<TopicItem>>> topics;

    private String iid;

    @Inject
    public FirebaseTopicsRepository(AppExecutors appExecutors) {
        super(appExecutors);
        topics = new MutableLiveData<>();
    }

    public MutableLiveData<HashMap<String, List<TopicItem>>> getTopics(String idd, MutableLiveData<ResourceStatus> status) {
        this.iid = idd;
        this.topics.setValue(null);
        this.refreshData(status);
        return this.topics;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {
        fetchTopics();
    }

    private void fetchTopics() throws Exception {

        HashMap<String, List<TopicItem>> mTopics = new HashMap<>();

        URL url = new URL(APIEndPoints.FIREBASE_TOPICS + iid);

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

            TopicItem topic = new TopicItem();
            JSONObject topicJSON = items.getJSONObject(j);

            topic.setTopic(topicJSON.getString("topic"));
            topic.setSubscribed(topicJSON.getBoolean("subscribed"));

            String category = topicJSON.getString("category");

            if (mTopics.get(category) == null) {
                List<TopicItem> topics = new ArrayList<>();
                topics.add(topic);
                mTopics.put(category, topics);
            } else {
                mTopics.get(category).add(topic);
            }
        }

        topics.postValue(mTopics);
    }
}
