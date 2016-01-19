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

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;

public class I405TollRatesFragment extends BaseFragment {
	
    private static final String TAG = I405TollRatesFragment.class.getSimpleName();
    private WebView webview;
	private ViewGroup mRootView;
	//private View mLoadingSpinner;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_webview_with_spinner, null);
		//mLoadingSpinner = mRootView.findViewById(R.id.loading_spinner);
		//mLoadingSpinner.setVisibility(View.VISIBLE);
		webview = (WebView)mRootView.findViewById(R.id.webview);
		webview.setVisibility(View.GONE);
		webview.setWebViewClient(new myWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadDataWithBaseURL(null, formatText(), "text/html", "utf-8", null);

		disableAds(mRootView);


		return mRootView;
	}

	private String formatText()	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<p><strong>I-405 Express Toll Lanes between Bellevue and Lynnwood</strong><br />");
		sb.append("I-405 express toll lanes will let drivers choose to travel faster by paying a toll. "); 
		sb.append("Toll rates will adjust between 75 cents and $10 based on traffic volumes in the express ");
		sb.append("toll lane. Drivers will pay the rate they see upon entering the lanes, even if they see "); 
		sb.append("a higher price down the road. Transit, vanpools, carpools and motorcycles can use the ");
		sb.append("lanes for free with a <em>Good To Go!</em> account and pass.</p>");
		sb.append("<p><strong>Access to express toll lanes</strong><br />");
		sb.append("Drivers who choose to use the lanes, will merge to the far left regular lane and can ");
		sb.append("enter express toll lanes at designated access points that are marked with dashed lines. ");
		sb.append("Just remember that failure to use designated access points will result in a $136 ticket ");
		sb.append("for crossing the double white lines.</p>");
		sb.append("<p>There are two direct access ramps to I-405 express toll lanes that allow you to ");
		sb.append("directly enter the express toll lanes from the middle of the freeway. These ramps are at ");
		sb.append("Northeast 6th Street in Bellevue and Northeast 128th Street in Kirkland.</p>");
		sb.append("<p><strong>Using the lanes</strong><br />");
		sb.append("Any existing <em>Good To Go!</em> pass can be used to pay a toll.</p>");
		sb.append("<p>If you carpool on the I-405 express toll lanes, you must meet the occupancy requirements ");
		sb.append("and have a <em>Good To Go!</em> account and Flex Pass set to HOV mode to travel toll-free. Carpool ");
		sb.append("requirements are three occupants during weekday peaks hours (5-9 a.m. and 3-7 p.m.) and two ");
		sb.append("occupants during off-peak hours (mid-day, evenings and weekends).</p>");
		sb.append("<p>If a driver does not have a <em>Good To Go!</em> account, a Pay By Mail toll bill will be mailed ");
		sb.append("to the vehicle’s registered owner for an additional $2 per toll transaction.</p>");
	    sb.append("<p>Visit <a href=\"http://www.GoodToGo405.org\">GoodToGo405.org</a> for more information.</p>");
			
		return sb.toString();
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
			//mLoadingSpinner.setVisibility(View.GONE);
		}
	}
}
