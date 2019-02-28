package gov.wa.wsdot.android.wsdot.repository;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import gov.wa.wsdot.android.wsdot.shared.TwitterItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class TwitterRepository extends NetworkResourceRepository {

    private final static String TAG = TwitterRepository.class.getSimpleName();

    private MediatorLiveData<List<TwitterItem>> twitterPosts;

    private MutableLiveData<String> mScreenName = new MutableLiveData<>();

    @Inject
    public TwitterRepository(AppExecutors appExecutors) {
        super(appExecutors);
        mScreenName.setValue("all");
    }

    public void setScreenName(String screenName) {
        this.mScreenName.setValue(screenName);
    }

    public MutableLiveData<List<TwitterItem>> getTwitterPosts(MutableLiveData<ResourceStatus> status) {
        if (twitterPosts == null){
            twitterPosts = new MediatorLiveData<>();
            twitterPosts.addSource(mScreenName, s -> {
                this.refreshData(status);
            });
        }
        return this.twitterPosts;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {
        String urlPattern = "(https?:\\/\\/[-a-zA-Z0-9._~:\\/?#@!$&\'()*+,;=%]+)";
        String atPattern = "@+([_a-zA-Z0-9-]+)";
        String hashPattern = "#+([_a-zA-Z0-9-]+)";
        String ampPattern = "(&amp;)";
        String text;
        String htmlText;

        List<TwitterItem> mItems = new ArrayList<>();
        TwitterItem i;
        URL url;

        if (mScreenName.getValue() == null || mScreenName.getValue().equals("all")) {
            url = new URL(APIEndPoints.WSDOT_TWITTER);
        } else {
            url = new URL(APIEndPoints.WSDOT_TWITTER + mScreenName.getValue());
        }

        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONArray items = new JSONArray(jsonFile);

        int numItems = items.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = items.getJSONObject(j);
            i = new TwitterItem();
            text = item.getString("text");
            text = text.replaceAll(ampPattern, "&");
            htmlText = text.replaceAll(urlPattern, "<a href=\"$1\">$1</a>");
            htmlText = htmlText.replaceAll(atPattern, "<a href=\"http://twitter.com/#!/$1\">@$1</a>");
            htmlText = htmlText.replaceAll(hashPattern, "<a href=\"http://twitter.com/#!/search?q=%23$1\">#$1</a>");

            i.setId(item.getString("id"));
            JSONObject entities = item.getJSONObject("entities");

            try {
                JSONArray media = entities.getJSONArray("media");
                JSONObject mediaItem = media.getJSONObject(0);
                i.setMediaUrl(mediaItem.getString("media_url"));
            } catch (JSONException e) {
                // TODO Nothing.
            }

            if (i.getMediaUrl() == null) {
                try {
                    JSONArray urls = entities.getJSONArray("urls");
                    JSONObject urlItem = urls.getJSONObject(0);
                    String expanded_url = urlItem.getString("expanded_url");
                    if (expanded_url.matches("(.*)twitpic.com(.*)")) {
                        i.setMediaUrl(urlItem.getString("expanded_url"));
                    }
                } catch (Exception e1) {
                    // TODO Nothing.
                }
            }

            i.setText(text);
            i.setFormatedHtmlText(htmlText);

            JSONObject user = item.getJSONObject("user");
            i.setUserName(user.getString("name"));
            i.setScreenName(user.getString("screen_name"));

            try {
                i.setCreatedAt(ParserUtils.relativeTimeFromUTC(item.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            } catch (Exception e) {
                i.setCreatedAt("");
                Log.e(TAG, "Error parsing date", e);
            }

            mItems.add(i);
        }

        twitterPosts.postValue(mItems);
    }
}