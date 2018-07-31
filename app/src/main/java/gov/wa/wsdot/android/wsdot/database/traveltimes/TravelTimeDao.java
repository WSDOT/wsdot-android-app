package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class TravelTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTravelTimes(TravelTimeEntity... travelTimes);

    @Query("DELETE FROM travel_times")
    public abstract void deleteAll();

    @Query("SELECT * FROM travel_times WHERE id IN (:ids)")
    public abstract LiveData<List<TravelTimeEntity>> loadTravelTimesFor(List<Integer> ids);

    @Transaction
    public void deleteAndInsertTransaction(TravelTimeEntity... travelTimes) {
        deleteAll();
        insertTravelTimes(travelTimes);
    }
}
