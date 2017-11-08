package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.support.annotation.MainThread;

/**
 * Callback called when the user was loaded from the repository.
 */
public interface LoadBorderWaitsCallback {

    /**
     * Method called when the user was loaded from the repository.
     *
     * @param borderWaits the user from repository.
     */
    @MainThread
    void onBorderWaitsLoaded(BorderWaitEntity[] borderWaits);

    /**
     * Method called when there was no user in the repository.
     */
    @MainThread
    void onDataNotAvailable();
}