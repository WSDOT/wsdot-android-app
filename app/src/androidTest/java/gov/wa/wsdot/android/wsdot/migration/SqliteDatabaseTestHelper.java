package gov.wa.wsdot.android.wsdot.migration;
/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.BorderWaitColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.CachesColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.CamerasColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.FerriesSchedulesColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.FerriesTerminalSailingSpaceColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.HighwayAlertsColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.LocationColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.MountainPassesColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.MyRouteColumns;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.Tables;
import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.TravelTimesColumns;


public class SqliteDatabaseTestHelper {

    public static void insertCacheItem(int cacheTime, String tableName, SqliteTestDbOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cache_table_name", tableName);
        values.put("cache_last_updated", cacheTime);
        db.insertWithOnConflict("caches", null, values,
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public static void createTable(SqliteTestDbOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.CACHES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CachesColumns.CACHE_TABLE_NAME + " TEXT,"
                + CachesColumns.CACHE_LAST_UPDATED + " INTEGER);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.CAMERAS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CamerasColumns.CAMERA_ID + " INTEGER,"
                + CamerasColumns.CAMERA_TITLE + " TEXT,"
                + CamerasColumns.CAMERA_URL + " TEXT,"
                + CamerasColumns.CAMERA_LATITUDE + " REAL,"
                + CamerasColumns.CAMERA_LONGITUDE + " REAL,"
                + CamerasColumns.CAMERA_HAS_VIDEO + " INTEGER NOT NULL default 0,"
                + CamerasColumns.CAMERA_ROAD_NAME + " TEXT,"
                + CamerasColumns.CAMERA_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.HIGHWAY_ALERTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_ID + " INTEGER,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_HEADLINE + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_START_LATITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_START_LONGITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_END_LATITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_END_LONGITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_CATEGORY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_PRIORITY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_ROAD_NAME + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_LAST_UPDATED + " TEXT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.MOUNTAIN_PASSES + " ("
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
                + MountainPassesColumns.MOUNTAIN_PASS_LATITUDE + " REAL,"
                + MountainPassesColumns.MOUNTAIN_PASS_LONGITUDE + " REAL,"
                + MountainPassesColumns.MOUNTAIN_PASS_CAMERA + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_FORECAST + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_ICON + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.TRAVEL_TIMES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TravelTimesColumns.TRAVEL_TIMES_ID + " INTEGER,"
                + TravelTimesColumns.TRAVEL_TIMES_TITLE + " TEXT,"
                + TravelTimesColumns.TRAVEL_TIMES_UPDATED + " TEXT,"
                + TravelTimesColumns.TRAVEL_TIMES_DISTANCE + " TEXT,"
                + TravelTimesColumns.TRAVEL_TIMES_AVERAGE + " INTEGER,"
                + TravelTimesColumns.TRAVEL_TIMES_CURRENT + " INTEGER,"
                + TravelTimesColumns.TRAVEL_TIMES_START_LATITUDE + " REAL,"
                + TravelTimesColumns.TRAVEL_TIMES_START_LONGITUDE + " REAL,"
                + TravelTimesColumns.TRAVEL_TIMES_END_LATITUDE + " REAL,"
                + TravelTimesColumns.TRAVEL_TIMES_END_LONGITUDE + " REAL,"
                + TravelTimesColumns.TRAVEL_TIMES_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.FERRIES_SCHEDULES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_ID + " INTEGER,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_TITLE + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_CROSSING_TIME + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_DATE + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_ALERT + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_UPDATED + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.FERRIES_TERMINAL_SAILING_SPACE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_ID + " INTEGER,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_NAME + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_ABBREV + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_DEPARTING_SPACES + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_LAST_UPDATED + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.BORDER_WAIT + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BorderWaitColumns.BORDER_WAIT_ID + " INTEGER,"
                + BorderWaitColumns.BORDER_WAIT_TITLE + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_UPDATED + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_LANE + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_ROUTE + " INTEGER,"
                + BorderWaitColumns.BORDER_WAIT_DIRECTION + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_TIME + " INTEGER,"
                + BorderWaitColumns.BORDER_WAIT_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.MAP_LOCATION + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LocationColumns.LOCATION_ID + " INTEGER,"
                + LocationColumns.LOCATION_TITLE + " TEXT,"
                + LocationColumns.LOCATION_LAT + " INTEGER,"
                + LocationColumns.LOCATION_LONG + " INTEGER,"
                + LocationColumns.LOCATION_ZOOM + " INTEGER);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.MY_ROUTE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MyRouteColumns.MY_ROUTE_ID + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_TITLE + " TEXT, "
                + MyRouteColumns.MY_ROUTE_LOCATIONS + " TEXT, "
                + MyRouteColumns.MY_ROUTE_DISPLAY_LAT + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_DISPLAY_LONG + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_DISPLAY_ZOOM + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_FOUND_FAVORITES + " INTEGER NOT NULL default 0, "
                + MyRouteColumns.MY_ROUTE_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.close();
    }

    public static void clearDatabase(SqliteTestDbOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS caches");
        db.execSQL("DROP TABLE IF EXISTS CameraEntity");
        db.execSQL("DROP TABLE IF EXISTS highway_alerts");
        db.execSQL("DROP TABLE IF EXISTS mountain_passes");
        db.execSQL("DROP TABLE IF EXISTS travel_times");
        db.execSQL("DROP TABLE IF EXISTS ferries_schedules");
        db.execSQL("DROP TABLE IF EXISTS ferries_terminal_sailing_space");
        db.execSQL("DROP TABLE IF EXISTS border_wait");
        db.execSQL("DROP TABLE IF EXISTS map_location");
        db.execSQL("DROP TABLE IF EXISTS my_route");

        db.close();
    }
}

