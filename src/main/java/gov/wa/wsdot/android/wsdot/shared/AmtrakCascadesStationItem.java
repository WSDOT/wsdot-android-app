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
import java.util.Comparator;

public class AmtrakCascadesStationItem implements Serializable {

    private static final long serialVersionUID = 6651744587890383235L;
    
    private String stationCode;
    private String stationName;
    private int sortOrder;
    private Double latitude;
    private Double longitude;
    private int distance;
    
    public AmtrakCascadesStationItem() {
    }
    
    /**
     * 
     * @param stationCode
     * @param stationName
     * @param sortOrder
     * @param latitude
     * @param longitude
     */
    public AmtrakCascadesStationItem(String stationCode, String stationName,
            int sortOrder, Double latitude, Double longitude) {
        
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.sortOrder = sortOrder;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = -1;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AmtrakCascadesStationItem && obj != null
                && ((AmtrakCascadesStationItem) obj).getStationCode().equalsIgnoreCase(this.stationCode);
    }
    
    public static Comparator<AmtrakCascadesStationItem> stationDistanceComparator = new Comparator<AmtrakCascadesStationItem>() {
        public int compare(AmtrakCascadesStationItem station1, AmtrakCascadesStationItem station2) {
            int stationDistance1 = station1.getDistance();
            int stationDistance2 = station2.getDistance();

            // Ascending order
            return stationDistance1 - stationDistance2;
        }
    };
    
    public static Comparator<AmtrakCascadesStationItem> stationOrderComparator = new Comparator<AmtrakCascadesStationItem>() {
        public int compare(AmtrakCascadesStationItem station1, AmtrakCascadesStationItem station2) {
            int stationOrder1 = station1.getSortOrder();
            int stationOrder2 = station2.getSortOrder();

            // Ascending order
            return stationOrder1 - stationOrder2;
        }
    };

    public static Comparator<AmtrakCascadesStationItem> stationNameComparator = new Comparator<AmtrakCascadesStationItem>() {
        public int compare(AmtrakCascadesStationItem station1, AmtrakCascadesStationItem station2) {
            String stationName1 = station1.getStationName().toUpperCase();
            String stationName2 = station2.getStationName().toUpperCase();

            // Ascending order
            return stationName1.compareTo(stationName2);
        }
    };

}
