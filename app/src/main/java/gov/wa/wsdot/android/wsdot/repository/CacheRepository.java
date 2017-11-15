package gov.wa.wsdot.android.wsdot.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.caches.CacheDao;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;

@Singleton  // informs Dagger that this class should be constructed once
public class CacheRepository {

    private static String TAG = CacheRepository.class.getSimpleName();

    private final CacheDao cacheDao;
    private final AppExecutors appExecutors;

    @Inject
    CacheRepository(CacheDao cacheDao, AppExecutors appExecutors) {
        this.cacheDao = cacheDao;
        this.appExecutors = appExecutors;
    }

    // Don't call on main thread! (Should only be used by other repos in their threads.
    CacheEntity getCacheTimeFor(String tableName) {
        return cacheDao.loadCacheTimeFor(tableName).get(0);
    }

    public void setCacheTime(CacheEntity cache) {
        appExecutors.diskIO().execute(() -> {
            cacheDao.insertCache(cache);
        });
    }
}
