package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class MyRouteMapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Long route_id;

    private JSONArray routeJSON = new JSONArray();
    private Double displayLat = 0.0;
    private Double displayLong = 0.0;
    private int displayZoom = 0;

    private MyRouteViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_route_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle args = getIntent().getExtras();

        String route_name = args.getString("route_name");
        route_id = args.getLong("route_id");

        Log.e("test", String.valueOf(route_id));

        Toolbar mToolbar = findViewById(R.id.toolbar);

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);

        viewModel.loadMyRoute(route_id).observe(this, myRoute -> {
            if (myRoute != null){

                try {
                    routeJSON = new JSONArray(myRoute.getRouteLocations());
                } catch (JSONException e){
                    routeJSON = new JSONArray();
                }
                displayLat = myRoute.getLatitude();
                displayLong = myRoute.getLongitude();
                displayZoom = myRoute.getZoom();

                drawRouteOnMap(ParserUtils.getRouteArrayList(routeJSON));
                LatLng latLng = new LatLng(displayLat, displayLong);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, displayZoom));
            }
        });
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
     * Draws a polyline of the users route on the map
     * @param myRouteLocations
     */
    private void drawRouteOnMap(ArrayList<LatLng> myRouteLocations) {
        PolylineOptions polylineOptions = new PolylineOptions().width(20).color(Color.BLUE).addAll(myRouteLocations);
        Polyline polyLine = mMap.addPolyline(polylineOptions);
        polyLine.setPoints(myRouteLocations);
    }
}