package gov.wa.wsdot.android.wsdot.util;

import org.json.JSONArray;
import org.json.JSONException;

public class Converters {

    // Converts json array to java array
    public static String[] fromJsonString(String value) {

        try {
            JSONArray json = new JSONArray(value);

            String[] array = new String[json.length()];

            for (int i = 0; i < json.length(); i++){
                array[i] = json.getString(i);
            }

            return array;

        } catch (JSONException e) {
            return new String[0];
        }
    }

    // Converts an array of strings into a json array
    public static String stringArrayToJsonString(String[] values) {

        StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (int i = 0; i < values.length; i++){

            sb.append(values[i]);

            if (i != values.length -1) {
                sb.append(",");
            }
        }

        sb.append("]");

        return sb.toString();
    }

}
