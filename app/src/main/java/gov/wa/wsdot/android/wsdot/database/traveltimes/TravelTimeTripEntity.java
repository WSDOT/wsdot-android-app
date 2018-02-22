package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

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
