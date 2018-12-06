package gov.wa.wsdot.android.wsdot.util.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import gov.wa.wsdot.android.wsdot.repository.NetworkResourceSyncRepository;

import static gov.wa.wsdot.android.wsdot.util.network.Status.ERROR;
import static gov.wa.wsdot.android.wsdot.util.network.Status.LOADING;
import static gov.wa.wsdot.android.wsdot.util.network.Status.SUCCESS;

/**
 * A generic class that holds the state of a network Resource
 *
 * Typically, view models that are responsible for data fetched
 * from the network should have one of these.
 *
 * Repos that inherit from {@link NetworkResourceSyncRepository NetworkResourceSyncRepository}
 * will use this call to keep the view model up to date on progress.
 */

public class ResourceStatus {

    @NonNull
    public final Status status;
    @Nullable public final String message;

    private ResourceStatus(@NonNull Status status, @Nullable String message) {
        this.status = status;
        this.message = message;
    }

    public static ResourceStatus success() {
        return new ResourceStatus(SUCCESS, null);
    }

    public static ResourceStatus error(String msg) {
        return new ResourceStatus(ERROR, msg);
    }

    public static ResourceStatus loading() {
        return new ResourceStatus(LOADING, null);
    }
}