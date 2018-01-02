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

    @Query("SELECT * FROM travel_times")
    public abstract LiveData<List<TravelTimeEntity>> loadTravelTimes();

    @Query("SELECT * FROM travel_times")
    public abstract List<TravelTimeEntity> getTravelTimes();

    @Query("SELECT * FROM travel_times WHERE id LIKE :timeId")
    public abstract LiveData<TravelTimeEntity> loadTravelTimeFor(Integer timeId);

    @Query("SELECT * FROM travel_times WHERE title LIKE :query")
    public abstract LiveData<List<TravelTimeEntity>> queryTravelTimes(String query);

    @Query("SELECT * FROM travel_times WHERE is_starred = 1")
    public abstract List<TravelTimeEntity> getFavoriteTravelTimes();

    @Query("UPDATE travel_times SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(Integer id, Integer isStarred);

    @Query("DELETE FROM travel_times")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(TravelTimeEntity... travelTimes) {
        deleteAll();
        insertTravelTimes(travelTimes);
    }
}
