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

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.CachesColumns;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.CamerasColumns;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlertsColumns;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPassesColumns;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class WSDOTDatabase extends SQLiteOpenHelper {

	private static final String DEBUG_TAG = "WSDOTDatabase";
	private static final String DATABASE_NAME = "wsdot.db";
    private static final int DATABASE_VERSION = 1;

    interface Tables {
    	String CACHES = "caches";
        String CAMERAS = "cameras";
        String HIGHWAY_ALERTS = "highway_alerts";
        String MOUNTAIN_PASSES = "mountain_passes";
    }
    
	public WSDOTDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CACHES + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CachesColumns.CACHE_TABLE_NAME + " TEXT,"
                + CachesColumns.CACHE_LAST_UPDATED + " INTEGER);");
		
		db.execSQL("CREATE TABLE " + Tables.CAMERAS + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ CamerasColumns.CAMERA_ID + " INTEGER,"
                + CamerasColumns.CAMERA_TITLE + " TEXT,"
                + CamerasColumns.CAMERA_URL + " TEXT,"
                + CamerasColumns.CAMERA_LATITUDE + " REAL,"
                + CamerasColumns.CAMERA_LONGITUDE + " REAL,"
                + CamerasColumns.CAMERA_HAS_VIDEO + " INTEGER NOT NULL default 0,"
                + CamerasColumns.CAMERA_ROAD_NAME + " TEXT,"
                + CamerasColumns.CAMERA_IS_STARRED + " INTEGER NOT NULL default 0);");
		
        db.execSQL("CREATE TABLE " + Tables.HIGHWAY_ALERTS + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ HighwayAlertsColumns.HIGHWAY_ALERT_ID + " INTEGER,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_HEADLINE + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_LATITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_LONGITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_CATEGORY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_PRIORITY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_ROAD_NAME + " TEXT);");

        db.execSQL("CREATE TABLE " + Tables.MOUNTAIN_PASSES + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MountainPassesColumns.MOUNTAIN_PASS_ID + " INTEGER,"
                + MountainPassesColumns.MOUNTAIN_PASS_NAME + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_CONDITION + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_ELEVATION + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_TRAVEL_ADVISORY_ACTIVE + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_ROAD_CONDITION + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_TEMPERATURE + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_DATE_UPDATED + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_CAMERA + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_FORECAST + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_ICON + " INTEGER,"
                + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED + " INTEGER NOT NULL default 0);");
        
        seedData(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(DEBUG_TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.CACHES);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.CAMERAS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.HIGHWAY_ALERTS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.MOUNTAIN_PASSES);
        
        onCreate(db);		
	}
	
	private void seedData(SQLiteDatabase db) {
		db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('cameras', 0);");
		db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('highway_alerts', 0);");
		db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('mountain_passes', 0);");
		
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (1, 'Blewett Pass US 97', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (2, 'Cayuse Pass SR 123', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (3, 'Chinook Pass SR 410', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (5, 'Crystal to Greenwater SR 410', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (15, 'Disautel Pass SR 155', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (14, 'Loup Loup Pass SR 20', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (13, 'Manastash Ridge I-82', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (6, 'Mt. Baker Hwy SR 542', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (7, 'North Cascade Hwy SR 20', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (8, 'Satus Pass US 97', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (9, 'Sherman Pass SR 20', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (11, 'Snoqualmie Pass I-90', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (10, 'Stevens Pass US 2', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (16, 'Wauconda Pass SR 20', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
		db.execSQL("insert into mountain_passes (id, name, weather_condition, elevation, travel_advisory_active, road_condition, temperature, date_updated, restriction_one, restriction_one_direction, restriction_two, restriction_two_direction, camera, forecast, is_starred) values (12, 'White Pass US 12', '', '', '', '', '', '', '', '', '', '', '', '', 0, 0);");
	}
	
}
