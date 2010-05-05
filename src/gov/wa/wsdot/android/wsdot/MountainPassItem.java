package gov.wa.wsdot.android.wsdot;

import java.util.ArrayList;

public class MountainPassItem {

	private String weatherCondition;
	private String elevationInFeet;
	private String travelAdvisoryActive;
	private String longitude;
	private String mountainPassId;
	private String roadCondition;
	private String temperatureInFahrenheit;
	private String latitude;
	private String dateUpdated;
	private String mountainPassName;
	private String restrictionOneText;
	private String restrictionOneTravelDirection;
	private String restrictionTwoText;
	private String restrictionTwoTravelDirection;
	private Integer weatherIcon;
	private ArrayList<String> cameraUrls;
	
	public String getWeatherCondition() {
		return weatherCondition;
	}
	public void setWeatherCondition(String weatherCondition) {
		this.weatherCondition = weatherCondition;
	}
	public String getElevationInFeet() {
		return elevationInFeet;
	}
	public void setElevationInFeet(String elevationInFeet) {
		this.elevationInFeet = elevationInFeet;
	}
	public String getTravelAdvisoryActive() {
		return travelAdvisoryActive;
	}
	public void setTravelAdvisoryActive(String travelAdvisoryActive) {
		this.travelAdvisoryActive = travelAdvisoryActive; 
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getMountainPassId() {
		return mountainPassId;
	}
	public void setMountainPassId(String mountainPassId) {
		this.mountainPassId = mountainPassId;
	}
	public String getRoadCondition() {
		return roadCondition;
	}
	public void setRoadCondition(String roadCondition) {
		this.roadCondition = roadCondition;
	}
	public String getTemperatureInFahrenheit() {
		return temperatureInFahrenheit;
	}
	public void setTemperatureInFahrenheit(String temperatureInFahrenheit) {
		this.temperatureInFahrenheit = temperatureInFahrenheit;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public String getMountainPassName() {
		return mountainPassName;
	}
	public void setMountainPassName(String mountainPassName) {
		this.mountainPassName = mountainPassName;
	}
	public String getRestrictionOneText() {
		return restrictionOneText;
	}
	public void setRestrictionOneText(String restrictionOneText) {
		this.restrictionOneText = restrictionOneText;
	}
	public String getRestrictionOneTravelDirection() {
		return restrictionOneTravelDirection;
	}
	public void setRestrictionOneTravelDirection(String restrictionOneTravelDirection) {
		this.restrictionOneTravelDirection = restrictionOneTravelDirection;
	}
	public String getRestrictionTwoText() {
		return restrictionTwoText;
	}
	public void setRestrictionTwoText(String restrictionTwoText) {
		this.restrictionTwoText = restrictionTwoText;
	}
	public String getRestrictionTwoTravelDirection() {
		return restrictionTwoTravelDirection;
	}
	public void setRestrictionTwoTravelDirection(String restrictionTwoTravelDirection) {
		this.restrictionTwoTravelDirection = restrictionTwoTravelDirection;
	}
	public Integer getWeatherIcon() {
		return weatherIcon;
	}
	public void setWeatherIcon(Integer weatherIcon) {
		this.weatherIcon = weatherIcon;
	}
	public ArrayList<String> getCameraUrls() {
		return cameraUrls;
	}
	public void setCameraUrls(ArrayList<String> cameraUrls) {
		this.cameraUrls = cameraUrls;
	}
}