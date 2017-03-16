package gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.alert.AlertsListFragment;

/**
 * Created by simsl on 3/10/17.
 */

public class MyRouteAlertsListFragment extends AlertsListFragment {

    final String TAG = "MyRouteAlertsListFrag";
    final Double MAX_ALERT_DISTANCE = 0.248548;

    protected ArrayList<HighwayAlertsItem> getAlerts(ArrayList<HighwayAlertsItem> alerts, AsyncTask<Cursor, Void, ArrayList<HighwayAlertsItem>> task){

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
        //Log.e(TAG, "ROUTE: " + routeString.toString());
        ArrayList<HighwayAlertsItem> alertsOnRoute = new ArrayList<>();

        for (HighwayAlertsItem alert: alerts){
            if (task.isCancelled()) {
                return new ArrayList<>();
            }
            try {
                for (int i = 0; i < routeJSON.length(); i++) {
                    JSONObject locationJSON = routeJSON.getJSONObject(i);
                    if (getDistanceFromPoints(alert.getStartLatitude(), alert.getStartLongitude(), locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE ||
                            getDistanceFromPoints(alert.getEndLatitude(), alert.getEndLongitude(), locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE) {
                        alertsOnRoute.add(alert);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error reading route coordinate.", Toast.LENGTH_SHORT).show();
            }
        }
        return alertsOnRoute;
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