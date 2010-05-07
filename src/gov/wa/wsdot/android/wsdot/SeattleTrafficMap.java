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
