package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "border_waits")
public class BorderWaitEntity {

    @ColumnInfo(name = "id")
    @PrimaryKey
    public int id;

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
    private Integer isStarred;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getRoute() {
        return route;
    }
    public void setRoute(Integer route) {
        this.route = route;
    }
    public Integer getWait() {
        return wait;
    }
    public void setWait(Integer wait) {
        this.wait = wait;
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
    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }
    public Integer getIsStarred() { return isStarred; }
    public void setIsStarred(Integer isStarred) { this.isStarred = isStarred; }

}