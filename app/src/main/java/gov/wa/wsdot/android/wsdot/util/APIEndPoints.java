/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

public class APIEndPoints {

    public static final String WSDOT_API_KEY = "";
    public static final String GOOGLE_API_KEY = "";

    // Traffic
    public static final String CAMERAS = "https://data.wsdot.wa.gov/mobile/Cameras.js.gz";
    public static final String CAMERA_VIDEOS = "https://images.wsdot.wa.gov/nwvideo/";
    public static final String HIGHWAY_ALERTS = "https://data.wsdot.wa.gov/mobile/HighwayAlerts.js";

    public static final String TRAVEL_TIMES = "https://data.wsdot.wa.gov/mobile/TravelTimesv2.js";

    public static final String BORDER_WAITS = "https://data.wsdot.wa.gov/mobile/BorderCrossings.js";
    public static final String TRAVEL_CHARTS = "https://data.wsdot.wa.gov/mobile/travelChartsAndroidv1.js.gz";
    public static final String EXPRESS_LANES = "https://data.wsdot.wa.gov/mobile/ExpressLanes.js";
    public static final String EXPRESS_LANES_WEBSITE = "https://www.wsdot.wa.gov/Northwest/King/ExpressLanes";
    public static final String JBLM_IMAGE = "https://images.wsdot.wa.gov/traffic/flowmaps/jblm.png";

    // Tolling
    public static final String TOLL_RATES = "https://data.wsdot.wa.gov/mobile/StaticTollRates.js";
    public static final String I405_TOLL_RATES = "https://wsdot.com/traffic/api/api/tolling";

    // Ferries
    public static final String FERRY_SCHEDULES = "https://data.wsdot.wa.gov/mobile/WSFRouteSchedules.js.gz";
    public static final String SAILING_SPACES = "https://www.wsdot.wa.gov/ferries/api/terminals/rest/terminalsailingspace";
    public static final String VESSEL_LOCATIONS = "https://www.wsdot.wa.gov/ferries/api/vessels/rest/vessellocations";

    // Passes
    public static final String PASS_CONDITIONS = "https://data.wsdot.wa.gov/mobile/MountainPassConditions.js.gz";

    // Amtrak
    public static final String AMTRAK_SCHEDULE = "https://www.wsdot.wa.gov/traffic/api/amtrak/Schedulerest.svc/GetScheduleAsJson";
    public static final String AMTRAK_WEBSITE = "https://m.amtrak.com";

    // Social Media
    public static final String WSDOT_NEWS = "https://data.wsdot.wa.gov/mobile/News.js";
    public static final String WSDOT_BLOG = "https://www.googleapis.com/blogger/v3/blogs/3323104546148939812/posts";
    public static final String WSDOT_FACEBOOK = "https://www.wsdot.wa.gov/news/socialroom/posts/facebook";
    public static final String WSDOT_TWITTER = "https://www.wsdot.wa.gov/news/socialroom/posts/twitter/";
    public static final String YOUTUBE = "https://www.googleapis.com/youtube/v3/playlistItems";
    public static final String YOUTUBE_WATCH = "https://www.youtube.com/watch";

    // Banner Events
    public static final String EVENT = "https://data.wsdot.wa.gov/mobile/EventStatus.js";

    // Notification Topics
    public static final String FIREBASE_TOPICS =  "https://data.wsdot.wa.gov/mobile/NotificationTopics.js";
    public static final String FIREBASE_TOPICS_VERSION = "https://data.wsdot.wa.gov/mobile/NotificationTopicsVersion.js";
}
