package gov.wa.wsdot.android.wsdot.database.tollrates;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class TollTripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTravelTimes(TollTripEntity... tollTrips);

    @Query("DELETE FROM toll_trip")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(TollTripEntity... tollTrips) {
        deleteAll();
        insertTravelTimes(tollTrips);
    }
}
