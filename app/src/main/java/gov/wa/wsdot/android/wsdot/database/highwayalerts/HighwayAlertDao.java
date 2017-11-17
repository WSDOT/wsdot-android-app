package gov.wa.wsdot.android.wsdot.database.highwayalerts;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class HighwayAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertHighwayAlerts(HighwayAlertEntity... highwayAlerts);

    @Query("SELECT * FROM highway_alerts")
    public abstract LiveData<List<HighwayAlertEntity>> loadHighwayAlerts();

    @Query("SELECT * FROM highway_alerts WHERE highway_alert_priority LIKE :priority")
    public abstract LiveData<List<HighwayAlertEntity>> loadHighwayAlertsWith(String priority);

    @Query("DELETE FROM highway_alerts")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(HighwayAlertEntity... highwayAlerts) {
        deleteAll();
        insertHighwayAlerts(highwayAlerts);
    }
}
