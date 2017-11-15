package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

@Entity(tableName = "travel_times")
public class TravelTimesEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer travleTimeId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "updated")
    private String updated;

    @ColumnInfo(name = "distance")
    private String distance;

    @ColumnInfo(name = "average")
    private Integer average;

    @ColumnInfo(name = "current")
    private Integer current;

    @ColumnInfo(name = "start_latitude")
    private Double startLatitude;

    @ColumnInfo(name = "start_longitude")
    private Double startLongitude;

    @ColumnInfo(name = "end_latitude")
    private Double endLatitude;

    @ColumnInfo(name = "end_longitude")
    private Double endLongitude;

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Integer getTravleTimeId() {
        return travleTimeId;
    }

    public void setTravleTimeId(Integer travleTimeId) {
        this.travleTimeId = travleTimeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Integer getAverage() {
        return average;
    }

    public void setAverage(Integer average) {
        this.average = average;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }
}
