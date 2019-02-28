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
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
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
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationEntity;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.shared.LatLonItem;
import gov.wa.wsdot.android.wsdot.shared.RestAreaItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.alert.detail.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.ui.alert.map.MapHighwayAlertViewModel;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraListActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea.HighwayAlertListActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.besttimestotravel.TravelChartsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.expresslanes.SeattleExpressLanesActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.news.NewsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.restareas.RestAreaActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.SocialMediaTabActivity;
import gov.wa.wsdot.android.wsdot.ui.traveltimes.TravelTimesActivity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.map.RestAreasOverlay;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class TrafficMapActivity extends BaseActivity implements
        OnMarkerClickListener, OnMyLocationButtonClickListener, ConnectionCallbacks,
        OnConnectionFailedListener,
        OnRequestPermissionsResultCallback, OnMapReadyCallback,
        ClusterManager.OnClusterItemClickListener<CameraItem>,
        ClusterManager.OnClusterClickListener<CameraItem> {

    private static final String TAG = TrafficMapActivity.class.getSimpleName();

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private GoogleMap mMap;
    private Handler handler = new Handler();
    private Timer timer;

    private RestAreasOverlay restAreasOverlay = null;
    private List<CameraItem> cameras = new ArrayList<>();
    private List<HighwayAlertsItem> alerts = new ArrayList<>();
    private List<RestAreaItem> restAreas = new ArrayList<>();
    private HashMap<Marker, String> markers = new HashMap<>();

    boolean clusterCameras;
    boolean showCameras;
    boolean showAlerts;
    boolean showCallouts;
    boolean showRestAreas;

    ProgressBar mProgressBar;

    FloatingActionButton fabLayers;
    FloatingActionButton fabCameras;
    FloatingActionButton fabClusters;
    FloatingActionButton fabAlerts;
    FloatingActionButton fabRestareas;

    TextView fabLabelCameras;
    TextView fabLabelClusters;
    TextView fabLabelAlerts;
    TextView fabLabelRestareas;

    LinearLayout fabLayoutCameras;
    LinearLayout fabLayoutClusters;
    LinearLayout fabLayoutAlerts;
    LinearLayout fabLayoutRestareas;

    boolean isFABOpen = false;

    boolean bestTimesAvailable = false;
    String bestTimesTitle = "";

    private ArrayList<LatLonItem> seattleArea = new ArrayList<>();

    static private int menu_item_refresh = 1;

    private static AsyncTask<Void, Void, Void> mRestAreasOverlayTask = null;
    private LatLngBounds bounds;
    private double latitude;
    private double longitude;
    private int zoom;
    private GoogleApiClient mGoogleApiClient;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_ACCESS_FINE_LOCATION = 100;

    private static MapHighwayAlertViewModel mapHighwayAlertViewModel;
    private static MapCameraViewModel mapCameraViewModel;
    private static FavoriteMapLocationViewModel favoriteMapLocationViewModel;

    private boolean extrasRead = false;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ClusterManager<CameraItem> mClusterManager;

    private Toolbar mToolbar;

    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map);

        enableAds(getString(R.string.traffic_ad_target));

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Setup bounding box for Seattle area.
        seattleArea.add(new LatLonItem(48.01749, -122.46185));
        seattleArea.add(new LatLonItem(48.01565, -121.86584));
        seattleArea.add(new LatLonItem(47.27737, -121.86310));
        seattleArea.add(new LatLonItem(47.28109, -122.45911));

        // Initialize AsyncTasks
        mRestAreasOverlayTask = new RestAreasOverlayTask();

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

        // Check if we came from favorites/MyRoutes/alert
        if (savedInstanceState != null ) {
            extrasRead = savedInstanceState.getBoolean("read_extras", false);
        }

        if (!extrasRead) {
            Bundle b = getIntent().getExtras();
            if (b != null) {
                if (getIntent().hasExtra("lat"))
                    latitude = b.getDouble("lat", latitude);
                if (getIntent().hasExtra("long"))
                    longitude = b.getDouble("long", longitude);
                if (getIntent().hasExtra("zoom"))
                    zoom = b.getInt("zoom", zoom);
                getIntent().getExtras().clear();
            }
            extrasRead = true;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null){
                    return;
                }
                onNewLocation(locationResult.getLastLocation());
            }
        };

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);

        favoriteMapLocationViewModel = ViewModelProviders.of(this, viewModelFactory).get(FavoriteMapLocationViewModel.class);

        mapCameraViewModel = ViewModelProviders.of(this, viewModelFactory).get(MapCameraViewModel.class);
        mapCameraViewModel.init(null);

        mProgressBar = findViewById(R.id.data_progress_bar);

        setUpFabMenu();

        MyLogger.crashlyticsLog("Traffic", "Screen View", "TrafficMapActivity", 1);

        // check for travel charts
        new TravelChartsAvailableTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setFirebaseAnalyticsScreenName("TrafficMap");

        mGoogleApiClient.connect();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean seenTip = settings.getBoolean("KEY_SEEN_TRAFFIC_LAYERS_TIP", false);

        if (!seenTip) {
            try {
                TapTargetView.showFor(this, // `this` is an Activity
                        TapTarget.forView(fabLayers, "Map Layers", "Tap to edit what information displays on the Traffic Map.")
                                // All options below are optional
                                .outerCircleColor(R.color.primary_default)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(15)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(true)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(40),
                        new TapTargetView.Listener() {
                            @Override
                            public void onTargetClick(TapTargetView view) {
                                super.onTargetClick(view);      // This call is optional
                                showFABMenu();
                            }
                        });
            } catch (NullPointerException | IllegalArgumentException e) {
                Log.e(TAG, "Exception while trying to show tip view");
                Log.e(TAG, e.getMessage());
            }
        }

        settings.edit().putBoolean("KEY_SEEN_TRAFFIC_LAYERS_TIP", true).apply();

    }

    @Override
    protected void onPause() {
        super.onPause();

        closeFABMenu();

        if (mGoogleApiClient.isConnected()) {
            getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
            mGoogleApiClient.disconnect();
        }

        if (timer != null) {
            timer.cancel();
        }

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
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMarkerClickListener(this);

        mMap.setOnMapClickListener(latLng -> closeFABMenu());
        mMap.setOnCameraMoveStartedListener(i -> closeFABMenu());

        setUpClusterer();

        mMap.setOnCameraIdleListener(() -> {
            mapCameraViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);
            mapHighwayAlertViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);
            mClusterManager.onCameraIdle();
        });

        mProgressBar.setVisibility(View.VISIBLE);

        mapHighwayAlertViewModel = ViewModelProviders.of(this, viewModelFactory).get(MapHighwayAlertViewModel.class);

        mapHighwayAlertViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        mProgressBar.setVisibility(View.GONE);
                        if (mToolbar.getMenu().size() > menu_item_refresh) {
                            if (mToolbar.getMenu().getItem(menu_item_refresh).getActionView() != null) {
                                mToolbar.getMenu().getItem(menu_item_refresh).getActionView().getAnimation().setRepeatCount(0);
                            }
                        }
                        break;
                    case ERROR:
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "connection error, failed to load alerts", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mapHighwayAlertViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);

        mapHighwayAlertViewModel.getDisplayAlerts().observe(this, alertItems -> {
            Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Marker, String> entry = iter.next();
                if (entry.getValue().equalsIgnoreCase("alert")) {
                    entry.getKey().remove();
                    iter.remove();
                }
            }

            alerts.clear();
            alerts = alertItems;

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
        });

        mapCameraViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mProgressBar.setVisibility(View.GONE);
                        if (mToolbar.getMenu().size() > menu_item_refresh) {
                            if (mToolbar.getMenu().getItem(menu_item_refresh).getActionView() != null) {
                                mToolbar.getMenu().getItem(menu_item_refresh).getActionView().getAnimation().setRepeatCount(0);
                            }
                        }
                        break;
                    case ERROR:
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "connection error, failed to load cameras", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mapCameraViewModel.getDisplayCameras().observe(this, cameraItems -> {
            if (cameraItems != null) {

                Iterator<Entry<Marker, String>> iter = markers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Marker, String> entry = iter.next();
                    if (entry.getValue().equalsIgnoreCase("camera")) {
                        entry.getKey().remove();
                        iter.remove();
                    }
                }
                cameras.clear();
                cameras = cameraItems;

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
        });

        mapCameraViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        timer = new Timer();
        timer.schedule(new AlertsTimerTask(), 0, 300000); // Schedule alerts to update every 5 minutes

        enableMyLocation();

        if (mRestAreasOverlayTask.getStatus() == AsyncTask.Status.FINISHED) {
            mRestAreasOverlayTask = new RestAreasOverlayTask().execute();
        } else if (mRestAreasOverlayTask.getStatus() == AsyncTask.Status.PENDING) {
            mRestAreasOverlayTask.execute();
        }
    }

    private void setUpFabMenu() {

        // set up layers FAB menu
        fabLayers = findViewById(R.id.fab);

        fabLayoutCameras = findViewById(R.id.fabLayoutCameras);
        fabLayoutClusters = findViewById(R.id.fabLayoutClusters);
        fabLayoutAlerts = findViewById(R.id.fabLayoutAlerts);
        fabLayoutRestareas = findViewById(R.id.fabLayoutRestareas);

        fabLabelCameras = findViewById(R.id.fabLabelCameras);
        fabLabelClusters = findViewById(R.id.fabLabelClusters);
        fabLabelAlerts = findViewById(R.id.fabLabelAlerts);
        fabLabelRestareas = findViewById(R.id.fabLabelRestareas);

        fabCameras = findViewById(R.id.fabCameras);
        fabClusters = findViewById(R.id.fabClusters);
        fabAlerts = findViewById(R.id.fabAlerts);
        fabRestareas = findViewById(R.id.fabRestareas);

        if (!showCameras) {
            toggleFabOff(fabCameras);
        }

        if (!clusterCameras) {
            toggleFabOff(fabClusters);
        }

        if (!showAlerts) {
            toggleFabOff(fabAlerts);
        }

        if (!showRestAreas) {
            toggleFabOff(fabRestareas);
        }

        fabLayers.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        fabCameras.setOnClickListener(v -> {
            toggleCameras(fabCameras);
            closeFABMenu();
        });

        fabClusters.setOnClickListener(v -> {
            toggleCluster(fabClusters);
            closeFABMenu();
        });

        fabAlerts.setOnClickListener(v -> {
            toggleAlerts(fabAlerts);
            closeFABMenu();
        });

        fabRestareas.setOnClickListener(v -> {
            toggleRestAreas(fabRestareas);
            closeFABMenu();
        });
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
        } else if (markers.get(marker).equalsIgnoreCase("camera")) {
            MyLogger.crashlyticsLog("Traffic", "Tap", "Camera", 1);
            intent = new Intent(this, CameraActivity.class);
            b.putInt("id", Integer.parseInt(marker.getSnippet()));
            b.putString("advertisingTarget", getString(R.string.traffic_ad_target));
            intent.putExtras(b);
            TrafficMapActivity.this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("alert")) {
            MyLogger.crashlyticsLog("Traffic", "Tap", "Alert", 1);
            intent = new Intent(this, HighwayAlertDetailsActivity.class);
            b.putInt("id", Integer.valueOf(marker.getSnippet()));
            intent.putExtras(b);
            TrafficMapActivity.this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("restarea")) {
            MyLogger.crashlyticsLog("Traffic", "Tap", "Rest Area", 1);
            intent = new Intent(this, RestAreaActivity.class);
            intent.putExtra("restarea_json", marker.getSnippet());
            TrafficMapActivity.this.startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onClusterItemClick(CameraItem cameraItem) {

        Bundle b = new Bundle();

        Intent intent = new Intent(this, CameraActivity.class);
        b.putInt("id", cameraItem.getCameraId());
        intent.putExtras(b);
        TrafficMapActivity.this.startActivity(intent);
        return false;
    }

    @Override
    public boolean onClusterClick(Cluster<CameraItem> cluster) {
        if (isCameraGroup(cluster)) {

            Bundle b = new Bundle();
            Intent intent;
            intent = new Intent(this, CameraListActivity.class);

            // Load camera ids into array for bundle.
            int cameraIds[] = new int[cluster.getSize()];
            String cameraUrls[] = new String[cluster.getSize()];

            int index = 0;
            for (CameraItem camera : cluster.getItems()) {
                cameraIds[index] = camera.getCameraId();
                cameraUrls[index] = camera.getImageUrl();
                index++;
            }

            b.putStringArray("cameraUrls", cameraUrls);
            b.putIntArray("cameraIds", cameraIds);

            intent.putExtras(b);
            TrafficMapActivity.this.startActivity(intent);
        } else {
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
            chartMenuItem.getActionView().setOnClickListener(v -> TrafficMapActivity.this.onMenuItemSelected(0, chartMenuItem));
            menu_item_refresh = 2;
        } else {
            menu_item_refresh = 1;
        }

        for (int i = 0; i < menu.size(); i++) {
            switch (menu.getItem(i).getItemId()) {
                case R.id.toggle_cameras:
                    if (showCameras) {
                        menu.getItem(i).setTitle("Hide Cameras");
                        menu.getItem(i).setIcon(R.drawable.ic_menu_traffic_cam);
                    } else {
                        menu.getItem(i).setTitle("Show Cameras");
                        menu.getItem(i).setIcon(R.drawable.ic_menu_traffic_cam_off);
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        closeFABMenu();
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
                builder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.dismiss());
                builder.setPositiveButton(R.string.submit, (dialog, whichButton) -> {
                    String value = textEntryView.getText().toString();
                    dialog.dismiss();

                    MapLocationEntity location = new MapLocationEntity();

                    location.setTitle(value);

                    location.setLatitude(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude);
                    location.setLongitude(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude);
                    location.setZoom((int) mMap.getCameraPosition().zoom);

                    favoriteMapLocationViewModel.addMapLocation(location);

                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.refresh:
                refreshOverlays();
                return true;
            case R.id.alerts_in_area:
                if (mMap != null) {
                    LatLngBounds mBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    Intent alertsIntent = new Intent(this, HighwayAlertListActivity.class);
                    alertsIntent.putExtra("nelat", mBounds.northeast.latitude);
                    alertsIntent.putExtra("nelong", mBounds.northeast.longitude);
                    alertsIntent.putExtra("swlat", mBounds.southwest.latitude);
                    alertsIntent.putExtra("swlong", mBounds.southwest.longitude);
                    startActivity(alertsIntent);
                }
                return true;
            case R.id.express_lanes:
                Intent expressIntent = new Intent(this, SeattleExpressLanesActivity.class);
                startActivity(expressIntent);
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.social_media:
                Intent socialIntent = new Intent(this, SocialMediaTabActivity.class);
                startActivity(socialIntent);
                return true;
            case R.id.travel_times:
                Intent timesIntent = new Intent(this, TravelTimesActivity.class);
                startActivity(timesIntent);
                return true;
            case R.id.news:
                Intent newsIntent = new Intent(this, NewsActivity.class);
                startActivity(newsIntent);
                return true;
            case R.id.goto_bellingham:
                goToLocation(48.756302, -122.46151, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_chehalis:
                goToLocation(46.635529, -122.937698, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_everett:
                goToLocation(47.967976, -122.197627, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_hoodcanal:
                goToLocation(47.85268, -122.628365, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_mtvernon:
                goToLocation(48.420657, -122.334824, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_stanwood:
                goToLocation(48.22959, -122.34581, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_monroe:
                goToLocation(47.859476, -121.972446, 14);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_sultan:
                goToLocation(47.86034, -121.812286, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_olympia:
                goToLocation(47.021461, -122.899933, 13);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_seattle:
                goToLocation(47.5990, -122.3350, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_spokane:
                goToLocation(47.658566, -117.425995, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_tacoma:
                goToLocation(47.206275, -122.46254, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_vancouver:
                goToLocation(45.639968, -122.610512, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_wenatchee:
                goToLocation(47.435867, -120.309563, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_snoqualmiepass:
                goToLocation(47.404481, -121.4232569, 12);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_tricities:
                goToLocation(46.2503607, -119.2063781, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.goto_yakima:
                goToLocation(46.6063273, -120.4886952, 11);
                UIUtils.refreshActionBarMenu(this);
                return true;
            case R.id.map_legend:
                AlertDialog.Builder imageDialog = new AlertDialog.Builder(this, R.style.WSDOT_popup);
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

                View layout = inflater.inflate(R.layout.map_legend_layout, null);
                imageDialog.setView(layout);
                imageDialog.setPositiveButton(R.string.submit, (dialog, whichButton) -> dialog.dismiss());
                imageDialog.create();
                imageDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class AlertsTimerTask extends TimerTask {
        private Runnable runnable = new Runnable() {
            public void run() {
                mapHighwayAlertViewModel.refreshAlerts();
            }
        };

        public void run() {
            handler.post(runnable);
        }
    }

    private void refreshOverlays() {
        mProgressBar.setVisibility(View.VISIBLE);
        if (mMap != null) {
            mapCameraViewModel.refreshCameras();
            mapHighwayAlertViewModel.refreshAlerts();
        }
    }

    /*
     *  Adds or removes cameras from the cluster manager.
     *  When clustering is turned off all items are removed from the cluster manager and
     *  markers are plotted normally.
     */
    private void toggleCluster(FloatingActionButton fab) {

        if (clusterCameras) {

            clusterCameras = false;
            toggleFabOff(fab);

            if (cameras != null) {
                mClusterManager.clearItems();
                mClusterManager.cluster();
                addCameraMarkers(cameras);
            }
            //TODO:  firebase cluster off event


        } else {

            clusterCameras = true;

            toggleFabOn(fab);

            removeCameraMarkers();

            if (cameras != null && showCameras) {
                mClusterManager.addItems(cameras);
                mClusterManager.cluster();
            }
            //TODO:  firebase event
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
     * @param fab
     */
    private void toggleCameras(FloatingActionButton fab) {

        if (showCameras) {
            if (clusterCameras) {
                mClusterManager.clearItems();
                mClusterManager.cluster();
            } else {
                hideCameraMarkers();
            }

            toggleFabOff(fab);

            showCameras = false;

            //TODO:  firebase hide camera event
        } else {

            if (clusterCameras) {
                if (cameras != null) {
                    mClusterManager.addItems(cameras);
                    mClusterManager.cluster();
                }
            } else {
                showCameraMarkers();
            }

            toggleFabOn(fab);

            showCameras = true;

            //TODO:  firebase show event
        }

        // Save camera display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_CAMERAS", showCameras);
        editor.apply();
    }


    /**
     * Toggles rest area markers on/off
     * @param fab
     */
    private void toggleRestAreas(FloatingActionButton fab) {

        String label;

        if (showRestAreas) {
            toggleFabOff(fab);
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
            toggleFabOn(fab);
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

        // TODO: firebase event

        // Save rest areas display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_REST_AREAS", showRestAreas);
        editor.apply();
    }

    /**
     * Toggles alert markers on/off
     * @param fab
     */
    private void toggleAlerts(FloatingActionButton fab) {

        String label;

        if (showAlerts) {
            toggleFabOff(fab);
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
            toggleFabOn(fab);
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

        // TODO: firebase event

        // Save rest areas display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_ALERTS", showAlerts);
        editor.apply();
    }

    public void goToLocation(double latitude, double longitude, int zoomLevel) {
        if (mMap != null) {
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
        }
    }

    /**
     * Layers FAB menu logic
     */
    private void showFABMenu() {
        isFABOpen = true;

        fabLayoutCameras.setVisibility(View.VISIBLE);
        fabLayoutClusters.setVisibility(View.VISIBLE);
        fabLayoutAlerts.setVisibility(View.VISIBLE);
        fabLayoutRestareas.setVisibility(View.VISIBLE);

        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {

            fabLabelCameras.setPivotX(fabLabelCameras.getWidth());
            fabLabelCameras.setPivotY(fabLabelCameras.getHeight());
            fabLabelCameras.setRotation(40);

            fabLabelClusters.setPivotX(fabLabelClusters.getWidth());
            fabLabelClusters.setPivotY(fabLabelClusters.getHeight());
            fabLabelClusters.setRotation(40);

            fabLabelAlerts.setPivotX(fabLabelAlerts.getWidth());
            fabLabelAlerts.setPivotY(fabLabelAlerts.getHeight());
            fabLabelAlerts.setRotation(40);

            fabLabelRestareas.setPivotX(fabLabelRestareas.getWidth());
            fabLabelRestareas.setPivotY(fabLabelRestareas.getHeight());
            fabLabelRestareas.setRotation(40);

            fabLabelCameras.animate().translationY(-fabCameras.getHeight() / 2).setDuration(0);
            fabLabelClusters.animate().translationY(-fabClusters.getHeight() / 2).setDuration(0);
            fabLabelAlerts.animate().translationY(-fabAlerts.getHeight() / 2).setDuration(0);
            fabLabelRestareas.animate().translationY(-fabRestareas.getHeight() / 2).setDuration(0);

            fabLayoutCameras.animate().translationX(-getResources().getDimension(R.dimen.fab_1)).setDuration(270);
            fabLayoutClusters.animate().translationX(-getResources().getDimension(R.dimen.fab_2)).setDuration(270);
            fabLayoutAlerts.animate().translationX(-getResources().getDimension(R.dimen.fab_3)).setDuration(270);
            fabLayoutRestareas.animate().translationX(-getResources().getDimension(R.dimen.fab_4)).setDuration(270);

        } else {
            fabLayoutCameras.animate().translationY(-getResources().getDimension(R.dimen.fab_1)).setDuration(270);
            fabLayoutClusters.animate().translationY(-getResources().getDimension(R.dimen.fab_2)).setDuration(270);
            fabLayoutAlerts.animate().translationY(-getResources().getDimension(R.dimen.fab_3)).setDuration(270);
            fabLayoutRestareas.animate().translationY(-getResources().getDimension(R.dimen.fab_4)).setDuration(270);
        }
    }

    private void closeFABMenu() {

        if (isFABOpen) {

            isFABOpen = false;

            if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                fabLayoutCameras.animate().translationX(0);
                fabLayoutClusters.animate().translationX(0);
                fabLayoutAlerts.animate().translationX(0);
                fabLayoutRestareas.animate().translationX(0).setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fabLayoutCameras.setVisibility(View.GONE);
                        fabLayoutClusters.setVisibility(View.GONE);
                        fabLayoutAlerts.setVisibility(View.GONE);
                        fabLayoutRestareas.setVisibility(View.GONE);
                        fabLayoutRestareas.animate().setListener(null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

            } else {
                fabLayoutCameras.animate().translationY(0);
                fabLayoutClusters.animate().translationY(0);
                fabLayoutAlerts.animate().translationY(0);
                fabLayoutRestareas.animate().translationY(0).setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fabLayoutCameras.setVisibility(View.GONE);
                        fabLayoutClusters.setVisibility(View.GONE);
                        fabLayoutAlerts.setVisibility(View.GONE);
                        fabLayoutRestareas.setVisibility(View.GONE);
                        fabLayoutRestareas.animate().setListener(null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        }
    }

    private void toggleFabOn(FloatingActionButton fab) {
        TypedArray ta = this.getTheme().obtainStyledAttributes(R.styleable.ThemeStyles);
        fab.setBackgroundTintList(ColorStateList.valueOf(ta.getColor(R.styleable.ThemeStyles_fabButtonColor, getResources().getColor(R.color.primary_default))));
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_on));
    }

    private void toggleFabOff(FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.semi_white)));
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_off));
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

            try {
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
                Crashlytics.logException(e);
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
                Crashlytics.logException(e);
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


    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5 * 60000);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return mLocationRequest;
    }

    private void enableMyLocation() {
        if (checkPermission(true)) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    /**
     * Request location updates after checking permissions first.
     */
    private void requestLocationUpdates() {
        Log.e(TAG, "requesting location updates");
        if (checkPermission(false)) {
            if (mGoogleApiClient.isConnected()) {
                getFusedLocationProviderClient(this).requestLocationUpdates(createLocationRequest(),
                        mLocationCallback,
                        Looper.myLooper());
            }
        }
    }

    public boolean onMyLocationButtonClick() {
        if (checkPermission(true)){
            // Get last known recent location using new Google Play Services SDK (v11+)
            FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

            locationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        requestLocationUpdates();
                        if (location != null) {
                            moveToNewLocation(location);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    });
        }
        return true;
    }

    private boolean checkPermission(Boolean requestIfNeeded){
        if (ContextCompat.checkSelfPermission(TrafficMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(requestIfNeeded) {
                requestPermissions();
            }
        } else {
            return true;
        }
        return false;
    }

    private void requestPermissions() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            ActivityCompat.requestPermissions(TrafficMapActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(TrafficMapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    requestLocationUpdates();
                }
            }
        }
    }

    public void onNewLocation(Location location) {
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
            builder.setPositiveButton("I'm a Passenger", (dialog, id) -> {});
            builder.create().show();
        }
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

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }


    /**
     * Based on custom renderer demo:
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

    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("read_extras", extrasRead);
    }
}
