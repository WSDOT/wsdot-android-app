package gov.wa.wsdot.android.wsdot.database.ferries;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class FerryTerminalSailingSpacesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTerminalSpaces(FerryTerminalSailingSpacesEntity... spaces);

    @Query("SELECT * FROM ferries_terminal_sailing_space")
    public abstract LiveData<List<FerryTerminalSailingSpacesEntity>> loadFerryTerminalSpaces();

    @Query("SELECT * FROM ferries_terminal_sailing_space WHERE id LIKE :terminalId")
    public abstract LiveData<FerryTerminalSailingSpacesEntity> loadTerminalSpacesFor(Integer terminalId);

    @Query("DELETE FROM ferries_terminal_sailing_space")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(FerryTerminalSailingSpacesEntity... spaces) {
        deleteAll();
        insertTerminalSpaces(spaces);
    }
}
