package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.appcompat.widget.Toolbar;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.Utils;

public class TollRatesRouteActivity extends BaseActivity implements
        OnMapReadyCallback {

    private static final String TAG = TollRatesRouteActivity.class.getSimpleName();

    private WebView webview;
    private View mLoadingSpinner;
    private String title;
    private String description;
    private Toolbar mToolbar;

    private GoogleMap mMap;

    private LatLng mStartPoint;
    private LatLng mEndPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_with_lite_mapview);

        Bundle b = getIntent().getExtras();

        Double startLat = b.getDouble("startLat");
        Double startLong = b.getDouble("startLong");

        Double endLat = b.getDouble("endLat");
        Double endLong = b.getDouble("endLong");

        mStartPoint = new LatLng(startLat, startLong);
        mEndPoint = new LatLng(endLat, endLong);

        title = b.getString("title");
        description = b.getString("text");

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mLoadingSpinner = findViewById(R.id.progress_bar);
        mLoadingSpinner.setVisibility(View.VISIBLE);

        webview = findViewById(R.id.webview);
        webview.setWebViewClient(new myWebViewClient());
        webview.loadDataWithBaseURL(null, buildContent(), "text/html", "utf-8", null);


        disableAds();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the map and register for the ready callback
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setFirebaseAnalyticsScreenName("TollRoute");

    }

    /**
     * Called when the map is ready to add all markers and objects to the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarker();
    }

    private void addMarker() {
        if (mMap != null){

            mMap.getUiSettings().setMapToolbarEnabled(false);

            if (mStartPoint != null && mEndPoint != null) {

                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .position(mStartPoint));

                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(mEndPoint));

                Location centerLocation = Utils.getCenterLocation(mStartPoint.latitude, mStartPoint.longitude, mEndPoint.latitude, mEndPoint.longitude);

                LatLng center = new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude());

                int distance = Utils.getDistanceFromPoints(mStartPoint.latitude, mStartPoint.longitude, mEndPoint.latitude, mEndPoint.longitude);

                int zoom;
                if (distance < 3) {
                    zoom = 14;
                } else if (distance < 4) {
                    zoom = 13;
                } else if (distance < 9) {
                    zoom = 12;
                } else {
                    zoom = 11;
                }

                // Move camera to show all markers and locations
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));

            }
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

    public class myWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http:") || url.startsWith("https:")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mLoadingSpinner.setVisibility(View.GONE);
        }
    }

    private String buildContent() {

        StringBuilder sb = new StringBuilder();

        sb.append("<p>" + description + "</p>");

        return sb.toString();
    }
}

