package gov.wa.wsdot.android.wsdot.database.caches;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public abstract class CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCache(CacheEntity cache);

    @Query("SELECT * FROM caches WHERE cache_table_name LIKE :tableName")
    public abstract List<CacheEntity> loadCacheTimeFor(String tableName);

    @Query("DELETE FROM caches WHERE cache_table_name LIKE :tableName")
    public abstract void deleteCacheTimeFor(String tableName);

    @Transaction
    public void deleteAndInsertTransaction(CacheEntity cacheTime, String tableName) {
        deleteCacheTimeFor(tableName);
        insertCache(cacheTime);
    }
}