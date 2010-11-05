/*
 * Copyright (c) 2010 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.TextView;

public class About extends Activity {
	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		((TextView)findViewById(R.id.sub_section)).setText("About");
		
		webview = (WebView)findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setPluginsEnabled(true);
		webview.loadDataWithBaseURL(null, formatText(), "text/html", "utf-8", null);
	}
	
	private String formatText()	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<p>The mission of the Washington State Department of Transportation " +
				"is to keep people and business moving by operating and improving the state's " +
				"transportation systems vital to our taxpayers and communities.</p>" +
				"<p>The WSDOT mobile app was created to make it easier for you to know the latest " +
				"about Washington's transportation system.</p>" +
				"<p>Questions, comments or suggestions can be e-mailed to the <a href=\"mailto:webfeedback@wsdot.wa.gov\">WSDOT " +
				"Communications Office</a> or give us a call at " +
				"<a href=\"tel:3607057079\">360-705-7079</a>.</p>");
			
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
