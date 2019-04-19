package gov.wa.wsdot.android.wsdot.database.tollrates.constant;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "toll_rate_table")
public class TollRateTableEntity {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "route")
    private String route = "";




}


/*
    @objc dynamic var route: Int = 0
    @objc dynamic var message: String = ""
    @objc dynamic var numCol: Int = 0

    var tollTable = List<TollRateRowItem>()

    @objc dynamic var delete: Bool = false

    override static func primaryKey() -> String? {
        return "route"
    }


 */