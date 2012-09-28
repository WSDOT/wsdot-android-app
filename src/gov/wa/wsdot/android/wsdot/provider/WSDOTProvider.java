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

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Caches;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.provider.WSDOTDatabase.Tables;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class WSDOTProvider extends ContentProvider {

	private static final String DEBUG_TAG = "WSDOTProvider";
	private WSDOTDatabase mDb;
	
    private static final UriMatcher sUriMatcher = buildUriMatcher();	

    private static final int CACHES = 100;
    private static final int CACHES_ID = 101;
    
    private static final int CAMERAS = 200;
    private static final int CAMERAS_ID = 201;
    
    private static final int HIGHWAY_ALERTS = 300;
    private static final int HIGHWAY_ALERTS_ID = 301;

    private static final int MOUNTAIN_PASSES = 400;
    private static final int MOUNTAIN_PASSES_ID = 401;
    
    private static final int TRAVEL_TIMES = 500;
    private static final int TRAVEL_TIMES_ID = 501;
    
    private static final int FERRIES_SCHEDULES = 600;
    private static final int FERRIES_SCHEDULES_ID = 601;
    
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WSDOTContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, "caches", CACHES);
        matcher.addURI(authority, "caches/#", CACHES_ID);
        matcher.addURI(authority, "cameras", CAMERAS);
        matcher.addURI(authority, "cameras/#", CAMERAS_ID);
        matcher.addURI(authority, "highway_alerts", HIGHWAY_ALERTS);
        matcher.addURI(authority, "highway_alerts/#", HIGHWAY_ALERTS_ID);
        matcher.addURI(authority, "mountain_passes", MOUNTAIN_PASSES);
        matcher.addURI(authority, "mountain_passes/#", MOUNTAIN_PASSES_ID);
        matcher.addURI(authority, "travel_times", TRAVEL_TIMES);
        matcher.addURI(authority, "travel_times/#", TRAVEL_TIMES_ID);
        matcher.addURI(authority, "ferries_schedules", FERRIES_SCHEDULES);
        matcher.addURI(authority, "ferries_schedules/#", FERRIES_SCHEDULES_ID);
        
        return matcher;
	}

	@Override
	public boolean onCreate() {
		mDb = new WSDOTDatabase(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
        case CACHES:
            return Caches.CONTENT_TYPE;
        case CACHES_ID:
            return Caches.CONTENT_ITEM_TYPE;
        case CAMERAS:
            return Cameras.CONTENT_TYPE;
        case CAMERAS_ID:
            return Cameras.CONTENT_ITEM_TYPE;
        case HIGHWAY_ALERTS:
            return HighwayAlerts.CONTENT_TYPE;
        case HIGHWAY_ALERTS_ID:
            return HighwayAlerts.CONTENT_ITEM_TYPE;
        case MOUNTAIN_PASSES:
            return MountainPasses.CONTENT_TYPE;
        case MOUNTAIN_PASSES_ID:
            return MountainPasses.CONTENT_ITEM_TYPE;
        case TRAVEL_TIMES:
        	return TravelTimes.CONTENT_TYPE;
        case TRAVEL_TIMES_ID:
        	return TravelTimes.CONTENT_ITEM_TYPE;
        case FERRIES_SCHEDULES:
        	return FerriesSchedules.CONTENT_TYPE;
        case FERRIES_SCHEDULES_ID:
        	return FerriesSchedules.CONTENT_ITEM_TYPE;
        default:
        	throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs,	String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	
    	final int match = sUriMatcher.match(uri);
    	
    	switch (match) {
	    case CACHES:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.CACHES);
	    	// no filter
	        break;
	    case CACHES_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.CACHES);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;
	    case CAMERAS:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.CAMERAS);
	    	// no filter
	        break;
	    case CAMERAS_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.CAMERAS);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;
	    case HIGHWAY_ALERTS:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.HIGHWAY_ALERTS);
	    	// no filter
	        break;
	    case HIGHWAY_ALERTS_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.HIGHWAY_ALERTS);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;	        
	    case MOUNTAIN_PASSES:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.MOUNTAIN_PASSES);
	    	// no filter
	        break;
	    case MOUNTAIN_PASSES_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.MOUNTAIN_PASSES);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;
	    case TRAVEL_TIMES:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.TRAVEL_TIMES);
	    	// no filter
	        break;
	    case TRAVEL_TIMES_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.TRAVEL_TIMES);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;
	    case FERRIES_SCHEDULES:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.FERRIES_SCHEDULES);
	    	// no filter
	        break;
	    case FERRIES_SCHEDULES_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.FERRIES_SCHEDULES);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;
	        
	    default:
	    	throw new IllegalArgumentException("Unknown URI " + uri);
	    }
	 
	    Cursor cursor = queryBuilder.query(mDb.getReadableDatabase(),
	            projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    
	    return cursor;		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        int rowsAffected = 0;
        String id;
        
        switch (uriType) {
        case CACHES:
            rowsAffected = sqlDB.delete(Tables.CACHES, selection, selectionArgs);
            break;
        case CACHES_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.CACHES, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.CACHES,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;
        case CAMERAS:
            rowsAffected = sqlDB.delete(Tables.CAMERAS, selection, selectionArgs);
            break;
        case CAMERAS_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.CAMERAS, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.CAMERAS,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;
        case HIGHWAY_ALERTS:
            rowsAffected = sqlDB.delete(Tables.HIGHWAY_ALERTS, selection, selectionArgs);
            break;
        case HIGHWAY_ALERTS_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.HIGHWAY_ALERTS, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.HIGHWAY_ALERTS,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;            
        case MOUNTAIN_PASSES:
            rowsAffected = sqlDB.delete(Tables.MOUNTAIN_PASSES, selection, selectionArgs);
            break;
        case MOUNTAIN_PASSES_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.MOUNTAIN_PASSES, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.MOUNTAIN_PASSES,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;
        case TRAVEL_TIMES:
            rowsAffected = sqlDB.delete(Tables.TRAVEL_TIMES, selection, selectionArgs);
            break;
        case TRAVEL_TIMES_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.TRAVEL_TIMES, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.TRAVEL_TIMES,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;
        case FERRIES_SCHEDULES:
            rowsAffected = sqlDB.delete(Tables.FERRIES_SCHEDULES, selection, selectionArgs);
            break;
        case FERRIES_SCHEDULES_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.FERRIES_SCHEDULES, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.FERRIES_SCHEDULES,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        int rowsAdded = 0;
        
        switch(uriType) {
        case CAMERAS:
        	sqlDB.beginTransaction();
            try {
            	for (ContentValues value : values) {
	                sqlDB.insert(Tables.CAMERAS, null, value);
            	}
            	sqlDB.setTransactionSuccessful();
            	rowsAdded = values.length;
            } finally {
                getContext().getContentResolver().notifyChange(uri, null);
                sqlDB.endTransaction();
            }
            
            return rowsAdded;
        case HIGHWAY_ALERTS:
        	sqlDB.beginTransaction();
            try {
            	for (ContentValues value : values) {
	                sqlDB.insert(Tables.HIGHWAY_ALERTS, null, value);
            	}
            	sqlDB.setTransactionSuccessful();
            	rowsAdded = values.length;
            } finally {
                getContext().getContentResolver().notifyChange(uri, null);
                sqlDB.endTransaction();
            }
            
            return rowsAdded;
        case MOUNTAIN_PASSES:
        	sqlDB.beginTransaction();
            try {
            	for (ContentValues value : values) {
	                sqlDB.insert(Tables.MOUNTAIN_PASSES, null, value);
            	}
            	sqlDB.setTransactionSuccessful();
            	rowsAdded = values.length;
            } finally {
                getContext().getContentResolver().notifyChange(uri, null);
                sqlDB.endTransaction();
            }
            
            return rowsAdded;        
        case TRAVEL_TIMES:
        	sqlDB.beginTransaction();
            try {
            	for (ContentValues value : values) {
	                sqlDB.insert(Tables.TRAVEL_TIMES, null, value);
            	}
            	sqlDB.setTransactionSuccessful();
            	rowsAdded = values.length;
            } finally {
                getContext().getContentResolver().notifyChange(uri, null);
                sqlDB.endTransaction();
            }
            
            return rowsAdded;
        case FERRIES_SCHEDULES:
        	sqlDB.beginTransaction();
            try {
            	for (ContentValues value : values) {
	                sqlDB.insert(Tables.FERRIES_SCHEDULES, null, value);
            	}
            	sqlDB.setTransactionSuccessful();
            	rowsAdded = values.length;
            } finally {
                getContext().getContentResolver().notifyChange(uri, null);
                sqlDB.endTransaction();
            }
            
            return rowsAdded;
        default:
    		throw new UnsupportedOperationException("Unknown uri: " + uri);
    	}

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        
        switch(uriType) {
        case CACHES:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.CACHES, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
        case CAMERAS:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.CAMERAS, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
        case HIGHWAY_ALERTS:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.HIGHWAY_ALERTS, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
        case MOUNTAIN_PASSES:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.MOUNTAIN_PASSES, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
        case TRAVEL_TIMES:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.TRAVEL_TIMES, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
        case FERRIES_SCHEDULES:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.FERRIES_SCHEDULES, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
        default:
    		throw new UnsupportedOperationException("Unknown uri: " + uri);
    	}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        
        int rowsAffected;
        
        switch(uriType) {
        case CACHES:
        	rowsAffected = sqlDB.update(Tables.CACHES, values, selection, selectionArgs);
        	break;
        case CAMERAS:
        	rowsAffected = sqlDB.update(Tables.CAMERAS, values, selection, selectionArgs);
        	break;
        case MOUNTAIN_PASSES:
        	rowsAffected = sqlDB.update(Tables.MOUNTAIN_PASSES, values, selection, selectionArgs);
        	break;
        case TRAVEL_TIMES:
        	rowsAffected = sqlDB.update(Tables.TRAVEL_TIMES, values, selection, selectionArgs);
        	break;
        case FERRIES_SCHEDULES:
        	rowsAffected = sqlDB.update(Tables.FERRIES_SCHEDULES, values, selection, selectionArgs);
        	break;
        default:
            throw new IllegalArgumentException("Unknown or Invalid URI");
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
	}

}
