package gov.wa.wsdot.android.wsdot.ui.alert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.restareas.RestAreaActivity;

public class EmergencyAlertDetailsActivity extends BaseActivity {

    private static final String TAG = RestAreaActivity.class.getSimpleName();
    private WebView webview;
    private View mLoadingSpinner;
    private String title;
    private String message;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview_with_spinner);

        title = "";
        message = "";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString("message");
        } else {
            message = "Error loading message content.";
        }

        webview = (WebView)findViewById(R.id.webview);
        webview.setVisibility(View.GONE);
        webview.setWebViewClient(new EmergencyAlertDetailsActivity.myWebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadDataWithBaseURL(null, message, "text/html", "utf-8", null);

        title = "Alert";

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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
/*
    private String buildContent(RestAreaItem item) {
        StringBuilder sb = new StringBuilder();

        sb.append("<img src=");
        sb.append("'http://maps.googleapis.com/maps/api/staticmap?center=");
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
*/

}
