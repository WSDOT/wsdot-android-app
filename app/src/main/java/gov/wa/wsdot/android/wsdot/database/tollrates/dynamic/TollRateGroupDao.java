package gov.wa.wsdot.android.wsdot.database.tollrates.dynamic;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Query;

@Dao
public interface TollRateGroupDao {

    @Query("SELECT * FROM toll_rate_sign")
    LiveData<List<TollRateGroup>> loadTollRateGroups();

    @Query("SELECT * FROM toll_rate_sign")
    List<TollRateGroup> getTollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 405")
    LiveData<List<TollRateGroup>> loadI405TollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 405")
    List<TollRateGroup> getI405TollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE state_route = 167")
    LiveData<List<TollRateGroup>> loadSR167TollRateGroups();

    @Query("SELECT * FROM toll_rate_sign WHERE is_starred = 1")
    LiveData<List<TollRateGroup>> loadFavoriteTollRateGroups();

    @Entity(tableName = "toll_trip",
            foreignKeys = @ForeignKey(entity = TollRateSignEntity.class,
                    parentColumns = "id",
                    childColumns = "sign_id",
                    onDelete = ForeignKey.CASCADE))
    class TollTripEntity {

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
        private Double tollRate = 0.0;

        @ColumnInfo(name = "message")
        private String message;

        @ColumnInfo(name = "end_milepost")
        private Integer endMilepost;

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

        public Double getTollRate() {
            return tollRate;
        }

        public void setTollRate(Double tollRate) {
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

        public Integer getEndMilepost() {
            return endMilepost;
        }

        public void setEndMilepost(Integer endMilepost) {
            this.endMilepost = endMilepost;
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
}
