package gov.wa.wsdot.android.wsdot.database.cameras;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

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

    @Query("SELECT * FROM cameras WHERE id IN(:cameraIds)")
    public abstract LiveData<List<CameraEntity>> loadCamerasForIds(int[] cameraIds);

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
