package gov.wa.wsdot.android.wsdot.repository;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteDao;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.Utils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

@Singleton
public class MyRoutesRepository {

    private static String TAG = MyRoutesRepository.class.getSimpleName();

    private final MyRouteDao myRouteDao;
    private final AppExecutors appExecutors;

    private final CameraRepository cameraRepo;
    private MutableLiveData<ResourceStatus> mCameraStatus;

    private final TravelTimeRepository travelTimeRepo;
    private MutableLiveData<ResourceStatus> mTravelTimeStatus;

    private static final Double MAX_ITEM_DISTANCE = 0.248548;

    @Inject
    @VisibleForTesting
    public MyRoutesRepository(MyRouteDao myRouteDao,
                              TravelTimeRepository travelTimeRepo,
                              CameraRepository cameraRepo,
                              AppExecutors appExecutors) {

        this.myRouteDao = myRouteDao;
        this.appExecutors = appExecutors;

        this.mTravelTimeStatus = new MutableLiveData<>();
        this.mCameraStatus = new MutableLiveData<>();

        this.travelTimeRepo = travelTimeRepo;
        this.cameraRepo = cameraRepo;
    }

    public LiveData<List<MyRouteEntity>> loadMyRoutes() {
        return myRouteDao.loadMyRoutes();
    }

    public LiveData<List<MyRouteEntity>> loadFavoriteMyRoutes() {
        return myRouteDao.loadFavoriteMyRoutes();
    }

    public LiveData<MyRouteEntity> loadMyRoute(long routeId) {
        return myRouteDao.loadMyRouteForId(routeId);
    }

    public MyRouteEntity getMyRoute(long routeId){
        return myRouteDao.getMyRouteForId(routeId);
    }

    public void deleteMyRoute(long routeId){
        appExecutors.diskIO().execute(() -> myRouteDao.deleteMyRoute(routeId));
    }

    public void addMyRoute(MyRouteEntity myRoute){
        appExecutors.diskIO().execute(() -> myRouteDao.insertMyRoute(myRoute));
    }

    public void updateTitle(long routeId, String newTitle){
        appExecutors.diskIO().execute(() -> myRouteDao.updateTitle(routeId, newTitle));
    }

    public void setIsStarred(long id, Integer isStarred) {
        appExecutors.diskIO().execute(() -> myRouteDao.updateIsStarred(id, isStarred));
    }

    public void findCamerasOnRoute(MutableLiveData<Boolean> foundCameras, Long myRouteId){

        appExecutors.diskIO().execute(() -> {

            List<CameraEntity> cameras = cameraRepo.getCameras(mCameraStatus);

            MyRouteEntity route = myRouteDao.getMyRouteForId(myRouteId);

            ArrayList<Integer> camerasOnRouteIds = new ArrayList<>();

            try {
                for (CameraEntity camera : cameras) {
                    for (LatLng location : ParserUtils.getRouteArrayList(new JSONArray(route.getRouteLocations()))) {
                        if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                camera.getLatitude(), camera.getLongitude()) <= MAX_ITEM_DISTANCE) {

                            camerasOnRouteIds.add(camera.getCameraId());

                        }
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "failed to read route json");
            }

            myRouteDao.deleteMyRoute(route.getMyRouteId());
            JSONArray idsJSON = new JSONArray(camerasOnRouteIds);
            route.setCameraIdsJSON(idsJSON.toString());
            route.setFoundCameras(1);
            myRouteDao.insertMyRoute(route);

            foundCameras.postValue(true);

        });
    }

    public void findTravelTimesOnRoute(MutableLiveData<Boolean> foundTravelTimes, Long myRouteId){

        appExecutors.diskIO().execute(() -> {

            List<TravelTimeGroup> groups = travelTimeRepo.getTravelTimeGroups(mTravelTimeStatus);

            MyRouteEntity route = myRouteDao.getMyRouteForId(myRouteId);

            ArrayList<String> travelTimesOnRouteTitles = new ArrayList<>();


            try {
                for (TravelTimeGroup group : groups) {

                    Boolean routeAtStart = false;
                    Boolean routeAtEnd = false;

                    for (TravelTimeEntity time : group.travelTimes) {

                        for (LatLng location : ParserUtils.getRouteArrayList(new JSONArray(route.getRouteLocations()))) {

                            if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                    time.getStartLatitude(), time.getStartLongitude()) <= MAX_ITEM_DISTANCE) {
                                routeAtStart = true;
                            }

                            if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                    time.getEndLatitude(), time.getEndLongitude()) <= MAX_ITEM_DISTANCE) {
                                routeAtEnd = true;
                            }
                        }

                    }

                    if (routeAtStart && routeAtEnd) {
                        Log.e(TAG, "found one");
                        Log.e(TAG, group.trip.getTitle());

                        travelTimesOnRouteTitles.add(group.trip.getTitle());
                    }

                }

            } catch (JSONException e) {
                Log.e(TAG, "failed to read route json");
            }


            JSONArray titlesJSON = new JSONArray(travelTimesOnRouteTitles);

            Log.e(TAG, titlesJSON.toString());

            route.setTravelTimeTitlesJSON(titlesJSON.toString());
            route.setFoundTravelTimes(1);

            myRouteDao.insertMyRoute(route);

            Log.e(TAG, "found travel times");
            foundTravelTimes.postValue(true);

        });
    }

}
