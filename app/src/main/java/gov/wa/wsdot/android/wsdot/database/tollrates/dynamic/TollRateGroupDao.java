package gov.wa.wsdot.android.wsdot.database.tollrates.dynamic;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroup;

@Dao
public interface TollRateGroupDao {

    @Transaction
    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 405")
    LiveData<List<TollRateGroup>> loadI405TollRateGroups();

    @Transaction
    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 167")
    LiveData<List<TollRateGroup>> loadSR167TollRateGroups();

    @Transaction
    @Query("SELECT * FROM toll_rate_sign WHERE is_starred = 1")
    LiveData<List<TollRateGroup>> loadFavoriteTollRateGroups();

}
