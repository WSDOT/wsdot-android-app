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
