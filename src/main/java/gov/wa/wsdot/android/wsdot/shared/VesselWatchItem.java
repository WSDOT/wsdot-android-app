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

public class VesselWatchItem {

	private String inservice;
	private Integer vesselID;
	private String name;
	private String route;
    private String description;
	private String lastdock;
	private String arrivingTerminal;
	private String nextdep;
	private Double lat;
	private Double lon;
	private Double speed;
	private Integer head;
	private String headtxt;
	private String datetime;
	private Integer icon;
	
	public VesselWatchItem() {
	}
	
    public VesselWatchItem(Double latitude, Double longitude, String name,
            String description, Integer icon) {
	    this.lat = latitude;
	    this.lon = longitude;
	    this.name = name;
	    this.description = description;
	    this.icon = icon;
	}
	
	public String getInService() {
		return inservice;
	}
	public void setInService(String inservice) {
		this.inservice = inservice;
	}
	public Integer getVesselId() {
		return vesselID;
	}
	public void setVesselID(Integer vesselID) {
		this.vesselID = vesselID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route; 
	}
	public String getLastDock() {
		return lastdock;
	}
	public void setLastDock(String lastdock) {
		this.lastdock = lastdock; 
	}
	public String getArrivingTerminal() {
		return arrivingTerminal;
	}
	public void setArrivingTerminal(String arrivingTerminal) {
		this.arrivingTerminal = arrivingTerminal;
	}
	public String getNextDep() {
		return nextdep;
	}
	public void setNextDep(String nextdep) {
		this.nextdep = nextdep; 
	}
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLon() {
		return lon;
	}
	public void setLon(Double lon) {
		this.lon = lon;
	}
	public Double getSpeed() {
		return speed;
	}
	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	public Integer getHead() {
		return head;
	}
	public void setHead(Integer head) {
		this.head = head;
	}
	public String getHeadTxt() {
		return headtxt;
	}
	public void setHeadTxt(String headtxt) {
		this.headtxt = headtxt;
	}
	public String getDateTime() {
		return datetime;
	}
	public void setDateTime(String datetime) {
		this.datetime = datetime;
	}
	public Integer getIcon() {
		return icon;
	}
	public void setIcon(Integer icon) {
		this.icon = icon;
	}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}