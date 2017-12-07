package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.format.DateUtils;

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

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryTerminalSailingSpacesDao;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryTerminalSailingSpacesEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class FerryTerminalSpaceRepository extends NetworkResourceSyncRepository {

    private static String TAG = FerryTerminalSailingSpacesEntity.class.getSimpleName();

    private final FerryTerminalSailingSpacesDao terminalSailingSpacesDao;

    @Inject
    FerryTerminalSpaceRepository(FerryTerminalSailingSpacesDao terminalSailingSpacesDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, DateUtils.MINUTE_IN_MILLIS, "ferries_terminal_sailing_space");
        this.terminalSailingSpacesDao = terminalSailingSpacesDao;
    }

    public LiveData<FerryTerminalSailingSpacesEntity> getTerminalSpacesFor(Integer terminalId, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return terminalSailingSpacesDao.loadTerminalSpacesFor(terminalId);
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        long now = System.currentTimeMillis();
        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

        String sailingSpaceUrl =  APIEndPoints.SAILING_SPACES
                + "?apiaccesscode=" + APIEndPoints.WSDOT_API_KEY;

        URL url = new URL(sailingSpaceUrl);
        URLConnection urlConn = url.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;

        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONArray array = new JSONArray(jsonFile);

        List<FerryTerminalSailingSpacesEntity> terminals = new ArrayList<>();

        int numItems = array.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = array.getJSONObject(j);
            FerryTerminalSailingSpacesEntity sailingSpaces = new FerryTerminalSailingSpacesEntity();

            sailingSpaces.setTerminalId(item.getInt("TerminalID"));
            sailingSpaces.setName(item.getString("TerminalName"));
            sailingSpaces.setAbbrev(item.getString("TerminalAbbrev"));
            sailingSpaces.setDepartingSpaces(item.getString("DepartingSpaces"));
            sailingSpaces.setLastUpdated(dateFormat.format(new Date(System.currentTimeMillis())));

            terminals.add(sailingSpaces);
        }

        FerryTerminalSailingSpacesEntity[] spacesArray = new FerryTerminalSailingSpacesEntity[terminals.size()];
        spacesArray = terminals.toArray(spacesArray);

        terminalSailingSpacesDao.deleteAndInsertTransaction(spacesArray);

        CacheEntity scheduleCache = new CacheEntity("ferries_terminal_sailing_space", System.currentTimeMillis());
        getCacheRepository().setCacheTime(scheduleCache);
    }
}
