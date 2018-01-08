package gov.wa.wsdot.android.wsdot.database.ferries;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class FerryScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertFerrySchedules(FerryScheduleEntity... schedules);

    @Query("SELECT * FROM ferries_schedules")
    public abstract LiveData<List<FerryScheduleEntity>> loadFerrySchedules();

    @Query("SELECT * FROM ferries_schedules")
    public abstract List<FerryScheduleEntity> getFerrySchedules();

    @Query("SELECT * FROM ferries_schedules WHERE id LIKE :routeId")
    public abstract LiveData<FerryScheduleEntity> loadScheduleFor(Integer routeId);

    @Query("SELECT * FROM ferries_schedules WHERE is_starred = 1")
    public abstract LiveData<List<FerryScheduleEntity>> loadFavoriteFerrySchedules();

    @Query("SELECT * FROM ferries_schedules WHERE is_starred = 1")
    public abstract List<FerryScheduleEntity> getFavoriteFerrySchedules();

    @Query("UPDATE ferries_schedules SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(Integer id, Integer isStarred);

    @Query("DELETE FROM ferries_schedules")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(FerryScheduleEntity... schedules) {
        deleteAll();
        insertFerrySchedules(schedules);
    }
}
