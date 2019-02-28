package gov.wa.wsdot.android.wsdot.database.ferries;

import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ferries_terminal_sailing_space")
public class FerryTerminalSailingSpacesEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer terminalId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "abbrev")
    private String abbrev;

    @ColumnInfo(name = "departing_spaces")
    private String departingSpaces;

    @ColumnInfo(name = "last_updated")
    private String lastUpdated;

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Integer getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(Integer terminalId) {
        this.terminalId = terminalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getDepartingSpaces() {
        return departingSpaces;
    }

    public void setDepartingSpaces(String departingSpaces) {
        this.departingSpaces = departingSpaces;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }

}