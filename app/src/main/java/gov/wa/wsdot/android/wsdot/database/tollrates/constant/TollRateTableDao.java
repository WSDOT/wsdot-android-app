package gov.wa.wsdot.android.wsdot.database.tollrates.constant;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class TollRateTableDao {

    @Query("SELECT * FROM toll_rate_table WHERE route = :route")
    public abstract TollRateTableEntity getTollRateTable(String route);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTollRateTable(TollRateTableEntity... tollRateTable);

    @Query("DELETE FROM toll_rate_table")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(TollRateTableEntity... tollRateTable) {
        deleteAll();
        insertTollRateTable(tollRateTable);
    }

}
