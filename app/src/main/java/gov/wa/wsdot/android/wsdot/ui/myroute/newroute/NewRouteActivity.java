package gov.wa.wsdot.android.wsdot.ui.myroute.newroute;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.service.CamerasSyncService;
import gov.wa.wsdot.android.wsdot.service.FerriesSchedulesSyncService;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
import gov.wa.wsdot.android.wsdot.service.MyRouteTrackingService;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.ui.myroute.FindFavoritesOnRouteActivity;
import gov.wa.wsdot.android.wsdot.util.ProgressDialogFragment;

import static android.view.View.GONE;
import static gov.wa.wsdot.android.wsdot.util.ParserUtils.convertLocationsToJson;

public class NewRouteActivity extends FindFavoritesOnRouteActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback,
        TrackingRouteDialogFragment.TrackingRouteDialogListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        MyRouteTrackingService.Callbacks {

    private final String TAG = "NewRouteActivity";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private MountainPassesSyncReceiver mMountainPassesSyncReceiver;
    private FerriesSchedulesSyncReceiver mFerriesSchedulesSyncReceiver;
    private TravelTimesSyncReceiver mTravelTimesSyncReceiver;
    private CamerasSyncReceiver mCamerasSyncReceiver;

    private String DEFAULT_ROUTE_NAME = "My Route";

    private final int REQUEST_ACCESS_FINE_LOCATION = 100;

    private GoogleMap mMap;

    private boolean mIsBound = false;

    private MyRouteTrackingService mBoundService;

    private ProgressDialogFragment progressDialog;
    private int runningTasks = 0;

    private GoogleApiClient mGoogleApiClient;

    private final String TRACKING_DIALOG_FRAGMENT_TAG = "tracking_dialog";

    private List<LatLng> myRouteLocations = new ArrayList<>();

    private Boolean rebinding = false;

    private NewRouteViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            Log.e(TAG, "onServiceConneted");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyRouteTrackingService.LocalBinder binder = (MyRouteTrackingService.LocalBinder) service;
            mBoundService = binder.getService();
            mIsBound = true;

            mBoundService.registerClient(NewRouteActivity.this);

            if (rebinding){
                onFinishTrackingDialog();
                rebinding = false;
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Log.e(TAG, "ServiceDisconnected");
            mBoundService = null;
            mIsBound = false;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(getApplicationContext(), MyRouteTrackingService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_route);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        initStartButton();

        initDiscardButton();

        initSaveButton();

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NewRouteViewModel.class);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onResume(){
        super.onResume();
        checkLocationPermissionError();
        checkGoogleServiceConnectError();
        mGoogleApiClient.connect();

        // Ferries Route Schedules
        IntentFilter ferriesSchedulesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.FERRIES_SCHEDULES_RESPONSE");
        ferriesSchedulesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mFerriesSchedulesSyncReceiver = new FerriesSchedulesSyncReceiver();
        registerReceiver(mFerriesSchedulesSyncReceiver, ferriesSchedulesFilter);

        // Mountain Passes
        IntentFilter mountainPassesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
        mountainPassesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mMountainPassesSyncReceiver = new MountainPassesSyncReceiver();
        registerReceiver(mMountainPassesSyncReceiver, mountainPassesFilter);

        // Travel Times
        IntentFilter travelTimesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.TRAVEL_TIMES_RESPONSE");
        travelTimesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mTravelTimesSyncReceiver = new TravelTimesSyncReceiver();
        registerReceiver(mTravelTimesSyncReceiver, travelTimesFilter);

        // Cameras
        IntentFilter camerasFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasSyncReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasSyncReceiver, camerasFilter);

    }

    @Override
    public void onPause(){
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        unregisterReceiver(mFerriesSchedulesSyncReceiver);
        unregisterReceiver(mMountainPassesSyncReceiver);
        unregisterReceiver(mTravelTimesSyncReceiver);
        unregisterReceiver(mCamerasSyncReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddFavoritesDialog() {

        new AlertDialog.Builder(NewRouteActivity.this, R.style.AppCompatAlertDialogStyle)
                .setTitle("Add Favorites?")
                .setMessage("Traffic cameras, travel times, pass reports, and other content will " +
                        "be added to your favorites if they are on this route. " +
                        "\n\n You can do this later by tapping the settings button next to your route.")

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        progressDialog = new ProgressDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("message", "Finding Favorites...");
                        progressDialog.setArguments(args);
                        progressDialog.show(getSupportFragmentManager(), "progress_dialog");

                        runningTasks = MAX_NUM_TASKS;

                        Intent mCamerasIntent = new Intent(NewRouteActivity.this, CamerasSyncService.class);
                        startService(mCamerasIntent);

                        Intent mTravelTimesIntent = new Intent(NewRouteActivity.this, TravelTimesSyncService.class);
                        startService(mTravelTimesIntent);

                        Intent mFerriesSchedulesIntent = new Intent(NewRouteActivity.this, FerriesSchedulesSyncService.class);
                        startService(mFerriesSchedulesIntent);

                        Intent mMountainPassesIntent = new Intent(NewRouteActivity.this, MountainPassesSyncService.class);
                        startService(mMountainPassesIntent);
                    }
                })

                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showStartView();
                        mMap.clear();
                        moveToCurrentLocation();
                        Snackbar.make(findViewById(android.R.id.content), "Route Successfully Saved", Snackbar.LENGTH_LONG)
                                .show();
                    }
                })
                .show();
    }


    private void initStartButton(){
        Button startButton = findViewById(R.id.start_button);

        startButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                myRouteLocations.clear();

                startService(new Intent(NewRouteActivity.this, MyRouteTrackingService.class));
                doBindService();

                showTrackingDialog();

            } else {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){

                    new AlertDialog.Builder(NewRouteActivity.this, R.style.AppCompatAlertDialogStyle)
                            .setTitle("No Location Permission")
                            .setMessage("You must grant WSDOT permission to use this feature.")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                            })
                            .setIcon(R.drawable.ic_menu_mylocation)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .show();
                } else {
                    requestLocationPermission();
                }
            }
        });
    }

    private void initDiscardButton(){
        Button discardButton = (Button) findViewById(R.id.discard_button);

        discardButton.setOnClickListener(v -> {
            showStartView();
            mMap.clear();
            myRouteLocations.clear();
            moveToCurrentLocation();
        });
    }

    private void initSaveButton(){
        Button saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(NewRouteActivity.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Save Route");

            // Set up the input
            final EditText input = new EditText(NewRouteActivity.this);

            Drawable drawable = input.getBackground(); // get current EditText drawable
            drawable.setColorFilter(ContextCompat.getColor(NewRouteActivity.this, R.color.primary), PorterDuff.Mode.SRC_ATOP); // change the drawable color

            if(Build.VERSION.SDK_INT > 16) {
                input.setBackground(drawable); // set the new drawable to EditText
            }else{
                input.setBackgroundDrawable(drawable); // use setBackground Drawable because setBackground required API 16
            }

            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setCancelable(false);
            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String routeName = DEFAULT_ROUTE_NAME;

                    if (!input.getText().toString().trim().equals("")) {
                        routeName = input.getText().toString();
                    }

                    dialog.dismiss();

                    JSONArray json = convertLocationsToJson(myRouteLocations);

                    Long id = (new Date().getTime()/1000);

                    MyRouteEntity myRoute = new MyRouteEntity();

                    myRoute.setMyRouteId(id);
                    myRoute.setTitle(routeName);

                    myRoute.setLatitude(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude);
                    myRoute.setLongitude(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude);

                    myRoute.setZoom((int) mMap.getCameraPosition().zoom);
                    myRoute.setRouteLocations(json.toString());
                    myRoute.setIsStarred(1);

                    viewModel.addMyRoute(myRoute);

                    showAddFavoritesDialog();

                }
            });
            builder.show();
        });
    }

    private void showTrackingDialog() {
        FragmentManager fm = getSupportFragmentManager();
        TrackingRouteDialogFragment trackingRouteDialogFragment = TrackingRouteDialogFragment.newInstance("Tracking Route");
        trackingRouteDialogFragment.setCancelable(false);
        trackingRouteDialogFragment.show(fm, TRACKING_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onFinishTrackingDialog() {
        mMap.clear();
        if (mIsBound) {
            myRouteLocations = mBoundService.getRouteLocations();

            doUnbindService();
            mBoundService.stopSelf();

            if (myRouteLocations.size() > 1) {
                drawRouteOnMap();
                showConfirmRouteView();
            } else {
                Toast.makeText(this, "Not enough location data to make a route.", Toast.LENGTH_LONG).show();
            }

        } else {
            // Need to rebind
            rebinding = true;
            doBindService();
        }
    }

    private void showConfirmRouteView(){
        findViewById(R.id.start_recording_view).setVisibility(GONE);
        findViewById(R.id.done_recording_view).setVisibility(View.VISIBLE);

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        if (ContextCompat.checkSelfPermission(NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
            mMap.setMyLocationEnabled(false);
        }
    }

    private void showStartView(){
        findViewById(R.id.start_recording_view).setVisibility(View.VISIBLE);
        findViewById(R.id.done_recording_view).setVisibility(View.GONE);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ContextCompat.checkSelfPermission(NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
            mMap.setMyLocationEnabled(true);
            moveToCurrentLocation();
        }
    }

    private void drawRouteOnMap() {

        PolylineOptions polylineOptions = new PolylineOptions().width(20).color(Color.BLUE).addAll(myRouteLocations);
        Polyline polyLine = mMap.addPolyline(polylineOptions);
        polyLine.setPoints(myRouteLocations);

        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng location : myRouteLocations) {
            b.include(location);
        }

        LatLngBounds bounds = b.build();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(false);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(false);

        LatLng latLng = new LatLng(47.5990, -122.3350);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

        requestLocationPermission();
    }

    /**
     * Request location updates after checking permissions first.
     */
    private void requestLocationPermission() {

        if (ContextCompat.checkSelfPermission(NewRouteActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show explanation to user explaining why we need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setMessage("To create a route you must allow WSDOT access to your location.");
                builder.setTitle("Location Services");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(
                                NewRouteActivity.this,
                                new String[] {
                                        Manifest.permission.ACCESS_FINE_LOCATION },
                                REQUEST_ACCESS_FINE_LOCATION);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.setCancelable(false);
                builder.show();

            } else {
                // No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(NewRouteActivity.this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION },
                        REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            mMap.setMyLocationEnabled(true);
            moveToCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                    moveToCurrentLocation();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void trackingError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (getSupportFragmentManager().findFragmentByTag(TRACKING_DIALOG_FRAGMENT_TAG) != null){
            ((TrackingRouteDialogFragment) getSupportFragmentManager().findFragmentByTag(TRACKING_DIALOG_FRAGMENT_TAG)).dismiss();
        }
        doUnbindService();
    }

    private void checkGoogleServiceConnectError(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(MyRouteTrackingService.API_CONNECTION_ERROR_KEY, false)) {
            trackingError("API connection error.");
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MyRouteTrackingService.API_CONNECTION_ERROR_KEY, false);
            editor.apply();
        }
    }

    private void checkLocationPermissionError() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(MyRouteTrackingService.PERMISSION_ERROR_KEY, false)) {
            trackingError("Permissions revoked. Tracking disabled.");
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MyRouteTrackingService.PERMISSION_ERROR_KEY, false);
            editor.apply();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
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

    private void moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if (location != null) {
                Log.d(TAG, location.toString());
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
                LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            }
        }
    }

    @Override
    protected void taskComplete(){
        runningTasks--;
        if (runningTasks == 0){
            progressDialog.dismiss();
            showStartView();
            mMap.clear();
            moveToCurrentLocation();
            Snackbar.make(findViewById(android.R.id.content), "Route Successfully Saved", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected List<LatLng> getRoute() {
        return this.myRouteLocations;
    }

}
