package gov.wa.wsdot.android.wsdot.database.tollrates;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "toll_trip",
        foreignKeys = @ForeignKey(entity = TollRateSignEntity.class,
                parentColumns = "id",
                childColumns = "sign_id",
                onDelete = ForeignKey.CASCADE))
public class TollTripEntity {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "trip_name")
    private String tripName;

    @ColumnInfo(name = "end_location_name")
    private String endLocationName;

    @ColumnInfo(name = "sign_id")
    @NonNull
    private String signId = "";

    @ColumnInfo(name = "toll_rate")
    @NonNull
    private Double tollRate = 0.0;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "end_latitude")
    private Double endLatitude;

    @ColumnInfo(name = "end_longitude")
    private Double endLongitude;

    @ColumnInfo(name = "updated")
    private String updated;

    @NonNull
    public String getSignId() {
        return this.signId;
    }

    public void setSignId(@NonNull String signId){
        this.signId = signId;
    }

    @NonNull
    public Double getTollRate() {
        return tollRate;
    }

    public void setTollRate(@NonNull Double tollRate) {
        this.tollRate = tollRate;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getEndLocationName() {
        return endLocationName;
    }

    public void setEndLocationName(String endLocationName) {
        this.endLocationName = endLocationName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}
