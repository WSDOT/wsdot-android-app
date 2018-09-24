package gov.wa.wsdot.android.wsdot.migration;

import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import gov.wa.wsdot.android.wsdot.database.AppDatabase;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;

import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_10_11;
import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_11_12;
import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_7_8;
import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_8_9;
import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_9_10;
import static gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseTestHelper.insertCacheItem;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MigrationTest {

    private static final String TEST_DB_NAME = "test-db";

    private static final BorderWaitEntity borderWait = new BorderWaitEntity();

    // Helper for creating Room databases and migrations
    @Rule
    public MigrationTestHelper mMigrationTestHelper =
            new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                    AppDatabase.class.getCanonicalName(),
                    new FrameworkSQLiteOpenHelperFactory());

    // Helper for creating SQLite database in version 7
    private SqliteTestDbOpenHelper mSqliteTestDbHelper;

    @Before
    public void setUp() throws Exception {
        // To test migrations from version 7 of the database, we need to create the database
        // with version 7 using SQLite API
        mSqliteTestDbHelper = new SqliteTestDbOpenHelper(InstrumentationRegistry.getTargetContext(),
                TEST_DB_NAME);
        // We're creating the table for every test, to ensure that the table is in the correct state
        SqliteDatabaseTestHelper.createTable(mSqliteTestDbHelper);
    }

    @After
    public void tearDown() throws Exception {
        // Clear the database after every test
        SqliteDatabaseTestHelper.clearDatabase(mSqliteTestDbHelper);
    }

    @Test
    public void migrationFrom7To8_containsCorrectData() throws IOException {

        insertCacheItem(0, "border_wait", mSqliteTestDbHelper);

        // Re-open the database with version 8 and provide MIGRATION_7_8 as the migration process.
        mMigrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 9
                , true,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12);

        // Get the latest, migrated, version of the database
        // Check that the correct data is in the database
        List<CacheEntity> dbCache = getMigratedRoomDatabase().cacheDao().loadCacheTimeFor("border_wait");

        assertEquals(dbCache.get(0).getTableName(), "border_wait");
    }

    private AppDatabase getMigratedRoomDatabase() {
        AppDatabase database = Room.databaseBuilder(InstrumentationRegistry.getTargetContext(),
                AppDatabase.class, TEST_DB_NAME)
                .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .build();
        // close the database and release any stream resources when the test finishes
        mMigrationTestHelper.closeWhenFinished(database);
        return database;
    }
}
