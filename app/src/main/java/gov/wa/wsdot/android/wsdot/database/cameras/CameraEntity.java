package gov.wa.wsdot.android.wsdot.database.cameras;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

@Entity(tableName = "cameras")
public class CameraEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer cameraId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "direction")
    private String direction;

    @ColumnInfo(name = "milepost")
    private String milepost;

    @ColumnInfo(name = "has_video")
    @NonNull
    private Integer hasVideo = 0;

    @ColumnInfo(name = "road_name")
    private String roadName;

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Integer getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDirection() { return direction; }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMilepost() { return milepost; }

    public void setMilepost(String milepost){
        this.milepost = milepost;
    }

    @NonNull
    public Integer getHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(@NonNull Integer hasVideo) {
        this.hasVideo = hasVideo;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }
}
