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

import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MountainPassItemDetails extends Activity {
	private static final String DEBUG_TAG = "MountainPassItemDetails";
	DateFormat parseDateFormat = new SimpleDateFormat("yyyy,M,d,H,m"); //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]
	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mountainpass_item_details);
		
		Bundle b = getIntent().getExtras();		
		AnalyticsUtils.getInstance(this).trackPageView("/Mountain Passes/" + b.getString("MountainPassName") + "/Info");
		
		String weatherCondition;
		String temperatureInFahrenheit;
		
		weatherCondition = b.getString("WeatherCondition");
		temperatureInFahrenheit = b.getString("TemperatureInFahrenheit");
		
		if (weatherCondition.equals("")) weatherCondition = "Not available";
		if (temperatureInFahrenheit.equals("null")) {
			temperatureInFahrenheit = "Not available";
		} else {
			temperatureInFahrenheit = temperatureInFahrenheit + "\u00b0F";
		}

		String tempDate = b.getString("DateUpdated");
		try {
			tempDate = tempDate.replace("[", "");
			tempDate = tempDate.replace("]", "");
			
			String[] a = tempDate.split(",");
			StringBuilder result = new StringBuilder();
			for (int i=0; i < 5; i++) {
				result.append(a[i]);
				result.append(",");
			}
			tempDate = result.toString().trim();
			tempDate = tempDate.substring(0, tempDate.length()-1);
			Date date = parseDateFormat.parse(tempDate);
			((TextView)findViewById(R.id.DateUpdated)).setText(displayDateFormat.format(date));
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Error parsing date: " + tempDate, e);
		}
		
		((TextView)findViewById(R.id.MountainPassName)).setText(b.getString("MountainPassName"));
		((TextView)findViewById(R.id.WeatherCondition)).setText(weatherCondition);
		((TextView)findViewById(R.id.TemperatureInFahrenheit)).setText(temperatureInFahrenheit);
		((TextView)findViewById(R.id.ElevationInFeet)).setText(b.getString("ElevationInFeet") + " ft");
		((TextView)findViewById(R.id.RoadCondition)).setText(b.getString("RoadCondition"));
		((TextView)findViewById(R.id.heading_RestrictionOneTravelDirection)).setText("Restrictions " + b.getString("RestrictionOneTravelDirection") + ":");
		((TextView)findViewById(R.id.RestrictionOneText)).setText(b.getString("RestrictionOneText"));
		((TextView)findViewById(R.id.heading_RestrictionTwoTravelDirection)).setText("Restrictions " + b.getString("RestrictionTwoTravelDirection") + ":");
		((TextView)findViewById(R.id.RestrictionTwoText)).setText(b.getString("RestrictionTwoText"));
	}
}
