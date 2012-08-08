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

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
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
import android.util.Log;

public class WSDOTProvider extends ContentProvider {

	private static final String DEBUG_TAG = "WSDOTProvider";
	private WSDOTDatabase mDb;
	
    private static final UriMatcher sUriMatcher = buildUriMatcher();	

    private static final int CAMERAS = 100;
    private static final int CAMERAS_ID = 101;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WSDOTContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, Cameras.PATH_CAMERAS, CAMERAS);
        matcher.addURI(authority, Cameras.PATH_CAMERAS + "/#", CAMERAS_ID);
        
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
        case CAMERAS:
            return Cameras.CONTENT_TYPE;
        case CAMERAS_ID:
            return Cameras.CONTENT_ITEM_TYPE;
        default:
        	throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs,	String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setTables(WSDOTDatabase.Tables.CAMERAS);
    	
    	final int match = sUriMatcher.match(uri);
    	
    	switch (match) {
	    case CAMERAS:
	        // no filter
	        break;
	    case CAMERAS_ID:
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        
        if (uriType != CAMERAS) {
            throw new IllegalArgumentException("Invalid URI for insert");
        }
        
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        
        try {
            long newID = sqlDB.insertOrThrow(WSDOTDatabase.Tables.CAMERAS, null, values);
            if (newID > 0) {
                Uri newUri = ContentUris.withAppendedId(uri, newID);
                getContext().getContentResolver().notifyChange(uri, null);
                return newUri;
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }
        } catch (SQLiteConstraintException e) {
            Log.i(DEBUG_TAG, "Ignoring constraint failure.");
        }
        
        return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
