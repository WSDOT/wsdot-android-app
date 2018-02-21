package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

@Dao
public abstract class TravelTimeTripDao {

    @Query("SELECT * FROM travel_time_trip WHERE title = :title")
    public abstract TravelTimeTripEntity getTravelTimeTrip(String title);

    @Query("UPDATE travel_time_trip SET is_starred = :isStarred WHERE title = :title")
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
