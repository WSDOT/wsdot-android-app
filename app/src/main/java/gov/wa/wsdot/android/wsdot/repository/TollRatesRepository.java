package gov.wa.wsdot.android.wsdot.repository;

import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroup;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroupDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateSignDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateSignEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollTripDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.TollTripEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

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

    public LiveData<List<TollRateGroup>> loadSR167TollRateGroups(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return tollRateGroupDao.loadSR167TollRateGroups();
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

            if (!shouldSkipTrip(item)) {

                trip.setTripName(item.getString("TripName"));
                trip.setEndLocationName(item.getString("EndLocationName"));
                trip.setSignId(item.getString("StartLocationName").concat(item.getString("TravelDirection")));
                trip.setTollRate(item.getDouble("CurrentToll"));
                trip.setMessage(item.getString("CurrentMessage"));
                trip.setEndMilepost(item.getInt("EndMilepost"));
                trip.setEndLatitude(item.getDouble("EndLatitude"));
                trip.setEndLongitude(item.getDouble("EndLongitude"));

                try {
                    trip.setUpdated(dateFormat.format(new Date(System.currentTimeMillis())));
                } catch (Exception e) {
                    trip.setUpdated("unavailable");
                    Log.e(TAG, "Error parsing date", e);
                }

                mTollTrips.add(trip);

                TollRateSignEntity sign = tollRateSignDao.getTollRateSign(trip.getSignId());

                if (sign == null) {
                    sign = new TollRateSignEntity();
                }
                sign.setId(item.getString("StartLocationName").concat(item.getString("TravelDirection")));

                String locationName = item.getString("StartLocationName");

                if (item.getInt("StateRoute") == 405){
                    locationName = filter405LocationName(item.getString("StartLocationName"), item.getString("TravelDirection"));
                } else if (item.getInt("StateRoute") == 167){
                    locationName = filter167LocationName(item.getString("StartLocationName"), item.getString("TravelDirection"));
                }

                sign.setLocationName(locationName);
                sign.setMilepost(item.getInt("StartMilepost"));
                sign.setStateRoute(item.getInt("StateRoute"));
                sign.setTravelDirection(item.getString("TravelDirection"));
                sign.setStartLatitude(item.getDouble("StartLatitude"));
                sign.setStartLongitude(item.getDouble("StartLongitude"));

                mTollRateItems.add(sign);
            }
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

    private String filter405LocationName(String locationName, String direction){

        // Southbound name changes suggested by Tolling
        if (direction.equals("S")) {

            if (locationName.equals("231st SE")) {
                locationName = "SR 527";
            }

            if (locationName.equals("NE 53rd")) {
                locationName = "NE 70th Place";
            }

        }

        // Northbound name changes suggested by Tolling
        if (direction.equals("N")) {

            if (locationName.equals("NE 97th")) {
                locationName = "NE 85th St";
            }

            if (locationName.equals("231st SE")) {
                locationName = "SR 522";
            }

            if (locationName.equals("216th SE")) {
                locationName = "SR 527";
            }
        }

        if (locationName.equals("SR 524") || locationName.equals("NE 4th")){
            locationName = (direction.equals("N") ? "Bellevue" : "Lynnwood").concat(" - Start of toll lanes");
        } else {
            locationName = "Lane entrance near ".concat(locationName);
        }

        return locationName;
    }


    private String filter167LocationName(String locationName, String direction){

        // Southbound name changes suggested by Tolling
        if (direction.equals("S")) {

            if (locationName.equals("4th Ave N")) {
                locationName = "SR 516";
            }

            if (locationName.equals("S 192nd St")) {
                locationName = "S 180th St";
            }

            if (locationName.equals("S 23rd St")) {
                locationName = "I-405 (Renton)";
            }
        }

        // Northbound name changes suggested by Tolling
        if (direction.equals("N")) {

            if (locationName.equals("15th St SW")) {
                locationName = "SR 18 (Auburn)";
            }

            if (locationName.equals("7th St NW")) {
                locationName = "15th St SW";
            }

            if (locationName.equals("30th St NW")) {
                locationName = "S 277th St";
            }

            if (locationName.equals("S 265th St")) {
                locationName = "SR 516";
            }
        }

        locationName = "Lane entrance near ".concat(locationName);

        return locationName;
    }

    private boolean shouldSkipTrip(JSONObject tripJson) throws JSONException {

        /*
         * Removal of these routes since their displays are already shown
         * by other signs from the API.
         */
        if (tripJson.getString("StartLocationName").equals("NE 6th")
            && tripJson.getString("TravelDirection").equals("N")) {
            return true;
        }

        if (tripJson.getString("StartLocationName").equals("216th ST SE")
                && tripJson.getString("TravelDirection").equals("S")) {
            return true;
        }

        if (tripJson.getString("StartLocationName").equals("NE 145th")
                && tripJson.getString("TravelDirection").equals("S")) {
            return true;
        }

        /*
         * Removal suggested by tolling division since it's very similar to another location
         * and difficult to come up with a label people will recognize.
         */
        if (tripJson.getString("StartLocationName").equals("NE 108th")
                && tripJson.getString("TravelDirection").equals("S")) {
            return true;
        }

        // SR 167 trips to remove
        if (tripJson.getString("StartLocationName").equals("James St")
                && tripJson.getString("TravelDirection").equals("N")) {
            return true;
        }

        if (tripJson.getString("StartLocationName").equals("S 204th St")
                && tripJson.getString("TravelDirection").equals("N")) {
            return true;
        }

        if (tripJson.getString("StartLocationName").equals("1st Ave S")
                && tripJson.getString("TravelDirection").equals("S")) {
            return true;
        }

        if (tripJson.getString("StartLocationName").equals("12th St NW")
                && tripJson.getString("TravelDirection").equals("S")) {
            return true;
        }

        if (tripJson.getString("StartLocationName").equals("37th St NW")
                && tripJson.getString("TravelDirection").equals("S")) {
            return true;
        }

        if (tripJson.getString("StartLocationName").equals("Green River")
                && tripJson.getString("TravelDirection").equals("S")) {
            return true;
        }

        return false;
    }
}