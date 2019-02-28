package gov.wa.wsdot.android.wsdot.migration;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import androidx.room.Room;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import gov.wa.wsdot.android.wsdot.database.AppDatabase;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;

import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_10_11;
import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_11_12;
import static gov.wa.wsdot.android.wsdot.database.AppDatabase.MIGRATION_9_10;
import static gov.wa.wsdot.android.wsdot.migration.SqliteDatabaseTestHelper.insertCacheItem;
import static junit.framework.Assert.assertEquals;

public class MigrationTest {

    private static final String TEST_DB_NAME = "test-db";

    private static final BorderWaitEntity borderWait = new BorderWaitEntity();

    @Rule
    public MigrationTestHelper helper;

    public MigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                AppDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrationFrom9To12_containsCorrectData() throws IOException {

        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB_NAME, 9);

        insertCacheItem(0, "border_wait", db);

        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB_NAME, 12,
                true,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12);

        AppDatabase database = Room.databaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class, TEST_DB_NAME)
                .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .build();
        // Get the latest, migrated, version of the database
        // Check that the correct data is in the database
        List<CacheEntity> dbCache = database.cacheDao().loadCacheTimeFor("border_wait");

        assertEquals(dbCache.get(0).getTableName(), "border_wait");
    }

}
