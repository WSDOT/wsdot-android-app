package gov.wa.wsdot.android.wsdot.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitDataSource;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.borderwaits.LocalBorderWaitDataSource;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;

public class DownloadBorderWaitsAsyncTask extends AsyncTask<String, Void, String> {

    private static final String DEBUG_TAG = "BorderWaitSyncService";

    BorderWaitsTaskReceiverFragment mFragment;

    public DownloadBorderWaitsAsyncTask(Fragment fragment){
        mFragment = (BorderWaitsTaskReceiverFragment)fragment;
    }

    @Override
    protected String doInBackground(String... params) {

        long now = System.currentTimeMillis();
        boolean shouldUpdate = true;
        String responseString = "";

        boolean forceUpdate = false;


        if (shouldUpdate || forceUpdate) {

            try {
                URL url = new URL(APIEndPoints.BORDER_WAITS);
                URLConnection urlConn = url.openConnection();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String jsonFile = "";
                String line;

                while ((line = in.readLine()) != null)
                    jsonFile += line;
                in.close();

                JSONObject obj = new JSONObject(jsonFile);
                JSONObject result = obj.getJSONObject("waittimes");
                JSONArray items = result.getJSONArray("items");

                List<BorderWaitEntity> waits = new ArrayList<>();

                int numItems = items.length();

                for (int j=0; j < numItems; j++) {
                    JSONObject item = items.getJSONObject(j);

                    BorderWaitEntity wait = new BorderWaitEntity();

                    wait.setId(item.getInt("id"));
                    wait.setTitle(item.getString("name"));
                    wait.setDirection(item.getString("direction"));
                    wait.setLane(item.getString("lane"));
                    wait.setRoute(item.getInt("route"));
                    wait.setWait(item.getInt("wait"));
                    wait.setUpdated(item.getString("updated"));
                    wait.setIsStarred(0);

                    waits.add(wait);
                }

                BorderWaitEntity[] waitsArray = new BorderWaitEntity[waits.size()];
                waitsArray = waits.toArray(waitsArray);

                BorderWaitDataSource borderWaitDataSource = LocalBorderWaitDataSource.getInstance();
                borderWaitDataSource.insertOrUpdateBorderWaits(waitsArray);

                responseString = "OK";

            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Error: " + e.getMessage());
                responseString = e.getMessage();
            }
        } else {
            responseString = "NOP";
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.e("onPostExecute TEST", result);
        mFragment.receiveBorderWaitsDownloadResponse(result);
    }

}