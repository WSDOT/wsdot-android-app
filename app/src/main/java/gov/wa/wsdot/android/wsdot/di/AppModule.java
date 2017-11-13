package gov.wa.wsdot.android.wsdot.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gov.wa.wsdot.android.wsdot.database.AppDatabase;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitDao;

@Module(includes = ViewModelModule.class)
class AppModule {

    @Singleton @Provides
    AppDatabase provideDb(Application app) {
        return AppDatabase.getInstance(app);
    }

    @Singleton @Provides
    BorderWaitDao provideBorderWaitDao(AppDatabase db) {
        return db.borderWaitDao();
    }
}