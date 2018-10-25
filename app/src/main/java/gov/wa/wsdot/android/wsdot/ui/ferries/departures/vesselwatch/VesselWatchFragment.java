package gov.wa.wsdot.android.wsdot.ui.ferries.departures.vesselwatch;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class VesselWatchFragment extends BaseFragment
    implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, Injectable {

    final String TAG = VesselWatchFragment.class.getSimpleName();

    private int mScheduleId = 0;

    private GoogleMap mMap;
    private Handler handler = new Handler();
    private Timer timer;

    private List<CameraItem> cameras = new ArrayList<>();
    private List<VesselWatchItem> vessels = new ArrayList<>();

    private HashMap<Marker, String> markers = new HashMap<>();

    private ProgressBar mProgressBar;
    FloatingActionButton fabCameras;

    boolean showCameras;

    private static VesselWatchViewModel vesselViewModel;
    private static MapCameraViewModel mapCameraViewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getActivity().getIntent().getExtras();

        mScheduleId = args.getInt("scheduleId");

        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_vessel_watch, null);

        mProgressBar = rootView.findViewById(R.id.progress_bar);

        fabCameras = rootView.findViewById(R.id.fab);

        if (showCameras){
            fabCameras.setImageResource(R.drawable.ic_menu_traffic_cam);
        } else {
            fabCameras.setImageResource(R.drawable.ic_menu_traffic_cam_off);
        }

        fabCameras.setOnClickListener(view -> {
            toggleCameras(fabCameras);
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

        LatLng routeLatLng = getRouteLocation(mScheduleId);
        int zoom = getRouteZoom(mScheduleId);

        mMap.setOnCameraMoveListener(() -> {
            if (mMap != null) {
              mapCameraViewModel.setMapBounds(mMap.getProjection().getVisibleRegion().latLngBounds);
            }
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
            Log.e(TAG, "got reports");



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

        if (markers.get(marker).equalsIgnoreCase("vessel")) {
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
        }

        return true;
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
            fab.setImageResource(R.drawable.ic_menu_traffic_cam_off);
            showCameras = false;
            ((BaseActivity)getActivity()).setFirebaseAnalyticsEvent("ui_action", "type", "vessel_cameras_off");
        } else {
            showCameraMarkers();
            fab.setImageResource(R.drawable.ic_menu_traffic_cam);
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
}