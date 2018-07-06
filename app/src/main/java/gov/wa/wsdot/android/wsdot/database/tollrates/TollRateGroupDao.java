package gov.wa.wsdot.android.wsdot.database.tollrates;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Relation;

import java.util.List;

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
