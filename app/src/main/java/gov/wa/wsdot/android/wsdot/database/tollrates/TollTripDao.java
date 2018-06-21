package gov.wa.wsdot.android.wsdot.database.tollrates;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

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
