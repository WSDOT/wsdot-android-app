package gov.wa.wsdot.android.wsdot.util.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static gov.wa.wsdot.android.wsdot.util.network.Status.ERROR;
import static gov.wa.wsdot.android.wsdot.util.network.Status.LOADING;
import static gov.wa.wsdot.android.wsdot.util.network.Status.SUCCESS;

//a generic class that describes a data with a status
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