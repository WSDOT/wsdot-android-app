package gov.wa.wsdot.android.wsdot.database.ferries;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

@Entity(tableName = "weather_report")
public class WeatherReportEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "source")
    private String source;

    @ColumnInfo(name = "temperature")
    private Double temperature;

    @ColumnInfo(name = "wind_speed")
    private Double windSpeed;

    @ColumnInfo(name = "wind_direction")
    private Double windDirection;

    @ColumnInfo(name = "report")
    private String report;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "updated")
    private String updated;

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getReport() {
        return this.report;
    }

    public void setReport(String report){
        this.report = report;
    }

    public Double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getWindSpeed() {
        return this.windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getWindDirection() {
        return this.windDirection;
    }

    public void setWindDirection(Double windDirection) {
        this.windDirection = windDirection;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude){
        this.longitude = longitude;
    }

    public String getUpdated() {
        return this.updated;
    }

    public void setUpdated(String updated){
        this.updated = updated;
    }

}
