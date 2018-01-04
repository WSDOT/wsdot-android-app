package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeDao;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class TravelTimeRepository  extends NetworkResourceSyncRepository {

    private static String TAG = TravelTimeRepository.class.getSimpleName();

    private final TravelTimeDao travelTimeDao;

    @Inject
    public TravelTimeRepository(TravelTimeDao travelTimeDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (15 * DateUtils.MINUTE_IN_MILLIS), "travel_times");
        this.travelTimeDao = travelTimeDao;
    }

    public LiveData<List<TravelTimeEntity>> loadTravelTimes(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeDao.loadTravelTimes();
    }

    public List<TravelTimeEntity> getTravelTimes(MutableLiveData<ResourceStatus> status) {
        super.refreshDataOnSameThread(status, false);
        return travelTimeDao.getTravelTimes();
    }

    public LiveData<List<TravelTimeEntity>> queryTravelTimes(String query, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeDao.queryTravelTimes(query);
    }

    public LiveData<TravelTimeEntity> getTravelTimeFor(Integer id, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeDao.loadTravelTimeFor(id);
    }

    public LiveData<List<TravelTimeEntity>> loadFavoriteTravelTimes(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeDao.loadFavoriteTravelTimes();
    }


    public void setIsStarred(Integer id, Integer isStarred) {
        getExecutor().diskIO().execute(() -> {
            travelTimeDao.updateIsStarred(id, isStarred);
        });
    }

    @Override
    public void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        List<TravelTimeEntity> starred;

        starred = travelTimeDao.getFavoriteTravelTimes();

        URL url = new URL(APIEndPoints.TRAVEL_TIMES);
        URLConnection urlConn = url.openConnection();

        BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
        GZIPInputStream gzin = new GZIPInputStream(bis);
        InputStreamReader is = new InputStreamReader(gzin);
        BufferedReader in = new BufferedReader(is);

        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONObject result = obj.getJSONObject("traveltimes");
        JSONArray items = result.getJSONArray("items");
        List<TravelTimeEntity> times = new ArrayList<>();

        int numItems = items.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = items.getJSONObject(j);

            TravelTimeEntity timeData = new TravelTimeEntity();

            timeData.setTitle(item.getString("title"));
            timeData.setCurrent(item.getInt("current"));
            timeData.setAverage(item.getInt("average"));
            timeData.setDistance(item.getString("distance") + " miles");
            timeData.setTravelTimeId(Integer.parseInt(item.getString("routeid")));
            timeData.setUpdated(item.getString("updated"));
            timeData.setStartLatitude(item.getDouble("startLatitude"));
            timeData.setStartLongitude(item.getDouble("startLongitude"));
            timeData.setEndLatitude(item.getDouble("endLatitude"));
            timeData.setEndLongitude(item.getDouble("endLongitude"));

            for (TravelTimeEntity starredTime : starred) {
                if (starredTime.getTravelTimeId().equals(timeData.getTravelTimeId())){
                    timeData.setIsStarred(1);
                }
            }

            times.add(timeData);
        }

        TravelTimeEntity[] timesArray = new TravelTimeEntity[times.size()];
        timesArray = times.toArray(timesArray);

        travelTimeDao.deleteAndInsertTransaction(timesArray);

        CacheEntity travelCache = new CacheEntity("travel_times", System.currentTimeMillis());
        getCacheRepository().setCacheTime(travelCache);

    }
}