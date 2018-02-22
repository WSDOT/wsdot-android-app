package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TravelTimeGroupDao {

    @Query("SELECT * FROM travel_time_trips")
    LiveData<List<TravelTimeGroup>> loadTravelTimeGroups();

    @Query("SELECT * FROM travel_time_trips")
    List<TravelTimeGroup> getTravelTimeGroups();

    @Query("SELECT * FROM travel_time_trips WHERE is_starred = 1")
    LiveData<List<TravelTimeGroup>> loadFavoriteTravelTimeGroups();

    @Query("SELECT * FROM travel_time_trips WHERE title LIKE :query")
    LiveData<List<TravelTimeGroup>> queryTravelTimeGroups(String query);

}
