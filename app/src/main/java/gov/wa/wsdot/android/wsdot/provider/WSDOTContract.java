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
		String HIGHWAY_ALERT_LAST_UPDATED = "highway_alert_last_updated";
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
	
	interface TravelTimesColumns {
		String TRAVEL_TIMES_ID = "id";
		String TRAVEL_TIMES_TITLE = "title";
		String TRAVEL_TIMES_UPDATED = "updated";
		String TRAVEL_TIMES_DISTANCE = "distance";
		String TRAVEL_TIMES_AVERAGE = "average";
		String TRAVEL_TIMES_CURRENT = "current";
		String TRAVEL_TIMES_IS_STARRED = "is_starred";
	}
	
	interface FerriesSchedulesColumns {
		String FERRIES_SCHEDULE_ID = "id";
		String FERRIES_SCHEDULE_TITLE = "title";
        String FERRIES_SCHEDULE_CROSSING_TIME = "crossing_time";
		String FERRIES_SCHEDULE_DATE = "date";
		String FERRIES_SCHEDULE_ALERT = "alert";
		String FERRIES_SCHEDULE_UPDATED = "updated";
		String FERRIES_SCHEDULE_IS_STARRED = "is_starred";
	}
	
	interface FerriesTerminalSailingSpaceColumns {
	    String TERMINAL_ID = "id";
	    String TERMINAL_NAME = "name";
	    String TERMINAL_ABBREV = "abbrev";
	    String TERMINAL_DEPARTING_SPACES = "departing_spaces";
	    String TERMINAL_LAST_UPDATED = "last_updated";
	    String TERMINAL_IS_STARRED = "is_starred";
	}
	
	interface BorderWaitColumns {
		String BORDER_WAIT_ID = "id";
		String BORDER_WAIT_TITLE = "title";
		String BORDER_WAIT_UPDATED = "updated";
		String BORDER_WAIT_LANE = "lane";
		String BORDER_WAIT_ROUTE = "route";
		String BORDER_WAIT_DIRECTION = "direction";
		String BORDER_WAIT_TIME = "wait";
		String BORDER_WAIT_IS_STARRED = "is_starred";
	}

	interface LocationColumns {
		String LOCATION_ID = "id";
		String LOCATION_TITLE = "title";
		String LOCATION_LAT = "latitude";
		String LOCATION_LONG = "longitude";
		String LOCATION_ZOOM = "zoom";
	}

	interface MyRouteColumns {
		String MY_ROUTE_ID = "id";
		String MY_ROUTE_TITLE = "title";

		String MY_ROUTE_LOCATIONS = "locations";

		String MY_ROUTE_DISPLAY_LAT = "latitude";
		String MY_ROUTE_DISPLAY_LONG = "longitude";
		String MY_ROUTE_DISPLAY_ZOOM = "zoom";

		String MY_ROUTE_FOUND_FAVORITES = "found_favorites";

		String MY_ROUTE_IS_STARRED = "is_starred";
	}

	public static final String CONTENT_AUTHORITY = "gov.wa.wsdot.android.wsdot.provider.WSDOTProvider";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final String PATH_CACHES = "caches";
	private static final String PATH_CAMERAS = "cameras";
	private static final String PATH_CAMERAS_ROAD_NAME = "road_name";
	private static final String PATH_HIGHWAY_ALERTS = "highway_alerts";
	private static final String PATH_MOUNTAIN_PASSES = "mountain_passes";
	private static final String PATH_TRAVEL_TIMES = "travel_times";
	private static final String PATH_TRAVEL_TIMES_SEARCH = "search";
	private static final String PATH_FERRIES_SCHEDULES = "ferries_schedules";
	private static final String PATH_FERRIES_TERMINAL_SAILING_SPACE = "ferries_terminal_sailing_space";
	private static final String PATH_BORDER_WAIT = "border_wait";
	private static final String PATH_MAP_LOCATION = "map_location";
	private static final String PATH_MY_ROUTE = "my_route";
	
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
		
		public static final Uri CONTENT_ROAD_NAME_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAMERAS).appendPath(PATH_CAMERAS_ROAD_NAME).build();
		
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
	
	public static class TravelTimes implements BaseColumns, TravelTimesColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAVEL_TIMES).build();
		
		public static final Uri CONTENT_FILTER_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAVEL_TIMES).appendPath(PATH_TRAVEL_TIMES_SEARCH).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/travel_time";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/travel_time";

	}

	public static class FerriesSchedules implements BaseColumns, FerriesSchedulesColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_FERRIES_SCHEDULES).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/ferries_schedule";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/ferries_schedule";

	}
	
	public static class FerriesTerminalSailingSpace implements BaseColumns, FerriesTerminalSailingSpaceColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FERRIES_TERMINAL_SAILING_SPACE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/ferries_terminal_sailing_space";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/ferries_terminal_sailing_space";
	}

	public static class BorderWait implements BaseColumns, BorderWaitColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_BORDER_WAIT).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/border_wait";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/border_wait";

	}

	public static class MapLocation implements BaseColumns, LocationColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_MAP_LOCATION).build();

		public static final String CONTENT_TYPE =
				ContentResolver.CURSOR_DIR_BASE_TYPE + "/map_location";
		public static final String CONTENT_ITEM_TYPE =
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/map_location";
	}

	public static class MyRoute implements BaseColumns, MyRouteColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_MY_ROUTE).build();

		public static final String CONTENT_TYPE =
				ContentResolver.CURSOR_DIR_BASE_TYPE + "/my_route";
		public static final String CONTENT_ITEM_TYPE =
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/my_route";
	}
	
	private WSDOTContract() {
	}
}
