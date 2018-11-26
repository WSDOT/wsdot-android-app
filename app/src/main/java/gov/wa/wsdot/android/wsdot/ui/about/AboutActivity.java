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

package gov.wa.wsdot.android.wsdot.ui.about;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class AboutActivity extends BaseActivity {
	
    private static final String TAG = AboutActivity.class.getSimpleName();
	WebView webview;
	String versionName = "Not available";
	PackageInfo packageInfo;
	private View mLoadingSpinner;
    private Toolbar mToolbar;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    try {
	        packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	    	versionName = "v" + packageInfo.versionName;
	    } catch (NameNotFoundException e) {
	        Log.e(TAG, "Not found", e);
	    }		
		
		setContentView(R.layout.activity_webview_with_spinner);

		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

		disableAds();
		
		mLoadingSpinner = findViewById(R.id.progress_bar);
		mLoadingSpinner.setVisibility(View.VISIBLE);
		webview = findViewById(R.id.webview);
        webview.setVisibility(View.GONE);
		webview.setWebViewClient(new myWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadDataWithBaseURL(null, formatText(), "text/html", "utf-8", null);

		MyLogger.crashlyticsLog("Home", "Screen View", "AboutActivity", 1);


	}

	@Override
	public void onResume() {
		super.onResume();
		setFirebaseAnalyticsScreenName("About");
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
		
		sb.append("<p>The Washington State Department of Transportation provides and supports safe, ");
		sb.append("reliable and cost-effective transportation options to improve livable ");
		sb.append("communities and economic vitality for people and businesses. </p>");

		sb.append("<p>The WSDOT mobile app was created to make it easier for you to know the latest ");
		sb.append("about Washington's transportation system.</p>");

		sb.append("<p>Looking for a career at WSDOT? Check our ");
		sb.append("<a href=\"https://www.governmentjobs.com/careers/washington?department%5B0%5D=Dept.%20of%20Transportation\">current job openings</a>.");

		sb.append("<p>To report HOV, HOT lane or ferry line violators please call <a href=\"tel:+1-877-764-4376\">1-877-764-4376</a> ");
		sb.append("or use our <a href=\"http://www.wsdot.wa.gov/HOV/reporting\">online reporting form</a>.</p>");

		sb.append("<p>If you have questions or comments about the WSDOT ferry system, please e-mail a ");
		sb.append("<a href=\"mailto:wsfinfo@wsdot.wa.gov\"> WSF Information Agent</a>.</p>");

        sb.append("<p>Questions, comments or suggestions about this app can be e-mailed to the ");
        sb.append("<a href=\"mailto:webfeedback@wsdot.wa.gov\">WSDOT ");
        sb.append("Communications Office</a>.</p>");

		sb.append("<br/> <p style=\"color:#959595;\">App Version: ");
		sb.append(versionName);
			
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

			    // trim mailto:
			    String email = url.substring(7);

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});

                if (email.equals("webfeedback@wsdot.wa.gov")) {
                    if (versionName.equalsIgnoreCase("Not available")) {
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WSDOT Android App");
                    } else {
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WSDOT Android App " + versionName);
                    }
                }

                emailIntent.setType("message/rfc822"); // this prompts email client only
                startActivity(Intent.createChooser(emailIntent, "Send Email using"));
            } else if (url.startsWith("tel:")) {
                Intent phoneIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(phoneIntent);
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
}
