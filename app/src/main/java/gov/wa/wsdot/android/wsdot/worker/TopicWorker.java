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

public class TopicWorker extends Worker {

    private final String TAG = TopicWorker.class.getSimpleName();

    public TopicWorker(
            @NonNull Context context,
            @NonNull WorkerParameters parmas) {
        super(context, parmas);
    }

    @NonNull
    @Override
    public Result doWork() {

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

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putInt(getApplicationContext().getString(R.string.new_firebase_notification_topics_version), obj.getInt("version"));
            editor.putString(getApplicationContext().getString(R.string.firebase_notification_title), obj.getString("title"));
            editor.putString(getApplicationContext().getString(R.string.firebase_notification_description), obj.getString("description"));

            editor.apply();

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return Result.failure();
        }

        return Result.success();
    }
}
