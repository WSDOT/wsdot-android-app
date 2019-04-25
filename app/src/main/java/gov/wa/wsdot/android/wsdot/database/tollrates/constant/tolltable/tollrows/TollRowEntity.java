package gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import gov.wa.wsdot.android.wsdot.database.Converters;


@TypeConverters({Converters.class})
public class TollRowEntity {

    @PrimaryKey()
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

    @ColumnInfo(name = "end_time")
    private String endTime = "0:00";

    @ColumnInfo(name = "rows")
    private String[] rowValues;

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

    public String[] getRowValues() {
        return rowValues;
    }

    public void setRowValues(String[] rowValues) {
        this.rowValues = rowValues;
    }
}
