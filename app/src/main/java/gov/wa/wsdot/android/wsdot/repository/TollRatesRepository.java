package gov.wa.wsdot.android.wsdot.repository;

import android.text.format.DateUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.util.Converters;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.TollRateTable;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.TollRateTableDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.TollRateTableDataDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.TollRateTableDataEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows.TollRowDao;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows.TollRowEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class TollRatesRepository extends NetworkResourceSyncRepository {

    private final static String TAG = TollRatesRepository.class.getSimpleName();

    private final TollRateTableDao tollRateTableDao;
    private final TollRateTableDataDao tollRateTableDataDao;
    private final TollRowDao tollRowDao;

    @Inject
    public TollRatesRepository(TollRateTableDao tollRateTableDao,
                               TollRateTableDataDao tollRateTableDataDao,
                               TollRowDao tollRowDao,
                               AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, DateUtils.MINUTE_IN_MILLIS, "toll_trip");

        this.tollRateTableDao = tollRateTableDao;
        this.tollRateTableDataDao = tollRateTableDataDao;
        this.tollRowDao = tollRowDao;

    }

    public LiveData<TollRateTable> loadTollRatesFor(int route, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return tollRateTableDao.loadTollRatesFor(route);
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

        List<TollRateTableDataEntity> mTollTables = new ArrayList<>();

        List<TollRowEntity> mTollRows = new ArrayList<>();

        URL url;

        url = new URL(APIEndPoints.TOLL_RATES);
        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONArray items = new JSONObject(jsonFile).getJSONArray("TollRates");

        int numItems = items.length();

        for (int i = 0; i < numItems; i++) {

            JSONObject item = items.getJSONObject(i);

            TollRateTableDataEntity table = new TollRateTableDataEntity();

            table.setRoute(item.getInt("route"));
            table.setMessage(item.getString("message"));
            table.setNumCol(item.getInt("numCol"));

            JSONArray rows = new JSONArray(item.getString("tollTable"));
            int numRows = rows.length();

            for (int j = 0; j < numRows; j++) {

                TollRowEntity row = new TollRowEntity();
                JSONObject rowJson = rows.getJSONObject(j);

                row.setId(String.format(Locale.ENGLISH,"%d_%d", table.getRoute(), j));
                row.setRoute(table.getRoute());
                row.setHeader(rowJson.getBoolean("header"));

                if (rowJson.has("weekday")) {
                    row.setWeekday(rowJson.getBoolean("weekday"));
                } else {
                    row.setWeekday(true);
                }

                if (rowJson.has("start_time")) {
                    row.setStartTime(rowJson.getString("start_time"));
                }

                if (rowJson.has("end_time")) {
                    row.setEndTime(rowJson.getString("end_time"));
                }

                row.setRowValues(rowJson.getString("rows"));

                mTollRows.add(row);

            }

            mTollTables.add(table);
        }



        TollRateTableDataEntity[] tollTableArray = new TollRateTableDataEntity[mTollTables.size()];
        tollTableArray = mTollTables.toArray(tollTableArray);

        tollRateTableDataDao.deleteAndInsertTransaction(tollTableArray);

        TollRowEntity[] tollRowArray = new TollRowEntity[mTollRows.size()];
        tollRowArray = mTollRows.toArray(tollRowArray);

        tollRowDao.deleteAndInsertTransaction(tollRowArray);

        CacheEntity tollCache = new CacheEntity("toll_table", System.currentTimeMillis());
        getCacheRepository().setCacheTime(tollCache);



    }
}