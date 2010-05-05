package gov.wa.wsdot.android.wsdot;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.TextView;

public class PhotoItemDetails extends Activity {
	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_item_details);
		((TextView)findViewById(R.id.sub_section)).setText("Photos");
		Bundle b = getIntent().getExtras();
		
		((TextView)findViewById(R.id.photo_heading)).setText(b.getString("heading"));
		String html_content = b.getString("content");
		webview = (WebView)findViewById(R.id.photo_webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setPluginsEnabled(true);
		webview.loadData(html_content, "text/html", "utf-8");
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
