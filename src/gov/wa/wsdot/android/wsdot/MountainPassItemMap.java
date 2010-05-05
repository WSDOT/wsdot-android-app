package gov.wa.wsdot.android.wsdot;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;

import android.os.Bundle;
import android.widget.ImageView;

public class MountainPassItemMap extends MapActivity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_tabs);
        Bundle b = getIntent().getExtras();
        Float latitude = new Float(b.getString("Latitude"));
        Float longitude = new Float(b.getString("Longitude"));
        Integer weatherIcon = b.getInt("WeatherIcon");

        final MapView map = (MapView) findViewById(R.id.mapview);
        map.setSatellite(false);
        final MapController mapControl = map.getController();
        mapControl.setZoom(12);
        map.setBuiltInZoomControls(true);
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        mapControl.animateTo(newPoint);
        
        MapView.LayoutParams mapMarkerParams = new MapView.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
                newPoint, MapView.LayoutParams.TOP_LEFT );
        ImageView mapMarker = new ImageView(getApplicationContext());
        mapMarker.setImageResource(weatherIcon);
        map.addView(mapMarker, mapMarkerParams);        
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
