package gov.wa.wsdot.android.wsdot.ui.myroute.newroute;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.service.MyRouteTrackingService;

import static android.view.View.GONE;
import static gov.wa.wsdot.android.wsdot.util.ParserUtils.convertLocationsToJson;

public class NewRouteActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback,
        TrackingRouteDialogFragment.TrackingRouteDialogListener,
        MyRouteTrackingService.Callbacks{

    private final String TAG = "NewRouteActivity";

    private GoogleMap mMap;

    private boolean mIsBound = false;

    private MyRouteTrackingService mBoundService;

    private Button startButton;
    private Button discardButton;
    private Button saveButton;

    private final String TRACKING_DIALOG_FRAGMENT_TAG = "tracking_dialog";
    private String routeName = "My Route";

    private final int REQUEST_ACCESS_FINE_LOCATION = 100;

    private List<LatLng> myRouteLocations = new ArrayList<>();
    private Boolean tracking = false;
    private Boolean rebinding = false;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startButton = (Button) findViewById(R.id.start_button);

        startButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                    Log.e(TAG, "You gave us permission!! - Tracking location");
                    myRouteLocations.clear();
                    tracking = true;

                    startService(new Intent(NewRouteActivity.this, MyRouteTrackingService.class));
                    doBindService();

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
                    } else {
                        requestLocationPermission();
                    }
                }
            }
        });

        discardButton = (Button) findViewById(R.id.discard_button);

        discardButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                showStartView();
                mMap.clear();
                myRouteLocations.clear();
                //TODO onMyLocationButtonClick();
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

                        mMap.clear();
                        myRouteLocations.clear();
                        //TODO onMyLocationButtonClick();

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


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        checkLocationPermissionError();
        checkGoogleServiceConnectError();
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

    // TODO
    private void showAddFavoritesDialog(String id) {





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

        TrackingRouteDialogFragment trackingRouteDialogFragment = TrackingRouteDialogFragment.newInstance("Tracking Route");
        trackingRouteDialogFragment.setCancelable(false);
        trackingRouteDialogFragment.show(fm, TRACKING_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onFinishTrackingDialog() {
        mMap.clear();
        tracking = false;

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
            Log.e(TAG, "collected " + myRouteLocations.size() + " lat/lng points");

        } else {
            // Need to rebind
            Log.e(TAG, "rebinding");
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(NewRouteActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "Request permissions granted!!!");
                        mMap.setMyLocationEnabled(true);
                    }
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void trackingError(String message) {
        Log.e(TAG, "Oops!");
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
}