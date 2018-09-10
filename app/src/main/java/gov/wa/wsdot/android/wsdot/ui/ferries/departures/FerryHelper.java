package gov.wa.wsdot.android.wsdot.ui.ferries.departures;

import java.util.HashMap;
import java.util.Map;

import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;

public class FerryHelper {


    /**
     * Haversine formula
     *
     * Provides great-circle distances between two points on a sphere from
     * their longitudes and latitudes.
     *
     * http://en.wikipedia.org/wiki/Haversine_formula
     *
     * @param latitude
     * @param longitude
     */
    public static int getDistanceFromTerminal(int terminalId, double latitude, double longitude) {

        FerriesTerminalItem terminal = getTerminalLocationsMap().get(terminalId);
        double earthRadius = 20902200; // feet
        double dLat = Math.toRadians(terminal.getLatitude() - latitude);
        double dLng = Math.toRadians(terminal.getLongitude() - longitude);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(latitude))
                * Math.cos(Math.toRadians(terminal.getLatitude()));

        double c = 2 * Math.asin(Math.sqrt(a));
        int distance = (int) Math.round(earthRadius * c);

        return distance;
    }

    public static Map<Integer, FerriesTerminalItem> getTerminalLocationsMap() {
        Map<Integer, FerriesTerminalItem> ferriesTerminalMap = new HashMap<>();
        ferriesTerminalMap.put(1, new FerriesTerminalItem(1, "Anacortes", 48.507351, -122.677));
        ferriesTerminalMap.put(3, new FerriesTerminalItem(3, "Bainbridge Island", 47.622339, -122.509617));
        ferriesTerminalMap.put(4, new FerriesTerminalItem(4, "Bremerton", 47.561847, -122.624089));
        ferriesTerminalMap.put(5, new FerriesTerminalItem(5, "Clinton", 47.9754, -122.349581));
        ferriesTerminalMap.put(11, new FerriesTerminalItem(11, "Coupeville", 48.159008, -122.672603));
        ferriesTerminalMap.put(8, new FerriesTerminalItem(8, "Edmonds", 47.813378, -122.385378));
        ferriesTerminalMap.put(9, new FerriesTerminalItem(9, "Fauntleroy", 47.5232, -122.3967));
        ferriesTerminalMap.put(10, new FerriesTerminalItem(10, "Friday Harbor", 48.535783, -123.013844));
        ferriesTerminalMap.put(12, new FerriesTerminalItem(12, "Kingston", 47.794606, -122.494328));
        ferriesTerminalMap.put(13, new FerriesTerminalItem(13, "Lopez Island", 48.570928, -122.882764));
        ferriesTerminalMap.put(14, new FerriesTerminalItem(14, "Mukilteo", 47.949544, -122.304997));
        ferriesTerminalMap.put(15, new FerriesTerminalItem(15, "Orcas Island", 48.597333, -122.943494));
        ferriesTerminalMap.put(16, new FerriesTerminalItem(16, "Point Defiance", 47.306519, -122.514053));
        ferriesTerminalMap.put(17, new FerriesTerminalItem(17, "Port Townsend", 48.110847, -122.759039));
        ferriesTerminalMap.put(7, new FerriesTerminalItem(7, "Seattle", 47.602501, -122.340472));
        ferriesTerminalMap.put(18, new FerriesTerminalItem(18, "Shaw Island", 48.584792, -122.92965));
        ferriesTerminalMap.put(19, new FerriesTerminalItem(19, "Sidney B.C.", 48.643114, -123.396739));
        ferriesTerminalMap.put(20, new FerriesTerminalItem(20, "Southworth", 47.513064, -122.495742));
        ferriesTerminalMap.put(21, new FerriesTerminalItem(21, "Tahlequah", 47.331961, -122.507786));
        ferriesTerminalMap.put(22, new FerriesTerminalItem(22, "Vashon Island", 47.51095, -122.463639));
        return ferriesTerminalMap;
    }
}
