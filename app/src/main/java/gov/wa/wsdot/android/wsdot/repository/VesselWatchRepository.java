package gov.wa.wsdot.android.wsdot.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

@Singleton
public class VesselWatchRepository extends NetworkResourceRepository {

    private static String TAG = VesselWatchRepository.class.getSimpleName();

    private MutableLiveData<List<VesselWatchItem>> vessels;

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Integer> ferryIcons = new HashMap<>();

    @Inject
    public VesselWatchRepository(AppExecutors appExecutors) {
        // Supply the super class with data needed for super.refreshData()
        super(appExecutors);

        ferryIcons.put(0, R.drawable.ferry_0);
        ferryIcons.put(30, R.drawable.ferry_30);
        ferryIcons.put(60, R.drawable.ferry_60);
        ferryIcons.put(90, R.drawable.ferry_90);
        ferryIcons.put(120, R.drawable.ferry_120);
        ferryIcons.put(150, R.drawable.ferry_150);
        ferryIcons.put(180, R.drawable.ferry_180);
        ferryIcons.put(210, R.drawable.ferry_210);
        ferryIcons.put(240, R.drawable.ferry_240);
        ferryIcons.put(270, R.drawable.ferry_270);
        ferryIcons.put(300, R.drawable.ferry_300);
        ferryIcons.put(330, R.drawable.ferry_330);
        ferryIcons.put(360, R.drawable.ferry_360);

        vessels = new MutableLiveData<>();
    }

    public MutableLiveData<List<VesselWatchItem>> getVessels() {
        return this.vessels;
    }

    void fetchData(MutableLiveData<ResourceStatus> status) throws Exception {

        List<VesselWatchItem> vesselWatchItems = new ArrayList<VesselWatchItem>();

        URL url = new URL(APIEndPoints.VESSEL_LOCATIONS + "?apiaccesscode=" + APIEndPoints.WSDOT_API_KEY);

        URLConnection urlConn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String jsonFile = "";
        String line;
        while ((line = in.readLine()) != null)
            jsonFile += line;
        in.close();

        JSONArray items = new JSONArray(jsonFile);
        int ferryIcon;

        for (int j=0; j < items.length(); j++) {

            JSONObject item = items.getJSONObject(j);

            if (item.getString("InService").equalsIgnoreCase("false")) {
                continue;
            }

            int nearest = (item.getInt("Heading") + 30 / 2) / 30 * 30; // round heading to nearest 30 degrees
            ferryIcon = ferryIcons.get(nearest);

            String route = "";

            if (item.getJSONArray("OpRouteAbbrev").length() != 0) {
                route = item.getJSONArray("OpRouteAbbrev").getString(0).toUpperCase(Locale.ENGLISH);
            }

            String lastDock = item.getString("DepartingTerminalName");
            String arrivingTerminal = item.getString("ArrivingTerminalName");
            String leftDock = formatTime(item, "LeftDock");
            String scheduledDeparture = formatTime(item, "ScheduledDeparture");
            String eta = formatTime(item, "Eta");

            if (route.length() == 0) route = "Not available";
            if (lastDock.length() == 0) lastDock = "Not available";
            if (arrivingTerminal.length() == 0) arrivingTerminal = "Not available";

            VesselWatchItem vessel = new VesselWatchItem(
                    item.getDouble("Latitude"),
                    item.getDouble("Longitude"),
                    item.getString("VesselName"),
                    "<b>Route:</b> " + route
                            + "<br><b>Departing:</b> " + lastDock
                            + "<br><b>Arriving:</b> " + arrivingTerminal
                            + "<br><b>Scheduled Departure:</b> " + scheduledDeparture
                            + "<br><b>Actual Departure:</b> " + formatTimeForDisplay(item, "LeftDock")
                            + "<br><b>Estimated Arrival:</b> " + formatTimeForDisplay(item, "Eta")
                            + "<br><b>Heading:</b> "	+ Integer.toString(item.getInt("Heading")) + "\u00b0 "
                            + headingToHeadtxt(item.getInt("Heading"))
                            + "<br><b>Speed:</b> " + Double.toString(item.getDouble("Speed")) + " knots"
                            + "<br><br><a href=\"http://www.wsdot.com/ferries/vesselwatch/VesselDetail.aspx?vessel_id="
                            + item.getInt("VesselID") + "\">" + item.getString("VesselName") + " Web page</a>",
                    ferryIcon);

            vessel.setArrivingTerminal(arrivingTerminal);
            vessel.setLastDock(lastDock);
            vessel.setScheduledDeparture(scheduledDeparture);

            if (!item.getString("ArrivingTerminalID").equals("null")) {
                vessel.setArrivingTerminalId(item.getInt("ArrivingTerminalID"));
            } else {
                vessel.setArrivingTerminalId(-1);
            }

            if (!item.getString("DepartingTerminalID").equals("null")) {
                vessel.setDepartedTerminalId(item.getInt("DepartingTerminalID"));
            } else {
                vessel.setDepartedTerminalId(-1);
            }

            vessel.setEta(eta);
            vessel.setLeftDock(leftDock);

            vesselWatchItems.add(vessel);


        }
        vessels.postValue(vesselWatchItems);
    }

    /**
     * Giving vessel heading returns a string for the
     * cardinal direction.
     *
     * @param heading
     * @return direction string
     */
    private static String headingToHeadtxt(int heading){
        String directions[] = {"N", "NxE", "E", "SxE", "S", "SxW", "W", "NxW", "N"};
        return directions[ (int)Math.round((  ((double)heading % 360) / 45)) ];
    }

    /**
     * Formats the time field in JSON object
     *
     * @param item JSONObject for ferry data
     * @param time field name for time in item
     * @return Formatted time string.
     * @throws JSONException
     * @throws NumberFormatException
     */
    private static String formatTimeForDisplay(JSONObject item, String time) throws NumberFormatException, JSONException{
        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        if (item.isNull(time)) {
            return "--:--";
        } else {
            return dateFormat.format(new Date(Long.parseLong(item.getString(time).substring(6, 19))));
        }
    }

    /**
     * Formats the time field in JSON object
     *
     * @param item JSONObject for ferry data
     * @param time field name for time in item
     * @return Formatted time string.
     * @throws JSONException
     * @throws NumberFormatException
     */
    private static String formatTime(JSONObject item, String time) throws NumberFormatException, JSONException{
        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        if (item.isNull(time)) {
            return null;
        } else {
            return dateFormat.format(new Date(Long.parseLong(item.getString(time).substring(6, 19))));
        }
    }

}
