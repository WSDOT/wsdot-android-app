package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface BorderWaitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertBorderWaits(BorderWaitEntity... borderWaits);

    @Query("SELECT * FROM border_waits WHERE direction LIKE :direction")
    public BorderWaitEntity[] loadBorderWaitsFor(String direction);

    @Query("SELECT * FROM border_waits WHERE is_starred = 1")
    public BorderWaitEntity[] loadStarredBorderWaits();
}