package gov.wa.wsdot.android.wsdot.database.tollrates;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 *  POJO used by Room for retrieving a TollRateSignEntity with all of it's associated TollTripEntity.
 */
public class TollRateGroup {

    @Embedded
    public TollRateSignEntity tollRateSign;
    @Relation(parentColumn = "id", entityColumn = "sign_id", entity = TollTripEntity.class)
    public List<TollTripEntity> trips;

}
