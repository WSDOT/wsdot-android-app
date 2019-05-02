package gov.wa.wsdot.android.wsdot.database.tollrates.constant;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface TollRateTableDao {

    @Transaction
    @Query("SELECT * FROM toll_rate_table")
    LiveData<List<TollRateTable>> loadTollRates();

    @Transaction
    @Query("SELECT * FROM toll_rate_table WHERE route = :route")
    LiveData<TollRateTable> loadTollRatesFor(int route);

}
