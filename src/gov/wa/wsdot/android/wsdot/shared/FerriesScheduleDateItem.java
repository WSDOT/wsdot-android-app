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

package gov.wa.wsdot.android.wsdot.shared;


import java.io.Serializable;
import java.util.ArrayList;

public class FerriesScheduleDateItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4532721945762916647L;
	private String date;
	private ArrayList<FerriesTerminalItem> sailings = new ArrayList<FerriesTerminalItem>();
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public ArrayList<FerriesTerminalItem> getFerriesTerminalItem() {
		return sailings;
	}
	public void setFerriesTerminalItem(FerriesTerminalItem sailings) {
		this.sailings.add(sailings);
	}
	
}
