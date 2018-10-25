package gov.wa.wsdot.android.wsdot.database.ferries;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

@Entity(tableName = "weather_report")
public class WeatherReportEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "source")
    private String source;

    @ColumnInfo(name = "temperature")
    private Double temperature;

    @ColumnInfo(name = "wind_speed")
    private Double windSpeed;

    @ColumnInfo(name = "wind_direction")
    private Double windDirection;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "updated")
    private long updated;

}
