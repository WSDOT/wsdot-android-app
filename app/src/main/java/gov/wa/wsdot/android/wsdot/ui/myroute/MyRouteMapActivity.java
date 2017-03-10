package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

public class MyRouteMapActivity extends BaseActivity implements OnMapReadyCallback {

    final private String[] projection = {
            WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_LAT,
            WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_LONG,
            WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_ZOOM,
            WSDOTContract.MyRoute.MY_ROUTE_LOCATIONS,
    };

    private GoogleMap mMap;
    private String route_name;
    private Long route_id;

    private JSONArray routeJSON = new JSONArray();
    private Double displayLat = 0.0;
    private Double displayLong = 0.0;
    private int displayZoom = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_route_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle args = getIntent().getExtras();

        route_name = args.getString("route_name");
        route_id = args.getLong("route_id");

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setTitle(route_name);

        setSupportActionBar(mToolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fetchRouteData(route_id);
        drawRouteOnMap(getRouteArrayList(routeJSON));
        LatLng latLng = new LatLng(displayLat, displayLong);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, displayZoom));
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

    /**
     * converts a JSONArray of location data (ex. [{"latitude":0.0, "longitude":0.0}, ...]) into an ArrayList<LatLng>
     * @param jsonLocations
     */
    private ArrayList<LatLng> getRouteArrayList(JSONArray jsonLocations){
        ArrayList<LatLng> myRouteLocations = new ArrayList<>();
        try {
            for (int i = 0; i < jsonLocations.length(); i++){
                myRouteLocations.add(new LatLng(jsonLocations.getJSONObject(i).getDouble("latitude"), jsonLocations.getJSONObject(i).getDouble("longitude")));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return myRouteLocations;
    }

    /**
     * Sets global vars used for displaying users route on map.
     * vars set: routeJSON, displayLat, displayLong, displayZoom.
     * @param route_id
     */
    private void fetchRouteData(long route_id) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;

        try {
            cursor = resolver.query(WSDOTContract.MyRoute.CONTENT_URI,
                    projection,
                    WSDOTContract.MyRoute._ID + " = ?",
                    new String[]{String.valueOf(route_id)},
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {

                try {
                    routeJSON = new JSONArray(cursor.getString(cursor.getColumnIndex(WSDOTContract.MyRoute.MY_ROUTE_LOCATIONS)));
                } catch (JSONException e){
                    routeJSON = new JSONArray();
                }
                displayLat = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_LAT));
                displayLong = cursor.getDouble(cursor.getColumnIndex(WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_LONG));
                displayZoom = cursor.getInt(cursor.getColumnIndex(WSDOTContract.MyRoute.MY_ROUTE_DISPLAY_ZOOM));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Draws a polyline of the users route on the map
     * @param myRouteLocations
     */
    private void drawRouteOnMap(ArrayList<LatLng> myRouteLocations) {
        PolylineOptions polylineOptions = new PolylineOptions().width(20).color(Color.BLUE).addAll(myRouteLocations);
        Polyline polyLine = mMap.addPolyline(polylineOptions);
        polyLine.setPoints(myRouteLocations);
    }
}