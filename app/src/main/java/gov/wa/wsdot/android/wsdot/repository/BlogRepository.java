package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.shared.BlogItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class BlogRepository extends NetworkResourceRepository {

    private final static String TAG = BlogRepository.class.getSimpleName();

    private MutableLiveData<List<BlogItem>> blogPosts;

    @Inject
    public BlogRepository(AppExecutors appExecutors) {
        super(appExecutors);
        blogPosts = new MutableLiveData<>();
    }

    public MutableLiveData<List<BlogItem>> getBlogPosts(MutableLiveData<ResourceStatus> status) {
        return this.blogPosts;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {
        List<BlogItem> mItems = new ArrayList<>();
        BlogItem i = null;

        URL url = new URL(APIEndPoints.WSDOT_BLOG + "?alt=json&max-results=10");
        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONObject data = obj.getJSONObject("feed");
        JSONArray entries = data.getJSONArray("entry");

        int numEntries = entries.length();
        for (int j=0; j < numEntries; j++) {
            JSONObject entry = entries.getJSONObject(j);
            i = new BlogItem();
            i.setTitle(entry.getJSONObject("title").getString("$t"));

            try {
                i.setPublished(ParserUtils.relativeTime(entry.getJSONObject("published").getString("$t"), "yyyy-MM-dd'T'HH:mm:ss.SSSz", true));
            } catch (Exception e) {
                i.setPublished("Unavailable");
                Log.e(TAG, "Error parsing date", e);
            }

            String content = entry.getJSONObject("content").getString("$t");
            i.setContent(content);

            Document doc = Jsoup.parse(content);
            Element imgTable = doc.select("table img").first();
            Element imgDiv = doc.select("div:not(.blogger-post-footer) img").first();
            Element table = doc.select("table").first();
            if (imgTable != null) {
                String imgSrc = imgTable.attr("src");
                i.setImageUrl(imgSrc);
                if (table != null) {
                    try {
                        String caption = table.text();
                        i.setImageCaption(caption);
                    } catch (NullPointerException e) {
                        // TODO Auto-generated catch block
                    }
                }
            } else if (imgDiv != null) {
                String imgSrc = imgDiv.attr("src");
                i.setImageUrl(imgSrc);
            }

            String temp = content.replaceFirst("<i>(.*)</i><br /><br />", "");
            temp = temp.replaceFirst("<table(.*?)>.*?</table>", "");
            String tempDoc = Jsoup.parse(temp).text();
            try {
                String description = tempDoc.split("\\.", 2)[0] + ".";
                i.setDescription(description);
            } catch (ArrayIndexOutOfBoundsException e) {
                i.setDescription("");
            }

            i.setLink(entry.getJSONArray("link").getJSONObject(4).getString("href"));

            mItems.add(i);
        }
        blogPosts.postValue(mItems);
    }
}
