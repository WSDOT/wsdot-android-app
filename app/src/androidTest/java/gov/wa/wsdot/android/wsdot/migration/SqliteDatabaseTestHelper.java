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

import androidx.sqlite.db.SupportSQLiteDatabase;

public class SqliteDatabaseTestHelper {

    public static void insertCacheItem(int cacheTime, String tableName, SupportSQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("cache_table_name", tableName);
        values.put("cache_last_updated", cacheTime);
        db.insert("caches", SQLiteDatabase.CONFLICT_REPLACE, values);
    }

}

