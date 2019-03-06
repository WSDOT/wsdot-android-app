package gov.wa.wsdot.android.wsdot.repository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.MutableLiveData;
import gov.wa.wsdot.android.wsdot.shared.ExpressLaneItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class ExpressLanesRepository extends NetworkResourceRepository {

    private final static String TAG = ExpressLanesRepository.class.getSimpleName();

    private MutableLiveData<List<ExpressLaneItem>> expressLanesStatus;

    @Inject
    public ExpressLanesRepository(AppExecutors appExecutors) {
        // Supply the super class with data needed for super.refreshData()
        super(appExecutors);
        expressLanesStatus = new MutableLiveData<>();
    }

    public MutableLiveData<List<ExpressLaneItem>> getExpressLanes() {
        return this.expressLanesStatus;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        ArrayList<ExpressLaneItem> expressLaneItems = new ArrayList<ExpressLaneItem>();

        URL url = new URL(APIEndPoints.EXPRESS_LANES);
        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONObject result = obj.getJSONObject("express_lanes");
        JSONArray items = result.getJSONArray("routes");

        int numItems = items.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = items.getJSONObject(j);
            ExpressLaneItem i = new ExpressLaneItem();
            i.setTitle(item.getString("title"));
            i.setRoute(item.getInt("route"));
            i.setStatus(item.getString("status"));
            i.setUpdated(ParserUtils.relativeTime(item.getString("updated"), "yyyy-MM-dd h:mm a", false));
            expressLaneItems.add(i);
        }

        Collections.sort(expressLaneItems, new RouteComparator());

        expressLanesStatus.postValue(expressLaneItems);
    }

    private static class RouteComparator implements Comparator<ExpressLaneItem> {

        public int compare(ExpressLaneItem object1, ExpressLaneItem object2) {
            int route1 = object1.getRoute();
            int route2 = object2.getRoute();

            if (route1 > route2) {
                return 1;
            } else if (route1 < route2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
