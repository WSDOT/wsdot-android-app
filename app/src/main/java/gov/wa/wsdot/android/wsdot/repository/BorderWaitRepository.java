package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.text.format.DateUtils;
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

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitDao;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;

@Singleton
public class BorderWaitRepository {

    private static String TAG = BorderWaitRepository.class.getSimpleName();

    private final AppExecutors appExecutors;
    private final BorderWaitDao borderWaitDao;
    private final CacheRepository cacheRepository;

    @Inject
    public BorderWaitRepository(BorderWaitDao borderWaitDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        this.borderWaitDao = borderWaitDao;
        this.appExecutors = appExecutors;
        this.cacheRepository = cacheRepository;
    }

    public LiveData<List<BorderWaitEntity>> getBorderWaitsFor(String direction) {

        appExecutors.diskIO().execute(() -> {
                    CacheEntity cache = cacheRepository.getCacheTimeFor("border_wait");
                    long now = System.currentTimeMillis();
                    Boolean shouldUpdate = (Math.abs(now - cache.getLastUpdated()) > (15 * DateUtils.MINUTE_IN_MILLIS));
                    if (shouldUpdate) {
                        refreshBorderWaits();
                    }
                });

        // return a LiveData directly from the database.
        return borderWaitDao.loadBorderWaitsFor(direction);
    }

    public void refreshBorderWaits() {

            try {
                URL url = new URL(APIEndPoints.BORDER_WAITS);
                URLConnection urlConn = url.openConnection();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String jsonFile = "";
                String line;

                while ((line = in.readLine()) != null)
                    jsonFile += line;
                in.close();

                JSONObject obj = new JSONObject(jsonFile);
                JSONObject result = obj.getJSONObject("waittimes");
                JSONArray items = result.getJSONArray("items");

                List<BorderWaitEntity> waits = new ArrayList<>();

                int numItems = items.length();

                for (int j=0; j < numItems; j++) {
                    JSONObject item = items.getJSONObject(j);

                    BorderWaitEntity wait = new BorderWaitEntity();

                    wait.setBorderWaitId(item.getInt("id"));
                    wait.setTitle(item.getString("name"));
                    wait.setDirection(item.getString("direction"));
                    wait.setLane(item.getString("lane"));
                    wait.setRoute(item.getInt("route"));
                    wait.setWait(item.getInt("wait"));
                    wait.setUpdated(item.getString("updated"));
                    wait.setIsStarred(0);

                    waits.add(wait);
                }

                BorderWaitEntity[] waitsArray = new BorderWaitEntity[waits.size()];
                waitsArray = waits.toArray(waitsArray);

                borderWaitDao.deleteAndInsertTransaction(waitsArray);

                CacheEntity borderCache = new CacheEntity("border_wait", System.currentTimeMillis());
                cacheRepository.setCacheTime(borderCache);

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }

    }
}
