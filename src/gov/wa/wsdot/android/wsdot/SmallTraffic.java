package gov.wa.wsdot.android.wsdot;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class SmallTraffic extends Activity {
	WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.traffic);
		webview = (WebView)findViewById(R.id.traffic_webview);
		webview.loadUrl("http://www.wsdot.wa.gov/small/");
	}
}
