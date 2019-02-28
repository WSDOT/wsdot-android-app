package gov.wa.wsdot.android.wsdot.database.traveltimes;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "travel_time_trips")
public class TravelTimeTripEntity {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "title")
    private String title = "";

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }

}
