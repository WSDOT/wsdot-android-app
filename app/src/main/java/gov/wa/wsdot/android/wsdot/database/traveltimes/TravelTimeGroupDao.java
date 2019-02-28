package gov.wa.wsdot.android.wsdot.database.traveltimes;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

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

    @Query("SELECT * FROM travel_time_trips WHERE title IN(:titles)")
    LiveData<List<TravelTimeGroup>> loadTravelTimeGroupsForTitles(String[] titles);

}
