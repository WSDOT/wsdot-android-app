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

public class FerriesTerminalItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497969464783561721L;
	private Integer departingTerminalID;
	private String departingTerminalName;
	private Integer arrivingTerminalID;
	private String arrivingTerminalName;
	private ArrayList<FerriesAnnotationsItem> annotations = new ArrayList<FerriesAnnotationsItem>();
	private ArrayList<FerriesScheduleTimesItem> times = new ArrayList<FerriesScheduleTimesItem>();
	
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
	
	public ArrayList<FerriesAnnotationsItem> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(FerriesAnnotationsItem annotations) {
		this.annotations.add(annotations);
	}
	
	public ArrayList<FerriesScheduleTimesItem> getScheduleTimes() {
		return times;
	}
	public void setScheduleTimes(FerriesScheduleTimesItem times) {
		this.times.add(times);		
	}
}
