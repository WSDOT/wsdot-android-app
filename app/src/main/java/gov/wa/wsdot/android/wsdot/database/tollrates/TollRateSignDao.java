package gov.wa.wsdot.android.wsdot.database.tollrates;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class TollRateSignDao {

    @Query("SELECT * FROM toll_rate_sign WHERE id = :id")
    public abstract TollRateSignEntity getTollRateSign(String id);

    @Query("UPDATE toll_rate_sign SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(String id, Integer isStarred);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTollRateSign(TollRateSignEntity... tollRateSigns);

    @Query("DELETE FROM toll_rate_sign")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(TollRateSignEntity... tollRateSigns) {
        deleteAll();
        insertTollRateSign(tollRateSigns);
    }

}
