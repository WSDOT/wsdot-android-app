package gov.wa.wsdot.android.wsdot.database.trafficmap;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
