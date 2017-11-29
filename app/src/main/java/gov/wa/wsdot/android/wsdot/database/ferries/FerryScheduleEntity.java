package gov.wa.wsdot.android.wsdot.database.ferries;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

@Entity(tableName = "ferries_schedules")
public class FerryScheduleEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer ferryScheduleId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "crossing_time")
    private String crossingTime;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "alert")
    private String alert;

    @ColumnInfo(name = "updated")
    private String updated;

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Integer getId() {
        return id;
    }

    public Integer getFerryScheduleId() {
        return ferryScheduleId;
    }

    public void setFerryScheduleId(Integer ferryScheduleId) {
        this.ferryScheduleId = ferryScheduleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCrossingTime() {
        return crossingTime;
    }

    public void setCrossingTime(String crossingTime) {
        this.crossingTime = crossingTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }
}