/*
 * Copyright (c) 2012 Washington State Department of Transportation
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
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class BorderWaitTabs extends TabActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.tabs);
        ((TextView)findViewById(R.id.sub_section)).setText("Border Wait Times");

	    TabHost tabHost = getTabHost();
	    tabHost.getTabWidget().setBackgroundColor(0xff017359);   
	    TabHost.TabSpec spec;
	    Intent intent;

	    intent = new Intent().setClass(BorderWaitTabs.this, BorderWaitNorthbound.class);
	    spec = tabHost.newTabSpec("northbound")
	    				.setIndicator("Northbound")
	    				.setContent(intent);
	    tabHost.addTab(spec);        
        
	    intent = new Intent().setClass(BorderWaitTabs.this, BorderWaitSouthbound.class);
	    spec = tabHost.newTabSpec("southbound")
	    				.setIndicator("Southbound")
	    				.setContent(intent);
	    tabHost.addTab(spec);	    

	    tabHost.setCurrentTabByTag("northbound");        
    }
}