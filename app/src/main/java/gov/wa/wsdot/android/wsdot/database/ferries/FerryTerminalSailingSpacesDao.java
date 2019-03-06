package gov.wa.wsdot.android.wsdot.database.ferries;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

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
