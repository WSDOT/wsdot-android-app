package gov.wa.wsdot.android.wsdot;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class SeattleTrafficTabs extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tabs);
	    ((TextView)findViewById(R.id.sub_section)).setText("Seattle Area Traffic");
	    
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    tabHost.getTabWidget().setBackgroundColor(0xff017359);   
	    TabHost.TabSpec spec;
	    Intent intent;

	    intent = new Intent().setClass(this, SeattleTrafficMap.class);
	    spec = tabHost.newTabSpec("map")
	    				.setIndicator("Map", res.getDrawable(R.drawable.ic_tab_passes_map))
	    				.setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, SeattleTrafficAlerts.class);
	    spec = tabHost.newTabSpec("alerts")
	    					.setIndicator("Alerts", res.getDrawable(R.drawable.ic_tab_seattle_alerts))
	    					.setContent(intent);
	    tabHost.addTab(spec);

	    /*
	    intent = new Intent().setClass(this, SeattleTrafficTravelTimes.class);
	    spec = tabHost.newTabSpec("times")
						.setIndicator("Times", res.getDrawable(R.drawable.ic_tab_seattle_traveltimes))
						.setContent(intent);
	    tabHost.addTab(spec);
	    */

	    tabHost.setCurrentTabByTag("map");
	}
}
