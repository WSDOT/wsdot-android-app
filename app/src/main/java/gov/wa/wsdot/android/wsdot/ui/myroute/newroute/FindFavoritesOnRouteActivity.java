package gov.wa.wsdot.android.wsdot.ui.myroute.newroute;

import android.arch.lifecycle.ViewModelProvider;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertListViewModel;
import gov.wa.wsdot.android.wsdot.util.Utils;

/**
 * Created by simsl on 3/27/17.
 *
 * This class holds sync receivers for the various items that can be
 * added to a users favorites after they have created a custom route.
 *
 * This class holds two abstract methods getRoute() & taskComplete().
 * Each sync receiver calls taskComplete() when finished.
 *
 */

public abstract class FindFavoritesOnRouteActivity extends AppCompatActivity {

    private final String TAG = "FindFavoritesActivity";
    private static final Double MAX_ITEM_DISTANCE = 0.248548;

    protected final int MAX_NUM_TASKS = 4;

    @Inject
    ViewModelProvider.Factory viewModelFactory;


    private static final String[] cameras_projection = {
            WSDOTContract.Cameras._ID,
            WSDOTContract.Cameras.CAMERA_IS_STARRED,
            WSDOTContract.Cameras.CAMERA_LATITUDE,
            WSDOTContract.Cameras.CAMERA_LONGITUDE
    };

    private static final String[] travel_times_projection = {
            WSDOTContract.TravelTimes._ID,
            WSDOTContract.TravelTimes.TRAVEL_TIMES_IS_STARRED,
            WSDOTContract.TravelTimes.TRAVEL_TIMES_START_LATITUDE,
            WSDOTContract.TravelTimes.TRAVEL_TIMES_START_LONGITUDE,
            WSDOTContract.TravelTimes.TRAVEL_TIMES_END_LATITUDE,
            WSDOTContract.TravelTimes.TRAVEL_TIMES_END_LONGITUDE
    };

    private static final String[] ferry_schedule_projection = {
            WSDOTContract.FerriesSchedules._ID,
            WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_ID,
            WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED,
            WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_DATE
    };

    private static final String[] mountain_pass_projection = {
            WSDOTContract.MountainPasses._ID,
            WSDOTContract.MountainPasses.MOUNTAIN_PASS_NAME,
            WSDOTContract.MountainPasses.MOUNTAIN_PASS_IS_STARRED,
            WSDOTContract.MountainPasses.MOUNTAIN_PASS_ID,
            WSDOTContract.MountainPasses.MOUNTAIN_PASS_LATITUDE,
            WSDOTContract.MountainPasses.MOUNTAIN_PASS_LONGITUDE
    };

    protected abstract void taskComplete();
    protected abstract List<LatLng> getRoute();

    /**
     * Sync Receivers
     */
    public final class MountainPassesSyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {

                    Cursor data = getContentResolver().query(
                            WSDOTContract.MountainPasses.CONTENT_URI,
                            mountain_pass_projection,
                            WSDOTContract.MountainPasses.MOUNTAIN_PASS_IS_STARRED + "=?",
                            new String[] {Integer.toString(0)},
                            null
                    );

