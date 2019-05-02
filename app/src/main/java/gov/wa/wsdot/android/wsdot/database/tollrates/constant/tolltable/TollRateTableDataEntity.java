package gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "toll_rate_table")
public class TollRateTableDataEntity {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "route")
    private int route = -1;

    @ColumnInfo(name = "message")
    @NonNull
    private String message = "";

    @NonNull
    @ColumnInfo(name = "num_col")
    private int numCol = 0;

    public int getRoute() {
        return route;
    }

    public void setRoute(int route) {
        this.route = route;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumCol() {
        return numCol;
    }

    public void setNumCol(int numCol) {
        this.numCol = numCol;
    }
}