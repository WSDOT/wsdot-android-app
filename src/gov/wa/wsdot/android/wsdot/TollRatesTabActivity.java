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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class TollRatesTabActivity extends TabActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.tabs);
	    ((TextView)findViewById(R.id.sub_section)).setText("Toll Rates");
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    tabHost.getTabWidget().setBackgroundColor(0xff017359);   
	    TabHost.TabSpec spec;
	    Intent intent;

	    // SR 520 Bridge
	    intent = new Intent().setClass(this, SR520TollRatesActivity.class);
	    spec = tabHost.newTabSpec("sr520")
	    				.setIndicator("SR 520 Bridge", res.getDrawable(R.drawable.ic_tab_sr520))
	    				.setContent(intent);
	    tabHost.addTab(spec);
        
	    // SR 16 Tacoma Narrows Bridge
	    intent = new Intent().setClass(this, SR16TollRatesActivity.class);	    
	    spec = tabHost.newTabSpec("sr16")
	    				.setIndicator("Tacoma Narrows", res.getDrawable(R.drawable.ic_tab_sr16))
	    				.setContent(intent);
	    tabHost.addTab(spec);

	    // SR 167 Hot Lanes
	    intent = new Intent().setClass(this, SR167TollRatesActivity.class);	    
	    spec = tabHost.newTabSpec("sr167")
	    				.setIndicator("HOT Lanes", res.getDrawable(R.drawable.ic_tab_sr167))
	    				.setContent(intent);
	    tabHost.addTab(spec);
	    
	    tabHost.setCurrentTabByTag("sr520");
	}
}
