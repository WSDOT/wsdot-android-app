package gov.wa.wsdot.android.wsdot.ui.myroute.report;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.alert.detail.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteViewModel;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertListViewModel;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class MyRouteReportActivity extends BaseActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private final String TAG = MyRouteReportActivity.class.getSimpleName();

    private GoogleMap mMap;

    private MyRouteViewModel myRoutesViewModel;

    private MyRouteAlertListViewModel alertListViewModel;

    private HashMap<Marker, String> markers = new HashMap<>();
    private List<HighwayAlertsItem> alerts = new ArrayList<>();

    private JSONArray routeJSON = new JSONArray();
    private Double displayLat = 0.0;
    private Double displayLong = 0.0;
    private int displayZoom = 0;

    private long mRouteId = -1;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle args = getIntent().getExtras();

        if (args != null) {

            toolbar.setTitle(args.getString("route_name"));
            mRouteId = args.getLong("route_id");

        }

        myRoutesViewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        MyRouteReportFragment reportFragment = (MyRouteReportFragment) getSupportFragmentManager().findFragmentById(R.id.report_fragment);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.e("MyRouteReport", "map ready");

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setOnMarkerClickListener(this);

        alertListViewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteAlertListViewModel .class);

        Log.e("MyRouteReport", String.valueOf(mRouteId));

        myRoutesViewModel.loadMyRoute(mRouteId).observe(this, myRoute -> {
            if (myRoute != null) {

                // Focus on route and add it to the map
                try {
                    routeJSON = new JSONArray(myRoute.getRouteLocations());
                } catch (JSONException e){
                    routeJSON = new JSONArray();
                }
                displayLat = myRoute.getLatitude();
                displayLong = myRoute.getLongitude();
                displayZoom = myRoute.getZoom();

                drawRouteOnMap(ParserUtils.getRouteArrayList(routeJSON));
                LatLng viewlatLng = new LatLng(displayLat, displayLong);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viewlatLng, displayZoom));


                // Get highway alerts on route and display icons on map
                alertListViewModel.getHighwayAlertsInBounds(myRoute.getRouteLocations()).observe(this, alertItems -> {
                    if (alerts != null) {

                        Iterator<Map.Entry<Marker, String>> iter = markers.entrySet().iterator();
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

                                    Log.e(TAG, String.valueOf(alerts.get(i).getCategoryIcon()));

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
                    }
                });
            } else {
                Log.e("MyRouteReport", "im null..");
            }
        });

    }

    /**
     * Draws a polyline of the users route on the map
     * @param myRouteLocations
     */
    private void drawRouteOnMap(ArrayList<LatLng> myRouteLocations) {
        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.BLUE).addAll(myRouteLocations);
        Polyline polyLine = mMap.addPolyline(polylineOptions);
        polyLine.setPoints(myRouteLocations);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle b = new Bundle();
        Intent intent;

        if (markers.get(marker).equalsIgnoreCase("alert")) {
            MyLogger.crashlyticsLog("My Route", "Tap", "Alert", 1);
            intent = new Intent(this, HighwayAlertDetailsActivity.class);
            b.putInt("id", Integer.valueOf(marker.getSnippet()));
            intent.putExtras(b);
            this.startActivity(intent);
        }

        return true;
    }


}
