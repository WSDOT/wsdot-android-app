/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.ferries.vesselwatch;

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

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

public class VesselWatchDetailsActivity extends BaseActivity {

    private static final String TAG = VesselWatchDetailsActivity.class.getSimpleName();
    private WebView webview;
	private String mTitle;
	private String mDescription;
	private String mContent;
	private View mLoadingSpinner;
	private Toolbar mToolbar;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle b = getIntent().getExtras();
		mTitle = b.getString("title");
		mDescription = b.getString("description");

        setContentView(R.layout.activity_webview_with_spinner);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle(mTitle);
		setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
		mLoadingSpinner = findViewById(R.id.loading_spinner);
		mLoadingSpinner.setVisibility(View.VISIBLE);
		webview = (WebView)findViewById(R.id.webview);
		webview.setWebViewClient(new myWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		
		mContent = "<p>" + mDescription + "</p>";
		webview.loadDataWithBaseURL(null, mContent, "text/html", "utf-8", null);

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
