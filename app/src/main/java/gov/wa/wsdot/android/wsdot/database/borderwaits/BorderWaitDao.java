package gov.wa.wsdot.android.wsdot.database.borderwaits;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class BorderWaitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertBorderWaits(BorderWaitEntity... borderWaits);

    @Query("SELECT * FROM border_wait WHERE direction LIKE :direction")
    public abstract LiveData<List<BorderWaitEntity>> loadBorderWaitsFor(String direction);

    @Query("UPDATE border_wait SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(Integer id, Integer isStarred);

    @Query("SELECT * FROM border_wait WHERE is_starred = 1")
    public abstract LiveData<List<BorderWaitEntity>> loadFavoriteBorderWaits();

    @Query("SELECT * FROM border_wait WHERE is_starred = 1")
    public abstract List<BorderWaitEntity> getFavoriteBorderWaits();

    @Query("DELETE FROM border_wait")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(BorderWaitEntity... borderWaits) {
        deleteAll();
        insertBorderWaits(borderWaits);
    }

}