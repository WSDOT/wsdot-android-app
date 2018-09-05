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
    private static final int VER_8 = 8;
    private static final int VER_9 = 9;
    private static final int VER_10 = 10;
    private static final int VER_11 = 11;
    private static final int VER_12 = 12;

	private static final int DATABASE_VERSION = VER_12;

    interface Tables {
        String CACHES = "caches";
        String CAMERAS = "cameras";
        String HIGHWAY_ALERTS = "highway_alerts";
        String MOUNTAIN_PASSES = "mountain_passes";
        String TRAVEL_TIMES = "travel_times";
        String TRAVEL_TIME_TRIPS = "travel_time_trips";
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
	public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

}