package gov.wa.wsdot.android.wsdot.repository;

import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.wa.wsdot.android.wsdot.database.caches.CacheDao;
import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;

/**
 *  Handles access to the Caches database. The Caches database holds the last
 *  updated times for the numerous types of data we cache.
 */
@Singleton  // informs Dagger that this class should be constructed once
public class CacheRepository {

    private static String TAG = CacheRepository.class.getSimpleName();

    private final CacheDao cacheDao;

    @Inject
    @VisibleForTesting
    public CacheRepository(CacheDao cacheDao) {
        this.cacheDao = cacheDao;
    }

    @WorkerThread
    CacheEntity getCacheTimeFor(String tableName) {
        return cacheDao.loadCacheTimeFor(tableName).get(0);
    }

    @WorkerThread
    public void setCacheTime(CacheEntity cache) {
        cacheDao.deleteAndInsertTransaction(cache, cache.getTableName());
    }
}
