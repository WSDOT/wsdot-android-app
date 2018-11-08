package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeDao;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroupDao;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeTripDao;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeTripEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class TravelTimeRepository  extends NetworkResourceSyncRepository {

    private static String TAG = TravelTimeRepository.class.getSimpleName();

    private final TravelTimeDao travelTimeDao;
    private final TravelTimeTripDao travelTimeTripDao;
    private final TravelTimeGroupDao travelTimeGroupDao;

    @Inject
    public TravelTimeRepository(TravelTimeDao travelTimeDao,
                                TravelTimeTripDao travelTimeTripDao,
                                TravelTimeGroupDao travelTimeGroupDao,
                                AppExecutors appExecutors, CacheRepository cacheRepository) {

        super(appExecutors, cacheRepository, (15 * DateUtils.MINUTE_IN_MILLIS), "travel_times");
        this.travelTimeDao = travelTimeDao;
        this.travelTimeTripDao = travelTimeTripDao;
        this.travelTimeGroupDao = travelTimeGroupDao;
    }

    public LiveData<List<TravelTimeGroup>> loadTravelTimeGroups(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeGroupDao.loadTravelTimeGroups();
    }

    public List<TravelTimeGroup> getTravelTimeGroups(MutableLiveData<ResourceStatus> status) {
        super.refreshDataOnSameThread(status, false);
        return travelTimeGroupDao.getTravelTimeGroups();
    }

    public LiveData<List<TravelTimeGroup>> queryTravelTimeGroups(String query, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeGroupDao.queryTravelTimeGroups(query);
    }

    /*

    public LiveData<TravelTimeEntity> getTravelTimeFor(Integer id, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeDao.loadTravelTimeFor(id);
    }

    */

    public LiveData<List<TravelTimeEntity>> getTravelTimesWithIds(List<Integer> ids, MutableLiveData<ResourceStatus> status) {

        super.refreshData(status, false);
        return travelTimeDao.loadTravelTimesFor(ids);
    }

    public LiveData<List<TravelTimeGroup>> loadFavoriteTravelTimes(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return travelTimeGroupDao.loadFavoriteTravelTimeGroups();
    }

    public void setIsStarred(String title, Integer isStarred) {
        getExecutor().diskIO().execute(() -> {
            travelTimeTripDao.updateIsStarred(title, isStarred);
        });
    }

    @Override
    public void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        List<TravelTimeTripEntity> trips = new ArrayList<>();
        List<TravelTimeEntity> times = new ArrayList<>();

        URL url = new URL(APIEndPoints.TRAVEL_TIMES);
        URLConnection urlConn = url.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        StringBuilder jsonFile = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            jsonFile.append(line);
        }
        in.close();

        JSONArray items = new JSONArray(jsonFile.toString());

        int numItems = items.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = items.getJSONObject(j);

            TravelTimeEntity timeData = new TravelTimeEntity();

            timeData.setTripTitle(item.getString("title"));
            timeData.setVia(item.getString("via"));
            timeData.setCurrent(item.getInt("current_time"));
            timeData.setStatus(item.getString("status"));
            timeData.setAverage(item.getInt("avg_time"));
            timeData.setDistance(item.getString("miles") + " miles");
            timeData.setTravelTimeId(Integer.parseInt(item.getString("travel_time_id")));
            timeData.setUpdated(item.getString("updated"));
            timeData.setStartLatitude(item.getDouble("startLocationLatitude"));
            timeData.setStartLongitude(item.getDouble("startLocationLongitude"));
            timeData.setEndLatitude(item.getDouble("endLocationLatitude"));
            timeData.setEndLongitude(item.getDouble("endLocationLongitude"));

            times.add(timeData);

            TravelTimeTripEntity trip = travelTimeTripDao.getTravelTimeTrip(timeData.getTripTitle());

            if (trip == null){
                trip = new TravelTimeTripEntity();
                trip.setTitle(timeData.getTripTitle());
            }

            trips.add(trip);
        }

        TravelTimeTripEntity[] tripsArray = new TravelTimeTripEntity[trips.size()];
        tripsArray = trips.toArray(tripsArray);

        travelTimeTripDao.deleteAndInsertTransaction(tripsArray);

        TravelTimeEntity[] timesArray = new TravelTimeEntity[times.size()];
        timesArray = times.toArray(timesArray);

        travelTimeDao.deleteAndInsertTransaction(timesArray);

        CacheEntity travelCache = new CacheEntity("travel_times", System.currentTimeMillis());
        getCacheRepository().setCacheTime(travelCache);

    }
}