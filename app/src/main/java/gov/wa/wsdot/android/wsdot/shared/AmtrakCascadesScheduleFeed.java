/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

public class AmtrakCascadesScheduleFeed {
    private String ArrivalComment;
    private String ArrivalScheduleType;
    private String ArrivalTime;
    private String DepartureComment;
    private String DepartureScheduleType;
    private String DepartureTime;
    private String ScheduledArrivalTime;
    private String ScheduledDepartureTime;
    private int SortOrder;
    private String StationName;
    private String TrainMessage;
    private int TrainNumber;
    private int TripNumber;
    private String TrainName;
    private String UpdateTime;
	
    public AmtrakCascadesScheduleFeed() {
    }

    public String getArrivalComment() {
        return ArrivalComment;
    }

    public void setArrivalComment(String arrivalComment) {
        ArrivalComment = arrivalComment;
    }

    public String getArrivalScheduleType() {
        return ArrivalScheduleType;
    }

    public void setArrivalScheduleType(String arrivalScheduleType) {
        ArrivalScheduleType = arrivalScheduleType;
    }

    public String getArrivalTime() {
        return ArrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        ArrivalTime = arrivalTime;
    }

    public String getDepartureComment() {
        return DepartureComment;
    }

    public void setDepartureComment(String departureComment) {
        DepartureComment = departureComment;
    }

    public String getDepartureScheduleType() {
        return DepartureScheduleType;
    }

    public void setDepartureScheduleType(String departureScheduleType) {
        DepartureScheduleType = departureScheduleType;
    }

    public String getDepartureTime() {
        return DepartureTime;
    }

    public void setDepartureTime(String departureTime) {
        DepartureTime = departureTime;
    }

    public String getScheduledArrivalTime() {
        return ScheduledArrivalTime;
    }

    public void setScheduledArrivalTime(String scheduledArrivalTime) {
        ScheduledArrivalTime = scheduledArrivalTime;
    }

    public String getScheduledDepartureTime() {
        return ScheduledDepartureTime;
    }

    public void setScheduledDepartureTime(String scheduledDepartureTime) {
        ScheduledDepartureTime = scheduledDepartureTime;
    }

    public int getSortOrder() {
        return SortOrder;
    }

    public void setSortOrder(int sortOrder) {
        SortOrder = sortOrder;
    }

    public String getStationName() {
        return StationName;
    }

    public void setStationName(String stationName) {
        StationName = stationName;
    }

    public String getTrainMessage() {
        return TrainMessage;
    }

    public void setTrainMessage(String trainMessage) {
        TrainMessage = trainMessage;
    }

    public int getTrainNumber() {
        return TrainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        TrainNumber = trainNumber;
    }

    public int getTripNumber() {
        return TripNumber;
    }

    public void setTripNumber(int tripNumber) {
        TripNumber = tripNumber;
    }

    public String getTrainName() {
        return TrainName;
    }

    public void setTrainName(String trainName) {
        TrainName = trainName;
    }

    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String updateTime) {
        UpdateTime = updateTime;
    }

}
