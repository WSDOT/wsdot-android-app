package gov.wa.wsdot.android.wsdot;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class SeattleTrafficMap extends TrafficMap {
		
	void prepareMap() {
		super.setContentView(R.layout.map_tabs);

		Double latitude = 47.5990;
        Double longitude = -122.3350;
        map = (MapView) findViewById(R.id.mapview);
        map.setSatellite(false);
        final MapController mapControl = map.getController();
        mapControl.setZoom(12);
        map.setBuiltInZoomControls(true);
        map.setTraffic(true);
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        mapControl.animateTo(newPoint);
	}
}
