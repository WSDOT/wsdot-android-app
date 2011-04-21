/*
 * Copyright (c) 2011 Washington State Department of Transportation
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

public class SR167TollRatesActivity extends Activity {
	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.webview_tabs);
		webview = (WebView)findViewById(R.id.webview);
		webview.loadDataWithBaseURL(null, formatText(), "text/html", "utf-8", null);	
	}
	
	private String formatText()	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<ul>" +
				"<li>$.50 minimum during periods of little congestion.</li>" +
				"<li>$9.00 maximum during periods of heavy congestion.</li>" +
				"<li>During other levels of congestion, the toll amount varies between $.50 and $9.00.</li>" +
				"<li>Only single occupancy vehicles are tolled. Those vehicles with two or more passengers, and motorcycles, are not charged a toll.</li>" +
				"<li>Customers are only charged once - at the toll that is displayed when the vehicle enters the lane.</li>" +
				"</ul>" +
				"<p>The SR 167 HOT lanes project opened May 2008. Toll rates for HOT lanes are dynamically priced, meaning the toll can change throughout the day to ensure reliable travel times. The goal is to keep an average speed of at least 45 mph in the HOT lanes.</p>" +
				"<p>The toll rate can change based on congestion factors, time of day, traffic volumes, and traffic flow. Customers can anticipate that tolls will be in effect on the SR 167 HOT lanes between 5 a.m. and 7 p.m.</p>" +
				"<p><em>Good To Go!</em> customers who use the HOT lane should look for the electronic sign above the lane that displays the actual toll rate and make a choice to enter the lane.</p>");
			
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
