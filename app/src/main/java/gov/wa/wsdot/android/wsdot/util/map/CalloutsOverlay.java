/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.util.map;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.shared.CalloutItem;

public class CalloutsOverlay {
	@SuppressWarnings("unused")
    private static final String TAG = CalloutsOverlay.class.getSimpleName();
	private List<CalloutItem> calloutItems = new ArrayList<CalloutItem>();
	
	public CalloutsOverlay() {
        CalloutItem item = new CalloutItem();
        item.setTitle("JBLM");
        item.setImageUrl("http://images.wsdot.wa.gov/traffic/flowmaps/jblm.png");
        item.setLatitude(47.103033);
        item.setLongitude(-122.584394);

        calloutItems.add(item);
	}

    public List<CalloutItem> getCalloutItems() {
        return calloutItems;
    }
    
    public int size() {
        return calloutItems.size();
    }

}
