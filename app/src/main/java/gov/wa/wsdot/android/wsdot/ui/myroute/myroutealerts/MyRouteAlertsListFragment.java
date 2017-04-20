package gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.alert.AlertsListFragment;
import gov.wa.wsdot.android.wsdot.util.Utils;

/**
 * Implements abstract methods from AlertsListFragment for displaying a list of alerts on users route.
 */

public class MyRouteAlertsListFragment extends AlertsListFragment {

    final String TAG = "MyRouteAlertsListFrag";
    final Double MAX_ALERT_DISTANCE = 0.248548;

    protected ArrayList<HighwayAlertsItem> getAlerts(ArrayList<HighwayAlertsItem> alerts){

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

        ArrayList<HighwayAlertsItem> alertsOnRoute = new ArrayList<>();

        for (HighwayAlertsItem alert: alerts){
            try {
                for (int i = 0; i < routeJSON.length(); i++) {
                    JSONObject locationJSON = routeJSON.getJSONObject(i);
                    if (Utils.getDistanceFromPoints(alert.getStartLatitude(), alert.getStartLongitude(), locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE ||
                            Utils.getDistanceFromPoints(alert.getEndLatitude(), alert.getEndLongitude(), locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")) < MAX_ALERT_DISTANCE) {
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

}