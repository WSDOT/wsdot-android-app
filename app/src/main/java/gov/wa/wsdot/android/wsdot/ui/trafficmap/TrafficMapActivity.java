/*
 * Copyright (c) 2016 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.trafficmap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.service.CamerasSyncService;
import gov.wa.wsdot.android.wsdot.service.HighwayAlertsSyncService;
import gov.wa.wsdot.android.wsdot.shared.CalloutItem;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.shared.LatLonItem;
import gov.wa.wsdot.android.wsdot.shared.RestAreaItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.alert.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.ui.callout.CalloutActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.expresslanes.SeattleExpressLanesActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.incidents.TrafficAlertsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.restareas.RestAreaActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.traveltimes.TravelTimesActivity;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.map.CalloutsOverlay;
import gov.wa.wsdot.android.wsdot.util.map.CamerasOverlay;
import gov.wa.wsdot.android.wsdot.util.map.HighwayAlertsOverlay;
import gov.wa.wsdot.android.wsdot.util.map.RestAreasOverlay;

public class TrafficMapActivity extends BaseActivity implements
        OnMarkerClickListener, OnMyLocationButtonClickListener,
        OnCameraChangeListener, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener,
        OnRequestPermissionsResultCallback, OnMapReadyCallback {

    private static final String TAG = TrafficMapActivity.class.getSimpleName();

    private GoogleMap mMap;
    private HighwayAlertsOverlay alertsOverlay = null;
    private CamerasOverlay camerasOverlay = null;
    private RestAreasOverlay restAreasOverlay = null;
    private CalloutsOverlay calloutsOverlay = null;
    private List<CameraItem> cameras = new ArrayList<CameraItem>();
    private List<HighwayAlertsItem> alerts = new ArrayList<HighwayAlertsItem>();
    private List<RestAreaItem> restAreas = new ArrayList<>();
    private List<CalloutItem> callouts = new ArrayList<CalloutItem>();
    private HashMap<Marker, String> markers = new HashMap<Marker, String>();
    boolean showCameras;
    boolean showRestAreas;
    private ArrayList<LatLonItem> seattleArea = new ArrayList<LatLonItem>();

    static final private int MENU_ITEM_EXPRESS_LANES = Menu.FIRST;

    private CamerasSyncReceiver mCamerasReceiver;
    private HighwayAlertsSyncReceiver mHighwayAlertsSyncReceiver;
    private Intent camerasIntent;
    private Intent alertsIntent;
    private static AsyncTask<Void, Void, Void> mCamerasOverlayTask = null;
    private static AsyncTask<Void, Void, Void> mHighwayAlertsOverlayTask = null;
    private static AsyncTask<Void, Void, Void> mRestAreasOverlayTask = null;
    private static AsyncTask<Void, Void, Void> mCalloutsOverlayTask = null;
    private LatLngBounds bounds;
    private double latitude;
    private double longitude;
    private int zoom;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_ACCESS_FINE_LOCATION = 100;
    private Toolbar mToolbar;
    private boolean mPermissionDenied = false;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.map);

        enableAds();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup bounding box for Seattle area.
        seattleArea.add(new LatLonItem(48.01749, -122.46185));
        seattleArea.add(new LatLonItem(48.01565, -121.86584));
        seattleArea.add(new LatLonItem(47.27737, -121.86310));
        seattleArea.add(new LatLonItem(47.28109, -122.45911));

        // Initialize AsyncTasks
        mCamerasOverlayTask = new CamerasOverlayTask();
        mHighwayAlertsOverlayTask = new HighwayAlertsOverlayTask();
        mRestAreasOverlayTask = new RestAreasOverlayTask();
        mCalloutsOverlayTask = new CalloutsOverlayTask();

        // Check preferences and set defaults if none set
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true);
        showRestAreas = settings.getBoolean("KEY_SHOW_REST_AREAS", false);
        latitude = Double.parseDouble(settings.getString("KEY_TRAFFICMAP_LAT", "47.5990"));
        longitude = Double.parseDouble(settings.getString("KEY_TRAFFICMAP_LON", "-122.3350"));
        zoom = settings.getInt("KEY_TRAFFICMAP_ZOOM", 12);

        // Check if we came from favorites
        Bundle b = getIntent().getExtras();
        if (getIntent().hasExtra("lat"))
            latitude = b.getFloat("lat");
        if (getIntent().hasExtra("long"))
            longitude = b.getFloat("long");
        if (getIntent().hasExtra("zoom"))
            zoom = b.getInt("zoom");

        // Set up Service Intents.
        camerasIntent = new Intent(this, CamerasSyncService.class);
        alertsIntent = new Intent(this, HighwayAlertsSyncService.class);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        enableMyLocation();

        if (mCalloutsOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
            mCalloutsOverlayTask = new CalloutsOverlayTask().execute();
        } else if (mCalloutsOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
            mCalloutsOverlayTask.execute();
        }

        if (mRestAreasOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
            mRestAreasOverlayTask = new RestAreasOverlayTask().execute();
        } else if (mRestAreasOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
            mRestAreasOverlayTask.execute();
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();

        IntentFilter camerasFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasReceiver, camerasFilter);

        IntentFilter alertsFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.HIGHWAY_ALERTS_RESPONSE");
        alertsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mHighwayAlertsSyncReceiver = new HighwayAlertsSyncReceiver();
        registerReceiver(mHighwayAlertsSyncReceiver, alertsFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        this.unregisterReceiver(mCamerasReceiver);
        this.unregisterReceiver(mHighwayAlertsSyncReceiver);

        // Save last map location and zoom level.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        try {
            editor.putString("KEY_TRAFFICMAP_LAT", String.valueOf(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude));
            editor.putString("KEY_TRAFFICMAP_LON", String.valueOf(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude));
            editor.putInt("KEY_TRAFFICMAP_ZOOM", (int) mMap.getCameraPosition().zoom);
        } catch (NullPointerException e) {
            Log.e(TAG, "Error getting map bounds. Setting defaults to Seattle instead.");
            editor.putString("KEY_TRAFFICMAP_LAT", "47.5990");
            editor.putString("KEY_TRAFFICMAP_LON", "-122.3350");
            editor.putInt("KEY_TRAFFICMAP_ZOOM", 12);
        }
        editor.commit();
    }

    public void onCameraChange(CameraPosition cameraPosition) {
        setSupportProgressBarIndeterminateVisibility(true);
        startService(camerasIntent);
        startService(alertsIntent);
    }

    public boolean onMarkerClick(Marker marker) {
        Bundle b = new Bundle();
        Intent intent = new Intent();

        if (markers.get(marker).equalsIgnoreCase("camera")) {

            // GA tracker
            mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
            mTracker.setScreenName("/Traffic Map/Cameras");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());

            intent = new Intent(this, CameraActivity.class);
            b.putInt("id", Integer.parseInt(marker.getSnippet()));
            intent.putExtras(b);
            TrafficMapActivity.this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("alert")) {
            intent = new Intent(this, HighwayAlertDetailsActivity.class);
            b.putString("id", marker.getSnippet());
            intent.putExtras(b);
            TrafficMapActivity.this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("restarea")) {

            intent = new Intent(this, RestAreaActivity.class);
            intent.putExtra("restarea_json", marker.getSnippet());
            TrafficMapActivity.this.startActivity(intent);


        } else if (markers.get(marker).equalsIgnoreCase("callout")) {
            intent = new Intent(this, CalloutActivity.class);
            b.putString("url", marker.getSnippet());
            intent.putExtras(b);
            TrafficMapActivity.this.startActivity(intent);
        }
        return true;
    }

    public class CamerasSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {
                    // We've got cameras, now add them.
                    if (mCamerasOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
                        mCamerasOverlayTask = new CamerasOverlayTask().execute();
                    } else if (mCamerasOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
                        mCamerasOverlayTask.execute();
                    }
                } else {
                    Log.e("CameraDownloadReceiver", responseString);
                }
            }
        }
    }

    public class HighwayAlertsSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {
                    // We've got alerts, now add them.
                    if (mHighwayAlertsOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
                        mHighwayAlertsOverlayTask = new HighwayAlertsOverlayTask().execute();
                    } else if (mHighwayAlertsOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
                        mHighwayAlertsOverlayTask.execute();
                    }
                } else {
                    Log.e("HighwayAlertsSyncRecvr", responseString);
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.traffic, menu);

        if (showCameras) {
            menu.getItem(0).setTitle("Hide Cameras");
            menu.getItem(0).setIcon(R.drawable.ic_menu_traffic_cam);
        } else {
            menu.getItem(0).setTitle("Show Cameras");
            menu.getItem(0).setIcon(R.drawable.ic_menu_traffic_cam_off);

        }

        if (showRestAreas) {
            menu.getItem(4).setTitle("Hide Rest Areas");
        } else {
            menu.getItem(4).setTitle("Show Rest Areas");
        }

        /**
         * Check if current location is within a lat/lon bounding box surrounding
         * the greater Seattle area.
         */
        try {
            LatLng center = mMap.getCameraPosition().target;

            if (inPolygon(seattleArea, center.latitude, center.longitude)) {


                MenuItem menuItem_Lanes = menu.add(0, MENU_ITEM_EXPRESS_LANES, menu.size(), "Express Lanes");
                MenuItemCompat.setShowAsAction(menuItem_Lanes, MenuItemCompat.SHOW_AS_ACTION_NEVER);
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "Error getting LatLng center");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        switch (item.getItemId()) {

            case R.id.set_favorite:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);

                builder.setView(textEntryView);
                builder.setMessage(R.string.add_location_dialog);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }

                });
                builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText input = (EditText) textEntryView.findViewById(R.id.name);
                        String value = input.getText().toString();
                        dialog.dismiss();
                        ContentValues values = new ContentValues();

                        values.put(WSDOTContract.MapLocation.LOCATION_TITLE, value);
                        values.put(WSDOTContract.MapLocation.LOCATION_LAT, String.valueOf(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude));
                        values.put(WSDOTContract.MapLocation.LOCATION_LONG, String.valueOf(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude));
                        values.put(WSDOTContract.MapLocation.LOCATION_ZOOM, (int) mMap.getCameraPosition().zoom);

                        getContentResolver().insert(WSDOTContract.MapLocation.CONTENT_URI, values);
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.alerts_in_area:
                LatLngBounds mBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

                Intent alertsIntent = new Intent(this, TrafficAlertsActivity.class);

                alertsIntent.putExtra("nelat", mBounds.northeast.latitude);
                alertsIntent.putExtra("nelong", mBounds.northeast.longitude);
                alertsIntent.putExtra("swlat", mBounds.southwest.latitude);
                alertsIntent.putExtra("swlong", mBounds.southwest.longitude);

                startActivity(alertsIntent);
                return true;
            case MENU_ITEM_EXPRESS_LANES:
                Intent expressIntent = new Intent(this, SeattleExpressLanesActivity.class);
                startActivity(expressIntent);
                return true;

            case android.R.id.home:
                finish();
                return true;
            case R.id.toggle_cameras:
                toggleCameras(item);
                return true;
            case R.id.toggle_rest_areas:
                toggleRestAreas(item);
                return true;
            case R.id.travel_times:
                Intent timesIntent = new Intent(this, TravelTimesActivity.class);
                startActivity(timesIntent);
                return true;
            case R.id.goto_bellingham:
                mTracker.setScreenName("/Traffic Map/Go To Location/Bellingham");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Bellingham Traffic", 48.756302, -122.46151, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_chehalis:
                mTracker.setScreenName("/Traffic Map/Go To Location/Chehalis");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Chelalis Traffic", 46.635529, -122.937698, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_hoodcanal:
                mTracker.setScreenName("/Traffic Map/Go To Location/Hood Canal");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Hood Canal Traffic", 47.85268, -122.628365, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_mtvernon:
                mTracker.setScreenName("/Traffic Map/Go To Location/Mt. Vernon");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Mt Vernon Traffic", 48.420657, -122.334824, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_stanwood:
                mTracker.setScreenName("/Traffic Map/Go To Location/Standwood");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Stanwood Traffic", 48.22959, -122.34581, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_monroe:
                mTracker.setScreenName("/Traffic Map/Go To Location/Monroe");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Monroe Traffic", 47.859476, -121.972446, 14);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_sultan:
                mTracker.setScreenName("/Traffic Map/Go To Location/Sultan");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Sultan Traffic", 47.86034, -121.812286, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_olympia:
                mTracker.setScreenName("/Traffic Map/Go To Location/Olympia");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Olympia Traffic", 47.021461, -122.899933, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_seattle:
                mTracker.setScreenName("/Traffic Map/Go To Location/Seattle");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Seattle Area Traffic", 47.5990, -122.3350, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_spokane:
                mTracker.setScreenName("/Traffic Map/Go To Location/Spokane");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Spokane Area Traffic", 47.658566, -117.425995, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_tacoma:
                mTracker.setScreenName("/Traffic Map/Go To Location/Tacoma");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Tacoma Traffic", 47.206275, -122.46254, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_vancouver:
                mTracker.setScreenName("/Traffic Map/Go To Location/Vancouver");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Vancouver Area Traffic", 45.639968, -122.610512, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_wenatchee:
                mTracker.setScreenName("/Traffic Map/Go To Location/Wenatchee");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Wenatchee Traffic", 47.435867, -120.309563, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_snoqualmiepass:
                mTracker.setScreenName("/Traffic Map/Go To Location/Snoqualmie Pass");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Snoqualmie Pass Traffic", 47.404481, -121.4232569, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_tricities:
                mTracker.setScreenName("/Traffic Map/Go To Location/Tri-Cities");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Tri-Cities Traffic", 46.2503607, -119.2063781, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_yakima:
                mTracker.setScreenName("/Traffic Map/Go To Location/Yakima");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation("Yakima Traffic", 46.6063273, -120.4886952, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleCameras(MenuItem item) {
        // GA tracker
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        if (showCameras) {
            for (Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();

                if (value.equalsIgnoreCase("camera")) {
                    key.setVisible(false);
                }
            }

            item.setTitle("Show Cameras");
            item.setIcon(R.drawable.ic_menu_traffic_cam_off);
            showCameras = false;

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Traffic")
                    .setAction("Cameras")
                    .setLabel("Hide Cameras")
                    .build());


        } else {
            for (Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();

                if (value.equalsIgnoreCase("camera")) {
                    key.setVisible(true);
                }
            }
            item.setTitle("Hide Cameras");
            item.setIcon(R.drawable.ic_menu_traffic_cam);
            showCameras = true;

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Traffic")
                    .setAction("Cameras")
                    .setLabel("Show Cameras")
                    .build());
        }

        // Save camera display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_CAMERAS", showCameras);
        editor.commit();
    }


    private void toggleRestAreas(MenuItem item) {
        // GA tracker
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        String label;

        if (showRestAreas) {
            for (Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();

                if (value.equalsIgnoreCase("restarea")) {
                    key.setVisible(false);
                }
            }

            item.setTitle("Show Rest Areas");
            showRestAreas = false;
            label = "Hide Rest Areas";

        } else {
            for (Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();

                if (value.equalsIgnoreCase("restarea")) {
                    key.setVisible(true);
                }
            }
            item.setTitle("Hide Rest Areas");
            showRestAreas = true;
            label = "Show Rest Areas";

        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Traffic")
                .setAction("Rest Areas")
                .setLabel(label)
                .build());

        // Save rest areas display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_REST_AREAS", showRestAreas);
        editor.commit();
    }


    public void goToLocation(String title, double latitude, double longitude, int zoomLevel) {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
    }

    /**
     * Determine whether a point is inside a complex polygon.
     * <p>
     * Iterate through collection of LatLon objects in an arrayList and see
     * if the passed latitude and longitude point is within the collection.
     *
     * @param points  List of latitude and longitude coordinates
     * @param latitude  latitude to test
     * @param longitude  longitude to test
     * @return if point is inside the polygon
     * @see <a href="http://alienryderflex.com/polygon/">http://alienryderflex.com/polygon/</a>
     */
    public boolean inPolygon(ArrayList<LatLonItem> points, double latitude, double longitude) {
        int j = points.size() - 1;
        double lat = latitude;
        double lon = longitude;
        boolean inPoly = false;

        for (int i = 0; i < points.size(); i++) {
            if ((points.get(i).getLongitude() < lon && points.get(j).getLongitude() >= lon) ||
                    (points.get(j).getLongitude() < lon && points.get(i).getLongitude() >= lon)) {
                if (points.get(i).getLatitude() + (lon - points.get(i).getLongitude()) /
                        (points.get(j).getLongitude() - points.get(i).getLongitude()) *
                        (points.get(j).getLatitude() - points.get(i).getLatitude()) < lat) {
                    inPoly = !inPoly;
                }
            }
            j = i;
        }
        return inPoly;
    }

    class CamerasOverlayTask extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);

            camerasOverlay = null;
            if (mMap != null) {
                bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            }
        }

        @Override
        public Void doInBackground(Void... unused) {
            camerasOverlay = new CamerasOverlay(
                    TrafficMapActivity.this,
                    bounds,
                    null);

            return null;
        }

        @Override
        public void onPostExecute(Void unused) {
            Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Marker, String> entry = iter.next();
                if (entry.getValue().equalsIgnoreCase("camera")) {
                    entry.getKey().remove();
                    iter.remove();
                }
            }
            cameras.clear();
            cameras = camerasOverlay.getCameraMarkers();

            if (cameras != null) {
                if (cameras.size() != 0) {
                    for (int i = 0; i < cameras.size(); i++) {
                        LatLng latLng = new LatLng(cameras.get(i).getLatitude(), cameras.get(i).getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(cameras.get(i).getTitle())
                                .snippet(cameras.get(i).getCameraId().toString())
                                .icon(BitmapDescriptorFactory.fromResource(cameras.get(i).getCameraIcon()))
                                .visible(showCameras));

                        markers.put(marker, "camera");
                    }
                }
            }

            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    class HighwayAlertsOverlayTask extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);

            alertsOverlay = null;
            if (mMap != null) {
                bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            }
        }

        @Override
        public Void doInBackground(Void... unused) {
            alertsOverlay = new HighwayAlertsOverlay(TrafficMapActivity.this, bounds);

            return null;
        }

        @Override
        public void onPostExecute(Void unused) {
            Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Marker, String> entry = iter.next();
                if (entry.getValue().equalsIgnoreCase("alert")) {
                    entry.getKey().remove();
                    iter.remove();
                }
            }
            alerts.clear();
            alerts = alertsOverlay.getAlertMarkers();

            if (alerts != null) {
                if (alerts.size() != 0) {
                    for (int i = 0; i < alerts.size(); i++) {
                        LatLng latLng = new LatLng(alerts.get(i).getStartLatitude(), alerts.get(i).getStartLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(alerts.get(i).getEventCategory())
                                .snippet(alerts.get(i).getAlertId())
                                .icon(BitmapDescriptorFactory.fromResource(alerts.get(i).getCategoryIcon()))
                                .visible(true));

                        markers.put(marker, "alert");
                    }
                }
            }

            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    /**
     * Build and draw rest areas on the map
     */
    class RestAreasOverlayTask extends AsyncTask<Void, Void, Void> {
        @Override
        public void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);
            restAreasOverlay = null;
        }

        @Override
        public Void doInBackground(Void... unused) {
            restAreasOverlay = new RestAreasOverlay(getResources().openRawResource(R.raw.restareas));
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            super.onPostExecute(result);

            Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Marker, String> entry = iter.next();
                if (entry.getValue().equalsIgnoreCase("restArea")) {
                    entry.getKey().remove();
                    iter.remove();
                }
            }
            restAreas.clear();
            restAreas = restAreasOverlay.getRestAreaItems();

            try{
                for (int i = 0; i < restAreas.size(); i++) {
                    LatLng latLng = new LatLng(restAreas.get(i).getLatitude(), restAreas.get(i).getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(restAreas.get(i).getLocation())
                            // Save the whole rest area object as the snippet
                            .snippet(new Gson().toJson(restAreas.get(i)))
                            .icon(BitmapDescriptorFactory.fromResource(restAreas.get(i).getIcon()))
                            .visible(showRestAreas));
                    markers.put(marker, "restArea");
                }
            } catch (NullPointerException e) {
                // Ignore for now. Simply don't draw the marker.
            }

            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    /**
     * Build and draw any callouts on the map
     */
    class CalloutsOverlayTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setSupportProgressBarIndeterminateVisibility(true);
            calloutsOverlay = null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            calloutsOverlay = new CalloutsOverlay();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Marker, String> entry = iter.next();
                if (entry.getValue().equalsIgnoreCase("callout")) {
                    entry.getKey().remove();
                    iter.remove();
                }
            }

            callouts.clear();
            callouts = calloutsOverlay.getCalloutItems();

            try {
                if (callouts != null) {
                    if (callouts.size() != 0) {
                        for (int i = 0; i < callouts.size(); i++) {
                            LatLng latLng = new LatLng(callouts.get(i).getLatitude(), callouts.get(i).getLongitude());
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(callouts.get(i).getTitle())
                                    .snippet(callouts.get(i).getImageUrl())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.jblm))
                                    .visible(true));

                            markers.put(marker, "callout");
                        }
                    }
                }
            } catch (NullPointerException e) {
                // Ignore for now. Simply don't draw the marker.
            }

            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    public boolean onMyLocationButtonClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (location == null) {
                requestLocationUpdates();
            } else {
                handleNewLocation(location);
            }
        }

        return true;
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (location == null) {
                requestLocationUpdates();
            }
        }
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
    }

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    /**
     *
     * @param location
     */
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mMap.animateCamera(cameraUpdate);
    }

    /**
     * Request location updates after checking permissions first.
     */
    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(TrafficMapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    TrafficMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show explanation to user explaining why we need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("To receive relevant location based notifications you must allow us access to your location.");
                builder.setTitle("Location Services");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(
                                TrafficMapActivity.this,
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_ACCESS_FINE_LOCATION);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();

            } else {
                // No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(TrafficMapActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.i(TAG, "Request permissions granted!!!");
                mMap.setMyLocationEnabled(true);
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            } else {
                // Permission was denied or request was cancelled
                Log.i(TAG, "Request permissions denied...");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
