package gov.wa.wsdot.android.wsdot.shared.livedata;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;

/**
 *  LiveData class that transforms db entity Live data into a displayable
 *  object on a thread.
 *
 *  Used in favor of transformations.map since that method runs on main Thread.
 */
public class CameraItemLiveData extends LiveData<List<CameraItem>>
        implements Observer<List<CameraEntity>> {

    @NonNull
    private LiveData<List<CameraEntity>> sourceLiveData;

    public CameraItemLiveData(@NonNull LiveData<List<CameraEntity>> sourceLiveData) {
        this.sourceLiveData = sourceLiveData;
    }

    @Override protected void onActive()   { sourceLiveData.observeForever(this); }
    @Override protected void onInactive() { sourceLiveData.removeObserver(this); }

    @Override public void onChanged(@Nullable List<CameraEntity> cameras) {
        AsyncTask.execute(() -> postValue(processCameras(cameras)));
    }

    private List<CameraItem> processCameras(List<CameraEntity> cameras) {

        ArrayList<CameraItem> displayableAlertItemValues = new ArrayList<>();
        if (cameras != null) {
            for (CameraEntity camera : cameras) {

                int video = camera.getHasVideo();
                int cameraIcon = (video == 0) ? R.drawable.camera : R.drawable.camera_video;

                displayableAlertItemValues.add(new CameraItem(+
                        camera.getLatitude(),
                        camera.getLongitude(),
                        camera.getTitle(),
                        camera.getUrl(),
                        camera.getCameraId(),
                        cameraIcon
                ));
            }
        }
        return displayableAlertItemValues;

    }
}
