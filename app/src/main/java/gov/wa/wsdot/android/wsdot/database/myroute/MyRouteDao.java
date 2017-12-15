package gov.wa.wsdot.android.wsdot.database.myroute;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MyRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertMyRoute(MyRouteEntity myRoute);

    @Query("SELECT * FROM my_route")
    public abstract LiveData<List<MyRouteEntity>> loadMyRoutes();

    @Query("SELECT * FROM my_route WHERE is_starred = 1")
    public abstract LiveData<List<MyRouteEntity>> loadFavoriteMyRoutes();

    @Query("UPDATE my_route SET is_starred = :isStarred WHERE id = :id")
    public abstract void updateIsStarred(Integer id, Integer isStarred);

    @Query("DELETE FROM my_route WHERE id = :id")
    public abstract void deleteCacheTimeFor(Integer id);

}
