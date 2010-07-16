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

import java.io.Serializable;

public class FerriesRouteAlertItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5570654741856182708L;
	private Integer bulletinID;
	private String publishDate;
	private String alertDescription;
	private String alertFullTitle;
	private String alertFullText;
	
	public Integer getBulletinID() {
		return bulletinID;
	}
	public void setBulletinID(Integer bulletinID) {
		this.bulletinID = bulletinID;
	}
	
	public String getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}
	
	public String getAlertDescription() {
		return alertDescription;
	}
	public void setAlertDescription(String alertDescription) {
		this.alertDescription = alertDescription;
	}
	
	public String getAlertFullTitle() {
		return alertFullTitle;
	}
	public void setAlertFullTitle(String alertFullTitle) {
		this.alertFullTitle = alertFullTitle;
	}
	
	public String getAlertFullText() {
		return alertFullText;
	}
	public void setAlertFullText(String alertFullText) {
		this.alertFullText = alertFullText;
	}
}
