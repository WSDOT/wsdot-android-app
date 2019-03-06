package gov.wa.wsdot.android.wsdot.repository;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.MutableLiveData;
import gov.wa.wsdot.android.wsdot.shared.NewsItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class NewsReleaseRepository extends NetworkResourceRepository {

    private static String TAG = NewsReleaseRepository.class.getSimpleName();

    private MutableLiveData<List<NewsItem>> newsItems;

    @Inject
    public NewsReleaseRepository(AppExecutors appExecutors) {
        super(appExecutors);
        newsItems = new MutableLiveData<>();
    }

    public MutableLiveData<List<NewsItem>> getNewsItems() {
        return this.newsItems;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        DateFormat parseDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);

        URL url = new URL(APIEndPoints.WSDOT_NEWS);
        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONObject result = obj.getJSONObject("news");
        JSONArray items = result.getJSONArray("items");

        List<NewsItem> news = new ArrayList<>();

        int numItems = items.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = items.getJSONObject(j);
            NewsItem i = new NewsItem();
            i.setTitle(item.getString("title"));
            i.setDescription(item.getString("description"));
            i.setLink(item.getString("link"));

            try {
                Date date = parseDateFormat.parse(item.getString("pubdate"));
                i.setPubDate(displayDateFormat.format(date));
            } catch (Exception e) {
                i.setPubDate("");
                Log.e(TAG, "Error parsing date", e);
            }

            news.add(i);
        }


        newsItems.postValue(news);
    }
}
