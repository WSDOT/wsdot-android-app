package gov.wa.wsdot.android.wsdot.ui.trafficmap.restareas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.RestAreaItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class RestAreaActivity extends BaseActivity {

    private static final String TAG = RestAreaActivity.class.getSimpleName();
    private WebView webview;
    private View mLoadingSpinner;
    private String title;
    private Toolbar mToolbar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview_with_spinner);

        title = "";

        RestAreaItem restAreaItem = null;
        String jsonMyObject = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("restarea_json");
            restAreaItem = new Gson().fromJson(jsonMyObject, RestAreaItem.class);
        }

        webview = findViewById(R.id.webview);
        webview.setVisibility(View.GONE);
        webview.setWebViewClient(new myWebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);

        if (restAreaItem != null){
            title = restAreaItem.getRoute() + " - " + restAreaItem.getLocation() + " Rest Area";
            webview.loadDataWithBaseURL(null, buildContent(restAreaItem), "text/html", "utf-8", null);
        } else {
            title = "Rest Area";
            webview.loadDataWithBaseURL(null, "Error loading rest area information.", "text/html", "utf-8", null);
        }
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        MyLogger.crashlyticsLog("Traffic", "Screen View", "RestAreaActivity", 1);
        disableAds();

        mLoadingSpinner = findViewById(R.id.loading_spinner);
        mLoadingSpinner.setVisibility(View.VISIBLE);
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
            webview.setVisibility(View.VISIBLE);
            mLoadingSpinner.setVisibility(View.GONE);
        }
    }

    private String buildContent(RestAreaItem item) {
        StringBuilder sb = new StringBuilder();

        sb.append("<p> <h3> Milepost: ");
        sb.append(item.getMilepost());
        sb.append(" - ");
        sb.append(item.getDirection());
        sb.append("</h3></p>");

        sb.append("<p> <b> Amenities: </b> </p>");
        sb.append("<ul>");
        for (String amenity : item.getAmenities()) {
            sb.append("<li>");
            sb.append(amenity);
            sb.append("</li>");
        }
        sb.append("</ul>");

        if (!item.getNotes().equals("null")) {
            sb.append("<p><b>Special Notes: </b> </p> <p> ");
            sb.append(item.getNotes());
            sb.append(" </p>");
        }

        sb.append("<img src='");
        sb.append(APIEndPoints.STATIC_GOOGLE_MAPS);
        sb.append("?center=");
        sb.append(item.getLatitude());
        sb.append(",");
        sb.append(item.getLongitude());
        sb.append("&zoom=15&size=320x320&maptype=roadmap&markers=");
        sb.append(item.getLatitude());
        sb.append(",");
        sb.append(item.getLongitude());
        sb.append("&key=");
        sb.append(getString(R.string.google_static_map_key));
        sb.append("'>");

        return sb.toString();
    }
}
