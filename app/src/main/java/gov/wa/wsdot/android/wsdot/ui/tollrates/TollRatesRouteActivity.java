package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.Utils;

public class TollRatesRouteActivity extends BaseActivity {

    private static final String TAG = TollRatesRouteActivity.class.getSimpleName();

    private WebView webview;
    private View mLoadingSpinner;
    private String title;
    private String description;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview_with_spinner);

        title = "Express Toll Lane";
        description = "";

        Bundle b = getIntent().getExtras();

        Double startLat = b.getDouble("startLat");
        Double startLong = b.getDouble("startLong");

        Double endLat = b.getDouble("endLat");
        Double endLong = b.getDouble("endLong");

        String text = b.getString("text");

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mLoadingSpinner = findViewById(R.id.loading_spinner);
        mLoadingSpinner.setVisibility(View.VISIBLE);

        webview = findViewById(R.id.webview);
        webview.setWebViewClient(new myWebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadDataWithBaseURL(null, buildContent(text, startLat, startLong, endLat, endLong), "text/html", "utf-8", null);

       // disableAds();
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

    private String buildContent(String text, double startLat, double startLon, double endLat, double endLon) {

        Location centerLocation = Utils.getCenterLocation(startLat, startLon, endLat, endLon);

        double centerLat = centerLocation.getLatitude();
        double centerLon = centerLocation.getLongitude();

        StringBuilder sb = new StringBuilder();

        int distance = Utils.getDistanceFromPoints(startLat, startLon, endLat, endLon);

        int zoom;
        if (distance < 3) {
            zoom = 13;
        } else if (distance < 4) {
            zoom = 12;
        } else if (distance < 9) {
            zoom = 11;
        } else {
            zoom = 10;
        }

       // sb.append("<p>" + text + "</p>");

        sb.append("<img src='");
        sb.append(APIEndPoints.STATIC_GOOGLE_MAPS);
        sb.append("?center=");
        sb.append(centerLat + "," + centerLon);
        sb.append("&zoom=");
        sb.append(zoom);
        sb.append("&size=320x320&maptype=roadmap");
        sb.append("&markers=color:green%7Clabel:S%7C");
        sb.append(startLat + "," + startLon);
        sb.append("&markers=color:red%7Clabel:E%7C");
        sb.append(endLat + "," + endLon);
        sb.append("&key=" + APIEndPoints.GOOGLE_API_KEY + "'>");

        return sb.toString();
    }
}

