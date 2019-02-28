package gov.wa.wsdot.android.wsdot.database.mountainpasses;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

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
    public abstract LiveData<List<MountainPassEntity>> loadFavoriteMountainPasses();

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
