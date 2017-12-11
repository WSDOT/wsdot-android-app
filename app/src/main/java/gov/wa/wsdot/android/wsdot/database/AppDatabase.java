package gov.wa.wsdot.android.wsdot.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitDao;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.caches.CacheDao;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraDao;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleDao;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryTerminalSailingSpacesDao;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryTerminalSailingSpacesEntity;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertDao;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassDao;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteDao;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationDao;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationEntity;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeDao;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;

@Database(entities = {
        BorderWaitEntity.class,
        CacheEntity.class,
        CameraEntity.class,
        FerryScheduleEntity.class,
        FerryTerminalSailingSpacesEntity.class,
        HighwayAlertEntity.class,
        MountainPassEntity.class,
        MyRouteEntity.class,
        MapLocationEntity.class,
        TravelTimeEntity.class
    }, version = 8)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = AppDatabase.class.getSimpleName();

    private static AppDatabase INSTANCE;

    public abstract BorderWaitDao borderWaitDao();
    public abstract CacheDao cacheDao();
    public abstract CameraDao cameraDao();
    public abstract HighwayAlertDao highwayAlertDao();
    public abstract MountainPassDao mountainPassDao();
    public abstract TravelTimeDao travelTimesDao();
    public abstract FerryScheduleDao ferryScheduleDao();
    public abstract FerryTerminalSailingSpacesDao ferryTerminalSailingSpacesDao();
    public abstract MapLocationDao mapLocationDao();
    public abstract MyRouteDao myRouteDao();

    private static final Object sLock = new Object();

    interface Tables {
        String CACHES = "caches";
        String CAMERAS = "cameras";
        String HIGHWAY_ALERTS = "highway_alerts";
        String MOUNTAIN_PASSES = "mountain_passes";
        String TRAVEL_TIMES = "travel_times";
        String FERRIES_SCHEDULES = "ferries_schedules";
        String FERRIES_TERMINAL_SAILING_SPACE = "ferries_terminal_sailing_space";
        String BORDER_WAIT  = "border_wait";
        String MAP_LOCATION = "map_location";
        String MY_ROUTE = "my_route";
    }

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
        String HIGHWAY_ALERT_START_LATITUDE = "highway_alert_start_latitude";
        String HIGHWAY_ALERT_START_LONGITUDE = "highway_alert_start_longitude";
        String HIGHWAY_ALERT_END_LATITUDE = "highway_alert_end_latitude";
        String HIGHWAY_ALERT_END_LONGITUDE = "highway_alert_end_longitude";
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
        String MOUNTAIN_PASS_LATITUDE = "latitude";
        String MOUNTAIN_PASS_LONGITUDE = "longitude";
        String MOUNTAIN_PASS_IS_STARRED = "is_starred";
    }

    interface TravelTimesColumns {
        String TRAVEL_TIMES_ID = "id";
        String TRAVEL_TIMES_TITLE = "title";
        String TRAVEL_TIMES_UPDATED = "updated";
        String TRAVEL_TIMES_DISTANCE = "distance";
        String TRAVEL_TIMES_AVERAGE = "average";
        String TRAVEL_TIMES_CURRENT = "current";
        String TRAVEL_TIMES_START_LATITUDE = "start_latitude";
        String TRAVEL_TIMES_START_LONGITUDE = "start_longitude";
        String TRAVEL_TIMES_END_LATITUDE = "end_latitude";
        String TRAVEL_TIMES_END_LONGITUDE = "end_longitude";
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
        String MY_ROUTE_LOCATIONS = "route_locations";
        String MY_ROUTE_DISPLAY_LAT = "latitude";
        String MY_ROUTE_DISPLAY_LONG = "longitude";
        String MY_ROUTE_DISPLAY_ZOOM = "zoom";
        String MY_ROUTE_FOUND_FAVORITES = "found_favorites";
        String MY_ROUTE_IS_STARRED = "is_starred";
    }

    @VisibleForTesting
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE " + Tables.HIGHWAY_ALERTS
                    + " ADD COLUMN " + HighwayAlertsColumns.HIGHWAY_ALERT_LAST_UPDATED + " TEXT");
        }
    };

    @VisibleForTesting
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE " + Tables.FERRIES_TERMINAL_SAILING_SPACE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FerriesTerminalSailingSpaceColumns.TERMINAL_ID + " INTEGER,"
                    + FerriesTerminalSailingSpaceColumns.TERMINAL_NAME + " TEXT,"
                    + FerriesTerminalSailingSpaceColumns.TERMINAL_ABBREV + " TEXT,"
                    + FerriesTerminalSailingSpaceColumns.TERMINAL_DEPARTING_SPACES + " TEXT,"
                    + FerriesTerminalSailingSpaceColumns.TERMINAL_LAST_UPDATED + " TEXT,"
                    + FerriesTerminalSailingSpaceColumns.TERMINAL_IS_STARRED + " INTEGER NOT NULL default 0);");

            database.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('ferries_terminal_sailing_space', 0);");
        }
    };

    @VisibleForTesting
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE " + Tables.FERRIES_SCHEDULES
                    + " ADD COLUMN " + FerriesSchedulesColumns.FERRIES_SCHEDULE_CROSSING_TIME + " TEXT");
        }
    };

    @VisibleForTesting
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE " + Tables.MAP_LOCATION + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LocationColumns.LOCATION_ID + " INTEGER,"
                    + LocationColumns.LOCATION_TITLE + " TEXT,"
                    + LocationColumns.LOCATION_LAT + " INTEGER,"
                    + LocationColumns.LOCATION_LONG + " INTEGER,"
                    + LocationColumns.LOCATION_ZOOM + " INTEGER);");
        }
    };

    @VisibleForTesting
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE new_table("
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
                            + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_ICON + " TEXT,"
                            + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED + " INTEGER NOT NULL default 0); "
                            + " INSERT INTO new_table (SELECT "
                            + BaseColumns._ID + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_ID + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_NAME + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_CONDITION + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_ELEVATION + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_TRAVEL_ADVISORY_ACTIVE + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_ROAD_CONDITION + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_TEMPERATURE + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_DATE_UPDATED + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_CAMERA + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_FORECAST + ", "
                            + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED
                            + " FROM " + Tables.MOUNTAIN_PASSES + "); "

                            + " DROP TABLE " + Tables.MOUNTAIN_PASSES + "; "
                            + " ALTER TABLE new_table RENAME TO " + Tables.MOUNTAIN_PASSES + ";"
            );

            database.execSQL("UPDATE " + Tables.CACHES + " SET " + CachesColumns.CACHE_LAST_UPDATED + " = 0 WHERE "
                    + CachesColumns.CACHE_TABLE_NAME + "='" + Tables.MOUNTAIN_PASSES + "' ");

        }
    };

    @VisibleForTesting
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE " + Tables.MY_ROUTE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + MyRouteColumns.MY_ROUTE_ID + " INTEGER,"
                    + MyRouteColumns.MY_ROUTE_TITLE + " TEXT,"
                    + MyRouteColumns.MY_ROUTE_LOCATIONS + " TEXT,"
                    + MyRouteColumns.MY_ROUTE_DISPLAY_LAT + " INTEGER,"
                    + MyRouteColumns.MY_ROUTE_DISPLAY_LONG + " INTEGER,"
                    + MyRouteColumns.MY_ROUTE_DISPLAY_ZOOM + " INTEGER,"
                    + MyRouteColumns.MY_ROUTE_FOUND_FAVORITES + " INTEGER NOT NULL default 0,"
                    + MyRouteColumns.MY_ROUTE_IS_STARRED + " INTEGER NOT NULL default 0);");

            database.execSQL("DROP TABLE " + Tables.HIGHWAY_ALERTS + "; ");
            database.execSQL("CREATE TABLE " + Tables.HIGHWAY_ALERTS + " ("
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


            // Expire cache time
            database.execSQL("UPDATE " + Tables.CACHES + " SET " + CachesColumns.CACHE_LAST_UPDATED + " = 0 WHERE "
                    + CachesColumns.CACHE_TABLE_NAME + "='" + Tables.HIGHWAY_ALERTS + "'; ");

            // Add start & end location fields to travel times
            database.execSQL("CREATE TABLE new_times_table ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TravelTimesColumns.TRAVEL_TIMES_ID + " INTEGER,"
                    + TravelTimesColumns.TRAVEL_TIMES_TITLE + " TEXT,"
                    + TravelTimesColumns.TRAVEL_TIMES_UPDATED + " TEXT,"
                    + TravelTimesColumns.TRAVEL_TIMES_DISTANCE + " TEXT,"
                    + TravelTimesColumns.TRAVEL_TIMES_AVERAGE + " INTEGER,"
                    + TravelTimesColumns.TRAVEL_TIMES_CURRENT + " INTEGER,"
                    + TravelTimesColumns.TRAVEL_TIMES_START_LATITUDE + " REAL default 0,"
                    + TravelTimesColumns.TRAVEL_TIMES_START_LONGITUDE + " REAL default 0,"
                    + TravelTimesColumns.TRAVEL_TIMES_END_LATITUDE + " REAL default 0,"
                    + TravelTimesColumns.TRAVEL_TIMES_END_LONGITUDE + " REAL default 0,"
                    + TravelTimesColumns.TRAVEL_TIMES_IS_STARRED + " INTEGER NOT NULL default 0); ");

            database.execSQL("INSERT INTO new_times_table ("
                    + BaseColumns._ID + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_ID + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_TITLE + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_UPDATED + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_DISTANCE + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_AVERAGE + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_CURRENT + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_IS_STARRED + ") "
                    + " SELECT "
                    + BaseColumns._ID + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_ID + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_TITLE + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_UPDATED + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_DISTANCE + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_AVERAGE + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_CURRENT + ", "
                    + TravelTimesColumns.TRAVEL_TIMES_IS_STARRED
                    + " FROM " + Tables.TRAVEL_TIMES + "; ");

            database.execSQL("DROP TABLE " + Tables.TRAVEL_TIMES + "; ");
            database.execSQL(" ALTER TABLE new_times_table RENAME TO " + Tables.TRAVEL_TIMES + "; ");

            // Expire cache time
            database.execSQL("UPDATE " + Tables.CACHES + " SET " + CachesColumns.CACHE_LAST_UPDATED + " = 0 WHERE "
                    + CachesColumns.CACHE_TABLE_NAME + "='" + Tables.TRAVEL_TIMES + "'; ");

            // Add latitude & longitude to mountain pass table
            database.execSQL("CREATE TABLE new_passes_table("
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
                    + MountainPassesColumns.MOUNTAIN_PASS_LATITUDE + " REAL default 0,"
                    + MountainPassesColumns.MOUNTAIN_PASS_LONGITUDE + " REAL default 0,"
                    + MountainPassesColumns.MOUNTAIN_PASS_CAMERA + " TEXT,"
                    + MountainPassesColumns.MOUNTAIN_PASS_FORECAST + " TEXT,"
                    + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_ICON + " TEXT,"
                    + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED + " INTEGER NOT NULL default 0); ");

            database.execSQL(" INSERT INTO new_passes_table ( "
                    + BaseColumns._ID + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_ID + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_NAME + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_CONDITION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_ELEVATION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_TRAVEL_ADVISORY_ACTIVE + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_ROAD_CONDITION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_TEMPERATURE + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_DATE_UPDATED + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_CAMERA + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_FORECAST + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_ICON + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED + ") "
                    + "SELECT "
                    + BaseColumns._ID + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_ID + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_NAME + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_CONDITION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_ELEVATION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_TRAVEL_ADVISORY_ACTIVE + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_ROAD_CONDITION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_TEMPERATURE + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_DATE_UPDATED + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_ONE_DIRECTION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_RESTRICTION_TWO_DIRECTION + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_CAMERA + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_FORECAST + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_WEATHER_ICON + ", "
                    + MountainPassesColumns.MOUNTAIN_PASS_IS_STARRED
                    + " FROM " + Tables.MOUNTAIN_PASSES + "; ");

            database.execSQL(" DROP TABLE " + Tables.MOUNTAIN_PASSES + "; ");
            database.execSQL(" ALTER TABLE new_passes_table RENAME TO " + Tables.MOUNTAIN_PASSES + "; ");

            database.execSQL("UPDATE " + Tables.CACHES + " SET " + CachesColumns.CACHE_LAST_UPDATED + " = 0 WHERE "
                    + CachesColumns.CACHE_TABLE_NAME + "='" + Tables.MOUNTAIN_PASSES + "'; ");

        }
    };

    @VisibleForTesting
    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since we didn't alter the table, there's nothing else to do here.
        }
    };

    public static AppDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, "wsdot.db")
                        .addMigrations(
                                MIGRATION_1_2,
                                MIGRATION_2_3,
                                MIGRATION_3_4,
                                MIGRATION_4_5,
                                MIGRATION_5_6,
                                MIGRATION_6_7,
                                MIGRATION_7_8)
                        .addCallback(new Callback() {

                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('cameras', 0);");
                                db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('highway_alerts', 0);");
                                db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('mountain_passes', 0);");
                                db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('travel_times', 0);");
                                db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('ferries_schedules', 0);");
                                db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('ferries_terminal_sailing_space', 0);");
                                db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('border_wait', 0);");

                                // Front load the mountain pass cameras

                                // Blewett
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1138, 'US 97: Blewett Pass Summit', 'http://images.wsdot.wa.gov/nc/097vc16375.jpg', 47.334975, -120.578397, 0, 'US 97', 0);");

                                // Cayuse
                                // Chinook
                                // Crystal to Greenwater
                                // Disautel

                                // Loup Loup
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (4030, 'SR 20: Loup Loup Pass (East)', 'http://images.wsdot.wa.gov/nc/020vc21450.jpg', 48.3904, -119.87925, 0, 'SR 20', 0);");

                                // Manastash
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1127, 'Manastash Ridge Summit on I-82 @ MP 7', 'http://images.wsdot.wa.gov/rweather/UMRidge_medium.jpg', 46.89184, -120.43773, 0, 'I-82', 0);");

                                // Mt. Baker Hwy
                                // North Cascade Hwy

                                // Staus
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1137, 'Satus Pass on US 97 @ MP 27', 'http://images.wsdot.wa.gov/sw/097vc02711.jpg', 45.98296, -120.65381, 0, 'US 97', 0);");

                                // Sherman
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1161, 'Sherman Pass on SR-20 @ MP 320', 'http://images.wsdot.wa.gov/rweather/shermanpass_medium.jpg', 48.604742, -118.459912, 0, 'SR 20', 0);");

                                // Snoqualmie
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9433, 'Tinkham Road I-90 @ MP 45.26', 'http://images.wsdot.wa.gov/sc/090VC04526.jpg', 47.395833, -121.532778, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9029, 'Denny Creek on I-90 @ MP46.8', 'http://images.wsdot.wa.gov/sc/090VC04680.jpg', 47.396441, -121.49935, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9426, 'Asahel Curtis on I-90 @ MP48', 'http://images.wsdot.wa.gov/sc/090VC04810.jpg', 47.393333, -121.473333, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9427, 'Rockdale on I-90 @ MP49', 'http://images.wsdot.wa.gov/sc/090VC04938.jpg', 47.396111, -121.454444, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1100, 'Snoqualmie Summit on I-90 @ MP52', 'http://images.wsdot.wa.gov/sc/090VC05200.jpg', 47.428388, -121.419629, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1099, 'Franklin Falls on I-90 @ MP51.3', 'http://images.wsdot.wa.gov/sc/090VC05130.jpg', 47.42246, -121.40991, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9428, 'East Snoqualime Summit on I-90 @ MP53', 'http://images.wsdot.wa.gov/sc/090VC05347.jpg', 47.410833, -121.410278, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1102, 'Hyak on I-90 @ MP55.2', 'http://images.wsdot.wa.gov/sc/090VC05517.jpg', 47.37325, -121.37699, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9434, 'Old Keechelus Snow Shed I-90 @ MP 57.7', 'http://images.wsdot.wa.gov/sc/090VC05771.jpg', 47.357222, -121.367778, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9019, 'Easton Hill on I-90 @ MP67.4', 'http://images.wsdot.wa.gov/sc/090VC06740.jpg', 47.264479, -121.284702, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1103, 'Easton on I-90 @ MP70.6', 'http://images.wsdot.wa.gov/sc/090VC07060.jpg', 47.280581, -121.185882, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (8205, 'West Nelson on I-90 @ MP73.19', 'http://images.wsdot.wa.gov/sc/090VC07319.jpg', 47.217679, -121.128000, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (8094, 'Bullfrog-facing west on I-90 @ MP 79.54', 'http://images.wsdot.wa.gov/rweather/Medium_bullfrog2.jpg', 47.18406, -121.00733, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9028, 'Cle Elum on I-90 @ MP 84.6', 'http://images.wsdot.wa.gov/sc/090VC08460.jpg', 47.215350, -120.937219, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9430, 'Indian John Hill on I-90 @ MP89', 'http://images.wsdot.wa.gov/sc/090VC08940.jpg', 47.162222, -120.848889, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1104, 'Elk Heights on I-90 @ MP 92', 'http://images.wsdot.wa.gov/sc/090VC09212.jpg', 47.132458, -120.809648, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (8201, 'Elk Heights @ I-90 mp 93 looking East', 'http://images.wsdot.wa.gov/sc/090VC09360.jpg', 47.119951, -120.800231, 0, 'I-90', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (1105, 'Rocky Canyon on I-90 @ MP 96.2', 'http://images.wsdot.wa.gov/rweather/rocky_medium.jpg', 47.091975, -120.750985, 0, 'I-90', 0);");

                                // Stevens
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (8083, 'US 2 MP 87 Winton', 'http://images.wsdot.wa.gov/nc/winton.jpg', 47.7497, -120.73673, 0, 'US 2', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9437, 'US 2 MP 63 Big Windy', 'http://images.wsdot.wa.gov/nc/002vc06300.jpg', 47.7461, -121.121215, 0, 'US 2', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9145, 'US 2 MP 62 Old Faithful Avalanche Zone', 'http://images.wsdot.wa.gov/nc/002vc06190.jpg', 47.724431, -121.134085, 0, 'US 2', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (8062, 'US 2 MP 64 Stevens Pass Summit', 'http://images.wsdot.wa.gov/nc/002vc06458.jpg', 47.7513, -121.10619, 0, 'US 2', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (8063, 'US 2 MP 65 Stevens Pass Ski Lodge', 'http://images.wsdot.wa.gov/nc/002vc06430.jpg', 47.7513, -121.10619, 0, 'US 2', 0);");

                                // Wauconda

                                // White
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9463, 'White Pass Summit on US12 @ MP 150.9 - West', 'http://images.wsdot.wa.gov/sc/012vc15093.jpg', 46.637121, -121.393357, 0, 'US 12', 0);");
                                db.execSQL("insert into cameras (id, title, url, latitude, longitude, has_video, road_name, is_starred) values (9464, 'White Pass Summit on US12 @ MP 150.9 - East', 'http://images.wsdot.wa.gov/sc/012vc15095.jpg', 46.637262, -121.392994, 0, 'US 12', 0);");
                            }
                        }).build();
            }
            return INSTANCE;
        }
    }
}

