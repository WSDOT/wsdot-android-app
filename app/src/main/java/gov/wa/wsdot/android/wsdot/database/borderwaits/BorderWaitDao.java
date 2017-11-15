package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class BorderWaitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertBorderWaits(BorderWaitEntity... borderWaits);

    @Query("SELECT * FROM border_waits WHERE direction LIKE :direction")
    public abstract LiveData<List<BorderWaitEntity>> loadBorderWaitsFor(String direction);

    @Query("DELETE FROM border_waits")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(BorderWaitEntity... borderWaits) {
        deleteAll();
        insertBorderWaits(borderWaits);
    }

}