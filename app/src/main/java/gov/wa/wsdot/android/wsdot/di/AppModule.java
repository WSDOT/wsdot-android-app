package gov.wa.wsdot.android.wsdot.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gov.wa.wsdot.android.wsdot.database.AppDatabase;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitDao;
import gov.wa.wsdot.android.wsdot.database.caches.CacheDao;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraDao;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleDao;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryTerminalSailingSpacesDao;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertDao;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassDao;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteDao;
import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationDao;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimesDao;

/**
 *  For Dagger 2. A list of classes passed to the Object Graph that
 *  must be able to inject.
 */
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

    @Singleton @Provides
    CacheDao provideCacheDao(AppDatabase db) {
        return db.cacheDao();
    }

    @Singleton @Provides
    CameraDao provideCameraDao(AppDatabase db) {
        return db.cameraDao();
    }

    @Singleton @Provides
    FerryScheduleDao provideFerryScheduleDao(AppDatabase db) {
        return db.ferryScheduleDao();
    }

    @Singleton @Provides
    FerryTerminalSailingSpacesDao FerryTerminalSailingSpacesDao(AppDatabase db) {
        return db.ferryTerminalSailingSpacesDao();
    }

    @Singleton @Provides
    HighwayAlertDao provideHighwayDao(AppDatabase db) {
        return db.highwayAlertDao();
    }

    @Singleton @Provides
    MountainPassDao provideMountainPassDao(AppDatabase db) {
        return db.mountainPassDao();
    }

    @Singleton @Provides
    MyRouteDao provideMyRouteDao(AppDatabase db) {
        return db.myRouteDao();
    }

    @Singleton @Provides
    MapLocationDao provideMapLocationDao(AppDatabase db) {
        return db.mapLocationDao();
    }

    @Singleton @Provides
    TravelTimesDao provideTravelTimesDao(AppDatabase db) {
        return db.travelTimesDao();
    }
}