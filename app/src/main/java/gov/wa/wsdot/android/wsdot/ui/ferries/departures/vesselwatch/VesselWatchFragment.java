package gov.wa.wsdot.android.wsdot.ui.ferries.departures.vesselwatch;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.ImageViewCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.shared.WeatherItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.Utils;

public class VesselWatchFragment extends BaseFragment
    implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback,
        ClusterManager.OnClusterItemClickListener<WeatherItem>,
        ClusterManager.OnClusterClickListener<WeatherItem>,
        Injectable {

    final String TAG = VesselWatchFragment.class.getSimpleName();

    private int mScheduleId = 0;

    private GoogleMap mMap;
    private Handler handler = new Handler();
    private Timer timer;

    private List<CameraItem> cameras = new ArrayList<>();
    private List<VesselWatchItem> vessels = new ArrayList<>();
    private List<WeatherItem> weatherReports = new ArrayList<>();

    private HashMap<Marker, String> markers = new HashMap<>();

    private ProgressBar mProgressBar;

    FloatingActionButton fabCameras;
    FloatingActionButton fabWind;

    boolean showCameras;
    boolean showWeather;

    private static VesselWatchViewModel vesselViewModel;
    private static MapCameraViewModel mapCameraViewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ClusterManager<WeatherItem> mClusterManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getActivity().getIntent().getExtras();

        mScheduleId = args.getInt("scheduleId");

        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true);
        showWeather = settings.getBoolean("KEY_SHOW_WEATHER", false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_vessel_watch, null);

        mProgressBar = rootView.findViewById(R.id.progress_bar);

        fabCameras = rootView.findViewById(R.id.camera_fab);

        fabCameras.setImageResource(R.drawable.ic_menu_traffic_cam);

        if (showCameras) {
            toggleFabOn(fabCameras);
        } else {
            toggleFabOff(fabCameras);
        }

        fabCameras.setOnClickListener(view -> {
            toggleCameras(fabCameras);
        });

        fabWind = rootView.findViewById(R.id.wind_fab);

        if (showWeather){
            toggleFabOn(fabWind);
        } else {
            toggleFabOff(fabWind);
        }

        fabWind.setOnClickListener(view -> {
            toggleWeather(fabWind);
        });

        mapCameraViewModel = ViewModelProviders.of(this, viewModelFactory).get(MapCameraViewModel.class);
        mapCameraViewModel.init("ferries");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);

        MyLogger.crashlyticsLog("Ferries", "Screen View", "VesselWatchMapActivity", 1);

        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setTrafficEnabled(true);
        mMap.setOnMarkerClickListener(this);

        if (getContext() != null) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
            }
        } else {
            mMap.setMyLocationEnabled(false);
        }

        setUpClusterer();

        LatLng routeLatLng = getRouteLocation(mScheduleId);
        int zoom = getRouteZoom(mScheduleId);

        mMap.setOnCameraMoveListener(() -> {
            if (mMap != null) {
              mapCameraViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);
            }
        });

        mMap.setOnCameraIdleListener(() -> {
            mClusterManager.onCameraIdle();
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routeLatLng, zoom));
        vesselViewModel = ViewModelProviders.of(this, viewModelFactory).get(VesselWatchViewModel.class);

        vesselViewModel.getVesselResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mProgressBar.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this.getActivity(), "connection error, failed to load vessels", Toast.LENGTH_SHORT).show();
                }
            }
        });

        vesselViewModel.getVessels().observe(this, vesselItems -> {
            if (vesselItems != null){

                Iterator<Map.Entry<Marker, String>> iter = markers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Marker, String> entry = iter.next();
                    if (entry.getValue().equalsIgnoreCase("vessel")) {
                        entry.getKey().remove();
                        iter.remove();
                    }
                }
                vessels.clear();
                vessels = vesselItems;

                if (vessels.size() != 0) {
                    for (int i = 0; i < vessels.size(); i++) {
                        LatLng latLng = new LatLng(vessels.get(i).getLat(), vessels.get(i).getLon());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(vessels.get(i).getName())
                                .snippet(vessels.get(i).getDescription())
                                .icon(BitmapDescriptorFactory.fromResource(vessels.get(i).getIcon()))
                                .visible(true));

                        markers.put(marker, "vessel");
                    }
                }
            }
        });

        vesselViewModel.getWeatherReports().observe(this, weatherItems -> {

            if (weatherItems != null){
                weatherReports.clear();
                weatherReports = weatherItems;
                mClusterManager.clearItems();
                if (showWeather) {
                    mClusterManager.addItems(weatherReports);
                    mClusterManager.cluster();
                }
            }
        });

        vesselViewModel.refreshVessels();
        vesselViewModel.refreshWeatherReports();

        mapCameraViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mProgressBar.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(this.getActivity(), "connection error, failed to load cameras", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mapCameraViewModel.getDisplayCameras().observe(this, cameraItems -> {
            if (cameraItems != null) {

                // use tempMarkers to keep icons from disappearing when we quickly remove the old and add the new
                HashMap<Marker, String> tempMarkers = new HashMap<>();

                cameras.clear();
                cameras = cameraItems;

                if (cameras.size() != 0) {
                    for (int i = 0; i < cameras.size(); i++) {
                        LatLng latLng = new LatLng(cameras.get(i).getLatitude(), cameras.get(i).getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(cameras.get(i).getTitle())
                                .snippet(cameras.get(i).getCameraId().toString())
                                .icon(BitmapDescriptorFactory.fromResource(cameras.get(i).getCameraIcon()))
                                .visible(showCameras));

                        tempMarkers.put(marker, "camera");
                    }

                }

                Iterator<Map.Entry<Marker, String>> iter = markers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Marker, String> entry = iter.next();
                    if (entry.getValue().equalsIgnoreCase("camera")) {
                        entry.getKey().remove();
                        iter.remove();
                    }
                }

                markers.putAll(tempMarkers);

            }
        });

        mapCameraViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);

        timer = new Timer();
        timer.schedule(new VesselWatchFragment.VesselsTimerTask(), 0, 30000); // Schedule vessels to update every 30 seconds

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle b = new Bundle();
        Intent intent = new Intent();

        if (markers.get(marker) == null) { // Not in our markers, must be cluster icon
            mClusterManager.onMarkerClick(marker);
        } else if (markers.get(marker).equalsIgnoreCase("vessel")) {
            MyLogger.crashlyticsLog("Ferries", "Tap", "Vessel " + marker.getTitle(), 1);
            intent.setClass(getActivity(), VesselWatchDetailsActivity.class);
            b.putString("title", marker.getTitle());
            b.putString("description", marker.getSnippet());
            intent.putExtras(b);
            this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("camera")) {
            MyLogger.crashlyticsLog("Ferries", "Tap", "Camera " + marker.getSnippet(), 1);

            intent.setClass(getActivity(), CameraActivity.class);
            b.putInt("id", Integer.parseInt(marker.getSnippet()));
            intent.putExtras(b);
            this.startActivity(intent);
        } else if (markers.get(marker).equalsIgnoreCase("weather")) {
            MyLogger.crashlyticsLog("Ferries", "Tap", "Weather " + marker.getTitle(), 1);

            intent.setClass(getActivity(), VesselWatchDetailsActivity.class);
            b.putString("title", marker.getTitle());
            b.putString("description", marker.getSnippet());
            intent.putExtras(b);
            this.startActivity(intent);

        }

        return true;
    }

    private void toggleFabOn(FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.accent_default)));
    }

    private void toggleFabOff(FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.semi_white)));
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
            hideCameraMarkers();
            toggleFabOff(fab);
            showCameras = false;
            ((BaseActivity)getActivity()).setFirebaseAnalyticsEvent("ui_action", "type", "vessel_cameras_off");
        } else {
            showCameraMarkers();
            toggleFabOn(fab);
            showCameras = true;
            ((BaseActivity)getActivity()).setFirebaseAnalyticsEvent("ui_action", "type", "vessel_cameras_on");
        }

        // Save camera display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_CAMERAS", showCameras);
        editor.commit();
    }


    /**
     * sets all camera marker visibility to false.
     */
    private void hideCameraMarkers(){
        for (Map.Entry<Marker, String> entry : markers.entrySet()) {
            Marker key = entry.getKey();
            String value = entry.getValue();
            if (value.equalsIgnoreCase("camera")) {
                key.setVisible(false);
            }
        }
    }

    /**
     * sets all camera marker visibility to true.
     */
    private void showCameraMarkers(){
        for (Map.Entry<Marker, String> entry : markers.entrySet()) {
            Marker key = entry.getKey();
            String value = entry.getValue();
            if (value.equalsIgnoreCase("camera")) {
                key.setVisible(true);
            }
        }
    }

    /**
     * Toggle wind  & weather icon visibility
     *
     * @param fab
     */
    private void toggleWeather(FloatingActionButton fab) {

        if (showWeather) {
            toggleFabOff(fab);
            mClusterManager.clearItems();
            mClusterManager.cluster();
            showWeather = false;
            ((BaseActivity)getActivity()).setFirebaseAnalyticsEvent("ui_action", "type", "vessel_weather_off");
        } else {
            toggleFabOn(fab);
            if (weatherReports != null) {
                mClusterManager.addItems(weatherReports);
                mClusterManager.cluster();
            }
            showWeather = true;
            ((BaseActivity)getActivity()).setFirebaseAnalyticsEvent("ui_action", "type", "vessel_weather_on");
        }

        // Save camera display preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SHOW_WEATHER", showWeather);
        editor.commit();
    }

    public class VesselsTimerTask extends TimerTask {
        private Runnable runnable = new Runnable() {
            public void run() {
                vesselViewModel.refreshVessels();
            }
        };

        public void run() {
            handler.post(runnable);
        }
    }

    private LatLng getRouteLocation(int scheduleId) {
        switch (scheduleId) {
            case 272: // Ana-SJ
                return new LatLng(48.550921, -122.840836);
            case 9: // Ana-SJ
                return new LatLng(48.550921, -122.840836);
            case 10: // Ana-Sid
                return new LatLng(48.550921, -122.840836);
            case 6: // Ed-King
                return new LatLng(47.803096, -122.438718);
            case 13: // F-S
                return new LatLng(47.513625, -122.450820);
            case 14: // F-V
                return new LatLng(47.513625, -122.450820);
            case 7: // Muk-Cl
                return new LatLng(47.963857, -122.327721);
            case 8: // Pt-Key
                return new LatLng(48.135562, -122.714449);
            case 1: // Pd-Tal
                return new LatLng(47.319040, -122.510890);
            case 5: // Sea-Bi
                return new LatLng(47.600325, -122.437249);
            case 3: // Sea-Br
                return new LatLng(47.565125, -122.480508);
            case 15: // S-V
                return new LatLng(47.513625, -122.450820);
            default:
                return new LatLng(47.565125, -122.480508);
        }
    }

    private int getRouteZoom(int scheduleId){
        switch (scheduleId) {
            case 272: // Ana-SJ
                return 10;
            case 9: // Ana-SJ
                return 10;
            case 10: // Ana-Sid
                return 10;
            case 6: // Ed-King
                return 12;
            case 13: // F-S
                return 12;
            case 14: // F-V
                return 12;
            case 7: // Muk-Cl
                return 13;
            case 8: // Pt-Key
                return 12;
            case 1: // Pd-Tal
                return 13;
            case 5: // Sea-Bi
                return 11;
            case 3: // Sea-Br
                return 10;
            case 15: // S-V
                return 12;
            default:
                return 11;
        }
    }

    // Weather Report Cluster Logic

    /**
     * Arbitrarily assumes items in same space will have no more than 20 images. This also helps performance.
     *
     * @param cluster
     * @return
     */
    private boolean isWeatherGroup(Cluster<WeatherItem> cluster){
        WeatherItem firstItem = (WeatherItem) cluster.getItems().toArray()[0];
        for (WeatherItem item: cluster.getItems()) {

            if (Utils.getDistanceFromPoints(firstItem.getLatitude(), firstItem.getLongitude(), item.getLatitude(), item.getLongitude()) > 5){
                return false;
            }
        }
        return true;

    }

    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(VesselWatchFragment.this.getContext(), mMap);
        mClusterManager.setRenderer(new WeatherRenderer(VesselWatchFragment.this.getContext()));

        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterClickListener(this);

        mClusterManager.cluster();
    }

    @Override
    public boolean onClusterItemClick(WeatherItem weatherItem) {
        MyLogger.crashlyticsLog("Ferries", "Tap", "Weather " + weatherItem.getSource(), 1);

        Bundle b = new Bundle();
        Intent intent = new Intent();
        intent.setClass(getActivity(), VesselWatchDetailsActivity.class);
        b.putString("title", weatherItem.getSource());
        b.putString("description", weatherItem.getReport());
        intent.putExtras(b);
        this.startActivity(intent);


        return false;
    }

    /*
        If it's a group open the most recent recorded report
     */
    @Override
    public boolean onClusterClick(Cluster<WeatherItem> cluster) {
        if (isWeatherGroup(cluster)) {

            WeatherItem newestItem = null;

            for (WeatherItem item: cluster.getItems()) {
                if (newestItem == null) {
                    newestItem = item;
                } else {
                    if (newestItem.getUpdated().compareToIgnoreCase(item.getUpdated()) < 0){
                        newestItem = item;
                    }
                }
            }

            Bundle b = new Bundle();
            Intent intent = new Intent();
            intent.setClass(getActivity(), VesselWatchDetailsActivity.class);
            b.putString("title", newestItem.getSource());
            b.putString("description", newestItem.getReport());
            intent.putExtras(b);
            this.startActivity(intent);

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
     * Based on custom renderer demo:
     * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/clustering/view/DefaultClusterRenderer.java
     */
    private class WeatherRenderer extends DefaultClusterRenderer<WeatherItem> {
        private final IconGenerator mClusterIconGenerator;
        private final float mDensity;
        private final Bitmap singleCameraIcon = BitmapFactory.decodeResource(getResources(), R.drawable.weather_flag_blue);
        private final Bitmap openCameraGroupIcon = BitmapFactory.decodeResource(getResources(), R.drawable.weather_flag_blue);
        private SparseArray<BitmapDescriptor> mIcons = new SparseArray<>();

        private WeatherRenderer(Context context) {
            super(context, mMap, mClusterManager);
            mDensity = context.getResources().getDisplayMetrics().density;
            mClusterIconGenerator = new IconGenerator(context);
            mClusterIconGenerator.setContentView(makeSquareTextView(context));

        }

        private SquareTextView makeSquareTextView(Context context) {
            SquareTextView squareTextView = new SquareTextView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            squareTextView.setLayoutParams(layoutParams);
            int twelveDpi = (int) (6 * mDensity);
            squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
            return squareTextView;
        }

        private LayerDrawable makeClusterBackground(Drawable backgroundImage) {
            return new LayerDrawable(new Drawable[]{backgroundImage});
        }

        private Drawable getBackgroundImage(int bucket){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.weather_flag_blue, null);
        }

        @Override
        protected void onBeforeClusterItemRendered(WeatherItem item, MarkerOptions markerOptions) {
            // Draw a single camera
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(singleCameraIcon));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<WeatherItem> cluster, MarkerOptions markerOptions) {

            int bucket = getBucket(cluster);

            if (isWeatherGroup(cluster)) {
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
}