package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.Nullable;
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
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertDao;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;


@Singleton
public class HighwayAlertRepository {

    private static String TAG = BorderWaitRepository.class.getSimpleName();

    private final AppExecutors appExecutors;
    private final HighwayAlertDao highwayAlertDao;
    private final CacheRepository cacheRepository;

    private Exception e = null;

    @Inject
    public HighwayAlertRepository(HighwayAlertDao highwayAlertDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        this.highwayAlertDao = highwayAlertDao;
        this.appExecutors = appExecutors;
        this.cacheRepository = cacheRepository;
    }

    public LiveData<List<HighwayAlertEntity>> getHighwayAlerts(MutableLiveData<ResourceStatus> status) {
        refreshData(status, false);
        return highwayAlertDao.loadHighwayAlerts();
    }

    public LiveData<List<HighwayAlertEntity>> getHighwayAlertsFor(String priority, MutableLiveData<ResourceStatus> status) {
        refreshData(status, false);
        return highwayAlertDao.loadHighwayAlertsWith(priority);
    }

    public void refreshData(MutableLiveData<ResourceStatus> status, Boolean forceRefresh){

        appExecutors.diskIO().execute(() -> {

            status.postValue(ResourceStatus.loading());

            CacheEntity cache = cacheRepository.getCacheTimeFor("highway_alerts");
            long now = System.currentTimeMillis();
            Log.e(TAG, String.valueOf(cache.getLastUpdated()));
            Boolean shouldUpdate = (Math.abs(now - cache.getLastUpdated()) > (5 * DateUtils.MINUTE_IN_MILLIS));
            if (shouldUpdate || forceRefresh) {

                fetchHighwayAlerts();
                appExecutors.mainThread().execute(() -> {
                    if (e != null) {
                        status.postValue(ResourceStatus.error("network error"));
                    } else {
                        status.postValue(ResourceStatus.success());
                        e = null;
                    }
                });
            }else{
                status.postValue(ResourceStatus.success());
            }
        });
    }

    private void fetchHighwayAlerts() {
        appExecutors.networkIO().execute(() -> {
            try {
                URL url = new URL(APIEndPoints.HIGHWAY_ALERTS);
                URLConnection urlConn = url.openConnection();

                DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String jsonFile = "";
                String line;

                while ((line = in.readLine()) != null) {
                    jsonFile += line;
                }

                in.close();

                JSONObject obj = new JSONObject(jsonFile);
                JSONObject result = obj.getJSONObject("alerts");
                JSONArray items = result.getJSONArray("items");

                List<HighwayAlertEntity> highwayAlerts = new ArrayList<>();

                int numItems = items.length();
                for (int j = 0; j < numItems; j++) {
                    JSONObject item = items.getJSONObject(j);

                    JSONObject startRoadwayLocation = item.getJSONObject("StartRoadwayLocation");
                    JSONObject endRoadwayLocation = item.getJSONObject("EndRoadwayLocation");

                    HighwayAlertEntity alert = new HighwayAlertEntity();
                    alert.setAlertId(item.getInt("AlertID"));
                    alert.setHeadline(item.getString("HeadlineDescription"));
                    alert.setCategory(item.getString("EventCategory"));
                    alert.setPriority(item.getString("Priority"));
                    alert.setStartLatitude(startRoadwayLocation.getDouble("Latitude"));
                    alert.setStartLongitude(startRoadwayLocation.getDouble("Longitude"));
                    alert.setEndLatitude(endRoadwayLocation.getDouble("Latitude"));
                    alert.setEndLongitude(endRoadwayLocation.getDouble("Longitude"));
                    alert.setRoadName(startRoadwayLocation.getString("RoadName"));
                    alert.setLastUpdated(dateFormat.format(new Date(Long.parseLong(item
                            .getString("LastUpdatedTime").substring(6, 19)))));

                    highwayAlerts.add(alert);
                }

                HighwayAlertEntity[] alertsArray = new HighwayAlertEntity[highwayAlerts.size()];
                alertsArray = highwayAlerts.toArray(alertsArray);

                highwayAlertDao.deleteAndInsertTransaction(alertsArray);

                CacheEntity cache = new CacheEntity("highway_alerts", System.currentTimeMillis());
                cacheRepository.setCacheTime(cache);

            } catch (Exception e) {
                this.e = e;
                Log.e(TAG, e.toString());
                Log.e(TAG, "Error: " + e.getMessage());
            }
        });
    }
}
