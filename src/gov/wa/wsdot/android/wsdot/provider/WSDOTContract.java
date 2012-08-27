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
	    String CAMERA_ID = "camera_id";
	    String CAMERA_TITLE = "camera_title";
	    String CAMERA_URL = "camera_url";
	    String CAMERA_LATITUDE = "camera_latitude";
	    String CAMERA_LONGITUDE = "camera_longitude";
	    String CAMERA_HAS_VIDEO = "camera_has_video";
	    String CAMERA_ROAD_NAME = "camera_road_name";
	}
	
	interface FavoritesColumns {
		String FAVORITES_ID = "favorites_id";
		String FAVORITES_TABLE_NAME = "favorites_table_name";
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
	
	public static final String CONTENT_AUTHORITY = "gov.wa.wsdot.android.wsdot.provider.WSDOTProvider";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final String PATH_CACHES = "caches";
	private static final String PATH_CAMERAS = "cameras";
	private static final String PATH_FAVORITES = "favorites";
	private static final String PATH_HIGHWAY_ALERTS = "highway_alerts";

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

	public static class Favorites implements BaseColumns, FavoritesColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/favorite";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/favorite";

	}
	
	public static class HighwayAlerts implements BaseColumns, HighwayAlertsColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_HIGHWAY_ALERTS).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/highway_alert";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/highway_alert";		
	}
	
	private WSDOTContract() {
	}
}
