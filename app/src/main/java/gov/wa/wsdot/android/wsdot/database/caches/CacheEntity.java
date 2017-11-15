package gov.wa.wsdot.android.wsdot.database.caches;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

@Entity(tableName = "caches")
public class CacheEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "cache_table_name")
    private String tableName;

    @ColumnInfo(name = "cache_last_updated")
    private Integer lastUpdated;

    public CacheEntity(String tableName, Integer lastUpdated){
        this.tableName = tableName;
        this.lastUpdated = lastUpdated;
    }

    public String getTableName() {
        return this.tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getLastUpdated() {
        return this.lastUpdated;
    }
    public void setLastUpdated(Integer lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
