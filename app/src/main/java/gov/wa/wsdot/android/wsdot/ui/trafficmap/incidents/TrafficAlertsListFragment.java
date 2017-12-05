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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.incidents;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.alert.AlertsListFragment;

public class TrafficAlertsListFragment extends AlertsListFragment {

    protected ArrayList<HighwayAlertsItem> getAlerts(ArrayList<HighwayAlertsItem> alerts){

        //Retrieve the bounds from the intent. Defaults to 0
        Intent intent = getActivity().getIntent();

        Double nelat = intent.getDoubleExtra("nelat", 0.0);
        Double nelong = intent.getDoubleExtra("nelong", 0.0);
        Double swlat = intent.getDoubleExtra("swlat", 0.0);
        Double swlong = intent.getDoubleExtra("swlong", 0.0);

        LatLng northEast = new LatLng(nelat, nelong);
        LatLng southWest = new LatLng(swlat, swlong);

        LatLngBounds mBounds = new LatLngBounds(southWest, northEast);

        ArrayList<HighwayAlertsItem> alertsInArea = new ArrayList<>();

        for (HighwayAlertsItem alert: alerts) {
            LatLng alertStartLocation = new LatLng(alert.getStartLatitude(), alert.getStartLongitude());

            // If alert is within bounds of shown on screen show it on list
            if (mBounds.contains(alertStartLocation) ||
                    alert.getEventCategory().toLowerCase().equals("amber")) {
                alertsInArea.add(alert);
            }
        }
        return alertsInArea;
    }
}
