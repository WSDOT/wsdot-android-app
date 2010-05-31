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

import android.os.Bundle;
import android.widget.TextView;

public class VesselWatch extends MainMenu {
	
	@Override
	void prepareMenu() {
		addMenuItem("Anacortes / San Juan Islands / Sidney B.C.", VesselWatchMap.class);
		addMenuItem("San Juan Islands Inter-Island", VesselWatchMap.class);
		addMenuItem("Port Townsend / Keystone", VesselWatchMap.class);
		addMenuItem("Mukilteo / Clinton", VesselWatchMap.class);
		addMenuItem("Edmonds / Kingston", VesselWatchMap.class);
		addMenuItem("Seattle / Bainbridge", VesselWatchMap.class);
		addMenuItem("Seattle", VesselWatchMap.class);
		addMenuItem("Fauntleroy / Vashon / Southworth", VesselWatchMap.class);
		addMenuItem("Point Defiance / Tahlequah", VesselWatchMap.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((TextView)findViewById(R.id.sub_section)).setText("Ferries Vessel Watch");
	}
} 