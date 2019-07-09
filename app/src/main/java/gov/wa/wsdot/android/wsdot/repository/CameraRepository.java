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
import androidx.sqlite.db.SimpleSQLiteQuery;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraDao;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteDao;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class CameraRepository extends NetworkResourceSyncRepository {

    private CameraDao cameraDao;
    private MyRouteDao myRouteDao;

    @Inject
    CameraRepository(CameraDao cameraDao, MyRouteDao myRouteDao, AppExecutors appExecutors, CacheRepository cacheRepository) {
        super(appExecutors, cacheRepository, (7 * DateUtils.DAY_IN_MILLIS), "cameras");
        this.cameraDao = cameraDao;
        this.myRouteDao = myRouteDao;
    }

    public LiveData<List<CameraEntity>> loadCameras(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return cameraDao.loadCameras();
    }

    public List<CameraEntity> getCameras(MutableLiveData<ResourceStatus> status) {
        super.refreshDataOnSameThread(status, false);
        return cameraDao.getCameras();
    }

    public LiveData<List<CameraEntity>> loadFavoriteCameras(MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return cameraDao.loadFavoriteCameras();
    }

    public LiveData<CameraEntity> getCamera(Integer cameraId, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return cameraDao.loadCamera(cameraId);
    }

    // Need to build the query ourselves since Room and SQL can't handle possible 999 or more parameters in a query
    public LiveData<List<CameraEntity>> loadCamerasForIds(int[] cameraIds, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM cameras WHERE id IN ");

        sb.append("(");
        for (int i = 0; i < cameraIds.length; i++) {
            sb.append(String.valueOf(cameraIds[i]));
            if (i != cameraIds.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(");");

        SimpleSQLiteQuery query = new SimpleSQLiteQuery(sb.toString());

        return cameraDao.loadCamerasForIds(query);
    }

    public LiveData<List<CameraEntity>> getCamerasForRoad(String roadName, MutableLiveData<ResourceStatus> status) {
        super.refreshData(status, false);
        return cameraDao.loadCamerasForRoad(roadName);
    }

    public void setIsStarred(Integer id, Integer isStarred) {
        getExecutor().diskIO().execute(() -> {
            cameraDao.updateIsStarred(id, isStarred);
        });
    }

    @Override
    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CameraEntity[] camerasArray = new CameraEntity[0];
            cameraDao.deleteAndInsertTransaction(camerasArray);
        }

        List<CameraEntity> starredCameras = cameraDao.getFavoriteCameras();
        List<Integer> starredCameraIds = new ArrayList<>();
        for (CameraEntity camera : starredCameras){
            starredCameraIds.add(camera.getCameraId());
        }

        URL url = new URL(APIEndPoints.CAMERAS);

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

        JSONObject obj = new JSONObject(jsonFile);
        JSONObject result = obj.getJSONObject("cameras");
        JSONArray items = result.getJSONArray("items");
        List<CameraEntity> cameras = new ArrayList<>();

        int numItems = items.length();
        for (int j=0; j < numItems; j++) {
            JSONObject item = items.getJSONObject(j);
            CameraEntity cameraData = new CameraEntity();

            cameraData.setCameraId(item.getInt("id"));
            cameraData.setTitle(item.getString("title"));
            cameraData.setUrl(item.getString("url"));
            cameraData.setLatitude(item.getDouble("lat"));
            cameraData.setLongitude(item.getDouble("lon"));
            cameraData.setDirection(item.getString("direction"));
            cameraData.setMilepost(item.getString("milepost"));
            cameraData.setHasVideo(item.getInt("video"));
            cameraData.setRoadName(item.getString("roadName"));

            if (starredCameraIds.contains(Integer.parseInt(item.getString("id")))) {
                cameraData.setIsStarred(1);
            }

            cameras.add(cameraData);
        }

        CameraEntity[] camerasArray = new CameraEntity[cameras.size()];
        camerasArray = cameras.toArray(camerasArray);

        cameraDao.deleteAndInsertTransaction(camerasArray);

        CacheEntity camerasCache = new CacheEntity("cameras", System.currentTimeMillis());
        getCacheRepository().setCacheTime(camerasCache);

        resetMyRouteCameras();

    }

    private void resetMyRouteCameras() {
        List<MyRouteEntity> routes = myRouteDao.getMyRoutes();
        for (MyRouteEntity route: routes){
            myRouteDao.updateFoundCameras(route.getMyRouteId(), 0);
        }
    }
}
