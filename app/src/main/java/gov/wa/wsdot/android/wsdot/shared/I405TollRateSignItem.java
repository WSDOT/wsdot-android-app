package gov.wa.wsdot.android.wsdot.shared;

import java.util.ArrayList;
import java.util.List;

public class I405TollRateSignItem {

    private String startLocationName;
    private int stateRoute;
    private String travelDirection;
    private Double startLatitude;
    private Double startLongitude;
    private List<I405TripItem> trips = new ArrayList<>();

    public String getStartLocationName() {
        return startLocationName;
    }

    public void setStartLocationName(String startLocationName) {
        this.startLocationName = startLocationName;
    }

    public int getStateRoute() {
        return stateRoute;
    }

    public void setStateRoute(int stateRoute) {
        this.stateRoute = stateRoute;
    }

    public String getTravelDirection() {
        return travelDirection;
    }

    public void setTravelDirection(String travelDirection) {
        this.travelDirection = travelDirection;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public List<I405TripItem> getTrips() {
        return trips;
    }

    public void setTrips(List<I405TripItem> trips) {
        this.trips = trips;
    }
}
