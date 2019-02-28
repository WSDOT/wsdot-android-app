package gov.wa.wsdot.android.wsdot.database.tollrates;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface TollRateGroupDao {

    @Query("SELECT * FROM toll_rate_sign")
    LiveData<List<TollRateGroup>> loadTollRateGroups();

    @Query("SELECT * FROM toll_rate_sign")
    List<TollRateGroup> getTollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 405")
    LiveData<List<TollRateGroup>> loadI405TollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 405")
    List<TollRateGroup> getI405TollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 167")
    LiveData<List<TollRateGroup>> loadSR167TollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE is_starred = 1")
    LiveData<List<TollRateGroup>> loadFavoriteTollRateGroups();

}
