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

import gov.wa.wsdot.android.wsdot.shared.FacebookItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class FacebookRepository extends NetworkResourceRepository {

    private final static String TAG = FacebookRepository.class.getSimpleName();

    private MutableLiveData<List<FacebookItem>> facebookPosts;

    @Inject
    public FacebookRepository(AppExecutors appExecutors) {
        super(appExecutors);
        facebookPosts = new MutableLiveData<>();
    }

    public MutableLiveData<List<FacebookItem>> getFacebookPosts(MutableLiveData<ResourceStatus> status) {
        return this.facebookPosts;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {
        String urlPattern = "(https?:\\/\\/[-a-zA-Z0-9._~:\\/?#@!$&\'()*+,;=%]+)";
        String text;
        String htmlText;

        List<FacebookItem> mFacebookItems = new ArrayList<>();

        URL url;

        url = new URL(APIEndPoints.WSDOT_FACEBOOK);
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
            FacebookItem i = new FacebookItem();
            htmlText = "";
            text = item.getString("message");
            htmlText = text.replaceAll(urlPattern, "<a href=\"$1\">$1</a>");

            i.setMessage(text);
            i.setmHtmlFormattedMessage(htmlText);
            i.setId(item.getString("id"));

            try {
                i.setCreatedAt(ParserUtils.relativeTimeFromUTC(
                        item.getString("created_at"),
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            } catch (Exception e) {
                i.setCreatedAt("");
                Log.e(TAG, "Error parsing date", e);
            }

            mFacebookItems.add(i);
        }
        facebookPosts.postValue(mFacebookItems);
    }
}