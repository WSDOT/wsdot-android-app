package gov.wa.wsdot.android.wsdot.database.caches;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.provider.BaseColumns;

@Entity(tableName = "caches")
public class CacheEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "cache_table_name")
    private String tableName;

    @ColumnInfo(name = "cache_last_updated")
    private Long lastUpdated;

    public CacheEntity(String tableName, Long lastUpdated){
        this.tableName = tableName;
        this.lastUpdated = lastUpdated;
    }

    public String getTableName() {
        return this.tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getLastUpdated() {
        return this.lastUpdated;
    }
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
