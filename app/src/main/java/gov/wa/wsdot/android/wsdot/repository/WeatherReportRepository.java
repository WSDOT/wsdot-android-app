package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
import java.text.ParseException;
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
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.Utils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class WeatherReportRepository extends NetworkResourceSyncRepository {

    private static String TAG = WeatherReportRepository.class.getSimpleName();

    private final WeatherReportDao weatherReportDao;

    // Date format from weather report
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm", Locale.US);

    @Inject
    WeatherReportRepository(WeatherReportDao weatherReportDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (5 * DateUtils.MINUTE_IN_MILLIS), "weather_report");
        this.weatherReportDao = weatherReportDao;
    }

    public LiveData<List<WeatherReportEntity>> getReports(MutableLiveData<ResourceStatus> status){
        super.refreshData(status, true);
        return weatherReportDao.loadWeatherReports();
    }

    public LiveData<List<WeatherReportEntity>> getReportsInRange(Date startDate, Date endDate, MutableLiveData<ResourceStatus> status){
        super.refreshData(status, false);
        return weatherReportDao.loadWeatherReportsBetween(dateFormat.format(startDate), dateFormat.format(endDate));
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        String weatherReportUrl = APIEndPoints.WEATHER_REPORTS;

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

            if (!item.isNull("windSpeed")) {
                weatherReport.setWindSpeed(item.getDouble("windSpeed"));
            } else {
                weatherReport.setWindSpeed(null);
            }

            if (!item.isNull("windDirection")) {
                weatherReport.setWindDirection(item.getDouble("windDirection"));
            } else {
                weatherReport.setWindDirection(null);
            }

            if (!item.isNull("temperature")) {
                weatherReport.setTemperature(item.getDouble("temperature"));
            } else {
                weatherReport.setTemperature(null);
            }

            weatherReport.setLatitude(item.getDouble("latitude"));
            weatherReport.setLongitude(item.getDouble("longitude"));
            weatherReport.setUpdated(item.getString("updated"));

            weatherReport.setReport(formatTime(weatherReport.getUpdated())
                    + (weatherReport.getWindSpeed() != null ? "<br><br><b>Wind Speed:</b> " + weatherReport.getWindSpeed() + " mph" : null)
                    + (weatherReport.getWindDirection() != null ? "<br><br><b>Wind Direction:</b> " + Utils.headingToHeadtxt(weatherReport.getWindDirection()) : null)
                    + (weatherReport.getTemperature() != null ? "<br><br><b>Temperature:</b> " + weatherReport.getTemperature() + "°F" : null));

            reports.add(weatherReport);
        }

        WeatherReportEntity[] reportsArray = new WeatherReportEntity[reports.size()];
        reportsArray = reports.toArray(reportsArray);

        weatherReportDao.deleteAndInsertTransaction(reportsArray);

        CacheEntity weatherReportsCache = new CacheEntity("weather_report", System.currentTimeMillis());
        getCacheRepository().setCacheTime(weatherReportsCache);

    }

    /**
     * Formats the time string
     *
     * @param time time string from data
     * @return Formatted time string.
     * @throws ParseException
     */
    private static String formatTime(String time) throws ParseException {
        DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        DateFormat sourceDateFormat = new SimpleDateFormat("yyyyMMddhhmm");
        return displayDateFormat.format(sourceDateFormat.parse(time));
    }

}
