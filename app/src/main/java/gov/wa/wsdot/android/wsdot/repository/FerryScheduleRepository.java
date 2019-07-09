package gov.wa.wsdot.android.wsdot.repository;

import android.os.Build;
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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleDao;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class FerryScheduleRepository extends NetworkResourceSyncRepository {

    private static String TAG = FerryScheduleRepository.class.getSimpleName();

    private final FerryScheduleDao ferryScheduleDao;

    @Inject
    FerryScheduleRepository(FerryScheduleDao ferryScheduleDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (15 * DateUtils.MINUTE_IN_MILLIS), "ferries_schedules");
        this.ferryScheduleDao = ferryScheduleDao;
    }

    public LiveData<List<FerryScheduleEntity>> loadFerrySchedules(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return ferryScheduleDao.loadFerrySchedules();
    }

    public List<FerryScheduleEntity> getFerrySchedules(MutableLiveData<ResourceStatus> status) {
        super.refreshDataOnSameThread(status, false);
        return ferryScheduleDao.getFerrySchedules();
    }

    public LiveData<FerryScheduleEntity> loadFerryScheduleFor(Integer id, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return ferryScheduleDao.loadScheduleFor(id);
    }

    public LiveData<List<FerryScheduleEntity>> loadFavoriteSchedules(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return ferryScheduleDao.loadFavoriteFerrySchedules();
    }

    public void setIsStarred(Integer id, Integer isStarred) {
        getExecutor().diskIO().execute(() -> {
            ferryScheduleDao.updateIsStarred(id, isStarred);
        });
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        List<FerryScheduleEntity> starred;

        starred = ferryScheduleDao.getFavoriteFerrySchedules();

        String urlString = APIEndPoints.FERRY_SCHEDULES;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            urlString = urlString.replace("https:", "http:");
        }

        URL url = new URL(urlString);

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

        JSONArray items = new JSONArray(jsonFile);
        List<FerryScheduleEntity> schedules = new ArrayList<>();

        int numItems = items.length();
        for (int i=0; i < numItems; i++) {
            JSONObject item = items.getJSONObject(i);
            FerryScheduleEntity schedule = new FerryScheduleEntity();

            schedule.setFerryScheduleId(item.getInt("RouteID"));
            schedule.setTitle(item.getString("Description"));
            schedule.setCrossingTime(item.getString("CrossingTime"));
            schedule.setDate(item.getString("Date"));
            schedule.setAlert(item.getString("RouteAlert"));
            schedule.setUpdated(item.getString("CacheDate"));

            for (FerryScheduleEntity starredSchedule : starred) {
                if (starredSchedule.getFerryScheduleId().equals(schedule.getFerryScheduleId())) {
                    schedule.setIsStarred(1);
                }
            }

            schedules.add(schedule);
        }

        FerryScheduleEntity[] schedulesArray = new FerryScheduleEntity[schedules.size()];
        schedulesArray = schedules.toArray(schedulesArray);

        ferryScheduleDao.deleteAndInsertTransaction(schedulesArray);

        CacheEntity scheduleCache = new CacheEntity("ferries_schedules", System.currentTimeMillis());
        getCacheRepository().setCacheTime(scheduleCache);
    }
}
