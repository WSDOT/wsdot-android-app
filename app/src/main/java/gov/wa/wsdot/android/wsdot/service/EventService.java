package gov.wa.wsdot.android.wsdot.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;

public class EventService extends IntentService {

    private static final String DEBUG_TAG = "EventService";

    public EventService() {
        super("EventService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            URL url = new URL(APIEndPoints.EVENT);

            URLConnection urlConn = url.openConnection();

            BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
            InputStreamReader is = new InputStreamReader(bis);
            BufferedReader in = new BufferedReader(is);

            String jsonFile = "";
            String line;
            while ((line = in.readLine()) != null)
                jsonFile += line;
            in.close();

            JSONObject obj = new JSONObject(jsonFile);

            Log.e(DEBUG_TAG, obj.toString());

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString(getString(R.string.event_start_date), obj.getString("startDate"));
            editor.putString(getString(R.string.event_end_date), obj.getString("endDate"));
            editor.putString(getString(R.string.event_banner_text_key), obj.getString("bannerText"));
            editor.putString(getString(R.string.event_title_key), obj.getString("title"));
            editor.putString(getString(R.string.event_details_key), obj.getString("details"));

            editor.putInt(getString(R.string.event_theme_key), 1);

            editor.commit();


        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error: " + e.getMessage());
        }
    }
}