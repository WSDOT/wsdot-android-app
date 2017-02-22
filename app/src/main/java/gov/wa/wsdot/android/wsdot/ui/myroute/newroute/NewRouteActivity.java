package gov.wa.wsdot.android.wsdot.ui.myroute.newroute;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;

import static android.view.View.GONE;
import static gov.wa.wsdot.android.wsdot.util.ParserUtils.convertLocationsToJson;

public class NewRouteActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener, ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback,
        TrackingRouteFragment.TrackingRouteDialogListener {

    private final String TAG = "NewRouteActivity";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;

    private Button startButton;
    private Button discardButton;
    private Button saveButton;

    private String routeName = "My Route";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_ACCESS_FINE_LOCATION = 100;

    private List<LatLng> myRouteLocations = new ArrayList<>();
    private Boolean tracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startButton = (Button) findViewById(R.id.start_button);

        startButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                    Log.e(TAG, "You gave us permission!! - Tracking location");
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, NewRouteActivity.this);
                    myRouteLocations.clear();
                    tracking = true;
                    requestLocationUpdates();

                    showTrackingDialog();

                } else {
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){

                        new AlertDialog.Builder(NewRouteActivity.this, R.style.AppCompatAlertDialogStyle)
                                .setTitle("No Location Permission")
                                .setMessage("You must grant WSDOT permission to use this feature.")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                    }
                                })
                                .setIcon(R.drawable.ic_menu_mylocation)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .show();
                    }
                    requestLocationUpdates();

                }
            }
        });

        discardButton = (Button) findViewById(R.id.discard_button);

        discardButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                showStartView();
                mMap.clear();
                myRouteLocations.clear();
                onMyLocationButtonClick();
            }
        });

        saveButton = (Button) findViewById(R.id.save_button);

        saveButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(final View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(NewRouteActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Save Route");

                // Set up the input
                final EditText input = new EditText(NewRouteActivity.this);

                Drawable drawable = input.getBackground(); // get current EditText drawable
                drawable.setColorFilter(ContextCompat.getColor(NewRouteActivity.this, R.color.primary), PorterDuff.Mode.SRC_ATOP); // change the drawable color

                if(Build.VERSION.SDK_INT > 16) {
                    input.setBackground(drawable); // set the new drawable to EditText
                }else{
                    input.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
                }

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().trim().equals("")) {
                            routeName = input.getText().toString();
                        }

                        dialog.dismiss();

                        ContentValues values = new ContentValues();

                        Log.e(TAG, String.valueOf(myRouteLocations.size()));

                        JSONArray json = convertLocationsToJson(myRouteLocations);

                        Log.e(TAG, json.toString());

                        String id = String.valueOf(new Date().getTime()/1000);

                        values.put(WSDOTContract.MyRoute.MY_ROUTE_ID, id);
                        values.put(WSDOTContract.MyRoute.MY_ROUTE_TITLE, routeName);
                        values.put(WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_LAT, String.valueOf(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude));
                        values.put(WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_LONG, String.valueOf(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude));
                        values.put(WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_ZOOM, (int) mMap.getCameraPosition().zoom);
                        values.put(WSDOTContract.MyRoute.MY_ROUTE_LOCATIONS, json.toString());
                        values.put(WSDOTContract.MyRoute.MY_ROUTE_FOUND_FAVORITES, 0);
                        values.put(WSDOTContract.MyRoute.MY_ROUTE_IS_STARRED, 1);

                        getContentResolver().insert(WSDOTContract.MyRoute.CONTENT_URI, values);

                        //TODO: Add favorites?
                        //showAddFavoritesDialog(id);

                        showStartView();
                        if (mGoogleApiClient.isConnected()) {
                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, NewRouteActivity.this);
                        }
                        mMap.clear();
                        myRouteLocations.clear();
                        onMyLocationButtonClick();

                        Snackbar.make(findViewById(android.R.id.content), "Route Successfully Saved", Snackbar.LENGTH_LONG)
                                .show();

                    }
                });
                builder.show();
            }
        });

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationRequest = LocationRequest.create()
                .setInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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


    private void showAddFavoritesDialog(String id){



        ContentValues values = new ContentValues();
        values.put(WSDOTContract.MyRoute.MY_ROUTE_FOUND_FAVORITES,  1);

        this.getContentResolver().update(
                WSDOTContract.MyRoute.CONTENT_URI,
                values,
                WSDOTContract.MyRoute._ID + "=?",
                new String[] {id}
        );







    }


    private void showTrackingDialog() {
        FragmentManager fm = getSupportFragmentManager();
        TrackingRouteFragment  trackingRouteFragment = TrackingRouteFragment.newInstance("Tracking Route");
        trackingRouteFragment.setCancelable(false);
        trackingRouteFragment.show(fm, "fragment_edit_name");
    }

    @Override
    public void onFinishTrackingDialog() {
        mMap.clear();
        tracking = false;

        if (myRouteLocations.size() > 1) {
            drawRouteOnMap();
            showConfirmRouteView();
        } else {
            Toast.makeText(this, "Not enough location data to make a route.", Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "collected " + myRouteLocations.size() + " lat/lng points");
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
        mMap.setOnMyLocationButtonClickListener(this);

        LatLng latLng = new LatLng(47.5990, -122.3350);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

        enableMyLocation();
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

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
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

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (location != null) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
                LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                mMap.moveCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub

        Log.e(TAG, "Location changed!");

        if (tracking) {
            myRouteLocations.add(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
        }


    }

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    /**
     * @param location
     */
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        Log.e(TAG, "New location!");

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
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 || permissions.length > 0) { // Check if request was canceled.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Log.i(TAG, "Request permissions granted!!!");
                    mMap.setMyLocationEnabled(true);
                    Location location = LocationServices.FusedLocationApi
                            .getLastLocation(mGoogleApiClient);

                    if (location != null) {
                        double currentLatitude = location.getLatitude();
                        double currentLongitude = location.getLongitude();
                        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                        mMap.moveCamera(cameraUpdate);
                    }
                } else {
                    // Permission was denied or request was cancelled
                    Log.i(TAG, "Request permissions denied...");
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
