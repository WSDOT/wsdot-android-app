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

package gov.wa.wsdot.android.wsdot;

import java.util.ArrayList;
import java.io.Serializable;

public class FerriesRouteItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84316089995566867L;
	private Integer routeID;
	private String description;
	private ArrayList<FerriesRouteAlertItem> routeAlert = new ArrayList<FerriesRouteAlertItem>();
	private ArrayList<FerriesScheduleDateItem> scheduleDate = new ArrayList<FerriesScheduleDateItem>();

	public Integer getRouteID() {
		return routeID;
	}
	public void setRouteID(Integer routeID) {
		this.routeID = routeID;
	}
		
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public ArrayList<FerriesRouteAlertItem> getFerriesRouteAlertItem() {
		return routeAlert;
	}
	public void setFerriesRouteAlertItem(FerriesRouteAlertItem routeAlert) {
		this.routeAlert.add(routeAlert);
	}
	
	public ArrayList<FerriesScheduleDateItem> getFerriesScheduleDateItem() {
		return scheduleDate;
	}
	public void setFerriesScheduleDateItem(FerriesScheduleDateItem scheduleDate) {
		this.scheduleDate.add(scheduleDate);
	}
}
