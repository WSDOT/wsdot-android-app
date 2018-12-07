package gov.wa.wsdot.android.wsdot.database.mountainpasses;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

@Entity(tableName = "mountain_passes")
public class MountainPassEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Integer passId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "weather_condition")
    private String weatherCondition;

    @ColumnInfo(name = "elevation")
    private String elevation;

    @ColumnInfo(name = "travel_advisory_active")
    private String travelAdisory;

    @ColumnInfo(name = "road_condition")
    private String roadCondition;

    @ColumnInfo(name = "temperature")
    private String temperature;

    @ColumnInfo(name = "date_updated")
    private String dateUpdated;

    @ColumnInfo(name = "restriction_one")
    private String restrictionOne;

    @ColumnInfo(name = "restriction_one_direction")
    private String restrictionOneDirection;

    @ColumnInfo(name = "restriction_two")
    private String restrictionTwo;

    @ColumnInfo(name = "restriction_two_direction")
    private String restrictionTwoDirection;

    @ColumnInfo(name = "camera")
    private String camera;

    @ColumnInfo(name = "forecast")
    private String forecast;

    @ColumnInfo(name = "weather_icon")
    private String weatherIcon;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Integer getId() { return id; }

    public Integer getPassId() {
        return passId;
    }

    public void setPassId(Integer passId) {
        this.passId = passId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public String getTravelAdisory() {
        return travelAdisory;
    }

    public void setTravelAdisory(String travelAdisory) {
        this.travelAdisory = travelAdisory;
    }

    public String getRoadCondition() {
        return roadCondition;
    }

    public void setRoadCondition(String roadCondition) {
        this.roadCondition = roadCondition;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getRestrictionOne() {
        return restrictionOne;
    }

    public void setRestrictionOne(String restrictionOne) {
        this.restrictionOne = restrictionOne;
    }

    public String getRestrictionOneDirection() {
        return restrictionOneDirection;
    }

    public void setRestrictionOneDirection(String restrictionOneDirection) {
        this.restrictionOneDirection = restrictionOneDirection;
    }

    public String getRestrictionTwo() {
        return restrictionTwo;
    }

    public void setRestrictionTwo(String restrictionTwo) {
        this.restrictionTwo = restrictionTwo;
    }

    public String getRestrictionTwoDirection() {
        return restrictionTwoDirection;
    }

    public void setRestrictionTwoDirection(String restrictionTwoDirection) {
        this.restrictionTwoDirection = restrictionTwoDirection;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
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

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }
}
