package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.provider.BaseColumns;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "travel_times",
        foreignKeys = @ForeignKey(entity = TravelTimeTripEntity.class,
                parentColumns = "title",
                childColumns = "trip_title",
                onDelete = ForeignKey.CASCADE))
public class TravelTimeEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer travelTimeId;

    @ColumnInfo(name = "trip_title")
    private String tripTitle;

    @ColumnInfo(name = "via")
    private String via;

    @ColumnInfo(name = "status")
    private String status;

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

    public Integer getTravelTimeId() {
        return travelTimeId;
    }

    public void setTravelTimeId(Integer travelTimeId) {
        this.travelTimeId = travelTimeId;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getStatus() { return status; }

    public void setStatus(String status){
        this.status = status;
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

}
