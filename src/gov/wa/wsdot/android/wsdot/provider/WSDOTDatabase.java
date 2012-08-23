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
                + CamerasColumns.CAMERA_ROAD_NAME + " TEXT);");
        
        db.execSQL("CREATE TABLE " + Tables.HIGHWAY_ALERTS + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ HighwayAlertsColumns.HIGHWAY_ALERT_ID + " INTEGER,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_HEADLINE + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_LATITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_LONGITUDE + " REAL,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_CATEGORY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_PRIORITY + " TEXT,"
                + HighwayAlertsColumns.HIGHWAY_ALERT_ROAD_NAME + " TEXT);");

        seedData(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.e(DEBUG_TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.CACHES);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.CAMERAS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.HIGHWAY_ALERTS);
        
        onCreate(db);		
	}
	
	private void seedData(SQLiteDatabase db) {
		db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('cameras', 0);");
		db.execSQL("insert into caches (cache_table_name, cache_last_updated) values ('highway_alerts', 0);");
	}
	
}
