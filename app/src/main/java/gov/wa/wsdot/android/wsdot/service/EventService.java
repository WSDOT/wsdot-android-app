package gov.wa.wsdot.android.wsdot.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;

/**
 *  A one off service that checks the event status URL for information about current/upcoming
 *  events. Data is saved to Shared Preferences.
 *
 *  *IMPORTANT* The data pulled from this service will not effect the apps display until
 *  the app restarts. WSDOTApplication checks shared preferences and sets the event active
 *  flag to true if there's event data and the date is within the start and end date.
 *  It was done this way to avoid tying up app start up with a network call.
 *
 *  Server must respond with a json object of the format:
 *  {
 *      "startDate": "yyyy-MM-dd",
 *      "endDate" : "yyyy-MM-dd",
 *      "themeId": number, - Id of the theme to use when event is active.
 *      "bannerText": string - text for the banner
 *      "title": string - title for Toolbar in EventActivity
 *      "details": string - information about event to display in EventActivity
 *  }
 *
 */
public class EventService extends IntentService {

    private static final String DEBUG_TAG = "EventService";

    public EventService() {
        super("EventService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Check if we have an active event for the event banner
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

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString(getString(R.string.event_start_date), obj.getString("startDate"));
            editor.putString(getString(R.string.event_end_date), obj.getString("endDate"));
            editor.putString(getString(R.string.event_banner_text_key), obj.getString("bannerText"));
            editor.putString(getString(R.string.event_title_key), obj.getString("title"));
            editor.putString(getString(R.string.event_details_key), obj.getString("details"));
            editor.putInt(getString(R.string.event_theme_key), obj.getInt("themeId"));

            editor.commit();

        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error: " + e.getMessage());
        }

        // Check if there is a new list of notification topics.
        try {
            URL url = new URL(APIEndPoints.FIREBASE_TOPICS_VERSION);

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

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putInt(getString(R.string.new_firebase_notification_topics_version), obj.getInt("version"));

            editor.commit();

        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error: " + e.getMessage());
        }

    }
}