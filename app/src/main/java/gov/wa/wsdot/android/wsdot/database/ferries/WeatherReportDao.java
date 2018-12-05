package gov.wa.wsdot.android.wsdot.database.ferries;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class WeatherReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertWeatherReports(WeatherReportEntity... reports);

    @Query("SELECT * FROM weather_report")
    public abstract LiveData<List<WeatherReportEntity>> loadWeatherReports();

    @Query("SELECT * FROM weather_report WHERE updated BETWEEN :endTime AND :startTime")
    public abstract LiveData<List<WeatherReportEntity>> loadWeatherReportsBetween(String startTime, String endTime);

    @Query("DELETE FROM weather_report")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(WeatherReportEntity... reports) {
        deleteAll();
        insertWeatherReports(reports);
    }

}
