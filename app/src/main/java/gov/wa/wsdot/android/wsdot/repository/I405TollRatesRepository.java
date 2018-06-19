package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import android.util.TimeUtils;

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

import gov.wa.wsdot.android.wsdot.shared.I405TollRateSignItem;
import gov.wa.wsdot.android.wsdot.shared.I405TripItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class I405TollRatesRepository extends NetworkResourceRepository {

    private final static String TAG = I405TollRatesRepository.class.getSimpleName();

    private MutableLiveData<List<I405TollRateSignItem>> tollRates;

    @Inject
    public I405TollRatesRepository(AppExecutors appExecutors) {
        super(appExecutors);
        tollRates = new MutableLiveData<>();
    }

    public MutableLiveData<List<I405TollRateSignItem>> getTollRates(MutableLiveData<ResourceStatus> status) {
        return this.tollRates;
    }

    /*
        Builds Toll sign and trip items from API.
        Sign items are a collection of trips from the starting point
        held in the sign item. Trip items hold the actual toll rates
     */
    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        List<I405TollRateSignItem> mTollRateItems = new ArrayList<>();

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

            if (item.getInt("StateRoute") == 405) {

                I405TripItem trip = new I405TripItem();
                trip.setTripName(item.getString("TripName"));
                trip.setEndLocationName(item.getString("EndLocationName"));
                trip.setToll(item.getLong("CurrentToll"));
                trip.setMessage(item.getString("CurrentMessage"));
                trip.setEndLatitude(item.getDouble("EndLatitude"));
                trip.setEndLongitude(item.getDouble("EndLongitude"));

                try {
                    trip.setUpdatedAt(ParserUtils.relativeTime(
                            dateFormat.format(new Date(System.currentTimeMillis())),
                            "MMMM d, yyyy h:mm a",
                            false));

                } catch (Exception e) {
                    trip.setUpdatedAt("");
                    Log.e(TAG, "Error parsing date", e);
                }


                // Check if we've already found this starting point
                boolean foundSign = false;
                int i = -1;

                for (I405TollRateSignItem tollRate: mTollRateItems) {
                    i++;
                    if (tollRate.getStartLocationName().equals(item.getString("StartLocationName"))) {
                        mTollRateItems.get(i).getTrips().add(trip);
                        foundSign = true;
                    }
                }

                if (!foundSign){
                    I405TollRateSignItem tollRateSignItem = new I405TollRateSignItem();

                    tollRateSignItem.setStartLocationName(item.getString("StartLocationName"));
                    tollRateSignItem.setStateRoute(item.getInt("StateRoute"));
                    tollRateSignItem.setTravelDirection(item.getString("TravelDirection"));
                    tollRateSignItem.setStartLatitude(item.getDouble("StartLatitude"));
                    tollRateSignItem.setStartLongitude(item.getDouble("StartLongitude"));
                    tollRateSignItem.getTrips().add(trip);

                    mTollRateItems.add(tollRateSignItem);
                }


            }
        }
        tollRates.postValue(mTollRateItems);
    }
}