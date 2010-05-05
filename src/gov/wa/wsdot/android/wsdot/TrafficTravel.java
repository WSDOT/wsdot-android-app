package gov.wa.wsdot.android.wsdot;

import android.os.Bundle;
import android.widget.TextView;

public class TrafficTravel extends MainMenu {
	
	@Override
	void prepareMenu() {
		addMenuItem("Mountain Passes", MountainPassConditions.class);
		addMenuItem("Seattle Area", SeattleTrafficTabs.class);
		addMenuItem("Small Traffic Site", SmallTraffic.class);
		addMenuItem("Tacoma", TacomaTrafficMap.class);
		addMenuItem("Olympia", OlympiaTrafficMap.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((TextView)findViewById(R.id.sub_section)).setText("Traffic & Travel");
	}
} 