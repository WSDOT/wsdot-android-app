package gov.wa.wsdot.android.wsdot.repository;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

/**
 *  Abstract class for repos that require network data.
 *
 *  Holds implementation for updating status of network calls
 *
 *  Subclasses will need to implement fetchData - this method should download new
 *  data and update the database.
 *
 */
public abstract class NetworkResourceRepository {

    private static String TAG = NetworkResourceRepository.class.getSimpleName();

    private final AppExecutors appExecutors;

    NetworkResourceRepository(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    // NOTE: this method is responsible for updating the cache time after success
    abstract void fetchData(MutableLiveData<ResourceStatus> status) throws Exception;

    // Checks the caches database to see if the last cache time is older than the updateInterval
    public void refreshData(MutableLiveData<ResourceStatus> status){

        appExecutors.networkIO().execute(() -> {
            status.postValue(ResourceStatus.loading());
            try {
                fetchData(status);
                status.postValue(ResourceStatus.success());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                status.postValue(ResourceStatus.error("network error"));
            }
        });
    }

    // Getters for subclasses
    AppExecutors getExecutor(){ return this.appExecutors; }
}
