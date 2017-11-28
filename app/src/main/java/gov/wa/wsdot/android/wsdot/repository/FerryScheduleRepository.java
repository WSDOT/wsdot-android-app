package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.MutableLiveData;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleDao;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class FerryScheduleRepository extends NetworkResourceRepository {

    private static String TAG = FerryScheduleRepository.class.getSimpleName();

    private final FerryScheduleDao ferryScheduleDao;
    private final AppExecutors appExecutors;

    @Inject
    FerryScheduleRepository(FerryScheduleDao ferryScheduleDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (15 * DateUtils.MINUTE_IN_MILLIS), "ferries_schedules");
        this.ferryScheduleDao = ferryScheduleDao;
        this.appExecutors = appExecutors;
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

        List<FerryScheduleEntity> starred;

        starred = ferryScheduleDao.getFavoriteFerrySchedules();

        URL url = new URL(APIEndPoints.FERRY_SCHEDULES);

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
            schedule.setUpdated(dateFormat.format(new Date(Long.parseLong(item
                    .getString("CacheDate").substring(6, 19)))));

            for (FerryScheduleEntity starredSchedule : starred) {
                if (starredSchedule.getFerryScheduleId() == schedule.getFerryScheduleId()){
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
