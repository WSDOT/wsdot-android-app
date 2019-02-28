package gov.wa.wsdot.android.wsdot.database.highwayalerts;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class HighwayAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertHighwayAlerts(HighwayAlertEntity... highwayAlerts);

    @Query("SELECT * FROM highway_alerts")
    public abstract LiveData<List<HighwayAlertEntity>> loadHighwayAlerts();

    @Query("SELECT * FROM highway_alerts WHERE highway_alert_priority LIKE :priority")
    public abstract LiveData<List<HighwayAlertEntity>> loadHighwayAlertsWith(String priority);

    @Query("SELECT * FROM highway_alerts WHERE highway_alert_id = :alertId")
    public abstract LiveData<HighwayAlertEntity> loadHighwayAlert(Integer alertId);

    @Query("DELETE FROM highway_alerts")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(HighwayAlertEntity... highwayAlerts) {
        deleteAll();
        insertHighwayAlerts(highwayAlerts);
    }
}
