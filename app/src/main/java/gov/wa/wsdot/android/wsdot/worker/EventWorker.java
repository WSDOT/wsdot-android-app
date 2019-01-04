package gov.wa.wsdot.android.wsdot.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;

public class EventWorker extends Worker {

    private final String TAG = EventWorker.class.getSimpleName();

    public EventWorker(
            @NonNull Context context,
            @NonNull WorkerParameters parmas) {
        super(context, parmas);
    }

    @NonNull
    @Override
    public Result doWork() {

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

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString(getApplicationContext().getString(R.string.event_start_date), obj.getString("startDate"));
            editor.putString(getApplicationContext().getString(R.string.event_end_date), obj.getString("endDate"));
            editor.putString(getApplicationContext().getString(R.string.event_banner_text_key), obj.getString("bannerText"));
            editor.putString(getApplicationContext().getString(R.string.event_title_key), obj.getString("title"));
            editor.putString(getApplicationContext().getString(R.string.event_details_key), obj.getString("details"));
            editor.putInt(getApplicationContext().getString(R.string.event_theme_key), obj.getInt("themeId"));

            editor.apply();

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return Result.failure();
        }

        return Result.success();
    }
}
