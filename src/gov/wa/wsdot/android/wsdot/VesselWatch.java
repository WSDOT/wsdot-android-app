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
		addMenuItem("Anacortes / San Juan Islands / Sidney B.C.", AnacortesVesselWatchMap.class);
		addMenuItem("San Juan Islands Inter-Island", SanJuanIslandsVesselWatchMap.class);
		addMenuItem("Port Townsend / Keystone", PortTownsendVesselWatchMap.class);
		addMenuItem("Mukilteo / Clinton", MukilteoVesselWatchMap.class);
		addMenuItem("Edmonds / Kingston", EdmondsVesselWatchMap.class);
		addMenuItem("Seattle / Bainbridge", SeattleBainbridgeVesselWatchMap.class);
		addMenuItem("Seattle", SeattleVesselWatchMap.class);
		addMenuItem("Fauntleroy / Vashon / Southworth", FauntleroyVesselWatchMap.class);
		addMenuItem("Point Defiance / Tahlequah", PointDefianceVesselWatchMap.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((TextView)findViewById(R.id.sub_section)).setText("Ferries Vessel Watch");
	}
} 