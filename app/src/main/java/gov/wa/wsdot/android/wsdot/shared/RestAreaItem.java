package gov.wa.wsdot.android.wsdot.shared;


import java.util.ArrayList;
import java.util.List;

public class RestAreaItem {

    private String route;
    private String location;
    private int milepost;
    private String direction;
    private Double latitude;
    private Double longitude;
    private Integer icon;
    private String notes;

    private List<String> amenities = new ArrayList<>();

    public RestAreaItem() {}

    public String getLocation(){return this.location;}
    public void setLocation(String location){this.location = location;}

    public String getRoute(){return this.route;}
    public void setRoute(String route){this.route = route;}

    public int getMilepost(){return this.milepost;}
    public void setMilepost(int milepost){this.milepost = milepost;}

    public String getDirection(){return this.direction;}
    public void setDirection(String direction){this.direction = direction;}

    public Double getLatitude(){return this.latitude;}
    public void setLatitude(Double latitude){this.latitude = latitude;}

    public Double getLongitude(){return this.longitude;}
    public void setLongitude(Double longitude){this.longitude = longitude;}

    public Integer getIcon(){return this.icon;}
    public void setIcon(Integer icon){this.icon = icon;}

    public String getNotes(){return this.notes;}
    public void setNotes(String restrictions){this.notes = restrictions;}

    public List<String> getAmenities(){return this.amenities;}
    public void addAmenitie(String amenities){this.amenities.add(amenities);}
}
