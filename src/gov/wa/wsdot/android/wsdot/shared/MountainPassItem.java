/*
 * Copyright (c) 2010 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.shared;


import java.io.Serializable;
import java.util.ArrayList;

public class MountainPassItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6136235109640235027L;
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
	private boolean selected;
	private ArrayList<CameraItem> camera = new ArrayList<CameraItem>();
	private ArrayList<ForecastItem> forecast = new ArrayList<ForecastItem>();
	
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
	public ArrayList<CameraItem> getCameraItem() {
		return camera;
	}
	public void setCameraItem(CameraItem camera) {
		this.camera.add(camera);
	}
	public ArrayList<ForecastItem> getForecastItem() {
		return forecast;
	}
	public void setForecastItem(ForecastItem forecast) {
		this.forecast.add(forecast);
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}	
}