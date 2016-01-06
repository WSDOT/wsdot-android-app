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

/**
 * Simple object class for icons drawn on the traffic map.
 */
public class CalloutItem implements Serializable {
    private static final long serialVersionUID = 1391850823921985693L;
    private String title;
	private String imageUrl;
    private Double latitude;
	private Double longitude;
	
	/**
	 * Constructor
	 */
	public CalloutItem() {
	}
	
	/**
	 * Constructor
	 * 
	 * @param title
	 * @param imageUrl
	 * @param latitude
	 * @param longitude
	 */
	public CalloutItem(String title, String imageUrl, Double latitude, Double longitude) {
	    this.title = title;
	    this.imageUrl = imageUrl;
	    this.latitude = latitude;
	    this.longitude = longitude;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
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