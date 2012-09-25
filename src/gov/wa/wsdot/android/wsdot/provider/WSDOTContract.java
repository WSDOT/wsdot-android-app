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

package gov.wa.wsdot.android.wsdot.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class WSDOTContract {

	interface CachesColumns {
		String CACHE_TABLE_NAME = "cache_table_name";
		String CACHE_LAST_UPDATED = "cache_last_updated";
	}
	
	interface CamerasColumns {
	    String CAMERA_ID = "id";
	    String CAMERA_TITLE = "title";
	    String CAMERA_URL = "url";
	    String CAMERA_LATITUDE = "latitude";
	    String CAMERA_LONGITUDE = "longitude";
	    String CAMERA_HAS_VIDEO = "has_video";
	    String CAMERA_ROAD_NAME = "road_name";
	    String CAMERA_IS_STARRED = "is_starred";
	}
	
	interface HighwayAlertsColumns {
		String HIGHWAY_ALERT_ID = "highway_alert_id";
		String HIGHWAY_ALERT_HEADLINE = "highway_alert_headline";
		String HIGHWAY_ALERT_LATITUDE = "highway_alert_latitude";
		String HIGHWAY_ALERT_LONGITUDE = "highway_alert_longitude";
		String HIGHWAY_ALERT_CATEGORY = "highway_alert_category";
		String HIGHWAY_ALERT_PRIORITY = "highway_alert_priority";
		String HIGHWAY_ALERT_ROAD_NAME = "highway_alert_road_name";
	}

	interface MountainPassesColumns {
		String MOUNTAIN_PASS_ID = "id";
		String MOUNTAIN_PASS_NAME = "name";
		String MOUNTAIN_PASS_WEATHER_CONDITION = "weather_condition";
		String MOUNTAIN_PASS_ELEVATION = "elevation";
		String MOUNTAIN_PASS_TRAVEL_ADVISORY_ACTIVE = "travel_advisory_active";
		String MOUNTAIN_PASS_ROAD_CONDITION = "road_condition";
		String MOUNTAIN_PASS_TEMPERATURE = "temperature";
		String MOUNTAIN_PASS_DATE_UPDATED = "date_updated";
		String MOUNTAIN_PASS_RESTRICTION_ONE = "restriction_one";
		String MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION = "restriction_one_direction";
		String MOUNTAIN_PASS_RESTRICTION_TWO = "restriction_two";
		String MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION = "restriction_two_direction";
		String MOUNTAIN_PASS_CAMERA = "camera";
		String MOUNTAIN_PASS_FORECAST = "forecast";
		String MOUNTAIN_PASS_WEATHER_ICON = "weather_icon";
		String MOUNTAIN_PASS_IS_STARRED = "is_starred";
	}	
	
	public static final String CONTENT_AUTHORITY = "gov.wa.wsdot.android.wsdot.provider.WSDOTProvider";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final String PATH_CACHES = "caches";
	private static final String PATH_CAMERAS = "cameras";
	private static final String PATH_HIGHWAY_ALERTS = "highway_alerts";
	private static final String PATH_MOUNTAIN_PASSES = "mountain_passes";
	
	public static class Caches implements BaseColumns, CachesColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CACHES).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/cache";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/cache";

	}	
	
	public static class Cameras implements BaseColumns, CamerasColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAMERAS).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/camera";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/camera";

	}
	
	public static class HighwayAlerts implements BaseColumns, HighwayAlertsColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_HIGHWAY_ALERTS).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/highway_alert";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/highway_alert";		
	}
	
	public static class MountainPasses implements BaseColumns, MountainPassesColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOUNTAIN_PASSES).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/mountain_pass";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/mountain_pass";

	}
	
	private WSDOTContract() {
	}
}
