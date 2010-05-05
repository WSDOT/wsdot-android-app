package gov.wa.wsdot.android.wsdot;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class MountainPassItemTabs extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tabs);
	    ((TextView)findViewById(R.id.sub_section)).setText("Mountain Passes");
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    tabHost.getTabWidget().setBackgroundColor(0xff017359);   
	    TabHost.TabSpec spec;
	    Intent intent;

	    Bundle b = getIntent().getExtras();
	    intent = new Intent().setClass(this, MountainPassItemDetails.class);
	    intent.putExtras(b);
	    spec = tabHost.newTabSpec("info")
	    				.setIndicator("Info", res.getDrawable(R.drawable.ic_tab_passes_info))
	    				.setContent(intent);
	    tabHost.addTab(spec);

	    Bundle b1 = getIntent().getExtras();
	    ArrayList<String> cameraUrls = new ArrayList<String>(b1.getStringArrayList("Cameras"));

	    // If there are no cameras for this pass, do not show the camera tab
	    if (cameraUrls.isEmpty()) {
	    } else {
		    intent = new Intent().setClass(this, MountainPassItemCamera.class);
		    intent.putExtras(b1);
		    spec = tabHost.newTabSpec("cameras")
		    				.setIndicator("Cameras", res.getDrawable(R.drawable.ic_tab_passes_camera))
		    				.setContent(intent);
		    tabHost.addTab(spec);
	    }

	    Bundle b2 = getIntent().getExtras();
	    intent = new Intent().setClass(this, MountainPassItemMap.class);
	    intent.putExtras(b2);
	    spec = tabHost.newTabSpec("map")
	    				.setIndicator("Map", res.getDrawable(R.drawable.ic_tab_passes_map))
	    				.setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTabByTag("info");
	}
}
