package gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts;

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.alert.AlertsListFragment;

/**
 * Created by simsl on 3/10/17.
 */

public class MyRouteAlertsListFragment extends AlertsListFragment {

    final String TAG = "MyRouteAlertsListFrag";
    final Double MAX_ALERT_DISTANCE = 0.248548;

    protected ArrayList<HighwayAlertsItem> getAlerts(Cursor cursor){

        //Retrieve the bounds from the intent. Defaults to 0
        Intent intent = getActivity().getIntent();

        String routeString = intent.getExtras().getString("route");

        JSONArray routeJSON = new JSONArray();

        try {
            routeJSON = new JSONArray(routeString);
        } catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "Error reading route coordinates.", Toast.LENGTH_SHORT).show();
        }

        Log.e(TAG, "ROUTE: " + routeString.toString());

        ArrayList<HighwayAlertsItem> items = new ArrayList<>();
        items.clear();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                Double startLatitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LATITUDE));
                Double startLongitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_START_LONGITUDE));

                Double endLatitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_END_LATITUDE));
                Double endLongitude = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_END_LONGITUDE));

                LatLng alertStartLocation = new LatLng(startLatitude, startLongitude);
                LatLng alertEndLocation = new LatLng(endLatitude, endLongitude);

                try {
                    for (int i = 0; i < routeJSON.length(); i++) {
                        JSONObject locationJSON = routeJSON.getJSONObject(i);
                        if (getDistanceFromPoints(alertStartLocation.latitude, alertStartLocation.longitude, locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE){
                            HighwayAlertsItem item = new HighwayAlertsItem();
                            item.setHeadlineDescription(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_HEADLINE)));
                            item.setEventCategory(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY)).toLowerCase());
                            item.setLastUpdatedTime(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED)));
                            item.setAlertId(Integer.toString(cursor.getInt(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_ID))));

                            if (!items.contains(item)) {
                                items.add(item);
                            }
                        } else if (getDistanceFromPoints(alertEndLocation.latitude, alertEndLocation.longitude, locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE) {
                            HighwayAlertsItem item = new HighwayAlertsItem();
                            item.setHeadlineDescription(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_HEADLINE)));
                            item.setEventCategory(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_CATEGORY)).toLowerCase());
                            item.setLastUpdatedTime(cursor.getString(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED)));
                            item.setAlertId(Integer.toString(cursor.getInt(cursor.getColumnIndex(WSDOTContract.HighwayAlerts.HIGHWAY_ALERT_ID))));
                            if (!items.contains(item)) {
                                items.add(item);
                            }
                        }
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error reading route coordinate.", Toast.LENGTH_SHORT).show();
                }
                cursor.moveToNext();
            }
        }
        return items;
    }

    /**
     * Haversine formula
     *
     * Provides great-circle distances between two points on a sphere from their longitudes and latitudes
     *
     * http://en.wikipedia.org/wiki/Haversine_formula
     *
     */
    protected int getDistanceFromPoints(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        double earthRadius = 3958.75; // miles
        double dLat = Math.toRadians(latitudeA - latitudeB);
        double dLng = Math.toRadians(longitudeA - longitudeB);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(latitudeB))
                * Math.cos(Math.toRadians(latitudeA));

        double c = 2 * Math.asin(Math.sqrt(a));
        return (int) Math.round(earthRadius * c);

    }
}