                    if (data != null && data.moveToFirst()) {
                        while (data.moveToNext()) {

                            for (LatLng location : getRoute()) {

                                if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                        data.getDouble(data.getColumnIndex(WSDOTContract.MountainPasses.MOUNTAIN_PASS_LATITUDE)),
                                        data.getDouble((data.getColumnIndex(WSDOTContract.MountainPasses.MOUNTAIN_PASS_LONGITUDE)))) <= MAX_ITEM_DISTANCE) {

                                    ContentValues values = new ContentValues();
                                    values.put(WSDOTContract.MountainPasses.MOUNTAIN_PASS_IS_STARRED, 1);
                                    getContentResolver().update(
                                            WSDOTContract.MountainPasses.CONTENT_URI,
                                            values,
                                            WSDOTContract.MountainPasses._ID + "=?",
                                            new String[]{data.getString(data.getColumnIndex(WSDOTContract.MountainPasses._ID))}
                                    );
                                }
                            }
                        }
                        data.close();
                        taskComplete();
                    }
                } else {
                    taskComplete();
                    Log.e("PassesSyncReceiver", responseString);
                }
            }
        }
    }

    public final class FerriesSchedulesSyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {

                    Cursor data = getContentResolver().query(
                            WSDOTContract.FerriesSchedules.CONTENT_URI,
                            ferry_schedule_projection,
                            WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED + "=?",
                            new String[] {Integer.toString(0)},
                            null
                    );

                    SparseArray<FerriesTerminalItem> terminalLocations = Utils.getTerminalLocations();

                    if (data != null && data.moveToFirst()) {
                        while (data.moveToNext()) {

                            ArrayList<FerriesTerminalItem> terminalItems = getTerminals(data.getString(data.getColumnIndex(WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_DATE)));
                            for (FerriesTerminalItem terminal: terminalItems){

                                Boolean nearStartTerminal = false;
                                Boolean nearEndTerminal = false;

                                for (LatLng location : getRoute()) {

                                    if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                            terminalLocations.get(terminal.getArrivingTerminalID()).getLatitude(),
                                            terminalLocations.get(terminal.getArrivingTerminalID()).getLongitude()) <= MAX_ITEM_DISTANCE){
                                        nearStartTerminal = true;
                                    }

                                    if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                            terminalLocations.get(terminal.getDepartingTerminalID()).getLatitude(),
                                            terminalLocations.get(terminal.getDepartingTerminalID()).getLongitude()) <= MAX_ITEM_DISTANCE) {
                                        nearEndTerminal = true;
                                    }

                                    if (nearStartTerminal && nearEndTerminal){
                                        ContentValues values = new ContentValues();
                                        values.put(WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 1);
                                        getContentResolver().update(
                                                WSDOTContract.FerriesSchedules.CONTENT_URI,
                                                values,
                                                WSDOTContract.FerriesSchedules._ID + "=?",
                                                new String[]{data.getString(data.getColumnIndex(WSDOTContract.FerriesSchedules._ID))}
                                        );
                                        break;
                                    }

                                }
                            }
                        }
                        data.close();
                        taskComplete();
                    }
                } else {
                    taskComplete();
                    Log.e("FerriesSyncReceiver", responseString);
                }
            }
        }
    }

    public final class TravelTimesSyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");
            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {

                    Cursor data = getContentResolver().query(
                            WSDOTContract.TravelTimes.CONTENT_URI,
                            travel_times_projection,
                            WSDOTContract.TravelTimes.TRAVEL_TIMES_IS_STARRED + "=?",
                            new String[] {Integer.toString(0)},
                            null
                    );
                    if (data != null && data.moveToFirst()) {
                        while (data.moveToNext()) {
                            for (LatLng location : getRoute()) {
                                if ((Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                                data.getDouble(data.getColumnIndex(WSDOTContract.TravelTimes.TRAVEL_TIMES_START_LATITUDE)),
                                                data.getDouble((data.getColumnIndex(WSDOTContract.TravelTimes.TRAVEL_TIMES_START_LONGITUDE)))) <= MAX_ITEM_DISTANCE)
                                        && (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                                data.getDouble(data.getColumnIndex(WSDOTContract.TravelTimes.TRAVEL_TIMES_END_LATITUDE)),
                                                data.getDouble(data.getColumnIndex(WSDOTContract.TravelTimes.TRAVEL_TIMES_END_LONGITUDE)))) <= MAX_ITEM_DISTANCE) {

                                    ContentValues values = new ContentValues();
                                    values.put(WSDOTContract.TravelTimes.TRAVEL_TIMES_IS_STARRED, 1);
                                    getContentResolver().update(
                                            WSDOTContract.TravelTimes.CONTENT_URI,
                                            values,
                                            WSDOTContract.TravelTimes._ID + "=?",
                                            new String[]{data.getString(data.getColumnIndex(WSDOTContract.TravelTimes._ID))}
                                    );
                                }
                            }
                        }
                        data.close();
                        taskComplete();
                    }
                } else {
                    taskComplete();
                    Log.e("TravelTimesSyncReceiver", responseString);
                }
            }
        }
    }

    public final class CamerasSyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");
            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {
                    Cursor data = getContentResolver().query(
                            WSDOTContract.Cameras.CONTENT_URI,
                            cameras_projection,
                            WSDOTContract.Cameras.CAMERA_IS_STARRED + "=?",
                            new String[] {Integer.toString(0)},
                            null
                    );

                    if (data != null && data.moveToFirst()) {
                        while (data.moveToNext()) {
                            for (LatLng location : getRoute()) {
                                if (Utils.getDistanceFromPoints(location.latitude, location.longitude,
                                                data.getDouble(data.getColumnIndex(WSDOTContract.Cameras.CAMERA_LATITUDE)),
                                                data.getDouble((data.getColumnIndex(WSDOTContract.Cameras.CAMERA_LONGITUDE)))) <= MAX_ITEM_DISTANCE) {
                                    ContentValues values = new ContentValues();
                                    values.put(WSDOTContract.Cameras.CAMERA_IS_STARRED, 1);
                                    getContentResolver().update(
                                            WSDOTContract.Cameras.CONTENT_URI,
                                            values,
                                            WSDOTContract.Cameras._ID + "=?",
                                            new String[]{data.getString(data.getColumnIndex(WSDOTContract.Cameras._ID))}
                                    );
                                }
                            }
                        }
                        data.close();
                    }
                    taskComplete();
                } else {
                    taskComplete();
                    Log.e("CamerasSyncReceiver", responseString);
                }
            }
        }
    }

    private ArrayList<FerriesTerminalItem> getTerminals(String datesString) {
        ArrayList<FerriesTerminalItem> terminalItems = new ArrayList<>();
        FerriesTerminalItem terminal;

        try {
            JSONArray dates = new JSONArray(datesString);
            int numDates = dates.length();
            for (int j = 0; j < numDates; j++) {
                JSONObject date = dates.getJSONObject(j);

                JSONArray sailings = date.getJSONArray("Sailings");
                int numSailings = sailings.length();
                for (int k=0; k < numSailings; k++) {
                    JSONObject sailing = sailings.getJSONObject(k);
                    terminal = new FerriesTerminalItem();
                    terminal.setArrivingTerminalID(sailing.getInt("ArrivingTerminalID"));
                    terminal.setArrivingTerminalName(sailing.getString("ArrivingTerminalName"));
                    terminal.setDepartingTerminalID(sailing.getInt("DepartingTerminalID"));
                    terminal.setDepartingTerminalName(sailing.getString("DepartingTerminalName"));

                    terminalItems.add(terminal);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding schedule date items", e);
        }
        return terminalItems;
    }
}