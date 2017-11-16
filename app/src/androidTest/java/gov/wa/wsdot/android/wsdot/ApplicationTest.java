package gov.wa.wsdot.android.wsdot;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.test.ApplicationTestCase;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import gov.wa.wsdot.android.wsdot.database.AppDatabase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

}