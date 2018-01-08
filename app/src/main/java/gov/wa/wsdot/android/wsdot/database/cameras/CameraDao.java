package gov.wa.wsdot.android.wsdot.database.cameras;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class CameraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCameras(CameraEntity... cameras);

    @Query("SELECT * FROM cameras")
    public abstract LiveData<List<CameraEntity>> loadCameras();

    @Query("SELECT * FROM cameras")
    public abstract List<CameraEntity> getCameras();

    @Query("SELECT * FROM cameras WHERE id LIKE :cameraId")
    public abstract LiveData<CameraEntity> loadCamera(Integer cameraId);

    @Query("SELECT * FROM cameras WHERE road_name LIKE :roadName")
    public abstract LiveData<List<CameraEntity>> loadCamerasForRoad(String roadName);

    @Query("SELECT * FROM cameras WHERE is_starred = 1")
    public abstract LiveData<List<CameraEntity>> loadFavoriteCameras();

    @Query("SELECT * FROM cameras WHERE is_starred = 1")
    public abstract List<CameraEntity> getFavoriteCameras();

    @Query("UPDATE cameras SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(Integer id, Integer isStarred);

    @Query("DELETE FROM cameras")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(CameraEntity... cameras) {
        deleteAll();
        insertCameras(cameras);
    }
}
