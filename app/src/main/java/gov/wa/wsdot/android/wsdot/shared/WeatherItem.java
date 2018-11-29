package gov.wa.wsdot.android.wsdot.shared;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class WeatherItem implements ClusterItem {

    private String source;

    private Double windSpeed;
    private Double windDirection;

    private String report;

    private Double latitude;
    private Double longitude;

    private String updated;

    public String getSource(){
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getReport() {
        return this.report;
    }

    public void setReport(String report) {
        this.report = report;
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

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getUpdated() {
        return this.updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public WeatherItem(String source, Double windSpeed, Double windDirection, String report, Double latitude, Double longitude, String updated){
        this.source = source;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.report = report;
        this.latitude = latitude;
        this.longitude = longitude;
        this.updated = updated;
        this.clusterPosition = new LatLng(latitude, longitude);
    }

    // Cluster logic
    private final LatLng clusterPosition;

    @Override
    public LatLng getPosition() {
        return this.clusterPosition;
    }
}
