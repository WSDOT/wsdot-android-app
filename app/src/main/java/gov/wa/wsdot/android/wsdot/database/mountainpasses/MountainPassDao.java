package gov.wa.wsdot.android.wsdot.database.mountainpasses;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class MountainPassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertMountainPasses(MountainPassEntity... passes);

    @Query("SELECT * FROM mountain_passes")
    public abstract LiveData<List<MountainPassEntity>> loadMountainPasses();

    @Query("SELECT * FROM mountain_passes")
    public abstract List<MountainPassEntity> getMountainPasses();

    @Query("SELECT * FROM mountain_passes WHERE id LIKE :passId")
    public abstract LiveData<MountainPassEntity> loadMountainPassFor(Integer passId);

    @Query("SELECT * FROM mountain_passes WHERE is_starred = 1")
    public abstract List<MountainPassEntity> getFavoriteMountainPasses();

    @Query("UPDATE mountain_passes SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(Integer id, Integer isStarred);

    @Query("DELETE FROM mountain_passes")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(MountainPassEntity... passes) {
        deleteAll();
        insertMountainPasses(passes);
    }
}
