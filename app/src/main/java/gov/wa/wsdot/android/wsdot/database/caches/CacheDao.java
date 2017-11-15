package gov.wa.wsdot.android.wsdot.database.caches;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertCaches(CacheEntity... caches);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertCache(CacheEntity cache);

    @Query("SELECT * FROM caches WHERE cache_table_name LIKE :tableName")
    public List<CacheEntity> loadCacheTimeFor(String tableName);

    @Delete
    public void deleteChaches(CacheEntity... caches);

}
