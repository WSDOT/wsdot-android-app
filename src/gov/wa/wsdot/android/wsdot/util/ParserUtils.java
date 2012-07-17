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

package gov.wa.wsdot.android.wsdot.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ParserUtils {
	
	public static String relativeTime(String createdAt, String datePattern, boolean isUTC) {
		DateFormat parseDateFormat = new SimpleDateFormat(datePattern);
		Date parseDate;

		if (isUTC) {
			parseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}		
		
		try {
			parseDate = parseDateFormat.parse(createdAt);
		} catch (ParseException e) {
			return "Error with date";
		}
		
		return getRelative(parseDate);

	}
	
	public static String relativeTime(Date createdAt) { 
		return(getRelative(createdAt));
	}
	
	private static String getRelative(Date date) {
		DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");	
		int delta = 0;
		  
		try {
			Date relativeDate = new Date();
			delta = (int)((relativeDate.getTime() - date.getTime()) / 1000); // convert to seconds
			if (delta < 60) {
				return "just now";
			} else if (delta < 120) {
                return "1 minute ago";
			} else if (delta < (60*60)) {
                return Integer.toString(delta / 60) + " minutes ago";
			} else if (delta < (120*60)) {
                return "1 hour ago";
			} else if (delta < (24*60*60)) {
                return Integer.toString(delta / 3600) + " hours ago";
			} else {
                return displayDateFormat.format(date);
			}
		} catch (Exception e) {
			return "Unavailable";
		}		
		
	}
}
