/*
 * Copyright (c) 2012 Washington State Department of Transportation
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockFragmentActivity {
	private static final String DEBUG_TAG = "About";
	WebView webview;
	String versionName = "Not available";
	PackageInfo packageInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		AnalyticsUtils.getInstance(this).trackPageView("/About");
		
	    try {
	        packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	    	versionName = "v" + packageInfo.versionName;
	    } catch (NameNotFoundException e) {
	        Log.e(DEBUG_TAG, "Not found", e);
	    }		
		
		setContentView(R.layout.webview);
		
		webview = (WebView)findViewById(R.id.webview);
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
				"<p>Questions, comments or suggestions about this app can be e-mailed to the " +
				"<a href=\"mailto:webfeedback@wsdot.wa.gov?subject=WSDOT Android App\">WSDOT " +
				"Communications Office</a>.</p>" +
				"<div class=\"g-plus\" data-height=\"69\" data-href=\"https://plus.google.com/103669336535896913187\" data-rel=\"publisher\"></div>" +
				"<script type=\"text/javascript\">" +
				"(function() {" +
				"var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;" +
				"po.src = 'https://apis.google.com/js/plusone.js';" +
				"var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);" +
				" })();" +
				"</script>" +
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
}
