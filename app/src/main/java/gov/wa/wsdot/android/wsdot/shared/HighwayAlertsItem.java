/*
 * Copyright (c) 2014 Washington State Department of Transportation
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

public class HighwayAlertsItem {
    private String priority;
    private String lastUpdatedTime;
    private String alertId;
    private Double endLatitude;
    private Double endLongitude;
    private String region;
    private String eventCategory;
    private String county;
    private String extendedDescription;
    private String eventStatus;
    private String startTime;
    private String endTime;
    private String headlineDescription;
    private Double startLatitude;
    private Double startLongitude;
    private Integer categoryIcon;

    public HighwayAlertsItem() {
    }

    public HighwayAlertsItem(String headline){
        this.headlineDescription = headline;
    }

    public HighwayAlertsItem(Double startLatitude, Double startLongitude,
                             Double endLatitude, Double endLongitude,
                             String eventCategory, String headlineDescription,
                             String lastUpdatedTime) {
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.eventCategory = eventCategory;
        this.headlineDescription = headlineDescription;
        this.lastUpdatedTime = lastUpdatedTime;
    }
    
    public HighwayAlertsItem(String alertId, Double startLatitude, Double startLongitude,
                             Double endLatitude, Double endLongitude,
                             String eventCategory, String headlineDescription, String lastUpdatedTime,
                             String priority, Integer categoryIcon) {
        this.alertId = alertId;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.startLongitude = startLongitude;
        this.eventCategory = eventCategory;
        this.headlineDescription = headlineDescription;
        this.lastUpdatedTime = lastUpdatedTime;
        this.priority = priority;
        this.categoryIcon = categoryIcon;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getHeadlineDescription() {
        return headlineDescription;
    }

    public void setHeadlineDescription(String headlineDescription) {
        this.headlineDescription = headlineDescription;
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

    public Integer getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(Integer categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof HighwayAlertsItem) {
            HighwayAlertsItem alert = (HighwayAlertsItem) object;
            if (this.alertId == null) {
                return (alert.alertId == null);
            }
            else {
                return alertId.equals(alert.alertId);
            }
        }
        return false;
    }
}