package gov.wa.wsdot.android.wsdot.database.highwayalerts;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

@Entity(tableName = "highway_alerts")
public class HighwayAlertEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "highway_alert_id")
    private Integer alertId;

    @ColumnInfo(name = "highway_alert_headline")
    private String headline;

    @ColumnInfo(name = "highway_alert_start_latitude")
    private Double startLatitude;

    @ColumnInfo(name = "highway_alert_start_longitude")
    private Double startLongitude;

    @ColumnInfo(name = "highway_alert_end_latitude")
    private Double endLatitude;

    @ColumnInfo(name = "highway_alert_end_longitude")
    private Double endLongitude;

    @ColumnInfo(name = "highway_alert_category")
    private String category;

    @ColumnInfo(name = "highway_alert_priority")
    private String priority;

    @ColumnInfo(name = "highway_alert_road_name")
    private String roadName;

    @ColumnInfo(name = "highway_alert_last_updated")
    private String lastUpdated;

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
