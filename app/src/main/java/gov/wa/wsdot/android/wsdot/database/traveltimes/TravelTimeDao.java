package gov.wa.wsdot.android.wsdot.database.traveltimes;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

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
