package gov.wa.wsdot.android.wsdot.database.borderwaits;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public abstract class BorderWaitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertBorderWaits(BorderWaitEntity... borderWaits);

    @Query("SELECT * FROM border_wait WHERE direction LIKE :direction")
    public abstract LiveData<List<BorderWaitEntity>> loadBorderWaitsFor(String direction);

    @Query("DELETE FROM border_wait")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(BorderWaitEntity... borderWaits) {
        deleteAll();
        insertBorderWaits(borderWaits);
    }

}