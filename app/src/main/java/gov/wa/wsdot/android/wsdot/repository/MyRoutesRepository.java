package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteDao;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeGroup;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.Utils;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class MyRoutesRepository {

    private static String TAG = MyRoutesRepository.class.getSimpleName();

    private final MyRouteDao myRouteDao;
    private final AppExecutors appExecutors;

    private final CameraRepository cameraRepo;
    private MutableLiveData<ResourceStatus> mCameraStatus;

    private final TravelTimeRepository travelTimeRepo;
    private MutableLiveData<ResourceStatus> mTravelTimeStatus;

    private final MountainPassRepository mountainPassRepo;
    private MutableLiveData<ResourceStatus> mPassStatus;

    private final FerryScheduleRepository ferryScheduleRepo;
    private MutableLiveData<ResourceStatus> mFerryStatus;

    private static final Double MAX_ITEM_DISTANCE = 0.248548;

    @Inject
    @VisibleForTesting
    public MyRoutesRepository(MyRouteDao myRouteDao,
                              TravelTimeRepository travelTimeRepo,
                              CameraRepository cameraRepo,
                              MountainPassRepository mountainPassRepo,
                              FerryScheduleRepository ferryScheduleRepo,
                              AppExecutors appExecutors) {

        this.myRouteDao = myRouteDao;
        this.appExecutors = appExecutors;

        this.mTravelTimeStatus = new MutableLiveData<>();
        this.mCameraStatus = new MutableLiveData<>();
        this.mPassStatus = new MutableLiveData<>();
        this.mFerryStatus = new MutableLiveData<>();

        this.travelTimeRepo = travelTimeRepo;
        this.cameraRepo = cameraRepo;
        this.mountainPassRepo = mountainPassRepo;
        this.ferryScheduleRepo = ferryScheduleRepo;
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

    public void findFavoritesOnRoute(MutableLiveData<Boolean> foundFavorites, Long myRouteId){
        appExecutors.diskIO().execute(() -> {

            MyRouteEntity route = myRouteDao.getMyRouteForId(myRouteId);

            List<CameraEntity> cameras = cameraRepo.getCameras(mCameraStatus);
            List<TravelTimeGroup> travelTimeGroups = travelTimeRepo.getTravelTimeGroups(mTravelTimeStatus);
            List<FerryScheduleEntity> ferrySchedules = ferryScheduleRepo.getFerrySchedules(mFerryStatus);
            List<MountainPassEntity> mountainPasses = mountainPassRepo.getMountainPasses(mPassStatus);

            findCamerasOnRoute(route, cameras);
            findTravelTimesOnRoute(route, travelTimeGroups);
            findFerriesOnRoute(route, ferrySchedules);
            findPassesOnRoute(route, mountainPasses);

            foundFavorites.postValue(true);

        });
    }

    private void findCamerasOnRoute(MyRouteEntity route, List<CameraEntity> cameras){
        try {
            for (CameraEntity camera: cameras){
                for (LatLng location: ParserUtils.getRouteArrayList(new JSONArray(route.getRouteLocations()))) {
                    if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                        camera.getLatitude(), camera.getLongitude()) <= MAX_ITEM_DISTANCE) {
                        cameraRepo.setIsStarred(camera.getCameraId(), 1);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "failed to read route json");
        }
    }

    private void findTravelTimesOnRoute(MyRouteEntity route, List<TravelTimeGroup> groups){
        try {
            for (TravelTimeGroup group: groups) {
                for (TravelTimeEntity time : group.travelTimes) {
                    for (LatLng location : ParserUtils.getRouteArrayList(new JSONArray(route.getRouteLocations()))) {
                        if ((Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                time.getStartLatitude(), time.getStartLongitude()) <= MAX_ITEM_DISTANCE)
                                && (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                time.getEndLatitude(), time.getEndLongitude()) <= MAX_ITEM_DISTANCE)) {

                            travelTimeRepo.setIsStarred(group.trip.getTitle(), 1);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "failed to read route json");
        }
    }

    private void findFerriesOnRoute(MyRouteEntity route, List<FerryScheduleEntity> ferries) {
        try {
            SparseArray<FerriesTerminalItem> terminalLocations = Utils.getTerminalLocations();

            for (FerryScheduleEntity ferrySchedule: ferries) {

                ArrayList<FerriesTerminalItem> terminalItems = getTerminals(ferrySchedule.getDate());
                for (FerriesTerminalItem terminal: terminalItems){

                    Boolean nearStartTerminal = false;
                    Boolean nearEndTerminal = false;

                    for (LatLng location: ParserUtils.getRouteArrayList(new JSONArray(route.getRouteLocations()))) {

                        if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                terminalLocations.get(terminal.getArrivingTerminalID()).getLatitude(),
                                terminalLocations.get(terminal.getArrivingTerminalID()).getLongitude()) <= MAX_ITEM_DISTANCE){
                            nearStartTerminal = true;
                        }

                        if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                terminalLocations.get(terminal.getDepartingTerminalID()).getLatitude(),
                                terminalLocations.get(terminal.getDepartingTerminalID()).getLongitude()) <= MAX_ITEM_DISTANCE) {
                            nearEndTerminal = true;
                        }

                        if (nearStartTerminal && nearEndTerminal){
                            ferryScheduleRepo.setIsStarred(ferrySchedule.getFerryScheduleId(), 1);
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "failed to read route json");
        }
    }

    private void findPassesOnRoute(MyRouteEntity route, List<MountainPassEntity> passes){
        try {
            for (MountainPassEntity pass: passes){
                for (LatLng location: ParserUtils.getRouteArrayList(new JSONArray(route.getRouteLocations()))) {
                    if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                            pass.getLatitude(), pass.getLongitude()) <= MAX_ITEM_DISTANCE) {
                        mountainPassRepo.setIsStarred(pass.getPassId(), 1);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "failed to read route json");
        }
    }

    private ArrayList<FerriesTerminalItem> getTerminals(String datesString) {
        ArrayList<FerriesTerminalItem> terminalItems = new ArrayList<>();
        FerriesTerminalItem terminal;

        try {
            JSONArray dates = new JSONArray(datesString);
            int numDates = dates.length();
            for (int j = 0; j < numDates; j++) {
                JSONObject date = dates.getJSONObject(j);

                JSONArray sailings = date.getJSONArray("Sailings");
                int numSailings = sailings.length();
                for (int k=0; k < numSailings; k++) {
                    JSONObject sailing = sailings.getJSONObject(k);
                    terminal = new FerriesTerminalItem();
                    terminal.setArrivingTerminalID(sailing.getInt("ArrivingTerminalID"));
                    terminal.setArrivingTerminalName(sailing.getString("ArrivingTerminalName"));
                    terminal.setDepartingTerminalID(sailing.getInt("DepartingTerminalID"));
                    terminal.setDepartingTerminalName(sailing.getString("DepartingTerminalName"));

                    terminalItems.add(terminal);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding schedule date items", e);
        }
        return terminalItems;
    }
}
