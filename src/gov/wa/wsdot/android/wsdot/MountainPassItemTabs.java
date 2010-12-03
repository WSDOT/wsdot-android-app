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

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class MountainPassItemTabs extends TabActivity {
	
	private ArrayList<CameraItem> cameraItems;
	private ArrayList<ForecastItem> forecastItems;
	
	@SuppressWarnings("unchecked")
	@Override
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

	    cameraItems = (ArrayList<CameraItem>)getIntent().getSerializableExtra("Cameras");
	    forecastItems = (ArrayList<ForecastItem>)getIntent().getSerializableExtra("Forecasts");
	        
	    // If there are no cameras for this pass, do not show the camera or map tabs
	    if (cameraItems.isEmpty()) {
	    } else {
		    intent = new Intent().setClass(this, MountainPassItemCamera.class);	    
		    b.putSerializable("Cameras", cameraItems);
		    intent.putExtras(b);
		    spec = tabHost.newTabSpec("cameras")
		    				.setIndicator("Cameras", res.getDrawable(R.drawable.ic_tab_passes_camera))
		    				.setContent(intent);
		    tabHost.addTab(spec);
	    	
	    	intent = new Intent().setClass(this, MountainPassItemMap.class);	    
		    intent.putExtras(b);
		    spec = tabHost.newTabSpec("map")
		    				.setIndicator("Map", res.getDrawable(R.drawable.ic_tab_passes_map))
		    				.setContent(intent);
		    tabHost.addTab(spec);
	    }
	    
	    // If there is no forecast for this pass, do not show the tab
	    if (forecastItems.isEmpty()) {
	    } else {
		    intent = new Intent().setClass(this, MountainPassItemForecast.class);	    
		    b.putSerializable("Forecasts", forecastItems);
		    intent.putExtras(b);
		    spec = tabHost.newTabSpec("forecast")
		    				.setIndicator("Forecast", res.getDrawable(R.drawable.ic_tab_passes_forecast))
		    				.setContent(intent);
		    tabHost.addTab(spec);
	    }
	    
	    tabHost.setCurrentTabByTag("info");
	}
}
