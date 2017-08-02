/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

public class FerriesScheduleTimesItem implements Serializable {

	private static final long serialVersionUID = -4358683796023869875L;
	private String departingTime;
	private String arrivingTime;
	private ArrayList<FerriesAnnotationIndexesItem> annotationIndexes = new ArrayList<FerriesAnnotationIndexesItem>();
    private int reservableSpaceCount;
    private int driveUpSpaceCount;
    private int maxSpaceCount;
    private String lastUpdated;
	
    public FerriesScheduleTimesItem() {
        this.reservableSpaceCount = -1;
        this.driveUpSpaceCount = -1;
        this.maxSpaceCount = -1;
    }
    
	public String getDepartingTime() {
		return departingTime;
	}
	public void setDepartingTime(String departingTime) {
		this.departingTime = departingTime;
	}
	public String getArrivingTime() {
		return arrivingTime;
	}
	public void setArrivingTime(String arrivingTime) {
		this.arrivingTime = arrivingTime;
	}
	public ArrayList<FerriesAnnotationIndexesItem> getAnnotationIndexes() {
		return annotationIndexes;
	}
	public void setAnnotationIndexes(FerriesAnnotationIndexesItem annotationIndexes) {
		this.annotationIndexes.add(annotationIndexes);
	}
    public int getReservableSpaceCount() {
        return reservableSpaceCount;
    }
    public void setReservableSpaceCount(int reservableSpaceCount) {
        this.reservableSpaceCount = reservableSpaceCount;
    }
    public int getDriveUpSpaceCount() {
        return driveUpSpaceCount;
    }
    public void setDriveUpSpaceCount(int driveUpSpaceCount) {
        this.driveUpSpaceCount = driveUpSpaceCount;
    }
    public int getMaxSpaceCount() {
        return maxSpaceCount;
    }
    public void setMaxSpaceCount(int maxSpaceCount) {
        this.maxSpaceCount = maxSpaceCount;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
