package gov.wa.wsdot.android.wsdot.migration;

public class SqliteDatabaseStringsHelper {

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
}
