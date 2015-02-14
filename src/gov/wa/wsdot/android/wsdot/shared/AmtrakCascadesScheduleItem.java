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

import java.io.Serializable;

public class AmtrakCascadesScheduleItem implements Serializable {

    private static final long serialVersionUID = 673409068915778886L;
    private String arrivalComment;
    private String arrivalScheduleType;
    private String arrivalTime;
    private String departureComment;
    private String departureScheduleType;
    private String departureTime;
    private String scheduledArrivalTime;
    private String scheduledDepartureTime;
    private int sortOrder;
    private String stationName;
    private String trainMessage;
    private int trainNumber;
    private int tripNumber;
    private String trainName;
    private String updateTime;
	
    public AmtrakCascadesScheduleItem() {
    }

    public String getArrivalComment() {
        return arrivalComment;
    }

    public void setArrivalComment(String arrivalComment) {
        this.arrivalComment = arrivalComment;
    }

    public String getArrivalScheduleType() {
        return arrivalScheduleType;
    }

    public void setArrivalScheduleType(String arrivalScheduleType) {
        this.arrivalScheduleType = arrivalScheduleType;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureComment() {
        return departureComment;
    }

    public void setDepartureComment(String departureComment) {
        this.departureComment = departureComment;
    }

    public String getDepartureScheduleType() {
        return departureScheduleType;
    }

    public void setDepartureScheduleType(String departureScheduleType) {
        this.departureScheduleType = departureScheduleType;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getScheduledArrivalTime() {
        return scheduledArrivalTime;
    }

    public void setScheduledArrivalTime(String scheduledArrivalTime) {
        this.scheduledArrivalTime = scheduledArrivalTime;
    }
    
    public String getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public void setScheduledDepartureTime(String scheduledDepartureTime) {
        this.scheduledDepartureTime = scheduledDepartureTime;
    }
    
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }
    
    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getTrainMessage() {
        return trainMessage;
    }

    public void setTrainMessage(String trainMessage) {
        this.trainMessage = trainMessage;
    }

    public int getTripNumber() {
        return tripNumber;
    }

    public void setTripNumber(int tripNumber) {
        this.tripNumber = tripNumber;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

}
