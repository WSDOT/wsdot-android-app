package gov.wa.wsdot.android.wsdot.database.myroute;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

@Entity(tableName = "my_route")
public class MyRouteEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer myRouteId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "route_locations")
    private String route_locations;

    @ColumnInfo(name = "latitude")
    private Integer latitude;

    @ColumnInfo(name = "longitude")
    private Integer longitude;

    @ColumnInfo(name = "zoom")
    private Integer zoom;

    @ColumnInfo(name = "found_favorites")
    @NonNull
    private Integer foundFavorites = 0;

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Integer getMyRouteId() {
        return myRouteId;
    }

    public void setMyRouteId(Integer myRouteId) {
        this.myRouteId = myRouteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRoute_locations() {
        return route_locations;
    }

    public void setRoute_locations(String route_locations) {
        this.route_locations = route_locations;
    }

    public Integer getLatitude() {
        return latitude;
    }

    public void setLatitude(Integer latitude) {
        this.latitude = latitude;
    }

    public Integer getLongitude() {
        return longitude;
    }

    public void setLongitude(Integer longitude) {
        this.longitude = longitude;
    }

    public Integer getZoom() {
        return zoom;
    }

    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }

    @NonNull
    public Integer getFoundFavorites() {
        return foundFavorites;
    }

    public void setFoundFavorites(@NonNull Integer foundFavorites) {
        this.foundFavorites = foundFavorites;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }
}
