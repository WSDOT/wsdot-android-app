package gov.wa.wsdot.android.wsdot.database.traveltimes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class TravelTimeTripDao {

    @Query("SELECT * FROM travel_time_trips WHERE title = :title")
    public abstract TravelTimeTripEntity getTravelTimeTrip(String title);

    @Query("UPDATE travel_time_trips SET is_starred = :isStarred WHERE title = :title")
    public abstract void updateIsStarred(String title, Integer isStarred);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTravelTimeTrips(TravelTimeTripEntity... travelTimeGroups);

    @Query("DELETE FROM travel_times")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(TravelTimeTripEntity... travelTimeGroups) {
        deleteAll();
        insertTravelTimeTrips(travelTimeGroups);
    }

}
