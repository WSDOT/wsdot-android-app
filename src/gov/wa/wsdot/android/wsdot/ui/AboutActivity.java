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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AboutActivity extends ActionBarActivity {
	
    private static final String TAG = AboutActivity.class.getSimpleName();
	WebView webview;
	String versionName = "Not available";
	PackageInfo packageInfo;
	private View mLoadingSpinner;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		AnalyticsUtils.getInstance(this).trackPageView("/About");
		
	    try {
	        packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	    	versionName = "v" + packageInfo.versionName;
	    } catch (NameNotFoundException e) {
	        Log.e(TAG, "Not found", e);
	    }		
		
		setContentView(R.layout.fragment_webview_with_spinner);
		mLoadingSpinner = findViewById(R.id.loading_spinner);
		mLoadingSpinner.setVisibility(View.VISIBLE);
		webview = (WebView)findViewById(R.id.webview);
		webview.setWebViewClient(new myWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadDataWithBaseURL(null, formatText(), "text/html", "utf-8", null);
	
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

	private String formatText()	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<p>The mission of the Washington State Department of Transportation " +
				"is to keep people and business moving by operating and improving the state's " +
				"transportation systems vital to our taxpayers and communities.</p>" +
				"<p>The WSDOT mobile app was created to make it easier for you to know the latest " +
				"about Washington's transportation system.</p>" +
				"<p><strong>Advertising</strong><br />" +
                "In an effort to generate a new source of revenue, WSDOT is conducting a " +
				"<a href=\"http://www.wsdot.wa.gov/funding/partners/projects/webadvertising.htm\" target=\"_blank\">digital advertising pilot project</a> " +
				"to allow commercial advertising through its websites, social media tools and mobile apps.</p>" +
				"<p>Questions, comments or suggestions about this app can be e-mailed to the " +
				"<a href=\"mailto:webfeedback@wsdot.wa.gov\">WSDOT " +
				"Communications Office</a>.</p>" +
				"<br /><p style=\"color:#959595;\">App Version: " + versionName);
			
		return sb.toString();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	        webview.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
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
			} else if (url.startsWith("mailto:")) {
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"webfeedback@wsdot.wa.gov"});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WSDOT Android App");
				emailIntent.setType("message/rfc822"); // this prompts email client only
				startActivity(Intent.createChooser(emailIntent, "Send Email using"));
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
