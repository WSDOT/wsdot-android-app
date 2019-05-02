package gov.wa.wsdot.android.wsdot.database.tollrates.dynamic;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.tollratesign.TollRateSignEntity;
import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.tollratesign.tolltrips.TollTripEntity;

/**
 *  POJO used by Room for retrieving a TollRateSntity with all of it's associated TollTripEntity.
 */
public class TollRateGroup {

    @Embedded
    public TollRateSignEntity tollRateSign;
    @Relation(parentColumn = "id", entityColumn = "sign_id", entity = TollTripEntity.class)
    public List<TollTripEntity> trips;

}
