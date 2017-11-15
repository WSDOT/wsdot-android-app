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

package gov.wa.wsdot.android.wsdot.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WSDOTDatabase extends SQLiteOpenHelper {

	private static final String TAG = WSDOTDatabase.class.getSimpleName();
	private static final String DATABASE_NAME = "wsdot.db";

	private static final int VER_1 = 1;
	private static final int VER_2 = 2;
	private static final int VER_3 = 3;
	private static final int VER_4 = 4;
    private static final int VER_5 = 5;
	private static final int VER_6 = 6;
    private static final int VER_7 = 7;
	private static final int DATABASE_VERSION = VER_7;

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

	public WSDOTDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private void seedData(SQLiteDatabase db) {

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

}