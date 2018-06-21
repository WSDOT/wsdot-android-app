package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.format.DateUtils;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroupDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateSignDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateSignEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollTripDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollTripEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class TollRatesRepository extends NetworkResourceSyncRepository {

    private final static String TAG = TollRatesRepository.class.getSimpleName();

    private final TollTripDao tollTripDao;
    private final TollRateSignDao tollRateSignDao;
    private final TollRateGroupDao tollRateGroupDao;

    @Inject
    public TollRatesRepository(TollTripDao tollTripDao,
                               TollRateSignDao tollRateSignDao,
                               TollRateGroupDao tollRateGroupDao,
                               AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, DateUtils.MINUTE_IN_MILLIS, "toll_trip");

        this.tollTripDao = tollTripDao;
        this.tollRateSignDao = tollRateSignDao;
        this.tollRateGroupDao = tollRateGroupDao;

    }

    public LiveData<List<TollRateGroup>> loadI405TollRateGroups(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return tollRateGroupDao.loadI405TollRateGroups();
    }

    public List<TollRateGroup> getI405TollRateGroups(MutableLiveData<ResourceStatus> status) {
        super.refreshDataOnSameThread(status, false);
        return tollRateGroupDao.getI405TollRateGroups();
    }

    public LiveData<List<TollRateGroup>> loadFavoriteTolls(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return tollRateGroupDao.loadFavoriteTollRateGroups();
    }

    public void setIsStarred(String location_name, Integer isStarred) {
        getExecutor().diskIO().execute(() -> {
            tollRateSignDao.updateIsStarred(location_name, isStarred);
        });
    }

    /**
     * Builds Toll sign and trip items from API.
     * A toll sign item holds the location and favorite status.
     *
     * sign items and trip items are group together upon retrieval from Room by
     * the sign's location and the trip's start location
     *
    */
    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

        List<TollRateSignEntity> mTollRateItems = new ArrayList<>();
        List<TollTripEntity> mTollTrips = new ArrayList<>();

        URL url;

        url = new URL(APIEndPoints.I405_TOLL_RATES + "?accesscode=" + APIEndPoints.WSDOT_API_KEY);
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

            TollTripEntity trip = new TollTripEntity();

            trip.setTripName(item.getString("TripName"));
            trip.setEndLocationName(item.getString("EndLocationName"));
            trip.setSignId(item.getString("StartLocationName").concat(item.getString("TravelDirection")));
            trip.setTollRate(item.getDouble("CurrentToll"));
            trip.setMessage(item.getString("CurrentMessage"));
            trip.setEndLatitude(item.getDouble("EndLatitude"));
            trip.setEndLongitude(item.getDouble("EndLongitude"));

            try {
                trip.setUpdated(ParserUtils.relativeTime(
                        dateFormat.format(new Date(System.currentTimeMillis())),
                        "MMMM d, yyyy h:mm a",
                        false));
            } catch (Exception e) {
                trip.setUpdated("unavailable");
                Log.e(TAG, "Error parsing date", e);
            }

            mTollTrips.add(trip);

            TollRateSignEntity sign = tollRateSignDao.getTollRateSign(trip.getSignId());

            if (sign == null){

                sign = new TollRateSignEntity();

                sign.setId(item.getString("StartLocationName").concat(item.getString("TravelDirection")));
                sign.setLocationName(item.getString("StartLocationName"));
                sign.setStateRoute(item.getInt("StateRoute"));
                sign.setTravelDirection(item.getString("TravelDirection"));
                sign.setStartLatitude(item.getDouble("StartLatitude"));
                sign.setStartLongitude(item.getDouble("StartLongitude"));

            }

            mTollRateItems.add(sign);
        }


        TollRateSignEntity[] tollRateSignsArray = new TollRateSignEntity[mTollRateItems.size()];
        tollRateSignsArray = mTollRateItems.toArray(tollRateSignsArray);

        tollRateSignDao.deleteAndInsertTransaction(tollRateSignsArray);

        TollTripEntity[] tollTripsArray = new TollTripEntity[mTollTrips.size()];
        tollTripsArray = mTollTrips.toArray(tollTripsArray);

        tollTripDao.deleteAndInsertTransaction(tollTripsArray);

        CacheEntity tollCache = new CacheEntity("toll_trip", System.currentTimeMillis());
        getCacheRepository().setCacheTime(tollCache);

    }
}