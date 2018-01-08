package gov.wa.wsdot.android.wsdot.database.trafficmap;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MapLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertMapLocation(MapLocationEntity mapLocation);

    @Query("SELECT * FROM map_location")
    public abstract LiveData<List<MapLocationEntity>> loadMapLocations();

    @Query("UPDATE map_location SET title = :title WHERE _id = :id")
    public abstract void updateTitle(Integer id, String title);

    @Query("DELETE FROM map_location WHERE _id = :id")
    public abstract void deleteMapLocation(Integer id);
}
