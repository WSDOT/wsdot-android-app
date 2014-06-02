/*
 * Copyright (c) 2014 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HighwayAlertDetailsActivity extends ActionBarActivity {
    
    private ContentResolver resolver;

	private WebView webview;
	private View mLoadingSpinner;	
    private String title = "";
    private String description = "";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        resolver = getContentResolver();
        Cursor cursor = null;
        
        title = "";
        description = "";
        String content = "";
        String latitude = "";
        String longitude = "";
        String lastUpdated = "";
        String staticGoogleMap = "";
        
        String[] projection = {
                HighwayAlerts.HIGHWAY_ALERT_LATITUDE,
                HighwayAlerts.HIGHWAY_ALERT_LONGITUDE,
                HighwayAlerts.HIGHWAY_ALERT_CATEGORY,
                HighwayAlerts.HIGHWAY_ALERT_HEADLINE,
                HighwayAlerts.HIGHWAY_ALERT_PRIORITY,
                HighwayAlerts.HIGHWAY_ALERT_LAST_UPDATED,
                HighwayAlerts.HIGHWAY_ALERT_ID
                };
		
		Bundle b = getIntent().getExtras();
		String id = b.getString("id");
		
        try {
            cursor = resolver.query(
                    HighwayAlerts.CONTENT_URI,
                    projection,
                    HighwayAlerts.HIGHWAY_ALERT_ID + "=?",
                    new String[] {id},
                    null
                    );
            
            if (cursor != null && cursor.moveToFirst()) {
                title = "Highway Alert - " + cursor.getString(2);
                description = cursor.getString(3);
                latitude = cursor.getString(0);
                longitude = cursor.getString(1);
                lastUpdated = cursor.getString(5);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }   
		
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(title);
		
		setContentView(R.layout.fragment_webview_with_spinner);
		mLoadingSpinner = findViewById(R.id.loading_spinner);
		mLoadingSpinner.setVisibility(View.VISIBLE);
		webview = (WebView)findViewById(R.id.webview);
		webview.setWebViewClient(new myWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		
		staticGoogleMap = "http://maps.googleapis.com/maps/api/staticmap?center="
                + latitude + "," + longitude
                + "&zoom=15&size=320x320&maptype=roadmap&markers="
                + latitude + "," + longitude
                + "&sensor=false";
		
		content = "<p>" + description + "</p>"
		        + "<p>" + ParserUtils.relativeTime(lastUpdated, "MMMM d, yyyy h:mm a", false) + "</p>"
		        + "<img src=" + staticGoogleMap + ">";
		
		webview.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
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
	
}
