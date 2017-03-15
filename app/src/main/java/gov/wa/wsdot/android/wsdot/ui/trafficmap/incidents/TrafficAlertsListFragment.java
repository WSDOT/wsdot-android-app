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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.incidents;

import android.content.Intent;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.alert.AlertsListFragment;

public class TrafficAlertsListFragment extends AlertsListFragment {

    protected ArrayList<HighwayAlertsItem> getAlerts(Cursor cursor){

        //Retrieve the bounds from the intent. Defaults to 0
        Intent intent = getActivity().getIntent();

        Double nelat = intent.getDoubleExtra("nelat", 0.0);
        Double nelong = intent.getDoubleExtra("nelong", 0.0);
        Double swlat = intent.getDoubleExtra("swlat", 0.0);
        Double swlong = intent.getDoubleExtra("swlong", 0.0);

        LatLng northEast = new LatLng(nelat, nelong);
        LatLng southWest = new LatLng(swlat, swlong);

        LatLngBounds mBounds = new LatLngBounds(southWest, northEast);

        HighwayAlertsItem i;
        ArrayList<HighwayAlertsItem> items = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                Double startLatitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LATITUDE));
                Double startLongitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LONGITUDE));

                LatLng alertStartLocation = new LatLng(startLatitude, startLongitude);

                // If alert is within bounds of shown on screen show it on list
                if (mBounds.contains(alertStartLocation) ||
                        cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY)).toLowerCase().equals("amber")) {

                    i = new HighwayAlertsItem();

                    i.setHeadlineDescription(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_HEADLINE)));
                    i.setEventCategory(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY)).toLowerCase());
                    i.setLastUpdatedTime(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED)));
                    i.setAlertId(Integer.toString(cursor.getInt(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_ID))));

                    items.add(i);
                }
                cursor.moveToNext();
            }
        }

        return items;
    }
}
