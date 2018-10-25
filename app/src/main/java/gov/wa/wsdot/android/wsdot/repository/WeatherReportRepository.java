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
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.WeatherReportDao;
import gov.wa.wsdot.android.wsdot.database.ferries.WeatherReportEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class WeatherReportRepository extends NetworkResourceSyncRepository {

    private static String TAG = WeatherReportRepository.class.getSimpleName();

    private final WeatherReportDao weatherReportDao;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm", Locale.US);

    @Inject
    WeatherReportRepository(WeatherReportDao weatherReportDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (5 * DateUtils.MINUTE_IN_MILLIS), "weather_report");
        this.weatherReportDao = weatherReportDao;
    }

    public LiveData<List<WeatherReportEntity>> getReports(MutableLiveData<ResourceStatus> status){
        super.refreshData(status, false);
        return weatherReportDao.loadWeatherReports();

    }

    public LiveData<List<WeatherReportEntity>> getReportsInRange(Date startDate, Date endDate, MutableLiveData<ResourceStatus> status){
        super.refreshData(status, false);
        return weatherReportDao.loadWeatherReportsBetween(startDate, endDate);

    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd-hh:mm");

        String weatherReportUrl =  APIEndPoints.WEATHER_REPORTS;

        URL url = new URL(weatherReportUrl);
        URLConnection urlConn = url.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONArray array = obj.getJSONObject("weather").getJSONArray("reports");

        List<WeatherReportEntity> reports = new ArrayList<>();

        int numItems = array.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = array.getJSONObject(j);

            WeatherReportEntity weatherReport = new WeatherReportEntity();

            weatherReport.setSource(item.getString("source"));
            weatherReport.setWindSpeed(item.getDouble("windSpeed"));
            weatherReport.setWindDirection(item.getDouble("windDirection"));
            weatherReport.setTemperature(item.getDouble("temperature"));
            weatherReport.setLatitude(item.getDouble("latitude"));
            weatherReport.setLongitude(item.getDouble("longitude"));
            weatherReport.setUpdated(item.getString("updated"));

            Log.e(TAG, String.format("Updated: %s", weatherReport.getUpdated()));

            Date date = dateFormat.parse(weatherReport.getUpdated());

            Log.e(TAG, dateFormat2.format(date));

            reports.add(weatherReport);
        }

        WeatherReportEntity[] reportsArray = new WeatherReportEntity[reports.size()];
        reportsArray = reports.toArray(reportsArray);

        weatherReportDao.deleteAndInsertTransaction(reportsArray);

        CacheEntity weatherReportsCache = new CacheEntity("weather_report", System.currentTimeMillis());
        getCacheRepository().setCacheTime(weatherReportsCache);

    }
}
