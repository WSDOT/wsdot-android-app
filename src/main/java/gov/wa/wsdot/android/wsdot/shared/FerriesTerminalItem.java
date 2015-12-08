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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FerriesTerminalItem implements Serializable {

    private static final long serialVersionUID = -3497969464783561721L;
	private Integer departingTerminalID;
	private String departingTerminalName;
	private Integer arrivingTerminalID;
	private String arrivingTerminalName;
	private List<FerriesAnnotationsItem> annotations = new ArrayList<FerriesAnnotationsItem>();
	private List<FerriesScheduleTimesItem> times = new ArrayList<FerriesScheduleTimesItem>();
    private Double latitude;
    private Double longitude;

    /**
     * Default constructor
     */
    public FerriesTerminalItem() {
    }

    /**
     *
     * @param departingTerminalID  Unique identifier for departing terminal
     * @param departingTerminalName  The name of the terminal
     * @param latitude  The latitude of the terminal
     * @param longitude  The longitude of the terminal
     */
    public FerriesTerminalItem(Integer departingTerminalID, String departingTerminalName,
            Double latitude, Double longitude) {

        this.departingTerminalID = departingTerminalID;
        this.departingTerminalName = departingTerminalName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getDepartingTerminalID() {
		return departingTerminalID;
	}
	public void setDepartingTerminalID(Integer departingTerminalID) {
		this.departingTerminalID = departingTerminalID;
	}
	
	public String getDepartingTerminalName() {
		return departingTerminalName;
	}
	public void setDepartingTerminalName(String departingTerminalName) {
		this.departingTerminalName = departingTerminalName;
	}
	
	public Integer getArrivingTerminalID() {
		return arrivingTerminalID;
	}
	public void setArrivingTerminalID(Integer arrivingTerminalID) {
		this.arrivingTerminalID = arrivingTerminalID;
	}
	
	public String getArrivingTerminalName() {
		return arrivingTerminalName;
	}
	public void setArrivingTerminalName(String arrivingTerminalName) {
		this.arrivingTerminalName = arrivingTerminalName;
	}
	
	public List<FerriesAnnotationsItem> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(FerriesAnnotationsItem annotations) {
		this.annotations.add(annotations);
	}
	public List<FerriesScheduleTimesItem> getScheduleTimes() {
		return times;
	}
	public void setScheduleTimes(FerriesScheduleTimesItem times) {
		this.times.add(times);		
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

}
