package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import gov.wa.wsdot.android.wsdot.database.caches.CacheEntity;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

/**
 *  Abstract class for repos that require network data.
 *
 *  Holds implementation for checking the last cache time for a given
 *  resource specified by the tableName field.
 *
 *  Subclasses will need to implement fetchData - this method should download new
 *  data and update the database. As well as be responsible for updating
 *  the cache time table after success
 *
 */
public abstract class NetworkResourceSyncRepository {

    private static String TAG = NetworkResourceSyncRepository.class.getSimpleName();

    private final AppExecutors appExecutors;
    private final CacheRepository cacheRepository;

    private final long updateInterval;
    private final String tableName;

    NetworkResourceSyncRepository(AppExecutors appExecutors, CacheRepository cacheRepository, long updateInterval, String tableName) {
        this.appExecutors = appExecutors;
        this.cacheRepository = cacheRepository;
        this.updateInterval = updateInterval;
        this.tableName = tableName;
    }

    // NOTE: this method is responsible for updating the cache time after success
    abstract void fetchData(MutableLiveData<ResourceStatus> status) throws Exception;

    // Checks the caches database to see if the last cache time is older than the updateInterval
    public void refreshData(MutableLiveData<ResourceStatus> status, Boolean forceRefresh){
        status.setValue(ResourceStatus.loading());
        appExecutors.diskIO().execute(() -> {
            CacheEntity cache = cacheRepository.getCacheTimeFor(this.tableName);
            long now = System.currentTimeMillis();
            Boolean shouldUpdate = (Math.abs(now - cache.getLastUpdated()) > updateInterval);
            if (shouldUpdate || forceRefresh) {
                try {
                    fetchData(status);
                    status.postValue(ResourceStatus.success());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    status.postValue(ResourceStatus.error("network error"));
                }
            } else{
                status.postValue(ResourceStatus.success());
            }
        });
    }

    // Checks the caches database to see if the last cache time is older than the updateInterval
    public void refreshDataOnSameThread(MutableLiveData<ResourceStatus> status, Boolean forceRefresh){

            status.postValue(ResourceStatus.loading());

            CacheEntity cache = cacheRepository.getCacheTimeFor(this.tableName);
            long now = System.currentTimeMillis();
            Boolean shouldUpdate = (Math.abs(now - cache.getLastUpdated()) > updateInterval);
            if (shouldUpdate || forceRefresh) {
                try {
                    fetchData(status);
                    status.postValue(ResourceStatus.success());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    status.postValue(ResourceStatus.error("network error"));
                }
            } else{
                status.postValue(ResourceStatus.success());
            }

    }

    // Getters for subclasses
    AppExecutors getExecutor(){ return this.appExecutors; }
    CacheRepository getCacheRepository(){ return this.cacheRepository; }
}
