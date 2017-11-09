package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface BorderWaitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertBorderWaits(BorderWaitEntity... borderWaits);

    @Query("SELECT * FROM border_waits WHERE direction LIKE :direction")
    public LiveData<List<BorderWaitEntity>> loadBorderWaitsFor(String direction);

}