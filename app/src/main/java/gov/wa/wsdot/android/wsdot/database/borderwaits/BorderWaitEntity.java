package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "border_wait")
public class BorderWaitEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer borderWaitId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "updated")
    private String updated;

    @ColumnInfo(name = "lane")
    private String lane;

    @ColumnInfo(name = "route")
    private Integer route;

    @ColumnInfo(name = "direction")
    private String direction;

    @ColumnInfo(name = "wait")
    private Integer wait;

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Integer getBorderWaitId() {
        return this.borderWaitId;
    }

    public void setBorderWaitId(int borderWaitId) {
        this.borderWaitId = borderWaitId;
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

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Integer getRoute() {
        return route;
    }

    public void setRoute(Integer route) {
        this.route = route;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getWait() {
        return wait;
    }

    public void setWait(Integer wait) {
        this.wait = wait;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }
}