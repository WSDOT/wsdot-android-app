package gov.wa.wsdot.android.wsdot.database.tollrates.constant;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.TollRateTableDataEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows.TollRowEntity;


/**
 *  POJO used by Room for retrieving a TollRateTableDataEntity with all of it's associated TollTableRowEntities.
 */
public class TollRateTable {

    @Embedded
    public TollRateTableDataEntity tollRateTableData;

    @Relation(parentColumn = "route", entityColumn = "route", entity = TollRowEntity.class)
    public List<TollRowEntity> rows;

}

