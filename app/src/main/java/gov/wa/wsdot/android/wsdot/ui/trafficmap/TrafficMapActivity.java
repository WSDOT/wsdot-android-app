/*
 * Copyright (c) 2017 Washington State Department of Transportation
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
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
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.service.CamerasSyncService;
import gov.wa.wsdot.android.wsdot.service.HighwayAlertsSyncService;
import gov.wa.wsdot.android.wsdot.shared.CalloutItem;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.shared.LatLonItem;
import gov.wa.wsdot.android.wsdot.shared.RestAreaItem;
import gov.wa.wsdot.android.wsdot.shared.TravelChartRouteItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.alert.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.ui.callout.CalloutActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraListActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.besttimestotravel.TravelChartsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.expresslanes.SeattleExpressLanesActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.incidents.TrafficAlertsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.restareas.RestAreaActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.traveltimes.TravelTimesActivity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.map.CalloutsOverlay;
import gov.wa.wsdot.android.wsdot.util.map.CamerasOverlay;
import gov.wa.wsdot.android.wsdot.util.map.HighwayAlertsOverlay;
import gov.wa.wsdot.android.wsdot.util.map.RestAreasOverlay;

public class TrafficMapActivity extends BaseActivity implements
        OnMarkerClickListener, OnMyLocationButtonClickListener, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener,
        OnRequestPermissionsResultCallback, OnMapReadyCallback,
        ClusterManager.OnClusterItemClickListener<CameraItem>,
        ClusterManager.OnClusterClickListener<CameraItem> {

    private static final String TAG = TrafficMapActivity.class.getSimpleName();

    private GoogleMap mMap;
    private HighwayAlertsOverlay alertsOverlay = null;
    private CamerasOverlay camerasOverlay = null;
    private RestAreasOverlay restAreasOverlay = null;
    private CalloutsOverlay calloutsOverlay = null;
    private List<CameraItem> cameras = new ArrayList<>();
    private List<HighwayAlertsItem> alerts = new ArrayList<>();
    private List<RestAreaItem> restAreas = new ArrayList<>();
    private List<CalloutItem> callouts = new ArrayList<>();
    private HashMap<Marker, String> markers = new HashMap<>();

    boolean clusterCameras;
    boolean showCameras;
    boolean showAlerts;
    boolean showCallouts;
    boolean showRestAreas;

    boolean bestTimesAvailable = false;
    String bestTimesTitle = "";

    private ArrayList<LatLonItem> seattleArea = new ArrayList<>();

    static final private int MENU_ITEM_EXPRESS_LANES = Menu.FIRST;
    static private int menu_item_refresh = 1;

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

    // Declare a variable for the cluster manager.
    private ClusterManager<CameraItem> mClusterManager;

    private Toolbar mToolbar;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map);

        enableAds(getString(R.string.default_ad_target));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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
        clusterCameras = settings.getBoolean("KEY_CLUSTER_CAMERAS", false);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true);
        showAlerts = settings.getBoolean("KEY_SHOW_ALERTS", true);
        showCallouts = settings.getBoolean("KEY_SHOW_CALLOUTS", true);
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

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);

        // check for travel charts
        new TravelChartsAvailableTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean seenTip = settings.getBoolean("KEY_SEEN_TRAFFIC_MENU_TIP", false);

        if (!seenTip && !clusterCameras && showCameras && showAlerts) {
            try {
                TapTargetView.showFor(this,                 // `this` is an Activity
                    TapTarget.forToolbarOverflow(mToolbar, "Map Options", "Save locations to your favorites, edit traffic map layers & more.")
                            // All options below are optional
                            .outerCircleColor(R.color.primary)      // Specify a color for the outer circle
                            .targetCircleColor(R.color.white)   // Specify a color for the target circle
                            .titleTextSize(20)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                            .textColor(R.color.white)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                            .targetRadius(60),                  // Specify the target radius (in dp)
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);      // This call is optional

                        }
                    });
            } catch (NullPointerException e){
                Log.e(TAG, "Null pointer exception while trying to show tip view");
            }
        }

        settings.edit().putBoolean("KEY_SEEN_TRAFFIC_MENU_TIP", true).apply();

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
        editor.apply();
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

        mMap.setOnCameraIdleListener(new OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                startService(camerasIntent);
                startService(alertsIntent);
                mClusterManager.onCameraIdle();
            }
        });

        setUpClusterer();

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

    // Icon Clustering helpers
    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setRenderer(new CameraRenderer());

        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterClickListener(this);

        mClusterManager.cluster();
    }

    public boolean onMarkerClick(Marker marker) {
        Bundle b = new Bundle();
        Intent intent;
        if (markers.get(marker) == null) { // Not in our markers, must be cluster icon
            mClusterManager.onMarkerClick(marker);
        } else  if (markers.get(marker).equalsIgnoreCase("camera")) {
            // GA tracker
            mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
            mTracker.setScreenName("/Traffic Map/Cameras");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
            intent = new Intent(this, CameraActivity.class);
            b.putInt("id", Integer.parseInt(marker.getSnippet()));
            b.putString("advertisingTarget", "other");
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

    @Override
    public boolean onClusterItemClick(CameraItem cameraItem) {
        // GA tracker
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
        mTracker.setScreenName("/Traffic Map/Camera");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        Bundle b = new Bundle();

        Intent intent = new Intent(this, CameraActivity.class);
        b.putInt("id", cameraItem.getCameraId());
        intent.putExtras(b);
        TrafficMapActivity.this.startActivity(intent);
        return false;
    }

    @Override
    public boolean onClusterClick(Cluster<CameraItem> cluster) {
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
        if (isCameraGroup(cluster)){
            mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
            mTracker.setScreenName("/Traffic Map/Camera Group");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
            Bundle b = new Bundle();
            Intent intent;
            intent = new Intent(this, CameraListActivity.class);

            // Load camera ids into array for bundle.
            int cameraIds[] = new int[cluster.getSize()];
            String cameraUrls[] = new String[cluster.getSize()];

            int index = 0;
            for (CameraItem camera: cluster.getItems()) {
                cameraIds[index] = camera.getCameraId();
                cameraUrls[index] = camera.getImageUrl();
                index++;
            }

            b.putStringArray("cameraUrls", cameraUrls);
            b.putIntArray("cameraIds", cameraIds);

            intent.putExtras(b);
            TrafficMapActivity.this.startActivity(intent);
        }else {
            mTracker.setScreenName("/Traffic Map/Camera Cluster");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (ClusterItem item : cluster.getItems()) {
                builder.include(item.getPosition());
            }
            // Get the LatLngBounds
            final LatLngBounds bounds = builder.build();

            // Animate camera to the bounds
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    /**
     * Set up the App bar menu
     *
     * Loop through all menu items, checking ID for set up.
     * We do it this way because item indices aren't set since the menu is dynamic.
     * (ex. travel charts may be added to the start)
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.traffic, menu);

        if (bestTimesAvailable) {
            menu.add(0, R.id.best_times_to_travel, 0, "Best Times to Travel")
                    .setIcon(R.drawable.ic_menu_chart)
                    .setActionView(R.layout.action_bar_notification_icon)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            final MenuItem chartMenuItem = menu.findItem(R.id.best_times_to_travel);
            // Since we added an action view, need to hook up the onclick ourselves.
            chartMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(View v) {
                                                                     TrafficMapActivity.this.onMenuItemSelected(0, chartMenuItem);
                                                                 }
                                                             });
            menu_item_refresh = 2;
        } else {
            menu_item_refresh = 1;
        }

        for (int i = 0; i < menu.size(); i++){
            switch (menu.getItem(i).getItemId()){
                case R.id.toggle_cameras:
                    if (showCameras) {
                        menu.getItem(i).setTitle("Hide Cameras");
                        menu.getItem(i).setIcon(R.drawable.ic_menu_traffic_cam);
                    } else {
                        menu.getItem(i).setTitle("Show Cameras");
                        menu.getItem(i).setIcon(R.drawable.ic_menu_traffic_cam_off);
                    }
                    break;
                case R.id.toggle_clustering:
                    if (clusterCameras) {
                        menu.getItem(i).setTitle("Uncluster Cameras");
                        menu.getItem(i).setIcon(R.drawable.ic_menu_traffic_cam);
                    } else {
                        menu.getItem(i).setTitle("Cluster Cameras");
                        menu.getItem(i).setIcon(R.drawable.ic_menu_traffic_cam_off);
                    }
                    break;
                case R.id.toggle_alerts:
                    if (showAlerts) {
                        menu.getItem(i).setTitle("Hide Highway Alerts");
                    } else {
                        menu.getItem(i).setTitle("Show Highway Alerts");
                    }
                    break;
                case R.id.toggle_callouts:
                    if (showCallouts) {
                        menu.getItem(i).setTitle("Hide JBLM");
                    } else {
                        menu.getItem(i).setTitle("Show JBLM");
                    }
                    break;
                case R.id.toggle_rest_areas:
                    if (showRestAreas) {
                        menu.getItem(i).setTitle("Hide Rest Areas");
                    } else {
                        menu.getItem(i).setTitle("Show Rest Areas");
                    }
                    break;
                default:
                    break;
            }
        }

        /*
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

            case R.id.best_times_to_travel:

                Intent chartsIntent = new Intent(this, TravelChartsActivity.class);
                chartsIntent.putExtra("title", bestTimesTitle);

                startActivity(chartsIntent);

                break;
            case R.id.set_favorite:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.WSDOT_popup);

                final EditText textEntryView = new EditText(this);
                textEntryView.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(textEntryView);
                builder.setMessage(R.string.add_location_dialog);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = textEntryView.getText().toString();
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
            case R.id.refresh:
                refreshOverlays(item);
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
            case R.id.toggle_clustering:
                toggleCluster(item);
                return true;
            case R.id.toggle_cameras:
                toggleCameras(item);
                return true;
            case R.id.toggle_alerts:
                toggleAlerts(item);
                return true;
            case R.id.toggle_callouts:
                toggleCallouts();
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
                goToLocation(48.756302, -122.46151, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_chehalis:
                mTracker.setScreenName("/Traffic Map/Go To Location/Chehalis");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(46.635529, -122.937698, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_hoodcanal:
                mTracker.setScreenName("/Traffic Map/Go To Location/Hood Canal");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.85268, -122.628365, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_mtvernon:
                mTracker.setScreenName("/Traffic Map/Go To Location/Mt. Vernon");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(48.420657, -122.334824, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_stanwood:
                mTracker.setScreenName("/Traffic Map/Go To Location/Standwood");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(48.22959, -122.34581, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_monroe:
                mTracker.setScreenName("/Traffic Map/Go To Location/Monroe");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.859476, -121.972446, 14);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_sultan:
                mTracker.setScreenName("/Traffic Map/Go To Location/Sultan");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.86034, -121.812286, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_olympia:
                mTracker.setScreenName("/Traffic Map/Go To Location/Olympia");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.021461, -122.899933, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_seattle:
                mTracker.setScreenName("/Traffic Map/Go To Location/Seattle");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.5990, -122.3350, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_spokane:
                mTracker.setScreenName("/Traffic Map/Go To Location/Spokane");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.658566, -117.425995, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_tacoma:
                mTracker.setScreenName("/Traffic Map/Go To Location/Tacoma");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.206275, -122.46254, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_vancouver:
                mTracker.setScreenName("/Traffic Map/Go To Location/Vancouver");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(45.639968, -122.610512, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_wenatchee:
                mTracker.setScreenName("/Traffic Map/Go To Location/Wenatchee");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.435867, -120.309563, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_snoqualmiepass:
                mTracker.setScreenName("/Traffic Map/Go To Location/Snoqualmie Pass");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(47.404481, -121.4232569, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_tricities:
                mTracker.setScreenName("/Traffic Map/Go To Location/Tri-Cities");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(46.2503607, -119.2063781, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_yakima:
                mTracker.setScreenName("/Traffic Map/Go To Location/Yakima");
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                goToLocation(46.6063273, -120.4886952, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.map_legend:
                AlertDialog.Builder imageDialog = new AlertDialog.Builder(this, R.style.WSDOT_popup);
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

                View layout = inflater.inflate(R.layout.map_legend_layout, null);
                imageDialog.setView(layout);
                imageDialog.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    }
                });
                imageDialog.create();
                imageDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void refreshOverlays(final MenuItem item){

        // Force service intents to refresh data
        // Set extra back to false so force wont happen on idle.
        camerasIntent.putExtra("forceUpdate", true);
        alertsIntent.putExtra("forceUpdate", true);

        // define the animation for rotation
        Animation animation = new RotateAnimation(360.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(Animation.INFINITE);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                mToolbar.getMenu().getItem(menu_item_refresh).setActionView(null);
                mToolbar.getMenu().getItem(menu_item_refresh).setIcon(R.drawable.ic_menu_refresh);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        ImageView imageView = new ImageView(this, null, android.R.style.Widget_Material_ActionButton);
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_menu_refresh));
        imageView.setPadding(31, imageView.getPaddingTop(), 32, imageView.getPaddingBottom());
        imageView.startAnimation(animation);
        item.setActionView(imageView);
        startService(camerasIntent);
        startService(alertsIntent);
        camerasIntent.putExtra("forceUpdate", false);
        alertsIntent.putExtra("forceUpdate", false);
    }

    /*
     *  Adds or removes cameras from the cluster manager.
     *  When clustering is turned off all items are removed from the cluster manager and
     *  markers are plotted normally.
     */
    private void toggleCluster(MenuItem item){
        // GA tracker
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        if (clusterCameras) {

            clusterCameras = false;

            if (cameras != null) {
                mClusterManager.clearItems();
                mClusterManager.cluster();
                addCameraMarkers(cameras);
            }

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Traffic")
                    .setAction("Cameras")
                    .setLabel("Clustering off")
                    .build());

        } else {

            clusterCameras = true;

            removeCameraMarkers();

            if (cameras != null && showCameras) {
                mClusterManager.addItems(cameras);
                mClusterManager.cluster();
            }

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Traffic")
                    .setAction("Cameras")
                    .setLabel("Clustering on")
                    .build());
        }

        // Save camera display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_CLUSTER_CAMERAS", clusterCameras);
        editor.apply();
    }

    /**
     * Toggle camera visibility
     * checks clusterCameras to see the current state of the camera markers and hide
     * them accordingly.
     *
     * @param item
     */
    private void toggleCameras(MenuItem item) {
        // GA tracker
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        if (showCameras) {
            if (clusterCameras) {
                mClusterManager.clearItems();
                mClusterManager.cluster();
            }else {
                hideCameraMarkers();
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

            if (clusterCameras) {
                if (cameras != null) {
                    mClusterManager.addItems(cameras);
                    mClusterManager.cluster();
                }
            } else {
                showCameraMarkers();
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
        editor.apply();
    }

    /**
     * Toggles callout markers
     */
    private void toggleCallouts() {
        // GA tracker
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        String label;

        if (showCallouts) {
            hideCalloutMarkers();
            showCallouts = false;
            label = "Hide JBLM";

        } else {
            showCalloutMarkers();
            showCallouts = true;
            label = "Show JBLM";

        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Traffic")
                .setAction("Toggle JBLM")
                .setLabel(label)
                .build());

        // Save rest areas display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_CALLOUTS", showCallouts);
        editor.apply();
    }

    /**
     * Toggles rest area markers on/off
     * @param item
     */
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
        editor.apply();
    }

    /**
     * Toggles alert markers on/off
     * @param item
     */
    private void toggleAlerts(MenuItem item) {
        // GA tracker
        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        String label;

        if (showAlerts) {
            for (Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();

                if (value.equalsIgnoreCase("alert")) {
                    key.setVisible(false);
                }
            }

            showAlerts = false;
            label = "Hide Alerts";

        } else {
            for (Entry<Marker, String> entry : markers.entrySet()) {
                Marker key = entry.getKey();
                String value = entry.getValue();

                if (value.equalsIgnoreCase("alert")) {
                    key.setVisible(true);
                }
            }
            showAlerts = true;
            label = "Show Alerts";
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Traffic")
                .setAction("Highway Alerts")
                .setLabel(label)
                .build());

        // Save rest areas display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_ALERTS", showAlerts);
        editor.apply();
    }

    public void goToLocation(double latitude, double longitude, int zoomLevel) {
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
        boolean inPoly = false;

        for (int i = 0; i < points.size(); i++) {
            if ((points.get(i).getLongitude() <longitude && points.get(j).getLongitude() >= longitude) ||
                    (points.get(j).getLongitude() < longitude && points.get(i).getLongitude() >= longitude)) {
                if (points.get(i).getLatitude() + (longitude - points.get(i).getLongitude()) /
                        (points.get(j).getLongitude() - points.get(i).getLongitude()) *
                        (points.get(j).getLatitude() - points.get(i).getLatitude()) < latitude) {
                    inPoly = !inPoly;
                }
            }
            j = i;
        }
        return inPoly;
    }

    /**
     * Loads Cameras.
     * Checks clusterCameras boolean and will either add camera items to the
     * cluster manager or simply plot them normally.
     */
    class CamerasOverlayTask extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
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


                if (clusterCameras) {
                    mClusterManager.clearItems();
                    if (showCameras) {
                        mClusterManager.addItems(cameras);
                    }
                    mClusterManager.cluster();
                } else {
                    addCameraMarkers(cameras);
                }
            }
            if (mToolbar.getMenu().getItem(menu_item_refresh).getActionView() != null) {
                mToolbar.getMenu().getItem(menu_item_refresh).getActionView().getAnimation().setRepeatCount(0);
            }
        }
    }

    class HighwayAlertsOverlayTask extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
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
                                .visible(showAlerts));

                        markers.put(marker, "alert");
                    }
                }
            }
            if (mToolbar.getMenu().getItem(menu_item_refresh).getActionView() != null) {
                mToolbar.getMenu().getItem(menu_item_refresh).getActionView().getAnimation().setRepeatCount(0);
            }
        }
    }

    /**
     * Build and draw rest areas on the map
     */
    class RestAreasOverlayTask extends AsyncTask<Void, Void, Void> {
        @Override
        public void onPreExecute() {
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
        }
    }

    /**
     * Build and draw any callouts on the map
     */
    private class CalloutsOverlayTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                                    .visible(showCallouts));

                            markers.put(marker, "callout");
                        }
                    }
                }
            } catch (NullPointerException e) {
                // Ignore for now. Simply don't draw the marker.
            }
        }
    }

    /**
     *  Checks for posted travel charts.
     */
    private class TravelChartsAvailableTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(APIEndPoints.TRAVEL_CHARTS);
                URLConnection urlConn = url.openConnection();

                BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);

                String jsonFile = "";
                String line;
                while ((line = in.readLine()) != null)
                    jsonFile += line;
                in.close();

                JSONObject obj = new JSONObject(jsonFile);

                TrafficMapActivity.this.bestTimesTitle = obj.getString("name");

                return obj.getBoolean("available");

            } catch (Exception e) {
                Log.e(TAG, "Error parsing travel chart JSON feed", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            TrafficMapActivity.this.bestTimesAvailable = result;
            TrafficMapActivity.this.invalidateOptionsMenu();
        }
    }

    public boolean onMyLocationButtonClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            requestLocationUpdates();

            if (location != null) {
                moveToNewLocation(location);
            }
        }
        return true;
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
    }

    public void onLocationChanged(Location location) {
        // check users speed
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (location.getSpeed() > 9 && !settings.getBoolean("KEY_SEEN_DRIVER_ALERT", false)) {

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("KEY_SEEN_DRIVER_ALERT", true);
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.WSDOT_popup);
            builder.setCancelable(false);
            builder.setTitle("You're moving fast.");
            builder.setMessage("Please do not use the app while driving.");
            builder.setPositiveButton("I'm a Passenger", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {}
            });
            builder.create().show();
        }
    }

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    /**
     *
     * @param location - The new location returned from location updates
     */
    private void moveToNewLocation(Location location) {
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
            ActivityCompat.requestPermissions(TrafficMapActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
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
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 || permissions.length > 0) { // Check if request was canceled.
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
        }
    }

    /**
     * Based on custom renderer demo here:
     * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/clustering/view/DefaultClusterRenderer.java
     */
    private class CameraRenderer extends DefaultClusterRenderer<CameraItem> {
        private final IconGenerator mClusterIconGenerator;
        private final float mDensity;
        private final Bitmap singleCameraIcon = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
        private final Bitmap openCameraGroupIcon = BitmapFactory.decodeResource(getResources(), R.drawable.camera_cluster_open);
        private SparseArray<BitmapDescriptor> mIcons = new SparseArray<>();

        private CameraRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);
            Context context = getApplicationContext();
            mDensity = context.getResources().getDisplayMetrics().density;
            mClusterIconGenerator = new IconGenerator(context);
            mClusterIconGenerator.setContentView(makeSquareTextView(context));
            mClusterIconGenerator.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        }

        private SquareTextView makeSquareTextView(Context context) {
            SquareTextView squareTextView = new SquareTextView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            squareTextView.setLayoutParams(layoutParams);
            squareTextView.setId(R.id.amu_text);
            int twelveDpi = (int) (6 * mDensity);
            squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
            return squareTextView;
        }

        private LayerDrawable makeClusterBackground(Drawable backgroundImage) {
            ShapeDrawable outline = new ShapeDrawable(new OvalShape());
            outline.getPaint().setColor(0x80ffffff); // Transparent white.
            LayerDrawable background = new LayerDrawable(new Drawable[]{outline, backgroundImage});
            int strokeWidth = (int) (getApplication().getResources().getDisplayMetrics().density * 2);
            background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
            return background;
        }

        private Drawable getBackgroundImage(int bucket){
            if (bucket < 11){
                return ResourcesCompat.getDrawable(getResources(), R.drawable.camera_cluster_1, null);
            } else if (bucket < 51){
                return ResourcesCompat.getDrawable(getResources(), R.drawable.camera_cluster_2, null);
            } else if (bucket < 101){
                return ResourcesCompat.getDrawable(getResources(), R.drawable.camera_cluster_3, null);
            } else if (bucket < 201){
                return ResourcesCompat.getDrawable(getResources(), R.drawable.camera_cluster_4, null);
            } else {
                return ResourcesCompat.getDrawable(getResources(), R.drawable.camera_cluster_5, null);
            }
        }

        @Override
        protected void onBeforeClusterItemRendered(CameraItem camera, MarkerOptions markerOptions) {
            // Draw a single camera
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(singleCameraIcon));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<CameraItem> cluster, MarkerOptions markerOptions) {
            // Draw multiple cameras

            // Loop through all cameras in cluster, check lat/long, if same make group?
            // How do we mark this special kind of cluster? With the blue icon.
            // How to we capture click events and know it's one of these groups?
            int bucket = getBucket(cluster);

            if (isCameraGroup(cluster)) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(openCameraGroupIcon));
            } else {
                BitmapDescriptor descriptor = mIcons.get(bucket);

                if (descriptor == null) {
                    String countText = getClusterText(bucket);
                    mClusterIconGenerator.setBackground(makeClusterBackground(getBackgroundImage(bucket)));
                    Bitmap icon = mClusterIconGenerator.makeIcon(countText);
                    descriptor = BitmapDescriptorFactory.fromBitmap(icon);
                    mIcons.put(bucket, descriptor);
                }
                markerOptions.icon(descriptor);
            }
        }
        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }
    }

    // Camera marker and clustering Helper functions

    /**
     * Checks if this cluster can be opened to a list of cameras
     * Arbitrarily assumes cameras in same space will have no more than 20 images. This also helps performance.
     *
     * @param cluster
     * @return
     */
    private boolean isCameraGroup(Cluster<CameraItem> cluster){
        if (cluster.getSize() < 20) {
            CameraItem firstCamera = (CameraItem) cluster.getItems().toArray()[0];
            for (CameraItem camera: cluster.getItems()){
                if (!firstCamera.getLatitude().equals(camera.getLatitude()) || !firstCamera.getLongitude().equals(camera.getLongitude())){
                    return false;
                }
            }
            return true;
        }else {
            return false;
        }
    }

    private void hideCalloutMarkers() {
        for (Entry<Marker, String> entry : markers.entrySet()) {
            Marker key = entry.getKey();
            String value = entry.getValue();
            if (value.equalsIgnoreCase("callout")) {
                key.setVisible(false);
            }
        }
    }

    private void showCalloutMarkers() {
        for (Entry<Marker, String> entry : markers.entrySet()) {
            Marker key = entry.getKey();
            String value = entry.getValue();
            if (value.equalsIgnoreCase("callout")) {
                key.setVisible(true);
            }
        }
    }

    /**
     * sets all camera marker visibility to false.
     * NOTE: Doesn't work for clusters
     */
    private void hideCameraMarkers(){
        for (Entry<Marker, String> entry : markers.entrySet()) {
            Marker key = entry.getKey();
            String value = entry.getValue();
            if (value.equalsIgnoreCase("camera")) {
                key.setVisible(false);
            }
        }
    }

    /**
     * sets all camera marker visibility to true.
     * NOTE: Doesn't work for clusters
     */
    private void showCameraMarkers(){
        for (Entry<Marker, String> entry : markers.entrySet()) {
            Marker key = entry.getKey();
            String value = entry.getValue();
            if (value.equalsIgnoreCase("camera")) {
                key.setVisible(true);
            }
        }
    }

    /**
     * Helper
     * Adds camera markers from the map.
     * NOTE: Doesn't work for clusters
     */
    private void addCameraMarkers(List<CameraItem>cameras){
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

    /**
     * Removes camera markers from the map.
     * Uses for switching clustering on/off.
     * NOTE: Doesn't work for clusters
     */
    private void removeCameraMarkers(){
        for (Entry<Marker, String> entry : markers.entrySet()) {
            Marker key = entry.getKey();
            String value = entry.getValue();
            if (value.equalsIgnoreCase("camera")) {
                key.remove();
            }
        }
    }
}