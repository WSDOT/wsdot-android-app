package gov.wa.wsdot.android.wsdot.database.tollrates;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "toll_rate_sign")
public class TollRateSignEntity {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "id")
    private String id = "";

    @NonNull
    @ColumnInfo(name = "location_name")
    private String locationName = "";

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    @ColumnInfo(name = "state_route")
    @NonNull
    private Integer stateRoute = 0;

    @ColumnInfo(name = "milepost")
    @NonNull
    private Integer milepost = 0;

    @ColumnInfo(name = "travel_direction")
    @NonNull
    private String travelDirection = "";

    @ColumnInfo(name = "start_latitude")
    @NonNull
    private Double startLatitude = 0.0;

    @ColumnInfo(name = "start_longitude")
    @NonNull
    private Double startLongitude = 0.0;

    @NonNull
    public String getId() {
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }

    @NonNull
    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }

    @NonNull
    public Integer getStateRoute() {
        return stateRoute;
    }

    public void setStateRoute(@NonNull Integer stateRoute) {
        this.stateRoute = stateRoute;
    }

    @NonNull
    public Integer getMilepost() {
        return milepost;
    }

    public void setMilepost(@NonNull Integer milepost) {
        this.milepost = milepost;
    }

    @NonNull
    public String getTravelDirection() {
        return travelDirection;
    }

    public void setTravelDirection(@NonNull String travelDirection) {
        this.travelDirection = travelDirection;
    }

    @NonNull
    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(@NonNull Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    @NonNull
    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(@NonNull Double startLongitude) {
        this.startLongitude = startLongitude;
    }
}
