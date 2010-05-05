package gov.wa.wsdot.android.wsdot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class NewsItemDetails extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_item_details);
		((TextView)findViewById(R.id.sub_section)).setText("News");
		Bundle b = getIntent().getExtras();
		((TextView)findViewById(R.id.heading)).setText(b.getString("heading"));
		((TextView)findViewById(R.id.description)).setText(b.getString("description"));
		((TextView)findViewById(R.id.link)).setText(b.getString("link"));		
	}

}
