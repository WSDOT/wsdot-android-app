package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TravelTimeGroupDao {

    @Query("SELECT * FROM travel_time_trip")
    LiveData<List<TravelTimeGroup>> loadTravelTimeGroups();

    @Query("SELECT * FROM travel_time_trip")
    List<TravelTimeGroup> getTravelTimeGroups();

    @Query("SELECT * FROM travel_time_trip WHERE is_starred = 1")
    LiveData<List<TravelTimeGroup>> loadFavoriteTravelTimeGroups();

    @Query("SELECT * FROM travel_time_trip WHERE is_starred = 1")
    List<TravelTimeGroup> getFavoriteTravelTimeGroups();

    @Query("SELECT * FROM travel_time_trip WHERE title LIKE :query")
    LiveData<List<TravelTimeGroup>> queryTravelTimeGroups(String query);

}
