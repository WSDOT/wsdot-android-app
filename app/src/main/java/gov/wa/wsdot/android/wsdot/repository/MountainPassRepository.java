package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.VisibleForTesting;
import android.text.format.DateUtils;
import android.util.Log;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassDao;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class MountainPassRepository extends NetworkResourceRepository {

    private static String TAG = MountainPassRepository.class.getSimpleName();

    private final MountainPassDao mountainPassDao;

    @Inject
    public MountainPassRepository(MountainPassDao mountainPassDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (15 * DateUtils.MINUTE_IN_MILLIS), "mountain_passes");
        this.mountainPassDao = mountainPassDao;
    }

    public LiveData<List<MountainPassEntity>> getMountainPasses(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return mountainPassDao.loadMountainPasses();
    }

    public LiveData<MountainPassEntity> getMountainPassFor(Integer id, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return mountainPassDao.loadMountainPassFor(id);
    }

    @Override
    @VisibleForTesting
    public void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        DateFormat parseDateFormat = new SimpleDateFormat("yyyy,M,d,HH,m"); //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]
        DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

        List<MountainPassEntity> starred = new ArrayList<>();

        starred = mountainPassDao.getFavoriteMountainPasses();

        URL url = new URL(APIEndPoints.PASS_CONDITIONS);
        URLConnection urlConn = url.openConnection();

        BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
        GZIPInputStream gzin = new GZIPInputStream(bis);
        InputStreamReader is = new InputStreamReader(gzin);
        BufferedReader in = new BufferedReader(is);

        String mDateUpdated = "";
        String jsonFile = "";
        String line;
        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONObject obj = new JSONObject(jsonFile);
        JSONObject result = obj.getJSONObject("GetMountainPassConditionsResult");
        JSONArray passConditions = result.getJSONArray("PassCondition");
        String weatherCondition;
        String weather_image_name;
        String forecast_weather_image_name;
        List<MountainPassEntity> passes = new ArrayList<>();

        int numConditions = passConditions.length();
        for (int j=0; j < numConditions; j++) {
            JSONObject pass = passConditions.getJSONObject(j);
            MountainPassEntity passData = new MountainPassEntity();
            weatherCondition = pass.getString("WeatherCondition");
            weather_image_name = getWeatherImage(WeatherPhrases.weatherPhrasesDay, weatherCondition);

            String tempDate = pass.getString("DateUpdated");

            try {
                tempDate = tempDate.replace("[", "");
                tempDate = tempDate.replace("]", "");

                String[] a = tempDate.split(",");
                StringBuilder sb = new StringBuilder();
                for (int m=0; m < 5; m++) {
                    sb.append(a[m]);
                    sb.append(",");
                }

                tempDate = sb.toString().trim();
                tempDate = tempDate.substring(0, tempDate.length()-1);

                parseDateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                Date date = parseDateFormat.parse(tempDate);

                mDateUpdated = displayDateFormat.format(date);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + tempDate, e);
                mDateUpdated = "N/A";
            }

            JSONArray forecasts = pass.getJSONArray("Forecast");
            JSONArray forecastItems = new JSONArray();

            int numForecasts = forecasts.length();
            for (int l=0; l < numForecasts; l++) {
                JSONObject forecast = forecasts.getJSONObject(l);

                if (isNight(forecast.getString("Day"))) {
                    forecast_weather_image_name = getWeatherImage(WeatherPhrases.weatherPhrasesNight, forecast.getString("ForecastText"));
                } else {
                    forecast_weather_image_name = getWeatherImage(WeatherPhrases.weatherPhrasesDay, forecast.getString("ForecastText"));
                }

                forecast.put("weather_icon", forecast_weather_image_name);

                if (l == 0) {
                    if (weatherCondition.equals("")) {
                        weatherCondition = forecast.getString("ForecastText").split("\\.")[0] + ".";
                        weather_image_name = forecast_weather_image_name;
                    }
                }
                forecastItems.put(forecast);
            }

            passData.setPassId(pass.getInt("MountainPassId"));
            passData.setName(pass.getString("MountainPassName"));
            passData.setWeatherIcon(weather_image_name);
            passData.setForecast(forecastItems.toString());
            passData.setWeatherCondition(weatherCondition);
            passData.setDateUpdated(mDateUpdated);
            passData.setCamera(pass.getString("Cameras"));
            passData.setElevation(pass.getString("Cameras"));
            passData.setTravelAdisory(pass.getString("TravelAdvisoryActive"));
            passData.setRoadCondition(pass.getString("RoadCondition"));
            passData.setTemperature(pass.getString("TemperatureInFahrenheit"));
            JSONObject restrictionOne = pass.getJSONObject("RestrictionOne");
            passData.setRestrictionOne(restrictionOne.getString("RestrictionText"));
            passData.setRestrictionOneDirection(restrictionOne.getString("TravelDirection"));
            JSONObject restrictionTwo = pass.getJSONObject("RestrictionTwo");
            passData.setRestrictionTwo(restrictionTwo.getString("RestrictionText"));
            passData.setRestrictionTwoDirection(restrictionTwo.getString("TravelDirection"));
            passData.setLatitude(pass.getDouble("Latitude"));
            passData.setLongitude(pass.getDouble("Longitude"));

            for (MountainPassEntity starredPass : starred) {
                if (starredPass.getPassId() == passData.getPassId()){
                    passData.setIsStarred(1);
                }
            }

            passes.add(passData);

        }

        MountainPassEntity[] passesArray = new MountainPassEntity[passes.size()];
        passesArray = passes.toArray(passesArray);

        mountainPassDao.deleteAndInsertTransaction(passesArray);

        CacheEntity passCache = new CacheEntity("mountain_passes", System.currentTimeMillis());
        getCacheRepository().setCacheTime(passCache);

    }

    private static String getWeatherImage(HashMap<String, String[]> weatherPhrases, String weather) {
        String image_name = "weather_na";
        Set<Map.Entry<String, String[]>> weatherSet = weatherPhrases.entrySet();
        Iterator<Map.Entry<String, String[]>> i = weatherSet.iterator();

        if (weather.equals("")) return image_name;

        String[] forecastArr = weather.split("\\.");

        for (String forecastPart: forecastArr) {
            while(i.hasNext()) {
                Map.Entry<String, String[]> me = i.next();
                for (String phrase: me.getValue()) {
                    if (forecastPart.toLowerCase().trim().startsWith(phrase)) {
                        image_name = me.getKey();
                        return image_name;
                    }
                }
            }
            i = weatherSet.iterator();
        }
        return image_name;
    }

    private static boolean isNight(String text) {
        String patternStr = "night|tonight";
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        boolean matchFound = matcher.find();

        return matchFound;
    }

    private static class WeatherPhrases {
        static HashMap<String, String[]> weatherPhrasesNight = new HashMap<>();
        static HashMap<String, String[]> weatherPhrasesDay = new HashMap<>();

        WeatherPhrases(){

            String[] weather_clear = {"fair", "sunny", "clear"};
            String[] weather_few_clouds = {"few clouds", "scattered clouds", "scattered clouds", "mostly sunny", "mostly clear"};
            String[] weather_partly_cloudy = {"partly cloudy", "partly sunny"};
            String[] weather_cloudy = {"cloudy", "increasing clouds"};
            String[] weather_mostly_cloudy = {"broken", "mostly cloudy"};
            String[] weather_overcast = {"overcast"};
            String[] weather_light_rain = {"light rain", "showers"};
            String[] weather_rain = {"rain", "heavy rain", "raining"};
            String[] weather_snow = {"snow", "snowing", "light snow", "heavy snow"};
            String[] weather_fog = {"fog"};
            String[] weather_sleet = {"rain snow", "light rain snow", "heavy rain snow", "rain and snow"};
            String[] weather_hail = {"ice pellets", "light ice pellets", "heavy ice pellets", "hail"};
            String[] weather_thunderstorm = {"thunderstorm", "thunderstorms"};

            weatherPhrasesDay.put("ic_list_sunny", weather_clear);
            weatherPhrasesDay.put("ic_list_cloudy_1", weather_few_clouds);
            weatherPhrasesDay.put("ic_list_cloudy_2", weather_partly_cloudy);
            weatherPhrasesDay.put("ic_list_cloudy_3", weather_cloudy);
            weatherPhrasesDay.put("ic_list_cloudy_4", weather_mostly_cloudy);
            weatherPhrasesDay.put("ic_list_overcast", weather_overcast);
            weatherPhrasesDay.put("ic_list_light_rain", weather_light_rain);
            weatherPhrasesDay.put("ic_list_shower_3", weather_rain);
            weatherPhrasesDay.put("ic_list_snow_4", weather_snow);
            weatherPhrasesDay.put("ic_list_fog", weather_fog);
            weatherPhrasesDay.put("ic_list_sleet", weather_sleet);
            weatherPhrasesDay.put("ic_list_hail", weather_hail);
            weatherPhrasesDay.put("ic_list_tstorm_3", weather_thunderstorm);

            weatherPhrasesNight.put("ic_list_sunny_night", weather_clear);
            weatherPhrasesNight.put("ic_list_cloudy_1_night", weather_few_clouds);
            weatherPhrasesNight.put("ic_list_cloudy_2_night", weather_partly_cloudy);
            weatherPhrasesNight.put("ic_list_cloudy_3_night", weather_cloudy);
            weatherPhrasesNight.put("ic_list_cloudy_4_night", weather_mostly_cloudy);
            weatherPhrasesNight.put("ic_list_overcast", weather_overcast);
            weatherPhrasesNight.put("ic_list_light_rain", weather_light_rain);
            weatherPhrasesNight.put("ic_list_shower_3", weather_rain);
            weatherPhrasesNight.put("ic_list_snow_4", weather_snow);
            weatherPhrasesNight.put("ic_list_fog_night", weather_fog);
            weatherPhrasesNight.put("ic_list_sleet", weather_sleet);
            weatherPhrasesNight.put("ic_list_hail", weather_hail);
            weatherPhrasesNight.put("ic_list_tstorm_3", weather_thunderstorm);
        }
    }
}
