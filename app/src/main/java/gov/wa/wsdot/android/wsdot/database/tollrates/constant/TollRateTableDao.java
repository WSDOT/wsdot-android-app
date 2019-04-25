package gov.wa.wsdot.android.wsdot.database.tollrates.constant;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TollRateTableDao {

    @Query("SELECT * FROM toll_rate_sign")
    LiveData<List<TollRateTable>> loadTollRates();

    @Query("SELECT * FROM toll_rate_sign WHERE state_route = :route")
    LiveData<TollRateTable> loadTollRatesFor(int route);

}
