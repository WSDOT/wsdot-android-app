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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.shared.YouTubeItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class YouTubeRepository extends NetworkResourceRepository {

    private final static String TAG = YouTubeRepository.class.getSimpleName();

    private MutableLiveData<List<YouTubeItem>> youtubePosts;

    @Inject
    public YouTubeRepository(AppExecutors appExecutors) {
        super(appExecutors);
        youtubePosts = new MutableLiveData<>();
    }

    public MutableLiveData<List<YouTubeItem>> getYoutubePosts(MutableLiveData<ResourceStatus> status) {
        return this.youtubePosts;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        List<YouTubeItem> mItems = new ArrayList<>();

        URL url = new URL(APIEndPoints.YOUTUBE + "?part=snippet&maxResults=10&playlistId=UUmWr7UYgRp4v_HvRfEgquXg&key="
                + APIEndPoints.GOOGLE_API_KEY);
        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONArray items = obj.getJSONArray("items");

        int numItems = items.length();
        for (int j = 0; j < numItems; j++) {
            JSONObject item = items.getJSONObject(j);
            JSONObject snippet = item.getJSONObject("snippet");
            JSONObject thumbnail = snippet.getJSONObject("thumbnails");
            JSONObject resourceId = snippet.getJSONObject("resourceId");
            YouTubeItem i = new YouTubeItem();
            i.setId(resourceId.getString("videoId"));
            i.setTitle(snippet.getString("title"));
            i.setDescription(snippet.getString("description"));
            i.setThumbNailUrl(thumbnail.getJSONObject("high").getString("url"));
            i.setViewCount("Unavailable");

            try {
                i.setUploaded(ParserUtils.relativeTime(
                        snippet.getString("publishedAt"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true));
            } catch (Exception e) {
                i.setUploaded("Unavailable");
                Log.e(TAG, "Error parsing date", e);
            }

            mItems.add(i);
        }

        youtubePosts.postValue(mItems);

    }
}