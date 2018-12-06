package gov.wa.wsdot.android.wsdot.util;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;

public class Utils {

    final static String TAG = Utils.class.getSimpleName();

    /**
     * Haversine formula
     *
     * Provides great-circle distances between two points on a sphere from their longitudes and latitudes
     *
     * http://en.wikipedia.org/wiki/Haversine_formula
     *
     */
    public static int getDistanceFromPoints(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
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

    public static Location getCenterLocation(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {

        double dLon = Math.toRadians(longitudeB - longitudeA);

        latitudeA = Math.toRadians(latitudeA);
        latitudeB = Math.toRadians(latitudeB);
        longitudeA = Math.toRadians(longitudeA);

        double Bx = Math.cos(latitudeB) * Math.cos(dLon);
        double By = Math.cos(latitudeB) * Math.sin(dLon);
        double latitudeC = Math.atan2(Math.sin(latitudeA) + Math.sin(latitudeB), Math.sqrt((Math.cos(latitudeA) + Bx) * (Math.cos(latitudeA) + Bx) + By * By));
        double longitudeC = longitudeA + Math.atan2(By, Math.cos(latitudeA) + Bx);

        Location location = new Location("");
        location.setLatitude(Math.toDegrees(latitudeC));
        location.setLongitude(Math.toDegrees(longitudeC));

        return location;

    }

    public static SparseArray<FerriesTerminalItem> getTerminalLocations() {

        SparseArray<FerriesTerminalItem> ferriesTerminalMap = new SparseArray<>();

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

    public static Boolean currentDateInRange(String startDateString, String endDateString, String dateFormat){
        DateFormat df = new SimpleDateFormat(dateFormat, Locale.US);

        Date startDate;
        Date endDate;

        try {
            startDate = df.parse(startDateString);
            endDate = df.parse(endDateString);
        } catch (ParseException e) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(1997, 1, 1, 0, 0, 0);
            startDate = cal.getTime();
            endDate = cal.getTime();
            e.printStackTrace();
        }

        Date today = new Date();

        return !(today.before(startDate) || today.after(endDate));

    }

    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     *
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] b = new byte[bufferSize];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    public static void saveOrderedList(Collection collection, String key, SharedPreferences sharedPreferences){
        JSONArray jsonArray = new JSONArray(collection);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, jsonArray.toString());
        editor.apply();
    }

    public static ArrayList<Integer> loadOrderedIntList(String key, SharedPreferences sharedPreferences){
        ArrayList<Integer> arrayList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(key, "[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                arrayList.add(jsonArray.getInt(i));
            }
            return arrayList;
        } catch (JSONException e){
            return arrayList;
        }
    }
}
