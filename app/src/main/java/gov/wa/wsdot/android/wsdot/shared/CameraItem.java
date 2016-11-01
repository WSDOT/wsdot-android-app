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

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.Comparator;

public class CameraItem implements Serializable, ClusterItem {
	private static final long serialVersionUID = 7852844361445836316L;
	private String title;
	private String imageUrl;
	private String roadName;
	private Double longitude;
	private Double latitude;
	private Drawable image;
	private Integer cameraId;
	private Integer cameraIcon;
	private Integer distance;
	private final LatLng  clusterPosition;

    public CameraItem(){
        clusterPosition = null;
    }

    public CameraItem(Double latitude, Double longitude, String title,
            Integer cameraId, Integer cameraIcon) {
	    this.latitude = latitude;
	    this.longitude = longitude;
	    this.title = title;
	    this.cameraId = cameraId;
	    this.cameraIcon = cameraIcon;
		this.clusterPosition = new LatLng(latitude, longitude);
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
	public String getRoadName() {
		return roadName;
	}
	public void setRoadName(String roadName) {
		this.roadName = roadName;
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
	public Drawable getImage() {
		return image;
	}
	public void setImage(Drawable image) {
		this.image = image;
	}
	public Integer getCameraId() {
		return cameraId;
	}
	public void setCameraId(Integer cameraId) {
		this.cameraId = cameraId;
	}

    public Integer getCameraIcon() {
        return cameraIcon;
    }

    public void setCameraIcon(Integer cameraIcon) {
        this.cameraIcon = cameraIcon;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }
    
    public static Comparator<CameraItem> cameraDistanceComparator = new Comparator<CameraItem>() {
        public int compare(CameraItem o1, CameraItem o2) {
            int cameraDistance1 = o1.getDistance();
            int cameraDistance2 = o2.getDistance();
            
            return cameraDistance1 - cameraDistance2;
        }
    };

	@Override
	public LatLng getPosition() {
		return this.clusterPosition;
	}
}
