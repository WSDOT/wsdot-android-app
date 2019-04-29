package gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class TollRowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTravelTimes(TollRowEntity... tollRow);

    @Query("SELECT * FROM toll_rate_table_row WHERE route = :route")
    public abstract TollRowEntity getTollRow(String route);

    @Query("DELETE FROM toll_rate_table_row")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(TollRowEntity... tollRows) {
        deleteAll();
        insertTravelTimes(tollRows);
    }

}
