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

	interface CamerasColumns {
	    String CAMERA_ID = "camera_id";
	    String CAMERA_TITLE = "camera_title";
	    String CAMERA_URL = "camera_url";
	    String CAMERA_LATITUDE = "camera_latitude";
	    String CAMERA_LONGITUDE = "camera_longitude";
	    String CAMERA_HAS_VIDEO = "camera_has_video";
	    String CAMERA_IS_FAVORITE = "camera_is_favorite";
	    String CAMERA_ROAD_NAME = "camera_road_name";
	}
	
	public static final String CONTENT_AUTHORITY = "gov.wa.wsdot.android.wsdot.provider.WSDOTProvider";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final String PATH_CAMERAS = "cameras";
	
	public static class Cameras implements CamerasColumns, BaseColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAMERAS).build();
		
	    public static final String CONTENT_TYPE =
	    		ContentResolver.CURSOR_DIR_BASE_TYPE + "/camera";
	    public static final String CONTENT_ITEM_TYPE =
	    		ContentResolver.CURSOR_ITEM_BASE_TYPE + "/camera";

	}
	
	private WSDOTContract() {
	}
}
