package gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.TollRateTableDataEntity;

@Entity(tableName = "toll_rate_table_row",
        foreignKeys = @ForeignKey(entity = TollRateTableDataEntity.class,
            parentColumns = "route",
            childColumns = "route",
            onDelete = ForeignKey.CASCADE))
public class TollRowEntity {

    @PrimaryKey()
    @ColumnInfo(name = "id")
    @NonNull
    private String id = "000_0";

    @ColumnInfo(name = "route")
    private int route = -1;

    @NonNull
    @ColumnInfo(name = "is_header")
    private Boolean isHeader = false;

    @ColumnInfo(name = "is_weekday")
    @NonNull
    private Boolean isWeekday = true;

    @NonNull
    @ColumnInfo(name = "start_time")
    private String startTime = "0:00";

    @NonNull
    @ColumnInfo(name = "end_time")
    private String endTime = "0:00";

    @ColumnInfo(name = "row_values")
    private String rowValues;

    public String getId() {
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }

    public int getRoute() {
        return route;
    }

    public void setRoute(int route) {
        this.route = route;
    }

    @NonNull
    public Boolean getHeader() {
        return isHeader;
    }

    public void setHeader(@NonNull Boolean header) {
        isHeader = header;
    }

    @NonNull
    public Boolean getWeekday() {
        return isWeekday;
    }

    public void setWeekday(@NonNull Boolean weekday) {
        isWeekday = weekday;
    }

    @NonNull
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(@NonNull String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRowValues() {
        return rowValues;
    }

    public void setRowValues(String rowValues) {
        this.rowValues = rowValues;
    }
}
