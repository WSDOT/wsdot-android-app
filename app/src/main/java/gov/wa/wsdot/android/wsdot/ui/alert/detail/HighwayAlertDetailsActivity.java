/*
 * Copyright (c) 2017 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.alert.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class HighwayAlertDetailsActivity extends BaseActivity implements
        OnMapReadyCallback {
    
    private static final String TAG = HighwayAlertDetailsActivity.class.getSimpleName();

	private WebView webview;
	private View mLoadingSpinner;
	private String title;
	private String description;
    private Toolbar mToolbar;

    private LatLng mAlertLatLng;

    private Tracker mTracker;

    private boolean fromNotification = false;

    private GoogleMap mMap;

    HighwayAlertDetailsViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
		super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_with_lite_mapview);

        title = "Highway Alert";
        description = "";
		
		Bundle b = getIntent().getExtras();
		Integer id = b.getInt("id");
		Boolean force = b.getBoolean("refresh", false);

		fromNotification = b.getBoolean("from_notification");

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

		viewModel = ViewModelProviders.of(this, viewModelFactory).get(HighwayAlertDetailsViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mLoadingSpinner = findViewById(R.id.loading_spinner);
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        if (mToolbar.getTitle().toString().equals("Highway Alert")) {
                            webview = findViewById(R.id.webview);
                            webview.setWebViewClient(new myWebViewClient());
                            webview.loadDataWithBaseURL(null, "<p>Sorry, this alert has expired.</p>", "text/html", "utf-8", null);
                        }
                        break;
                    case ERROR:
                        if (mToolbar.getTitle().toString().equals("Highway Alert")) {
                            mLoadingSpinner.setVisibility(View.GONE);
                            webview = findViewById(R.id.webview);
                            webview.setWebViewClient(new myWebViewClient());
                            webview.loadDataWithBaseURL(null, "Connection error, failed to load alert.", "text/html", "utf-8", null);
                        } else {
                            Toast.makeText(this, "Connection error", Toast.LENGTH_LONG).show();
                        }
                }
            }
        });

		viewModel.getHighwayAlertFor(id, force).observe(this, alertItem -> {

		    if (alertItem != null) {

                mAlertLatLng = new LatLng(alertItem.getStartLatitude(), alertItem.getStartLongitude());

                title = "Highway Alert - " + alertItem.getCategory();
                description = alertItem.getHeadline();

                mToolbar = findViewById(R.id.toolbar);
                mToolbar.setTitle(title);
                setSupportActionBar(mToolbar);
                if(getSupportActionBar() != null){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }

                webview = findViewById(R.id.webview);
                webview.setWebViewClient(new myWebViewClient());
                webview.loadDataWithBaseURL(null, buildContent(alertItem), "text/html", "utf-8", null);

                // Get the map and register for the ready callback
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                if (fromNotification){
                    mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Notification")
                            .setAction("Message Opened")
                            .setLabel(alertItem.getCategory())
                            .build());
                }

            }
        });



        disableAds();
        MyLogger.crashlyticsLog("Highway Alerts", "Screen View", "HighwayAlertDetailsActivity", 1);
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

            if (mAlertLatLng != null) {

                mMap.addMarker(new MarkerOptions()
                        .position(mAlertLatLng));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mAlertLatLng, 15));

            }
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_action_provider, menu);
        // Set file with share history to the provider and set the share intent.
        MenuItem menuItem_Share = menu.findItem(R.id.action_share);
        ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem_Share);
        shareAction.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        shareAction.setShareIntent(createShareIntent());
    	
    	return super.onCreateOptionsMenu(menu);
	}
    
	private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, description);
        
        return shareIntent;
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
	
    private String buildContent(HighwayAlertEntity item) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<p>" + item.getHeadline() + "</p>");
        sb.append("<p style='color:#7d7d7d;'>"
                + ParserUtils.relativeTime(item.getLastUpdated(),
                        "MMMM d, yyyy h:mm a", false) + "</p>");

        return sb.toString();
    }
}
