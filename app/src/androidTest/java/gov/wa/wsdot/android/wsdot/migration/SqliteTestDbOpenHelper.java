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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseStringsHelper.*;
/**
 * Helper class for creating the test database version 7 with SQLite.
 */
public class SqliteTestDbOpenHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 7;

    public SqliteTestDbOpenHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

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
                + HighwayAlertsColumns.HIGHWAY_ALERT_START_LATITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_START_LONGITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_END_LATITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_END_LONGITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_CATEGORY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_PRIORITY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_ROAD_NAME + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_LAST_UPDATED + " TEXT);");

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
                + MountainPassesColumns.MOUNTAIN_PASS_LATITUDE + " REAL,"
                + MountainPassesColumns.MOUNTAIN_PASS_LONGITUDE + " REAL,"
                + MountainPassesColumns.MOUNTAIN_PASS_CAMERA + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_FORECAST + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_ICON + " TEXT,"
                + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE " + Tables.TRAVEL_TIMES + " ("
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

        db.execSQL("CREATE TABLE " + Tables.FERRIES_SCHEDULES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_ID + " INTEGER,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_TITLE + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_CROSSING_TIME + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_DATE + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_ALERT + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_UPDATED + " TEXT,"
                + FerriesSchedulesColumns.FERRIES_SCHEDULE_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE " + Tables.FERRIES_TERMINAL_SAILING_SPACE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_ID + " INTEGER,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_NAME + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_ABBREV + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_DEPARTING_SPACES + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_LAST_UPDATED + " TEXT,"
                + FerriesTerminalSailingSpaceColumns.TERMINAL_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE " + Tables.BORDER_WAIT + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BorderWaitColumns.BORDER_WAIT_ID + " INTEGER,"
                + BorderWaitColumns.BORDER_WAIT_TITLE + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_UPDATED + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_LANE + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_ROUTE + " INTEGER,"
                + BorderWaitColumns.BORDER_WAIT_DIRECTION + " TEXT,"
                + BorderWaitColumns.BORDER_WAIT_TIME + " INTEGER,"
                + BorderWaitColumns.BORDER_WAIT_IS_STARRED + " INTEGER NOT NULL default 0);");

        db.execSQL("CREATE TABLE " + Tables.MAP_LOCATION + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LocationColumns.LOCATION_ID + " INTEGER,"
                + LocationColumns.LOCATION_TITLE + " TEXT,"
                + LocationColumns.LOCATION_LAT + " INTEGER,"
                + LocationColumns.LOCATION_LONG + " INTEGER,"
                + LocationColumns.LOCATION_ZOOM + " INTEGER);");

        db.execSQL("CREATE TABLE " + Tables.MY_ROUTE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MyRouteColumns.MY_ROUTE_ID + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_TITLE + " TEXT, "
                + MyRouteColumns.MY_ROUTE_LOCATIONS + " TEXT, "
                + MyRouteColumns.MY_ROUTE_DISPLAY_LAT + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_DISPLAY_LONG + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_DISPLAY_ZOOM + " INTEGER, "
                + MyRouteColumns.MY_ROUTE_FOUND_FAVORITES + " INTEGER NOT NULL default 0, "
                + MyRouteColumns.MY_ROUTE_IS_STARRED + " INTEGER NOT NULL default 0);");

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
