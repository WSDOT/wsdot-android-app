package gov.wa.wsdot.android.wsdot.database.myroute;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface MyRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertMyRoutes(MyRouteEntity... myRoutes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertMyRoute(MyRouteEntity myRoute);

    @Query("SELECT * FROM my_route")
    public abstract LiveData<List<MyRouteEntity>> loadMyRoutes();

    @Query("SELECT * FROM my_route")
    public abstract List<MyRouteEntity> getMyRoutes();

    @Query("SELECT * FROM my_route WHERE is_starred = 1")
    public abstract LiveData<List<MyRouteEntity>> loadFavoriteMyRoutes();

    @Query("SELECT * FROM my_route WHERE id = :id LIMIT 1")
    public abstract LiveData<MyRouteEntity> loadMyRouteForId(Long id);

    @Query("SELECT * FROM my_route WHERE id = :id LIMIT 1")
    public abstract MyRouteEntity getMyRouteForId(Long id);

    @Query("UPDATE my_route SET found_cameras = :foundCameras WHERE id = :id")
    public abstract void updateFoundCameras(Long id, int foundCameras);

    @Query("UPDATE my_route SET camera_ids_json = :idsString WHERE id = :id")
    public abstract void updateCameraIdsJson(Long id, String idsString);

    @Query("UPDATE my_route SET found_travel_times = :foundTravelTimes WHERE id = :id")
    public abstract void updateFoundTravelTimes(Long id, int foundTravelTimes);

    @Query("UPDATE my_route SET travel_time_titles_json = :titlesString WHERE id = :id")
    public abstract void updateTravelTimesTitlesJson(Long id, String titlesString);

    @Query("UPDATE my_route SET title = :title WHERE id = :id")
    public abstract void updateTitle(Long id, String title);

    @Query("UPDATE my_route SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(Long id, Integer isStarred);

    @Query("DELETE FROM my_route WHERE id = :id")
    public abstract void deleteMyRoute(Long id);

}
