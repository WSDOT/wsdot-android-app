package gov.wa.wsdot.android.wsdot;

import java.text.DecimalFormat;

public class LatLonItem {
	private double latitude;
	private double longitude;
	private DecimalFormat df = new DecimalFormat("###.#####");
	
	public LatLonItem(double lattitude, double longitude) {
		this.latitude = lattitude;
		this.longitude = longitude;
	}
	
	public String toString() {
		return df.format(latitude) + ", " + df.format(longitude);
	}
	
	public LatLonItem() {
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
